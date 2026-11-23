package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY lastPlayedAt DESC, createdAt DESC")
    fun observeAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getGameById(id: Long): GameEntity?

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    fun observeGameById(id: Long): Flow<GameEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Query("UPDATE games SET isFavorite = :favorite WHERE id = :gameId")
    suspend fun setFavorite(gameId: Long, favorite: Boolean)

    @Query("UPDATE games SET playTimeSeconds = playTimeSeconds + :deltaSeconds, lastPlayedAt = :timestamp WHERE id = :gameId")
    suspend fun recordPlaySession(gameId: Long, deltaSeconds: Long, timestamp: Long)

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGameById(gameId: Long)

    @Query("SELECT COUNT(*) FROM games")
    suspend fun countGames(): Int
}

@Dao
interface SaveStateDao {
    @Query("SELECT * FROM save_states WHERE gameId = :gameId ORDER BY slotIndex ASC")
    fun observeSaveStatesForGame(gameId: Long): Flow<List<SaveStateEntity>>

    @Query("SELECT * FROM save_states ORDER BY createdAt DESC")
    suspend fun getAllSaveStates(): List<SaveStateEntity>

    @Query("SELECT * FROM save_states WHERE gameId = :gameId AND slotIndex = :slotIndex LIMIT 1")
    suspend fun getBySlot(gameId: Long, slotIndex: Int): SaveStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSaveState(saveState: SaveStateEntity): Long

    @Query("DELETE FROM save_states WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ControlProfileDao {
    @Query("SELECT * FROM control_profiles WHERE gameId = :gameId ORDER BY updatedAt DESC")
    fun observeProfilesForGame(gameId: Long): Flow<List<ControlProfileEntity>>

    @Query("SELECT * FROM control_profiles ORDER BY updatedAt DESC")
    suspend fun getAllProfiles(): List<ControlProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ControlProfileEntity): Long

    @Query("UPDATE control_profiles SET isActive = (id = :profileId) WHERE gameId = :gameId")
    suspend fun setActiveProfile(gameId: Long, profileId: Long)
}

@Dao
interface ErrorLogDao {
    @Query("SELECT * FROM error_logs ORDER BY timestamp DESC LIMIT 100")
    fun observeLogs(): Flow<List<ErrorLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ErrorLogEntity)

    @Query("DELETE FROM error_logs")
    suspend fun clearLogs()
}
