package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ErrorLogEntity
import com.example.data.local.GlobalSettingsState
import com.example.data.model.AppLanguage
import com.example.data.model.RuntimePluginInfo
import com.example.data.model.ShaderFilter
import com.example.data.model.ThemePreset
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AccentPink
import com.example.ui.theme.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonSwitch
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.StorageStatsUi

@Composable
fun SettingsScreen(
    settings: GlobalSettingsState,
    storageStats: StorageStatsUi,
    plugins: List<RuntimePluginInfo>,
    errorLogs: List<ErrorLogEntity>,
    onToggleGraphicsFilters: (Boolean) -> Unit,
    onToggleAudioMidi: (Boolean) -> Unit,
    onToggleControlsMapping: (Boolean) -> Unit,
    onTogglePerformanceBoost: (Boolean) -> Unit,
    onToggleCloudBackup: (Boolean) -> Unit,
    onToggleLanguage: (AppLanguage) -> Unit,
    onCycleTheme: (ThemePreset) -> Unit,
    onSelectShader: (ShaderFilter) -> Unit,
    onSelectSoundFont: (String) -> Unit,
    onToggleLowBattery: (Boolean) -> Unit,
    onExportLocalBackup: (Uri) -> Unit,
    onImportLocalBackup: (Uri) -> Unit,
    onClearCache: () -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    var activeModal by remember { mutableStateOf<String?>(null) }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri -> if (uri != null) onExportLocalBackup(uri) },
    )
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) onImportLocalBackup(uri) },
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = strings.settingsTitle,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp),
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        // Exact 8 Glass Pill Settings Rows matching Phone 4!
        item {
            SettingsTogglePillRow(
                title = strings.graphicsFilters,
                subtitle = if (strings.isArabic) settings.defaultShader.labelAr else settings.defaultShader.labelEn,
                checked = settings.graphicsFiltersEnabled,
                onCheckedChange = onToggleGraphicsFilters,
                onRowClick = { activeModal = "GRAPHICS" },
                testTag = "setting_graphics_row",
            )
        }
        item {
            SettingsTogglePillRow(
                title = strings.audioMidi,
                subtitle = settings.selectedSoundFont,
                checked = settings.audioMidiEnabled,
                onCheckedChange = onToggleAudioMidi,
                onRowClick = { activeModal = "AUDIO" },
                testTag = "setting_audio_row",
            )
        }
        item {
            SettingsTogglePillRow(
                title = strings.controlsMapping,
                subtitle = if (strings.isArabic) "تخصيص يد التحكم واللمس" else "Bluetooth / USB Gamepad & Touch",
                checked = settings.controlsMappingEnabled,
                onCheckedChange = onToggleControlsMapping,
                onRowClick = { activeModal = "CONTROLS" },
                testTag = "setting_controls_row",
            )
        }
        item {
            SettingsTogglePillRow(
                title = strings.performanceBoost,
                subtitle = if (settings.lowBatteryMode) {
                    if (strings.isArabic) "وضع توفير البطارية (30 FPS)" else "Low Battery Saver (30 FPS)"
                } else {
                    if (strings.isArabic) "تسريع GPU وJIT" else "GPU & JIT Frame Pacing"
                },
                checked = settings.performanceBoostEnabled,
                onCheckedChange = onTogglePerformanceBoost,
                onRowClick = { activeModal = "PERFORMANCE" },
                testTag = "setting_performance_row",
            )
        }
        item {
            SettingsTogglePillRow(
                title = strings.cloudBackup,
                subtitle = if (strings.isArabic) "نسخ احتياطي محلي (.rpcbak)" else "Local Backup (.rpcbak) • Cloud off",
                checked = settings.cloudBackupEnabled,
                onCheckedChange = {
                    onToggleCloudBackup(it)
                    activeModal = "BACKUP"
                },
                onRowClick = { activeModal = "BACKUP" },
                testTag = "setting_backup_row",
            )
        }
        item {
            SettingsTogglePillRow(
                title = strings.languageSetting,
                subtitle = settings.language.displayName,
                checked = settings.language == AppLanguage.ARABIC,
                onCheckedChange = { isAr ->
                    onToggleLanguage(if (isAr) AppLanguage.ARABIC else AppLanguage.ENGLISH)
                },
                onRowClick = {
                    val next = if (settings.language == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
                    onToggleLanguage(next)
                },
                testTag = "setting_language_row",
            )
        }
        item {
            SettingsTogglePillRow(
                title = strings.darkNeonTheme,
                subtitle = if (strings.isArabic) settings.themePreset.labelAr else settings.themePreset.labelEn,
                checked = true,
                onCheckedChange = {
                    val vals = ThemePreset.entries
                    onCycleTheme(vals[(vals.indexOf(settings.themePreset) + 1) % vals.size])
                },
                onRowClick = {
                    val vals = ThemePreset.entries
                    onCycleTheme(vals[(vals.indexOf(settings.themePreset) + 1) % vals.size])
                },
                testTag = "setting_theme_row",
            )
        }
        item {
            SettingsTogglePillRow(
                title = strings.aboutSetting,
                subtitle = "Retro PC Emulator v1.0 • Wine / DOSBox-X / ScummVM",
                checked = activeModal == "ABOUT",
                onCheckedChange = { if (it) activeModal = "ABOUT" else activeModal = null },
                onRowClick = { activeModal = "ABOUT" },
                testTag = "setting_about_row",
            )
        }

        // Additional Utility Cards: Storage Manager, Plugin Updater, Error Logs
        item {
            Spacer(modifier = Modifier.height(6.dp))
            SettingsActionCard(
                title = strings.storageManager,
                detail = if (strings.isArabic) {
                    "الألعاب: %.1f MB • اللقطات: %.1f MB • الكاش: %.1f MB".format(storageStats.gamesMb, storageStats.screenshotsMb, storageStats.cacheMb)
                } else {
                    "Games: %.1f MB • Shots: %.1f MB • Cache: %.1f MB".format(storageStats.gamesMb, storageStats.screenshotsMb, storageStats.cacheMb)
                },
                actionLabel = if (strings.isArabic) "مسح الكاش" else "Clear Cache",
                onClick = onClearCache,
                testTag = "setting_storage_card",
            )
        }
        item {
            SettingsActionCard(
                title = strings.pluginUpdater,
                detail = "${plugins.size} modules up to date (Wine 9.0, Box64, DOSBox-X, ScummVM, DXVK)",
                actionLabel = if (strings.isArabic) "عرض" else "Inspect",
                onClick = { activeModal = "PLUGINS" },
                testTag = "setting_plugins_card",
            )
        }
        item {
            SettingsActionCard(
                title = strings.errorLogs,
                detail = if (strings.isArabic) "${errorLogs.size} سجل تشخيص" else "${errorLogs.size} diagnostic entries",
                actionLabel = if (strings.isArabic) "فتح السجل" else "View Logs",
                onClick = { activeModal = "LOGS" },
                testTag = "setting_logs_card",
            )
        }
    }

    // Detail Dialogs
    when (activeModal) {
        "GRAPHICS" -> {
            AlertDialog(
                onDismissRequest = { activeModal = null },
                containerColor = Color(0xFF171333),
                title = { Text(strings.graphicsFilters, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShaderFilter.entries.forEach { sf ->
                            val selected = settings.defaultShader == sf
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (selected) NeonPurple.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f))
                                    .clickable { onSelectShader(sf) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(if (strings.isArabic) sf.labelAr else sf.labelEn, color = Color.White)
                                if (selected) Text("ON", color = NeonCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { activeModal = null }) { Text(strings.doneBtn, color = NeonCyan) } },
            )
        }
        "AUDIO" -> {
            val fonts = listOf("FluidR3_GM.sf2", "TimGM6mb.sf2", "Roland_SC55_Lite.sf2")
            AlertDialog(
                onDismissRequest = { activeModal = null },
                containerColor = Color(0xFF171333),
                title = { Text(strings.audioMidi, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        fonts.forEach { sf ->
                            val selected = settings.selectedSoundFont == sf
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (selected) NeonPurple.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f))
                                    .clickable { onSelectSoundFont(sf) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(sf, color = Color.White)
                                if (selected) Text("ACTIVE", color = NeonCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { activeModal = null }) { Text(strings.doneBtn, color = NeonCyan) } },
            )
        }
        "PERFORMANCE" -> {
            AlertDialog(
                onDismissRequest = { activeModal = null },
                containerColor = Color(0xFF171333),
                title = { Text(strings.performanceBoost, color = Color.White) },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(if (strings.isArabic) "توفير البطارية (30 FPS)" else "Low Battery Saver (30 FPS)", color = Color.White)
                        NeonSwitch(checked = settings.lowBatteryMode, onCheckedChange = onToggleLowBattery)
                    }
                },
                confirmButton = { TextButton(onClick = { activeModal = null }) { Text(strings.doneBtn, color = NeonCyan) } },
            )
        }
        "BACKUP" -> {
            AlertDialog(
                onDismissRequest = { activeModal = null },
                containerColor = Color(0xFF171333),
                title = { Text(strings.cloudBackup, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(strings.cloudBackupUnavailableNote, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(NeonPurple)
                                .clickable { exportBackupLauncher.launch("retro_pc_backup.rpcbak") }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(strings.localBackupExport, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, NeonCyan, RoundedCornerShape(16.dp))
                                .clickable { importBackupLauncher.launch(arrayOf("*/*")) }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(strings.localBackupImport, color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { activeModal = null }) { Text(strings.doneBtn, color = NeonCyan) } },
            )
        }
        "PLUGINS" -> {
            AlertDialog(
                onDismissRequest = { activeModal = null },
                containerColor = Color(0xFF171333),
                title = { Text(strings.pluginUpdater, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        plugins.forEach { p ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(p.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(if (strings.isArabic) p.descriptionAr else p.descriptionEn, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                                }
                                Text("v${p.version}", color = NeonEmerald, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { activeModal = null }) { Text(strings.doneBtn, color = NeonCyan) } },
            )
        }
        "LOGS" -> {
            AlertDialog(
                onDismissRequest = { activeModal = null },
                containerColor = Color(0xFF171333),
                title = { Text(strings.errorLogs, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (errorLogs.isEmpty()) {
                            Text(if (strings.isArabic) "لا توجد أخطاء مسجلة." else "No diagnostic errors logged.", color = NeonEmerald)
                        } else {
                            errorLogs.take(8).forEach { log ->
                                Text(
                                    text = "[${log.tag}] ${if (strings.isArabic) log.messageAr else log.messageEn}",
                                    color = AccentPink,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                },
                dismissButton = {
                    if (errorLogs.isNotEmpty()) {
                        TextButton(onClick = onClearLogs) { Text("Clear", color = AccentPink) }
                    }
                },
                confirmButton = { TextButton(onClick = { activeModal = null }) { Text(strings.doneBtn, color = NeonCyan) } },
            )
        }
        "CONTROLS", "ABOUT" -> {
            AlertDialog(
                onDismissRequest = { activeModal = null },
                containerColor = Color(0xFF171333),
                title = {
                    Text(
                        text = if (activeModal == "CONTROLS") strings.controlsMapping else strings.aboutSetting,
                        color = Color.White,
                    )
                },
                text = {
                    Text(
                        text = if (activeModal == "CONTROLS") {
                            if (strings.isArabic) {
                                "يدعم أزرار اللمس القابلة للتحريك وتغيير الحجم والشفافية، والماوس المباشر/التراك باد، ولوحة المفاتيح الكاملة، وأي يد تحكم Bluetooth أو USB تلقائياً."
                            } else {
                                "Supports draggable/resizable touch overlays, Direct & Trackpad mouse, full AT-102 virtual keyboard, and automatic Bluetooth/USB Gamepad mapping."
                            }
                        } else {
                            "Retro PC Emulator v1.0\nEngines: Wine + Box86/Box64, DOSBox-X, ScummVM\nWrappers: DXVK (DX 3-9), cnc-ddraw, dgVoodoo2, nGlide\nUI: Neon Purple/Cyan Glassmorphism"
                        },
                        color = TextSecondary,
                    )
                },
                confirmButton = { TextButton(onClick = { activeModal = null }) { Text(strings.doneBtn, color = NeonCyan) } },
            )
        }
    }
}

@Composable
private fun SettingsTogglePillRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onRowClick: () -> Unit,
    testTag: String,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .minimumInteractiveComponentSize()
            .clickable(onClick = onRowClick),
        cornerRadius = 24.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            NeonSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                testTag = "${testTag}_switch",
            )
        }
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    detail: String,
    actionLabel: String,
    onClick: () -> Unit,
    testTag: String,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick),
        cornerRadius = 24.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(text = detail, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeonPurple.copy(alpha = 0.28f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelMedium, color = NeonCyan)
            }
        }
    }
}
