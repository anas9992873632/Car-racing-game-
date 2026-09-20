package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RaceResult
import com.example.ui.components.ArcadeButton
import com.example.ui.components.ButtonColorScheme
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardBorder
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NitroGreen
import com.example.ui.theme.RacingRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurboGold

@Composable
fun RaceResultScreen(
    result: RaceResult,
    onPlayAgain: () -> Unit,
    onHomeClick: () -> Unit
) {
    val isWinner = result.position == 1
    val bannerColor = when (result.position) {
        1 -> TurboGold
        2 -> Color(0xFFCBD5E1)
        3 -> Color(0xFFF97316)
        else -> TextMuted
    }

    val bannerTitle = when (result.position) {
        1 -> "1ST PLACE - VICTORY!"
        2 -> "2ND PLACE - PODIUM!"
        3 -> "3RD PLACE - PODIUM!"
        else -> "4TH PLACE - FINISHED"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Trophy / Medallion Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(bannerColor.copy(alpha = 0.15f))
                .border(2.dp, bannerColor, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = bannerColor,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = bannerTitle,
            color = bannerColor,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            letterSpacing = 2.sp
        )

        Text(
            text = if (isWinner) "NEW TRACK CHAMPION!" else "GOOD RACE!",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Rewards Section Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CutCornerShape(14.dp))
                .background(CarbonCard)
                .border(1.dp, CarbonCardBorder, CutCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "RACE REWARDS",
                    color = TurboGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RewardItem(
                        icon = Icons.Default.MonetizationOn,
                        value = "+${result.coinsEarned}",
                        label = "Coins Earned",
                        color = TurboGold
                    )
                    RewardItem(
                        icon = Icons.Default.Star,
                        value = "+${result.xpEarned} XP",
                        label = "Driver XP",
                        color = NeonCyan
                    )
                    if (result.gemsEarned > 0) {
                        RewardItem(
                            icon = Icons.Default.Diamond,
                            value = "+${result.gemsEarned}",
                            label = "Gems",
                            color = NitroGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Race Telemetry & Stats Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CutCornerShape(14.dp))
                .background(CarbonCard)
                .border(1.dp, CarbonCardBorder, CutCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "RACE TELEMETRY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                val totalSecs = (result.totalTimeMillis / 1000).toInt()
                val totalMs = (result.totalTimeMillis % 1000) / 10
                StatRow(
                    label = "Total Time",
                    value = String.format("%02d:%02d.%02d", totalSecs / 60, totalSecs % 60, totalMs)
                )

                if (result.bestLapMillis > 0L) {
                    val lapSecs = (result.bestLapMillis / 1000).toInt()
                    val lapMs = (result.bestLapMillis % 1000) / 10
                    StatRow(
                        label = "Best Lap",
                        value = String.format("%02d:%02d.%02d", lapSecs / 60, lapSecs % 60, lapMs)
                    )
                }

                StatRow(
                    label = "Top Speed",
                    value = "${result.topSpeedKmh} KM/H"
                )

                StatRow(
                    label = "Coins Picked Up",
                    value = "${result.coinsCollectedInRace}"
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Action Buttons (Play Again & Home)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ArcadeButton(
                text = "RACE AGAIN",
                icon = Icons.Default.Refresh,
                colorScheme = ButtonColorScheme.PRIMARY,
                isLarge = true,
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth()
            )

            ArcadeButton(
                text = "RETURN TO HOME",
                icon = Icons.Default.Home,
                colorScheme = ButtonColorScheme.DARK,
                onClick = onHomeClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun RewardItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(CutCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.5f), CutCornerShape(8.dp))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 10.sp
        )
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
