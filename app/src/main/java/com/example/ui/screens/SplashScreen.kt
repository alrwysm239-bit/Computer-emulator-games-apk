package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AccentPink
import com.example.ui.theme.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.RetroSpaceBackground
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val strings = LocalAppStrings.current
    var progress by remember { mutableFloatStateOf(0.1f) }
    val pulse = rememberInfiniteTransition(label = "splashPulse").animateFloat(
        initialValue = 0.85f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    LaunchedEffect(Unit) {
        for (step in 1..10) {
            delay(100L)
            progress = step / 10f
        }
        delay(120L)
        onFinished()
    }

    RetroSpaceBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            GlassCard(
                modifier = Modifier.size(132.dp),
                cornerRadius = 32.dp,
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(22.dp)) {
                    val glowAlpha = (pulse.value - 0.8f).coerceIn(0.2f, 0.9f)
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                        cornerRadius = CornerRadius(14.dp.toPx()),
                        style = Stroke(width = 4.dp.toPx()),
                    )
                    val playPath = Path().apply {
                        moveTo(size.width * 0.38f, size.height * 0.28f)
                        lineTo(size.width * 0.72f, size.height * 0.50f)
                        lineTo(size.width * 0.38f, size.height * 0.72f)
                        close()
                    }
                    drawPath(playPath, color = AccentPink.copy(alpha = glowAlpha))
                    drawLine(
                        color = NeonCyan,
                        start = Offset(size.width * 0.2f, size.height * 0.84f),
                        end = Offset(size.width * 0.8f, size.height * 0.84f),
                        strokeWidth = 3.dp.toPx(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "RETRO PC EMULATOR",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (strings.isArabic) {
                    "تهيئة Wine / Box64 • DOSBox-X • ScummVM"
                } else {
                    "Initializing Wine / Box64 • DOSBox-X • ScummVM"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = NeonCyan,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = NeonCyan,
                trackColor = Color.White.copy(alpha = 0.12f),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (strings.isArabic) {
                    "DirectX 3-9 • nGlide • cnc-ddraw • SoundFont"
                } else {
                    "DirectX 3-9 • nGlide • cnc-ddraw • SoundFont"
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
