package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ControlProfileEntity
import com.example.data.local.GameEntity
import com.example.data.local.SaveStateEntity
import com.example.data.model.AspectRatioMode
import com.example.data.model.ShaderFilter
import com.example.data.model.TouchControlMode
import com.example.engine.ControlLayoutConfig
import com.example.engine.RetroAudioSynthesizer
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AccentPink
import com.example.ui.theme.GlassCard
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.RetroSpaceBackground
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun EmulatorPlayScreen(
    game: GameEntity,
    saveStates: List<SaveStateEntity>,
    controlProfiles: List<ControlProfileEntity>,
    audioEnabled: Boolean,
    lowBatteryMode: Boolean,
    onSaveState: (Int, String, Bitmap?) -> Unit,
    onSaveScreenshot: (Bitmap) -> Unit,
    onSaveControlLayout: (String, ControlLayoutConfig) -> Unit,
    onActivateProfile: (Long) -> Unit,
    onRecordPlayTime: (Long) -> Unit,
    onExit: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val synthesizer = remember { RetroAudioSynthesizer() }
    val focusRequester = remember { FocusRequester() }

    val activeProfile = remember(controlProfiles) {
        controlProfiles.firstOrNull { it.isActive } ?: controlProfiles.firstOrNull()
    }
    var controlLayout by remember(activeProfile) {
        mutableStateOf(
            activeProfile?.let { ControlLayoutConfig.fromJson(it.layoutJson) } ?: ControlLayoutConfig()
        )
    }

    var isPaused by remember { mutableStateOf(false) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var controlMode by remember { mutableStateOf(TouchControlMode.GAMEPAD) }
    var showSaveLoadDialog by remember { mutableStateOf<String?>(null) } // "SAVE" or "LOAD"
    var showCheatsDialog by remember { mutableStateOf(false) }
    var showProfilesDialog by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }

    // Live interactive engine simulation state
    var speed by remember { mutableFloatStateOf(42f) }
    var steerX by remember { mutableFloatStateOf(0f) } // -1..1
    var roadScroll by remember { mutableFloatStateOf(0f) }
    var curveOffset by remember { mutableFloatStateOf(0f) }
    var score by remember { mutableIntStateOf(0) }
    var nitroActive by remember { mutableStateOf(false) }
    var cheatInfiniteNitro by remember { mutableStateOf(false) }
    var cheatTurboSpeed by remember { mutableStateOf(false) }
    var mouseCursorX by remember { mutableFloatStateOf(0.5f) }
    var mouseCursorY by remember { mutableFloatStateOf(0.5f) }
    var lastKeyboardInput by remember { mutableStateOf("READY") }
    var activeShader by remember(game.shaderFilter) {
        mutableStateOf(ShaderFilter.entries.firstOrNull { it.name == game.shaderFilter } ?: ShaderFilter.CRT_SCANLINES)
    }
    var sessionSeconds by remember { mutableLongStateOf(0L) }
    val targetFps = if (lowBatteryMode) 30 else game.targetFps.coerceIn(30, 120)
    var currentFps by remember { mutableIntStateOf(targetFps) }

    BackHandler {
        if (isDrawerOpen) {
            isDrawerOpen = false
        } else if (isEditMode) {
            isEditMode = false
        } else {
            onRecordPlayTime(sessionSeconds)
            onExit()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            synthesizer.release()
        }
    }

    // Main 60Hz / 30Hz game loop
    LaunchedEffect(isPaused, targetFps, cheatTurboSpeed) {
        focusRequester.requestFocus()
        val frameDelayMs = (1000L / targetFps).coerceAtLeast(8L)
        var tick = 0
        while (true) {
            delay(frameDelayMs)
            if (!isPaused) {
                val maxSpeed = if (nitroActive || cheatInfiniteNitro || cheatTurboSpeed) 145f else 98f
                val accel = if (nitroActive) 1.6f else 0.45f
                speed = (speed + accel).coerceAtMost(maxSpeed)
                roadScroll = (roadScroll + speed * 0.012f) % 1000f
                curveOffset = sin(roadScroll * 0.06f) * 0.28f + steerX * 0.45f
                score += (speed * 0.1f).toInt()
                tick++
                if (tick % targetFps == 0) {
                    sessionSeconds++
                    if (isRecording) recordingSeconds++
                    currentFps = (targetFps - (tick % 2)).coerceAtLeast(28)
                }
            }
        }
    }

    fun captureFramebufferBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(480, 320, Bitmap.Config.ARGB_8888)
        val c = AndroidCanvas(bmp)
        c.drawColor(android.graphics.Color.rgb(9, 42, 71))
        val roadPaint = Paint().apply {
            color = android.graphics.Color.rgb(66, 70, 88)
            style = Paint.Style.FILL
        }
        val path = AndroidPath().apply {
            moveTo(240f + curveOffset * 90f - 28f, 155f)
            lineTo(240f + curveOffset * 90f + 28f, 155f)
            lineTo(395f, 320f)
            lineTo(85f, 320f)
            close()
        }
        c.drawPath(path, roadPaint)
        val textPaint = Paint().apply {
            color = android.graphics.Color.CYAN
            textSize = 22f
            isAntiAlias = true
        }
        c.drawText("${game.title}  $currentFps FPS", 24f, 36f, textPaint)
        return bmp
    }

    RetroSpaceBackground(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionLeft, Key.A -> {
                            steerX = -0.85f
                            lastKeyboardInput = "LEFT"
                            true
                        }
                        Key.DirectionRight, Key.D -> {
                            steerX = 0.85f
                            lastKeyboardInput = "RIGHT"
                            true
                        }
                        Key.Spacebar, Key.ButtonA -> {
                            nitroActive = true
                            synthesizer.playTone(620.0, 90, game.masterVolume, audioEnabled)
                            true
                        }
                        Key.Escape, Key.ButtonStart -> {
                            isDrawerOpen = !isDrawerOpen
                            true
                        }
                        else -> false
                    }
                } else if (event.type == KeyEventType.KeyUp) {
                    when (event.key) {
                        Key.DirectionLeft, Key.DirectionRight, Key.A, Key.D -> {
                            steerX = 0f
                            true
                        }
                        Key.Spacebar, Key.ButtonA -> {
                            nitroActive = false
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 1. Top Game Viewport matching Phone 3 ("City Racing 60 FPS" + retro horizon road)
            val aspectModifier = when (AspectRatioMode.entries.firstOrNull { it.name == game.aspectRatio }) {
                AspectRatioMode.RATIO_16_9 -> Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                AspectRatioMode.FULL_SCREEN -> Modifier.fillMaxWidth().weight(0.46f)
                else -> Modifier.fillMaxWidth().aspectRatio(4f / 3f)
            }

            Box(
                modifier = aspectModifier
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF0A2A46))
                    .border(1.5.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
                    .pointerInput(controlMode) {
                        if (controlMode == TouchControlMode.DIRECT_MOUSE) {
                            detectTapGestures { offset ->
                                mouseCursorX = (offset.x / size.width).coerceIn(0f, 1f)
                                mouseCursorY = (offset.y / size.height).coerceIn(0f, 1f)
                                synthesizer.playTone(880.0, 40, game.masterVolume, audioEnabled)
                            }
                        } else if (controlMode == TouchControlMode.TRACKPAD_MOUSE) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                mouseCursorX = (mouseCursorX + dragAmount.x / size.width).coerceIn(0f, 1f)
                                mouseCursorY = (mouseCursorY + dragAmount.y / size.height).coerceIn(0f, 1f)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val horizonY = size.height * 0.48f
                    // Sky gradient & retro grid
                    drawRect(color = Color(0xFF0A2742), size = Size(size.width, horizonY))
                    drawRect(
                        color = Color(0xFF082038),
                        topLeft = Offset(0f, horizonY),
                        size = Size(size.width, size.height - horizonY),
                    )

                    // Perspective cyan grid lines matching Phone 3!
                    val gridSteps = 8
                    for (i in 0..gridSteps) {
                        val t = i.toFloat() / gridSteps
                        val y = horizonY + (size.height - horizonY) * (t * t)
                        drawLine(
                            color = NeonCyan.copy(alpha = 0.25f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.2.dp.toPx(),
                        )
                    }
                    for (j in -6..6) {
                        val topX = size.width * 0.5f + j * (size.width * 0.04f)
                        val botX = size.width * 0.5f + j * (size.width * 0.22f)
                        drawLine(
                            color = NeonCyan.copy(alpha = 0.20f),
                            start = Offset(topX, horizonY),
                            end = Offset(botX, size.height),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }

                    // 3D Trapezoid Road matching Phone 3
                    val topCenter = size.width * 0.5f + curveOffset * size.width * 0.18f
                    val topHalf = size.width * 0.032f
                    val botHalf = size.width * 0.20f
                    val roadPath = Path().apply {
                        moveTo(topCenter - topHalf, horizonY)
                        lineTo(topCenter + topHalf, horizonY)
                        lineTo(size.width * 0.5f + botHalf, size.height)
                        lineTo(size.width * 0.5f - botHalf, size.height)
                        close()
                    }
                    drawPath(roadPath, color = Color(0xFF474E64))

                    // Animated road dashes
                    for (d in 0..4) {
                        val phase = ((roadScroll * 0.05f + d * 0.2f) % 1f)
                        val y1 = horizonY + (size.height - horizonY) * (phase * phase)
                        val y2 = horizonY + (size.height - horizonY) * (((phase + 0.08f).coerceAtMost(1f)).let { it * it })
                        val x1 = topCenter + (size.width * 0.5f - topCenter) * phase
                        val x2 = topCenter + (size.width * 0.5f - topCenter) * (phase + 0.08f)
                        drawLine(
                            color = if (nitroActive) NeonCyan else Color.White.copy(alpha = 0.65f),
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = (1.5f + phase * 3f).dp.toPx(),
                        )
                    }

                    // Retro car sprite silhouette
                    val carX = size.width * 0.5f + steerX * size.width * 0.14f
                    val carY = size.height * 0.85f
                    drawRoundRect(
                        color = if (nitroActive) AccentPink else NeonPurple,
                        topLeft = Offset(carX - 22.dp.toPx(), carY - 12.dp.toPx()),
                        size = Size(44.dp.toPx(), 18.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                    )
                    drawCircle(
                        color = NeonCyan,
                        radius = 4.dp.toPx(),
                        center = Offset(carX - 13.dp.toPx(), carY - 3.dp.toPx()),
                    )
                    drawCircle(
                        color = NeonCyan,
                        radius = 4.dp.toPx(),
                        center = Offset(carX + 13.dp.toPx(), carY - 3.dp.toPx()),
                    )

                    // Optional CRT Scanlines overlay
                    if (activeShader == ShaderFilter.CRT_SCANLINES) {
                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                color = Color.Black.copy(alpha = 0.22f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                            )
                            y += 3.5.dp.toPx()
                        }
                    }

                    // Virtual mouse cursor when Mouse mode active
                    if (controlMode == TouchControlMode.DIRECT_MOUSE || controlMode == TouchControlMode.TRACKPAD_MOUSE) {
                        val cx = mouseCursorX * size.width
                        val cy = mouseCursorY * size.height
                        drawCircle(color = AccentPink, radius = 6.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 2.dp.toPx()))
                        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(cx, cy))
                    }
                }

                // Top HUD inside viewport: "City Racing  60 FPS" + Menu Drawer Icon + REC badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { isDrawerOpen = true },
                        modifier = Modifier
                            .testTag("ingame_menu_button")
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f)),
                    ) {
                        Icon(Icons.Rounded.Menu, contentDescription = "Menu", tint = Color.White)
                    }

                    Text(
                        text = "${game.title}  $currentFps FPS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeonCyan,
                    )

                    if (isRecording) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentPink.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("REC ${recordingSeconds}s", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        Text(
                            text = "${speed.toInt()} KM/H",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }

                if (isPaused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (strings.isArabic) "متوقف مؤقتاً" else "PAUSED",
                            style = MaterialTheme.typography.headlineMedium,
                            color = NeonCyan,
                        )
                    }
                }
            }

            // 2. Pill Action Bar: Save | Load | Cheats | Record (matching Phone 3!)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ToolPillSegment(strings.saveAction, "play_save_btn") { showSaveLoadDialog = "SAVE" }
                    VerticalDividerDot()
                    ToolPillSegment(strings.loadAction, "play_load_btn") { showSaveLoadDialog = "LOAD" }
                    VerticalDividerDot()
                    ToolPillSegment(strings.cheatsAction, "play_cheats_btn") { showCheatsDialog = true }
                    VerticalDividerDot()
                    ToolPillSegment(
                        if (isRecording) strings.stopRecordAction else strings.recordAction,
                        "play_record_btn",
                        highlight = isRecording,
                    ) {
                        if (!isRecording) {
                            recordingSeconds = 0
                            isRecording = true
                        } else {
                            isRecording = false
                        }
                    }
                }
            }

            // 3. Edit Controls Scale/Opacity Bar (when EDIT is active)
            if (isEditMode) {
                GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (strings.isArabic) "اسحب الأزرار أو العصا لتغيير مكانها" else "Drag joystick or A/B/X/Y buttons to reposition",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonCyan,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Scale", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(56.dp))
                            Slider(
                                value = controlLayout.joystickScale,
                                onValueChange = { s ->
                                    controlLayout = controlLayout.copy(
                                        joystickScale = s,
                                        buttons = controlLayout.buttons.map { it.copy(scale = s) },
                                    )
                                },
                                valueRange = 0.7f..1.4f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonPurple),
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Alpha", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(56.dp))
                            Slider(
                                value = controlLayout.joystickAlpha,
                                onValueChange = { a ->
                                    controlLayout = controlLayout.copy(
                                        joystickAlpha = a,
                                        buttons = controlLayout.buttons.map { it.copy(alpha = a) },
                                    )
                                },
                                valueRange = 0.35f..1.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = AccentPink, activeTrackColor = NeonPurple),
                            )
                        }
                    }
                }
            }

            // 4. Controls Area: Virtual Joystick + ABXY OR Full PC Keyboard
            if (controlMode == TouchControlMode.FULL_KEYBOARD) {
                VirtualPcKeyboard(
                    onKeyPress = { keyLabel ->
                        lastKeyboardInput = keyLabel
                        synthesizer.playTone(540.0, 35, game.masterVolume, audioEnabled)
                        when (keyLabel) {
                            "A", "LEFT" -> steerX = -0.8f
                            "D", "RIGHT" -> steerX = 0.8f
                            "W", "UP", "SPACE" -> nitroActive = true
                            "S", "DOWN" -> speed = (speed - 15f).coerceAtLeast(10f)
                        }
                    },
                    onKeyRelease = {
                        steerX = 0f
                        nitroActive = false
                    },
                    lastKey = lastKeyboardInput,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val density = LocalDensity.current
                    val areaW = with(density) { maxWidth.toPx() }
                    val areaH = with(density) { maxHeight.toPx() }

                    // Virtual Analog Joystick (Left)
                    val joySizeDp = (128f * controlLayout.joystickScale).dp
                    val joyPx = with(density) { joySizeDp.toPx() }
                    val joyLeft = (controlLayout.joystickX * areaW - joyPx / 2f).coerceIn(0f, (areaW - joyPx).coerceAtLeast(0f))
                    val joyTop = (controlLayout.joystickY * areaH - joyPx / 2f).coerceIn(0f, (areaH - joyPx).coerceAtLeast(0f))

                    var thumbOffset by remember { mutableStateOf(Offset.Zero) }

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(joyLeft.roundToInt(), joyTop.roundToInt()) }
                            .size(joySizeDp)
                            .alpha(controlLayout.joystickAlpha)
                            .clip(CircleShape)
                            .background(NeonPurple.copy(alpha = 0.14f))
                            .border(2.5.dp, NeonPurple, CircleShape)
                            .pointerInput(isEditMode, areaW, areaH) {
                                detectDragGestures(
                                    onDragEnd = {
                                        thumbOffset = Offset.Zero
                                        steerX = 0f
                                    },
                                    onDragCancel = {
                                        thumbOffset = Offset.Zero
                                        steerX = 0f
                                    },
                                ) { change, dragAmount ->
                                    change.consume()
                                    if (isEditMode) {
                                        val newX = (controlLayout.joystickX + dragAmount.x / areaW).coerceIn(0.12f, 0.48f)
                                        val newY = (controlLayout.joystickY + dragAmount.y / areaH).coerceIn(0.2f, 0.85f)
                                        controlLayout = controlLayout.copy(joystickX = newX, joystickY = newY)
                                    } else {
                                        val maxR = joyPx * 0.28f
                                        val raw = thumbOffset + dragAmount
                                        val dist = raw.getDistance()
                                        thumbOffset = if (dist > maxR) raw * (maxR / dist) else raw
                                        steerX = (thumbOffset.x / maxR).coerceIn(-1f, 1f)
                                        if (thumbOffset.y < -maxR * 0.4f) nitroActive = true else nitroActive = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(thumbOffset.x.roundToInt(), thumbOffset.y.roundToInt()) }
                                .size((48f * controlLayout.joystickScale).dp)
                                .clip(CircleShape)
                                .background(NeonPurple),
                        )
                    }

                    // A / B / X / Y Neon Buttons (Right) matching Phone 3 colors!
                    controlLayout.buttons.forEach { btn ->
                        val btnColor = when (btn.id) {
                            "A" -> NeonCyan
                            "B" -> AccentPink
                            "X" -> NeonPurple
                            else -> NeonAmber
                        }
                        val btnTextColor = if (btn.id == "A" || btn.id == "Y") Color(0xFF0A1120) else Color.White
                        val btnDp = (56f * btn.scale).dp
                        val btnPx = with(density) { btnDp.toPx() }
                        val left = (btn.xFraction * areaW - btnPx / 2f).coerceIn(0f, (areaW - btnPx).coerceAtLeast(0f))
                        val top = (btn.yFraction * areaH - btnPx / 2f).coerceIn(0f, (areaH - btnPx).coerceAtLeast(0f))

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
                                .size(btnDp)
                                .alpha(btn.alpha)
                                .testTag("gamepad_btn_${btn.id}")
                                .clip(CircleShape)
                                .background(btnColor)
                                .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                .pointerInput(isEditMode, areaW, areaH) {
                                    if (isEditMode) {
                                        detectDragGestures { change, drag ->
                                            change.consume()
                                            controlLayout = controlLayout.copy(
                                                buttons = controlLayout.buttons.map { item ->
                                                    if (item.id == btn.id) {
                                                        item.copy(
                                                            xFraction = (item.xFraction + drag.x / areaW).coerceIn(0.45f, 0.92f),
                                                            yFraction = (item.yFraction + drag.y / areaH).coerceIn(0.15f, 0.88f),
                                                        )
                                                    } else item
                                                }
                                            )
                                        }
                                    } else {
                                        detectTapGestures(
                                            onPress = {
                                                synthesizer.playTone(
                                                    when (btn.id) {
                                                        "A" -> 660.0
                                                        "B" -> 440.0
                                                        "X" -> 780.0
                                                        else -> 520.0
                                                    },
                                                    durationMs = 70,
                                                    volume = game.masterVolume,
                                                    enabled = audioEnabled,
                                                )
                                                when (btn.id) {
                                                    "A", "X" -> nitroActive = true
                                                    "B" -> speed = (speed - 18f).coerceAtLeast(10f)
                                                }
                                                tryAwaitRelease()
                                                nitroActive = false
                                            }
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = btn.label,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = btnTextColor,
                            )
                        }
                    }
                }
            }

            // 5. Bottom Control Bar: MOUSE | KEYBOARD | EDIT (matching Phone 3!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ControlModeBottomPill(
                    label = strings.mouseBtn,
                    active = controlMode == TouchControlMode.DIRECT_MOUSE || controlMode == TouchControlMode.TRACKPAD_MOUSE,
                    testTag = "play_mouse_btn",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        controlMode = when (controlMode) {
                            TouchControlMode.DIRECT_MOUSE -> TouchControlMode.TRACKPAD_MOUSE
                            TouchControlMode.TRACKPAD_MOUSE -> TouchControlMode.GAMEPAD
                            else -> TouchControlMode.DIRECT_MOUSE
                        }
                    },
                )
                ControlModeBottomPill(
                    label = strings.keyboardBtn,
                    active = controlMode == TouchControlMode.FULL_KEYBOARD,
                    testTag = "play_keyboard_btn",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        controlMode = if (controlMode == TouchControlMode.FULL_KEYBOARD) {
                            TouchControlMode.GAMEPAD
                        } else {
                            TouchControlMode.FULL_KEYBOARD
                        }
                    },
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("play_edit_btn")
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isEditMode) NeonCyan else NeonPurple)
                        .clickable {
                            if (isEditMode) {
                                onSaveControlLayout("Custom ${game.title}", controlLayout)
                            }
                            isEditMode = !isEditMode
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isEditMode) strings.doneBtn else strings.editBtn,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isEditMode) Color(0xFF091220) else Color.White,
                    )
                }
            }
        }

        // In-Game Slide-Over Side Drawer (Pause, Restart, Controls, Screenshot, Exit)
        if (isDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { isDrawerOpen = false },
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.74f)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(14.dp)
                        .clickable(enabled = false) {},
                    cornerRadius = 28.dp,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonCyan,
                        )
                        DrawerActionRow(
                            icon = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            label = if (isPaused) strings.resumeGame else strings.pauseGame,
                            onClick = {
                                isPaused = !isPaused
                                isDrawerOpen = false
                            },
                        )
                        DrawerActionRow(
                            icon = Icons.Rounded.Refresh,
                            label = strings.restartGame,
                            onClick = {
                                speed = 35f
                                roadScroll = 0f
                                score = 0
                                isPaused = false
                                isDrawerOpen = false
                            },
                        )
                        DrawerActionRow(
                            icon = Icons.Rounded.CameraAlt,
                            label = strings.screenshotBtn,
                            onClick = {
                                onSaveScreenshot(captureFramebufferBitmap())
                                isDrawerOpen = false
                            },
                        )
                        DrawerActionRow(
                            icon = Icons.Rounded.Gamepad,
                            label = strings.changeControls,
                            onClick = {
                                isDrawerOpen = false
                                showProfilesDialog = true
                            },
                        )
                        DrawerActionRow(
                            icon = Icons.Rounded.Save,
                            label = "Shader: ${if (strings.isArabic) activeShader.labelAr else activeShader.labelEn}",
                            onClick = {
                                val list = ShaderFilter.entries
                                activeShader = list[(list.indexOf(activeShader) + 1) % list.size]
                            },
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        DrawerActionRow(
                            icon = Icons.AutoMirrored.Rounded.ExitToApp,
                            label = strings.exitGame,
                            tint = AccentPink,
                            onClick = {
                                onRecordPlayTime(sessionSeconds)
                                onExit()
                            },
                        )
                    }
                }
            }
        }
    }

    // Save / Load Multi-Slot Dialog
    if (showSaveLoadDialog != null) {
        val isSave = showSaveLoadDialog == "SAVE"
        AlertDialog(
            onDismissRequest = { showSaveLoadDialog = null },
            containerColor = Color(0xFF171333),
            title = {
                Text(
                    text = if (isSave) "${strings.saveAction} State" else "${strings.loadAction} State",
                    color = Color.White,
                )
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items((1..6).toList()) { slot ->
                        val existing = saveStates.firstOrNull { it.slotIndex == slot }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable {
                                    if (isSave) {
                                        val snap = JSONObject().apply {
                                            put("speed", speed.toDouble())
                                            put("roadScroll", roadScroll.toDouble())
                                            put("score", score)
                                            put("steerX", steerX.toDouble())
                                        }.toString()
                                        onSaveState(slot, snap, captureFramebufferBitmap())
                                        showSaveLoadDialog = null
                                    } else if (existing != null) {
                                        try {
                                            val obj = JSONObject(existing.snapshotJson)
                                            speed = obj.optDouble("speed", 42.0).toFloat()
                                            roadScroll = obj.optDouble("roadScroll", 0.0).toFloat()
                                            score = obj.optInt("score", 0)
                                        } catch (_: Exception) {
                                        }
                                        showSaveLoadDialog = null
                                    }
                                }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("Slot $slot", color = Color.White, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (existing != null) "Saved (${existing.fileSizeBytes / 1024 + 1} KB)" else "Empty Slot",
                                    color = if (existing != null) NeonCyan else TextSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            Text(
                                text = if (isSave) strings.saveAction else strings.loadAction,
                                color = if (isSave || existing != null) NeonPurple else TextSecondary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSaveLoadDialog = null }) {
                    Text("Close", color = NeonCyan)
                }
            },
        )
    }

    // Cheats Dialog
    if (showCheatsDialog) {
        AlertDialog(
            onDismissRequest = { showCheatsDialog = false },
            containerColor = Color(0xFF171333),
            title = { Text(strings.cheatsAction, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Infinite Nitro / Boost", color = Color.White)
                        com.example.ui.theme.NeonSwitch(
                            checked = cheatInfiniteNitro,
                            onCheckedChange = { cheatInfiniteNitro = it },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Overclock Engine (145 KM/H)", color = Color.White)
                        com.example.ui.theme.NeonSwitch(
                            checked = cheatTurboSpeed,
                            onCheckedChange = { cheatTurboSpeed = it },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCheatsDialog = false }) {
                    Text(strings.doneBtn, color = NeonCyan)
                }
            },
        )
    }

    // Control Profiles Switcher & Creator
    if (showProfilesDialog) {
        var newProfileName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showProfilesDialog = false },
            containerColor = Color(0xFF171333),
            title = { Text(strings.changeControls, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    controlProfiles.forEach { prof ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (prof.isActive) NeonPurple.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f))
                                .clickable {
                                    onActivateProfile(prof.id)
                                    controlLayout = ControlLayoutConfig.fromJson(prof.layoutJson)
                                    showProfilesDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(prof.profileName, color = Color.White)
                            if (prof.isActive) Text("ACTIVE", color = NeonCyan, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("New Profile Name") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            onSaveControlLayout(newProfileName.trim(), controlLayout)
                        }
                        showProfilesDialog = false
                    }
                ) {
                    Text(strings.saveAction, color = NeonCyan)
                }
            },
        )
    }
}

@Composable
private fun ToolPillSegment(
    label: String,
    testTag: String,
    highlight: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .testTag(testTag)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (highlight) AccentPink else Color.White,
        )
    }
}

@Composable
private fun VerticalDividerDot() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(16.dp)
            .background(Color.White.copy(alpha = 0.25f))
    )
}

@Composable
private fun ControlModeBottomPill(
    label: String,
    active: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) NeonCyan.copy(alpha = 0.28f) else Color(0xFF1B183A))
            .border(1.dp, if (active) NeonCyan else Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (active) NeonCyan else Color.White,
        )
    }
}

@Composable
private fun DrawerActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint)
        Text(text = label, style = MaterialTheme.typography.titleMedium, color = tint)
    }
}

@Composable
private fun VirtualPcKeyboard(
    onKeyPress: (String) -> Unit,
    onKeyRelease: () -> Unit,
    lastKey: String,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf("ESC", "F1", "F2", "F3", "F4", "F5", "F8", "DEL"),
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L", "ENT"),
        listOf("SHIFT", "Z", "X", "C", "V", "B", "N", "M", "UP", "ALT"),
        listOf("CTRL", "TAB", "SPACE", "LEFT", "DOWN", "RIGHT"),
    )
    GlassCard(modifier = modifier, cornerRadius = 22.dp) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Text(
                text = "PC AT-102 KEYBOARD • LAST: $lastKey",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    row.forEach { keyCap ->
                        val weight = if (keyCap == "SPACE") 2.5f else 1f
                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .pointerInput(keyCap) {
                                    detectTapGestures(
                                        onPress = {
                                            onKeyPress(keyCap)
                                            tryAwaitRelease()
                                            onKeyRelease()
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = keyCap,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
