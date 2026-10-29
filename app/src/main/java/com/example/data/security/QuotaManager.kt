package com.example.data.security

import android.content.Context
import com.example.data.plan.PlanManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuotaUsageState(
    val dailyAiQueriesUsed: Int = 0,
    val dailyAiQueriesLimit: Int = 20,
    val monthlyUploadsUsed: Int = 0,
    val monthlyUploadsLimit: Int = 5,
    val voiceNotesUsedToday: Int = 0,
    val podcastsGeneratedThisMonth: Int = 0
)

/**
 * QuotaManager (OWASP MASVS / F0.6 Hardening).
 *
 * Gere de forma centralizada e segura as cotas de consumo de IA, uploads e multimédia:
 * - Evita abuso de recursos e custos excessivos de LLM.
 * - Atualiza automaticamente limites baseados nas FeatureFlags do plano ativo.
 * - Ciclo de renovação diária para IA e mensal para uploads.
 * - Preparado para sincronização com Firestore / Backend NestJS.
 */
class QuotaManager private constructor(context: Context) {

    companion object {
        private const val TAG = "QuotaManager"
        private const val KEY_LAST_DAY = "quota_last_day_stamp"
        private const val KEY_LAST_MONTH = "quota_last_month_stamp"
        private const val KEY_AI_USED = "quota_ai_used"
        private const val KEY_UPLOADS_USED = "quota_uploads_used"
        private const val KEY_VOICE_USED = "quota_voice_used"
        private const val KEY_PODCAST_USED = "quota_podcast_used"

        @Volatile
        private var instance: QuotaManager? = null

        fun getInstance(context: Context): QuotaManager {
            return instance ?: synchronized(this) {
                instance ?: QuotaManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val securePrefs = KudulaSecurePreferences.getInstance(context)
    private val planManager = PlanManager.getInstance(context)

    private val _quotaState = MutableStateFlow(QuotaUsageState())
    val quotaState: StateFlow<QuotaUsageState> = _quotaState.asStateFlow()

    init {
        checkAndResetQuotas()
        refreshState()
    }

    private fun getCurrentDayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun getCurrentMonthKey(): String =
        SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    private fun checkAndResetQuotas() {
        val today = getCurrentDayKey()
        val thisMonth = getCurrentMonthKey()

        val savedDay = securePrefs.getEncryptedString(KEY_LAST_DAY, "")
        if (savedDay != today) {
            securePrefs.putEncryptedString(KEY_LAST_DAY, today)
            securePrefs.putEncryptedInt(KEY_AI_USED, 0)
            securePrefs.putEncryptedInt(KEY_VOICE_USED, 0)
            SecureLogger.d(TAG, "Cotas diárias de IA e Voz redefinidas para o novo dia: $today")
        }

        val savedMonth = securePrefs.getEncryptedString(KEY_LAST_MONTH, "")
        if (savedMonth != thisMonth) {
            securePrefs.putEncryptedString(KEY_LAST_MONTH, thisMonth)
            securePrefs.putEncryptedInt(KEY_UPLOADS_USED, 0)
            securePrefs.putEncryptedInt(KEY_PODCAST_USED, 0)
            SecureLogger.d(TAG, "Cotas mensais de uploads redefinidas para o novo mês: $thisMonth")
        }
    }

    fun refreshState() {
        val flags = planManager.featureFlags.value
        val aiUsed = securePrefs.getEncryptedInt(KEY_AI_USED, 0)
        val uploadsUsed = securePrefs.getEncryptedInt(KEY_UPLOADS_USED, 0)
        val voiceUsed = securePrefs.getEncryptedInt(KEY_VOICE_USED, 0)
        val podcastUsed = securePrefs.getEncryptedInt(KEY_PODCAST_USED, 0)

        _quotaState.value = QuotaUsageState(
            dailyAiQueriesUsed = aiUsed,
            dailyAiQueriesLimit = flags.dailyAiPromptQuota,
            monthlyUploadsUsed = uploadsUsed,
            monthlyUploadsLimit = flags.monthlyUploadQuota,
            voiceNotesUsedToday = voiceUsed,
            podcastsGeneratedThisMonth = podcastUsed
        )
    }

    fun canPerformAiQuery(): Boolean {
        checkAndResetQuotas()
        val current = _quotaState.value
        return current.dailyAiQueriesUsed < current.dailyAiQueriesLimit
    }

    fun consumeAiQuery(): Boolean {
        if (!canPerformAiQuery()) return false
        val newUsed = _quotaState.value.dailyAiQueriesUsed + 1
        securePrefs.putEncryptedInt(KEY_AI_USED, newUsed)
        refreshState()
        SecureLogger.d(TAG, "Cota de IA consumida: $newUsed/${_quotaState.value.dailyAiQueriesLimit}")
        return true
    }

    fun canUploadDocument(): Boolean {
        checkAndResetQuotas()
        val current = _quotaState.value
        return current.monthlyUploadsUsed < current.monthlyUploadsLimit
    }

    fun consumeUpload(): Boolean {
        if (!canUploadDocument()) return false
        val newUsed = _quotaState.value.monthlyUploadsUsed + 1
        securePrefs.putEncryptedInt(KEY_UPLOADS_USED, newUsed)
        refreshState()
        SecureLogger.d(TAG, "Cota de Upload consumida: $newUsed/${_quotaState.value.monthlyUploadsLimit}")
        return true
    }

    fun canRecordVoiceNote(durationSeconds: Int): Boolean {
        val flags = planManager.featureFlags.value
        if (flags.canUseVoiceNotesUnlimited) return durationSeconds <= 60
        // No plano Free: máx 30 segundos
        return durationSeconds <= 30
    }

    fun consumeVoiceNote(): Boolean {
        val newUsed = _quotaState.value.voiceNotesUsedToday + 1
        securePrefs.putEncryptedInt(KEY_VOICE_USED, newUsed)
        refreshState()
        return true
    }
}
