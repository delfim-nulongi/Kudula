package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ReflectionJournal
import com.example.model.MascotMood
import com.example.ui.components.KudulaEmptyStateCard
import com.example.ui.components.MascotOwlAvatar
import com.example.ui.theme.*
import com.example.viewmodel.LearningSessionUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Cognitive Reflection Journal
 * Tela de reflexão diária orientada pelo Kudu para metacognição e ancoragem sináptica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CognitiveReflectionScreen(
    state: LearningSessionUiState,
    onSaveReflection: (text: String, mood: String, energy: Int, confidence: Int, tags: List<String>, question: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestedQuestions = remember {
        listOf(
            "O que finalmente fez sentido hoje?",
            "Onde perdeste o foco?",
            "O que gostarias de rever amanhã?",
            "Que analogia socrática usarias para explicar o que aprendeste?"
        )
    }

    var selectedQuestion by remember { mutableStateOf(suggestedQuestions[0]) }
    var reflectionText by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("Focado") }
    var energyLevel by remember { mutableFloatStateOf(85f) }
    var confidenceLevel by remember { mutableFloatStateOf(90f) }

    val availableTags = remember {
        listOf("KCLS", "KDAP", "Arquitetura", "Microtelemetria", "Grafos Semânticos", "IA Multimodal", "Foco", "Retenção")
    }
    val selectedTags = remember { mutableStateListOf("KCLS", "Foco") }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val moods = listOf(
        "Focado" to "🎯",
        "Reflexivo" to "💡",
        "Confiante" to "🚀",
        "Cansado" to "🔋",
        "Inspirado" to "✨"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Diário Reflexivo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = KudulaVioletLight.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Metacognição",
                                    color = KudulaVioletLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Ancoragem neural e consolidação de sinapses diárias",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary,
                            fontSize = 10.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KudulaNavyDark)
            )
        },
        containerColor = KudulaNavyDark,
        modifier = modifier.testTag("cognitive_reflection_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mascot prompt banner
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(KudulaViolet.copy(alpha = 0.5f), KudulaCyanGlow.copy(alpha = 0.3f)))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(46.dp)) {
                            MascotOwlAvatar(mood = MascotMood.CURIOUS, eyeGlow = 1f)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kudu • Reflexão do Dia",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "«Aprender sem refletir é como ingerir alimentos sem os digerir. Pára 2 minutos para fixar o teu salto cognitivo.»",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Question Selector Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Pergunta Orientadora do Kudu:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    suggestedQuestions.forEach { question ->
                        val isSelected = selectedQuestion == question
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) KudulaElectricBlue.copy(alpha = 0.3f) else KudulaNavyContainer,
                            border = BorderStroke(1.dp, if (isSelected) KudulaCyanGlow else BorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedQuestion = question }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) KudulaCyanGlow else TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = question,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Journal Text Area
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "A tua resposta reflexiva:",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reflectionText,
                            onValueChange = { reflectionText = it },
                            placeholder = { Text("Escreve com as tuas palavras. Não te preocupes com formalidade...", fontSize = 12.sp, color = TextTertiary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("reflection_text_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KudulaCyanGlow,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Mood & Telemetry Sliders
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Estado Cognitivo & Humor:",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        // Mood selector pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            moods.forEach { (moodName, emoji) ->
                                val isSelected = selectedMood == moodName
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) KudulaViolet.copy(alpha = 0.35f) else KudulaNavyContainer,
                                    border = BorderStroke(1.dp, if (isSelected) KudulaVioletLight else BorderSubtle),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedMood = moodName }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = emoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = moodName,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Energy Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Nível de Energia:", color = TextSecondary, fontSize = 11.sp)
                                Text("${energyLevel.toInt()}%", color = KudulaCyanGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = energyLevel,
                                onValueChange = { energyLevel = it },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = KudulaCyanGlow,
                                    activeTrackColor = KudulaElectricBlue,
                                    inactiveTrackColor = KudulaNavyContainer
                                )
                            )
                        }

                        // Confidence Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Confiança no Conceito:", color = TextSecondary, fontSize = 11.sp)
                                Text("${confidenceLevel.toInt()}%", color = KudulaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = confidenceLevel,
                                onValueChange = { confidenceLevel = it },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = KudulaGold,
                                    activeTrackColor = KudulaGold.copy(alpha = 0.8f),
                                    inactiveTrackColor = KudulaNavyContainer
                                )
                            )
                        }
                    }
                }
            }

            // Smart Tags
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tags Automáticas de Rastreio:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(availableTags) { tag ->
                            val isSelected = selectedTags.contains(tag)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                                },
                                label = { Text("#$tag", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KudulaCyanGlow.copy(alpha = 0.2f),
                                    selectedLabelColor = KudulaCyanGlow,
                                    containerColor = KudulaNavyContainer,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) KudulaCyanGlow else BorderSubtle
                                )
                            )
                        }
                    }
                }
            }

            // Save Action Button
            item {
                Button(
                    onClick = {
                        val textToSave = if (reflectionText.isNotBlank()) reflectionText else "Consolidação de conceitos chave sob protocolo KDAP."
                        onSaveReflection(
                            textToSave,
                            selectedMood,
                            energyLevel.toInt(),
                            confidenceLevel.toInt(),
                            selectedTags.toList(),
                            selectedQuestion
                        )
                        reflectionText = ""
                        saveSuccessMessage = "Reflexão gravada no Room e ligada ao KLSE com sucesso!"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_reflection_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Reflexão no KLSE", fontWeight = FontWeight.Bold)
                }

                if (saveSuccessMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = saveSuccessMessage!!,
                        color = KudulaSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Past Reflections History Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reflexões Cognitivas Anteriores (${state.reflections.size}):",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            if (state.reflections.isEmpty()) {
                item {
                    KudulaEmptyStateCard(
                        icon = Icons.Default.Psychology,
                        badge = "Diário Metacognitivo",
                        title = "Nenhuma reflexão gravada ainda",
                        description = "O registo metacognitivo consolida as tuas conexões neurais. Responde ao estímulo socrático acima e clica em 'Guardar Reflexão no KLSE' para iniciar o teu histórico.",
                        testTag = "reflection_empty_card"
                    )
                }
            } else {
                items(state.reflections) { ref ->
                    PastReflectionCard(reflection = ref)
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun PastReflectionCard(
    reflection: ReflectionJournal,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()) }
    val formattedDate = remember(reflection.timestamp) { dateFormat.format(Date(reflection.timestamp)) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavyContainer),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = KudulaVioletLight.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${reflection.mood} • ${reflection.energy}% Energia",
                        color = KudulaVioletLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = formattedDate,
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "«${reflection.promptQuestion}»",
                color = KudulaCyanGlow,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = reflection.text,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            if (reflection.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    reflection.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = KudulaNavyDark
                        ) {
                            Text(
                                text = "#$tag",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
