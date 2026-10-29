package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MascotMood
import com.example.model.UserCognitiveProfile
import com.example.ui.theme.*

enum class InsightCategory(
    val label: String,
    val icon: ImageVector,
    val color: Color
) {
    APRENDIZAGEM("Aprendizagem", Icons.Default.School, KudulaElectricBlue),
    FOCO("Foco", Icons.Default.CenterFocusStrong, KudulaCyanGlow),
    CARREIRA("Carreira", Icons.Default.TrendingUp, KudulaTealLight),
    BEM_ESTAR("Bem-estar", Icons.Default.Spa, KudulaPink)
}

data class DailyInsight(
    val category: InsightCategory,
    val quote: String,
    val contextTag: String,
    val actionableTip: String
)

/**
 * 2. Daily Insight Engine (DIE)
 * Motor local inteligente de insights contextuais diários sem chamadas externas.
 * Seleciona e alterna mensagens com base no perfil cognitivo, IPA, IKC e modo fluxo.
 */
@Composable
fun DailyInsightEngineCard(
    profile: UserCognitiveProfile,
    isFlowModeHighIpa: Boolean,
    modifier: Modifier = Modifier
) {
    // Banco local com mais de 30 insights categorizados
    val allInsights = remember(profile, isFlowModeHighIpa) {
        listOf(
            // --- Aprendizagem ---
            DailyInsight(
                category = InsightCategory.APRENDIZAGEM,
                quote = "A tua jornada começa agora: cada micro-conceito dominado constrói a base da tua autonomia intelectual.",
                contextTag = if (profile.academicArea.isNotBlank()) "Foco em ${profile.academicArea}" else "Início da Tua Jornada",
                actionableTip = "Explora o Tutor Socrático ou adiciona o teu primeiro material na Biblioteca."
            ),
            DailyInsight(
                category = InsightCategory.APRENDIZAGEM,
                quote = "Explica um conceito com as tuas próprias palavras para o fixares permanentemente.",
                contextTag = "Técnica de Feynman • KDAP",
                actionableTip = "Tenta resumir o último tema lido em 2 frases simples."
            ),
            DailyInsight(
                category = InsightCategory.APRENDIZAGEM,
                quote = "Aprender não é memorizar; é construir conexões que a tua mente consegue revisitar.",
                contextTag = if (profile.learningStyle.isNotBlank()) "Estilo: ${profile.learningStyle}" else "Aprendizagem Cognitiva",
                actionableTip = "Liga o conceito novo a algo prático do teu quotidiano."
            ),
            DailyInsight(
                category = InsightCategory.APRENDIZAGEM,
                quote = "Se uma matéria parece difícil, divide-a em três analogias do teu dia a dia.",
                contextTag = "Cognição Adaptativa",
                actionableTip = "O cérebro compreende metáforas 3x mais rápido."
            ),
            DailyInsight(
                category = InsightCategory.APRENDIZAGEM,
                quote = "A memória de longo prazo adora desafios espaçados. O que viste ontem consolida-se hoje.",
                contextTag = "Curva de Ebbinghaus",
                actionableTip = "Faz uma revisão rápida de 3 minutos no Smart Revisão."
            ),
            DailyInsight(
                category = InsightCategory.APRENDIZAGEM,
                quote = "Transforma notas em perguntas. Responder ativa o dobro dos circuitos cognitivos.",
                contextTag = "Recuperação Ativa",
                actionableTip = "Pergunta ao Kudu: «Faz-me uma pergunta sobre este tópico»."
            ),
            DailyInsight(
                category = InsightCategory.APRENDIZAGEM,
                quote = "O erro não é falha; é o sinal biológico de que o teu cérebro está a recalibrar sinapses.",
                contextTag = "Mentalidade de Crescimento",
                actionableTip = "Micro-hesitações ajudam o algoritmo KDAP a ajustar a dificuldade."
            ),
            DailyInsight(
                category = InsightCategory.APRENDIZAGEM,
                quote = "Quando ensinas mentalmente a alguém, o teu IKC consolida o dobro do conhecimento.",
                contextTag = "Metacognição",
                actionableTip = "Simula que estás a dar uma aula curta sobre o tópico de hoje."
            ),
            DailyInsight(
                category = InsightCategory.APRENDIZAGEM,
                quote = "Alterna matérias complexas com exercícios práticos para manter o córtex ativo.",
                contextTag = "Interleaving Cognitivo",
                actionableTip = "Muda de tópico após 25 minutos de imersão contínua."
            ),

            // --- Foco ---
            DailyInsight(
                category = InsightCategory.FOCO,
                quote = "Hoje vale mais 15 minutos concentrado do que uma hora distraído.",
                contextTag = if (isFlowModeHighIpa) "Fluxo Alto (IPA ${profile.ipa.score}%)" else "Modo Moderado",
                actionableTip = "Fecha abas secundárias e foca apenas num único parágrafo."
            ),
            DailyInsight(
                category = InsightCategory.FOCO,
                quote = "Protege a tua atenção como o recurso mais valioso da tua jornada.",
                contextTag = "Gestão de Atenção",
                actionableTip = "Coloca o telemóvel virado para baixo nos próximos 20 minutos."
            ),
            DailyInsight(
                category = InsightCategory.FOCO,
                quote = "Mono-tarefa com intenção é o superpoder dos profissionais mais brilhantes.",
                contextTag = "Foco Profundo",
                actionableTip = "Uma tarefa concluída vale mais que cinco iniciadas pela metade."
            ),
            DailyInsight(
                category = InsightCategory.FOCO,
                quote = "A tua mente precisa de 7 minutos para entrar em fluxo. Dá-lhe esse tempo sem pressa.",
                contextTag = "Tempo de Aquecimento",
                actionableTip = "Começa com a leitura do índice antes do conteúdo denso."
            ),
            DailyInsight(
                category = InsightCategory.FOCO,
                quote = "Um ambiente limpo gera clareza mental instantânea. Prepara o teu espaço de estudo.",
                contextTag = "Ergonomia Cognitiva",
                actionableTip = "Deixa apenas um caderno e a Kudula abertos à tua frente."
            ),
            DailyInsight(
                category = InsightCategory.FOCO,
                quote = "A distração é cara; o foco consciente rende juros compostos em cada conceito.",
                contextTag = "Economia da Atenção",
                actionableTip = "Define o teu objetivo único para o bloco atual de estudo."
            ),
            DailyInsight(
                category = InsightCategory.FOCO,
                quote = "Respira fundo 3 vezes antes de começar. O teu córtex pré-frontal agradece o oxigénio.",
                contextTag = "Regulação Fisiológica",
                actionableTip = "Inspira em 4 segundos, segura 4 e solta em 6."
            ),

            // --- Carreira ---
            DailyInsight(
                category = InsightCategory.CARREIRA,
                quote = "A competência que praticas hoje aproxima-te do teu próximo objetivo.",
                contextTag = "Área: ${profile.academicArea.ifBlank { "Engenharia & IA" }}",
                actionableTip = "Cada minuto registado alimenta o teu Proof of Learning."
            ),
            DailyInsight(
                category = InsightCategory.CARREIRA,
                quote = "Conhecimento provado criptograficamente abre portas em qualquer mercado global.",
                contextTag = "Proof of Learn SHA-256",
                actionableTip = "O teu IKC é o teu selo verificável de competência."
            ),
            DailyInsight(
                category = InsightCategory.CARREIRA,
                quote = "O mercado valoriza quem sabe aprender rápido e resolver problemas ambíguos.",
                contextTag = "Perfil: ${profile.mbti?.ifBlank { "Personalizado" } ?: "Personalizado"} • RIASEC ${profile.riasec?.ifBlank { "Geral" } ?: "Geral"}",
                actionableTip = "Aprender a aprender é a habilidade definitiva do século XXI."
            ),
            DailyInsight(
                category = InsightCategory.CARREIRA,
                quote = "A tua curiosidade guiada pelo KDAP está a forjar o perfil profissional do futuro.",
                contextTag = "Direção: ${profile.careerDirection.ifBlank { "Inovação Global" }}",
                actionableTip = "Avança mais um passo no Simulador do Teu Eu Futuro."
            ),
            DailyInsight(
                category = InsightCategory.CARREIRA,
                quote = "Construir bases sólidas hoje garante que nunca ficarás obsoleto amanhã.",
                contextTag = "Competências de Alta Demanda",
                actionableTip = "Foca nos fundamentos; as ferramentas mudam, os princípios ficam."
            ),
            DailyInsight(
                category = InsightCategory.CARREIRA,
                quote = "Pensamento crítico somado a ferramentas de IA coloca-te nos 5% de topo.",
                contextTag = "Vantagem Competitiva",
                actionableTip = "Usa o Tutor Socrático para validar a tua linha de raciocínio."
            ),

            // --- Bem-estar ---
            DailyInsight(
                category = InsightCategory.BEM_ESTAR,
                quote = "Consistência e clareza mental superam a intensidade sem foco.",
                contextTag = "Equilíbrio Cognitivo",
                actionableTip = "Dedica 10 minutos focados hoje para calibrar o teu ritmo diário."
            ),
            DailyInsight(
                category = InsightCategory.BEM_ESTAR,
                quote = "O sono é quando o teu cérebro limpa resíduos e organiza as memórias do dia.",
                contextTag = "Consolidação Sináptica",
                actionableTip = "Uma boa noite de sono vale mais do que 4 horas de estudo cansado."
            ),
            DailyInsight(
                category = InsightCategory.BEM_ESTAR,
                quote = "Pequenas pausas hidratam a tua energia e previnem a sobrecarga cognitiva.",
                contextTag = "Gestão de Energia",
                actionableTip = "Bebe um copo de água agora mesmo para reidratar os neurónios."
            ),
            DailyInsight(
                category = InsightCategory.BEM_ESTAR,
                quote = "Celebra os pequenos progressos de hoje. A consistência nasce da autocompaixão.",
                contextTag = "Streak: ${profile.streakDays} dias seguidos",
                actionableTip = "Reconhece o esforço que fizeste até aqui."
            ),
            DailyInsight(
                category = InsightCategory.BEM_ESTAR,
                quote = "Se sentires cansaço mental, fecha os olhos 3 minutos. O cérebro faz um reset rápido.",
                contextTag = "Micro-recuperação",
                actionableTip = "Desliga os estímulos visuais momentaneamente."
            ),
            DailyInsight(
                category = InsightCategory.BEM_ESTAR,
                quote = "Aprender deve ser uma maratona sustentável, não uma corrida de exaustão.",
                contextTag = "Ritmo Biológico",
                actionableTip = "A consistência diária vence sempre o estudo descontrolado."
            )
        )
    }

    // Escolhe automaticamente com base no perfil e estado
    var selectedIndex by remember(isFlowModeHighIpa, profile.ipa.score, profile.streakDays) {
        val initialCategory = when {
            profile.streakDays >= 3 -> InsightCategory.CARREIRA
            profile.ipa.score >= 80 -> InsightCategory.FOCO
            else -> InsightCategory.APRENDIZAGEM
        }
        val firstMatch = allInsights.indexOfFirst { it.category == initialCategory }
        mutableIntStateOf(if (firstMatch >= 0) firstMatch else 0)
    }

    val currentInsight = allInsights[selectedIndex % allInsights.size]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_insight_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(
                    currentInsight.category.color.copy(alpha = 0.6f),
                    BorderSubtle
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Kudu Avatar + Title + Category Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(38.dp)) {
                        MascotOwlAvatar(
                            mood = when (currentInsight.category) {
                                InsightCategory.APRENDIZAGEM -> MascotMood.CURIOUS
                                InsightCategory.FOCO -> MascotMood.THINKING
                                InsightCategory.CARREIRA -> MascotMood.PROUD
                                InsightCategory.BEM_ESTAR -> MascotMood.CALM
                            },
                            eyeGlow = 0.9f
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "INSIGHT DE HOJE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = currentInsight.contextTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = currentInsight.category.color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Category Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = currentInsight.category.color.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, currentInsight.category.color.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = currentInsight.category.icon,
                            contentDescription = currentInsight.category.label,
                            tint = currentInsight.category.color,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentInsight.category.label,
                            color = currentInsight.category.color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Insight Body
            AnimatedContent(
                targetState = currentInsight,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "insight_text_transition"
            ) { insight ->
                Column {
                    Text(
                        text = "«${insight.quote}»",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.5.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = KudulaCyanGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = insight.actionableTip,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Discrete "Outro Insight" Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        selectedIndex = (selectedIndex + 1) % allInsights.size
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = KudulaNavyContainer.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.testTag("btn_another_insight")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Outro Insight",
                            tint = KudulaCyanGlow,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Outro Insight",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
