package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.ui.theme.KudulaCyanGlow
import com.example.ui.theme.KudulaElectricBlue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private data class NodeDefinition(
    val nx: Float,
    val ny: Float,
    val speedX: Float,
    val speedY: Float,
    val phase: Float,
    val isGreen: Boolean,
    val radiusDp: Float
)

private data class EdgeConnection(
    val from: Int,
    val to: Int,
    val impulseSpeed: Float,
    val impulseOffset: Float
)

/**
 * Animated Neural Network with Liquid Glass in continuous motion.
 * Designed specifically for the User Profile card background, featuring
 * blue (Electric Blue, Cyan Glow) and green (Emerald, Mint Teal) tones
 * aligned with the Kudula Design System.
 */
@Composable
fun AnimatedNeuralLiquidBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neural_liquid_transition")

    // Continuous time animation driving liquid blobs and impulses
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "continuous_time"
    )

    // Pulse animation for node halos
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    // Secondary slower wave for liquid glass fluid displacement
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "liquid_wave"
    )

    // Stable neural nodes distributed across the card
    val nodes = remember {
        listOf(
            NodeDefinition(0.08f, 0.18f, 0.9f, 1.1f, 0.2f, isGreen = false, radiusDp = 3.5f),
            NodeDefinition(0.18f, 0.42f, 1.2f, 0.8f, 1.1f, isGreen = true, radiusDp = 4.0f),
            NodeDefinition(0.09f, 0.76f, 0.7f, 1.3f, 2.3f, isGreen = false, radiusDp = 3.0f),
            NodeDefinition(0.28f, 0.16f, 1.1f, 0.9f, 0.8f, isGreen = true, radiusDp = 4.5f),
            NodeDefinition(0.32f, 0.65f, 0.8f, 1.2f, 3.1f, isGreen = false, radiusDp = 3.8f),
            NodeDefinition(0.24f, 0.88f, 1.0f, 0.7f, 1.9f, isGreen = true, radiusDp = 3.2f),
            NodeDefinition(0.50f, 0.10f, 1.3f, 1.0f, 2.7f, isGreen = false, radiusDp = 4.2f),
            NodeDefinition(0.48f, 0.90f, 0.9f, 1.1f, 0.5f, isGreen = true, radiusDp = 3.8f),
            NodeDefinition(0.70f, 0.18f, 1.0f, 0.8f, 3.4f, isGreen = true, radiusDp = 4.0f),
            NodeDefinition(0.68f, 0.62f, 1.1f, 1.2f, 1.6f, isGreen = false, radiusDp = 3.6f),
            NodeDefinition(0.74f, 0.86f, 0.8f, 1.0f, 2.9f, isGreen = true, radiusDp = 3.4f),
            NodeDefinition(0.88f, 0.26f, 1.2f, 0.9f, 0.4f, isGreen = false, radiusDp = 4.0f),
            NodeDefinition(0.92f, 0.55f, 0.9f, 1.1f, 1.8f, isGreen = true, radiusDp = 3.5f),
            NodeDefinition(0.86f, 0.82f, 1.0f, 0.8f, 3.7f, isGreen = false, radiusDp = 3.2f),
            NodeDefinition(0.40f, 0.38f, 0.7f, 0.9f, 1.3f, isGreen = true, radiusDp = 3.6f),
            NodeDefinition(0.60f, 0.40f, 1.1f, 0.8f, 2.4f, isGreen = false, radiusDp = 3.8f)
        )
    }

    // Interconnecting synaptic edges with traveling impulses
    val edges = remember {
        listOf(
            EdgeConnection(0, 1, 1.2f, 0.1f),
            EdgeConnection(1, 2, 0.9f, 0.6f),
            EdgeConnection(0, 3, 1.1f, 0.3f),
            EdgeConnection(3, 4, 1.0f, 0.8f),
            EdgeConnection(1, 4, 1.3f, 0.2f),
            EdgeConnection(4, 5, 0.8f, 0.5f),
            EdgeConnection(2, 5, 1.1f, 0.9f),
            EdgeConnection(3, 6, 1.2f, 0.4f),
            EdgeConnection(3, 14, 0.9f, 0.7f),
            EdgeConnection(6, 8, 1.0f, 0.2f),
            EdgeConnection(14, 15, 1.4f, 0.0f),
            EdgeConnection(15, 8, 1.1f, 0.6f),
            EdgeConnection(8, 11, 0.9f, 0.3f),
            EdgeConnection(11, 12, 1.2f, 0.8f),
            EdgeConnection(9, 12, 1.0f, 0.4f),
            EdgeConnection(12, 13, 1.1f, 0.1f),
            EdgeConnection(10, 13, 0.8f, 0.7f),
            EdgeConnection(7, 10, 1.3f, 0.5f),
            EdgeConnection(5, 7, 1.0f, 0.2f),
            EdgeConnection(4, 7, 0.9f, 0.6f),
            EdgeConnection(15, 9, 1.2f, 0.8f),
            EdgeConnection(9, 10, 1.0f, 0.3f)
        )
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val isNarrow = width < 400f

            // 1. Base deep dark glass canvas
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D1424),
                        Color(0xFF09101C),
                        Color(0xFF060B14)
                    )
                )
            )

            // 2. Liquid Glass Blobs in continuous motion
            // Blob A: Cyan / Electric Blue liquid glass pool (oscillates smoothly)
            val blob1X = width * (0.28f + 0.18f * cos(time + 0.5f))
            val blob1Y = height * (0.35f + 0.22f * sin(time * 0.8f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        KudulaCyanGlow.copy(alpha = 0.40f),
                        KudulaElectricBlue.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(blob1X, blob1Y),
                    radius = width * 0.55f
                )
            )

            // Blob B: Emerald / Mint Green liquid glass pool (moves in complementary harmony)
            val blob2X = width * (0.74f + 0.16f * sin(waveOffset))
            val blob2Y = height * (0.65f + 0.20f * cos(waveOffset * 0.9f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.36f), // Emerald green
                        Color(0xFF059669).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(blob2X, blob2Y),
                    radius = width * 0.52f
                )
            )

            // Blob C: Teal / Deep Blue liquid glass shimmer pool
            val blob3X = width * (0.50f + 0.14f * cos(waveOffset * 1.2f + 2.0f))
            val blob3Y = height * (0.15f + 0.12f * sin(waveOffset * 1.1f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = 0.28f),
                        Color(0xFF0D9488).copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(blob3X, blob3Y),
                    radius = width * 0.40f
                )
            )

            // 3. Compute dynamic position for each neural node
            val nodePositions = nodes.mapIndexed { idx, def ->
                val floatAmpX = 8f * if (isNarrow) 0.7f else 1.0f
                val floatAmpY = 6f * if (isNarrow) 0.7f else 1.0f
                val currentX = def.nx * width + floatAmpX * sin(time * def.speedX + def.phase)
                val currentY = def.ny * height + floatAmpY * cos(time * def.speedY + def.phase)
                Offset(currentX, currentY)
            }

            // 4. Draw Synaptic Connections (Edges) with Blue-Green Gradient
            edges.forEach { edge ->
                if (edge.from < nodePositions.size && edge.to < nodePositions.size) {
                    val p1 = nodePositions[edge.from]
                    val p2 = nodePositions[edge.to]
                    val node1 = nodes[edge.from]
                    val node2 = nodes[edge.to]

                    val c1 = if (node1.isGreen) Color(0xFF10B981).copy(alpha = 0.45f) else KudulaCyanGlow.copy(alpha = 0.50f)
                    val c2 = if (node2.isGreen) Color(0xFF34D399).copy(alpha = 0.45f) else KudulaElectricBlue.copy(alpha = 0.50f)

                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(c1, c2),
                            start = p1,
                            end = p2
                        ),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.3f
                    )

                    // 5. Animated Action Potential Spark (Impulse traveling along connection)
                    val rawT = (time * edge.impulseSpeed + edge.impulseOffset) % 6.28318f
                    val progress = (sin(rawT) + 1f) / 2f // smooth 0..1 back and forth

                    val sparkX = p1.x + (p2.x - p1.x) * progress
                    val sparkY = p1.y + (p2.y - p1.y) * progress
                    val sparkColor = if (edge.from % 2 == 0) Color(0xFF67E8F9) else Color(0xFF6EE7B7)

                    // Impulse core glow
                    drawCircle(
                        color = sparkColor.copy(alpha = 0.90f),
                        radius = 2.4f,
                        center = Offset(sparkX, sparkY)
                    )
                    // Impulse soft corona
                    drawCircle(
                        color = sparkColor.copy(alpha = 0.35f),
                        radius = 5.0f,
                        center = Offset(sparkX, sparkY)
                    )
                }
            }

            // 6. Draw Neural Nodes
            nodePositions.forEachIndexed { index, pos ->
                val def = nodes[index]
                val baseColor = if (def.isGreen) Color(0xFF10B981) else KudulaCyanGlow
                val accentColor = if (def.isGreen) Color(0xFF6EE7B7) else Color(0xFF7DD3FC)

                // Breathing Halo
                val haloRadius = def.radiusDp * 3.2f * (0.85f + 0.25f * pulse)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.65f * pulse),
                            baseColor.copy(alpha = 0.15f * pulse),
                            Color.Transparent
                        ),
                        center = pos,
                        radius = haloRadius
                    )
                )

                // Node Inner Glow
                drawCircle(
                    color = baseColor.copy(alpha = 0.90f),
                    radius = def.radiusDp * 1.1f,
                    center = pos
                )

                // Core Node Center Highlight
                drawCircle(
                    color = accentColor,
                    radius = def.radiusDp * 0.55f,
                    center = pos
                )
            }

            // 7. Liquid Glass Specular Shimmer Edge along top
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        KudulaCyanGlow.copy(alpha = 0.55f),
                        Color(0xFF34D399).copy(alpha = 0.60f),
                        KudulaElectricBlue.copy(alpha = 0.40f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, 1f),
                end = Offset(width, 1f),
                strokeWidth = 1.8f
            )

            // 8. Frosted Glass Protection Layer to ensure 100% text readability
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF091122).copy(alpha = 0.38f),
                        Color(0xFF060D1A).copy(alpha = 0.52f),
                        Color(0xFF081224).copy(alpha = 0.42f)
                    )
                )
            )
        }

        // Foreground content (User Photo, Name, Email, Badges)
        content()
    }
}
