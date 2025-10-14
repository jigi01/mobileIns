package com.example.myapplication.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM players ORDER BY fullName ASC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>
    
    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: Long): PlayerEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long
    
    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 50")
    fun getTopScores(): Flow<List<ScoreEntity>>
    
    @Query("SELECT * FROM scores WHERE playerId = :playerId ORDER BY score DESC")
    fun getPlayerScores(playerId: Long): Flow<List<ScoreEntity>>
    
    @Insert
    suspend fun insertScore(score: ScoreEntity)
    
    @Query("DELETE FROM players")
    suspend fun deleteAllPlayers()
    
    @Query("DELETE FROM scores")
    suspend fun deleteAllScores()
}
