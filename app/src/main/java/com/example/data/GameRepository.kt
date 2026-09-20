package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class GameRepository(private val dao: GameDao) {

    val profileFlow: Flow<PlayerProfileEntity?> = dao.getPlayerProfile()
    val allCarsFlow: Flow<List<CarStatusEntity>> = dao.getAllCars()
    val allTracksFlow: Flow<List<TrackRecordEntity>> = dao.getAllTracks()
    val allMissionsFlow: Flow<List<MissionEntity>> = dao.getAllMissions()

    suspend fun initializeDefaultsIfNeeded() {
        val currentProfile = dao.getPlayerProfileSync()
        if (currentProfile == null) {
            dao.insertOrUpdateProfile(
                PlayerProfileEntity(
                    id = 1,
                    coins = 3500,
                    gems = 30,
                    xp = 250,
                    level = 1,
                    selectedCarId = "apex_swift",
                    lastDailyRewardDay = 0,
                    lastDailyRewardTimestamp = 0L,
                    soundEnabled = true,
                    musicEnabled = true,
                    vibrationEnabled = true,
                    steeringSensitivity = 1.0f,
                    controlType = "BUTTONS",
                    graphicsQuality = "HIGH"
                )
            )
        }

        // Initialize cars if missing
        val defaultCars = CarCatalog.ALL_CARS.map { car ->
            CarStatusEntity(
                carId = car.id,
                isUnlocked = car.id == "apex_swift",
                engineLevel = 1,
                turboLevel = 1,
                tiresLevel = 1,
                brakesLevel = 1,
                handlingLevel = 1,
                nitroLevel = 1,
                maxSpeedLevel = 1,
                selectedColorHex = car.primaryColor
            )
        }
        dao.insertCarsIfMissing(defaultCars)

        // Initialize tracks if missing
        val defaultTracks = TrackCatalog.ALL_TRACKS.mapIndexed { index, track ->
            TrackRecordEntity(
                trackId = track.id,
                isUnlocked = index == 0, // First track unlocked by default
                bestTimeMillis = 0L,
                bestLapMillis = 0L,
                timesPlayed = 0,
                starsEarned = 0
            )
        }
        dao.insertTracksIfMissing(defaultTracks)

        // Initialize default missions
        val defaultMissions = listOf(
            MissionEntity("m_first_race", "Rookie Debut", "Complete your first race", 0, 1, 1000, 5, 100),
            MissionEntity("m_win_3", "Podium Master", "Win 3 races in 1st place", 0, 3, 2500, 15, 300),
            MissionEntity("m_collect_coins", "Treasure Hunter", "Collect 100 coins on the track", 0, 100, 1500, 10, 200),
            MissionEntity("m_nitro_10", "Speed Demon", "Use Nitro boost 10 times", 0, 10, 1200, 5, 150),
            MissionEntity("m_clean_finish", "Flawless Driver", "Finish a race without crashing", 0, 1, 2000, 10, 250),
            MissionEntity("m_drive_dist", "Endurance Legend", "Drive over 10,000 meters total", 0, 10000, 3000, 20, 500)
        )
        dao.insertMissionsIfMissing(defaultMissions)
    }

    suspend fun selectCar(carId: String) {
        val profile = dao.getPlayerProfileSync() ?: return
        dao.insertOrUpdateProfile(profile.copy(selectedCarId = carId))
    }

    suspend fun buyCar(carId: String): Boolean {
        val carDef = CarCatalog.getCar(carId)
        val profile = dao.getPlayerProfileSync() ?: return false

        if (profile.coins >= carDef.unlockCoins && profile.gems >= carDef.unlockGems) {
            val updatedProfile = profile.copy(
                coins = profile.coins - carDef.unlockCoins,
                gems = profile.gems - carDef.unlockGems,
                selectedCarId = carId
            )
            dao.insertOrUpdateProfile(updatedProfile)
            val currentStatus = dao.getCarSync(carId)
            if (currentStatus != null) {
                dao.insertOrUpdateCar(currentStatus.copy(isUnlocked = true))
            } else {
                dao.insertOrUpdateCar(CarStatusEntity(carId = carId, isUnlocked = true))
            }
            return true
        }
        return false
    }

    suspend fun upgradeCar(carId: String, category: UpgradeCategory): Boolean {
        val profile = dao.getPlayerProfileSync() ?: return false
        val carStatus = dao.getCarSync(carId) ?: return false

        val currentLevel = when (category) {
            UpgradeCategory.ENGINE -> carStatus.engineLevel
            UpgradeCategory.TURBO -> carStatus.turboLevel
            UpgradeCategory.TIRES -> carStatus.tiresLevel
            UpgradeCategory.BRAKES -> carStatus.brakesLevel
            UpgradeCategory.HANDLING -> carStatus.handlingLevel
            UpgradeCategory.NITRO -> carStatus.nitroLevel
            UpgradeCategory.MAX_SPEED -> carStatus.maxSpeedLevel
        }

        if (currentLevel >= 5) return false
        val cost = currentLevel * 800

        if (profile.coins >= cost) {
            dao.insertOrUpdateProfile(profile.copy(coins = profile.coins - cost))
            val updatedCar = when (category) {
                UpgradeCategory.ENGINE -> carStatus.copy(engineLevel = currentLevel + 1)
                UpgradeCategory.TURBO -> carStatus.copy(turboLevel = currentLevel + 1)
                UpgradeCategory.TIRES -> carStatus.copy(tiresLevel = currentLevel + 1)
                UpgradeCategory.BRAKES -> carStatus.copy(brakesLevel = currentLevel + 1)
                UpgradeCategory.HANDLING -> carStatus.copy(handlingLevel = currentLevel + 1)
                UpgradeCategory.NITRO -> carStatus.copy(nitroLevel = currentLevel + 1)
                UpgradeCategory.MAX_SPEED -> carStatus.copy(maxSpeedLevel = currentLevel + 1)
            }
            dao.insertOrUpdateCar(updatedCar)
            return true
        }
        return false
    }

    suspend fun setCarColor(carId: String, colorHex: Long) {
        val carStatus = dao.getCarSync(carId) ?: return
        dao.insertOrUpdateCar(carStatus.copy(selectedColorHex = colorHex))
    }

    suspend fun recordRaceCompletion(
        trackId: String,
        position: Int,
        totalTimeMillis: Long,
        bestLapMillis: Long,
        coinsEarned: Int,
        xpEarned: Int,
        gemsEarned: Int,
        distanceTraveledMeters: Int,
        usedNitroCount: Int,
        crashedCount: Int,
        coinsCollected: Int
    ): RaceResult {
        val profile = dao.getPlayerProfileSync() ?: PlayerProfileEntity()
        val newXp = profile.xp + xpEarned
        val xpPerLevel = 600
        val newLevel = (newXp / xpPerLevel) + 1

        val newCoins = profile.coins + coinsEarned
        val newGems = profile.gems + gemsEarned

        dao.insertOrUpdateProfile(
            profile.copy(
                coins = newCoins,
                gems = newGems,
                xp = newXp,
                level = newLevel,
                completedCareerRaces = if (position <= 3) profile.completedCareerRaces + 1 else profile.completedCareerRaces
            )
        )

        // Track record update
        val existingRecord = dao.getTrackSync(trackId)
        val bestTotal = if (existingRecord != null && existingRecord.bestTimeMillis > 0) {
            minOf(existingRecord.bestTimeMillis, totalTimeMillis)
        } else totalTimeMillis

        val bestLap = if (existingRecord != null && existingRecord.bestLapMillis > 0) {
            minOf(existingRecord.bestLapMillis, bestLapMillis)
        } else bestLapMillis

        val stars = when (position) {
            1 -> 3
            2 -> 2
            3 -> 1
            else -> 0
        }

        dao.insertOrUpdateTrack(
            TrackRecordEntity(
                trackId = trackId,
                isUnlocked = true,
                bestTimeMillis = bestTotal,
                bestLapMillis = bestLap,
                timesPlayed = (existingRecord?.timesPlayed ?: 0) + 1,
                starsEarned = maxOf(existingRecord?.starsEarned ?: 0, stars)
            )
        )

        // Unlock next track if won or level increased
        var unlockedNextTrack = false
        val trackList = TrackCatalog.ALL_TRACKS
        val currentIndex = trackList.indexOfFirst { it.id == trackId }
        if (position <= 3 && currentIndex != -1 && currentIndex + 1 < trackList.size) {
            val nextTrack = trackList[currentIndex + 1]
            dao.insertOrUpdateTrack(
                TrackRecordEntity(
                    trackId = nextTrack.id,
                    isUnlocked = true
                )
            )
            unlockedNextTrack = true
        }

        // Update missions
        updateMissionProgress("m_first_race", 1)
        if (position == 1) updateMissionProgress("m_win_3", 1)
        if (coinsCollected > 0) updateMissionProgress("m_collect_coins", coinsCollected)
        if (usedNitroCount > 0) updateMissionProgress("m_nitro_10", usedNitroCount)
        if (crashedCount == 0 && position <= 3) updateMissionProgress("m_clean_finish", 1)
        if (distanceTraveledMeters > 0) updateMissionProgress("m_drive_dist", distanceTraveledMeters)

        return RaceResult(
            position = position,
            totalRacers = 4,
            totalTimeMillis = totalTimeMillis,
            bestLapMillis = bestLapMillis,
            coinsEarned = coinsEarned,
            xpEarned = xpEarned,
            gemsEarned = gemsEarned,
            coinsCollectedInRace = coinsCollected,
            topSpeedKmh = 278,
            newRecordsBroken = totalTimeMillis <= bestTotal,
            unlockedNextTrack = unlockedNextTrack
        )
    }

    private suspend fun updateMissionProgress(missionId: String, increment: Int) {
        val missions = dao.getAllMissions().firstOrNull() ?: return
        val mission = missions.find { it.id == missionId } ?: return
        if (mission.isCompleted) return

        val newProgress = mission.currentProgress + increment
        val isCompleted = newProgress >= mission.targetProgress
        dao.insertOrUpdateMission(
            mission.copy(
                currentProgress = newProgress,
                isCompleted = isCompleted
            )
        )
    }

    suspend fun claimMission(missionId: String): Boolean {
        val missions = dao.getAllMissions().firstOrNull() ?: return false
        val mission = missions.find { it.id == missionId } ?: return false
        if (!mission.isCompleted || mission.isClaimed) return false

        val profile = dao.getPlayerProfileSync() ?: return false
        dao.insertOrUpdateProfile(
            profile.copy(
                coins = profile.coins + mission.rewardCoins,
                gems = profile.gems + mission.rewardGems,
                xp = profile.xp + mission.rewardXp,
                level = ((profile.xp + mission.rewardXp) / 600) + 1
            )
        )
        dao.insertOrUpdateMission(mission.copy(isClaimed = true))
        return true
    }

    suspend fun claimDailyReward(dayNumber: Int): Boolean {
        val profile = dao.getPlayerProfileSync() ?: return false
        val reward = DailyRewardCatalog.REWARDS.find { it.dayNumber == dayNumber } ?: return false

        dao.insertOrUpdateProfile(
            profile.copy(
                coins = profile.coins + reward.rewardCoins,
                gems = profile.gems + reward.rewardGems,
                lastDailyRewardDay = dayNumber,
                lastDailyRewardTimestamp = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun updateSettings(
        sound: Boolean,
        music: Boolean,
        vibration: Boolean,
        sensitivity: Float,
        controlType: String,
        graphics: String
    ) {
        val profile = dao.getPlayerProfileSync() ?: return
        dao.insertOrUpdateProfile(
            profile.copy(
                soundEnabled = sound,
                musicEnabled = music,
                vibrationEnabled = vibration,
                steeringSensitivity = sensitivity,
                controlType = controlType,
                graphicsQuality = graphics
            )
        )
    }

    suspend fun resetAllProgress() {
        val defaultProfile = PlayerProfileEntity(
            id = 1,
            coins = 2000,
            gems = 20,
            xp = 0,
            level = 1,
            selectedCarId = "apex_swift"
        )
        dao.insertOrUpdateProfile(defaultProfile)

        // Reset cars
        CarCatalog.ALL_CARS.forEach { car ->
            dao.insertOrUpdateCar(
                CarStatusEntity(
                    carId = car.id,
                    isUnlocked = car.id == "apex_swift"
                )
            )
        }

        // Reset tracks
        TrackCatalog.ALL_TRACKS.forEachIndexed { index, track ->
            dao.insertOrUpdateTrack(
                TrackRecordEntity(
                    trackId = track.id,
                    isUnlocked = index == 0
                )
            )
        }
    }
}
