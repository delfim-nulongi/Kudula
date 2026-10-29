package com.example.data.plan

/**
 * Feature Flags para o ecossistema Kudula (F0.6).
 *
 * Desacopla as regras de negócio das telas Compose.
 * No futuro, estas flags serão atualizadas em tempo real via Firebase Remote Config / Firestore,
 * eliminando verificações diretas de 'if (plan == "Pro")' nos Composables.
 */
data class FeatureFlags(
    val canUseVoiceNotesUnlimited: Boolean = false,
    val canUseFutureSelf: Boolean = false,
    val canUseCareerTwin: Boolean = false,
    val canUseProofOfLearning: Boolean = false,
    val canUseCorporateFeatures: Boolean = false,
    val canExportPdfReports: Boolean = false,
    val dailyAiPromptQuota: Int = 20,
    val monthlyUploadQuota: Int = 5
) {
    companion object {
        fun forPlan(plan: com.example.model.KudulaPlan): FeatureFlags = when (plan) {
            com.example.model.KudulaPlan.FREE -> FeatureFlags(
                canUseVoiceNotesUnlimited = false,
                canUseFutureSelf = false,
                canUseCareerTwin = false,
                canUseProofOfLearning = false,
                canUseCorporateFeatures = false,
                canExportPdfReports = false,
                dailyAiPromptQuota = 20,
                monthlyUploadQuota = 5
            )
            com.example.model.KudulaPlan.STUDENT -> FeatureFlags(
                canUseVoiceNotesUnlimited = true,
                canUseFutureSelf = false,
                canUseCareerTwin = false,
                canUseProofOfLearning = false,
                canUseCorporateFeatures = false,
                canExportPdfReports = false,
                dailyAiPromptQuota = 200,
                monthlyUploadQuota = 30
            )
            com.example.model.KudulaPlan.PLUS -> FeatureFlags(
                canUseVoiceNotesUnlimited = true,
                canUseFutureSelf = true,
                canUseCareerTwin = false,
                canUseProofOfLearning = false,
                canUseCorporateFeatures = false,
                canExportPdfReports = true,
                dailyAiPromptQuota = 500,
                monthlyUploadQuota = 100
            )
            com.example.model.KudulaPlan.PRO -> FeatureFlags(
                canUseVoiceNotesUnlimited = true,
                canUseFutureSelf = true,
                canUseCareerTwin = true,
                canUseProofOfLearning = true,
                canUseCorporateFeatures = false,
                canExportPdfReports = true,
                dailyAiPromptQuota = Int.MAX_VALUE,
                monthlyUploadQuota = Int.MAX_VALUE
            )
            com.example.model.KudulaPlan.BUSINESS -> FeatureFlags(
                canUseVoiceNotesUnlimited = true,
                canUseFutureSelf = true,
                canUseCareerTwin = true,
                canUseProofOfLearning = true,
                canUseCorporateFeatures = true,
                canExportPdfReports = true,
                dailyAiPromptQuota = Int.MAX_VALUE,
                monthlyUploadQuota = Int.MAX_VALUE
            )
            com.example.model.KudulaPlan.INSTITUTION -> FeatureFlags(
                canUseVoiceNotesUnlimited = true,
                canUseFutureSelf = true,
                canUseCareerTwin = true,
                canUseProofOfLearning = true,
                canUseCorporateFeatures = true,
                canExportPdfReports = true,
                dailyAiPromptQuota = Int.MAX_VALUE,
                monthlyUploadQuota = Int.MAX_VALUE
            )
        }
    }
}
