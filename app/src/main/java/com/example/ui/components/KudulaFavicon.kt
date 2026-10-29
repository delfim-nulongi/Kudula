package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

/**
 * Official Kudula Logo Composable.
 * Uses the exact immutable official Kudula PNG asset (drawable/kudula_logo_official.png).
 * Strictly adheres to official branding mandates (never redrawn, never Canvas/SVG, only scaled).
 */
@Composable
fun KudulaOfficialLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    Image(
        painter = painterResource(id = R.drawable.kudula_logo_official),
        contentDescription = "Kudula Official Logo",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

/**
 * Official Kudula Favicon Logo Composable with optional aura glow microinteraction.
 */
@Composable
fun KudulaFaviconLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    showAuraGlow: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "kudula_logo_aura")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (showAuraGlow) {
            // Ambient neon aura
            Box(
                modifier = Modifier
                    .size(size * 1.05f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                KudulaElectricBlue.copy(alpha = auraAlpha * 0.45f),
                                KudulaViolet.copy(alpha = auraAlpha * 0.30f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Image(
            painter = painterResource(id = R.drawable.kudula_logo_official),
            contentDescription = "Kudula Official Logo",
            modifier = Modifier.size(size),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Kudula Official Wordmark Composable.
 * Renders "Kudula" with capital 'K' and lowercase 'udula' in clean modern typography.
 */
@Composable
fun KudulaWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 38.sp,
    color: Color = Color.White
) {
    Text(
        text = "Kudula",
        style = MaterialTheme.typography.displayMedium.copy(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        ),
        color = color,
        modifier = modifier
    )
}

