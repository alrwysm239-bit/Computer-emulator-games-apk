package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.ThemePreset

data class RetroExtendedColors(
    val bgTop: Color,
    val bgMid: Color,
    val bgBottom: Color,
    val glassFill: Color,
    val glassBorder: Color,
    val cyanGlow: Color,
    val purpleGlow: Color,
    val pinkAccent: Color,
)

val LocalRetroColors = staticCompositionLocalOf {
    RetroExtendedColors(
        bgTop = SpaceBgTop,
        bgMid = SpaceBgMid,
        bgBottom = SpaceBgBottom,
        glassFill = GlassSurface,
        glassBorder = GlassBorderLight,
        cyanGlow = NeonCyan,
        purpleGlow = NeonPurple,
        pinkAccent = AccentPink,
    )
}

private val RetroDarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = TextPrimary,
    primaryContainer = HeroPurpleStart,
    onPrimaryContainer = TextPrimary,
    secondary = NeonCyan,
    onSecondary = DarkTextOnCyan,
    secondaryContainer = Color(0xFF0A344B),
    onSecondaryContainer = NeonCyan,
    tertiary = AccentPink,
    onTertiary = TextPrimary,
    background = SpaceBgMid,
    onBackground = TextPrimary,
    surface = GlassSurface,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderLight,
    error = Color(0xFFFF5964),
    onError = Color.White,
)

private val RetroShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun RetroPcTheme(
    themePreset: ThemePreset = ThemePreset.NEON_DARK,
    language: AppLanguage = AppLanguage.ENGLISH,
    content: @Composable () -> Unit,
) {
    val extendedColors = when (themePreset) {
        ThemePreset.NEON_DARK -> RetroExtendedColors(
            bgTop = SpaceBgTop,
            bgMid = SpaceBgMid,
            bgBottom = SpaceBgBottom,
            glassFill = GlassSurface.copy(alpha = 0.78f),
            glassBorder = GlassBorderLight,
            cyanGlow = NeonCyan,
            purpleGlow = NeonPurple,
            pinkAccent = AccentPink,
        )
        ThemePreset.CYBER_AMOLED -> RetroExtendedColors(
            bgTop = AmoledBgTop,
            bgMid = Color(0xFF04040C),
            bgBottom = AmoledBgBottom,
            glassFill = Color(0xFF0F0D1F).copy(alpha = 0.86f),
            glassBorder = NeonCyan.copy(alpha = 0.25f),
            cyanGlow = NeonCyan,
            purpleGlow = NeonPurple,
            pinkAccent = AccentPink,
        )
        ThemePreset.RETRO_SYNTH -> RetroExtendedColors(
            bgTop = SynthBgTop,
            bgMid = Color(0xFF140B29),
            bgBottom = SynthBgBottom,
            glassFill = Color(0xFF21123F).copy(alpha = 0.78f),
            glassBorder = AccentPink.copy(alpha = 0.28f),
            cyanGlow = NeonCyan,
            purpleGlow = NeonPurple,
            pinkAccent = AccentPink,
        )
    }

    val layoutDirection = if (language == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr
    val typography = createRetroTypography(isArabic = language == AppLanguage.ARABIC)

    CompositionLocalProvider(
        LocalRetroColors provides extendedColors,
        LocalLayoutDirection provides layoutDirection,
    ) {
        MaterialTheme(
            colorScheme = RetroDarkColorScheme,
            typography = typography,
            shapes = RetroShapes,
            content = content,
        )
    }
}
