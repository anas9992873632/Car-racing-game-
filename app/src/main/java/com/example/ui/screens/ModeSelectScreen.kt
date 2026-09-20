package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AIDifficulty
import com.example.data.PlayerProfileEntity
import com.example.data.RaceMode
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
import com.example.ui.theme.RacingRedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurboGold

@Composable
fun ModeSelectScreen(
    profile: PlayerProfileEntity,
    onBackClick: () -> Unit,
    onModeSelected: (RaceMode, AIDifficulty) -> Unit
) {
    var selectedMode by remember { mutableStateOf(RaceMode.QUICK_RACE) }
    var selectedDifficulty by remember { mutableStateOf(AIDifficulty.NORMAL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top navigation bar
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
                    text = "SELECT RACE MODE",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Choose your racing rules and AI difficulty",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Difficulty Selector Strip
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text(
                text = "AI OPPONENT DIFFICULTY",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AIDifficulty.values().forEach { diff ->
                    val isSelected = diff == selectedDifficulty
                    val color = when (diff) {
                        AIDifficulty.EASY -> NitroGreen
                        AIDifficulty.NORMAL -> NeonCyan
                        AIDifficulty.HARD -> TurboGold
                        AIDifficulty.EXTREME -> RacingRed
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(CutCornerShape(6.dp))
                            .background(if (isSelected) color.copy(alpha = 0.25f) else CarbonCard)
                            .border(
                                1.5.dp,
                                if (isSelected) color else CarbonCardBorder,
                                CutCornerShape(6.dp)
                            )
                            .clickable { selectedDifficulty = diff }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = diff.label.uppercase(),
                            color = if (isSelected) color else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mode List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(RaceMode.values()) { mode ->
                val isSelected = mode == selectedMode
                val icon = when (mode) {
                    RaceMode.QUICK_RACE -> Icons.Default.Flag
                    RaceMode.CAREER -> Icons.Default.MilitaryTech
                    RaceMode.TIME_TRIAL -> Icons.Default.Timer
                    RaceMode.CHAMPIONSHIP -> Icons.Default.Stars
                    RaceMode.ENDLESS -> Icons.Default.AllInclusive
                    RaceMode.CHALLENGE -> Icons.Default.Bolt
                }

                val accent = when (mode) {
                    RaceMode.QUICK_RACE -> RacingRed
                    RaceMode.CAREER -> TurboGold
                    RaceMode.TIME_TRIAL -> NeonCyan
                    RaceMode.CHAMPIONSHIP -> Color(0xFFC084FC)
                    RaceMode.ENDLESS -> NitroGreen
                    RaceMode.CHALLENGE -> Color(0xFFF97316)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CutCornerShape(12.dp))
                        .background(if (isSelected) CarbonSurface else CarbonCard)
                        .border(
                            1.5.dp,
                            if (isSelected) accent else CarbonCardBorder,
                            CutCornerShape(12.dp)
                        )
                        .clickable { selectedMode = mode }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CutCornerShape(8.dp))
                                .background(accent.copy(alpha = 0.15f))
                                .border(1.dp, accent.copy(alpha = 0.5f), CutCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = mode.title,
                                tint = accent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.title,
                                color = if (isSelected) accent else TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = mode.description,
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ArcadeButton(
                text = "CONTINUE TO TRACKS",
                colorScheme = ButtonColorScheme.PRIMARY,
                isLarge = true,
                onClick = { onModeSelected(selectedMode, selectedDifficulty) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
