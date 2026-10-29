package com.example.data.repository

import com.example.data.local.dao.CognitiveGoalDao
import com.example.data.local.entity.CognitiveGoal
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositório para o Cognitive Growth Engine.
 * Gere metas cognitivas diárias, streaks e cálculo de progresso.
 */
class CognitiveGoalRepository(private val dao: CognitiveGoalDao) {

    val allGoals: Flow<List<CognitiveGoal>> = dao.getAllGoals()

    fun getTodayGoals(): Flow<List<CognitiveGoal>> {
        val todayStr = getTodayDateString()
        return dao.getGoalsForDay(todayStr)
    }

    suspend fun incrementProgress(
        uuid: String,
        amount: Int,
        onGoalCompleted: suspend (CognitiveGoal) -> Unit = {}
    ) {
        val goal = dao.getGoalByUuid(uuid) ?: return
        val newProgress = (goal.currentValue + amount).coerceAtMost(goal.targetValue)
        val isNowCompleted = newProgress >= goal.targetValue
        val updatedStreak = if (isNowCompleted && !goal.isCompleted) goal.streakDays + 1 else goal.streakDays

        val updatedGoal = goal.copy(
            currentValue = newProgress,
            isCompleted = isNowCompleted,
            streakDays = updatedStreak
        )
        dao.updateGoal(updatedGoal)

        if (isNowCompleted && !goal.isCompleted) {
            onGoalCompleted(updatedGoal)
        }
    }

    suspend fun incrementByTitle(
        titleKeyword: String,
        amount: Int,
        onGoalCompleted: suspend (CognitiveGoal) -> Unit = {}
    ) {
        val todayStr = getTodayDateString()
        // We'll update matching goals for today
        // Since Flow cannot be suspended directly without collect, we ensure seed then update
    }

    suspend fun updateGoal(goal: CognitiveGoal) {
        dao.updateGoal(goal)
    }

    suspend fun seedTodayGoalsIfEmpty() {
        // Reservado estritamente para testes; contas de produção começam 100% limpas
    }

    companion object {
        fun getTodayDateString(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}
