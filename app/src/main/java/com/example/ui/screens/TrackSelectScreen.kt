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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
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
import com.example.data.PlayerProfileEntity
import com.example.data.TrackCatalog
import com.example.data.TrackDef
import com.example.data.TrackRecordEntity
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
fun TrackSelectScreen(
    profile: PlayerProfileEntity,
    trackRecords: List<TrackRecordEntity>,
    onBackClick: () -> Unit,
    onTrackConfirmed: (TrackDef) -> Unit
) {
    var selectedTrack by remember { mutableStateOf(TrackCatalog.ALL_TRACKS.first()) }

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
                    text = "SELECT CIRCUIT",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "8 Worldwide Championship Tracks",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Track List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(TrackCatalog.ALL_TRACKS) { track ->
                val isSelected = track.id == selectedTrack.id
                val record = trackRecords.find { it.trackId == track.id }
                val isUnlocked = (record?.isUnlocked == true) || profile.level >= track.requiredLevel || track.id == "city_rush"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CutCornerShape(12.dp))
                        .background(if (isSelected) CarbonSurface else CarbonCard)
                        .border(
                            1.5.dp,
                            if (isSelected) RacingRed else CarbonCardBorder,
                            CutCornerShape(12.dp)
                        )
                        .clickable(enabled = isUnlocked) { selectedTrack = track }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Environment Color Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CutCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(track.skyTopColor), Color(track.skyBottomColor))
                                    )
                                )
                                .border(1.dp, Color(track.curbColor1), CutCornerShape(8.dp))
                        ) {
                            if (!isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = track.name,
                                    color = if (isUnlocked) TextPrimary else TextMuted,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                if (!isUnlocked) {
                                    Text(
                                        text = "LVL ${track.requiredLevel} REQUIRED",
                                        color = RacingRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "${track.lengthMeters}M",
                                        color = NeonCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = track.location.uppercase(),
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = track.description,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 2,
                                lineHeight = 14.sp
                            )

                            if (isUnlocked && record != null && record.bestTimeMillis > 0L) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Best Time",
                                        tint = TurboGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val sec = (record.bestTimeMillis / 1000).toInt()
                                    val ms = (record.bestTimeMillis % 1000) / 10
                                    Text(
                                        text = "Best: ${sec / 60}m ${sec % 60}.${ms}s",
                                        color = TurboGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Start Race Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ArcadeButton(
                text = "START RACE: ${selectedTrack.name.uppercase()}",
                colorScheme = ButtonColorScheme.PRIMARY,
                isLarge = true,
                onClick = { onTrackConfirmed(selectedTrack) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
