package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CarCatalog
import com.example.data.CarDef
import com.example.data.CarStatusEntity
import com.example.data.PlayerProfileEntity
import com.example.data.UpgradeCategory
import com.example.ui.components.ArcadeButton
import com.example.ui.components.ButtonColorScheme
import com.example.ui.components.CarPreviewCanvas
import com.example.ui.components.GameHeader
import com.example.ui.components.StatBar
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
fun GarageScreen(
    profile: PlayerProfileEntity,
    carsStatus: List<CarStatusEntity>,
    onBackClick: () -> Unit,
    onSelectCar: (String) -> Unit,
    onBuyCar: (String) -> Unit,
    onUpgradeCar: (String, UpgradeCategory) -> Unit,
    onSetColor: (String, Long) -> Unit
) {
    var selectedCarIndex by remember {
        val idx = CarCatalog.ALL_CARS.indexOfFirst { it.id == profile.selectedCarId }
        mutableIntStateOf(if (idx != -1) idx else 0)
    }

    val carDef = CarCatalog.ALL_CARS[selectedCarIndex]
    val status = carsStatus.find { it.carId == carDef.id }
    val isUnlocked = status?.isUnlocked == true || carDef.id == "apex_swift"
    val isEquipped = profile.selectedCarId == carDef.id

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Upgrades, 1: Paint Shop, 2: Specs
    var viewFromRear by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "garage_hover")
    val carHover by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Header
        GameHeader(
            coins = profile.coins,
            gems = profile.gems,
            level = profile.level,
            xp = profile.xp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
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
                    text = "GARAGE & TUNING",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Upgrade Engine, Turbo & Body",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // CAR SELECTOR CAROUSEL
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CarCatalog.ALL_CARS.indices.toList()) { idx ->
                val car = CarCatalog.ALL_CARS[idx]
                val carStatus = carsStatus.find { it.carId == car.id }
                val unlocked = carStatus?.isUnlocked == true || car.id == "apex_swift"
                val isSelected = idx == selectedCarIndex

                Box(
                    modifier = Modifier
                        .clip(CutCornerShape(8.dp))
                        .background(if (isSelected) CarbonSurface else CarbonCard)
                        .border(
                            1.5.dp,
                            if (isSelected) RacingRed else CarbonCardBorder,
                            CutCornerShape(8.dp)
                        )
                        .clickable { selectedCarIndex = idx }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!unlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = car.name.uppercase(),
                            color = if (isSelected) RacingRed else if (unlocked) TextPrimary else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Interactive Car Showcase Stage
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp)
                .clip(CutCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF1E293B), CarbonCard, CarbonDark)
                    )
                )
                .border(1.dp, CarbonCardBorder, CutCornerShape(16.dp))
        ) {
            // Camera perspective toggle button
            IconButton(
                onClick = { viewFromRear = !viewFromRear },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CarbonCard)
                    .border(1.dp, CarbonCardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rotate View",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Car Model Canvas
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(top = carHover.dp),
                contentAlignment = Alignment.Center
            ) {
                CarPreviewCanvas(
                    carDef = carDef,
                    overrideColorHex = status?.selectedColorHex,
                    isNitroActive = true,
                    tiltAngle = 0f,
                    viewFromRear = viewFromRear
                )
            }

            // Equipped Badge
            if (isEquipped) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(CutCornerShape(6.dp))
                        .background(NitroGreen)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "EQUIPPED",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // TAB BAR (Upgrades, Paint Shop, Specs)
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = CarbonDark,
            contentColor = TextPrimary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = RacingRed
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("UPGRADES", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("PAINT SHOP", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("CAR SPECS", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        // TAB CONTENT
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // UPGRADES TAB
                    UpgradeCategory.values().forEach { cat ->
                        val currentLvl = when (cat) {
                            UpgradeCategory.ENGINE -> status?.engineLevel ?: 1
                            UpgradeCategory.TURBO -> status?.turboLevel ?: 1
                            UpgradeCategory.TIRES -> status?.tiresLevel ?: 1
                            UpgradeCategory.BRAKES -> status?.brakesLevel ?: 1
                            UpgradeCategory.HANDLING -> status?.handlingLevel ?: 1
                            UpgradeCategory.NITRO -> status?.nitroLevel ?: 1
                            UpgradeCategory.MAX_SPEED -> status?.maxSpeedLevel ?: 1
                        }

                        val cost = currentLvl * 800
                        val isMax = currentLvl >= 5
                        val canAfford = profile.coins >= cost

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(CutCornerShape(10.dp))
                                .background(CarbonCard)
                                .border(1.dp, CarbonCardBorder, CutCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cat.title.uppercase(),
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "LVL $currentLvl/5",
                                            color = TurboGold,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = cat.description,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // 5-segment upgrade bar
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        for (i in 1..5) {
                                            Box(
                                                modifier = Modifier
                                                    .width(18.dp)
                                                    .height(5.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(if (i <= currentLvl) NitroGreen else Color(0xFF1E293B))
                                            )
                                        }
                                    }
                                }

                                if (isUnlocked) {
                                    if (isMax) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CutCornerShape(6.dp))
                                                .background(NitroGreen.copy(alpha = 0.2f))
                                                .border(1.dp, NitroGreen, CutCornerShape(6.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "MAX",
                                                color = NitroGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    } else {
                                        ArcadeButton(
                                            text = "$cost C",
                                            icon = Icons.Default.MonetizationOn,
                                            colorScheme = if (canAfford) ButtonColorScheme.GOLD else ButtonColorScheme.DARK,
                                            enabled = canAfford,
                                            onClick = { onUpgradeCar(carDef.id, cat) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // PAINT SHOP TAB
                    Text(
                        text = "SELECT CUSTOM PAINT",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val paintOptions = listOf(
                        0xFFE11D48 to "Crimson Red",
                        0xFF2563EB to "Cobalt Blue",
                        0xFFF59E0B to "Sunset Orange",
                        0xFF10B981 to "Emerald Green",
                        0xFF8B5CF6 to "Royal Violet",
                        0xFF06B6D4 to "Cyan Glow",
                        0xFFFACC15 to "Cyber Gold",
                        0xFF1E293B to "Stealth Carbon"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        paintOptions.take(4).forEach { (colorHex, name) ->
                            val isColorSelected = status?.selectedColorHex == colorHex
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(
                                        2.dp,
                                        if (isColorSelected) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable(enabled = isUnlocked) {
                                        onSetColor(carDef.id, colorHex)
                                    }
                            ) {
                                if (isColorSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        paintOptions.drop(4).forEach { (colorHex, name) ->
                            val isColorSelected = status?.selectedColorHex == colorHex
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(
                                        2.dp,
                                        if (isColorSelected) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable(enabled = isUnlocked) {
                                        onSetColor(carDef.id, colorHex)
                                    }
                            ) {
                                if (isColorSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // SPECS TAB
                    StatBar(label = "Top Speed", currentValue = carDef.baseSpeed, color = RacingRed)
                    Spacer(modifier = Modifier.height(12.dp))
                    StatBar(label = "Acceleration", currentValue = carDef.baseAcceleration, color = TurboGold)
                    Spacer(modifier = Modifier.height(12.dp))
                    StatBar(label = "Handling", currentValue = carDef.baseHandling, color = NeonCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    StatBar(label = "Braking", currentValue = carDef.baseBraking, color = NitroGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    StatBar(label = "Nitro Surge", currentValue = carDef.baseNitro, color = Color(0xFFC084FC))
                }
            }
        }

        // BOTTOM ACTION BAR (Select Car / Buy Car)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isUnlocked) {
                if (isEquipped) {
                    ArcadeButton(
                        text = "CAR CURRENTLY EQUIPPED",
                        colorScheme = ButtonColorScheme.DARK,
                        enabled = false,
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ArcadeButton(
                        text = "EQUIP ${carDef.name.uppercase()}",
                        colorScheme = ButtonColorScheme.PRIMARY,
                        isLarge = true,
                        onClick = { onSelectCar(carDef.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                val canAfford = profile.coins >= carDef.unlockCoins && profile.gems >= carDef.unlockGems
                val priceLabel = if (carDef.unlockGems > 0) {
                    "UNLOCK FOR ${carDef.unlockCoins} COINS + ${carDef.unlockGems} GEMS"
                } else {
                    "UNLOCK FOR ${carDef.unlockCoins} COINS"
                }
                ArcadeButton(
                    text = priceLabel,
                    colorScheme = if (canAfford) ButtonColorScheme.GOLD else ButtonColorScheme.DARK,
                    enabled = canAfford,
                    isLarge = true,
                    onClick = { onBuyCar(carDef.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
