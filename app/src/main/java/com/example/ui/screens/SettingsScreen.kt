package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ReadingVoiceConfig
import com.example.model.SupportedLanguage
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue

@Composable
fun SettingsScreen(
    isMascotVisible: Boolean,
    onToggleMascot: () -> Unit,
    selectedLanguage: SupportedLanguage,
    onSelectLanguage: (SupportedLanguage) -> Unit,
    isReduceMotion: Boolean,
    onToggleReduceMotion: (Boolean) -> Unit,
    isHighContrast: Boolean,
    onToggleHighContrast: (Boolean) -> Unit,
    voiceConfig: ReadingVoiceConfig = ReadingVoiceConfig(),
    onVoiceConfigChange: (ReadingVoiceConfig) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPlans: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 12.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(if (isNarrow) 12.dp else 16.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (isNarrow) 34.dp else 38.dp)
                        .clip(CircleShape)
                        .background(KudulaElectricBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(if (isNarrow) 18.dp else 22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Definições do Sistema",
                        style = if (isNarrow) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Preferências, Acessibilidade e Governação",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = if (isNarrow) 11.sp else 12.sp
                    )
                }
            }
        }

        // Account & User Profile Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProfile() }
                    .testTag("settings_account_profile_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(KudulaElectricBlue.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "A Tua Conta & Perfil",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Gerir email, senha, nome e sincronização",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
                }
            }
        }

        // Entrada 2 — Definições: Assinatura e Planos
        item {
            val planManager = remember { com.example.data.plan.PlanManager.getInstance() }
            val currentPlan by planManager.currentPlan.collectAsStateWithLifecycle()

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPlans() }
                    .testTag("settings_plans_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(KudulaElectricBlue.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = KudulaCyanGlow,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Assinatura e Planos",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = KudulaElectricBlue.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = currentPlan.displayName,
                                        color = KudulaCyanGlow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Gere o teu plano e desbloqueia ferramentas de IA",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
                }
            }
        }

        // Mascot Companion Settings Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                modifier = Modifier.fillMaxWidth().testTag("mascot_settings_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Companheiro Cognitivo (Kudu)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Exibe o mascote animado que reage a vitórias, fadiga, erros e conquistas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isMascotVisible,
                        onCheckedChange = { onToggleMascot() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = KudulaElectricBlue
                        ),
                        modifier = Modifier.testTag("mascot_toggle_switch")
                    )
                }
            }
        }

        // Voice Configuration Card for Active Reading & AI Speech
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(KudulaCyanGlow.copy(alpha = 0.4f), KudulaElectricBlue.copy(alpha = 0.3f)))
                ),
                modifier = Modifier.fillMaxWidth().testTag("voice_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KudulaCyanGlow.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "VOZ DA IA • LEITURA EM VOZ ALTA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = KudulaCyanGlow,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Configuração de síntese neural para o Leitor Ativo e Mentor",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Tipo de Voz
                    Text(
                        text = "Tipo de Voz Neural:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val voiceTypes = listOf("Neural Socrática", "Narrativa Acadêmica", "Clara & Amigável", "Foco Imersivo")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        voiceTypes.take(2).forEach { vt ->
                            val isSel = voiceConfig.voiceType == vt
                            Surface(
                                onClick = { onVoiceConfigChange(voiceConfig.copy(voiceType = vt)) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) KudulaElectricBlue else KudulaNavyContainer,
                                border = if (isSel) null else CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = vt,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        voiceTypes.drop(2).forEach { vt ->
                            val isSel = voiceConfig.voiceType == vt
                            Surface(
                                onClick = { onVoiceConfigChange(voiceConfig.copy(voiceType = vt)) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) KudulaElectricBlue else KudulaNavyContainer,
                                border = if (isSel) null else CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = vt,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Género da Voz
                    Text(
                        text = "Género da Voz:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Feminina", "Masculina").forEach { gen ->
                            val isSel = voiceConfig.gender == gen
                            Surface(
                                onClick = { onVoiceConfigChange(voiceConfig.copy(gender = gen)) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) KudulaViolet else KudulaNavyContainer,
                                border = if (isSel) null else CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (gen == "Feminina") Icons.Default.Face else Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = if (isSel) Color.White else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = gen,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Ritmo de Fala
                    Text(
                        text = "Ritmo & Tom:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val rhythms = listOf("Cadenciado & Focado", "Dinâmico", "Pausado & Reflexivo")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rhythms.forEach { r ->
                            val isSel = voiceConfig.rhythm == r
                            Surface(
                                onClick = { onVoiceConfigChange(voiceConfig.copy(rhythm = r)) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) KudulaElectricBlue.copy(alpha = 0.35f) else KudulaNavyContainer,
                                border = BorderStroke(1.dp, if (isSel) KudulaCyanGlow else BorderSubtle),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = r,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) KudulaCyanGlow else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Velocidade de Reprodução
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Velocidade da Leitura:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${voiceConfig.speed}x",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = KudulaCyanGlow
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        speeds.forEach { spd ->
                            val isSel = voiceConfig.speed == spd
                            Surface(
                                onClick = { onVoiceConfigChange(voiceConfig.copy(speed = spd)) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) KudulaCyanGlow else KudulaNavyContainer,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${spd}x",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSel) KudulaNavyDark else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Language Selection Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                modifier = Modifier.fillMaxWidth().testTag("language_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "IDIOMA DA PLATAFORMA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = KudulaCyanGlow,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Deteção automática de idioma suportada. Idioma principal: Português (Angola).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SupportedLanguage.entries.forEach { lang ->
                        val isSelected = selectedLanguage == lang
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) KudulaElectricBlue.copy(alpha = 0.25f) else KudulaNavyContainer,
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(KudulaCyanGlow, KudulaElectricBlue))) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSelectLanguage(lang) }
                                .testTag("language_option_${lang.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = lang.flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = KudulaCyanGlow)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Accessibility Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                modifier = Modifier.fillMaxWidth().testTag("accessibility_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACESSIBILIDADE & DESIGN INCLUSIVO (WCAG AA)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = KudulaVioletLight,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reduzir Movimento (Reduce Motion)", style = MaterialTheme.typography.titleSmall, color = Color.White)
                            Text("Desativa transições animadas e efeitos de pulsação nos grafos.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isReduceMotion,
                            onCheckedChange = onToggleReduceMotion,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = KudulaViolet
                            ),
                            modifier = Modifier.testTag("reduce_motion_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alto Contraste", style = MaterialTheme.typography.titleSmall, color = Color.White)
                            Text("Aumenta o contraste visual para leitura sob luz solar intensa.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isHighContrast,
                            onCheckedChange = onToggleHighContrast,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = KudulaTeal
                            ),
                            modifier = Modifier.testTag("high_contrast_switch")
                        )
                    }
                }
            }
        }

        // Privacy & Responsible AI Governance Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavyContainer),
                modifier = Modifier.fillMaxWidth().testTag("governance_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GOVERNAÇÃO DE IA & PRIVACIDADE (HITL)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = KudulaSuccess,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "O sistema segue rigorosamente o princípio Human-in-the-Loop (HITL). Os teus dados de telemetria pertencem-te e são usados exclusivamente para adaptar o KCLS e emitir tuas provas criptográficas de aprendizagem (PoL).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
