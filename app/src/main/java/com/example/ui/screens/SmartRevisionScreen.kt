package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlashcardItem
import com.example.ui.components.KudulaEmptyStateCard
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo

@Composable
fun SmartRevisionScreen(
    flashcards: List<FlashcardItem>,
    currentIndex: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    onNavigateToLibrary: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact

    val questionOptions = listOf(5, 10, 20, 30, 40, 50)
    var selectedQuantity by remember { mutableIntStateOf(10) }
    var localCardIndex by remember(selectedQuantity) { mutableIntStateOf(0) }

    val activeCardPool = remember(flashcards, selectedQuantity) {
        if (flashcards.isEmpty()) {
            emptyList()
        } else if (flashcards.size >= selectedQuantity) {
            flashcards.take(selectedQuantity)
        } else {
            // Replicate cards if pool is smaller than selected
            val extended = mutableListOf<FlashcardItem>()
            var i = 0
            while (extended.size < selectedQuantity) {
                val base = flashcards[i % flashcards.size]
                extended.add(base.copy(id = "${base.id}_${extended.size + 1}"))
                i++
            }
            extended
        }
    }

    val safeIndex = if (activeCardPool.isNotEmpty()) localCardIndex.coerceIn(0, activeCardPool.size - 1) else 0
    val currentCard = activeCardPool.getOrNull(safeIndex) ?: flashcards.firstOrNull()

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400),
        label = "flip"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 12.dp)
            .testTag("smart_revision_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Revisão Espaçada",
                    style = if (isNarrow) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Curva de Esquecimento Adaptativa",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = if (isNarrow) 10.sp else 11.sp
                )
            }

            if (activeCardPool.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = KudulaElectricBlue.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${safeIndex + 1} / ${activeCardPool.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = KudulaCyanGlow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        fontSize = if (isNarrow) 11.sp else 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (currentCard == null || activeCardPool.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                KudulaEmptyStateCard(
                    icon = Icons.Default.Bolt,
                    badge = "Repetição Espaçada",
                    title = "Nenhum flashcard para revisão ainda",
                    description = "Os teus cartões de memória sináptica são gerados automaticamente quando adicionas materiais na Biblioteca ou processas vídeos no YouTube Intel.",
                    actionText = "Explorar Biblioteca",
                    actionIcon = Icons.Default.AutoStories,
                    onActionClick = { onNavigateToLibrary?.invoke() },
                    testTag = "smart_revision_empty_card"
                )
            }
        } else {
            // Question Count Filter Selector (5, 10, 20, 30, 40, 50)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Perguntas:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isNarrow) 10.sp else 11.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    questionOptions.forEach { count ->
                        val isSelected = selectedQuantity == count
                        Surface(
                            onClick = {
                                selectedQuantity = count
                                localCardIndex = 0
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) KudulaElectricBlue else KudulaNavyContainer,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) KudulaCyanGlow else BorderSubtle
                            ),
                            modifier = Modifier.testTag("filter_questions_$count")
                        ) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = if (isNarrow) 10.sp else 11.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Spaced Repetition Queue Progress
            LinearProgressIndicator(
                progress = { ((safeIndex + 1).toFloat() / activeCardPool.size.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = KudulaCyanGlow,
                trackColor = KudulaNavyContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive 3D Flashcard
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { onFlip() }
                    .testTag("active_flashcard_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFlipped) KudulaNavySurface else KudulaNavyContainer
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        if (isFlipped) listOf(KudulaTealLight, KudulaElectricBlue) else listOf(KudulaElectricBlue, KudulaViolet)
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation < 90f) {
                        // FRONT: Question
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = KudulaElectricBlue.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = currentCard.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = KudulaCyanGlow,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = currentCard.question,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                lineHeight = 26.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Toca no cartão para virar e ver a resposta", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                        }
                    } else {
                        // BACK: Answer (rotated 180 so it's upright)
                        Column(
                            modifier = Modifier.graphicsLayer { rotationY = 180f },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = KudulaSuccess.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "RESPOSTA FUNDAMENTAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = KudulaSuccess,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = currentCard.answer,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                lineHeight = 24.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Retenção Atual no Grafo: ${currentCard.retentionScore}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = KudulaCyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recall Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        localCardIndex = (localCardIndex + 1) % activeCardPool.size
                        onAnswer(false)
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("revision_hard_button"),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KudulaWarning)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isNarrow) "Reforçar" else "Reforçar (+5 XP)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isNarrow) 12.sp else 13.sp,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = {
                        localCardIndex = (localCardIndex + 1) % activeCardPool.size
                        onAnswer(true)
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("revision_easy_button"),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KudulaSuccess)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isNarrow) "Lembrei" else "Lembrei (+25 XP)",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isNarrow) 12.sp else 13.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
