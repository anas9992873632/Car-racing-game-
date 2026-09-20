package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.data.CarCatalog
import com.example.data.TrackCatalog
import com.example.ui.screens.DailyRewardScreen
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.MissionsScreen
import com.example.ui.screens.ModeSelectScreen
import com.example.ui.screens.RaceGameScreen
import com.example.ui.screens.RaceResultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrackSelectScreen
import com.example.ui.theme.RacingRed

@Composable
fun TurboRushApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val carsStatus by viewModel.carsStatus.collectAsState()
    val trackRecords by viewModel.trackRecords.collectAsState()
    val missions by viewModel.missions.collectAsState()
    val activeEngine by viewModel.activeRacingEngine.collectAsState()

    // Android Hardware Back button handling
    BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.LOADING) {
        when (currentScreen) {
            AppScreen.RACE -> {
                activeEngine?.let { engine ->
                    if (!engine.isPaused) {
                        engine.isPaused = true
                    } else {
                        viewModel.navigateTo(AppScreen.HOME)
                    }
                } ?: viewModel.navigateTo(AppScreen.HOME)
            }
            AppScreen.RACE_RESULT -> viewModel.navigateTo(AppScreen.HOME)
            AppScreen.TRACK_SELECT -> viewModel.navigateTo(AppScreen.MODE_SELECT)
            AppScreen.MODE_SELECT -> viewModel.navigateTo(AppScreen.HOME)
            else -> viewModel.navigateTo(AppScreen.HOME)
        }
    }

    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RacingRed)
        }
        return
    }

    val currentProfile = profile!!
    val selectedCarDef = CarCatalog.getCar(currentProfile.selectedCarId)

    Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
        when (screen) {
            AppScreen.LOADING -> {
                LoadingScreen(
                    onLoadingComplete = {
                        viewModel.navigateTo(AppScreen.HOME)
                    }
                )
            }

            AppScreen.HOME -> {
                HomeScreen(
                    profile = currentProfile,
                    selectedCar = selectedCarDef,
                    onPlayClick = { viewModel.navigateTo(AppScreen.MODE_SELECT) },
                    onGarageClick = { viewModel.navigateTo(AppScreen.GARAGE) },
                    onCarsClick = { viewModel.navigateTo(AppScreen.GARAGE) },
                    onTracksClick = { viewModel.navigateTo(AppScreen.TRACK_SELECT) },
                    onMissionsClick = { viewModel.navigateTo(AppScreen.MISSIONS) },
                    onDailyRewardClick = { viewModel.navigateTo(AppScreen.DAILY_REWARD) },
                    onSettingsClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
                )
            }

            AppScreen.MODE_SELECT -> {
                ModeSelectScreen(
                    profile = currentProfile,
                    onBackClick = { viewModel.navigateTo(AppScreen.HOME) },
                    onModeSelected = { mode, diff ->
                        viewModel.selectedRaceMode = mode
                        viewModel.selectedDifficulty = diff
                        viewModel.navigateTo(AppScreen.TRACK_SELECT)
                    }
                )
            }

            AppScreen.TRACK_SELECT -> {
                TrackSelectScreen(
                    profile = currentProfile,
                    trackRecords = trackRecords,
                    onBackClick = { viewModel.navigateTo(AppScreen.MODE_SELECT) },
                    onTrackConfirmed = { track ->
                        viewModel.selectedTrackDef = track
                        viewModel.startRace()
                    }
                )
            }

            AppScreen.RACE -> {
                activeEngine?.let { engine ->
                    RaceGameScreen(
                        engine = engine,
                        controlType = currentProfile.controlType,
                        steeringSensitivity = currentProfile.steeringSensitivity,
                        onRaceFinished = { result ->
                            viewModel.onRaceFinished(result)
                        },
                        onExitRace = {
                            viewModel.navigateTo(AppScreen.HOME)
                        }
                    )
                } ?: run {
                    viewModel.navigateTo(AppScreen.HOME)
                }
            }

            AppScreen.RACE_RESULT -> {
                viewModel.currentRaceResult?.let { result ->
                    RaceResultScreen(
                        result = result,
                        onPlayAgain = {
                            viewModel.startRace()
                        },
                        onHomeClick = {
                            viewModel.navigateTo(AppScreen.HOME)
                        }
                    )
                } ?: run {
                    viewModel.navigateTo(AppScreen.HOME)
                }
            }

            AppScreen.GARAGE -> {
                GarageScreen(
                    profile = currentProfile,
                    carsStatus = carsStatus,
                    onBackClick = { viewModel.navigateTo(AppScreen.HOME) },
                    onSelectCar = { carId -> viewModel.selectCar(carId) },
                    onBuyCar = { carId -> viewModel.buyCar(carId) },
                    onUpgradeCar = { carId, category -> viewModel.upgradeCar(carId, category) },
                    onSetColor = { carId, colorHex -> viewModel.setCarColor(carId, colorHex) }
                )
            }

            AppScreen.MISSIONS -> {
                MissionsScreen(
                    profile = currentProfile,
                    missions = missions,
                    onBackClick = { viewModel.navigateTo(AppScreen.HOME) },
                    onClaimMission = { missionId -> viewModel.claimMission(missionId) }
                )
            }

            AppScreen.DAILY_REWARD -> {
                DailyRewardScreen(
                    profile = currentProfile,
                    canClaim = true,
                    onBackClick = { viewModel.navigateTo(AppScreen.HOME) },
                    onClaimReward = {
                        viewModel.claimDailyReward()
                        viewModel.navigateTo(AppScreen.HOME)
                    }
                )
            }

            AppScreen.SETTINGS -> {
                SettingsScreen(
                    profile = currentProfile,
                    onBackClick = { viewModel.navigateTo(AppScreen.HOME) },
                    onUpdateProfile = { newProfile -> viewModel.updateProfile(newProfile) },
                    onResetProgress = { viewModel.resetGameData() }
                )
            }
        }
    }
}
