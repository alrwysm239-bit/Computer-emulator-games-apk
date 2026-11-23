package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GameEntity
import com.example.data.model.AspectRatioMode
import com.example.data.model.AudioChip
import com.example.data.model.DxWrapper
import com.example.data.model.EngineType
import com.example.data.model.GameCategory
import com.example.data.model.PerformanceMode
import com.example.data.model.ShaderFilter
import com.example.data.model.SupportedResolutions
import com.example.data.model.WindowsCompatibility
import com.example.engine.ImportedGameInspection
import com.example.engine.decodeExecutablesJson
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AccentPink
import com.example.ui.theme.ColoredDot
import com.example.ui.theme.DarkTextOnCyan
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassCard
import com.example.ui.theme.LibraryTilePalette
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGradientPillButton
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonSwitch
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ImportFlowState
import java.io.File

@Composable
fun GamesAndImportScreen(
    games: List<GameEntity>,
    searchQuery: String,
    selectedCategory: GameCategory,
    selectedFormatFilter: String?,
    favoritesOnly: Boolean,
    importState: ImportFlowState,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (GameCategory) -> Unit,
    onFormatToggle: (String) -> Unit,
    onFavoritesToggle: () -> Unit,
    onCycleCompat: () -> Unit,
    onCycleEngine: () -> Unit,
    onCycleResolution: () -> Unit,
    onFilePicked: (Uri) -> Unit,
    onFolderPicked: (Uri) -> Unit,
    onConfirmPendingImport: (String, String, EngineType, WindowsCompatibility) -> Unit,
    onDismissPendingImport: () -> Unit,
    onPlayGame: (GameEntity) -> Unit,
    onToggleFavorite: (GameEntity) -> Unit,
    onOpenGameConfig: (GameEntity) -> Unit,
    onDeleteGame: (GameEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) onFilePicked(uri) },
    )
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri -> if (uri != null) onFolderPicked(uri) },
    )

    val launchFilePicker = {
        filePickerLauncher.launch(
            arrayOf(
                "application/octet-stream",
                "application/x-msdownload",
                "application/zip",
                "application/x-zip-compressed",
                "application/x-iso9660-image",
                "application/x-rar-compressed",
                "application/x-7z-compressed",
                "*/*",
            )
        )
    }

    val totalPlayMinutes = remember(games) { games.sumOf { it.playTimeSeconds } / 60L }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header: Import Game (matching Phone 2)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.importGame,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                    color = Color.White,
                )
                Text(
                    text = "${strings.totalPlaytime}: ${totalPlayMinutes}m",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan,
                )
            }
        }

        // Cyan-bordered Glass Dropzone (matching Phone 2)
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("import_dropzone")
                    .clickable(onClick = launchFilePicker),
                cornerRadius = 28.dp,
                borderBrush = Brush.linearGradient(listOf(GlassBorderCyan, NeonCyan.copy(alpha = 0.75f))),
                containerColor = Color(0xFF0A1633).copy(alpha = 0.72f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (importState.isImporting) {
                        CircularProgressIndicator(color = NeonCyan)
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = strings.importGame,
                            tint = NeonCyan,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                    Text(
                        text = strings.addFileTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = strings.addFileSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                    Row(
                        modifier = Modifier
                            .testTag("import_folder_button")
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                            .clickable { folderPickerLauncher.launch(null) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = strings.importFolder,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.importFolder,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        // Three Neon-Dot Pill Rows matching Phone 2: Compatibility, Engine, Resolution
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ImportOptionPillRow(
                    dotColor = NeonPurple,
                    text = "${strings.compatibilityLabel}: ${if (strings.isArabic) importState.selectedCompat.labelAr else importState.selectedCompat.labelEn}",
                    testTag = "import_compat_pill",
                    onClick = onCycleCompat,
                )
                ImportOptionPillRow(
                    dotColor = NeonCyan,
                    text = "${strings.engineLabel}: ${if (strings.isArabic) importState.selectedEngine.labelAr else importState.selectedEngine.labelEn}",
                    testTag = "import_engine_pill",
                    onClick = onCycleEngine,
                )
                ImportOptionPillRow(
                    dotColor = AccentPink,
                    text = "${strings.resolutionLabel}: ${importState.selectedResolutionW}x${importState.selectedResolutionH}",
                    testTag = "import_resolution_pill",
                    onClick = onCycleResolution,
                )
            }
        }

        if (importState.errorMessage != null) {
            item {
                Text(
                    text = importState.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentPink,
                )
            }
        }

        // START IMPORT Purple Pill Button matching Phone 2
        item {
            NeonGradientPillButton(
                text = strings.startImport,
                onClick = launchFilePicker,
                testTag = "start_import_button",
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Library Search & Filters Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.myLibrary,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth().testTag("library_search_input"),
                placeholder = { Text(strings.searchGamesHint, color = TextMuted) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = NeonCyan) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF14112E),
                    unfocusedContainerColor = Color(0xFF14112E),
                ),
            )
        }

        // Format & Favorites Filter Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = favoritesOnly,
                    onClick = onFavoritesToggle,
                    label = { Text(strings.favoritesOnly) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (favoritesOnly) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentPink.copy(alpha = 0.28f),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = AccentPink,
                    ),
                )
                listOf("EXE", "ZIP", "ISO", "RAR", "7Z").forEach { fmt ->
                    FilterChip(
                        selected = selectedFormatFilter == fmt,
                        onClick = { onFormatToggle(fmt) },
                        label = { Text(fmt) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPurple.copy(alpha = 0.35f),
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
                GameCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onCategorySelect(cat) },
                        label = { Text(if (strings.isArabic) cat.labelAr else cat.labelEn) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                            selectedLabelColor = NeonCyan,
                        ),
                    )
                }
            }
        }

        items(games, key = { it.id }) { game ->
            LibraryDetailRowCard(
                game = game,
                isArabic = strings.isArabic,
                onPlay = { onPlayGame(game) },
                onToggleFavorite = { onToggleFavorite(game) },
                onConfig = { onOpenGameConfig(game) },
                onDelete = { onDeleteGame(game) },
            )
        }
    }

    if (importState.pendingInspection != null) {
        MultiExecutablePickerDialog(
            inspection = importState.pendingInspection,
            isArabic = strings.isArabic,
            onConfirm = onConfirmPendingImport,
            onDismiss = onDismissPendingImport,
        )
    }
}

@Composable
private fun ImportOptionPillRow(
    dotColor: Color,
    text: String,
    testTag: String,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick),
        cornerRadius = 24.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ColoredDot(color = dotColor, size = 14.dp)
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LibraryDetailRowCard(
    game: GameEntity,
    isArabic: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onConfig: () -> Unit,
    onDelete: () -> Unit,
) {
    val tileColor = LibraryTilePalette[game.coverColorIndex.mod(LibraryTilePalette.size)]
    val engineEnum = EngineType.entries.firstOrNull { it.name == game.engineType } ?: EngineType.AUTO
    val compatEnum = WindowsCompatibility.entries.firstOrNull { it.name == game.compatibilityMode } ?: WindowsCompatibility.AUTO
    val playMins = game.playTimeSeconds / 60L

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(tileColor)
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = game.fileFormat,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${if (isArabic) engineEnum.labelAr else engineEnum.labelEn} • ${if (isArabic) compatEnum.labelAr else compatEnum.labelEn}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${game.resolutionWidth}x${game.resolutionHeight} • ${playMins}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(42.dp)) {
                    Icon(
                        imageVector = if (game.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (game.isFavorite) AccentPink else TextSecondary,
                    )
                }
                IconButton(onClick = onConfig, modifier = Modifier.size(42.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Game Settings",
                        tint = Color.White,
                    )
                }
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeonCyan),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = DarkTextOnCyan,
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(38.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun MultiExecutablePickerDialog(
    inspection: ImportedGameInspection,
    isArabic: Boolean,
    onConfirm: (String, String, EngineType, WindowsCompatibility) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf(inspection.suggestedTitle) }
    var selectedExe by remember { mutableStateOf(inspection.primaryExecutablePath) }
    var selectedEngine by remember { mutableStateOf(inspection.detectedEngine) }
    var selectedCompat by remember { mutableStateOf(inspection.detectedCompatibility) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF171333),
        title = { Text(strings.selectExecutableTitle, color = Color.White) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (inspection.hasSetupExe) {
                    Text(
                        text = strings.setupDetectedNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonCyan,
                    )
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isArabic) "اسم اللعبة" else "Game Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                inspection.allExecutables.forEach { exePath ->
                    val name = File(exePath).name
                    val isSelected = selectedExe == exePath
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) NeonPurple.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.06f))
                            .border(1.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable { selectedExe = exePath }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = name,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.weight(1f),
                        )
                        if (name.equals("setup.exe", ignoreCase = true)) {
                            Text("SETUP", color = AccentPink, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, selectedExe, selectedEngine, selectedCompat) }) {
                Text(strings.startImport, color = NeonCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel", color = TextSecondary)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameConfigModalSheet(
    game: GameEntity,
    onSave: (GameEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalAppStrings.current
    var engine by remember { mutableStateOf(EngineType.entries.firstOrNull { it.name == game.engineType } ?: EngineType.AUTO) }
    var compat by remember { mutableStateOf(WindowsCompatibility.entries.firstOrNull { it.name == game.compatibilityMode } ?: WindowsCompatibility.AUTO) }
    var perfMode by remember { mutableStateOf(PerformanceMode.entries.firstOrNull { it.name == game.performanceMode } ?: PerformanceMode.BALANCED) }
    var wrapper by remember { mutableStateOf(DxWrapper.entries.firstOrNull { it.name == game.dxWrapper } ?: DxWrapper.AUTO) }
    var aspect by remember { mutableStateOf(AspectRatioMode.entries.firstOrNull { it.name == game.aspectRatio } ?: AspectRatioMode.RATIO_4_3) }
    var shader by remember { mutableStateOf(ShaderFilter.entries.firstOrNull { it.name == game.shaderFilter } ?: ShaderFilter.CRT_SCANLINES) }
    var audioChip by remember { mutableStateOf(AudioChip.entries.firstOrNull { it.name == game.audioChip } ?: AudioChip.SOUNDBLASTER_16) }
    var resW by remember { mutableIntStateOf(game.resolutionWidth) }
    var resH by remember { mutableIntStateOf(game.resolutionHeight) }
    var fps by remember { mutableIntStateOf(game.targetFps) }
    var gpuAccel by remember { mutableStateOf(game.gpuAcceleration) }
    var installDx by remember { mutableStateOf(game.installDirectX) }
    var installVc by remember { mutableStateOf(game.installVcRedist) }
    var installDotNet by remember { mutableStateOf(game.installDotNet) }
    var volume by remember { mutableFloatStateOf(game.masterVolume) }
    var executablePath by remember { mutableStateOf(game.executablePath) }
    val allExes = remember(game.availableExecutablesJson) { decodeExecutablesJson(game.availableExecutablesJson) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF14112E),
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "${game.title} — ${strings.gameSettingsTitle}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )

            ConfigCycleRow(
                label = strings.engineLabel,
                value = if (strings.isArabic) engine.labelAr else engine.labelEn,
                onClick = {
                    val vals = EngineType.entries
                    engine = vals[(vals.indexOf(engine) + 1) % vals.size]
                },
            )
            ConfigCycleRow(
                label = strings.compatibilityLabel,
                value = if (strings.isArabic) compat.labelAr else compat.labelEn,
                onClick = {
                    val vals = WindowsCompatibility.entries
                    compat = vals[(vals.indexOf(compat) + 1) % vals.size]
                },
            )
            ConfigCycleRow(
                label = if (strings.isArabic) "طبقة الرسوميات" else "Graphics Wrapper",
                value = if (strings.isArabic) wrapper.labelAr else wrapper.labelEn,
                onClick = {
                    val vals = DxWrapper.entries
                    wrapper = vals[(vals.indexOf(wrapper) + 1) % vals.size]
                },
            )
            ConfigCycleRow(
                label = strings.resolutionLabel,
                value = "${resW}x${resH}",
                onClick = {
                    val idx = SupportedResolutions.indexOfFirst { it.width == resW && it.height == resH }
                    val next = SupportedResolutions[(idx + 1) % SupportedResolutions.size]
                    resW = next.width
                    resH = next.height
                },
            )
            ConfigCycleRow(
                label = if (strings.isArabic) "نسبة العرض" else "Aspect Ratio",
                value = if (strings.isArabic) aspect.labelAr else aspect.labelEn,
                onClick = {
                    val vals = AspectRatioMode.entries
                    aspect = vals[(vals.indexOf(aspect) + 1) % vals.size]
                },
            )
            ConfigCycleRow(
                label = if (strings.isArabic) "فلتر الشاشة" else "Shader Filter",
                value = if (strings.isArabic) shader.labelAr else shader.labelEn,
                onClick = {
                    val vals = ShaderFilter.entries
                    shader = vals[(vals.indexOf(shader) + 1) % vals.size]
                },
            )
            ConfigCycleRow(
                label = if (strings.isArabic) "وضع الأداء" else "Performance Mode",
                value = if (strings.isArabic) perfMode.labelAr else perfMode.labelEn,
                onClick = {
                    val vals = PerformanceMode.entries
                    perfMode = vals[(vals.indexOf(perfMode) + 1) % vals.size]
                },
            )
            ConfigCycleRow(
                label = if (strings.isArabic) "معدل الإطارات" else "Target FPS",
                value = "$fps FPS",
                onClick = {
                    val opts = listOf(30, 60, 90, 120)
                    fps = opts[(opts.indexOf(fps) + 1) % opts.size]
                },
            )
            ConfigCycleRow(
                label = if (strings.isArabic) "بطاقة الصوت" else "Audio Device",
                value = if (strings.isArabic) audioChip.labelAr else audioChip.labelEn,
                onClick = {
                    val vals = AudioChip.entries
                    audioChip = vals[(vals.indexOf(audioChip) + 1) % vals.size]
                },
            )

            Column {
                Text(
                    text = if (strings.isArabic) "مستوى الصوت: ${(volume * 100).toInt()}%" else "Master Volume: ${(volume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonPurple),
                )
            }

            Text(
                text = strings.directXLayersTitle,
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan,
            )
            ConfigSwitchRow(
                title = if (strings.isArabic) "تسريع GPU (Vulkan/OpenGL)" else "GPU Hardware Acceleration",
                checked = gpuAccel,
                onCheckedChange = { gpuAccel = it },
            )
            ConfigSwitchRow(
                title = "DirectX 3-9 + DXVK",
                checked = installDx,
                onCheckedChange = { installDx = it },
            )
            ConfigSwitchRow(
                title = "Visual C++ 2005–2022 Redist",
                checked = installVc,
                onCheckedChange = { installVc = it },
            )
            ConfigSwitchRow(
                title = ".NET Framework 2.0–4.0",
                checked = installDotNet,
                onCheckedChange = { installDotNet = it },
            )

            if (allExes.size > 1) {
                ConfigCycleRow(
                    label = if (strings.isArabic) "ملف التشغيل" else "Executable",
                    value = File(executablePath).name,
                    onClick = {
                        val idx = allExes.indexOf(executablePath)
                        executablePath = allExes[(idx + 1) % allExes.size]
                    },
                )
            }

            NeonGradientPillButton(
                text = if (strings.isArabic) "حفظ الإعدادات" else "SAVE CONFIG",
                onClick = {
                    onSave(
                        game.copy(
                            engineType = engine.name,
                            compatibilityMode = compat.name,
                            performanceMode = perfMode.name,
                            dxWrapper = wrapper.name,
                            resolutionWidth = resW,
                            resolutionHeight = resH,
                            aspectRatio = aspect.name,
                            shaderFilter = shader.name,
                            audioChip = audioChip.name,
                            targetFps = fps,
                            gpuAcceleration = gpuAccel,
                            installDirectX = installDx,
                            installVcRedist = installVc,
                            installDotNet = installDotNet,
                            masterVolume = volume,
                            executablePath = executablePath,
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun ConfigCycleRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick),
        cornerRadius = 20.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            Text(text = value, style = MaterialTheme.typography.labelLarge, color = NeonCyan)
        }
    }
}

@Composable
private fun ConfigSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        NeonSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
