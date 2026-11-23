package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.GameEntity
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.ProvideAppStrings
import com.example.ui.screens.EmulatorPlayScreen
import com.example.ui.screens.GameConfigModalSheet
import com.example.ui.screens.GamesAndImportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.theme.BottomNavTabItem
import com.example.ui.theme.FloatingGlassBottomBar
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RetroPcTheme
import com.example.ui.theme.RetroSpaceBackground
import com.example.ui.viewmodel.RetroEmulatorViewModel

const val ROUTE_SPLASH = "splash"
const val ROUTE_HOME = "home"
const val ROUTE_GAMES = "games"
const val ROUTE_STORE = "store"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_PLAY = "play"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        setContent {
            RetroEmulatorApp()
        }
    }
}

@Composable
fun RetroEmulatorApp(
    viewModel: RetroEmulatorViewModel = viewModel(),
) {
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val allGames by viewModel.allGames.collectAsStateWithLifecycle()
    val filteredGames by viewModel.filteredGames.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedFormatFilter by viewModel.selectedFormatFilter.collectAsStateWithLifecycle()
    val favoritesOnly by viewModel.favoritesOnly.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
    val errorLogs by viewModel.errorLogs.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_SPLASH
    val snackbarHostState = remember { SnackbarHostState() }
    var configGame by remember { mutableStateOf<GameEntity?>(null) }

    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    RetroPcTheme(
        themePreset = settings.themePreset,
        language = settings.language,
    ) {
        ProvideAppStrings(language = settings.language) {
            val strings = LocalAppStrings.current
            val navItems = listOf(
                BottomNavTabItem(ROUTE_HOME, strings.navHome, "tab_home"),
                BottomNavTabItem(ROUTE_GAMES, strings.navGames, "tab_games"),
                BottomNavTabItem(ROUTE_STORE, strings.navStore, "tab_store"),
                BottomNavTabItem(ROUTE_SETTINGS, strings.navSettings, "tab_settings"),
            )
            val showBottomBar = currentRoute in setOf(ROUTE_HOME, ROUTE_GAMES, ROUTE_STORE, ROUTE_SETTINGS)

            RetroSpaceBackground {
                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_SPLASH,
                        enterTransition = { fadeIn(animationSpec = tween(220)) },
                        exitTransition = { fadeOut(animationSpec = tween(180)) },
                    ) {
                        composable(ROUTE_SPLASH) {
                            SplashScreen(
                                onFinished = {
                                    navController.navigate(ROUTE_HOME) {
                                        popUpTo(ROUTE_SPLASH) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(ROUTE_HOME) {
                            HomeScreen(
                                games = allGames,
                                onPlayGame = { game -> navController.navigate("$ROUTE_PLAY/${game.id}") },
                                onOpenGameConfig = { game -> configGame = game },
                                onNavigateToImport = {
                                    navController.navigate(ROUTE_GAMES) {
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }

                        composable(ROUTE_GAMES) {
                            GamesAndImportScreen(
                                games = filteredGames,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                selectedFormatFilter = selectedFormatFilter,
                                favoritesOnly = favoritesOnly,
                                importState = importState,
                                onSearchChange = viewModel::updateSearchQuery,
                                onCategorySelect = viewModel::updateCategoryFilter,
                                onFormatToggle = viewModel::updateFormatFilter,
                                onFavoritesToggle = viewModel::toggleFavoritesOnly,
                                onCycleCompat = viewModel::cycleImportCompatibility,
                                onCycleEngine = viewModel::cycleImportEngine,
                                onCycleResolution = viewModel::cycleImportResolution,
                                onFilePicked = { uri -> viewModel.handlePickedUri(uri, isDirectoryTree = false) },
                                onFolderPicked = { uri -> viewModel.handlePickedUri(uri, isDirectoryTree = true) },
                                onConfirmPendingImport = viewModel::confirmPendingImport,
                                onDismissPendingImport = viewModel::dismissPendingImport,
                                onPlayGame = { game -> navController.navigate("$ROUTE_PLAY/${game.id}") },
                                onToggleFavorite = viewModel::toggleFavorite,
                                onOpenGameConfig = { game -> configGame = game },
                                onDeleteGame = viewModel::deleteGame,
                            )
                        }

                        composable(ROUTE_STORE) {
                            StoreScreen(
                                catalog = viewModel.storeCatalog,
                                installedGames = allGames,
                                downloads = downloads,
                                onDownloadItem = viewModel::downloadStoreItem,
                                onPlayInstalled = { game -> navController.navigate("$ROUTE_PLAY/${game.id}") },
                            )
                        }

                        composable(ROUTE_SETTINGS) {
                            SettingsScreen(
                                settings = settings,
                                storageStats = storageStats,
                                plugins = viewModel.runtimePlugins,
                                errorLogs = errorLogs,
                                onToggleGraphicsFilters = viewModel::setGraphicsFilters,
                                onToggleAudioMidi = viewModel::setAudioMidi,
                                onToggleControlsMapping = viewModel::setControlsMapping,
                                onTogglePerformanceBoost = viewModel::setPerformanceBoost,
                                onToggleCloudBackup = viewModel::setCloudBackup,
                                onToggleLanguage = viewModel::setLanguage,
                                onCycleTheme = viewModel::setThemePreset,
                                onSelectShader = viewModel::setDefaultShader,
                                onSelectSoundFont = viewModel::setSoundFont,
                                onToggleLowBattery = viewModel::setLowBatteryMode,
                                onExportLocalBackup = viewModel::exportLocalBackup,
                                onImportLocalBackup = viewModel::importLocalBackup,
                                onClearCache = viewModel::clearCache,
                                onClearLogs = viewModel::clearErrorLogs,
                            )
                        }

                        composable(
                            route = "$ROUTE_PLAY/{gameId}",
                            arguments = listOf(navArgument("gameId") { type = NavType.LongType }),
                        ) { entry ->
                            val gameId = entry.arguments?.getLong("gameId") ?: 0L
                            val game by viewModel.observeGame(gameId).collectAsStateWithLifecycle(initialValue = null)
                            val saveStates by viewModel.observeSaveStates(gameId).collectAsStateWithLifecycle(initialValue = emptyList())
                            val profiles by viewModel.observeControlProfiles(gameId).collectAsStateWithLifecycle(initialValue = emptyList())

                            val activeGame = game ?: allGames.firstOrNull { it.id == gameId }
                            if (activeGame != null) {
                                EmulatorPlayScreen(
                                    game = activeGame,
                                    saveStates = saveStates,
                                    controlProfiles = profiles,
                                    audioEnabled = settings.audioMidiEnabled,
                                    lowBatteryMode = settings.lowBatteryMode,
                                    onSaveState = { slot, snap, bmp ->
                                        viewModel.saveGameState(activeGame, slot, snap, bmp)
                                    },
                                    onSaveScreenshot = { bmp ->
                                        viewModel.saveScreenshot(activeGame.title, bmp)
                                    },
                                    onSaveControlLayout = { name, layout ->
                                        viewModel.saveControlProfile(activeGame.id, name, layout)
                                    },
                                    onActivateProfile = { profileId ->
                                        viewModel.activateControlProfile(activeGame.id, profileId)
                                    },
                                    onRecordPlayTime = { secs ->
                                        viewModel.recordPlayTime(activeGame.id, secs)
                                    },
                                    onExit = { navController.popBackStack() },
                                )
                            }
                        }
                    }

                    if (showBottomBar) {
                        FloatingGlassBottomBar(
                            items = navItems,
                            currentRoute = currentRoute,
                            onSelect = { targetRoute ->
                                if (currentRoute != targetRoute) {
                                    navController.navigate(targetRoute) {
                                        popUpTo(ROUTE_HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }

                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (showBottomBar) 92.dp else 20.dp, start = 20.dp, end = 20.dp),
                    ) { data ->
                        Snackbar(
                            snackbarData = data,
                            shape = RoundedCornerShape(20.dp),
                            containerColor = Color(0xFF1D1840),
                            contentColor = NeonCyan,
                        )
                    }

                    if (configGame != null) {
                        GameConfigModalSheet(
                            game = configGame!!,
                            onSave = { updated -> viewModel.updateGameConfig(updated) },
                            onDismiss = { configGame = null },
                        )
                    }
                }
            }
        }
    }
}
