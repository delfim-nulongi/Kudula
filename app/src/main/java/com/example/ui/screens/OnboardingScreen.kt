package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.model.MascotMood
import com.example.model.QuickChallengeState
import com.example.ui.components.MascotOwlAvatar
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    currentStep: Int,
    selectedGoals: Set<String>,
    selectedStudyStyle: String,
    selectedExplanation: String,
    selectedAspiration: String,
    tutorName: String,
    explanationTone: String,
    onStepChange: (Int) -> Unit,
    onToggleGoal: (String) -> Unit,
    onSetStudyStyle: (String) -> Unit,
    onSetExplanation: (String) -> Unit,
    onSetAspiration: (String) -> Unit,
    onPersonalizeTutor: (String, String) -> Unit,
    onInstantValueDemo: () -> Unit,
    onFirstChallengeSuccess: () -> Unit = {},
    onFinish: () -> Unit,
    initialProfile: com.example.model.UserCognitiveProfile = com.example.model.UserCognitiveProfile(),
    onSaveFullProfile: (
        academicArea: String,
        professionalArea: String,
        companionName: String,
        tutorName: String,
        dailyRhythm: String,
        learningStyle: String,
        goals: Set<String>,
        interests: List<String>,
        careerDirection: String,
        riasec: List<String>,
        mbti: String,
        intelligences: List<String>,
        tutorPersonality: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact
    val totalSteps = 14

    // Priority 3: Goal Gradient Effect - Never starts at 0%! Begins at 20% and accelerates to 100%
    val progressPercent = (20 + (currentStep * 6)).coerceAtMost(100)
    val animatedProgress by animateFloatAsState(
        targetValue = 0.20f + (currentStep.toFloat() / (totalSteps - 1).coerceAtLeast(1)) * 0.80f,
        label = "goal_gradient"
    )

    // Step 1 & 2: Academic & Professional Area Local State
    var selectedAcademicArea by remember(initialProfile) { mutableStateOf(initialProfile.academicArea.ifBlank { "Tecnologia & Programação" }) }
    var customAcademicInput by remember { mutableStateOf("") }
    var isCustomAcademicSelected by remember { mutableStateOf(false) }

    var selectedProfessionalArea by remember(initialProfile) { mutableStateOf(initialProfile.professionalArea.ifBlank { "Desenvolvedor de Software" }) }
    var customProfessionalInput by remember { mutableStateOf("") }
    var isCustomProfessionalSelected by remember { mutableStateOf(false) }

    // Other cognitive dimensions local state
    var dailyRhythm by remember(initialProfile) { mutableStateOf(initialProfile.dailyRhythm.ifBlank { "Noite (Foco Silencioso)" }) }
    var companionNameState by remember(initialProfile) { mutableStateOf(initialProfile.companionName.ifBlank { "Kudu" }) }
    var customTutorInput by remember(tutorName, initialProfile) { mutableStateOf(initialProfile.tutorCustomName.ifBlank { tutorName.ifBlank { "Mentor Kai" } }) }
    var selectedToneState by remember(explanationTone, initialProfile) { mutableStateOf(initialProfile.tutorPersonality.ifBlank { explanationTone.ifBlank { "Socrático Questionador" } }) }
    var selectedInterests by remember(initialProfile) {
        mutableStateOf(
            if (initialProfile.mainInterests.isNotEmpty()) initialProfile.mainInterests.toSet()
            else setOf("Inteligência Artificial & LLMs", "Sistemas Distribuídos & Cloud", "Neurociência & Cognição")
        )
    }
    var riasecScores by remember(initialProfile) {
        mutableStateOf(
            if (initialProfile.riasecScores.isNotEmpty()) initialProfile.riasecScores.toSet()
            else setOf("Investigativo (I)", "Artístico (A)", "Empreendedor (E)")
        )
    }
    var selectedIntelligences by remember(initialProfile) {
        mutableStateOf(
            if (initialProfile.multipleIntelligences.isNotEmpty()) initialProfile.multipleIntelligences.toSet()
            else setOf("Lógica-Matemática", "Espacial", "Intrapessoal")
        )
    }

    // Step 8: MBTI Preference Builder (4 Dichotomies)
    var mbtiEorI by remember { mutableStateOf("I") }
    var mbtiSorN by remember { mutableStateOf("N") }
    var mbtiTorF by remember { mutableStateOf("T") }
    var mbtiJorP by remember { mutableStateOf("J") }
    val calculatedMbti = "$mbtiEorI$mbtiSorN$mbtiTorF$mbtiJorP"

    // Priority 11: Quick Challenge State Machine: IDLE -> ANSWERED -> CELEBRATING -> LOCKED
    var challengeState by remember { mutableStateOf(QuickChallengeState.IDLE) }
    var selectedChallengeOption by remember { mutableStateOf<Int?>(null) }
    val correctChallengeOption = 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 10.dp)
            .testTag("onboarding_screen")
    ) {
        // Official Brand Header & Goal Gradient Tracker
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (isNarrow) 32.dp else 38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(KudulaElectricBlue, KudulaViolet)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Onboarding Cognitivo",
                        tint = Color.White,
                        modifier = Modifier.size(if (isNarrow) 18.dp else 22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Calibração Cognitiva",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Onboarding KDAP • 12 Etapas",
                        style = MaterialTheme.typography.labelSmall,
                        color = KudulaCyanGlow,
                        fontSize = 9.sp
                    )
                }
            }

            Surface(
                onClick = onInstantValueDemo,
                shape = RoundedCornerShape(12.dp),
                color = KudulaViolet.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(KudulaVioletLight, KudulaCyanGlow)))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚡", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Demo KCLS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = KudulaCyanGlow,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Goal Gradient Motivational Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = KudulaElectricBlue.copy(alpha = 0.18f),
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(KudulaCyanGlow, KudulaElectricBlue))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🎯", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Calibração Cognitiva • $progressPercent% Concluído",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = KudulaCyanGlow
                    )
                    Text(
                        text = "O teu perfil adapta o motor neurocognitivo às tuas metas.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(KudulaElectricBlue, KudulaCyanGlow)))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "ETAPA ${currentStep + 1} DE $totalSteps",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = KudulaCyanGlow,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 14 Step Content Switcher
        Box(modifier = Modifier.weight(1f)) {
            when (currentStep) {
                // ETAPA 1: Área Acadêmica
                0 -> {
                    val academicAreas = listOf(
                        "Engenharia" to "Engenharia de Software, Mecânica, Eletrotécnica e Civil",
                        "Medicina" to "Ciências Médicas, Clínica Geral e Cirurgia",
                        "Tecnologia" to "Ciência da Computação, Redes e Telecomunicações",
                        "Gestão & Negócios" to "Administração, Finanças, Economia e Contabilidade",
                        "Direito" to "Direito Público, Privado e Regulamentação Digital",
                        "Educação" to "Pedagogia, Metodologias Ativas e Docência",
                        "Ciências Exatas & Naturais" to "Física, Química, Matemática e Biologia",
                        "Artes & Design" to "Design Visual, Arquitetura, Multimídia e Artes",
                        "Agricultura & Ambiente" to "Agronomia, Ecologia e Gestão Sustentável",
                        "Saúde & Biológicas" to "Enfermagem, Farmácia, Fisioterapia e Nutrição"
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Qual é a tua área acadêmica?",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Configura o vocabulário base e as analogias que o tutor utilizará.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(academicAreas) { (areaTitle, areaDesc) ->
                            val isSelected = !isCustomAcademicSelected && selectedAcademicArea == areaTitle
                            SelectableCard(
                                title = areaTitle,
                                subtitle = areaDesc,
                                isSelected = isSelected,
                                onClick = {
                                    isCustomAcademicSelected = false
                                    selectedAcademicArea = areaTitle
                                }
                            )
                        }

                        // Last card: Outra (Custom text field)
                        item {
                            SelectableCard(
                                title = "Outra Área Acadêmica",
                                subtitle = if (isCustomAcademicSelected && customAcademicInput.isNotBlank())
                                    "Personalizado: $customAcademicInput"
                                else
                                    "Toca para definir uma área personalizada",
                                isSelected = isCustomAcademicSelected,
                                onClick = {
                                    isCustomAcademicSelected = true
                                    if (customAcademicInput.isNotBlank()) {
                                        selectedAcademicArea = customAcademicInput
                                    }
                                }
                            )

                            if (isCustomAcademicSelected) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = customAcademicInput,
                                        onValueChange = {
                                            customAcademicInput = it
                                            selectedAcademicArea = it.ifBlank { "Personalizada" }
                                        },
                                        placeholder = { Text("Digita a tua área acadêmica...", color = TextTertiary, fontSize = 12.sp) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = KudulaCyanGlow,
                                            unfocusedBorderColor = BorderSubtle,
                                            focusedContainerColor = KudulaNavyContainer,
                                            unfocusedContainerColor = KudulaNavyContainer,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f).testTag("custom_academic_input")
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            if (customAcademicInput.isNotBlank()) {
                                                selectedAcademicArea = customAcademicInput
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue)
                                    ) {
                                        Text("Salvar", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // ETAPA 2: Área Profissional
                1 -> {
                    val professionalRoles = listOf(
                        "Desenvolvedor de Software" to "Criação de software, arquitetura web e mobile",
                        "Engenheiro" to "Soluções técnicas, automação e infraestrutura",
                        "Médico" to "Diagnósticos, cuidados clínicos e medicina de precisão",
                        "Professor / Educador" to "Transmissão pedagógica e capacitação acadêmica",
                        "Designer UI/UX" to "Experiência de utilizador e interfaces estéticas",
                        "Empreendedor / Fundador" to "Criação de startups, produtos e modelos de negócio",
                        "Gestor / Líder de Produto" to "Coordenação de equipas e priorização estratégica",
                        "Cientista / Investigador" to "Pesquisa experimental e desenvolvimento científico",
                        "Analista de Dados / Negócios" to "Inteligência analítica e modelação estatística",
                        "Marketing & Comunicação" to "Estratégia de crescimento, marca e impacto de público"
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Qual é a tua ocupação ou meta profissional?",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "O Kudula adapta casos práticos e quizes para desafios do teu quotidiano.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(professionalRoles) { (roleTitle, roleDesc) ->
                            val isSelected = !isCustomProfessionalSelected && selectedProfessionalArea == roleTitle
                            SelectableCard(
                                title = roleTitle,
                                subtitle = roleDesc,
                                isSelected = isSelected,
                                onClick = {
                                    isCustomProfessionalSelected = false
                                    selectedProfessionalArea = roleTitle
                                    onSetAspiration(roleTitle)
                                }
                            )
                        }

                        // Last card: Personalizar
                        item {
                            SelectableCard(
                                title = "Personalizar Ocupação",
                                subtitle = if (isCustomProfessionalSelected && customProfessionalInput.isNotBlank())
                                    "Ocupação: $customProfessionalInput"
                                else
                                    "Toca para definir uma profissão específica",
                                isSelected = isCustomProfessionalSelected,
                                onClick = {
                                    isCustomProfessionalSelected = true
                                    if (customProfessionalInput.isNotBlank()) {
                                        selectedProfessionalArea = customProfessionalInput
                                        onSetAspiration(customProfessionalInput)
                                    }
                                }
                            )

                            if (isCustomProfessionalSelected) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = customProfessionalInput,
                                        onValueChange = {
                                            customProfessionalInput = it
                                            selectedProfessionalArea = it.ifBlank { "Personalizada" }
                                            onSetAspiration(selectedProfessionalArea)
                                        },
                                        placeholder = { Text("Digita a tua profissão ou cargo...", color = TextTertiary, fontSize = 12.sp) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = KudulaCyanGlow,
                                            unfocusedBorderColor = BorderSubtle,
                                            focusedContainerColor = KudulaNavyContainer,
                                            unfocusedContainerColor = KudulaNavyContainer,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f).testTag("custom_professional_input")
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            if (customProfessionalInput.isNotBlank()) {
                                                selectedProfessionalArea = customProfessionalInput
                                                onSetAspiration(customProfessionalInput)
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue)
                                    ) {
                                        Text("Salvar", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // ETAPA 3: Objetivos Principais de Aprendizagem
                2 -> {
                    val availableGoals = listOf(
                        "Carreira Global & Liderança Técnica" to "Preparar para atuar em equipas globais de tecnologia e inovação",
                        "Aprovação em Concursos ou Exames de Alto Desempenho" to "Rigor metodológico e retenção de longo prazo",
                        "Empreendedorismo & Criação de Soluções" to "Construir produtos digitais e negócios de alto impacto",
                        "Especialização Científica & Investigação" to "Aprofundar teses, papers acadêmicos e descobertas",
                        "Domínio Prático de Novas Tecnologias" to "Aprender IA, arquiteturas distribuídas e linguagens modernas",
                        "Transição de Carreira Acelerada" to "Pivotar de área com scaffold cognitivo acelerado"
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Quais são os teus objetivos principais?",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Seleciona as aspirações para calibrar as recomendações do motor KCLS.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(availableGoals) { (goalTitle, goalDesc) ->
                            val isSelected = selectedGoals.contains(goalTitle)
                            SelectableCard(
                                title = goalTitle,
                                subtitle = goalDesc,
                                isSelected = isSelected,
                                onClick = { onToggleGoal(goalTitle) }
                            )
                        }
                    }
                }

                // ETAPA 4: Ritmo Diário de Estudo
                3 -> {
                    val rhythms = listOf(
                        "Madrugador Cognitivo (05:00 - 08:00)" to "Mente descansada com silêncio matinal absoluto.",
                        "Foco Noturno (21:00 - 23:30)" to "Concentração profunda em blocos sem interrupções.",
                        "Micro-Sessões Pomodoro (3 blocos de 25 min)" to "Picos curtos com intervalos de recuperação de atenção.",
                        "Fins de Semana Imersivos" to "Sessões profundas de 4 horas dedicadas a projetos práticos."
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Qual é o teu ritmo diário de estudo?",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "O Kudula adapta os alertas do IPA ao teu biorritmo de atenção.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(rhythms) { (rhythmTitle, rhythmDesc) ->
                            val isSelected = dailyRhythm == rhythmTitle
                            SelectableCard(
                                title = rhythmTitle,
                                subtitle = rhythmDesc,
                                isSelected = isSelected,
                                onClick = { dailyRhythm = rhythmTitle }
                            )
                        }
                    }
                }

                // ETAPA 5: Estilo de Aprendizagem
                4 -> {
                    val styles = listOf(
                        "Prático & Construtivo" to "Aprender construindo, executando código e resolvendo bugs reais.",
                        "Analítico & Estruturado" to "Compreender deduções lógicas, provas formais e arquitetura causal.",
                        "Visual & Mapeado" to "Absorver por diagramas conceituais, grafos semânticos e mapas mentais.",
                        "Conversacional & Socrático" to "Aprender por debate reflexivo, perguntas provocatórias e contra-exemplos."
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Como a tua mente absorve melhor?",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "O Motor Adaptativo KCLS ajusta as analogias e o formato do conteúdo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(styles) { (styleTitle, styleDesc) ->
                            val isSelected = selectedStudyStyle == styleTitle
                            SelectableCard(
                                title = styleTitle,
                                subtitle = styleDesc,
                                isSelected = isSelected,
                                onClick = { onSetStudyStyle(styleTitle) }
                            )
                        }
                    }
                }

                // ETAPA 6: Principais Interesses
                5 -> {
                    val interestCatalog = listOf(
                        "Inteligência Artificial & LLMs",
                        "Sistemas Distribuídos & Cloud",
                        "Neurociência & Cognição",
                        "Gestão de Produto & Inovação",
                        "Algoritmos Avançados & Matemática",
                        "Cibersegurança & Criptografia",
                        "Bioinformática & Genômica",
                        "Economia Comportamental & Decisão"
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Quais áreas despertam a tua curiosidade?",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Podes selecionar múltiplos domínios para enriquecer os teus grafos de conhecimento.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(interestCatalog) { interest ->
                            val isSelected = selectedInterests.contains(interest)
                            SelectableCard(
                                title = interest,
                                subtitle = if (isSelected) "✓ Tópico Prioritário no Grafo" else "Toca para adicionar aos interesses",
                                isSelected = isSelected,
                                onClick = {
                                    selectedInterests = if (isSelected) selectedInterests - interest else selectedInterests + interest
                                }
                            )
                        }
                    }
                }

                // ETAPA 7: RIASEC Explorer
                6 -> {
                    val riasecCategories = listOf(
                        "Investigativo (I)" to "Gosto de analisar, pesquisar, compreender fenômenos e resolver problemas lógicos.",
                        "Artístico (A)" to "Gosto de criar, inovar, expressar conceitos originais e desenhar experiências.",
                        "Empreendedor (E)" to "Gosto de liderar, influenciar pessoas, tomar riscos e construir projetos com tração.",
                        "Realista (R)" to "Gosto de construir com as próprias mãos, operar ferramentas e ver resultados concretos.",
                        "Social (S)" to "Gosto de ensinar, colaborar, orientar o crescimento alheio e atuar em comunidade.",
                        "Convencional (C)" to "Gosto de organizar dados, estabelecer processos claros, precisão e rigor metodológico."
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Explorador Vocacional RIASEC (Holland)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Identifica os teus vetores motivacionais profundos para orientar a tua mentoria.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(riasecCategories) { (riasecTitle, riasecDesc) ->
                            val isSelected = riasecScores.contains(riasecTitle)
                            SelectableCard(
                                title = riasecTitle,
                                subtitle = riasecDesc,
                                isSelected = isSelected,
                                onClick = {
                                    riasecScores = if (isSelected) riasecScores - riasecTitle else riasecScores + riasecTitle
                                }
                            )
                        }
                    }
                }

                // ETAPA 8: Construtor Tipológico MBTI
                7 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Construtor de Preferências MBTI",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Calibra como processas energia, percepção, decisão e organização temporal.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Live Calculated MBTI Card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = KudulaElectricBlue.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, KudulaCyanGlow),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = calculatedMbti,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = KudulaCyanGlow
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = when (calculatedMbti) {
                                                "INTJ" -> "Arquiteto Estratégico (Visão de Sistemas & Rigor)"
                                                "INTP" -> "Lógico Inovador (Modelagem Teórica & Análise)"
                                                "ENTJ" -> "Comandante Executor (Liderança & Eficiência)"
                                                "ENTP" -> "Visionário Socrático (Debates & Exploração)"
                                                "INFJ" -> "Conselheiro Idealista (Propósito & Metacognição)"
                                                "INFP" -> "Mediador Filosófico (Autenticidade & Significado)"
                                                "ENFJ" -> "Protagonista Inspirador (Coletividade & Mentoria)"
                                                "ENFP" -> "Ativista Criativo (Conexão de Ideias Livres)"
                                                else -> "Perfil Cognitivo Calibrado ($calculatedMbti)"
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "O tutor moldará os diálogos socráticos com base nesta topologia.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // 4 Dichotomies
                        item {
                            MbtiDichotomyRow(
                                title = "Energia & Atenção",
                                optionA = "I" to "Introversão (Reflexão Interna)",
                                optionB = "E" to "Extroversão (Ação Externa)",
                                selected = mbtiEorI,
                                onSelect = { mbtiEorI = it }
                            )
                        }

                        item {
                            MbtiDichotomyRow(
                                title = "Percepção da Realidade",
                                optionA = "N" to "Intuição (Padrões & Modelos)",
                                optionB = "S" to "Sensação (Fatos & Detalhes)",
                                selected = mbtiSorN,
                                onSelect = { mbtiSorN = it }
                            )
                        }

                        item {
                            MbtiDichotomyRow(
                                title = "Tomada de Decisão",
                                optionA = "T" to "Pensamento (Lógica Causal)",
                                optionB = "F" to "Sentimento (Valores & Empatia)",
                                selected = mbtiTorF,
                                onSelect = { mbtiTorF = it }
                            )
                        }

                        item {
                            MbtiDichotomyRow(
                                title = "Estilo de Vida & Organização",
                                optionA = "J" to "Julgamento (Planos Estruturados)",
                                optionB = "P" to "Percepção (Adaptabilidade Aberta)",
                                selected = mbtiJorP,
                                onSelect = { mbtiJorP = it }
                            )
                        }
                    }
                }

                // ETAPA 9: Inteligências Múltiplas (Todas as 9 de Howard Gardner)
                8 -> {
                    val all9Intelligences = listOf(
                        "Linguística" to "Sensibilidade à linguagem oral e escrita, semântica e retórica",
                        "Lógica-Matemática" to "Capacidade de dedução lógica, cálculo e pensamento científico",
                        "Musical" to "Reconhecimento de padrões auditivos, ritmo, métrica e harmonia",
                        "Espacial" to "Visualização 3D, mapas conceituais, topologia e orientação espacial",
                        "Corporal-Cinestésica" to "Expressão corporal, coordenação motora fina e memória tátil",
                        "Interpessoal" to "Compreensão das intenções, motivações e sentimentos de terceiros",
                        "Intrapessoal" to "Auto-conhecimento, regulação emocional e metacognição pessoal",
                        "Naturalista" to "Identificação de padrões ecológicos, fauna, flora e ecossistemas",
                        "Existencial" to "Reflexão sobre sentido, filosofia profunda e sistemas holísticos"
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Inteligências Múltiplas (9 Dimensões de Gardner)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Seleciona os teus vetores cognitivos mais fortes para ativar a constelação neural.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(all9Intelligences) { (intelTitle, intelDesc) ->
                            val isSelected = selectedIntelligences.contains(intelTitle)
                            SelectableCard(
                                title = intelTitle,
                                subtitle = intelDesc,
                                isSelected = isSelected,
                                onClick = {
                                    selectedIntelligences = if (isSelected) selectedIntelligences - intelTitle else selectedIntelligences + intelTitle
                                }
                            )
                        }
                    }
                }

                // ETAPA 10: Personalidade do Tutor de IA
                9 -> {
                    val tutorTones = listOf(
                        "Questionador Socrático" to "Estimula autonomia através de perguntas que expõem contradições e geram descoberta.",
                        "Prático & Construtivo" to "Foco em código funcional, passos acionáveis e arquitetura sem rodeios.",
                        "Analogias Criativas" to "Explica conceitos abstratos com metáforas do quotidiano e exemplos intuitivos.",
                        "Desafiador Rigoroso" to "Exige rigor matemático, contra-provas e validação contra alucinações conceituais."
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Personalidade do teu Mentor de IA",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Escolhe o arquétipo comunicacional que te mantém focado e desafiado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(tutorTones) { (toneTitle, toneDesc) ->
                            val isSelected = selectedToneState == toneTitle
                            SelectableCard(
                                title = toneTitle,
                                subtitle = toneDesc,
                                isSelected = isSelected,
                                onClick = {
                                    selectedToneState = toneTitle
                                    onPersonalizeTutor(customTutorInput, toneTitle)
                                }
                            )
                        }
                    }
                }

                // ETAPA 11: Nome do Companheiro Kudu
                10 -> {
                    val companionNames = listOf("Kudu", "Mwene", "Kamba", "Ngola")

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Como queres chamar o teu Mascote Kudu?",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "O Kudu acompanha o teu biorritmo, comemora as tuas vitórias e zela pelo teu descanso.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Friendly Animated Mascot Card
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = KudulaNavyContainer),
                                border = BorderStroke(
                                    1.5.dp,
                                    Brush.horizontalGradient(listOf(KudulaCyanGlow, KudulaElectricBlue, KudulaViolet))
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("onboarding_mascot_friendly_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(KudulaElectricBlue.copy(alpha = 0.3f), Color.Transparent)
                                                )
                                            )
                                            .border(2.dp, KudulaCyanGlow, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.kudu_friendly_mascot),
                                            contentDescription = "Mascote Kudu Amigável",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = KudulaElectricBlue.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🦉 ", fontSize = 14.sp)
                                            Text(
                                                text = "«Olá! Serei o teu mentor e guardião do ritmo de estudo!»",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Sugestões de Nomes:",
                                style = MaterialTheme.typography.labelMedium,
                                color = KudulaCyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                companionNames.forEach { name ->
                                    val isSelected = companionNameState.equals(name, ignoreCase = true)
                                    Surface(
                                        onClick = { companionNameState = name },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) KudulaElectricBlue else KudulaNavyContainer,
                                        border = BorderStroke(1.dp, if (isSelected) KudulaCyanGlow else BorderSubtle),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ou digita um nome personalizado:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = companionNameState,
                                onValueChange = { companionNameState = it },
                                placeholder = { Text("Nome do Mascote...", color = TextTertiary) },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KudulaCyanGlow,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedContainerColor = KudulaNavyContainer,
                                    unfocusedContainerColor = KudulaNavyContainer,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("companion_name_input")
                            )
                        }
                    }
                }

                // ETAPA 12: Nome do Tutor de IA
                11 -> {
                    val tutorNameSuggestions = listOf("Mentor Kai", "Sócrates", "Athena", "Prof. Ndalu")

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Como queres chamar o teu Mentor Cognitivo?",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Personaliza a identidade da IA que te guiará nos estudos profundos.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Sugestões de Nomes:",
                                style = MaterialTheme.typography.labelMedium,
                                color = KudulaCyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tutorNameSuggestions.forEach { name ->
                                    val isSelected = customTutorInput.equals(name, ignoreCase = true)
                                    Surface(
                                        onClick = {
                                            customTutorInput = name
                                            onPersonalizeTutor(name, selectedToneState)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) KudulaViolet else KudulaNavyContainer,
                                        border = BorderStroke(1.dp, if (isSelected) KudulaVioletLight else BorderSubtle),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ou digita um nome personalizado:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customTutorInput,
                                onValueChange = {
                                    customTutorInput = it
                                    onPersonalizeTutor(it, selectedToneState)
                                },
                                placeholder = { Text("Nome do Mentor...", color = TextTertiary) },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KudulaCyanGlow,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedContainerColor = KudulaNavyContainer,
                                    unfocusedContainerColor = KudulaNavyContainer,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("tutor_name_input")
                            )
                        }
                    }
                }

                // ETAPA 13: Goal Gradient Effect & Calibração Cognitiva Final
                12 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Efeito Gradiente de Meta (Goal Gradient)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "A tua prontidão cognitiva atingiu 92%. A reta final acelera a motivação intrínseca.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Architecture Summary Card
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                                border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🧠", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "ARQUITETURA COGNITIVA CALIBRADA",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = KudulaCyanGlow
                                            )
                                            Text(
                                                text = "Sinergia $selectedAcademicArea ⇄ $selectedProfessionalArea",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = BorderSubtle)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    val calculatedInitialIpa = remember(dailyRhythm, selectedIntelligences) {
                                        val rhythmBonus = when {
                                            dailyRhythm.contains("Madrugador") -> 12
                                            dailyRhythm.contains("Manhã") -> 10
                                            dailyRhythm.contains("Tarde") -> 8
                                            dailyRhythm.contains("Noite") -> 10
                                            else -> 10
                                        }
                                        val intBonus = (selectedIntelligences.size * 2).coerceAtMost(6)
                                        (rhythmBonus + intBonus).coerceIn(8, 16)
                                    }

                                    val calculatedInitialIkc = remember(selectedInterests, riasecScores, challengeState) {
                                        val base = (selectedInterests.size + riasecScores.size).coerceIn(4, 8)
                                        if (challengeState == QuickChallengeState.CELEBRATING || challengeState == QuickChallengeState.LOCKED) {
                                            base + 4
                                        } else {
                                            base
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Prontidão (IPA)", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                            Text("$calculatedInitialIpa/100 (Calibração Inicial)", style = MaterialTheme.typography.bodyMedium, color = KudulaCyanGlow, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Conhecimento (IKC)", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                            Text("$calculatedInitialIkc/100 (Início da Jornada)", style = MaterialTheme.typography.bodyMedium, color = KudulaVioletLight, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Tipologia MBTI", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                            Text(calculatedMbti, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = KudulaNavyContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Mascote: $companionNameState • Mentor: $customTutorInput",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = KudulaTealLight
                                            )
                                            Text(
                                                text = "Tom: $selectedToneState • Ritmo: $dailyRhythm",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ETAPA 14: Primeira Vitória (First Victory) com Correção de Bug do Desafio Rápido (Priority 11)
                13 -> {
                    val challengeOptions = listOf(
                        "Memorização repetitiva mecânica por 4 horas diárias",
                        "Decomposição em nós semânticos, validação socrática e adaptação ao IPA",
                        "Apenas leitura passiva de resumos pré-fabricados",
                        "Assistir a longos vídeos sem resolução prática de problemas"
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Primeira Vitória: Micro-Desafio KCLS",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Personalizado para $selectedAcademicArea & $selectedProfessionalArea.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KudulaCyanGlow
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Micro-lesson Content
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "💡", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "MICRO-CONCEITO FUNDAMENTAL",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = KudulaCyanGlow
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val microConceptText = when {
                                        selectedAcademicArea.contains("Saúde", ignoreCase = true) || selectedProfessionalArea.contains("Médic", ignoreCase = true) ->
                                            "Na área de $selectedAcademicArea e prática clínica, a retenção ativa e a resolução de casos socráticos previnem o esquecimento de protocolos críticos. O Kudula decompõe diretrizes em nós conceituais para garantir raciocínio diagnóstico de precisão."
                                        selectedAcademicArea.contains("Gestão", ignoreCase = true) || selectedProfessionalArea.contains("Negócio", ignoreCase = true) ->
                                            "No contexto de $selectedAcademicArea e liderança, a tomada de decisão sob pressão exige modelos mentais consolidados. O Kudula decompõe estratégias em nós conceituais acoplados a micro-desafios socráticos para aplicação prática imediata."
                                        selectedAcademicArea.contains("Direito", ignoreCase = true) || selectedAcademicArea.contains("Humanidades", ignoreCase = true) ->
                                            "Em $selectedAcademicArea, o domínio hermenêutico e a argumentação dependem de conexões conceituais ativas. O Kudula estrutura normas e fundamentos em mapas semânticos testados por perguntas socráticas espaçadas."
                                        selectedAcademicArea.contains("Design", ignoreCase = true) ->
                                            "No ecossistema de $selectedAcademicArea, a resolução de problemas complexos e a empatia de interação necessitam de repertório consolidado. O Kudula organiza princípios de usabilidade e arquitetura cognitiva para rápida assimilação."
                                        selectedAcademicArea.contains("Engenharia", ignoreCase = true) || selectedAcademicArea.contains("Exatas", ignoreCase = true) ->
                                            "Nas ciências de $selectedAcademicArea, a resolução de problemas técnicos exige aplicação deliberada de princípios fundamentais. O Kudula acopla teoria a micro-desafios graduais de alta precisão."
                                        else ->
                                            "A ilusão de competência acontece quando relemos textos passivamente. O Kudula combate o esquecimento decompondo o conteúdo de $selectedAcademicArea em nós conceituais acoplados a micro-desafios socráticos na hora exata da tua prontidão cognitiva."
                                    }
                                    Text(
                                        text = microConceptText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Pergunta de Validação:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Como o motor Kudula garante retenção duradoura sem sobrecarga?",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        // State Machine options (IDLE -> ANSWERED -> CELEBRATING -> LOCKED)
                        items(challengeOptions.indices.toList()) { index ->
                            val optionText = challengeOptions[index]
                            val isSelected = selectedChallengeOption == index
                            val isCorrect = index == correctChallengeOption

                            val cardColor = when {
                                challengeState == QuickChallengeState.CELEBRATING && isCorrect -> Color(0xFF10B981).copy(alpha = 0.25f)
                                challengeState == QuickChallengeState.ANSWERED && isSelected && !isCorrect -> Color(0xFFEF4444).copy(alpha = 0.20f)
                                isSelected -> KudulaElectricBlue.copy(alpha = 0.25f)
                                else -> KudulaNavyContainer
                            }

                            val borderColor = when {
                                challengeState == QuickChallengeState.CELEBRATING && isCorrect -> Color(0xFF10B981)
                                challengeState == QuickChallengeState.ANSWERED && isSelected && !isCorrect -> Color(0xFFEF4444)
                                isSelected -> KudulaCyanGlow
                                else -> BorderSubtle
                            }

                            Surface(
                                onClick = {
                                    if (challengeState != QuickChallengeState.LOCKED) {
                                        selectedChallengeOption = index
                                        // Priority 11 Fix: If correct -> CELEBRATING. If wrong -> ANSWERED (celebration cleared immediately!)
                                        if (index == correctChallengeOption) {
                                            challengeState = QuickChallengeState.CELEBRATING
                                            onFirstChallengeSuccess()
                                        } else {
                                            challengeState = QuickChallengeState.ANSWERED
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = cardColor,
                                border = BorderStroke(1.dp, borderColor),
                                modifier = Modifier.fillMaxWidth().testTag("challenge_option_$index")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) KudulaCyanGlow else KudulaNavySurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ('A'.code + index).toChar().toString(),
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) KudulaNavyDark else TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Priority 11: Consistent Celebration Banner (only visible during CELEBRATING or LOCKED with correct answer)
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                AnimatedVisibility(visible = challengeState == QuickChallengeState.CELEBRATING || (challengeState == QuickChallengeState.LOCKED && selectedChallengeOption == correctChallengeOption)) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.22f),
                                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "🎉", fontSize = 22.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Resposta Perfeita! Primeira Vitória Garantida!",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF34D399)
                                                )
                                                Text(
                                                    text = "+150 XP • IKC Compreensão: 88% • Perfil Pronto para Ativação",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }

                                // Gentle constructive hint when wrong answer is selected
                                AnimatedVisibility(visible = challengeState == QuickChallengeState.ANSWERED && selectedChallengeOption != null && selectedChallengeOption != correctChallengeOption) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "🔍", fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Ajuste Conceitual KCLS",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFF87171)
                                                )
                                                Text(
                                                    text = "A leitura passiva gera a ilusão de competência. O segredo está na validação socrática orientada pelo IPA.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = { onStepChange(currentStep - 1) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Text("Anterior", color = TextSecondary)
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            Button(
                onClick = {
                    if (currentStep < totalSteps - 1) {
                        onStepChange(currentStep + 1)
                    } else {
                        // Persist full cognitive profile & finalize
                        onSaveFullProfile(
                            selectedAcademicArea,
                            selectedProfessionalArea,
                            companionNameState,
                            customTutorInput,
                            dailyRhythm,
                            selectedStudyStyle,
                            selectedGoals,
                            selectedInterests.toList(),
                            selectedProfessionalArea,
                            riasecScores.toList(),
                            calculatedMbti,
                            selectedIntelligences.toList(),
                            selectedToneState
                        )
                        onFinish()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue),
                modifier = Modifier.testTag("onboarding_next_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (currentStep == totalSteps - 1) "Ativar Kudula OS & Começar" else "Seguinte",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectableCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) KudulaElectricBlue.copy(alpha = 0.22f) else KudulaNavyContainer,
        border = BorderStroke(
            1.dp,
            if (isSelected) {
                Brush.horizontalGradient(listOf(KudulaCyanGlow, KudulaElectricBlue))
            } else {
                Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) KudulaCyanGlow else TextSecondary,
                    fontSize = 11.sp
                )
            }
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = KudulaCyanGlow,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MbtiDichotomyRow(
    title: String,
    optionA: Pair<String, String>,
    optionB: Pair<String, String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = KudulaCyanGlow
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(optionA, optionB).forEach { (code, desc) ->
                val isSelected = selected == code
                Surface(
                    onClick = { onSelect(code) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) KudulaElectricBlue.copy(alpha = 0.35f) else KudulaNavyContainer,
                    border = BorderStroke(1.dp, if (isSelected) KudulaCyanGlow else BorderSubtle),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) KudulaCyanGlow else Color.White
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "✓", color = KudulaCyanGlow, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
