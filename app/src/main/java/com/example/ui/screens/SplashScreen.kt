package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MascotMood
import com.example.ui.components.KudulaFaviconLogo
import com.example.ui.components.KudulaWordmark
import com.example.ui.components.MascotOwlAvatar
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo
import kotlinx.coroutines.delay

enum class SplashPhase {
    INITIAL_LOADING_1_8S,
    WELCOME_SCREEN
}

@Composable
fun SplashScreen(
    onStartClick: () -> Unit,
    onExploreFirstClick: () -> Unit = onStartClick,
    modifier: Modifier = Modifier
) {
    var currentPhase by remember { mutableStateOf(SplashPhase.INITIAL_LOADING_1_8S) }

    AnimatedContent(
        targetState = currentPhase,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "kudula_splash_phase_transition"
    ) { phase ->
        when (phase) {
            SplashPhase.INITIAL_LOADING_1_8S -> {
                KudulaInitialLoadingScreen(
                    onLoadingComplete = {
                        currentPhase = SplashPhase.WELCOME_SCREEN
                    },
                    modifier = modifier
                )
            }
            SplashPhase.WELCOME_SCREEN -> {
                KudulaWelcomeIntroScreen(
                    onStartClick = onStartClick,
                    onExploreFirstClick = onExploreFirstClick,
                    modifier = modifier
                )
            }
        }
    }
}

/**
 * 6. Splash Screen (F0.6)
 * Duração: 1,8 segundos.
 * Animações:
 * - logo cresce suavemente (0.85 -> 1.0f);
 * - brilho azul-roxo pulsante;
 * - partículas/auras suaves.
 * Mostrar:
 * - Kudula
 * - Inteligência para Aprender, Orientação para Crescer.
 */
@Composable
fun KudulaInitialLoadingScreen(
    onLoadingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact

    var startAnimation by remember { mutableStateOf(false) }

    // Logo growth animation
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.82f,
        animationSpec = tween(durationMillis = 5000, easing = EaseOutCubic),
        label = "splash_logo_scale"
    )

    // Blue-purple glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "splash_aura")
    val auraGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_glow"
    )

    // Progress bar for 5.0 seconds
    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 5000, easing = LinearEasing),
        label = "splash_5s_progress"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(5000L)
        onLoadingComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .testTag("splash_loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Soft background ambient radial gradient (blue-purple)
        Box(
            modifier = Modifier
                .size(340.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            KudulaElectricBlue.copy(alpha = auraGlow * 0.35f),
                            KudulaViolet.copy(alpha = auraGlow * 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = windowInfo.screenHorizontalPadding)
        ) {
            // Kudula Favicon Logo growing smoothly
            Box(
                modifier = Modifier.scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                KudulaFaviconLogo(
                    size = if (isNarrow) 110.dp else 130.dp,
                    showAuraGlow = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Kudula Wordmark (Capital K, lowercase udula)
            KudulaWordmark(
                fontSize = if (isNarrow) 34.sp else 40.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle specified in prompt: "Inteligência para Aprender, Orientação para Crescer."
            Text(
                text = "Inteligência para Aprender, Orientação para Crescer.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = KudulaCyanGlow,
                textAlign = TextAlign.Center,
                fontSize = if (isNarrow) 12.sp else 13.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 1.8-Second Subtle Loading Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (isNarrow) 0.65f else 0.50f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    KudulaCyanGlow,
                                    KudulaElectricBlue,
                                    KudulaViolet,
                                    KudulaPink
                                )
                            )
                        )
                )
            }
        }
    }
}

/**
 * 7. Welcome Screen (F0.6 — Exatamente igual à imagem anexa 1)
 * Elementos:
 * - Ícone oficial Kudula (K colorido)
 * - Kudula
 * - Cápsula: "Sistema Operativo Cognitivo para Potencial Humano"
 * - Texto: "Inteligência para Aprender,\nOrientação para Crescer."
 * - Subtexto: "«Não medimos respostas decoradas.\nProvamos o que você verdadeiramente compreende.»"
 * - Botão: "Iniciar Experiência Cognitiva ➔"
 * - Contagem regressiva ativa: "Abertura automática em 25s" (avança automaticamente aos 0s)
 * - Rodapé: "🇦🇴 Luanda • 🇺🇸 Silicon Valley • 🌐 Global"
 */
@Composable
fun KudulaWelcomeIntroScreen(
    onStartClick: () -> Unit,
    onExploreFirstClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact
    val scrollState = rememberScrollState()

    // 30s Automatic Countdown Timer
    var countdownSeconds by remember { mutableIntStateOf(30) }

    LaunchedEffect(Unit) {
        while (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds -= 1
        }
        onStartClick()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .testTag("welcome_intro_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 24.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Official Kudula Logo Asset (as in Attachment 1)
            Box(
                modifier = Modifier
                    .size(if (isNarrow) 96.dp else 115.dp),
                contentAlignment = Alignment.Center
            ) {
                KudulaFaviconLogo(
                    size = if (isNarrow) 96.dp else 115.dp,
                    showAuraGlow = true
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Kudula Title
            Text(
                text = "Kudula",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = if (isNarrow) 34.sp else 40.sp,
                    fontWeight = FontWeight.Black
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Pill Capsule: "Sistema Operativo Cognitivo para Potencial Humano"
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = KudulaElectricBlue.copy(alpha = 0.15f),
                border = BorderStroke(1.2.dp, KudulaCyanGlow.copy(alpha = 0.7f)),
                modifier = Modifier.testTag("capsule_kudula_os")
            ) {
                Text(
                    text = "Sistema Operativo Cognitivo para Potencial Humano",
                    color = KudulaCyanGlow,
                    fontSize = if (isNarrow) 11.sp else 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Slogan: "Inteligência para Aprender,\nOrientação para Crescer."
            Text(
                text = "Inteligência para Aprender,\nOrientação para Crescer.",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = if (isNarrow) 19.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = if (isNarrow) 26.sp else 30.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Quote: "«Não medimos respostas decoradas.\nProvamos o que você verdadeiramente compreende.»"
            Text(
                text = "«Não medimos respostas decoradas.\nProvamos o que você verdadeiramente compreende.»",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = if (isNarrow) 12.sp else 13.5.sp,
                    lineHeight = if (isNarrow) 18.sp else 21.sp
                ),
                color = TextSecondary.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(if (isNarrow) 0.95f else 0.88f)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Primary Action Button: "Iniciar Experiência Cognitiva ➔"
            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KudulaElectricBlue
                ),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth(if (isNarrow) 0.92f else 0.85f)
                    .height(54.dp)
                    .testTag("welcome_start_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Iniciar Experiência Cognitiva",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = if (isNarrow) 15.sp else 16.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Avançar",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Abertura automática em 25s (Contador regressivo ativo)
            Text(
                text = if (countdownSeconds > 0) "Abertura automática em ${countdownSeconds}s" else "A iniciar experiência...",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer: 🇦🇴 Luanda • 🇺🇸 Silicon Valley • 🌐 Global
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "🇦🇴 Luanda  •  🇺🇸 Silicon Valley  •  🌐 Global",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
