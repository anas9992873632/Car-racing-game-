package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 1500,
    val gems: Int = 25,
    val xp: Int = 0,
    val level: Int = 1,
    val selectedCarId: String = "apex_swift",
    val lastDailyRewardDay: Int = 0,
    val lastDailyRewardTimestamp: Long = 0L,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val steeringSensitivity: Float = 1.0f,
    val controlType: String = "BUTTONS", // BUTTONS, SWIPE
    val graphicsQuality: String = "HIGH", // HIGH, MEDIUM, LOW
    val completedCareerRaces: Int = 0
)

@Entity(tableName = "car_status")
data class CarStatusEntity(
    @PrimaryKey val carId: String,
    val isUnlocked: Boolean,
    val engineLevel: Int = 1,
    val turboLevel: Int = 1,
    val tiresLevel: Int = 1,
    val brakesLevel: Int = 1,
    val handlingLevel: Int = 1,
    val nitroLevel: Int = 1,
    val maxSpeedLevel: Int = 1,
    val selectedColorHex: Long = 0xFFE11D48
)

@Entity(tableName = "track_records")
data class TrackRecordEntity(
    @PrimaryKey val trackId: String,
    val isUnlocked: Boolean,
    val bestTimeMillis: Long = 0L,
    val bestLapMillis: Long = 0L,
    val timesPlayed: Int = 0,
    val starsEarned: Int = 0
)

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val rewardCoins: Int,
    val rewardGems: Int,
    val rewardXp: Int,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
)
