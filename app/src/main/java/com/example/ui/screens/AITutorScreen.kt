package com.example.ui.screens

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.model.ChatMessage
import com.example.model.KDAPLevel
import com.example.model.MascotMood
import com.example.ui.components.IntelligentAiLoadingCard
import com.example.ui.components.MascotOwlAvatar
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo

@Composable
fun AITutorScreen(
    messages: List<ChatMessage>,
    isThinking: Boolean,
    kdapLevel: KDAPLevel,
    onSendMessage: (String) -> Unit,
    onSetKdapLevel: (KDAPLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact
    var inputText by remember { mutableStateOf("") }
    var isVoiceActive by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .testTag("ai_tutor_screen")
    ) {
        // Top KDAP Scaffolding Selector
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = KudulaNavySurface,
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(BorderSubtle, BorderSubtle)))
        ) {
            Column(modifier = Modifier.padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(KudulaElectricBlue.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Tutor Cognitivo",
                                tint = KudulaCyanGlow,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = KudulaCyanGlow,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isNarrow) "KDAP:" else "KDAP SCAFFOLDING:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = kdapLevel.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = KudulaCyanGlow,
                        fontSize = if (isNarrow) 11.sp else 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    KDAPLevel.entries.forEach { level ->
                        val isSelected = kdapLevel == level
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) KudulaElectricBlue else KudulaNavyContainer,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSetKdapLevel(level) }
                                .testTag("kdap_chip_${level.name.lowercase()}")
                        ) {
                            Text(
                                text = when (level) {
                                    KDAPLevel.AUTONOMOUS -> "Autônomo"
                                    KDAPLevel.SCAFFOLDING -> "Scaffold"
                                    KDAPLevel.GUIDED -> "Guiado"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = if (isNarrow) 10.sp else 11.sp,
                                maxLines = 1,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }

        // Quick Suggestion Chips Bar
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val quickCapabilities = listOf(
                "💡 Explicar Conceito" to "Explica este conceito de forma intuitiva e passo-a-passo.",
                "🧩 Simplificar" to "Por favor simplifica esta explicação como se eu estivesse a começar hoje.",
                "🎯 Criar Quiz" to "Gera um quiz socrático de 2 perguntas para testar a minha retenção ativa.",
                "🌉 Analogia do Dia a Dia" to "Cria uma analogia do quotidiano para me ajudar a visualizar este tema.",
                "💻 Exemplo Prático" to "Dá-me um exemplo prático do mundo real com aplicação direta."
            )
            items(quickCapabilities) { (label, prompt) ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = KudulaNavyContainer,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.clickable { onSendMessage(prompt) }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = KudulaCyanGlow,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { message ->
                ChatBubble(
                    message = message,
                    onPromptClick = onSendMessage
                )
            }

            if (isThinking) {
                item {
                    IntelligentAiLoadingCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }

        // Voice Placeholder Bar (Shown when voice button is activated)
        AnimatedVisibility(
            visible = isVoiceActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            VoiceWaveformPlaceholderBar(
                onClose = { isVoiceActive = false },
                onUseVoiceSample = { prompt ->
                    inputText = prompt
                    isVoiceActive = false
                }
            )
        }

        // Input Field & Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = KudulaNavySurface,
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(BorderSubtle, BorderSubtle)))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isNarrow) 8.dp else 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Input Placeholder Button
                IconButton(
                    onClick = { isVoiceActive = !isVoiceActive },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isVoiceActive) KudulaViolet.copy(alpha = 0.35f) else KudulaNavyContainer
                        )
                ) {
                    Icon(
                        imageVector = if (isVoiceActive) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Entrada por Voz",
                        tint = if (isVoiceActive) KudulaVioletLight else KudulaCyanGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isNarrow) "Pergunta ao Kudu..." else "Pergunta ao Kudu... (ex: 'O que é KDAP?')",
                            color = TextTertiary,
                            fontSize = if (isNarrow) 12.sp else 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = KudulaNavyContainer,
                        unfocusedContainerColor = KudulaNavyContainer,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = KudulaCyanGlow,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_tutor_input_field")
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(if (isNarrow) 42.dp else 46.dp)
                        .clip(CircleShape)
                        .background(KudulaElectricBlue)
                        .testTag("ai_tutor_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceWaveformPlaceholderBar(
    onClose: () -> Unit,
    onUseVoiceSample: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(450, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "w1"
    )
    val waveScale2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "w2"
    )
    val waveScale3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(520, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "w3"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = KudulaNavySurface,
        border = BorderStroke(1.dp, KudulaVioletLight.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENTRADA DE VOZ (MICROTELEMETRIA)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = KudulaVioletLight,
                        fontSize = 10.sp
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar", tint = TextTertiary, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simulated Audio Waveform Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val heights = listOf(waveScale1, waveScale2, waveScale3, waveScale1, waveScale3, waveScale2, waveScale1, waveScale3)
                heights.forEach { scale ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .width(4.dp)
                            .fillMaxHeight(scale.coerceIn(0.2f, 1.0f))
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.verticalGradient(listOf(KudulaCyanGlow, KudulaVioletLight))
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fala naturalmente. O Kudu processa cadência e hesitações para calibrar o nível KDAP.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        onUseVoiceSample("Podes explicar-me como o KDAP evita o congelamento cognitivo em algoritmos de grafos?")
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Usar Amostra de Voz", style = MaterialTheme.typography.labelSmall, color = KudulaCyanGlow)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onPromptClick: (String) -> Unit
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(if (isUser) 0.88f else 0.95f)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(top = 2.dp)
                ) {
                    MascotOwlAvatar(mood = MascotMood.HAPPY, eyeGlow = 0.8f)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) KudulaElectricBlue else KudulaNavySurface
                ),
                border = if (isUser) null else CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {SelectionContainer {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                }
                    // Analogy Card
                    message.analogyConcept?.let { analogy ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = KudulaViolet.copy(alpha = 0.2f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(KudulaVioletLight, KudulaCyanGlow))
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🌉", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ANALOGIA COGNITIVA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = KudulaVioletLight
                                    )
                                    Text(
                                        text = analogy,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Socratic Follow-up
                    message.socraticFollowUp?.let { followUp ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = KudulaTeal.copy(alpha = 0.2f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(KudulaTealLight, KudulaCyanGlow))
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🤔", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "PERGUNTA SOCRÁTICA DE REFLEXÃO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = KudulaCyanGlow
                                    )
                                    Text(
                                        text = followUp,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Contextual Action Chips on Tutor Cards
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BorderSubtle.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "AÇÕES CONTEXTUAIS:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextTertiary,
                            fontSize = 9.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ContextualActionChip(
                                label = "🧩 Simplificar",
                                onClick = { onPromptClick("Por favor simplifica esta explicação anterior de forma ainda mais clara.") }
                            )
                            ContextualActionChip(
                                label = "🎯 Criar Quiz",
                                onClick = { onPromptClick("Gera 2 perguntas de validação com alternativas sobre o que acabaste de explicar.") }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ContextualActionChip(
                                label = "🌉 Analogia",
                                onClick = { onPromptClick("Cria uma analogia do quotidiano para me ajudar a memorizar esta resposta.") }
                            )
                            ContextualActionChip(
                                label = "💻 Exemplo Prático",
                                onClick = { onPromptClick("Dá-me um exemplo prático passo-a-passo no código ou na vida real.") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUser) Color.White.copy(alpha = 0.7f) else TextTertiary,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        // Suggested Prompt Chips
        if (message.suggestedPrompt.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.padding(start = 40.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                message.suggestedPrompt.forEach { prompt ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = KudulaNavyContainer,
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(KudulaElectricBlue.copy(alpha = 0.5f), BorderSubtle))),
                        modifier = Modifier.clickable { onPromptClick(prompt) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = prompt, style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContextualActionChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = KudulaNavyContainer,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = KudulaCyanGlow,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
