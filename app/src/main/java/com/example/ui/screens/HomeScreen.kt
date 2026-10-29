package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CognitiveGoal
import com.example.data.local.entity.KudulaEvent
import com.example.data.local.entity.LearningSession
import com.example.model.LibraryItem
import com.example.model.MascotMood
import com.example.model.UserCognitiveProfile
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo
import com.example.viewmodel.KudulaScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    profile: UserCognitiveProfile,
    isMascotVisible: Boolean,
    mascotMood: MascotMood,
    mascotMessage: String,
    libraryItems: List<LibraryItem>,
    isFlowModeHighIpa: Boolean,
    onDismissMascot: () -> Unit,
    onMascotTap: () -> Unit,
    onNavigate: (KudulaScreen) -> Unit,
    onOpenItem: (LibraryItem) -> Unit,
    onOpenDemoMode: () -> Unit,
    onToggleFlowMode: () -> Unit,
    onOpenVoiceNotes: () -> Unit = {},
    sessions: List<LearningSession> = emptyList(),
    events: List<KudulaEvent> = emptyList(),
    goals: List<CognitiveGoal> = emptyList(),
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .testTag("home_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = windowInfo.screenHorizontalPadding,
                end = windowInfo.screenHorizontalPadding,
                top = 14.dp,
                bottom = 90.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (isNarrow) 12.dp else 16.dp)
        ) {
            // Dynamic Greeting & Cognitive Status
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Olá, ${profile.name.ifBlank { "Estudante" }} 👋",
                            style = if (isNarrow) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Inteligência para Aprender • Luanda & Global",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = if (isNarrow) 11.sp else 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = KudulaElectricBlue.copy(alpha = 0.2f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(KudulaCyanGlow, KudulaElectricBlue))),
                        modifier = Modifier.clickable { onNavigate(KudulaScreen.IKC_DASHBOARD) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = if (isNarrow) 8.dp else 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (profile.ikc.hasData) "${profile.ikc.overallAverage}" else "—",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = KudulaCyanGlow
                            )
                            Text(
                                text = "IKC GLOBAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // 1. TUTOR IA KUDU (Informativo no Topo)
            item {
                TutorIaKuduHeroCard(
                    mood = mascotMood,
                    message = if (mascotMessage.isNotBlank()) {
                        mascotMessage
                    } else {
                        "Olá, ${profile.name.ifBlank { "Estudante" }}! Sou o Kudu, o teu tutor cognitivo. Estou pronto para te guiar com método socrático e reforço ativo da tua aprendizagem."
                    },
                    onChatClick = { onNavigate(KudulaScreen.AI_TUTOR) }
                )
            }

            // 2. Learning Readiness Gauge (IPA)
            item {
                IPAReadinessCard(ipa = profile.ipa)
            }

            // 3. Daily Insight Engine (DIE) — Insights de Hoje
            item {
                DailyInsightEngineCard(
                    profile = profile,
                    isFlowModeHighIpa = isFlowModeHighIpa
                )
            }

            // 4. Meta do Dia / Foco de Hoje
            item {
                val completedGoals = remember(goals) { goals.count { it.isCompleted } }
                val totalGoals = goals.size
                val focusProgress = if (totalGoals > 0) (completedGoals.toFloat() / totalGoals.toFloat()).coerceIn(0f, 1f) else 0f
                val focusTitle = if (profile.academicArea.isNotBlank()) "Explorar ${profile.academicArea}" else "Iniciar a tua Jornada Cognitiva"
                val focusSubtitle = if (totalGoals > 0) {
                    "Conclui as tuas metas de hoje para acelerar a tua retenção sináptica."
                } else {
                    "Configura os teus primeiros desafios ou faz uma leitura para gerar dados reais de progresso."
                }
                val focusStatusText = if (totalGoals > 0) {
                    "🎯 ${(focusProgress * 100).toInt()}% concluído ($completedGoals de $totalGoals metas)"
                } else {
                    "🎯 0% concluído • Dá o teu primeiro passo hoje!"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("todays_focus_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(KudulaViolet, KudulaElectricBlue))
                    )
                ) {
                    Column(modifier = Modifier.padding(if (isNarrow) 14.dp else 18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = KudulaViolet.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "FOCO DE HOJE • KDAP",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = KudulaVioletLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = if (totalGoals > 0) "$completedGoals / $totalGoals Metas" else "Meta: A definir",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = focusTitle,
                            style = if (isNarrow) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = focusSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = focusStatusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = KudulaCyanGlow,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isNarrow) 10.sp else 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(focusProgress)
                                    .fillMaxHeight()
                                    .background(Brush.horizontalGradient(listOf(KudulaElectricBlue, KudulaCyanGlow)))
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    if (libraryItems.isNotEmpty()) {
                                        onOpenItem(libraryItems.first())
                                    } else {
                                        onNavigate(KudulaScreen.LIBRARY)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue),
                                modifier = Modifier.testTag("start_today_focus_button")
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (totalGoals > 0) "Continuar no Fluxo" else "Iniciar Primeiro Estudo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isNarrow) 13.sp else 14.sp
                                )
                            }

                            Text(
                                text = "+50 XP ao concluir",
                                style = MaterialTheme.typography.labelSmall,
                                color = KudulaGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 5. Slide show de cards para continuar aprendizagem
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CONTINUAR APRENDIZAGEM",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Ver Todos (${libraryItems.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = KudulaCyanGlow,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigate(KudulaScreen.LIBRARY) }
                    )
                }
            }

            // Library Horizontal Carousel or Smart Empty State
            item {
                if (libraryItems.isEmpty()) {
                    KudulaEmptyStateCard(
                        icon = Icons.Default.AutoStories,
                        title = "Nenhum material em curso",
                        description = "O teu espaço de estudo ganha vida ao carregar o teu primeiro PDF, documento ou link do YouTube.",
                        actionText = "Explorar Biblioteca",
                        actionIcon = Icons.Default.Add,
                        onActionClick = { onNavigate(KudulaScreen.LIBRARY) },
                        testTag = "home_empty_library_card"
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(libraryItems) { item ->
                            LibraryCarouselCard(
                                item = item,
                                onClick = { onOpenItem(item) }
                            )
                        }
                    }
                }
            }

            // 6. Botões de Ações Cognitivas Rápidas
            item {
                Text(
                    text = "AÇÕES COGNITIVAS RÁPIDAS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(windowInfo.itemSpacing),
                    verticalArrangement = Arrangement.spacedBy(windowInfo.itemSpacing),
                    maxItemsInEachRow = if (isNarrow) 2 else 3
                ) {
                    QuickActionCard(
                        icon = Icons.Default.AutoAwesome,
                        title = "Tutor Socrático",
                        subtitle = "Perguntas & Analogias",
                        accent = KudulaElectricBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(KudulaScreen.AI_TUTOR) }
                    )
                    QuickActionCard(
                        icon = Icons.Default.PlayCircleFilled,
                        title = "YouTube Intel",
                        subtitle = "Resumo & Mapas",
                        accent = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(KudulaScreen.YOUTUBE_INTELLIGENCE) }
                    )
                    QuickActionCard(
                        icon = Icons.Default.Bolt,
                        title = "Smart Revisão",
                        subtitle = "Repetição Ativa",
                        accent = KudulaTealLight,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(KudulaScreen.SMART_REVISION) }
                    )
                    QuickActionCard(
                        icon = Icons.Default.Mic,
                        title = "Voice Notes",
                        subtitle = "Grava até 60s",
                        accent = KudulaViolet,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenVoiceNotes
                    )
                    QuickActionCard(
                        icon = Icons.Default.Speed,
                        title = "Momento Cognitivo",
                        subtitle = "Ritmo & Prontidão",
                        accent = KudulaGold,
                        modifier = Modifier.weight(1f),
                        onClick = { /* Permanece na tela e exibe card detalhado abaixo */ }
                    )
                    QuickActionCard(
                        icon = Icons.Default.TrendingUp,
                        title = "Teu Eu Futuro",
                        subtitle = "Projeção Neurocognitiva",
                        accent = KudulaCyanGlow,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(KudulaScreen.FUTURE_SELF) }
                    )
                }
            }

            // 7. Dados, Cálculos & Estatísticas mais abaixo
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "DADOS & ESTATÍSTICAS COGNITIVAS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp
                )
            }

            // Momento Cognitivo Card (Ajustado ao conteúdo)
            item {
                CognitiveMomentumCard(
                    profile = profile,
                    sessions = sessions,
                    goals = goals,
                    isFlowModeHighIpa = isFlowModeHighIpa
                )
            }

            // Widget "A tua Evolução" — Gráfico de 30 Dias
            item {
                EvolutionChart30DaysCard(
                    profile = profile,
                    sessions = sessions,
                    events = events,
                    goals = goals,
                    onStartLearning = {
                        if (libraryItems.isNotEmpty()) onOpenItem(libraryItems.first()) else onNavigate(KudulaScreen.LIBRARY)
                    }
                )
            }

            // Widget "Learning Intelligence Ring" (Anel de Crescimento Cognitivo)
            item {
                val weeklyGrowth = remember(sessions) {
                    val recent = sessions.filter { System.currentTimeMillis() - it.timestamp <= 7 * 86400000L }
                    if (recent.isNotEmpty()) recent.size.coerceIn(1, 8) else 0
                }
                val completedGoals = remember(goals) {
                    goals.count { it.isCompleted }
                }
                val minutesThisWeek = remember(sessions) {
                    sessions.filter { System.currentTimeMillis() - it.timestamp <= 7 * 86400000L }.sumOf { it.studyMinutes }
                }
                val revisionsCount = remember(events) {
                    events.count { it.eventType == KudulaEvent.TYPE_QUIZ_COMPLETED || it.eventType == KudulaEvent.TYPE_CHAPTER_READ }
                }
                val sessionsCount = remember(sessions) {
                    sessions.size
                }

                LearningIntelligenceRing(
                    ikcValue = if (profile.ikc.hasData) profile.ikc.overallAverage else 0,
                    weeklyGrowth = weeklyGrowth,
                    completedGoalsCount = completedGoals,
                    studyMinutesThisWeek = minutesThisWeek,
                    revisionsCount = revisionsCount,
                    sessionsCount = sessionsCount
                )
            }

            // Cognitive Flow Mode Adaptive Banner
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isFlowModeHighIpa) KudulaElectricBlue.copy(alpha = 0.15f) else Color(0xFFD97706).copy(alpha = 0.18f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            if (isFlowModeHighIpa) listOf(KudulaCyanGlow.copy(alpha = 0.6f), KudulaElectricBlue) else listOf(KudulaGold, Color(0xFFF59E0B))
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (isFlowModeHighIpa) "⚡" else "🌙", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFlowModeHighIpa) "MODO FLUXO COGNITIVO ALTO (IPA 87%)" else "MODO SUPORTE & PAUSA (IPA 62%)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isFlowModeHighIpa) KudulaCyanGlow else KudulaGold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isFlowModeHighIpa) {
                                "Interface otimizada para foco profundo. Conceitos antecipados no cache: Algoritmos de Consenso, Redes Neurais & Mapas Semânticos."
                            } else {
                                "Kudu detectou saturação recente após 90min de estudo. Sugerimos uma pausa regenerativa de 5 min ou micro-revisão passiva."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = onToggleFlowMode,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isFlowModeHighIpa) KudulaCyanGlow.copy(alpha = 0.5f) else KudulaGold.copy(alpha = 0.5f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isFlowModeHighIpa) KudulaCyanGlow else KudulaGold,
                                    containerColor = KudulaNavyContainer.copy(alpha = 0.7f)
                                ),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_simular_fadiga")
                            ) {
                                Icon(
                                    imageVector = if (isFlowModeHighIpa) Icons.Default.Bedtime else Icons.Default.Bolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isFlowModeHighIpa) "SIMULAR FADIGA" else "RESTAURAR FLUXO ALTO",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // Upgrade Inteligente do Kudu (Só aparece se o usuário atingir 70%+ de uso real)
            item {
                val planManager = remember { com.example.data.plan.PlanManager.getInstance() }
                LaunchedEffect(sessions) {
                    val totalMinutes = sessions.sumOf { it.studyMinutes }
                    planManager.updateUsageBasedOnActivity(totalMinutes, sessions.size)
                }
                val currentPlan by planManager.currentPlan.collectAsStateWithLifecycle()
                val planUsagePercent by planManager.planUsagePercent.collectAsStateWithLifecycle()
                val recommendedPlan = remember(currentPlan) { planManager.getRecommendedUpgrade() }

                if (currentPlan == com.example.model.KudulaPlan.FREE && planUsagePercent >= 70) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                        border = BorderStroke(
                            1.5.dp,
                            Brush.horizontalGradient(listOf(KudulaGold, KudulaElectricBlue))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("adaptive_upgrade_card")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = KudulaGold.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, KudulaGold.copy(alpha = 0.5f)),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = "⚡", fontSize = 16.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Kudu • Upgrade Inteligente",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = KudulaGold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = KudulaElectricBlue.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "$planUsagePercent% Potencial",
                                        color = KudulaCyanGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Já utilizaste $planUsagePercent% do teu potencial no plano ${currentPlan.displayName}.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "O plano ${recommendedPlan.displayName} desbloqueia: ${recommendedPlan.threeKeyBenefits.joinToString(", ")}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { onNavigate(KudulaScreen.PLANS) },
                                colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("see_plans_upgrade_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ver Planos (${recommendedPlan.displayName})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Contextual FAB floating at bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        ) {
            ContextualLearningFab(
                currentScreen = KudulaScreen.HOME,
                onClick = { onNavigate(KudulaScreen.AI_TUTOR) }
            )
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = KudulaNavyContainer,
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))),
        modifier = modifier.testTag("quick_action_${title.lowercase().replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun LibraryCarouselCard(
    item: LibraryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(230.dp)
            .clickable { onClick() }
            .testTag("carousel_item_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavyContainer),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = KudulaElectricBlue.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = item.type.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = KudulaCyanGlow,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "${item.ikcValidated}% IKC",
                    style = MaterialTheme.typography.labelSmall,
                    color = KudulaGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.category,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            val displayProgress = item.progress.coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF1E293B))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(displayProgress)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(KudulaElectricBlue, KudulaCyanGlow)))
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (item.progress <= 0f) "Não iniciado • 0%" else "${(displayProgress * 100).toInt()}% concluído",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontSize = 9.sp
                )
                Text(
                    text = item.readTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun TutorIaKuduHeroCard(
    mood: MascotMood,
    message: String,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("kudula_mascot_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = KudulaNavyContainer.copy(alpha = 0.95f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(KudulaElectricBlue.copy(alpha = 0.6f), KudulaViolet.copy(alpha = 0.6f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MascotOwlAvatar(mood = mood, eyeGlow = 0.9f)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TUTOR IA KUDU",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = KudulaCyanGlow,
                            letterSpacing = 0.8.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KudulaElectricBlue.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, KudulaElectricBlue.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "SOCRÁTICO & ATIVO",
                                color = KudulaCyanGlow,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        lineHeight = 18.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onChatClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chat_with_kudu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Conversar com o Kudu",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
