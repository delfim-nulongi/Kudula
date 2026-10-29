package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.YouTubeAnalysis
import com.example.ui.components.ContextualLearningFab
import com.example.ui.components.IntelligentAiLoadingCard
import com.example.ui.components.KudulaEmptyStateCard
import com.example.ui.components.SmartEmptyStateCard
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo
import com.example.viewmodel.KudulaScreen

@Composable
fun YouTubeIntelligenceScreen(
    currentUrl: String,
    analysis: YouTubeAnalysis?,
    isAnalyzing: Boolean,
    onUrlChange: (String) -> Unit,
    onAnalyze: (String) -> Unit,
    onRecordQuizAnswer: (Boolean, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Resumo & Ideias, 1: Timeline, 2: Mapa Mental, 3: Quiz
    var quizAnswered by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .testTag("youtube_intelligence_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (isNarrow) 38.dp else 44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartDisplay,
                        contentDescription = "Vídeo & Transcrição Cognitiva",
                        tint = Color.White,
                        modifier = Modifier.size(if (isNarrow) 22.dp else 26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "YouTube Cognitive Intelligence",
                        style = if (isNarrow) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "KCLS Video Extraction • Transcrição & Quizzes",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = if (isNarrow) 10.sp else 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Link Input Box
            OutlinedTextField(
                value = currentUrl,
                onValueChange = onUrlChange,
                placeholder = {
                    Text(
                        text = if (isNarrow) "Link do YouTube..." else "Cola aqui o link do YouTube...",
                        color = TextTertiary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = KudulaCyanGlow) },
                trailingIcon = {
                    if (isNarrow) {
                        FilledIconButton(
                            onClick = { onAnalyze(currentUrl) },
                            enabled = !isAnalyzing && currentUrl.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = KudulaElectricBlue),
                            modifier = Modifier.padding(end = 4.dp).size(38.dp).testTag("analyze_youtube_button")
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Processar IA", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    } else {
                        Button(
                            onClick = { onAnalyze(currentUrl) },
                            enabled = !isAnalyzing && currentUrl.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue),
                            modifier = Modifier.padding(end = 4.dp).testTag("analyze_youtube_button")
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Processar IA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = KudulaNavySurface,
                    unfocusedContainerColor = KudulaNavySurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = KudulaCyanGlow,
                    unfocusedBorderColor = BorderSubtle
                ),
                modifier = Modifier.fillMaxWidth().testTag("youtube_url_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isAnalyzing) {
                // Priority 2 — Intelligent AI Loading
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IntelligentAiLoadingCard(
                        phaseLabel = "Síntese Multimodal KCLS",
                        phaseDetail = "A decompor vídeo em timestamps, grafo conceitual e perguntas de retenção...",
                        progress = 0.7f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            } else if (analysis != null) {
                // Video Header Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartDisplay, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = analysis.videoTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = analysis.channel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(" • ", color = TextTertiary)
                                Text(
                                    text = analysis.duration,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KudulaCyanGlow,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs
                val tabTitles = listOf("Resumo & Ideias", "Linha do Tempo", "Grafo Mental", "Micro-Quiz")
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = KudulaCyanGlow,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) KudulaCyanGlow else TextSecondary,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Contents
                when (selectedTab) {
                    0 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f).testTag("youtube_summary_tab")
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = KudulaNavyContainer,
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "SÍNTESE COGNITIVA DA AULA",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = KudulaCyanGlow,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = analysis.summary,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "IDEIAS-CHAVE EXTRAÍDAS (KCLS)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }

                            items(analysis.keyIdeas) { idea ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = KudulaNavySurface,
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = idea, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f).testTag("youtube_timeline_tab")
                        ) {
                            items(analysis.timeline) { stamp ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = KudulaNavySurface,
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = KudulaElectricBlue.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = stamp.first,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = KudulaCyanGlow,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = stamp.second,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = KudulaNavyContainer,
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))),
                            modifier = Modifier.weight(1f).fillMaxWidth().testTag("youtube_mindmap_tab")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountTree, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Grafo de Relações Conceituais",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                MindMapNode(text = analysis.videoTitle, level = 0)
                                analysis.keyIdeas.forEachIndexed { idx, idea ->
                                    MindMapNode(text = "↳ Conceito #${idx + 1}: $idea", level = 1)
                                }
                            }
                        }
                    }

                    3 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f).testTag("youtube_quiz_tab")
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = KudulaNavyContainer,
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "MICRO-DESAFIO KDAP DE RETENÇÃO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = KudulaGold,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = analysis.quizQuestion,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        analysis.quizOptions.forEachIndexed { index, option ->
                                            val isSelected = quizAnswered == index
                                            val isCorrect = index == analysis.correctIndex
                                            val optionBg = when {
                                                quizAnswered == null -> KudulaNavySurface
                                                isSelected && isCorrect -> KudulaSuccess.copy(alpha = 0.25f)
                                                isSelected && !isCorrect -> KudulaWarning.copy(alpha = 0.25f)
                                                isCorrect -> KudulaSuccess.copy(alpha = 0.15f)
                                                else -> KudulaNavySurface
                                            }

                                            Surface(
                                                onClick = {
                                                    if (quizAnswered == null) {
                                                        quizAnswered = index
                                                        val correct = index == analysis.correctIndex
                                                        onRecordQuizAnswer(correct, 12000L)
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                color = optionBg,
                                                border = CardDefaults.outlinedCardBorder().copy(
                                                    brush = if (isSelected) {
                                                        Brush.horizontalGradient(if (isCorrect) listOf(KudulaSuccess, KudulaCyanGlow) else listOf(KudulaWarning, KudulaWarning))
                                                    } else {
                                                        Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))
                                                    }
                                                ),
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${('A' + index)}. ",
                                                        fontWeight = FontWeight.Bold,
                                                        color = KudulaCyanGlow
                                                    )
                                                    Text(
                                                        text = option,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = TextPrimary
                                                    )
                                                }
                                            }
                                        }

                                        if (quizAnswered != null) {
                                            val answeredCorrect = quizAnswered == analysis.correctIndex
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = if (answeredCorrect) "🎉 Exato! +65 XP creditados no teu IKC." else "💡 Quase! Revisa o trecho do vídeo aos 08:35.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (answeredCorrect) KudulaSuccess else KudulaWarning,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Empty State para novo utilizador sem dados mock
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    KudulaEmptyStateCard(
                        icon = Icons.Default.SmartDisplay,
                        badge = "Extração KCLS",
                        title = "Pronto para analisar a tua primeira aula",
                        description = "Insere acima o link de qualquer aula, palestra ou documentário do YouTube. O Kudu sintetiza os conceitos-chave, marcos temporais e quizzes de retenção.",
                        actionText = if (currentUrl.isNotBlank()) "Processar Vídeo Agora" else null,
                        actionIcon = Icons.Default.AutoAwesome,
                        onActionClick = {
                            if (currentUrl.isNotBlank()) onAnalyze(currentUrl)
                        },
                        testTag = "youtube_empty_state_card"
                    )
                }
            }
        }

        // Contextual Learning FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        ) {
            ContextualLearningFab(
                currentScreen = KudulaScreen.YOUTUBE_INTELLIGENCE,
                onClick = {
                    if (currentUrl.isNotBlank()) {
                        onAnalyze(currentUrl)
                    }
                }
            )
        }
    }
}

@Composable
fun MindMapNode(text: String, level: Int) {
    val indent = (level * 16).dp
    Row(
        modifier = Modifier.padding(start = indent, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            color = if (level == 0) KudulaCyanGlow else if (level == 1) TextPrimary else TextSecondary,
            fontWeight = if (level == 0) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}
