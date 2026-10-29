package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

enum class IntelligenceGrowthState(
    val label: String,
    val description: String,
    val glowColor: Color,
    val badgeContainer: Color
) {
    EXCELENTE(
        label = "Excelente",
        description = "Desempenho cognitivo no percentil de elite.",
        glowColor = KudulaCyanGlow,
        badgeContainer = KudulaCyanGlow.copy(alpha = 0.2f)
    ),
    EM_CRESCIMENTO(
        label = "Em crescimento",
        description = "Aceleração notável nos últimos 7 dias.",
        glowColor = Color(0xFF10B981),
        badgeContainer = Color(0xFF10B981).copy(alpha = 0.2f)
    ),
    CONSISTENTE(
        label = "Consistente",
        description = "Ritmo estável e consolidação de conceitos.",
        glowColor = KudulaElectricBlue,
        badgeContainer = KudulaElectricBlue.copy(alpha = 0.2f)
    ),
    RECUPERACAO(
        label = "Recuperação",
        description = "Consolidação e pausa ativa para recarga neural.",
        glowColor = KudulaViolet,
        badgeContainer = KudulaViolet.copy(alpha = 0.2f)
    )
}

@Composable
fun LearningIntelligenceRing(
    ikcValue: Int,
    weeklyGrowth: Int = 0,
    completedGoalsCount: Int = 0,
    studyMinutesThisWeek: Int = 0,
    revisionsCount: Int = 0,
    sessionsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    var showDetailDialog by remember { mutableStateOf(false) }

    val state = remember(ikcValue, weeklyGrowth) {
        when {
            ikcValue == 0 -> IntelligenceGrowthState.CONSISTENTE
            ikcValue >= 80 -> IntelligenceGrowthState.EXCELENTE
            weeklyGrowth >= 3 -> IntelligenceGrowthState.EM_CRESCIMENTO
            ikcValue >= 60 -> IntelligenceGrowthState.CONSISTENTE
            else -> IntelligenceGrowthState.RECUPERACAO
        }
    }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(ikcValue) {
        val target = if (ikcValue <= 0) 0f else (ikcValue.toFloat() / 100f).coerceIn(0.05f, 1f)
        animatedProgress.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ring_glow_anim")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
        border = BorderStroke(
            1.5.dp,
            Brush.linearGradient(
                listOf(
                    state.glowColor.copy(alpha = 0.65f),
                    KudulaViolet.copy(alpha = 0.40f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDetailDialog = true }
            .testTag("learning_intelligence_ring_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = state.glowColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Learning Intelligence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = state.badgeContainer,
                    border = BorderStroke(1.dp, state.glowColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = state.label,
                        color = state.glowColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Circular Ring Canvas
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .testTag("learning_intelligence_canvas_box"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // Background Track
                    drawArc(
                        color = Color(0xFF16213B),
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Subtitle Glow Under-Track
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                KudulaElectricBlue.copy(alpha = 0.35f * glowPulse),
                                KudulaViolet.copy(alpha = 0.35f * glowPulse),
                                state.glowColor.copy(alpha = 0.35f * glowPulse)
                            )
                        ),
                        startAngle = 140f,
                        sweepAngle = 260f * animatedProgress.value,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth + 6.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Main Electric Blue -> Violet Gradient Ring
                    drawArc(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                KudulaElectricBlue,
                                KudulaCyanGlow,
                                KudulaViolet
                            )
                        ),
                        startAngle = 140f,
                        sweepAngle = 260f * animatedProgress.value,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Center Content: IKC Value & Label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "IKC",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.65f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = if (ikcValue > 0) "$ikcValue" else "—",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = if (ikcValue == 0 && weeklyGrowth == 0) "A iniciar jornada" else if (weeklyGrowth >= 0) "+$weeklyGrowth esta semana" else "$weeklyGrowth esta semana",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (weeklyGrowth >= 0) state.glowColor else KudulaViolet
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle hint
            Text(
                text = "Toca para ver o relatório cognitivo semanal",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }

    // Detail Modal Dialog
    if (showDetailDialog) {
        Dialog(onDismissRequest = { showDetailDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavyDark),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(state.glowColor, KudulaViolet))),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("learning_intelligence_detail_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Learning Intelligence",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Índice de Crescimento Cognitivo (IKC)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                        IconButton(onClick = { showDetailDialog = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big IKC Status Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = KudulaNavySurface,
                        border = BorderStroke(1.dp, state.glowColor.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(state.badgeContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$ikcValue",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = state.glowColor
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Estado: ${state.label}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = state.description,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Metric Items Grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricRow(
                            icon = Icons.Default.Timer,
                            iconTint = KudulaCyanGlow,
                            title = "Tempo estudado esta semana",
                            value = "${studyMinutesThisWeek}m"
                        )
                        MetricRow(
                            icon = Icons.Default.CheckCircle,
                            iconTint = Color(0xFF10B981),
                            title = "Metas cognitivas concluídas",
                            value = "$completedGoalsCount metas"
                        )
                        MetricRow(
                            icon = Icons.Default.School,
                            iconTint = KudulaElectricBlue,
                            title = "Sessões cognitivas (KLSE)",
                            value = "$sessionsCount sessões"
                        )
                        MetricRow(
                            icon = Icons.Default.AutoAwesome,
                            iconTint = KudulaViolet,
                            title = "Revisões ativas consolidadas",
                            value = "$revisionsCount revisões"
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showDetailDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Entendido, continuar a evoluir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = KudulaNavySurface.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
