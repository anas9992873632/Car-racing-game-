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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CarCatalog
import com.example.data.CarDef
import com.example.data.PlayerProfileEntity
import com.example.ui.components.ArcadeButton
import com.example.ui.components.ButtonColorScheme
import com.example.ui.components.CarPreviewCanvas
import com.example.ui.components.GameHeader
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

@Composable
fun HomeScreen(
    profile: PlayerProfileEntity,
    selectedCar: CarDef,
    onPlayClick: () -> Unit,
    onGarageClick: () -> Unit,
    onCarsClick: () -> Unit,
    onTracksClick: () -> Unit,
    onMissionsClick: () -> Unit,
    onDailyRewardClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "home_bg")
    val speedLines by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "speed_lines"
    )
    val carBounce by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "car_bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
    ) {
        // Animated dynamic racing highway backdrop
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.48f

            // Horizon gradient
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF111827)),
                    startY = 0f,
                    endY = cy
                ),
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width, cy)
            )

            // Dynamic highway lines receding to horizon
            for (i in -8..8) {
                val x = cx + (i * 90f)
                drawLine(
                    color = NeonCyan.copy(alpha = 0.12f),
                    start = Offset(cx, cy - 40f),
                    end = Offset(x * 1.8f, size.height),
                    strokeWidth = 2f
                )
            }

            // Moving road markers
            val markerY = cy + ((speedLines / 100f) * (size.height - cy))
            drawLine(
                color = RacingRed.copy(alpha = 0.35f),
                start = Offset(0f, markerY),
                end = Offset(size.width, markerY),
                strokeWidth = 3f
            )
        }

        // Main Screen Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header: Level, XP, Coins, Gems
            GameHeader(
                coins = profile.coins,
                gems = profile.gems,
                level = profile.level,
                xp = profile.xp,
                onCoinsClick = onMissionsClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game Logo: TURBO RUSH RACING
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "TURBO RUSH",
                        color = RacingRed,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                    Text(
                        text = "R A C I N G",
                        color = TurboGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 7.sp
                    )
                }

                // Interactive Hero Car Showcase Card
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(CutCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(CarbonCard, CarbonSurface)
                            )
                        )
                        .border(1.5.dp, NeonCyan.copy(alpha = 0.35f), CutCornerShape(16.dp))
                        .clickable { onGarageClick() }
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedCar.name.uppercase(),
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = selectedCar.category,
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            // Tap to customize badge
                            Box(
                                modifier = Modifier
                                    .clip(CutCornerShape(6.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, CarbonCardBorder, CutCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "GARAGE >",
                                    color = TurboGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Car Model Render with subtle hover bounce
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .padding(top = carBounce.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CarPreviewCanvas(
                                carDef = selectedCar,
                                isNitroActive = true,
                                tiltAngle = 0f,
                                viewFromRear = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // LARGE PLAY BUTTON
                ArcadeButton(
                    text = "PLAY RACE",
                    icon = Icons.Default.PlayArrow,
                    colorScheme = ButtonColorScheme.PRIMARY,
                    isLarge = true,
                    onClick = onPlayClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Hub Grid (Garage, Cars, Tracks, Missions)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubMenuCard(
                        title = "GARAGE",
                        subtitle = "Upgrade Car",
                        icon = Icons.Default.Build,
                        accentColor = TurboGold,
                        modifier = Modifier.weight(1f),
                        onClick = onGarageClick
                    )
                    HubMenuCard(
                        title = "CARS",
                        subtitle = "7 Supercars",
                        icon = Icons.Default.DirectionsCar,
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f),
                        onClick = onCarsClick
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubMenuCard(
                        title = "TRACKS",
                        subtitle = "8 Grand Circuits",
                        icon = Icons.Default.Map,
                        accentColor = NitroGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onTracksClick
                    )
                    HubMenuCard(
                        title = "MISSIONS",
                        subtitle = "Earn Rewards",
                        icon = Icons.Default.EmojiEvents,
                        accentColor = RacingRed,
                        modifier = Modifier.weight(1f),
                        onClick = onMissionsClick
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Secondary Row: Daily Reward & Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ArcadeButton(
                        text = "Daily Gift",
                        icon = Icons.Default.CardGiftcard,
                        colorScheme = ButtonColorScheme.GOLD,
                        onClick = onDailyRewardClick,
                        modifier = Modifier.weight(1f)
                    )
                    ArcadeButton(
                        text = "Settings",
                        icon = Icons.Default.Settings,
                        colorScheme = ButtonColorScheme.DARK,
                        onClick = onSettingsClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HubMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CutCornerShape(12.dp))
            .background(CarbonCard)
            .border(1.dp, CarbonCardBorder, CutCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CutCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), CutCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
