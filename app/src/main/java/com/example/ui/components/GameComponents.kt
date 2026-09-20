package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun GameHeader(
    coins: Int,
    gems: Int,
    level: Int,
    xp: Int,
    modifier: Modifier = Modifier,
    onCoinsClick: (() -> Unit)? = null
) {
    val xpInCurrentLevel = xp % 600
    val xpProgress = (xpInCurrentLevel / 600f).coerceIn(0f, 1f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player Level & XP Card
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(CarbonCard)
                .border(1.dp, CarbonCardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(RacingRed, RacingRedDark)))
            ) {
                Text(
                    text = "L$level",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DRIVER",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$xpInCurrentLevel/600 XP",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                // XP Bar
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF243048))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(xpProgress)
                            .background(Brush.horizontalGradient(listOf(NeonCyan, TurboGold)))
                    )
                }
            }
        }

        // Coins & Gems
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Coins badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarbonCard)
                    .border(1.dp, TurboGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable(enabled = onCoinsClick != null) { onCoinsClick?.invoke() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Coins",
                    tint = TurboGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$coins",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }

            // Gems badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarbonCard)
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Gems",
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$gems",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ArcadeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    colorScheme: ButtonColorScheme = ButtonColorScheme.PRIMARY,
    isLarge: Boolean = false,
    enabled: Boolean = true,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }

    val gradient = when (colorScheme) {
        ButtonColorScheme.PRIMARY -> Brush.horizontalGradient(listOf(RacingRed, Color(0xFFF43F5E)))
        ButtonColorScheme.CYAN -> Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF0284C7)))
        ButtonColorScheme.GOLD -> Brush.horizontalGradient(listOf(TurboGold, Color(0xFFD97706)))
        ButtonColorScheme.GREEN -> Brush.horizontalGradient(listOf(NitroGreen, Color(0xFF059669)))
        ButtonColorScheme.DARK -> Brush.horizontalGradient(listOf(CarbonSurface, CarbonCard))
    }

    val borderColor = when (colorScheme) {
        ButtonColorScheme.PRIMARY -> RacingRed.copy(alpha = 0.7f)
        ButtonColorScheme.CYAN -> NeonCyan.copy(alpha = 0.7f)
        ButtonColorScheme.GOLD -> TurboGold.copy(alpha = 0.7f)
        ButtonColorScheme.GREEN -> NitroGreen.copy(alpha = 0.7f)
        ButtonColorScheme.DARK -> CarbonCardBorder
    }

    val height = if (isLarge) 54.dp else 46.dp
    val fontSize = if (isLarge) 16.sp else 14.sp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag(testTag.ifEmpty { "btn_${text.lowercase().replace(" ", "_")}" })
            .height(height)
            .clip(CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))))
            .border(1.5.dp, if (enabled) borderColor else Color.Transparent, CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (colorScheme == ButtonColorScheme.GOLD) Color(0xFF0F172A) else Color.White,
                    modifier = Modifier.size(if (isLarge) 22.dp else 18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                color = if (!enabled) Color(0xFF94A3B8) else if (colorScheme == ButtonColorScheme.GOLD) Color(0xFF0F172A) else Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
        }
    }
}

enum class ButtonColorScheme {
    PRIMARY,
    CYAN,
    GOLD,
    GREEN,
    DARK
}

@Composable
fun StatBar(
    label: String,
    currentValue: Int,
    upgradedValue: Int = currentValue,
    maxValue: Int = 100,
    color: Color = NeonCyan,
    modifier: Modifier = Modifier
) {
    val totalSegments = 10
    val activeSegments = (currentValue.toFloat() / maxValue * totalSegments).toInt().coerceIn(0, totalSegments)
    val upgradedSegments = (upgradedValue.toFloat() / maxValue * totalSegments).toInt().coerceIn(0, totalSegments)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$currentValue",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (upgradedValue > currentValue) {
                    Text(
                        text = " (+$upgradedValue)",
                        color = NitroGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Segmented bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            for (i in 0 until totalSegments) {
                val segColor = when {
                    i < activeSegments -> color
                    i < upgradedSegments -> NitroGreen
                    else -> Color(0xFF1E293B)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CutCornerShape(topStart = 2.dp, bottomEnd = 2.dp))
                        .background(segColor)
                )
            }
        }
    }
}
