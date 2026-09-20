package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameSoundEngine
import com.example.data.AIDifficulty
import com.example.data.CarCatalog
import com.example.data.CarDef
import com.example.data.CarStatusEntity
import com.example.data.GameRepository
import com.example.data.MissionEntity
import com.example.data.PlayerProfileEntity
import com.example.data.RaceMode
import com.example.data.RaceResult
import com.example.data.TrackCatalog
import com.example.data.TrackDef
import com.example.data.TrackRecordEntity
import com.example.data.UpgradeCategory
import com.example.game.CarPhysicsCalculator
import com.example.game.RacingEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    LOADING,
    HOME,
    MODE_SELECT,
    TRACK_SELECT,
    GARAGE,
    MISSIONS,
    DAILY_REWARD,
    SETTINGS,
    RACE,
    RACE_RESULT
}

class GameViewModel(
    application: Application,
    private val repository: GameRepository
) : AndroidViewModel(application) {

    val soundEngine = GameSoundEngine(application)

    private val _currentScreen = MutableStateFlow(AppScreen.LOADING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val profile: StateFlow<PlayerProfileEntity?> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val carsStatus: StateFlow<List<CarStatusEntity>> = repository.allCarsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val trackRecords: StateFlow<List<TrackRecordEntity>> = repository.allTracksFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val missions: StateFlow<List<MissionEntity>> = repository.allMissionsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Race Setup State
    var selectedRaceMode: RaceMode = RaceMode.QUICK_RACE
    var selectedDifficulty: AIDifficulty = AIDifficulty.NORMAL
    var selectedTrackDef: TrackDef = TrackCatalog.ALL_TRACKS.first()
    var currentRaceResult: RaceResult? = null

    private val _activeRacingEngine = MutableStateFlow<RacingEngine?>(null)
    val activeRacingEngine: StateFlow<RacingEngine?> = _activeRacingEngine.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    fun navigateTo(screen: AppScreen) {
        soundEngine.playButtonClick()
        _currentScreen.value = screen
    }

    fun startRace() {
        val currentProfile = profile.value ?: return
        val currentCarId = currentProfile.selectedCarId
        val carDef = CarCatalog.getCar(currentCarId)
        val carStatus = carsStatus.value.find { it.carId == currentCarId }
        val playerColor = carStatus?.selectedColorHex ?: carDef.primaryColor
        val stats = CarPhysicsCalculator.calculateStats(carDef, carStatus)
        val totalLaps = if (selectedRaceMode == RaceMode.ENDLESS) 1 else 3

        soundEngine.soundEnabled = currentProfile.soundEnabled
        soundEngine.musicEnabled = currentProfile.musicEnabled
        soundEngine.vibrationEnabled = currentProfile.vibrationEnabled

        val engine = RacingEngine(
            trackDef = selectedTrackDef,
            playerCarDef = carDef,
            effectiveStats = stats,
            playerColorHex = playerColor,
            raceMode = selectedRaceMode,
            difficulty = selectedDifficulty,
            totalLaps = totalLaps,
            soundEngine = soundEngine
        )

        _activeRacingEngine.value = engine
        _currentScreen.value = AppScreen.RACE
    }

    fun onRaceFinished(result: RaceResult) {
        currentRaceResult = result
        soundEngine.playVictory()

        val engine = _activeRacingEngine.value
        viewModelScope.launch {
            repository.recordRaceCompletion(
                trackId = selectedTrackDef.id,
                position = result.position,
                totalTimeMillis = result.totalTimeMillis,
                bestLapMillis = result.bestLapMillis,
                coinsEarned = result.coinsEarned,
                xpEarned = result.xpEarned,
                gemsEarned = result.gemsEarned,
                distanceTraveledMeters = (engine?.playerDistance ?: 1000f).toInt(),
                usedNitroCount = if (engine?.isNitroActive == true) 1 else 0,
                crashedCount = if (engine?.cameraShake ?: 0f > 10f) 1 else 0,
                coinsCollected = result.coinsCollectedInRace
            )
        }

        _currentScreen.value = AppScreen.RACE_RESULT
    }

    fun selectCar(carId: String) {
        soundEngine.playButtonClick()
        viewModelScope.launch {
            repository.selectCar(carId)
        }
    }

    fun buyCar(carId: String) {
        soundEngine.playVictory()
        viewModelScope.launch {
            repository.buyCar(carId)
        }
    }

    fun upgradeCar(carId: String, category: UpgradeCategory) {
        soundEngine.playButtonClick()
        viewModelScope.launch {
            repository.upgradeCar(carId, category)
        }
    }

    fun setCarColor(carId: String, colorHex: Long) {
        soundEngine.playButtonClick()
        viewModelScope.launch {
            repository.setCarColor(carId, colorHex)
        }
    }

    fun claimMission(missionId: String) {
        val mission = missions.value.find { it.id == missionId } ?: return
        if (mission.isCompleted && !mission.isClaimed) {
            soundEngine.playCoinCollect()
            viewModelScope.launch {
                repository.claimMission(missionId)
            }
        }
    }

    fun claimDailyReward() {
        val currentProfile = profile.value ?: return
        val currentStreak = ((currentProfile.lastDailyRewardDay % 7) + 1)
        soundEngine.playVictory()
        viewModelScope.launch {
            repository.claimDailyReward(currentStreak)
        }
    }

    fun updateProfile(newProfile: PlayerProfileEntity) {
        viewModelScope.launch {
            repository.updateSettings(
                sound = newProfile.soundEnabled,
                music = newProfile.musicEnabled,
                vibration = newProfile.vibrationEnabled,
                sensitivity = newProfile.steeringSensitivity,
                controlType = newProfile.controlType,
                graphics = newProfile.graphicsQuality
            )
        }
    }

    fun resetGameData() {
        soundEngine.playButtonClick()
        viewModelScope.launch {
            repository.resetAllProgress()
            _currentScreen.value = AppScreen.HOME
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }
}
