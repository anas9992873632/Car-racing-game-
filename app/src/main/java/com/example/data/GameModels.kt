package com.example.data

import androidx.compose.ui.graphics.Color

enum class RaceMode(val title: String, val description: String, val iconName: String) {
    QUICK_RACE("Quick Race", "Jump right into a fast 3-lap circuit with AI competitors", "flag"),
    CAREER("Career Mode", "Progress through seasonal championships of rising difficulty", "trophy"),
    TIME_TRIAL("Time Trial", "Push the limits alone on track and beat best lap records", "timer"),
    CHAMPIONSHIP("Championship", "Cumulative 4-stage tournament for gold cup and giant rewards", "stars"),
    ENDLESS("Endless Race", "Infinite open highway with speed challenges, boost pads and coins", "all_inclusive"),
    CHALLENGE("Challenge Mode", "Targeted adrenaline missions: speed traps and coin hunting", "bolt")
}

enum class AIDifficulty(val label: String, val speedMultiplier: Float, val aggression: Float) {
    EASY("Easy", 0.78f, 0.4f),
    NORMAL("Normal", 0.90f, 0.65f),
    HARD("Hard", 1.02f, 0.85f),
    EXTREME("Extreme", 1.15f, 0.98f)
}

enum class UpgradeCategory(
    val title: String,
    val description: String,
    val icon: String,
    val statBoostPerLevel: Float
) {
    ENGINE("Engine", "Increases top speed and high-end power", "speed", 12f),
    TURBO("Turbo", "Gives explosive acceleration out of turns", "bolt", 10f),
    TIRES("Tires", "Increases road grip and cornering stability", "radio_button_checked", 8f),
    BRAKES("Brakes", "Decreases stopping distance and adds control", "disc_full", 9f),
    HANDLING("Handling", "Sharpens steering response and drift control", "tune", 10f),
    NITRO("Nitro", "Extends nitro duration and boost surge", "local_fire_department", 14f),
    MAX_SPEED("Max Speed", "Pushes the physical limit of the car", "trending_up", 15f)
}

data class CarDef(
    val id: String,
    val name: String,
    val category: String,
    val tagLine: String,
    val baseSpeed: Int,       // 0-100 scale (e.g. 55 to 98)
    val baseAcceleration: Int,// 0-100 scale
    val baseHandling: Int,    // 0-100 scale
    val baseBraking: Int,     // 0-100 scale
    val baseNitro: Int,       // 0-100 scale
    val unlockCoins: Int,
    val unlockGems: Int = 0,
    val primaryColor: Long,
    val secondaryColor: Long,
    val bodyStyle: String,    // "sedan", "sports", "muscle", "super", "formula", "truck", "hyper"
    val maxUpgrades: Int = 5
)

object CarCatalog {
    val ALL_CARS = listOf(
        CarDef(
            id = "apex_swift",
            name = "Apex Swift",
            category = "Starter Car",
            tagLine = "Reliable hatchback tuned for rookie street racers",
            baseSpeed = 52,
            baseAcceleration = 54,
            baseHandling = 60,
            baseBraking = 55,
            baseNitro = 50,
            unlockCoins = 0,
            unlockGems = 0,
            primaryColor = 0xFF2563EB, // Electric Blue
            secondaryColor = 0xFFFFFFFF,
            bodyStyle = "sports"
        ),
        CarDef(
            id = "viper_gt",
            name = "Viper GT",
            category = "Sports Car",
            tagLine = "Lightweight coupe with razor-sharp acceleration",
            baseSpeed = 66,
            baseAcceleration = 70,
            baseHandling = 68,
            baseBraking = 64,
            baseNitro = 62,
            unlockCoins = 2500,
            unlockGems = 0,
            primaryColor = 0xFFE11D48, // Crimson Red
            secondaryColor = 0xFF1E293B,
            bodyStyle = "sports"
        ),
        CarDef(
            id = "thunderbolt_69",
            name = "Thunderbolt '69",
            category = "Muscle Car",
            tagLine = "Heavy American V8 torque that roars on the straights",
            baseSpeed = 74,
            baseAcceleration = 75,
            baseHandling = 56,
            baseBraking = 60,
            baseNitro = 72,
            unlockCoins = 5000,
            unlockGems = 5,
            primaryColor = 0xFFF59E0B, // Amber Orange
            secondaryColor = 0xFF000000,
            bodyStyle = "muscle"
        ),
        CarDef(
            id = "phantom_rs",
            name = "Phantom RS",
            category = "Super Car",
            tagLine = "Carbon fiber aero monster engineered for high G-forces",
            baseSpeed = 82,
            baseAcceleration = 84,
            baseHandling = 80,
            baseBraking = 78,
            baseNitro = 80,
            unlockCoins = 10000,
            unlockGems = 15,
            primaryColor = 0xFF8B5CF6, // Deep Purple
            secondaryColor = 0xFF10B981,
            bodyStyle = "super"
        ),
        CarDef(
            id = "formula_turbo",
            name = "Formula Turbo",
            category = "Racing Car",
            tagLine = "Open-wheel aerodynamic marvel built strictly for the track",
            baseSpeed = 89,
            baseAcceleration = 90,
            baseHandling = 92,
            baseBraking = 90,
            baseNitro = 85,
            unlockCoins = 18000,
            unlockGems = 25,
            primaryColor = 0xFFEF4444, // Scuderia Red
            secondaryColor = 0xFFFACC15,
            bodyStyle = "formula"
        ),
        CarDef(
            id = "titan_brawler",
            name = "Titan Brawler",
            category = "Off-road Car",
            tagLine = "Rugged all-terrain widebody that smashes past obstacles",
            baseSpeed = 76,
            baseAcceleration = 72,
            baseHandling = 75,
            baseBraking = 85,
            baseNitro = 70,
            unlockCoins = 25000,
            unlockGems = 35,
            primaryColor = 0xFF10B981, // Emerald Green
            secondaryColor = 0xFF374151,
            bodyStyle = "truck"
        ),
        CarDef(
            id = "valkyrie_horizon",
            name = "Valkyrie Horizon",
            category = "Hyper Car",
            tagLine = "Futuristic hybrid rocket with active neon aero spoilers",
            baseSpeed = 98,
            baseAcceleration = 97,
            baseHandling = 95,
            baseBraking = 94,
            baseNitro = 98,
            unlockCoins = 40000,
            unlockGems = 50,
            primaryColor = 0xFF06B6D4, // Neon Cyan
            secondaryColor = 0xFFEC4899,
            bodyStyle = "hyper"
        )
    )

    fun getCar(id: String): CarDef {
        return ALL_CARS.find { it.id == id } ?: ALL_CARS.first()
    }
}

data class TrackDef(
    val id: String,
    val name: String,
    val location: String,
    val description: String,
    val lengthMeters: Int,
    val laps: Int,
    val requiredLevel: Int,
    val skyTopColor: Long,
    val skyBottomColor: Long,
    val groundColor: Long,
    val roadColor: Long,
    val curbColor1: Long,
    val curbColor2: Long,
    val fogColor: Long,
    val sceneryType: String, // "city", "desert", "mountain", "forest", "snow", "night_city", "coastal", "neon"
    val weather: String      // "clear", "sunset", "night", "snow", "rain", "neon"
)

object TrackCatalog {
    val ALL_TRACKS = listOf(
        TrackDef(
            id = "city_rush",
            name = "City Rush",
            location = "Metropolis Highway",
            description = "High-speed sweeping city viaducts framed by gleaming skyscrapers and overhead bridges.",
            lengthMeters = 3200,
            laps = 3,
            requiredLevel = 1,
            skyTopColor = 0xFF1E3A8A,
            skyBottomColor = 0xFF60A5FA,
            groundColor = 0xFF334155,
            roadColor = 0xFF1F2937,
            curbColor1 = 0xFFEF4444,
            curbColor2 = 0xFFFFFFFF,
            fogColor = 0xFF93C5FD,
            sceneryType = "city",
            weather = "clear"
        ),
        TrackDef(
            id = "desert_highway",
            name = "Desert Highway",
            location = "Mojave Dunes",
            description = "Sun-scorched desert asphalt lined with towering red rock mesas, dunes and saguaro cacti.",
            lengthMeters = 3800,
            laps = 3,
            requiredLevel = 2,
            skyTopColor = 0xFFC2410C,
            skyBottomColor = 0xFFFDE047,
            groundColor = 0xFFD97706,
            roadColor = 0xFF27272A,
            curbColor1 = 0xFFEA580C,
            curbColor2 = 0xFFFEF08A,
            fogColor = 0xFFFBBF24,
            sceneryType = "desert",
            weather = "sunset"
        ),
        TrackDef(
            id = "mountain_pass",
            name = "Mountain Pass",
            location = "Alpine Ridge",
            description = "Dangerous hairpin turns winding along dramatic alpine cliffs and steep pine valleys.",
            lengthMeters = 4200,
            laps = 3,
            requiredLevel = 3,
            skyTopColor = 0xFF0F172A,
            skyBottomColor = 0xFF38BDF8,
            groundColor = 0xFF166534,
            roadColor = 0xFF18181B,
            curbColor1 = 0xFFDC2626,
            curbColor2 = 0xFFFFFFFF,
            fogColor = 0xFFBAE6FD,
            sceneryType = "mountain",
            weather = "clear"
        ),
        TrackDef(
            id = "forest_road",
            name = "Forest Road",
            location = "Redwood Sanctuary",
            description = "Twisting woodland curves through massive towering ancient redwoods and wooden barriers.",
            lengthMeters = 3500,
            laps = 3,
            requiredLevel = 4,
            skyTopColor = 0xFF14532D,
            skyBottomColor = 0xFF86EFAC,
            groundColor = 0xFF15803D,
            roadColor = 0xFF1E293B,
            curbColor1 = 0xFFEAB308,
            curbColor2 = 0xFFFFFFFF,
            fogColor = 0xFFBBF7D0,
            sceneryType = "forest",
            weather = "clear"
        ),
        TrackDef(
            id = "snow_valley",
            name = "Snow Valley",
            location = "Glacier Summit",
            description = "Chilling blizzard speeds across snow-packed icy mountain asphalt with reduced tire grip.",
            lengthMeters = 3900,
            laps = 3,
            requiredLevel = 5,
            skyTopColor = 0xFF1E293B,
            skyBottomColor = 0xFFCBD5E1,
            groundColor = 0xFFF1F5F9,
            roadColor = 0xFF475569,
            curbColor1 = 0xFF0284C7,
            curbColor2 = 0xFFFFFFFF,
            fogColor = 0xFFE2E8F0,
            sceneryType = "snow",
            weather = "snow"
        ),
        TrackDef(
            id = "night_city",
            name = "Night City",
            location = "Downtown Neon District",
            description = "Reflective midnight streets beneath towering cyberpunk holographic billboards and streetlights.",
            lengthMeters = 3600,
            laps = 3,
            requiredLevel = 6,
            skyTopColor = 0xFF020617,
            skyBottomColor = 0xFF1E1B4B,
            groundColor = 0xFF09090B,
            roadColor = 0xFF111827,
            curbColor1 = 0xFFEC4899,
            curbColor2 = 0xFF06B6D4,
            fogColor = 0xFF312E81,
            sceneryType = "night_city",
            weather = "night"
        ),
        TrackDef(
            id = "coastal_highway",
            name = "Coastal Highway",
            location = "Pacific Shore",
            description = "Golden sunset seaside boulevard flanked by palm trees, ocean waves and seaside barriers.",
            lengthMeters = 4000,
            laps = 3,
            requiredLevel = 7,
            skyTopColor = 0xFF431407,
            skyBottomColor = 0xFFF97316,
            groundColor = 0xFF0284C7,
            roadColor = 0xFF1F2937,
            curbColor1 = 0xFFF43F5E,
            curbColor2 = 0xFFFEF3C7,
            fogColor = 0xFFFB923C,
            sceneryType = "coastal",
            weather = "sunset"
        ),
        TrackDef(
            id = "neon_highway",
            name = "Neon Highway",
            location = "Cyber Void Grid",
            description = "Synthwave retro-futuristic grid road surrounded by pulsing digital wireframes and laser beams.",
            lengthMeters = 4500,
            laps = 3,
            requiredLevel = 8,
            skyTopColor = 0xFF0A0A0A,
            skyBottomColor = 0xFF581C87,
            groundColor = 0xFF18022B,
            roadColor = 0xFF0F0F1A,
            curbColor1 = 0xFF06B6D4,
            curbColor2 = 0xFFA855F7,
            fogColor = 0xFF3B0764,
            sceneryType = "neon",
            weather = "neon"
        )
    )

    fun getTrack(id: String): TrackDef {
        return ALL_TRACKS.find { it.id == id } ?: ALL_TRACKS.first()
    }
}

data class DailyRewardItem(
    val dayNumber: Int,
    val title: String,
    val rewardText: String,
    val rewardCoins: Int,
    val rewardGems: Int,
    val iconName: String,
    val isLargeReward: Boolean = false
)

object DailyRewardCatalog {
    val REWARDS = listOf(
        DailyRewardItem(1, "Day 1", "500 Coins", 500, 0, "coins"),
        DailyRewardItem(2, "Day 2", "1,000 Coins", 1000, 0, "coins"),
        DailyRewardItem(3, "Day 3", "2 Nitro Boosts + 1,200 Coins", 1200, 2, "bolt"),
        DailyRewardItem(4, "Day 4", "15 Gems", 500, 15, "diamond"),
        DailyRewardItem(5, "Day 5", "2,500 Coins", 2500, 0, "coins"),
        DailyRewardItem(6, "Day 6", "Car Upgrade Pack + 3,000 Coins", 3000, 5, "build"),
        DailyRewardItem(7, "Day 7", "Grand Champion Pack: 10,000 Coins & 35 Gems", 10000, 35, "trophy", isLargeReward = true)
    )
}

data class RaceResult(
    val position: Int,
    val totalRacers: Int,
    val totalTimeMillis: Long,
    val bestLapMillis: Long,
    val coinsEarned: Int,
    val xpEarned: Int,
    val gemsEarned: Int,
    val coinsCollectedInRace: Int,
    val topSpeedKmh: Int,
    val newRecordsBroken: Boolean,
    val unlockedNextTrack: Boolean = false,
    val unlockedNewCar: String? = null
)
