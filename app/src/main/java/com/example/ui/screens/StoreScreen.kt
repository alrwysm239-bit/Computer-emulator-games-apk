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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GameEntity
import com.example.data.model.DownloadTaskState
import com.example.data.model.StoreCatalogItem
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.DarkTextOnCyan
import com.example.ui.theme.GlassCard
import com.example.ui.theme.LibraryTilePalette
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextSecondary

@Composable
fun StoreScreen(
    catalog: List<StoreCatalogItem>,
    installedGames: List<GameEntity>,
    downloads: Map<String, DownloadTaskState>,
    onDownloadItem: (StoreCatalogItem) -> Unit,
    onPlayInstalled: (GameEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = strings.storeTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Text(
                    text = strings.storeSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }

        items(catalog, key = { it.id }) { item ->
            val download = downloads[item.id]
            val existingGame = installedGames.firstOrNull { it.title.equals(item.title, ignoreCase = true) }
            val tileColor = LibraryTilePalette[item.tileColorIndex.mod(LibraryTilePalette.size)]

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(tileColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = item.format.badge,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = if (strings.isArabic) item.titleAr else item.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonEmerald.copy(alpha = 0.22f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = item.licenseBadge,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = NeonEmerald,
                                    )
                                }
                            }
                            Text(
                                text = if (strings.isArabic) item.subtitleAr else item.subtitleEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${if (strings.isArabic) item.recommendedEngine.labelAr else item.recommendedEngine.labelEn} • ${item.sizeMb} MB",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                            )
                        }
                    }

                    if (download != null && download.isDownloading) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                progress = { download.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = NeonCyan,
                                trackColor = Color.White.copy(alpha = 0.12f),
                            )
                            Text(
                                text = if (strings.isArabic) download.statusTextAr else download.statusTextEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            if (existingGame != null) {
                                Box(
                                    modifier = Modifier
                                        .testTag("store_play_${item.id}")
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(NeonEmerald.copy(alpha = 0.20f))
                                        .border(1.dp, NeonEmerald, RoundedCornerShape(22.dp))
                                        .clickable { onPlayInstalled(existingGame) }
                                        .padding(horizontal = 18.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                                        Text(strings.installedBtn, color = NeonEmerald, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .testTag("store_download_${item.id}")
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(NeonCyan)
                                        .clickable { onDownloadItem(item) }
                                        .padding(horizontal = 20.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(Icons.Rounded.Download, contentDescription = null, tint = DarkTextOnCyan, modifier = Modifier.size(18.dp))
                                        Text(strings.downloadBtn, color = DarkTextOnCyan, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
