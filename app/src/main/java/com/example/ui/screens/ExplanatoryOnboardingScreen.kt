package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo

data class ExplanatoryStep(
    val stepIndex: Int,
    val title: String,
    val description: String,
    val benefitHighlight: String,
    val imageRes: Int
)

/**
 * Onboarding Explicativo Simples (F0.6 — Conforme imagem anexa 2)
 * 3 telas desenhadas para que qualquer usuário compreenda imediatamente:
 * 1. O que o aplicativo faz
 * 2. Para que serve
 * 3. Quais os benefícios práticos que recebe
 */
@Composable
fun ExplanatoryOnboardingScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit = onFinish,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact
    val scrollState = rememberScrollState()

    val steps = remember {
        listOf(
            ExplanatoryStep(
                stepIndex = 0,
                title = "Bem-vindo ao\nFuturo da Aprendizagem",
                description = "Vamos começar a jornada rumo ao conhecimento sólido. A Kudula lê livros, cadernos e vídeos do YouTube e explica tudo com palavras simples até entenderes de verdade.",
                benefitHighlight = "💡 Aprende sem decorar • Sem medo de matérias difíceis",
                imageRes = R.drawable.img_onboarding_1
            ),
            ExplanatoryStep(
                stepIndex = 1,
                title = "Vamos embarcar\nnesta jornada juntos",
                description = "Tens o Kudu, o teu tutor inteligente e paciente para tirar dúvidas sem vergonha. Ele descobre o teu melhor ritmo e avisa quando deves rever a matéria.",
                benefitHighlight = "⏱️ Estuda menos horas • Retém muito mais na memória",
                imageRes = R.drawable.img_students_one_phone_1790467498830
            ),
            ExplanatoryStep(
                stepIndex = 2,
                title = "O teu futuro\nestá em tuas mãos",
                description = "Tudo o que aprendes gera provas reais de competência, habilidades de alta demanda e caminhos claros para bolsas de estudo e carreiras globais.",
                benefitHighlight = "🚀 O teu conhecimento protegido criptograficamente",
                imageRes = R.drawable.img_young_professional_1790453801430
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]
    val isLastStep = currentStepIndex == steps.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(KudulaNavyDark, Color(0xFF131D38), KudulaNavyDark)
                )
            )
            .testTag("explanatory_onboarding_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, end = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = KudulaElectricBlue.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = "Passo ${currentStepIndex + 1} de 3",
                        color = KudulaCyanGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.testTag("skip_explanatory_onboarding")
                ) {
                    Text(
                        text = "Saltar",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arch Image Container (styled in dark blue design system)
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (isNarrow) 0.88f else 0.78f)
                    .height(if (isNarrow) 280.dp else 320.dp)
                    .clip(RoundedCornerShape(topStart = 140.dp, topEnd = 140.dp, bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(KudulaNavySurface)
                    .border(
                        2.5.dp,
                        Brush.verticalGradient(listOf(KudulaCyanGlow.copy(alpha = 0.7f), KudulaElectricBlue.copy(alpha = 0.4f))),
                        RoundedCornerShape(topStart = 140.dp, topEnd = 140.dp, bottomStart = 32.dp, bottomEnd = 32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep.imageRes,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "onboarding_img_anim"
                ) { imgRes ->
                    Image(
                        painter = painterResource(id = imgRes),
                        contentDescription = currentStep.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step Indicator Dots (Design system: cyan glow pill for active, round for inactive)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, _ ->
                    val isActive = index == currentStepIndex
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isActive) 26.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isActive) KudulaCyanGlow else Color.White.copy(alpha = 0.3f))
                            .clickable { currentStepIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Title & Description (attachment 2 style with dark theme typography)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
                    },
                    label = "onboarding_text_anim"
                ) { step ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = if (isNarrow) 22.sp else 25.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = if (isNarrow) 27.sp else 31.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = if (isNarrow) 13.sp else 14.5.sp,
                                lineHeight = if (isNarrow) 19.sp else 21.sp
                            ),
                            color = Color.White.copy(alpha = 0.90f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(if (isNarrow) 0.96f else 0.88f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Benefit Highlight Pill
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = KudulaElectricBlue.copy(alpha = 0.20f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.40f))
                        ) {
                            Text(
                                text = step.benefitHighlight,
                                color = KudulaCyanGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Action Button: "Seguinte" (Kudula Electric Blue with modern styling)
            Button(
                onClick = {
                    if (isLastStep) {
                        onFinish()
                    } else {
                        currentStepIndex += 1
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KudulaElectricBlue
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(if (isNarrow) 0.88f else 0.80f)
                    .height(52.dp)
                    .testTag("onboarding_next_action_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isLastStep) "Começar Agora" else "Seguinte",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Seguinte",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
