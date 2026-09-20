package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CarCatalog
import com.example.ui.components.CarPreviewCanvas
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanGlow
import com.example.ui.theme.RacingRed
import com.example.ui.theme.RacingRedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TurboGold
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (progress < 1.0f) {
            delay(35)
            progress += 0.022f
        }
        delay(200)
        onLoadingComplete()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "loading_anim")
    val roadOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "road_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        CarbonDark,
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background animated speed grid lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.6f
            for (i in -6..6) {
                val x = cx + (i * 70f)
                drawLine(
                    color = NeonCyan.copy(alpha = 0.15f),
                    start = Offset(cx, cy - 80f),
                    end = Offset(x * 1.5f, size.height),
                    strokeWidth = 2f
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Turbo Rush Logo
            Text(
                text = "TURBO RUSH",
                color = RacingRed,
                fontWeight = FontWeight.Black,
                fontSize = 38.sp,
                letterSpacing = 4.sp
            )
            Text(
                text = "R A C I N G",
                color = TurboGold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = 8.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Animated Racing Car
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CarPreviewCanvas(
                    carDef = CarCatalog.getCar("valkyrie_horizon"),
                    isNitroActive = true,
                    tiltAngle = 0f,
                    viewFromRear = true
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Loading Progress Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(260.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CutCornerShape(4.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), CutCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(progress.coerceIn(0f, 1f))
                            .background(Brush.horizontalGradient(listOf(RacingRed, NeonCyan, TurboGold)))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "LOADING ASSETS... ${(progress * 100).toInt().coerceIn(0, 100)}%",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tuning High-Performance Engine...",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
