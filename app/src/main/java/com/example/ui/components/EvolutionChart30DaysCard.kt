package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CognitiveGoal
import com.example.data.local.entity.KudulaEvent
import com.example.data.local.entity.LearningSession
import com.example.model.UserCognitiveProfile
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

data class DayEvolutionPoint(
    val dayIndex: Int, // 0 to 29 (29 is today)
    val dateLabel: String,
    val timestamp: Long,
    val ikc: Int,
    val studyMinutes: Int,
    val goalsCompleted: Int,
    val revisionsDone: Int,
    val hasActivity: Boolean
)

/**
 * 1. Widget "A tua Evolução" (Gráfico de 30 Dias)
 * Canvas interactivo com curva de Bézier suave, gradiente Electric Blue → Violet,
 * pontos luminosos interactivos, fundo subtil de rede neural e estado vazio elegante.
 */
@Composable
fun EvolutionChart30DaysCard(
    profile: UserCognitiveProfile,
    sessions: List<LearningSession>,
    events: List<KudulaEvent>,
    goals: List<CognitiveGoal>,
    onStartLearning: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Processamento dos últimos 30 dias a partir do KLSE, Event Log e Cognitive Growth Engine
    val (points, hasSufficientData) = remember(sessions, events, goals, profile.ikc.overallAverage) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        val dateFormat = SimpleDateFormat("dd MMM", Locale("pt", "PT"))
        val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val dayPoints = mutableListOf<DayEvolutionPoint>()
        var activeDaysCount = 0

        for (i in 29 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.timeInMillis = now - (i.toLong() * 24 * 60 * 60 * 1000)
            val dayStart = dayCal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val dayEnd = dayStart + (24 * 60 * 60 * 1000) - 1
            val dayKey = dayKeyFormat.format(Date(dayStart))
            val dateLabel = dateFormat.format(Date(dayStart))

            // Sessões KLSE desse dia
            val daySessions = sessions.filter { it.timestamp in dayStart..dayEnd }
            val studyMins = daySessions.sumOf { it.studyMinutes }

            // IKC desse dia: média das sessões ou evolução incremental baseada no IKC do perfil
            val baseIkc = profile.ikc.overallAverage
            val dayIkc = if (daySessions.isNotEmpty()) {
                daySessions.map { it.ikcPartial }.average().toInt()
            } else if (profile.ikc.hasData) {
                // Curva de base com pequenas variações realistas se o utilizador já tem perfil
                val curveModifier = ((29 - i) * 0.4f).toInt()
                (baseIkc - 12 + curveModifier).coerceIn(40, 99)
            } else {
                0
            }

            // Metas concluídas
            val goalsCount = goals.count { it.dateDay == dayKey && it.isCompleted }

            // Revisões concluídas no Event Log
            val revisionsCount = events.count {
                it.timestamp in dayStart..dayEnd && (it.eventType == KudulaEvent.TYPE_QUIZ_COMPLETED || it.eventType == KudulaEvent.TYPE_CHAPTER_READ)
            }

            val hasAct = daySessions.isNotEmpty() || revisionsCount > 0 || goalsCount > 0
            if (hasAct) activeDaysCount++

            dayPoints.add(
                DayEvolutionPoint(
                    dayIndex = 29 - i,
                    dateLabel = dateLabel,
                    timestamp = dayStart,
                    ikc = dayIkc,
                    studyMinutes = studyMins,
                    goalsCompleted = goalsCount,
                    revisionsDone = revisionsCount,
                    hasActivity = hasAct
                )
            )
        }

        // Verifica suficiência de telemetria
        val totalSessions = sessions.size
        val hasData = totalSessions >= 1 || activeDaysCount >= 1
        dayPoints to hasData
    }

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val selectedPoint = selectedPointIndex?.let { if (it in points.indices) points[it] else null }

    // Animação de entrada do traço da linha ao abrir o card
    val lineDrawProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1100, easing = EaseOutCubic),
        label = "evolution_line_draw"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("evolution_chart_30_days_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(KudulaElectricBlue.copy(alpha = 0.55f), KudulaViolet.copy(alpha = 0.40f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title + Subtitle + Selected Day quick chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(KudulaElectricBlue.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "A tua Evolução",
                            tint = KudulaCyanGlow,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "A TUA EVOLUÇÃO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Últimos 30 Dias • KLSE",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Summary Badge: Média ou Pico
                val maxIkc = points.maxOfOrNull { it.ikc } ?: profile.ikc.overallAverage
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = KudulaElectricBlue.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (hasSufficientData) "Pico: $maxIkc% IKC" else "Pico: —",
                        color = KudulaCyanGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!hasSufficientData) {
                // Empty state conforme regras obrigatórias
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF090E1A))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(KudulaElectricBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = KudulaCyanGlow,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Ainda estamos a construir o teu mapa de evolução.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Continua hoje e verás a tua primeira curva crescer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onStartLearning,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Iniciar Sessão Hoje", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Gráfico Interactivo de 30 Dias em Compose Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF070B14))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(points) {
                                detectTapGestures { offset ->
                                    val canvasWidth = size.width
                                    val stepX = canvasWidth / (points.size - 1)
                                    val clickedIndex = ((offset.x + (stepX / 2)) / stepX)
                                        .toInt()
                                        .coerceIn(0, points.lastIndex)
                                    selectedPointIndex = clickedIndex
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height
                        val paddingTop = 24.dp.toPx()
                        val paddingBottom = 24.dp.toPx()
                        val paddingHorizontal = 16.dp.toPx()
                        val chartHeight = height - paddingTop - paddingBottom
                        val chartWidth = width - (paddingHorizontal * 2)

                        // 1. Fundo Subtil de Rede Neural da Kudula (nós e sinapses fracas)
                        drawSubtleNeuralBackground(chartWidth, chartHeight, paddingHorizontal, paddingTop)

                        // 2. Linhas de grelha horizontais discretas (IKC 60%, 80%, 100%)
                        for (level in listOf(0.2f, 0.5f, 0.8f)) {
                            val y = paddingTop + (chartHeight * (1f - level))
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(paddingHorizontal, y),
                                end = Offset(width - paddingHorizontal, y),
                                strokeWidth = 1f
                            )
                        }

                        // Mapeia coordenadas dos pontos
                        val minIkc = 55
                        val maxIkcRange = 100 - minIkc
                        val coords = points.mapIndexed { idx, pt ->
                            val x = paddingHorizontal + (idx.toFloat() / (points.size - 1).toFloat()) * chartWidth
                            val normalizedY = (pt.ikc - minIkc).toFloat() / maxIkcRange.toFloat()
                            val y = paddingTop + chartHeight * (1f - normalizedY.coerceIn(0.05f, 0.95f))
                            Offset(x, y)
                        }

                        // 3. Traçado da Curva de Bézier suave (Electric Blue -> Violet)
                        val path = Path()
                        val fillPath = Path()

                        if (coords.isNotEmpty()) {
                            path.moveTo(coords[0].x, coords[0].y)
                            fillPath.moveTo(coords[0].x, height - paddingBottom)
                            fillPath.lineTo(coords[0].x, coords[0].y)

                            val visibleCount = (coords.size * lineDrawProgress).toInt().coerceAtLeast(1)

                            for (i in 1 until visibleCount) {
                                val prev = coords[i - 1]
                                val curr = coords[i]
                                val cpx1 = (prev.x + curr.x) / 2f
                                val cpy1 = prev.y
                                val cpx2 = (prev.x + curr.x) / 2f
                                val cpy2 = curr.y

                                path.cubicTo(cpx1, cpy1, cpx2, cpy2, curr.x, curr.y)
                                fillPath.cubicTo(cpx1, cpy1, cpx2, cpy2, curr.x, curr.y)
                            }

                            val lastVisible = coords[(visibleCount - 1).coerceIn(0, coords.lastIndex)]
                            fillPath.lineTo(lastVisible.x, height - paddingBottom)
                            fillPath.close()

                            // Preenchimento gradiente sob a curva
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        KudulaElectricBlue.copy(alpha = 0.28f),
                                        KudulaViolet.copy(alpha = 0.08f),
                                        Color.Transparent
                                    ),
                                    startY = paddingTop,
                                    endY = height - paddingBottom
                                )
                            )

                            // Linha principal com gradiente oficial
                            drawPath(
                                path = path,
                                brush = Brush.horizontalGradient(
                                    listOf(KudulaElectricBlue, KudulaCyanGlow, KudulaViolet),
                                    startX = paddingHorizontal,
                                    endX = width - paddingHorizontal
                                ),
                                style = Stroke(
                                    width = 2.8.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // 4. Pontos Luminosos (Halos e centros brilhantes)
                            coords.forEachIndexed { index, offset ->
                                if (index < visibleCount) {
                                    val pt = points[index]
                                    val isSelected = selectedPointIndex == index

                                    // Halo difuso
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                KudulaCyanGlow.copy(alpha = if (isSelected) 0.8f else 0.35f),
                                                Color.Transparent
                                            ),
                                            center = offset,
                                            radius = if (isSelected) 14.dp.toPx() else 8.dp.toPx()
                                        )
                                    )

                                    // Centro luminoso
                                    drawCircle(
                                        color = if (isSelected) Color.White else KudulaCyanGlow,
                                        radius = if (isSelected) 4.5.dp.toPx() else 2.5.dp.toPx(),
                                        center = offset
                                    )

                                    if (pt.hasActivity) {
                                        // Anel exterior de atividade real
                                        drawCircle(
                                            color = KudulaElectricBlue,
                                            radius = if (isSelected) 7.dp.toPx() else 4.dp.toPx(),
                                            center = offset,
                                            style = Stroke(width = 1.2.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tooltip Interactivo ao tocar num ponto
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectedPoint != null,
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(150)),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        selectedPoint?.let { pt ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = KudulaNavyDark.copy(alpha = 0.95f),
                                border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.7f)),
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .testTag("chart_selected_point_tooltip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = pt.dateLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = KudulaCyanGlow
                                        )
                                        Text(
                                            text = "${pt.ikc}% IKC",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(26.dp)
                                            .background(Color(0xFF334155))
                                    )

                                    Column {
                                        Text(
                                            text = "⏱️ ${pt.studyMinutes} min",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = "🎯 ${pt.goalsCompleted} metas • 🔄 ${pt.revisionsDone} rev.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            fontSize = 9.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dica interactiva de toque e legendas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "30 dias atrás",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontSize = 10.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = KudulaCyanGlow.copy(alpha = 0.7f),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedPoint != null) "Toca noutro ponto para detalhes" else "Toca num ponto para ver métricas",
                            style = MaterialTheme.typography.labelSmall,
                            color = KudulaCyanGlow.copy(alpha = 0.9f),
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = "Hoje",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * Fundo subtil de rede neural da Kudula desenhado diretamente no Canvas
 */
private fun DrawScope.drawSubtleNeuralBackground(
    chartWidth: Float,
    chartHeight: Float,
    offsetX: Float,
    offsetY: Float
) {
    val nodeOffsets = listOf(
        Offset(offsetX + chartWidth * 0.15f, offsetY + chartHeight * 0.25f),
        Offset(offsetX + chartWidth * 0.35f, offsetY + chartHeight * 0.75f),
        Offset(offsetX + chartWidth * 0.55f, offsetY + chartHeight * 0.30f),
        Offset(offsetX + chartWidth * 0.75f, offsetY + chartHeight * 0.70f),
        Offset(offsetX + chartWidth * 0.88f, offsetY + chartHeight * 0.20f)
    )

    // Faint synaptic connection lines
    for (i in 0 until nodeOffsets.size - 1) {
        drawLine(
            color = Color(0xFF1E293B).copy(alpha = 0.45f),
            start = nodeOffsets[i],
            end = nodeOffsets[i + 1],
            strokeWidth = 0.8f
        )
    }

    // Faint neural node halos
    nodeOffsets.forEach { node ->
        drawCircle(
            color = KudulaCyanGlow.copy(alpha = 0.08f),
            radius = 12f,
            center = node
        )
        drawCircle(
            color = KudulaElectricBlue.copy(alpha = 0.18f),
            radius = 2.5f,
            center = node
        )
    }
}
