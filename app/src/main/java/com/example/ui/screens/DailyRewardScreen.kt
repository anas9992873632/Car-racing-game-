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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.PlayerProfileEntity
import com.example.ui.components.ArcadeButton
import com.example.ui.components.ButtonColorScheme
import com.example.ui.components.GameHeader
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardBorder
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NitroGreen
import com.example.ui.theme.RacingRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurboGold

@Composable
fun DailyRewardScreen(
    profile: PlayerProfileEntity,
    canClaim: Boolean,
    onBackClick: () -> Unit,
    onClaimReward: () -> Unit
) {
    val streak = ((profile.lastDailyRewardDay % 7) + 1)
    val rewards = listOf(
        Triple(1, "1,000 Coins", false),
        Triple(2, "2,000 Coins", false),
        Triple(3, "5 Gems", true),
        Triple(4, "4,000 Coins", false),
        Triple(5, "10 Gems", true),
        Triple(6, "8,000 Coins", false),
        Triple(7, "15,000 C + 25 Gems", true)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        GameHeader(
            coins = profile.coins,
            gems = profile.gems,
            level = profile.level,
            xp = profile.xp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CutCornerShape(8.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonCardBorder, CutCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "DAILY REWARD BONUS",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Log in every day to claim bonus prizes",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rewards.forEach { (day, prize, isGem) ->
                val isClaimed = day < streak || (day == streak && !canClaim)
                val isCurrent = day == streak && canClaim

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CutCornerShape(10.dp))
                        .background(if (isCurrent) CarbonSurface else CarbonCard)
                        .border(
                            1.5.dp,
                            if (isCurrent) TurboGold else if (isClaimed) NitroGreen.copy(alpha = 0.5f) else CarbonCardBorder,
                            CutCornerShape(10.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CutCornerShape(6.dp))
                                    .background(if (isClaimed) NitroGreen.copy(alpha = 0.2f) else if (isCurrent) TurboGold.copy(alpha = 0.2f) else Color(0xFF1E293B))
                            ) {
                                if (isClaimed) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Claimed",
                                        tint = NitroGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = "D$day",
                                        color = if (isCurrent) TurboGold else TextMuted,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "DAY $day REWARD",
                                color = if (isCurrent) TextPrimary else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isGem) Icons.Default.Diamond else Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = if (isGem) NeonCyan else TurboGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prize,
                                color = if (isCurrent) TurboGold else TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ArcadeButton(
                text = if (canClaim) "CLAIM TODAY'S REWARD" else "COME BACK TOMORROW",
                icon = Icons.Default.CardGiftcard,
                colorScheme = if (canClaim) ButtonColorScheme.GOLD else ButtonColorScheme.DARK,
                enabled = canClaim,
                isLarge = true,
                onClick = onClaimReward,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
