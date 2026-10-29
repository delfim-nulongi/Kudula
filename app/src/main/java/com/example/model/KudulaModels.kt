package com.example.model

enum class MascotMood {
    HAPPY,
    THINKING,
    ENCOURAGING,
    SLEEPY,
    CELEBRATING,
    CURIOUS,
    PROUD,
    CALM
}

enum class AiPhase(val label: String, val detail: String) {
    READING("A ler e indexar conteúdo...", "A extrair topologia semântica do material"),
    THINKING("A analisar com motor KCLS...", "A decompor em grafos e pré-requisitos"),
    SIMPLIFYING("A destilar analogias...", "A adaptar explicações ao teu perfil cognitivo"),
    CONNECTING_IDEAS("A conectar ao teu Grafo...", "A sincronizar com as tuas sessões anteriores"),
    PREPARING_QUIZ("A calibrar desafios...", "A gerar perguntas de validação sem estresse"),
    REVEALING("Síntese Cognitiva Pronta!", "Transformação concluída com sucesso")
}

data class AiLoadingState(
    val isProcessing: Boolean = false,
    val phase: AiPhase = AiPhase.READING,
    val progress: Float = 0f,
    val contextualMessage: String = ""
)

enum class SupportedLanguage(val code: String, val displayName: String, val flag: String) {
    PT_AO("pt_AO", "Português (Angola)", "🇦🇴"),
    EN("en", "English", "🇺🇸"),
    FR("fr", "Français", "🇫🇷")
}

enum class LibraryItemType(val label: String) {
    PDF("PDF"),
    DOCX("DOCX"),
    TXT("TXT"),
    MARKDOWN("Markdown"),
    VIDEO("Vídeo"),
    AUDIO("Áudio"),
    YOUTUBE("YouTube"),
    PODCAST("Podcast"),
    IMAGE("Imagem"),
    GITHUB("GitHub"),
    WEB_ARTICLE("Artigo Web")
}

enum class QuickChallengeState {
    IDLE,
    ANSWERED,
    CELEBRATING,
    LOCKED
}

enum class KDAPLevel(val title: String, val description: String) {
    AUTONOMOUS("Autonomia Total", "Apoio mínimo, perguntas reflexivas e auto-descoberta"),
    SCAFFOLDING("Dificuldade Inteligente", "Pistas graduais na Zona de Desenvolvimento Proximal"),
    GUIDED("Mentoria Intensiva", "Explicações passo-a-passo e analogias guiadas")
}

data class ReadingVoiceConfig(
    val voiceType: String = "Neural Socrática", // "Neural Socrática", "Narrativa Acadêmica", "Clara & Amigável", "Foco Imersivo"
    val gender: String = "Feminina", // "Feminina", "Masculina"
    val rhythm: String = "Cadenciado & Focado", // "Cadenciado & Focado", "Dinâmico", "Pausado & Reflexivo"
    val speed: Float = 1.0f // 0.75f, 1.0f, 1.25f, 1.5f
)

data class IKCDimensions(
    val compreensao: Int = 0, // 0-100
    val retencao: Int = 0,
    val aplicacao: Int = 0,
    val explicacao: Int = 0,
    val confianca: Int = 0,
    val velocidade: Int = 0,
    val evolucao: Int = 0,
    val hasData: Boolean = false
) {
    val overallAverage: Int
        get() = if (!hasData) 0 else (compreensao + retencao + aplicacao + explicacao + confianca + velocidade + evolucao) / 7
}

data class IPAScore(
    val score: Int = 0, // 0-100
    val cognitiveAvailability: String = "",
    val cognitiveLoad: String = "",
    val fatigueLevel: String = "",
    val recommendations: String = "",
    val hasData: Boolean = false
)

enum class OnboardingMode {
    INITIAL_SETUP,
    PROFILE_UPDATE
}

data class UserCognitiveProfile(
    val name: String = "",
    val location: String = "",
    val academicArea: String = "",
    val professionalArea: String = "",
    val primaryGoal: String = "",
    val studyStyle: String = "",
    val preferredExplanation: String = "",
    val careerAspiration: String = "",
    val ikc: IKCDimensions = IKCDimensions(),
    val ipa: IPAScore = IPAScore(),
    val xp: Int = 0,
    val level: Int = 1,
    val levelName: String = "Iniciante Cognitivo",
    val streakDays: Int = 0,
    val kdapLevel: KDAPLevel = KDAPLevel.SCAFFOLDING,
    val mbti: String? = null,
    val riasec: String? = null,
    val panicFreezesAvoided: Int = 0,
    val authenticMasteryRate: Int = 0,
    val tutorCustomName: String = "Mentor Kai",
    val companionName: String = "Kudu",
    val dailyGoalMinutes: Int = 20,
    val dailyRhythm: String = "30 min/dia",
    val learningStyle: String = "",
    val mainInterests: List<String> = emptyList(),
    val careerDirection: String = "",
    val riasecScores: List<String> = emptyList(),
    val multipleIntelligences: List<String> = emptyList(),
    val tutorPersonality: String = "Coach",
    val preferredFormat: String = "Áudio + Síntese",
    val isDemoModeActive: Boolean = false,
    val isFlowModeHighIpa: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val onboardingVersion: Int = 0,
    val voiceConfig: ReadingVoiceConfig = ReadingVoiceConfig()
)

data class LibraryItem(
    val id: String,
    val title: String,
    val type: LibraryItemType,
    val category: String,
    val summary: String,
    val readTime: String,
    val progress: Float,
    val ikcValidated: Int,
    val tags: List<String> = emptyList(),
    val preloadedConcepts: List<String> = emptyList(),
    val difficulty: String = "Intermediário",
    val estimatedTime: String = "20 min",
    val source: String = "Verificado Kudula",
    val confidenceBadge: String = "98% Confiabilidade",
    val thumbnailUri: String? = null,
    val duration: String? = null,
    val fileSize: String? = null,
    val waveformPoints: List<Float> = emptyList(),
    val sourceUri: String? = null,
    val originalText: String? = null,
    val extractionComplete: Boolean = false,
    val editedStructureJson: String? = null
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val suggestedPrompt: List<String> = emptyList(),
    val analogyConcept: String? = null,
    val socraticFollowUp: String? = null
)

data class YouTubeAnalysis(
    val url: String,
    val videoTitle: String,
    val channel: String,
    val duration: String,
    val summary: String,
    val timeline: List<Pair<String, String>>,
    val keyIdeas: List<String>,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val correctIndex: Int
)

data class FlashcardItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: String,
    val retentionScore: Int,
    val isMemorized: Boolean = false
)

data class CareerRole(
    val id: String,
    val title: String,
    val description: String,
    val matchScore: Int,
    val salaryGlobal: String,
    val marketTrend: String,
    val topSkillsNeeded: List<String>,
    val missingSkills: List<String>
)

data class FutureSelfMilestone(
    val timeframe: String,
    val targetRole: String,
    val projectedIKC: Int,
    val projectedXP: Int,
    val unlockedAbilities: List<String>,
    val guidanceQuote: String
)

data class ProofOfLearningBadge(
    val id: String,
    val title: String,
    val category: String,
    val verificationHash: String,
    val issueDate: String,
    val verifiedMetric: String,
    val issuer: String = "Kudula Verified Learning Graph • SHA-256"
)

data class CommunityReply(
    val id: String,
    val author: String,
    val role: String = "Estudante",
    val avatar: String = "👤",
    val timeAgo: String,
    val content: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)

data class CommunityComment(
    val id: String,
    val author: String,
    val role: String = "Estudante",
    val avatar: String = "👤",
    val timeAgo: String,
    val content: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val replies: List<CommunityReply> = emptyList()
)

data class CommunityPost(
    val id: String,
    val author: String,
    val role: String,
    val location: String,
    val content: String,
    val tag: String,
    val likesCount: Int,
    val dislikesCount: Int = 0,
    val heartsCount: Int = 0,
    val userReaction: String? = null, // "LIKE", "DISLIKE", "HEART"
    val commentsCount: Int,
    val timeAgo: String,
    val comments: List<CommunityComment> = emptyList()
)
