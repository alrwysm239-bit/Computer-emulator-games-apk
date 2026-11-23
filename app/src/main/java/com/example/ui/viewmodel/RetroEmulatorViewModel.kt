package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ControlProfileEntity
import com.example.data.local.ErrorLogEntity
import com.example.data.local.GameEntity
import com.example.data.local.GlobalSettingsState
import com.example.data.local.PreferencesRepository
import com.example.data.local.SaveStateEntity
import com.example.data.model.AppLanguage
import com.example.data.model.DownloadTaskState
import com.example.data.model.EngineType
import com.example.data.model.GameCategory
import com.example.data.model.RuntimePluginInfo
import com.example.data.model.ShaderFilter
import com.example.data.model.StoreCatalogItem
import com.example.data.model.ThemePreset
import com.example.data.model.WindowsCompatibility
import com.example.data.repository.RetroRepository
import com.example.engine.ControlLayoutConfig
import com.example.engine.ImportedGameInspection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StorageStatsUi(
    val gamesMb: Float = 0f,
    val screenshotsMb: Float = 0f,
    val cacheMb: Float = 0f,
)

data class ImportFlowState(
    val selectedCompat: WindowsCompatibility = WindowsCompatibility.AUTO,
    val selectedEngine: EngineType = EngineType.AUTO,
    val selectedResolutionW: Int = 640,
    val selectedResolutionH: Int = 480,
    val isImporting: Boolean = false,
    val pendingInspection: ImportedGameInspection? = null,
    val errorMessage: String? = null,
)

class RetroEmulatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RetroRepository(application.applicationContext)
    private val prefs = PreferencesRepository(application.applicationContext)

    val settingsState: StateFlow<GlobalSettingsState> = prefs.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GlobalSettingsState(),
    )

    val allGames: StateFlow<List<GameEntity>> = repository.gamesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val errorLogs: StateFlow<List<ErrorLogEntity>> = repository.errorLogsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(GameCategory.ALL)
    val selectedCategory: StateFlow<GameCategory> = _selectedCategory.asStateFlow()

    private val _selectedFormatFilter = MutableStateFlow<String?>(null)
    val selectedFormatFilter: StateFlow<String?> = _selectedFormatFilter.asStateFlow()

    private val _favoritesOnly = MutableStateFlow(false)
    val favoritesOnly: StateFlow<Boolean> = _favoritesOnly.asStateFlow()

    val filteredGames: StateFlow<List<GameEntity>> = combine(
        allGames,
        _searchQuery,
        _selectedCategory,
        _selectedFormatFilter,
        _favoritesOnly,
    ) { games, query, category, fmt, favOnly ->
        games.filter { game ->
            val matchesQuery = query.isBlank() ||
                game.title.contains(query, ignoreCase = true) ||
                game.fileFormat.contains(query, ignoreCase = true) ||
                game.engineType.contains(query, ignoreCase = true)
            val matchesCat = category == GameCategory.ALL || game.category == category.name
            val matchesFmt = fmt == null || game.fileFormat.equals(fmt, ignoreCase = true)
            val matchesFav = !favOnly || game.isFavorite
            matchesQuery && matchesCat && matchesFmt && matchesFav
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _importState = MutableStateFlow(ImportFlowState())
    val importState: StateFlow<ImportFlowState> = _importState.asStateFlow()

    private val _downloads = MutableStateFlow<Map<String, DownloadTaskState>>(emptyMap())
    val downloads: StateFlow<Map<String, DownloadTaskState>> = _downloads.asStateFlow()

    private val _storageStats = MutableStateFlow(StorageStatsUi())
    val storageStats: StateFlow<StorageStatsUi> = _storageStats.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    val storeCatalog: List<StoreCatalogItem> = repository.getStoreCatalog()
    val runtimePlugins: List<RuntimePluginInfo> = repository.getRuntimePlugins()

    init {
        viewModelScope.launch {
            repository.ensureStarterLibrarySeeded()
            refreshStorageStats()
        }
    }

    fun observeGame(gameId: Long): Flow<GameEntity?> = repository.observeGame(gameId)
    fun observeSaveStates(gameId: Long): Flow<List<SaveStateEntity>> = repository.observeSaveStates(gameId)
    fun observeControlProfiles(gameId: Long): Flow<List<ControlProfileEntity>> = repository.observeControlProfiles(gameId)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategoryFilter(category: GameCategory) {
        _selectedCategory.value = category
    }

    fun updateFormatFilter(format: String?) {
        _selectedFormatFilter.value = if (_selectedFormatFilter.value == format) null else format
    }

    fun toggleFavoritesOnly() {
        _favoritesOnly.value = !_favoritesOnly.value
    }

    fun cycleImportCompatibility() {
        val values = WindowsCompatibility.entries
        _importState.update { state ->
            val next = values[(values.indexOf(state.selectedCompat) + 1) % values.size]
            state.copy(selectedCompat = next)
        }
    }

    fun cycleImportEngine() {
        val values = EngineType.entries
        _importState.update { state ->
            val next = values[(values.indexOf(state.selectedEngine) + 1) % values.size]
            state.copy(selectedEngine = next)
        }
    }

    fun cycleImportResolution() {
        val list = com.example.data.model.SupportedResolutions
        _importState.update { state ->
            val curIdx = list.indexOfFirst { it.width == state.selectedResolutionW && it.height == state.selectedResolutionH }
            val next = list[(curIdx + 1) % list.size]
            state.copy(selectedResolutionW = next.width, selectedResolutionH = next.height)
        }
    }

    fun handlePickedUri(uri: Uri, isDirectoryTree: Boolean) {
        val current = _importState.value
        _importState.update { it.copy(isImporting = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.inspectAndImportUri(
                uri = uri,
                isDirectoryTree = isDirectoryTree,
                preferredEngine = current.selectedEngine,
                preferredCompatibility = current.selectedCompat,
            )
            result.onSuccess { inspection ->
                if (inspection.allExecutables.size <= 1 && !inspection.hasSetupExe) {
                    repository.finalizeGameImport(
                        inspection = inspection,
                        customTitle = inspection.suggestedTitle,
                        chosenExecutablePath = inspection.primaryExecutablePath,
                        engineType = if (current.selectedEngine == EngineType.AUTO) inspection.detectedEngine else current.selectedEngine,
                        compatibilityMode = if (current.selectedCompat == WindowsCompatibility.AUTO) inspection.detectedCompatibility else current.selectedCompat,
                        resolutionWidth = current.selectedResolutionW,
                        resolutionHeight = current.selectedResolutionH,
                    )
                    _importState.update { it.copy(isImporting = false, pendingInspection = null) }
                    _statusBanner.value = "Imported ${inspection.suggestedTitle} (${inspection.format.badge})"
                    refreshStorageStats()
                } else {
                    _importState.update { it.copy(isImporting = false, pendingInspection = inspection) }
                }
            }.onFailure { err ->
                _importState.update {
                    it.copy(
                        isImporting = false,
                        errorMessage = err.message ?: "Unable to read selected file.",
                    )
                }
            }
        }
    }

    fun confirmPendingImport(
        customTitle: String,
        chosenExecutablePath: String,
        engineType: EngineType,
        compatibilityMode: WindowsCompatibility,
    ) {
        val inspection = _importState.value.pendingInspection ?: return
        val resW = _importState.value.selectedResolutionW
        val resH = _importState.value.selectedResolutionH
        viewModelScope.launch {
            repository.finalizeGameImport(
                inspection = inspection,
                customTitle = customTitle,
                chosenExecutablePath = chosenExecutablePath,
                engineType = engineType,
                compatibilityMode = compatibilityMode,
                resolutionWidth = resW,
                resolutionHeight = resH,
            )
            _importState.update { it.copy(pendingInspection = null) }
            _statusBanner.value = "Added $customTitle to Library"
            refreshStorageStats()
        }
    }

    fun dismissPendingImport() {
        _importState.update { it.copy(pendingInspection = null, errorMessage = null) }
    }

    fun toggleFavorite(game: GameEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(game.id, !game.isFavorite)
        }
    }

    fun updateGameConfig(game: GameEntity) {
        viewModelScope.launch {
            repository.updateGame(game)
            _statusBanner.value = "Saved settings for ${game.title}"
        }
    }

    fun deleteGame(game: GameEntity) {
        viewModelScope.launch {
            repository.deleteGame(game)
            refreshStorageStats()
        }
    }

    fun recordPlayTime(gameId: Long, deltaSeconds: Long) {
        viewModelScope.launch {
            repository.recordPlayTime(gameId, deltaSeconds)
        }
    }

    fun saveGameState(game: GameEntity, slotIndex: Int, snapshotJson: String, bitmap: Bitmap?) {
        viewModelScope.launch {
            repository.saveState(game, slotIndex, snapshotJson, bitmap)
            _statusBanner.value = "Saved Slot $slotIndex"
        }
    }

    fun saveScreenshot(gameTitle: String, bitmap: Bitmap) {
        viewModelScope.launch {
            val file = repository.saveStandaloneScreenshot(gameTitle, bitmap)
            _statusBanner.value = "Screenshot saved: ${file.name}"
            refreshStorageStats()
        }
    }

    fun saveControlProfile(gameId: Long, name: String, layout: ControlLayoutConfig) {
        viewModelScope.launch {
            repository.saveControlLayout(gameId, name, layout)
            _statusBanner.value = "Saved control profile: $name"
        }
    }

    fun activateControlProfile(gameId: Long, profileId: Long) {
        viewModelScope.launch {
            repository.activateControlProfile(gameId, profileId)
        }
    }

    fun downloadStoreItem(item: StoreCatalogItem) {
        if (_downloads.value[item.id]?.isDownloading == true) return
        viewModelScope.launch {
            repository.downloadAndInstallStoreGame(item).collect { progress ->
                _downloads.update { it + (item.id to progress) }
                if (progress.isCompleted) {
                    refreshStorageStats()
                }
            }
        }
    }

    fun exportLocalBackup(uri: Uri) {
        viewModelScope.launch {
            val res = repository.exportLocalBackupToUri(uri)
            res.onSuccess { count ->
                _statusBanner.value = "Exported $count saves & control profiles to .rpcbak"
            }.onFailure { err ->
                _statusBanner.value = "Backup failed: ${err.message}"
            }
        }
    }

    fun importLocalBackup(uri: Uri) {
        viewModelScope.launch {
            val res = repository.importLocalBackupFromUri(uri)
            res.onSuccess { count ->
                _statusBanner.value = "Restored $count items from local backup"
            }.onFailure { err ->
                _statusBanner.value = "Restore failed: ${err.message}"
            }
        }
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            val (games, shots, cache) = repository.getStorageStatsBytes()
            _storageStats.value = StorageStatsUi(
                gamesMb = games / (1024f * 1024f),
                screenshotsMb = shots / (1024f * 1024f),
                cacheMb = cache / (1024f * 1024f),
            )
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
            refreshStorageStats()
            _statusBanner.value = "Cache cleared"
        }
    }

    fun clearErrorLogs() {
        viewModelScope.launch { repository.clearErrorLogs() }
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    // Settings toggles
    fun setGraphicsFilters(enabled: Boolean) = viewModelScope.launch { prefs.setGraphicsFilters(enabled) }
    fun setAudioMidi(enabled: Boolean) = viewModelScope.launch { prefs.setAudioMidi(enabled) }
    fun setControlsMapping(enabled: Boolean) = viewModelScope.launch { prefs.setControlsMapping(enabled) }
    fun setPerformanceBoost(enabled: Boolean) = viewModelScope.launch { prefs.setPerformanceBoost(enabled) }
    fun setCloudBackup(enabled: Boolean) = viewModelScope.launch { prefs.setCloudBackup(enabled) }
    fun setLanguage(language: AppLanguage) = viewModelScope.launch { prefs.setLanguage(language) }
    fun setThemePreset(preset: ThemePreset) = viewModelScope.launch { prefs.setThemePreset(preset) }
    fun setDefaultShader(shader: ShaderFilter) = viewModelScope.launch { prefs.setDefaultShader(shader) }
    fun setSoundFont(name: String) = viewModelScope.launch { prefs.setSoundFont(name) }
    fun setLowBatteryMode(enabled: Boolean) = viewModelScope.launch { prefs.setLowBatteryMode(enabled) }
    fun setWineVersion(version: String) = viewModelScope.launch { prefs.setWineVersion(version) }
}
