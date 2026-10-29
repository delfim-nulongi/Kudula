package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CognitiveGoal
import com.example.data.local.entity.LearningSession
import com.example.model.UserCognitiveProfile
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

enum class MomentumType(
    val title: String,
    val message: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val secondaryColor: Color,
    val targetScore: Float
) {
    PRONTO_A_INICIAR(
        title = "Pronto a Começar",
        message = "Inicia a tua primeira sessão de estudo hoje.",
        icon = Icons.Default.Bolt,
        primaryColor = KudulaCyanGlow,
        secondaryColor = KudulaElectricBlue,
        targetScore = 0.05f
    ),
    ACELERACAO(
        title = "Aceleração",
        message = "Estás em aceleração.",
        icon = Icons.Default.Bolt,
        primaryColor = KudulaElectricBlue,
        secondaryColor = KudulaCyanGlow,
        targetScore = 0.88f
    ),
    CONSTANTE(
        title = "Constante",
        message = "Mantém este ritmo.",
        icon = Icons.Default.TrendingUp,
        primaryColor = KudulaSuccess,
        secondaryColor = Color(0xFF34D399),
        targetScore = 0.65f
    ),
    A_PERDER_RITMO(
        title = "A perder ritmo",
        message = "Pequenos passos hoje fazem diferença.",
        icon = Icons.Default.Speed,
        primaryColor = KudulaGold,
        secondaryColor = Color(0xFFF59E0B),
        targetScore = 0.40f
    ),
    PRECISAS_DESCANSAR(
        title = "Precisas descansar",
        message = "O teu cérebro também aprende quando recupera.",
        icon = Icons.Default.Bedtime,
        primaryColor = KudulaViolet,
        secondaryColor = KudulaVioletLight,
        targetScore = 0.22f
    )
}

/**
 * 3. Cognitive Momentum Card (Widget Exclusivo F0.5+)
 * Traduz KLSE, Event Log, metas, IPA e streak num estado visual intuitivo e fluido.
 * Medidor circular elegante com Compose Canvas, ponteiro animado, brilho e haptic feedback.
 */
@Composable
fun CognitiveMomentumCard(
    profile: UserCognitiveProfile,
    sessions: List<LearningSession>,
    goals: List<CognitiveGoal>,
    isFlowModeHighIpa: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Cálculo automático do Momento Cognitivo com base em KLSE, metas, streak e IPA
    val totalMinutesToday = remember(sessions) {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - (24 * 60 * 60 * 1000)
        sessions.filter { it.timestamp >= oneDayAgo }.sumOf { it.studyMinutes }
    }

    val completedGoalsCount = remember(goals) {
        goals.count { it.isCompleted }
    }

    val momentumType = remember(isFlowModeHighIpa, profile.ipa.score, profile.ipa.hasData, totalMinutesToday, completedGoalsCount, profile.streakDays, sessions) {
        when {
            sessions.isEmpty() || totalMinutesToday == 0 -> MomentumType.PRONTO_A_INICIAR
            totalMinutesToday >= 25 && profile.ipa.score >= 75 -> MomentumType.ACELERACAO
            totalMinutesToday >= 10 || completedGoalsCount >= 1 || profile.streakDays >= 2 -> MomentumType.CONSTANTE
            totalMinutesToday > 0 -> MomentumType.A_PERDER_RITMO
            else -> MomentumType.PRONTO_A_INICIAR
        }
    }

    // Micro vibração quando muda de estado
    var previousType by remember { mutableStateOf(momentumType) }
    LaunchedEffect(momentumType) {
        if (previousType != momentumType) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            previousType = momentumType
        }
    }

    // Animação suave do valor do medidor
    val animatedProgress by animateFloatAsState(
        targetValue = momentumType.targetScore,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "momentum_gauge_progress"
    )

    val animatedColor by animateColorAsState(
        targetValue = momentumType.primaryColor,
        animationSpec = tween(400),
        label = "momentum_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag("cognitive_momentum_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(animatedColor.copy(alpha = 0.55f), BorderSubtle)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(12.dp)
        ) {
            // Header: Title + Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(animatedColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = momentumType.icon,
                            contentDescription = momentumType.title,
                            tint = animatedColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "MOMENTO COGNITIVO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.6.sp,
                            fontSize = 10.sp
                        )
                        Text(
                            text = momentumType.message,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Momentum State Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = animatedColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, animatedColor.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = momentumType.title.uppercase(),
                        color = animatedColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Elegant Radial Gauge + Contextual stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Gauge drawn with Compose Canvas
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .testTag("momentum_gauge_canvas"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 6.dp.toPx()
                        val arcSize = size.width - strokeWidth
                        val startAngle = 145f
                        val sweepAngleMax = 250f
                        val currentSweep = sweepAngleMax * animatedProgress

                        // Background track
                        drawArc(
                            color = Color(0xFF1E293B),
                            startAngle = startAngle,
                            sweepAngle = sweepAngleMax,
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Subtle glowing corona behind progress arc
                        drawArc(
                            brush = Brush.radialGradient(
                                colors = listOf(animatedColor.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.width * 0.55f
                            ),
                            startAngle = startAngle,
                            sweepAngle = currentSweep,
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth * 1.8f, cap = StrokeCap.Round)
                        )

                        // Progress Arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    momentumType.secondaryColor,
                                    momentumType.primaryColor,
                                    momentumType.secondaryColor
                                )
                            ),
                            startAngle = startAngle,
                            sweepAngle = currentSweep,
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Glowing Pointer Knob at the end of progress arc
                        val currentRad = Math.toRadians((startAngle + currentSweep).toDouble())
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val radius = arcSize / 2f
                        val knobX = centerX + radius * cos(currentRad).toFloat()
                        val knobY = centerY + radius * sin(currentRad).toFloat()

                        drawCircle(
                            color = Color.White,
                            radius = strokeWidth * 0.7f,
                            center = Offset(knobX, knobY)
                        )
                        drawCircle(
                            color = animatedColor,
                            radius = strokeWidth * 0.4f,
                            center = Offset(knobX, knobY)
                        )
                    }

                    // Value Display inside the gauge
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (momentumType == MomentumType.PRONTO_A_INICIAR) "0%" else "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "ÍNDICE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 7.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Stats Summary
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                ) {
                    MomentumStatRow(
                        label = "Estudo Hoje",
                        value = "${totalMinutesToday} min",
                        accent = KudulaCyanGlow
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    val goalsText = if (goals.isEmpty()) {
                        "0 de 0"
                    } else {
                        "$completedGoalsCount de ${goals.size}"
                    }
                    MomentumStatRow(
                        label = "Metas Concluídas",
                        value = goalsText,
                        accent = KudulaSuccess
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    MomentumStatRow(
                        label = "Streak de Foco",
                        value = "${profile.streakDays} dias",
                        accent = KudulaGold
                    )
                }
            }
        }
    }
}

@Composable
private fun MomentumStatRow(
    label: String,
    value: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF090E1A))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp
        )
    }
}
