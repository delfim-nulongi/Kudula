package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IKCDimensions
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadialIKCChart(
    ikc: IKCDimensions,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact

    val labels = listOf(
        "Compreensão" to ikc.compreensao,
        "Retenção" to ikc.retencao,
        "Aplicação" to ikc.aplicacao,
        "Explicação" to ikc.explicacao,
        "Confiança" to ikc.confianca,
        "Velocidade" to ikc.velocidade,
        "Evolução" to ikc.evolucao
    )

    val transition = rememberInfiniteTransition(label = "radial")
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ikc_radial_chart_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (isNarrow) 230.dp else 260.dp)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = (size.width / 2) * 0.78f

                // Draw concentric guide circles (25%, 50%, 75%, 100%)
                for (fraction in listOf(0.25f, 0.50f, 0.75f, 1.0f)) {
                    drawCircle(
                        color = BorderSubtle.copy(alpha = 0.35f),
                        radius = maxRadius * fraction,
                        center = center,
                        style = Stroke(width = 1.2f)
                    )
                }

                val numSides = labels.size
                val angleStep = (2 * PI / numSides)

                // Draw spoke lines
                for (i in 0 until numSides) {
                    val angle = i * angleStep - PI / 2
                    val x = center.x + (maxRadius * cos(angle)).toFloat()
                    val y = center.y + (maxRadius * sin(angle)).toFloat()
                    drawLine(
                        color = BorderSubtle.copy(alpha = 0.45f),
                        start = center,
                        end = Offset(x, y),
                        strokeWidth = 1f
                    )
                }

                // Build polygon path for user IKC values
                val polygonPath = Path()
                val points = mutableListOf<Offset>()

                for (i in 0 until numSides) {
                    val valueFraction = (labels[i].second / 100f).coerceIn(0.1f, 1f) * pulse
                    val angle = i * angleStep - PI / 2
                    val r = maxRadius * valueFraction
                    val px = center.x + (r * cos(angle)).toFloat()
                    val py = center.y + (r * sin(angle)).toFloat()
                    val pt = Offset(px, py)
                    points.add(pt)
                    if (i == 0) polygonPath.moveTo(pt.x, pt.y) else polygonPath.lineTo(pt.x, pt.y)
                }
                polygonPath.close()

                // Fill polygon with electric blue / violet gradient
                drawPath(
                    path = polygonPath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            KudulaElectricBlueLight.copy(alpha = 0.45f),
                            KudulaVioletLight.copy(alpha = 0.25f)
                        ),
                        center = center,
                        radius = maxRadius
                    ),
                    style = Fill
                )

                // Stroke polygon
                drawPath(
                    path = polygonPath,
                    brush = Brush.linearGradient(listOf(KudulaCyanGlow, KudulaVioletLight)),
                    style = Stroke(width = 3.5f)
                )

                // Draw vertex dots
                for (pt in points) {
                    drawCircle(color = KudulaNavyDark, radius = 5.5f, center = pt)
                    drawCircle(color = KudulaCyanGlow, radius = 4f, center = pt)
                }
            }

            // Central Score Badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (ikc.hasData) "${ikc.overallAverage}" else "—",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = if (ikc.hasData) "IKC GLOBAL" else "SEM DADOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = KudulaCyanGlow,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dimension Badges Grid
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            labels.chunked(2).forEach { rowPair ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPair.forEach { (name, score) ->
                        DimensionChip(name = name, score = score, hasData = ikc.hasData, modifier = Modifier.weight(1f))
                    }
                    if (rowPair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DimensionChip(name: String, score: Int, hasData: Boolean = true, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = KudulaNavyContainer.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = if (hasData) "$score%" else "—",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (!hasData) TextTertiary else if (score >= 85) KudulaSuccess else if (score >= 75) KudulaCyanGlow else KudulaWarning
            )
        }
    }
}
