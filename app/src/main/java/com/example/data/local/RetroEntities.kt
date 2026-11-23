package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val fileFormat: String,
    val sourceUri: String,
    val extractedDirPath: String,
    val executablePath: String,
    val availableExecutablesJson: String = "[]",
    val needsSetup: Boolean = false,
    val setupCompleted: Boolean = true,
    val engineType: String = "AUTO",
    val compatibilityMode: String = "AUTO",
    val performanceMode: String = "BALANCED",
    val targetFps: Int = 60,
    val gpuAcceleration: Boolean = true,
    val resolutionWidth: Int = 640,
    val resolutionHeight: Int = 480,
    val aspectRatio: String = "RATIO_4_3",
    val shaderFilter: String = "CRT_SCANLINES",
    val audioChip: String = "SOUNDBLASTER_16",
    val soundFontName: String = "FluidR3_GM.sf2",
    val masterVolume: Float = 0.85f,
    val dxWrapper: String = "AUTO",
    val installDirectX: Boolean = true,
    val installVcRedist: Boolean = true,
    val installDotNet: Boolean = false,
    val category: String = "ACTION",
    val isFavorite: Boolean = false,
    val playTimeSeconds: Long = 0L,
    val lastPlayedAt: Long = 0L,
    val coverColorIndex: Int = 0,
    val coverArtPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "save_states")
data class SaveStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val gameId: Long,
    val slotIndex: Int,
    val title: String,
    val snapshotJson: String,
    val screenshotPath: String? = null,
    val fileSizeBytes: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val backedUpLocally: Boolean = false,
)

@Entity(tableName = "control_profiles")
data class ControlProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val gameId: Long,
    val profileName: String,
    val layoutJson: String,
    val isActive: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "error_logs")
data class ErrorLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val tag: String,
    val severity: String,
    val messageEn: String,
    val messageAr: String,
    val timestamp: Long = System.currentTimeMillis(),
)
