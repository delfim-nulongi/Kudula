package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CognitiveGoal
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para as metas cognitivas diárias.
 */
@Dao
interface CognitiveGoalDao {

    @Query("SELECT * FROM cognitive_goals ORDER BY isCompleted ASC, targetValue DESC")
    fun getAllGoals(): Flow<List<CognitiveGoal>>

    @Query("SELECT * FROM cognitive_goals WHERE dateDay = :dateDay ORDER BY isCompleted ASC")
    fun getGoalsForDay(dateDay: String): Flow<List<CognitiveGoal>>

    @Query("SELECT * FROM cognitive_goals WHERE uuid = :uuid LIMIT 1")
    suspend fun getGoalByUuid(uuid: String): CognitiveGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: CognitiveGoal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<CognitiveGoal>)

    @Update
    suspend fun updateGoal(goal: CognitiveGoal)

    @Query("UPDATE cognitive_goals SET currentValue = :value, isCompleted = :completed WHERE uuid = :uuid")
    suspend fun updateProgress(uuid: String, value: Int, completed: Boolean)

    @Query("SELECT * FROM cognitive_goals WHERE synced = 0 ORDER BY dateDay ASC")
    fun getUnsyncedGoals(): Flow<List<CognitiveGoal>>

    @Query("UPDATE cognitive_goals SET synced = 1 WHERE uuid = :uuid")
    suspend fun markSynced(uuid: String)

    @Delete
    suspend fun deleteGoal(goal: CognitiveGoal)
}
