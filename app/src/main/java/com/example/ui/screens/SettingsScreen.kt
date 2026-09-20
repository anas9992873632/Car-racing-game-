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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileEntity
import com.example.ui.components.ArcadeButton
import com.example.ui.components.ButtonColorScheme
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
fun SettingsScreen(
    profile: PlayerProfileEntity,
    onBackClick: () -> Unit,
    onUpdateProfile: (PlayerProfileEntity) -> Unit,
    onResetProgress: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
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
                    text = "SETTINGS",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Game Controls, Audio & Preferences",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION: CONTROLS
            Text(
                text = "STEERING & CONTROLS",
                color = TurboGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Control Type Switcher
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CutCornerShape(10.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonCardBorder, CutCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "CONTROL SCHEME",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("BUTTONS", "SWIPE").forEach { scheme ->
                            val isSelected = profile.controlType == scheme
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CutCornerShape(8.dp))
                                    .background(if (isSelected) RacingRed else CarbonSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) RacingRed else CarbonCardBorder,
                                        CutCornerShape(8.dp)
                                    )
                                    .clickable {
                                        onUpdateProfile(profile.copy(controlType = scheme))
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = if (scheme == "BUTTONS") "TOUCH BUTTONS" else "SWIPE / DRAG",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // Steering Sensitivity Slider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CutCornerShape(10.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonCardBorder, CutCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STEERING SENSITIVITY",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(profile.steeringSensitivity * 100).toInt()}%",
                            color = NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Slider(
                        value = profile.steeringSensitivity,
                        onValueChange = { newVal ->
                            onUpdateProfile(profile.copy(steeringSensitivity = newVal))
                        },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color(0xFF1E293B)
                        )
                    )
                }
            }

            // SECTION: AUDIO & HAPTICS
            Text(
                text = "AUDIO & HAPTICS",
                color = TurboGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Sound FX
            SettingToggleRow(
                title = "Sound Effects",
                subtitle = "Engine, nitro, collision, screech audio",
                isChecked = profile.soundEnabled,
                onCheckedChange = { onUpdateProfile(profile.copy(soundEnabled = it)) }
            )

            // Music
            SettingToggleRow(
                title = "Background Music",
                subtitle = "High-energy synthwave game music",
                isChecked = profile.musicEnabled,
                onCheckedChange = { onUpdateProfile(profile.copy(musicEnabled = it)) }
            )

            // Vibration
            SettingToggleRow(
                title = "Vibration & Haptic Feedback",
                subtitle = "Tactile impact rumble and nitro buzz",
                isChecked = profile.vibrationEnabled,
                onCheckedChange = { onUpdateProfile(profile.copy(vibrationEnabled = it)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // RESET PROGRESS BUTTON
            ArcadeButton(
                text = "RESET GAME DATA",
                icon = Icons.Default.RestartAlt,
                colorScheme = ButtonColorScheme.DARK,
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            )

            // Version info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TURBO RUSH RACING v1.0.0",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "High Octane Mobile Racing Arcade",
                    color = Color(0xFF475569),
                    fontSize = 10.sp
                )
            }
        }

        // Reset confirmation dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = {
                    Text(
                        text = "Reset Game Data?",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                },
                text = {
                    Text(
                        text = "This will reset all your coins, gems, car upgrades, and unlocked tracks to default. This action cannot be undone.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showResetDialog = false
                            onResetProgress()
                        }
                    ) {
                        Text("RESET EVERYTHING", color = RacingRed, fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("CANCEL", color = TextPrimary)
                    }
                },
                containerColor = CarbonCard
            )
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(10.dp))
            .background(CarbonCard)
            .border(1.dp, CarbonCardBorder, CutCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = RacingRed,
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFF1E293B)
                )
            )
        }
    }
}
