package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Deep space gradient background with Neon Purple (#8A5CFF) top-left aura & Cyan (#00D6FF) bottom-right glow.
 */
@Composable
fun RetroSpaceBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val retro = LocalRetroColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(retro.bgTop, retro.bgMid, retro.bgBottom)
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(retro.purpleGlow.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.08f),
                        radius = size.maxDimension * 0.55f,
                    ),
                    radius = size.maxDimension * 0.55f,
                    center = Offset(size.width * 0.12f, size.height * 0.08f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(retro.cyanGlow.copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(size.width * 0.90f, size.height * 0.92f),
                        radius = size.maxDimension * 0.50f,
                    ),
                    radius = size.maxDimension * 0.50f,
                    center = Offset(size.width * 0.90f, size.height * 0.92f),
                )
            },
        content = content,
    )
}

/**
 * Glassmorphism card with exact 28.dp rounded corners as specified in the design guide.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    borderBrush: Brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.22f),
            NeonPurple.copy(alpha = 0.25f),
            NeonCyan.copy(alpha = 0.18f),
        )
    ),
    containerColor: Color = LocalRetroColors.current.glassFill,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(width = 1.dp, brush = borderBrush, shape = shape),
        content = content,
    )
}

@Composable
fun NeonGradientPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    gradientColors: List<Color> = listOf(NeonPurple, Color(0xFF7048F2)),
    textColor: Color = Color.White,
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
            .minimumInteractiveComponentSize()
            .height(52.dp)
            .clip(shape)
            .background(Brush.horizontalGradient(gradientColors))
            .border(1.dp, Color.White.copy(alpha = 0.22f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun NeonSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = "",
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = NeonPurple,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFF332F54),
            uncheckedBorderColor = Color.White.copy(alpha = 0.15f),
        ),
    )
}

@Composable
fun ColoredDot(color: Color, size: Dp = 14.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

data class BottomNavTabItem(
    val route: String,
    val label: String,
    val testTag: String,
)

/**
 * Floating rounded-28dp glass bottom navigation bar matching Phone 1 in the reference design.
 */
@Composable
fun FloatingGlassBottomBar(
    items: List<BottomNavTabItem>,
    currentRoute: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1A1638).copy(alpha = 0.92f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    NavBarPillTab(
                        label = item.label,
                        selected = selected,
                        testTag = item.testTag,
                        onClick = { onSelect(item.route) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavBarPillTab(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .weight(1f)
            .testTag(testTag)
            .minimumInteractiveComponentSize()
            .clip(shape)
            .background(
                if (selected) NeonPurple.copy(alpha = 0.20f) else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) NeonPurple else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
