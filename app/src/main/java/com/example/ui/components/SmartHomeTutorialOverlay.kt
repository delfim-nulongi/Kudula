package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MascotMood
import com.example.ui.theme.*

data class TutorialStep(
    val stepIndex: Int,
    val title: String,
    val targetArea: String,
    val message: String,
    val icon: ImageVector,
    val accentColor: Color,
    val mood: MascotMood
)

/**
 * Tutorial Inteligente (Overlay sobre a Home)
 * Exibido sobre a tela Início com efeito blur de 20dp e escurecimento leve.
 * Destaca visualmente cada módulo essencial através de spotlight e cartões dinâmicos.
 */
@Composable
fun SmartHomeTutorialOverlay(
    onDismissTutorial: () -> Unit,
    onFinishTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = remember {
        listOf(
            TutorialStep(
                stepIndex = 1,
                title = "Passo 1 — A tua base",
                targetArea = "Kudu & IPA (Índice de Prontidão)",
                message = "Este é o teu ponto de partida. O Kudu acompanha-te e o IPA mostra quando a tua mente está mais pronta para aprender.",
                icon = Icons.Default.Psychology,
                accentColor = KudulaCyanGlow,
                mood = MascotMood.HAPPY
            ),
            TutorialStep(
                stepIndex = 2,
                title = "Passo 2 — Biblioteca",
                targetArea = "Biblioteca Inteligente",
                message = "Adiciona PDFs, livros, vídeos do YouTube, áudios e apontamentos. A Kudula transforma tudo em aprendizagem.",
                icon = Icons.Default.LocalLibrary,
                accentColor = KudulaElectricBlue,
                mood = MascotMood.CURIOUS
            ),
            TutorialStep(
                stepIndex = 3,
                title = "Passo 3 — Tutor Kudu",
                targetArea = "Tutor Socrático",
                message = "Faz perguntas sem vergonha. O Kudu explica de formas diferentes até fazer sentido.",
                icon = Icons.Default.AutoAwesome,
                accentColor = KudulaVioletLight,
                mood = MascotMood.ENCOURAGING
            ),
            TutorialStep(
                stepIndex = 4,
                title = "Passo 4 — Revisão Inteligente",
                targetArea = "Smart Revision (Repetição Espaçada)",
                message = "A Kudula lembra-te do momento certo para rever. Assim esqueces menos.",
                icon = Icons.Default.Bolt,
                accentColor = KudulaGold,
                mood = MascotMood.THINKING
            ),
            TutorialStep(
                stepIndex = 5,
                title = "Passo 5 — Carreira",
                targetArea = "MICK Carreiras & Oportunidades",
                message = "À medida que aprendes, descobres competências, oportunidades e caminhos para o teu futuro.",
                icon = Icons.Default.Work,
                accentColor = KudulaElectricBlueLight,
                mood = MascotMood.PROUD
            ),
            TutorialStep(
                stepIndex = 6,
                title = "Passo Final",
                targetArea = "Pronto para o teu Salto Cognitivo",
                message = "Já estás pronto. Vamos começar o teu próximo salto cognitivo.",
                icon = Icons.Default.EmojiEvents,
                accentColor = KudulaSuccess,
                mood = MascotMood.CELEBRATING
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]
    val isLastStep = currentStepIndex == steps.lastIndex

    // Spotlight breathing pulse
    val infiniteTransition = rememberInfiniteTransition(label = "spotlight_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.58f))
            .testTag("smart_home_tutorial_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Skip and Progress Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    steps.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .width(if (idx == currentStepIndex) 22.dp else 7.dp)
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (idx == currentStepIndex) KudulaCyanGlow else Color.White.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                TextButton(
                    onClick = onDismissTutorial,
                    modifier = Modifier.testTag("tutorial_skip_button")
                ) {
                    Text(
                        text = "Saltar",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Spotlight Visual Anchor (Simulates glowing spotlight over real underlying Home area)
            Box(
                modifier = Modifier
                    .size(110.dp * pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                currentStep.accentColor.copy(alpha = 0.45f),
                                currentStep.accentColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(KudulaNavySurface)
                        .border(BorderStroke(2.dp, currentStep.accentColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    MascotOwlAvatar(mood = currentStep.mood, eyeGlow = 1f)
                }
            }

            // Floating Guide Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface.copy(alpha = 0.95f)),
                border = BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(currentStep.accentColor.copy(alpha = 0.7f), KudulaElectricBlue.copy(alpha = 0.4f))
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tutorial_step_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Target area badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = currentStep.accentColor.copy(alpha = 0.18f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = currentStep.icon,
                                contentDescription = null,
                                tint = currentStep.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentStep.targetArea,
                                color = currentStep.accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentStep.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Navigation buttons inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStepIndex > 0) {
                            TextButton(
                                onClick = { currentStepIndex-- },
                                modifier = Modifier.testTag("tutorial_prev_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Voltar", color = TextSecondary, fontSize = 12.sp)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(40.dp))
                        }

                        Button(
                            onClick = {
                                if (isLastStep) {
                                    onFinishTutorial()
                                } else {
                                    currentStepIndex++
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLastStep) KudulaSuccess else KudulaElectricBlue
                            ),
                            modifier = Modifier.testTag("tutorial_next_button")
                        ) {
                            Text(
                                text = if (isLastStep) "Entrar na Kudula" else "Avançar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (isLastStep) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
