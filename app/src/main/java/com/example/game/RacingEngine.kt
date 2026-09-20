package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.audio.GameSoundEngine
import com.example.data.AIDifficulty
import com.example.data.CarCatalog
import com.example.data.CarDef
import com.example.data.RaceMode
import com.example.data.TrackCatalog
import com.example.data.TrackDef
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class AICarState(
    val name: String,
    val carDef: CarDef,
    val colorHex: Long,
    var distanceMeters: Float,
    var lateralX: Float,
    var speedMs: Float,
    val maxSpeedMs: Float,
    val accelRate: Float,
    var currentLap: Int = 1,
    var isNitroActive: Boolean = false,
    var nitroTimer: Float = 0f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    var size: Float,
    val color: Color
)

class RacingEngine(
    val trackDef: TrackDef,
    val playerCarDef: CarDef,
    val effectiveStats: CarEffectiveStats,
    val playerColorHex: Long,
    val raceMode: RaceMode,
    val difficulty: AIDifficulty,
    val totalLaps: Int,
    val soundEngine: GameSoundEngine? = null
) {
    val track = TrackGenerator.buildTrack(trackDef)

    // Race progression states
    var countdownValue: Int = 3 // 3, 2, 1, 0 (GO), -1 (racing started)
    var countdownTimer: Float = 1.0f
    var isRacing: Boolean = false
    var isFinished: Boolean = false
    var isPaused: Boolean = false
    var isGameOver: Boolean = false

    // Player state
    var playerDistance: Float = 0f
    var playerLateralX: Float = 0f // -1.0 left edge, 0 center, 1.0 right edge
    var playerSpeedMs: Float = 0f
    var playerNitroTank: Float = 1.0f // 0.0 to 1.0
    var isNitroActive: Boolean = false
    var currentLap: Int = 1
    var lapStartTime: Long = 0L
    var currentLapTimeMillis: Long = 0L
    var bestLapTimeMillis: Long = 0L
    var totalRaceTimeMillis: Long = 0L
    var playerRank: Int = 1

    // Controls input
    var steerInput: Float = 0f // -1.0 to 1.0
    var isAccelerating: Boolean = false
    var isBraking: Boolean = false
    var isNitroRequested: Boolean = false

    // Player statistics in race
    var coinsCollected: Int = 0
    var usedNitroCount: Int = 0
    var crashedCount: Int = 0
    var topSpeedReachedKmh: Float = 0f
    var totalDistanceDriven: Float = 0f

    // Visual & camera dynamics
    var cameraShake: Float = 0f
    var playerTilt: Float = 0f // -15 to +15 deg
    var spinOutTimer: Float = 0f
    var boostPadTimer: Float = 0f
    var checkpointNotification: String? = null
    var checkpointNotificationTimer: Float = 0f

    // AI Racers
    val aiRacers = mutableListOf<AICarState>()

    // Visual particles
    val particles = mutableListOf<Particle>()

    init {
        setupAIRacers()
    }

    private fun setupAIRacers() {
        val aiPool = listOf(
            Triple("Apex Nova", CarCatalog.getCar("viper_gt"), 0xFFE11D48),
            Triple("Shadow Bolt", CarCatalog.getCar("thunderbolt_69"), 0xFFF59E0B),
            Triple("Vortex Blade", CarCatalog.getCar("phantom_rs"), 0xFF8B5CF6)
        )

        val speedMult = difficulty.speedMultiplier
        aiPool.forEachIndexed { idx, (name, car, color) ->
            val aiMaxSpeed = (effectiveStats.topSpeedKmh / 3.6f) * speedMult * (0.94f + idx * 0.03f)
            aiRacers.add(
                AICarState(
                    name = name,
                    carDef = car,
                    colorHex = color,
                    distanceMeters = 15f + (idx * 16f), // AI starts slightly ahead
                    lateralX = if (idx == 0) -0.5f else if (idx == 1) 0.5f else 0.0f,
                    speedMs = 0f,
                    maxSpeedMs = aiMaxSpeed,
                    accelRate = effectiveStats.accelerationRate * (0.85f + idx * 0.05f)
                )
            )
        }
    }

    fun startRace() {
        countdownValue = 3
        countdownTimer = 1.0f
        isRacing = false
        isFinished = false
        isGameOver = false
        soundEngine?.playCountdownBeep(isGo = false)
    }

    fun update(dtSeconds: Float) {
        if (isPaused) return

        // Handle Countdown
        if (countdownValue >= 0) {
            countdownTimer -= dtSeconds
            cameraShake = if (countdownValue == 0) 6f else 2.5f

            if (countdownTimer <= 0f) {
                countdownValue--
                countdownTimer = 1.0f
                if (countdownValue == 0) {
                    soundEngine?.playCountdownBeep(isGo = true)
                } else if (countdownValue > 0) {
                    soundEngine?.playCountdownBeep(isGo = false)
                } else {
                    // Go!
                    isRacing = true
                    lapStartTime = System.currentTimeMillis()
                }
            }
            return
        }

        if (isFinished || isGameOver) return

        // Update Timers
        val now = System.currentTimeMillis()
        currentLapTimeMillis = now - lapStartTime
        totalRaceTimeMillis += (dtSeconds * 1000).toLong()

        if (checkpointNotificationTimer > 0f) {
            checkpointNotificationTimer -= dtSeconds
            if (checkpointNotificationTimer <= 0f) checkpointNotification = null
        }

        if (cameraShake > 0f) {
            cameraShake = max(0f, cameraShake - dtSeconds * 8f)
        }

        if (boostPadTimer > 0f) {
            boostPadTimer -= dtSeconds
        }

        if (spinOutTimer > 0f) {
            spinOutTimer -= dtSeconds
            playerTilt += 720f * dtSeconds
        } else {
            // Normal steering tilt smoothly following input
            val targetTilt = steerInput * 14f
            playerTilt += (targetTilt - playerTilt) * min(1f, dtSeconds * 10f)
        }

        updatePlayerPhysics(dtSeconds)
        updateAIOpponents(dtSeconds)
        updateCollisionsAndPickups()
        updateLeaderboardPosition()
        updateParticles(dtSeconds)
    }

    private fun updatePlayerPhysics(dt: Float) {
        val topSpeedMs = (effectiveStats.topSpeedKmh / 3.6f)

        // Nitro Logic
        if (isNitroRequested && playerNitroTank > 0.05f && playerSpeedMs > 5f) {
            if (!isNitroActive) {
                isNitroActive = true
                usedNitroCount++
                soundEngine?.playNitro()
            }
            playerNitroTank = max(0f, playerNitroTank - (dt / effectiveStats.nitroDurationSec))
            // Emit nitro particles
            spawnNitroParticles()
        } else {
            isNitroActive = false
            // Slow natural recharge when driving cleanly
            if (playerSpeedMs > 15f && playerNitroTank < 1.0f) {
                playerNitroTank = min(1.0f, playerNitroTank + (dt * effectiveStats.nitroRechargeRate * 0.15f))
            }
        }

        // Boost pad active boost
        val boostMultiplier = if (boostPadTimer > 0f) 1.55f else if (isNitroActive) effectiveStats.nitroBoostFactor else 1.0f
        val effectiveTopSpeed = topSpeedMs * boostMultiplier

        // Acceleration / Braking / Friction
        val currentSeg = track.getSegmentAt(playerDistance)
        val isOffroad = abs(playerLateralX) > 1.05f

        val accelForce = if (isAccelerating) {
            effectiveStats.accelerationRate * (if (isNitroActive || boostPadTimer > 0f) 1.8f else 1.0f)
        } else if (isBraking) {
            -effectiveStats.brakingForce
        } else {
            -4.5f // Engine braking
        }

        // Off-road drag penalty
        val offroadDrag = if (isOffroad) -14.0f else 0.0f
        // Hill resistance
        val hillResistance = currentSeg.hill * -1.8f

        playerSpeedMs += (accelForce + offroadDrag + hillResistance) * dt
        playerSpeedMs = playerSpeedMs.coerceIn(0f, effectiveTopSpeed)

        // Steering Physics
        if (spinOutTimer <= 0f && playerSpeedMs > 0.5f) {
            val speedFactor = (playerSpeedMs / topSpeedMs).coerceIn(0.2f, 1.0f)
            val turnRate = effectiveStats.handlingTurnRate * steerInput * speedFactor * dt * 2.2f
            playerLateralX += turnRate

            // Centrifugal curve push (natural road drift on curves!)
            val centrifugalPush = -currentSeg.curve * (playerSpeedMs / topSpeedMs) * 0.95f * dt
            playerLateralX += centrifugalPush

            // Tire smoke when turning hard at speed
            if (abs(steerInput) > 0.7f && playerSpeedMs > 30f) {
                spawnTireSmoke()
            }
        }

        // Clamp lateral position or allow slight shoulder run
        playerLateralX = playerLateralX.coerceIn(-1.75f, 1.75f)

        // Guardrail bump crash
        if (abs(playerLateralX) >= 1.68f && playerSpeedMs > 25f) {
            cameraShake = 5f
            playerSpeedMs *= 0.65f
            playerLateralX = if (playerLateralX > 0) 1.55f else -1.55f
            crashedCount++
            soundEngine?.playCrash()
            spawnSparks()
        }

        // Advance distance
        val deltaDist = playerSpeedMs * dt
        playerDistance += deltaDist
        totalDistanceDriven += deltaDist

        val currentSpeedKmh = playerSpeedMs * 3.6f
        if (currentSpeedKmh > topSpeedReachedKmh) {
            topSpeedReachedKmh = currentSpeedKmh
        }

        // Lap Progression
        if (playerDistance >= track.totalLengthMeters) {
            playerDistance -= track.totalLengthMeters
            onLapCompleted()
        }
    }

    private fun onLapCompleted() {
        val lapTime = currentLapTimeMillis
        if (bestLapTimeMillis == 0L || lapTime < bestLapTimeMillis) {
            bestLapTimeMillis = lapTime
        }

        checkpointNotification = "LAP $currentLap COMPLETED!"
        checkpointNotificationTimer = 2.0f
        soundEngine?.playVictory()

        if (currentLap >= totalLaps) {
            isFinished = true
            soundEngine?.playVictory()
        } else {
            currentLap++
            lapStartTime = System.currentTimeMillis()
        }
    }

    private fun updateAIOpponents(dt: Float) {
        val currentSeg = track.getSegmentAt(playerDistance)

        aiRacers.forEach { ai ->
            // AI acceleration and speed regulation
            val seg = track.getSegmentAt(ai.distanceMeters)
            val curveFactor = 1.0f - (abs(seg.curve) * 0.12f).coerceIn(0f, 0.35f)
            val targetSpeed = ai.maxSpeedMs * curveFactor

            if (ai.speedMs < targetSpeed) {
                ai.speedMs = min(targetSpeed, ai.speedMs + ai.accelRate * dt)
            } else {
                ai.speedMs = max(targetSpeed, ai.speedMs - 6.0f * dt)
            }

            // AI steering logic: follow center road, avoid edges and player
            val targetX = when {
                seg.curve > 1f -> -0.3f // apex inside corner
                seg.curve < -1f -> 0.3f
                else -> 0.0f
            }

            // Overtake player lateral offset if close
            val distToPlayer = ai.distanceMeters - playerDistance
            val adjustedTargetX = if (abs(distToPlayer) < 25f && abs(ai.lateralX - playerLateralX) < 0.4f) {
                if (playerLateralX > 0) -0.55f else 0.55f
            } else {
                targetX
            }

            ai.lateralX += (adjustedTargetX - ai.lateralX) * min(1f, dt * 1.8f)
            ai.distanceMeters += ai.speedMs * dt

            // AI Lap wrap
            if (ai.distanceMeters >= track.totalLengthMeters) {
                ai.distanceMeters -= track.totalLengthMeters
                ai.currentLap++
            }
        }
    }

    private fun updateCollisionsAndPickups() {
        // Player vs Pickups / Obstacles on track
        track.items.forEach { item ->
            if (item.isCollected) return@forEach

            val distDiff = abs(playerDistance - item.trackDistance)
            val wrapsTrack = abs(playerDistance - (item.trackDistance + track.totalLengthMeters)) < 8f ||
                    abs((playerDistance + track.totalLengthMeters) - item.trackDistance) < 8f

            if (distDiff < 7.5f || wrapsTrack) {
                val latDiff = abs(playerLateralX - item.lateralX)

                when (item.type) {
                    SceneryItemType.COIN -> {
                        if (latDiff < 0.45f) {
                            item.isCollected = true
                            coinsCollected++
                            soundEngine?.playCoinCollect()
                            spawnCoinSparkles(playerLateralX)
                        }
                    }
                    SceneryItemType.BOOST_PAD -> {
                        if (latDiff < 0.65f) {
                            boostPadTimer = 2.2f
                            cameraShake = 4f
                            soundEngine?.playNitro()
                            spawnNitroParticles()
                            checkpointNotification = "TURBO BOOST!"
                            checkpointNotificationTimer = 1.2f
                        }
                    }
                    SceneryItemType.OIL_SLICK -> {
                        if (latDiff < 0.48f && spinOutTimer <= 0f) {
                            spinOutTimer = 1.2f
                            playerSpeedMs *= 0.60f
                            cameraShake = 5f
                            soundEngine?.playCrash()
                            checkpointNotification = "SPIN OUT!"
                            checkpointNotificationTimer = 1.2f
                        }
                    }
                    SceneryItemType.BARRIER_CONE -> {
                        if (latDiff < 0.42f) {
                            item.isCollected = true
                            playerSpeedMs = max(10f, playerSpeedMs - 12f)
                            cameraShake = 4.5f
                            crashedCount++
                            soundEngine?.playCrash()
                            spawnSparks()
                        }
                    }
                    SceneryItemType.CHECKPOINT_ARCH -> {
                        if (!item.isCollected) {
                            item.isCollected = true
                            checkpointNotification = "CHECKPOINT!"
                            checkpointNotificationTimer = 1.5f
                            soundEngine?.playCountdownBeep(isGo = true)
                        }
                    }
                    else -> {}
                }
            }
        }

        // Player vs AI Cars
        aiRacers.forEach { ai ->
            val distDelta = abs(playerDistance - ai.distanceMeters)
            if (distDelta < 8.5f) {
                val latDelta = abs(playerLateralX - ai.lateralX)
                if (latDelta < 0.42f) {
                    // Collision!
                    cameraShake = 4.5f
                    soundEngine?.playCrash()
                    spawnSparks()

                    // Bump each other apart
                    if (playerLateralX > ai.lateralX) {
                        playerLateralX += 0.25f
                        ai.lateralX -= 0.25f
                    } else {
                        playerLateralX -= 0.25f
                        ai.lateralX += 0.25f
                    }

                    // Equalize / reduce speeds
                    val avgSpeed = (playerSpeedMs + ai.speedMs) * 0.48f
                    playerSpeedMs = max(8f, avgSpeed)
                    ai.speedMs = max(8f, avgSpeed)
                }
            }
        }
    }

    private fun updateLeaderboardPosition() {
        val playerTotalDist = (currentLap - 1) * track.totalLengthMeters + playerDistance
        var rank = 1

        aiRacers.forEach { ai ->
            val aiTotalDist = (ai.currentLap - 1) * track.totalLengthMeters + ai.distanceMeters
            if (aiTotalDist > playerTotalDist) {
                rank++
            }
        }
        playerRank = rank
    }

    private fun updateParticles(dt: Float) {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.alpha -= dt * 2.5f
            if (p.alpha <= 0f) {
                iter.remove()
            }
        }
    }

    private fun spawnNitroParticles() {
        for (i in 0 until 4) {
            particles.add(
                Particle(
                    x = (Math.random() * 40 - 20).toFloat(),
                    y = 30f + (Math.random() * 20).toFloat(),
                    vx = (Math.random() * 60 - 30).toFloat(),
                    vy = (80 + Math.random() * 120).toFloat(),
                    alpha = 1.0f,
                    size = 12f + (Math.random() * 10).toFloat(),
                    color = if (i % 2 == 0) Color(0xFF06B6D4) else Color(0xFF3B82F6)
                )
            )
        }
    }

    private fun spawnTireSmoke() {
        for (i in 0 until 2) {
            particles.add(
                Particle(
                    x = (Math.random() * 50 - 25).toFloat(),
                    y = 20f,
                    vx = (Math.random() * 40 - 20).toFloat(),
                    vy = (30 + Math.random() * 40).toFloat(),
                    alpha = 0.7f,
                    size = 16f,
                    color = Color(0x99E2E8F0)
                )
            )
        }
    }

    private fun spawnSparks() {
        for (i in 0 until 12) {
            particles.add(
                Particle(
                    x = (Math.random() * 60 - 30).toFloat(),
                    y = 10f,
                    vx = (Math.random() * 200 - 100).toFloat(),
                    vy = (-50 - Math.random() * 120).toFloat(),
                    alpha = 1.0f,
                    size = 6f,
                    color = Color(0xFFFBBF24)
                )
            )
        }
    }

    private fun spawnCoinSparkles(atX: Float) {
        for (i in 0 until 8) {
            particles.add(
                Particle(
                    x = atX * 100f + (Math.random() * 40 - 20).toFloat(),
                    y = -40f,
                    vx = (Math.random() * 100 - 50).toFloat(),
                    vy = (-60 - Math.random() * 80).toFloat(),
                    alpha = 1.0f,
                    size = 8f,
                    color = Color(0xFFFACC15)
                )
            )
        }
    }

    fun restartRace() {
        playerDistance = 0f
        playerLateralX = 0f
        playerSpeedMs = 0f
        playerNitroTank = 1.0f
        isNitroActive = false
        currentLap = 1
        currentLapTimeMillis = 0L
        bestLapTimeMillis = 0L
        totalRaceTimeMillis = 0L
        coinsCollected = 0
        usedNitroCount = 0
        crashedCount = 0
        isFinished = false
        isGameOver = false
        isPaused = false
        track.items.forEach { it.isCollected = false }
        aiRacers.clear()
        setupAIRacers()
        startRace()
    }
}
