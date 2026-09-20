package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RaceMode
import com.example.data.RaceResult
import com.example.game.AICarState
import com.example.game.RacingEngine
import com.example.game.SceneryItemType
import com.example.ui.components.ArcadeButton
import com.example.ui.components.ButtonColorScheme
import com.example.ui.components.CarPreviewCanvas
import com.example.ui.components.drawRearCar
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardBorder
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanGlow
import com.example.ui.theme.NitroGreen
import com.example.ui.theme.RacingRed
import com.example.ui.theme.RacingRedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurboGold
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RaceGameScreen(
    engine: RacingEngine,
    controlType: String = "BUTTONS",
    steeringSensitivity: Float = 1.0f,
    onRaceFinished: (RaceResult) -> Unit,
    onExitRace: () -> Unit
) {
    // 60 FPS Game Loop using withFrameNanos
    var frameTick by remember { mutableStateOf(0L) }

    LaunchedEffect(engine) {
        engine.startRace()
        var lastTime = System.nanoTime()

        while (true) {
            withFrameNanos { now ->
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                engine.update(dt)
                frameTick = now

                if (engine.isFinished) {
                    val pos = engine.playerRank
                    val bonusCoins = when (pos) {
                        1 -> 2500
                        2 -> 1500
                        3 -> 1000
                        else -> 500
                    }
                    val totalCoins = bonusCoins + (engine.coinsCollected * 20)
                    val xp = when (pos) {
                        1 -> 400
                        2 -> 250
                        3 -> 150
                        else -> 80
                    }
                    val gems = if (pos == 1) 5 else if (pos <= 3) 2 else 0

                    val result = RaceResult(
                        position = pos,
                        totalRacers = 4,
                        totalTimeMillis = engine.totalRaceTimeMillis,
                        bestLapMillis = engine.bestLapTimeMillis,
                        coinsEarned = totalCoins,
                        xpEarned = xp,
                        gemsEarned = gems,
                        coinsCollectedInRace = engine.coinsCollected,
                        topSpeedKmh = engine.topSpeedReachedKmh.toInt(),
                        newRecordsBroken = pos == 1
                    )
                    onRaceFinished(result)
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
    ) {
        val screenW = maxWidth.value
        val screenH = maxHeight.value

        // 3D Perspective Road Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(controlType) {
                    if (controlType == "SWIPE") {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                engine.steerInput = (dragAmount.x / 40f * steeringSensitivity).coerceIn(-1f, 1f)
                            },
                            onDragEnd = {
                                engine.steerInput = 0f
                            }
                        )
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            draw3DRacingWorld(
                engine = engine,
                w = w,
                h = h
            )
        }

        // Speed Lines on extreme speed/nitro
        if (engine.isNitroActive || engine.playerSpeedMs > 65f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height * 0.45f
                for (i in 0 until 16) {
                    val angle = (i * 22.5f) * (Math.PI.toFloat() / 180f)
                    val r1 = size.width * 0.25f + (Math.random() * 40).toFloat()
                    val r2 = size.width * 0.65f + (Math.random() * 60).toFloat()
                    drawLine(
                        color = Color.White.copy(alpha = (0.25f + Math.random() * 0.35f).toFloat()),
                        start = Offset(cx + cos(angle) * r1, cy + sin(angle) * r1),
                        end = Offset(cx + cos(angle) * r2, cy + sin(angle) * r2),
                        strokeWidth = (2f + Math.random() * 3f).toFloat()
                    )
                }
            }
        }

        // HUD OVERLAY (Top bar & Telemetry)
        RaceHUD(
            engine = engine,
            onPauseClick = { engine.isPaused = true },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        // Checkpoint Notification Popup
        AnimatedVisibility(
            visible = engine.checkpointNotification != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 120.dp)
        ) {
            engine.checkpointNotification?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(CutCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(RacingRed, TurboGold)))
                        .border(2.dp, Color.White, CutCornerShape(12.dp))
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // Countdown 3, 2, 1, GO! Display
        if (engine.countdownValue >= 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55000000))
            ) {
                val label = if (engine.countdownValue == 0) "GO!" else "${engine.countdownValue}"
                val color = if (engine.countdownValue == 0) NitroGreen else RacingRed
                Text(
                    text = label,
                    color = color,
                    fontWeight = FontWeight.Black,
                    fontSize = if (engine.countdownValue == 0) 84.sp else 96.sp,
                    letterSpacing = 4.sp
                )
            }
        }

        // TOUCH CONTROLS (Bottom area)
        TouchControlsLayer(
            engine = engine,
            controlType = controlType,
            steeringSensitivity = steeringSensitivity,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        )

        // PAUSE MODAL
        if (engine.isPaused) {
            PauseMenuModal(
                onResume = { engine.isPaused = false },
                onRestart = { engine.restartRace() },
                onExit = onExitRace
            )
        }
    }
}

private fun DrawScope.draw3DRacingWorld(
    engine: RacingEngine,
    w: Float,
    h: Float
) {
    val trackDef = engine.trackDef
    val horizonY = h * 0.44f + (engine.cameraShake * (Math.random() * 2 - 1).toFloat())
    val cameraShakeX = engine.cameraShake * (Math.random() * 2 - 1).toFloat()

    // 1. SKY GRADIENT
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(trackDef.skyTopColor), Color(trackDef.skyBottomColor)),
            startY = 0f,
            endY = horizonY
        ),
        topLeft = Offset(0f, 0f),
        size = Size(w, horizonY)
    )

    // 2. PARALLAX HORIZON MOUNTAINS / SKYLINE
    val currentCurve = engine.track.getSegmentAt(engine.playerDistance).curve
    val skylineOffset = (engine.playerDistance * 0.08f + currentCurve * 30f) % w

    drawDistantScenery(
        w = w,
        horizonY = horizonY,
        sceneryType = trackDef.sceneryType,
        offset = skylineOffset
    )

    // 3. GROUND PLANE
    drawRect(
        color = Color(trackDef.groundColor),
        topLeft = Offset(0f, horizonY),
        size = Size(w, h - horizonY)
    )

    // 4. PSEUDO-3D ROAD SEGMENTS
    val totalSegmentsToDraw = 85
    val segmentLength = engine.track.totalLengthMeters / engine.track.segments.size
    val currentSegIdx = ((engine.playerDistance / engine.track.totalLengthMeters) * engine.track.segments.size).toInt()

    var accumulatedCurve = 0f

    // Store projected scanline road geometry for drawing objects on top
    data class ProjectedSegment(
        val screenY: Float,
        val roadW: Float,
        val roadCenterX: Float,
        val scale: Float,
        val segDistance: Float
    )
    val projectedSegments = mutableListOf<ProjectedSegment>()

    for (n in totalSegmentsToDraw downTo 1) {
        val segIdx = (currentSegIdx + n) % engine.track.segments.size
        val seg = engine.track.segments[segIdx]
        accumulatedCurve += seg.curve * 0.35f

        val z = n * 4.2f
        val scale = 1.0f / (z * 0.045f)
        val screenY = horizonY + (1.0f - (1.0f / (1.0f + n * 0.06f))) * (h - horizonY)

        val baseRoadWidth = w * 0.85f
        val roadW = baseRoadWidth * scale

        // Road center shifts with player steering and track curves
        val roadCenterX = (w / 2f) + cameraShakeX + (accumulatedCurve * scale * 25f) - (engine.playerLateralX * roadW * 0.45f)

        projectedSegments.add(
            ProjectedSegment(
                screenY = screenY,
                roadW = roadW,
                roadCenterX = roadCenterX,
                scale = scale,
                segDistance = segIdx * segmentLength
            )
        )
    }

    // Draw road surface from horizon to bottom
    for (i in projectedSegments.indices) {
        val p1 = projectedSegments[i]
        val p2 = if (i + 1 < projectedSegments.size) projectedSegments[i + 1] else {
            ProjectedSegment(h, w * 1.8f, w / 2f - (engine.playerLateralX * w * 0.8f), 1.5f, 0f)
        }

        val isEven = (i + (engine.playerDistance / 10f).toInt()) % 2 == 0

        // Road polygon
        val roadPath = Path().apply {
            moveTo(p1.roadCenterX - p1.roadW / 2f, p1.screenY)
            lineTo(p1.roadCenterX + p1.roadW / 2f, p1.screenY)
            lineTo(p2.roadCenterX + p2.roadW / 2f, p2.screenY)
            lineTo(p2.roadCenterX - p2.roadW / 2f, p2.screenY)
            close()
        }
        val roadColor = if (isEven) Color(trackDef.roadColor) else Color(trackDef.roadColor).copy(alpha = 0.88f)
        drawPath(roadPath, color = roadColor)

        // Red & White Rumble Curbs (Left & Right)
        val curbW1 = p1.roadW * 0.07f
        val curbW2 = p2.roadW * 0.07f
        val curbColor = if (isEven) Color(trackDef.curbColor1) else Color(trackDef.curbColor2)

        // Left curb
        val leftCurbPath = Path().apply {
            moveTo(p1.roadCenterX - p1.roadW / 2f - curbW1, p1.screenY)
            lineTo(p1.roadCenterX - p1.roadW / 2f, p1.screenY)
            lineTo(p2.roadCenterX - p2.roadW / 2f, p2.screenY)
            lineTo(p2.roadCenterX - p2.roadW / 2f - curbW2, p2.screenY)
            close()
        }
        drawPath(leftCurbPath, color = curbColor)

        // Right curb
        val rightCurbPath = Path().apply {
            moveTo(p1.roadCenterX + p1.roadW / 2f, p1.screenY)
            lineTo(p1.roadCenterX + p1.roadW / 2f + curbW1, p1.screenY)
            lineTo(p2.roadCenterX + p2.roadW / 2f + curbW2, p2.screenY)
            lineTo(p2.roadCenterX + p2.roadW / 2f, p2.screenY)
            close()
        }
        drawPath(rightCurbPath, color = curbColor)

        // Center White Dashes
        if (isEven) {
            val dashW1 = p1.roadW * 0.02f
            val dashW2 = p2.roadW * 0.02f
            val dashPath = Path().apply {
                moveTo(p1.roadCenterX - dashW1 / 2f, p1.screenY)
                lineTo(p1.roadCenterX + dashW1 / 2f, p1.screenY)
                lineTo(p2.roadCenterX + dashW2 / 2f, p2.screenY)
                lineTo(p2.roadCenterX - dashW2 / 2f, p2.screenY)
                close()
            }
            drawPath(dashPath, color = Color.White.copy(alpha = 0.85f))
        }
    }

    // 5. DRAW ROADSIDE OBJECTS, PICKUPS, BOOST PADS & CHECKPOINT ARCHES
    engine.track.items.forEach { item ->
        val distAhead = item.trackDistance - engine.playerDistance
        val wrappedDist = if (distAhead < -10f) distAhead + engine.track.totalLengthMeters else distAhead

        if (wrappedDist in 5f..280f && !item.isCollected) {
            val zRatio = (wrappedDist / 280f).coerceIn(0.01f, 1.0f)
            val scale = 1.0f / (1.0f + zRatio * 8f)
            val screenY = horizonY + (1.0f - zRatio) * (h - horizonY) * 0.88f
            val roadW = w * 0.85f * scale
            val accumulated = currentCurve * (1f - zRatio) * 15f
            val roadCenterX = (w / 2f) + cameraShakeX + accumulated - (engine.playerLateralX * roadW * 0.45f)
            val objX = roadCenterX + (item.lateralX * roadW * 0.45f)

            drawTrackItem(
                type = item.type,
                cx = objX,
                cy = screenY,
                scale = scale,
                scenery = trackDef.sceneryType
            )
        }
    }

    // 6. DRAW AI OPPONENT CARS AT THEIR PROPER DEPTH
    engine.aiRacers.forEach { ai ->
        val distAhead = ai.distanceMeters - engine.playerDistance
        val wrappedDist = if (distAhead < -10f) distAhead + engine.track.totalLengthMeters else distAhead

        if (wrappedDist in 3f..240f) {
            val zRatio = (wrappedDist / 240f).coerceIn(0.01f, 1.0f)
            val scale = 1.0f / (1.0f + zRatio * 6f)
            val screenY = horizonY + (1.0f - zRatio) * (h - horizonY) * 0.88f
            val roadW = w * 0.85f * scale
            val accumulated = currentCurve * (1f - zRatio) * 15f
            val roadCenterX = (w / 2f) + cameraShakeX + accumulated - (engine.playerLateralX * roadW * 0.45f)
            val carX = roadCenterX + (ai.lateralX * roadW * 0.45f)

            drawAICarSprite(
                cx = carX,
                cy = screenY,
                scale = scale * 1.35f,
                carColor = Color(ai.colorHex)
            )
        }
    }

    // 7. DRAW PLAYER'S CAR IN FOREGROUND
    val playerCarW = w * 0.36f
    val playerCarH = playerCarW * 0.85f
    val playerCarX = (w / 2f) + cameraShakeX
    val playerCarY = h * 0.80f

    // Render rear view player car with tilt
    drawRearCar(
        w = playerCarW,
        h = playerCarH,
        bodyColor = Color(engine.playerColorHex),
        secondaryColor = Color(engine.playerCarDef.secondaryColor),
        carDef = engine.playerCarDef,
        tilt = engine.playerTilt,
        isNitro = engine.isNitroActive,
        flamePulse = 1.1f
    )

    // 8. PARTICLES (Sparks, Nitro, Smoke)
    engine.particles.forEach { p ->
        drawCircle(
            color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
            radius = p.size,
            center = Offset(playerCarX + p.x, playerCarY + p.y)
        )
    }
}

private fun DrawScope.drawDistantScenery(
    w: Float,
    horizonY: Float,
    sceneryType: String,
    offset: Float
) {
    when (sceneryType) {
        "city", "night_city", "neon" -> {
            // Silhouette Skyscrapers
            val buildingColor = if (sceneryType == "neon") Color(0xFF1E1035) else if (sceneryType == "night_city") Color(0xFF0F172A) else Color(0xFF334155)
            val bWidth = 45f
            for (i in 0 until (w / bWidth).toInt() + 4) {
                val bx = ((i * bWidth) - (offset * 0.4f) % bWidth)
                val bh = 40f + ((i * 37) % 80)
                drawRect(
                    color = buildingColor,
                    topLeft = Offset(bx, horizonY - bh),
                    size = Size(bWidth - 4f, bh)
                )
                // Window glow dots
                if (sceneryType != "city") {
                    val winColor = if (i % 2 == 0) NeonCyan.copy(alpha = 0.6f) else TurboGold.copy(alpha = 0.5f)
                    drawCircle(winColor, 2.5f, Offset(bx + 12f, horizonY - bh + 15f))
                    drawCircle(winColor, 2.5f, Offset(bx + 26f, horizonY - bh + 28f))
                }
            }
        }
        "mountain", "snow" -> {
            // Mountain Peaks
            val mColor = if (sceneryType == "snow") Color(0xFFCBD5E1) else Color(0xFF1E293B)
            val peakPath = Path().apply {
                moveTo(0f, horizonY)
                lineTo(w * 0.25f, horizonY - 95f)
                lineTo(w * 0.55f, horizonY - 40f)
                lineTo(w * 0.85f, horizonY - 110f)
                lineTo(w, horizonY - 30f)
                lineTo(w, horizonY)
                close()
            }
            drawPath(peakPath, color = mColor)
        }
        else -> {
            // Rolling Hills
            drawOval(
                color = Color(0xFF15803D).copy(alpha = 0.7f),
                topLeft = Offset(-w * 0.2f, horizonY - 60f),
                size = Size(w * 0.8f, 100f)
            )
            drawOval(
                color = Color(0xFF166534),
                topLeft = Offset(w * 0.4f, horizonY - 50f),
                size = Size(w * 0.8f, 90f)
            )
        }
    }
}

private fun DrawScope.drawTrackItem(
    type: SceneryItemType,
    cx: Float,
    cy: Float,
    scale: Float,
    scenery: String
) {
    val size = 50f * scale

    when (type) {
        SceneryItemType.COIN -> {
            // Gold Coin
            drawCircle(Color(0xFFEAB308), radius = size * 0.6f, center = Offset(cx, cy))
            drawCircle(Color(0xFFFACC15), radius = size * 0.48f, center = Offset(cx, cy))
            drawCircle(Color.White.copy(alpha = 0.8f), radius = size * 0.18f, center = Offset(cx - size * 0.15f, cy - size * 0.15f))
        }
        SceneryItemType.BOOST_PAD -> {
            // Glowing Neon Chevron Boost Pad
            val padW = size * 2.8f
            val padH = size * 1.2f
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(NitroGreen, Color(0xFF047857))),
                topLeft = Offset(cx - padW / 2f, cy - padH / 2f),
                size = Size(padW, padH),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Chevrons
            val arrowPath = Path().apply {
                moveTo(cx - padW * 0.25f, cy + padH * 0.25f)
                lineTo(cx, cy - padH * 0.25f)
                lineTo(cx + padW * 0.25f, cy + padH * 0.25f)
            }
            drawPath(arrowPath, color = Color.White, style = Stroke(width = 4f * scale))
        }
        SceneryItemType.OIL_SLICK -> {
            // Dark slippery oil puddle
            drawOval(
                color = Color(0xCC09090B),
                topLeft = Offset(cx - size * 1.2f, cy - size * 0.35f),
                size = Size(size * 2.4f, size * 0.7f)
            )
            // Iridescent sheen
            drawOval(
                brush = Brush.linearGradient(listOf(Color(0x66EC4899), Color(0x6606B6D4))),
                topLeft = Offset(cx - size * 0.8f, cy - size * 0.22f),
                size = Size(size * 1.6f, size * 0.44f)
            )
        }
        SceneryItemType.BARRIER_CONE -> {
            // Orange Traffic Cone
            val conePath = Path().apply {
                moveTo(cx, cy - size * 1.1f)
                lineTo(cx + size * 0.55f, cy + size * 0.3f)
                lineTo(cx - size * 0.55f, cy + size * 0.3f)
                close()
            }
            drawPath(conePath, color = Color(0xFFEA580C))
            // White stripe
            drawLine(Color.White, Offset(cx - size * 0.25f, cy - size * 0.2f), Offset(cx + size * 0.25f, cy - size * 0.2f), strokeWidth = 5f * scale)
        }
        SceneryItemType.TREE -> {
            // Tree (Pine or Palm)
            drawRect(Color(0xFF78350F), Offset(cx - size * 0.15f, cy - size * 0.4f), Size(size * 0.3f, size * 1.2f))
            drawCircle(Color(0xFF15803D), radius = size * 1.1f, center = Offset(cx, cy - size * 1.1f))
            drawCircle(Color(0xFF16A34A), radius = size * 0.8f, center = Offset(cx - size * 0.2f, cy - size * 1.3f))
        }
        SceneryItemType.CHECKPOINT_ARCH, SceneryItemType.FINISH_LINE -> {
            // Overhead Floating Arch across road
            val archW = size * 5.5f
            val archH = size * 2.2f
            drawRoundRect(
                color = if (type == SceneryItemType.FINISH_LINE) Color.White else NeonCyan,
                topLeft = Offset(cx - archW / 2f, cy - archH),
                size = Size(archW, size * 0.5f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Checkered blocks on finish line
            if (type == SceneryItemType.FINISH_LINE) {
                for (b in 0 until 10) {
                    if (b % 2 == 0) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(cx - archW / 2f + (b * archW / 10f), cy - archH),
                            size = Size(archW / 10f, size * 0.5f)
                        )
                    }
                }
            }
        }
        SceneryItemType.BRIDGE_ARCH -> {
            val bridgeW = size * 6.5f
            drawRect(
                color = Color(0xFF475569),
                topLeft = Offset(cx - bridgeW / 2f, cy - size * 2.8f),
                size = Size(bridgeW, size * 0.8f)
            )
        }
        SceneryItemType.STREET_LIGHT -> {
            drawLine(Color(0xFF94A3B8), Offset(cx, cy + size * 0.5f), Offset(cx, cy - size * 2.0f), strokeWidth = 3f * scale)
            drawLine(Color(0xFF94A3B8), Offset(cx, cy - size * 2.0f), Offset(cx + size * 0.8f, cy - size * 2.0f), strokeWidth = 3f * scale)
            drawCircle(Color(0xFFFEF08A), radius = 6f * scale, center = Offset(cx + size * 0.8f, cy - size * 1.9f))
        }
        SceneryItemType.BUILDING -> {
            val bW = size * 2.5f
            val bH = size * 4.5f
            drawRect(Color(0xFF1E293B), Offset(cx - bW / 2f, cy - bH), Size(bW, bH))
            drawRect(Color(0xFF0F172A), Offset(cx - bW / 2f + 4f, cy - bH + 4f), Size(bW - 8f, bH - 4f))
        }
        else -> {}
    }
}

private fun DrawScope.drawAICarSprite(
    cx: Float,
    cy: Float,
    scale: Float,
    carColor: Color
) {
    val carW = 60f * scale
    val carH = 45f * scale

    // Shadow
    drawOval(Color(0x55000000), Offset(cx - carW * 0.5f, cy + carH * 0.2f), Size(carW, carH * 0.4f))

    // Tires
    val tireW = carW * 0.18f
    val tireH = carH * 0.45f
    drawRoundRect(Color(0xFF111827), Offset(cx - carW * 0.52f, cy - carH * 0.05f), Size(tireW, tireH), CornerRadius(3f, 3f))
    drawRoundRect(Color(0xFF111827), Offset(cx + carW * 0.34f, cy - carH * 0.05f), Size(tireW, tireH), CornerRadius(3f, 3f))

    // Car Body
    drawRoundRect(
        color = carColor,
        topLeft = Offset(cx - carW * 0.42f, cy - carH * 0.35f),
        size = Size(carW * 0.84f, carH * 0.65f),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )

    // Rear Window
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(cx - carW * 0.28f, cy - carH * 0.30f),
        size = Size(carW * 0.56f, carH * 0.25f),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // Red LED taillights
    drawRoundRect(Color(0xFFFF1744), Offset(cx - carW * 0.38f, cy), Size(carW * 0.22f, carH * 0.12f), CornerRadius(2f, 2f))
    drawRoundRect(Color(0xFFFF1744), Offset(cx + carW * 0.16f, cy), Size(carW * 0.22f, carH * 0.12f), CornerRadius(2f, 2f))
}

@Composable
fun RaceHUD(
    engine: RacingEngine,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speedKmh = (engine.playerSpeedMs * 3.6f).toInt()
    val progress = (engine.playerDistance / engine.track.totalLengthMeters).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Top Row: Position, Time, Laps, Pause
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CutCornerShape(8.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonCardBorder, CutCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "POS ",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${engine.playerRank}",
                    color = if (engine.playerRank == 1) TurboGold else if (engine.playerRank == 2) Color(0xFFE2E8F0) else TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = " / 4",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Lap & Timer Display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (engine.raceMode == RaceMode.ENDLESS) "ENDLESS HIGHWAY" else "LAP ${engine.currentLap}/${engine.totalLaps}",
                    color = TurboGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                val totalSecs = (engine.totalRaceTimeMillis / 1000).toInt()
                val millis = (engine.totalRaceTimeMillis % 1000) / 10
                val mins = totalSecs / 60
                val secs = totalSecs % 60
                Text(
                    text = String.format("%02d:%02d.%02d", mins, secs, millis),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Coins & Pause button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CarbonCard)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = TurboGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${engine.coinsCollected}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .testTag("btn_pause_race")
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CarbonCard)
                        .border(1.dp, CarbonCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Mini Track Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(Brush.horizontalGradient(listOf(RacingRed, NeonCyan)))
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Digital Speedometer & Nitro Meter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Speedometer Digital Display
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .clip(CutCornerShape(8.dp))
                    .background(CarbonCard.copy(alpha = 0.85f))
                    .border(1.dp, CarbonCardBorder, CutCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$speedKmh",
                    color = if (speedKmh > 230) RacingRed else TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text(
                        text = "KM/H",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "GEAR ${minOf(6, (speedKmh / 45) + 1)}",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Nitro Gauge
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .clip(CutCornerShape(8.dp))
                    .background(CarbonCard.copy(alpha = 0.85f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), CutCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Nitro",
                        tint = if (engine.isNitroActive) NeonCyanGlow else NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "NITRO ${(engine.playerNitroTank * 100).toInt()}%",
                        color = if (engine.isNitroActive) NeonCyanGlow else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(engine.playerNitroTank)
                            .background(Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF38BDF8))))
                    )
                }
            }
        }
    }
}

@Composable
fun TouchControlsLayer(
    engine: RacingEngine,
    controlType: String,
    steeringSensitivity: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // LEFT & RIGHT STEERING BUTTONS
        if (controlType == "BUTTONS") {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // Steer Left Button
                TouchButton(
                    icon = Icons.Default.ChevronLeft,
                    testTag = "btn_steer_left",
                    sizeDp = 64,
                    color = CarbonSurface,
                    borderColor = CarbonCardBorder,
                    onPressStateChange = { isPressed ->
                        engine.steerInput = if (isPressed) -1.0f * steeringSensitivity else 0f
                    }
                )

                // Steer Right Button
                TouchButton(
                    icon = Icons.Default.ChevronRight,
                    testTag = "btn_steer_right",
                    sizeDp = 64,
                    color = CarbonSurface,
                    borderColor = CarbonCardBorder,
                    onPressStateChange = { isPressed ->
                        engine.steerInput = if (isPressed) 1.0f * steeringSensitivity else 0f
                    }
                )
            }
        } else {
            // Swipe / Tilt indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CutCornerShape(8.dp))
                    .background(CarbonCard.copy(alpha = 0.7f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "DRAG TO STEER",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ACCELERATOR, BRAKE & NITRO BUTTONS (Right side)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Brake Pedal Button
            TouchButton(
                label = "BRAKE",
                testTag = "btn_brake",
                sizeDp = 58,
                color = Color(0xFF3F1B24),
                borderColor = RacingRedDark,
                onPressStateChange = { isPressed ->
                    engine.isBraking = isPressed
                }
            )

            // Nitro Boost Button
            TouchButton(
                icon = Icons.Default.Bolt,
                label = "NITRO",
                testTag = "btn_nitro",
                sizeDp = 64,
                color = Color(0xFF0E3A4B),
                borderColor = NeonCyan,
                glowEffect = true,
                onPressStateChange = { isPressed ->
                    engine.isNitroRequested = isPressed
                }
            )

            // Gas Pedal Button
            TouchButton(
                label = "GAS",
                testTag = "btn_accelerate",
                sizeDp = 72,
                color = RacingRed,
                borderColor = Color(0xFFF43F5E),
                onPressStateChange = { isPressed ->
                    engine.isAccelerating = isPressed
                }
            )
        }
    }
}

@Composable
fun TouchButton(
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    sizeDp: Int = 60,
    color: Color = CarbonCard,
    borderColor: Color = CarbonCardBorder,
    glowEffect: Boolean = false,
    testTag: String = "",
    onPressStateChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        onPressStateChange(isPressed)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag(testTag)
            .size(sizeDp.dp)
            .clip(CutCornerShape(12.dp))
            .background(if (isPressed) color.copy(alpha = 0.6f) else color)
            .border(
                width = if (isPressed) 2.5.dp else 1.5.dp,
                color = if (isPressed) Color.White else borderColor,
                shape = CutCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (glowEffect) NeonCyanGlow else Color.White,
                    modifier = Modifier.size((sizeDp * 0.45f).dp)
                )
            }
            if (label != null) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = if (sizeDp > 60) 12.sp else 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun PauseMenuModal(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(320.dp)
                .clip(CutCornerShape(16.dp))
                .background(CarbonCard)
                .border(2.dp, CarbonCardBorder, CutCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "RACE PAUSED",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            ArcadeButton(
                text = "Resume",
                icon = Icons.Default.PlayArrow,
                colorScheme = ButtonColorScheme.PRIMARY,
                isLarge = true,
                onClick = onResume,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            ArcadeButton(
                text = "Restart Race",
                icon = Icons.Default.Refresh,
                colorScheme = ButtonColorScheme.DARK,
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            ArcadeButton(
                text = "Exit to Menu",
                icon = Icons.Default.Close,
                colorScheme = ButtonColorScheme.DARK,
                onClick = onExit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
