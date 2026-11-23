package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AccentPink
import com.example.ui.theme.DarkTextOnCyan
import com.example.ui.theme.GlassCard
import com.example.ui.theme.HeroPurpleEnd
import com.example.ui.theme.HeroPurpleStart
import com.example.ui.theme.LibraryTilePalette
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    games: List<GameEntity>,
    onPlayGame: (GameEntity) -> Unit,
    onOpenGameConfig: (GameEntity) -> Unit,
    onNavigateToImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val heroGame = games.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = strings.navHome,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp),
                        color = Color.White,
                    )
                    Text(
                        text = "Windows 95/98/XP • DOS • ScummVM",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                    )
                }
                Box(
                    modifier = Modifier
                        .testTag("home_import_button")
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(NeonPurple.copy(alpha = 0.25f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                        .clickable(onClick = onNavigateToImport)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = strings.importGame,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.importGame,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        // Hero "Continue Playing" Purple Glass Card matching Phone 1 in mockup
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
                containerColor = Color.Transparent,
                borderBrush = Brush.linearGradient(
                    listOf(NeonPurple.copy(alpha = 0.7f), NeonCyan.copy(alpha = 0.4f))
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(HeroPurpleStart, HeroPurpleEnd)
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = strings.continuePlaying,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                            if (heroGame != null) {
                                Text(
                                    text = "${heroGame.resolutionWidth}x${heroGame.resolutionHeight} • ${heroGame.targetFps} FPS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan,
                                )
                            }
                        }

                        Text(
                            text = heroGame?.title ?: "City Racing",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .testTag("hero_play_button")
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(50))
                                    .background(NeonCyan)
                                    .clickable {
                                        if (heroGame != null) onPlayGame(heroGame) else onNavigateToImport()
                                    }
                                    .padding(horizontal = 28.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = strings.play,
                                        tint = DarkTextOnCyan,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.play,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                        ),
                                        color = DarkTextOnCyan,
                                    )
                                }
                            }

                            if (heroGame != null) {
                                IconButton(
                                    onClick = { onOpenGameConfig(heroGame) },
                                    modifier = Modifier
                                        .testTag("hero_config_button")
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.14f)),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Tune,
                                        contentDescription = strings.gameSettingsTitle,
                                        tint = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // My Library Grid (3 columns of vibrant rounded cards matching Phone 1)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.myLibrary,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Text(
                    text = "${games.size} ${strings.gamesCountLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
        }

        if (games.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = strings.emptyLibraryTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = strings.emptyLibrarySubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            val rows = games.chunked(3)
            items(rows.size) { rowIndex ->
                val rowGames = rows[rowIndex]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowGames.forEach { game ->
                        HomeLibraryTile(
                            game = game,
                            onPlay = { onPlayGame(game) },
                            onConfig = { onOpenGameConfig(game) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(3 - rowGames.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeLibraryTile(
    game: GameEntity,
    onPlay: () -> Unit,
    onConfig: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tileColor = LibraryTilePalette[game.coverColorIndex.mod(LibraryTilePalette.size)]
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.95f)
                .testTag("home_game_tile_${game.id}")
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(tileColor, tileColor.copy(alpha = 0.82f))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
                .clickable(onClick = onPlay),
        ) {
            if (game.isFavorite) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "Favorite",
                    tint = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(16.dp),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onConfig),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = "Game Settings",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(15.dp),
                )
            }

            Text(
                text = game.fileFormat,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                ),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Text(
            text = game.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
