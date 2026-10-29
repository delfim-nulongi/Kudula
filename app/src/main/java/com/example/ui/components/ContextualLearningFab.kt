package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.KudulaScreen

data class FabConfig(
    val label: String,
    val icon: ImageVector,
    val containerGradient: List<Color>,
    val actionDescription: String
)

@Composable
fun ContextualLearningFab(
    currentScreen: KudulaScreen,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config = when (currentScreen) {
        KudulaScreen.READING_WORKSPACE -> FabConfig(
            label = "Explicar",
            icon = Icons.Default.Lightbulb,
            containerGradient = listOf(KudulaElectricBlue, KudulaViolet),
            actionDescription = "Explicar trecho com Socrático"
        )
        KudulaScreen.YOUTUBE_INTELLIGENCE -> FabConfig(
            label = "Analisar",
            icon = Icons.Default.SmartDisplay,
            containerGradient = listOf(Color(0xFFE11D48), KudulaViolet),
            actionDescription = "Extrair marcos e quiz"
        )
        KudulaScreen.SMART_REVISION -> FabConfig(
            label = "Pista KDAP",
            icon = Icons.Default.Psychology,
            containerGradient = listOf(KudulaTeal, KudulaElectricBlue),
            actionDescription = "Pista na Zona Proximal"
        )
        KudulaScreen.CAREER_HUB -> FabConfig(
            label = "Roadmap",
            icon = Icons.Default.AltRoute,
            containerGradient = listOf(KudulaViolet, KudulaElectricBlue),
            actionDescription = "Gerar Roadmap de Competências"
        )
        KudulaScreen.FUTURE_SELF -> FabConfig(
            label = "Simular",
            icon = Icons.Default.Visibility,
            containerGradient = listOf(KudulaGold, Color(0xFFD97706)),
            actionDescription = "Simular horizonte futuro"
        )
        KudulaScreen.LIBRARY -> FabConfig(
            label = "Processar KCLS",
            icon = Icons.Default.FileUpload,
            containerGradient = listOf(KudulaElectricBlue, KudulaCyanGlow),
            actionDescription = "Carregar e extrair conhecimento"
        )
        else -> FabConfig(
            label = "Tutor Kudu",
            icon = Icons.Default.AutoAwesome,
            containerGradient = listOf(KudulaElectricBlue, KudulaViolet),
            actionDescription = "Abrir mentoria socrática"
        )
    }

    AnimatedContent(
        targetState = config,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "contextual_fab",
        modifier = modifier
    ) { currentConfig ->
        // Priority 3: Containers Everywhere — icon and label wrapped in rounded elevated container
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            shadowElevation = 8.dp,
            modifier = Modifier
                .testTag("contextual_learning_fab")
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)),
                    RoundedCornerShape(20.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.horizontalGradient(currentConfig.containerGradient))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = currentConfig.icon,
                            contentDescription = currentConfig.actionDescription,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = currentConfig.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
