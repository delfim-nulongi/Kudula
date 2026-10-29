package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.data.local.KudulaDatabase
import com.example.data.sync.SyncManager
import com.example.data.sync.SyncRepository
import com.example.model.SupportedLanguage
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.util.LocalScreenWindowInfo
import com.example.ui.util.rememberScreenWindowInfo
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.KudulaScreen
import com.example.viewmodel.KudulaViewModel
import com.example.viewmodel.LearningSessionViewModel
import com.example.viewmodel.VoiceNotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.data.security.KudulaAppCheckManager.initAppCheck(applicationContext)
        enableEdgeToEdge()
        setContent {
            val viewModel: KudulaViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val learningSessionViewModel: LearningSessionViewModel = viewModel()
            val learningSessionState by learningSessionViewModel.uiState.collectAsStateWithLifecycle()
            val authViewModel: AuthViewModel = viewModel()
            val authState by authViewModel.uiState.collectAsStateWithLifecycle()
            val voiceNotesViewModel: VoiceNotesViewModel = viewModel()
            val voiceNotesState by voiceNotesViewModel.uiState.collectAsStateWithLifecycle()

            val context = LocalContext.current
            val syncManager = remember {
                val db = KudulaDatabase.getInstance(context)
                val syncRepo = SyncRepository(
                    sessionDao = db.learningSessionDao(),
                    eventDao = db.kudulaEventDao(),
                    goalDao = db.cognitiveGoalDao(),
                    reflectionDao = db.reflectionJournalDao(),
                    voiceNoteDao = db.voiceNoteDao()
                )
                SyncManager(context.applicationContext, syncRepo)
            }
            val isSyncing by syncManager.isSyncing.collectAsStateWithLifecycle()
            val syncStatusMessage by syncManager.syncStatusMessage.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                syncManager.onAppForegrounded()
            }
            LaunchedEffect(authState.authState) {
                if (authState.authState is com.example.data.auth.AuthState.Authenticated) {
                    val authUser = authState.authState as com.example.data.auth.AuthState.Authenticated
                    syncManager.onUserAuthenticated()
                    viewModel.loadUserProfile(authUser.uid, authUser.displayName, authUser.email)
                }
            }
            val windowInfo = rememberScreenWindowInfo()
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: KudulaScreen.SPLASH.route
            val currentScreen = KudulaScreen.entries.find { it.route == currentRoute } ?: KudulaScreen.SPLASH

            // Synchronize ViewModel mascot mood and message when route changes
            LaunchedEffect(currentScreen) {
                if (state.currentScreen != currentScreen) {
                    viewModel.navigateTo(currentScreen)
                }
            }

            // Observe progressive login trigger events
            LaunchedEffect(state.pendingProgressiveLoginReason) {
                state.pendingProgressiveLoginReason?.let { reason ->
                    authViewModel.triggerProgressiveLogin(reason)
                    viewModel.clearProgressiveLoginPrompt()
                }
            }

            CompositionLocalProvider(LocalScreenWindowInfo provides windowInfo) {
                KudulaTheme(darkTheme = !state.isHighContrast) {
                var showMoreSheet by remember { mutableStateOf(false) }
                var showLanguageDialog by remember { mutableStateOf(false) }
                var showMascotDialog by remember { mutableStateOf(false) }
                var showVoiceNotesSheet by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KudulaNavyDark)
                        .testTag("kudula_main_scaffold"),
                    topBar = {
                        val hideTopBar = currentScreen == KudulaScreen.SPLASH ||
                            currentScreen == KudulaScreen.EXPLANATORY_ONBOARDING ||
                            currentScreen == KudulaScreen.AUTH_LOGIN ||
                            currentScreen == KudulaScreen.ONBOARDING ||
                            currentScreen == KudulaScreen.READING_WORKSPACE ||
                            currentScreen == KudulaScreen.USER_PROFILE
                        if (!hideTopBar) {
                            KudulaTopBar(
                                profile = state.profile,
                                currentScreen = currentScreen,
                                currentLanguage = state.language,
                                onLanguageClick = { showLanguageDialog = true },
                                onSettingsClick = {
                                    navController.navigate(KudulaScreen.SETTINGS.route) {
                                        launchSingleTop = true
                                    }
                                },
                                onProfileClick = {
                                    navController.navigate(KudulaScreen.USER_PROFILE.route) {
                                        launchSingleTop = true
                                    }
                                },
                                onMascotClick = { showMascotDialog = true },
                                isMascotActive = state.isMascotVisible,
                                onInvestorDemoClick = { viewModel.openInvestorDemo() },
                                onToggleFlowMode = { viewModel.toggleFlowModeIpa() },
                                isFlowModeHighIpa = state.isFlowModeHighIpa
                            )
                        }
                    },
                    bottomBar = {
                        val hideBottomBar = currentScreen == KudulaScreen.SPLASH ||
                            currentScreen == KudulaScreen.EXPLANATORY_ONBOARDING ||
                            currentScreen == KudulaScreen.AUTH_LOGIN ||
                            currentScreen == KudulaScreen.ONBOARDING ||
                            currentScreen == KudulaScreen.READING_WORKSPACE ||
                            currentScreen == KudulaScreen.USER_PROFILE
                        if (!hideBottomBar) {
                            KudulaBottomNavigationBar(
                                currentScreen = currentScreen,
                                onSelectScreen = { screen ->
                                    navController.navigate(screen.route) {
                                        popUpTo(KudulaScreen.HOME.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onOpenMoreMenu = { showMoreSheet = true }
                            )
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = KudulaScreen.SPLASH.route,
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = { fadeIn(tween(250)) },
                            exitTransition = { fadeOut(tween(200)) },
                            popEnterTransition = { fadeIn(tween(250)) },
                            popExitTransition = { fadeOut(tween(200)) }
                        ) {
                            // 1. Tela de Boas-vindas (Splash)
                            composable(KudulaScreen.SPLASH.route) {
                                SplashScreen(
                                    onStartClick = {
                                        if (authState.authState is com.example.data.auth.AuthState.Authenticated) {
                                            val currentUid = (authState.authState as com.example.data.auth.AuthState.Authenticated).uid
                                            val isOnboarded = viewModel.isUserOnboarded(currentUid) || state.profile.onboardingCompleted
                                            if (isOnboarded) {
                                                navController.navigate(KudulaScreen.HOME.route) {
                                                    popUpTo(KudulaScreen.SPLASH.route) { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate(KudulaScreen.ONBOARDING.route) {
                                                    popUpTo(KudulaScreen.SPLASH.route) { inclusive = true }
                                                }
                                            }
                                        } else {
                                            navController.navigate(KudulaScreen.EXPLANATORY_ONBOARDING.route)
                                        }
                                    },
                                    onExploreFirstClick = { navController.navigate(KudulaScreen.EXPLANATORY_ONBOARDING.route) }
                                )
                            }

                            // 2. 3 Telas de Onboarding Explicativo
                            composable(KudulaScreen.EXPLANATORY_ONBOARDING.route) {
                                ExplanatoryOnboardingScreen(
                                    onFinish = { navController.navigate(KudulaScreen.AUTH_LOGIN.route) },
                                    onSkip = { navController.navigate(KudulaScreen.AUTH_LOGIN.route) }
                                )
                            }

                            // 3. Telas de Login Dedicadas (Email, Senha e Google)
                            composable(KudulaScreen.AUTH_LOGIN.route) {
                                AuthLoginScreen(
                                    authViewModel = authViewModel,
                                    authState = authState,
                                    onAuthSuccess = {
                                        val currentAuth = authState.authState
                                        val currentUid = if (currentAuth is com.example.data.auth.AuthState.Authenticated) {
                                            currentAuth.uid
                                        } else {
                                            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                        }
                                        val isOnboarded = (currentUid != null && viewModel.isUserOnboarded(currentUid)) || state.profile.onboardingCompleted
                                        if (isOnboarded) {
                                            navController.navigate(KudulaScreen.HOME.route) {
                                                popUpTo(KudulaScreen.AUTH_LOGIN.route) { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate(KudulaScreen.ONBOARDING.route) {
                                                popUpTo(KudulaScreen.AUTH_LOGIN.route) { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            }

                            // 4. Tela de Perfil do Utilizador (Firebase Auth)
                            composable(KudulaScreen.USER_PROFILE.route) {
                                UserProfileScreen(
                                    profile = state.profile,
                                    authState = authState,
                                    authViewModel = authViewModel,
                                    onUpdateName = { viewModel.updateProfileName(it) },
                                    onContinueToOnboarding = {
                                        navController.navigate(KudulaScreen.ONBOARDING.route)
                                    },
                                    onRecalibrateProfile = {
                                        viewModel.startProfileUpdate()
                                        navController.navigate(KudulaScreen.ONBOARDING.route)
                                    },
                                    onNavigateBack = {
                                        if (!navController.popBackStack()) {
                                            navController.navigate(KudulaScreen.HOME.route) {
                                                popUpTo(KudulaScreen.HOME.route) { inclusive = false }
                                            }
                                        }
                                    },
                                    onSignOutSuccess = {
                                        navController.navigate(KudulaScreen.AUTH_LOGIN.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    onNavigateToPlans = { navController.navigate(KudulaScreen.PLANS.route) }
                                )
                            }

                            // 5. Onboarding Cognitivo de 14 Etapas
                            composable(KudulaScreen.ONBOARDING.route) {
                                OnboardingScreen(
                                    currentStep = state.onboardingStep,
                                    selectedGoals = state.selectedGoals,
                                    selectedStudyStyle = state.selectedStudyStyle,
                                    selectedExplanation = state.selectedExplanationMode,
                                    selectedAspiration = state.selectedCareerAspiration,
                                    tutorName = state.aiTutorName,
                                    explanationTone = state.aiExplanationTone,
                                    initialProfile = state.profile,
                                    onStepChange = { viewModel.updateOnboardingStep(it) },
                                    onToggleGoal = { viewModel.toggleOnboardingGoal(it) },
                                    onSetStudyStyle = { viewModel.setOnboardingStudyStyle(it) },
                                    onSetExplanation = { viewModel.setOnboardingExplanation(it) },
                                    onSetAspiration = { viewModel.setOnboardingAspiration(it) },
                                    onPersonalizeTutor = { name, tone -> viewModel.personalizeAiTutor(name, tone) },
                                    onInstantValueDemo = { viewModel.runInstantValueDemo() },
                                    onFirstChallengeSuccess = {
                                        viewModel.unlockFirstCognitiveLeap()
                                        learningSessionViewModel.recordLearningSession(
                                            concept = "Primeiro Salto Cognitivo",
                                            sourceType = "DESAFIO_ONBOARDING",
                                            studyMinutes = 2,
                                            ikcPartial = 88,
                                            ipaPartial = 92
                                        )
                                    },
                                    onFinish = {
                                        val isUpdateMode = state.onboardingMode == com.example.model.OnboardingMode.PROFILE_UPDATE
                                        viewModel.finishOnboarding()
                                        if (isUpdateMode) {
                                            navController.navigate(KudulaScreen.USER_PROFILE.route) {
                                                popUpTo(KudulaScreen.ONBOARDING.route) { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate(KudulaScreen.HOME.route) {
                                                popUpTo(KudulaScreen.SPLASH.route) { inclusive = true }
                                            }
                                        }
                                    },
                                    onSaveFullProfile = { academicArea, professionalArea, compName, tutName, rhythm, style, goals, interests, aspiration, riasec, mbti, intelligences, personality ->
                                        viewModel.saveFullCognitiveProfile(
                                            academicArea = academicArea,
                                            professionalArea = professionalArea,
                                            companionName = compName,
                                            tutorName = tutName,
                                            dailyRhythm = rhythm,
                                            learningStyle = style,
                                            goals = goals,
                                            interests = interests,
                                            careerDirection = aspiration,
                                            riasec = riasec,
                                            mbti = mbti,
                                            intelligences = intelligences,
                                            tutorPersonality = personality
                                        )
                                    }
                                )
                            }

                            // 6. Menu Principal (HomeScreen)
                            composable(KudulaScreen.HOME.route) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    HomeScreen(
                                        profile = state.profile,
                                        isMascotVisible = state.isMascotVisible,
                                        mascotMood = state.mascotMood,
                                        mascotMessage = state.mascotMessage,
                                        libraryItems = state.libraryItems,
                                        isFlowModeHighIpa = state.isFlowModeHighIpa,
                                        onDismissMascot = { viewModel.toggleMascot() },
                                        onMascotTap = { showMascotDialog = true },
                                        onNavigate = { screen ->
                                            navController.navigate(screen.route) {
                                                launchSingleTop = true
                                            }
                                        },
                                        onOpenItem = { item ->
                                            viewModel.openReadingWorkspace(item)
                                            navController.navigate(KudulaScreen.READING_WORKSPACE.route)
                                        },
                                        onOpenDemoMode = { viewModel.openInvestorDemo() },
                                        onToggleFlowMode = { viewModel.toggleFlowModeIpa() },
                                        onOpenVoiceNotes = { showVoiceNotesSheet = true },
                                        sessions = learningSessionState.sessions,
                                        events = learningSessionState.events,
                                        goals = learningSessionState.goals,
                                        modifier = if (state.showHomeTutorial) Modifier.blur(20.dp) else Modifier
                                    )

                                    if (state.showHomeTutorial) {
                                        SmartHomeTutorialOverlay(
                                            onDismissTutorial = { viewModel.dismissHomeTutorial(proceedToOnboarding = false) },
                                            onFinishTutorial = {
                                                viewModel.dismissHomeTutorial(proceedToOnboarding = true)
                                                navController.navigate(KudulaScreen.ONBOARDING.route)
                                            }
                                        )
                                    }
                                }
                            }

                            // Biblioteca Inteligente
                            composable(KudulaScreen.LIBRARY.route) {
                                SmartLibraryScreen(
                                    libraryItems = state.libraryItems,

                                    onOpenItem = { item ->
                                        viewModel.openReadingWorkspace(item)
                                        navController.navigate(KudulaScreen.READING_WORKSPACE.route)
                                    },

                                    onAddItem = { title, type, cat ->
                                        viewModel.addCustomLibraryItem(title, type, cat)
                                    },

                                    onOpenYouTubeIntelligence = { url ->
                                        viewModel.setYouTubeUrl(url)
                                        viewModel.analyzeYouTubeVideo(url)
                                        navController.navigate(KudulaScreen.YOUTUBE_INTELLIGENCE.route)
                                    },

                                    // Liga a interface ao estado do processamento real.
                                    processingState = state.libraryProcessing,
                                    analyses = state.libraryAnalyses,

                                    // Envia o ficheiro ou URL para o pipeline real da Kudula AI.
                                    onProcessContent = { title, type, category, uri, youtubeUrl ->
                                        viewModel.processLibraryContent(
                                            title = title,
                                            type = type,
                                            category = category,
                                            uri = uri,
                                            youtubeUrl = youtubeUrl
                                        )
                                    },

                                    onCancelProcessing = {
                                        viewModel.cancelLibraryProcessing()
                                    },

                                    onRetryProcessing = {
                                        viewModel.retryLibraryProcessing()
                                    },

                                    onDismissProcessing = {
                                        viewModel.dismissLibraryProcessing()
                                    }
                                )
                            }

                            // Tutor Kudu IA
                            composable(KudulaScreen.AI_TUTOR.route) {
                                AITutorScreen(
                                    messages = state.chatMessages,
                                    isThinking = state.isTutorThinking,
                                    kdapLevel = state.profile.kdapLevel,
                                    onSendMessage = { viewModel.sendTutorMessage(it) },
                                    onSetKdapLevel = { viewModel.setKDAPLevel(it) }
                                )
                            }

                            // YouTube Intelligence
                            composable(KudulaScreen.YOUTUBE_INTELLIGENCE.route) {
                                YouTubeIntelligenceScreen(
                                    currentUrl = state.youtubeUrlInput,
                                    analysis = state.youtubeAnalysis,
                                    isAnalyzing = state.isAnalyzingYouTube,
                                    onUrlChange = { viewModel.setYouTubeUrl(it) },
                                    onAnalyze = { viewModel.analyzeYouTubeVideo(it) },
                                    onRecordQuizAnswer = { correct, time -> viewModel.recordQuizAnswer(correct, time) }
                                )
                            }

                            // Espaço de Leitura Ativa
                            composable(KudulaScreen.READING_WORKSPACE.route) {
                                ReadingWorkspaceScreen(
                                    item = state.currentReadingItem,
                                    analysis = state.currentReadingItem?.let { selectedItem ->
                                        state.libraryAnalyses[selectedItem.id]
                                    },
                                    fontSize = state.readingFontSize,
                                    themeMode = state.readingTheme,
                                    voiceConfig = state.profile.voiceConfig,
                                    onFontSizeChange = { viewModel.changeReadingFontSize(it) },
                                    onThemeModeChange = { viewModel.setReadingTheme(it) },
                                    onVoiceConfigChange = { viewModel.updateReadingVoiceConfig(it) },
                                    onSaveEditedStructure = { itemId, chapters ->
                                        viewModel.saveEditedLibraryStructure(itemId, chapters)
                                    },
                                    onResetStructureToAi = { itemId ->
                                        viewModel.resetLibraryStructureToAi(itemId)
                                    },
                                    onBack = { navController.popBackStack() },
                                    onAskTutor = { prompt ->
                                        viewModel.sendTutorMessage(prompt)
                                        navController.navigate(KudulaScreen.AI_TUTOR.route)
                                    }
                                )
                            }

                            // Revisão Adaptativa Inteligente
                            composable(KudulaScreen.SMART_REVISION.route) {
                                SmartRevisionScreen(
                                    flashcards = state.flashcards,
                                    currentIndex = state.activeFlashcardIndex,
                                    isFlipped = state.isFlashcardFlipped,
                                    onFlip = { viewModel.flipFlashcard() },
                                    onAnswer = { remembered -> viewModel.nextFlashcard(remembered) }
                                )
                            }

                            // Dashboard IKC (Índice de K-Competência)
                            composable(KudulaScreen.IKC_DASHBOARD.route) {
                                IKCDashboardScreen(profile = state.profile)
                            }

                            // Hub de Carreiras MICK
                            composable(KudulaScreen.CAREER_HUB.route) {
                                CareerHubScreen(
                                    profile = state.profile,
                                    roles = state.careerRoles,
                                    onExploreRole = { navController.navigate(KudulaScreen.FUTURE_SELF.route) }
                                )
                            }

                            // Projeção Eu do Futuro
                            composable(KudulaScreen.FUTURE_SELF.route) {
                                FutureSelfScreen(
                                    profile = state.profile,
                                    milestones = state.futureSelfMilestones
                                )
                            }

                            // Proof of Learning (Criptograficamente Verificado)
                            composable(KudulaScreen.PROOF_OF_LEARNING.route) {
                                ProofOfLearningScreen(
                                    profile = state.profile,
                                    badges = state.polBadges
                                )
                            }

                            // Comunidade de Aprendizagem
                            composable(KudulaScreen.COMMUNITY.route) {
                                CommunityScreen(
                                    posts = state.communityPosts,
                                    currentUserName = state.profile.name,
                                    currentUserRole = state.profile.levelName,
                                    onReact = { postId, reaction -> viewModel.reactToCommunityPost(postId, reaction) },
                                    onAddComment = { postId, text -> viewModel.addCommentToCommunityPost(postId, text) },
                                    onAddReply = { postId, commentId, text -> viewModel.addReplyToCommunityComment(postId, commentId, text) },
                                    onAddPost = { text, tag -> viewModel.addCommunityPost(text, tag) }
                                )
                            }

                            // Definições do Sistema
                            composable(KudulaScreen.SETTINGS.route) {
                                SettingsScreen(
                                    isMascotVisible = state.isMascotVisible,
                                    onToggleMascot = { viewModel.toggleMascot() },
                                    selectedLanguage = state.language,
                                    onSelectLanguage = { viewModel.setLanguage(it) },
                                    isReduceMotion = state.isReduceMotion,
                                    onToggleReduceMotion = { viewModel.setReduceMotion(it) },
                                    isHighContrast = state.isHighContrast,
                                    onToggleHighContrast = { viewModel.setHighContrast(it) },
                                    voiceConfig = state.profile.voiceConfig,
                                    onVoiceConfigChange = { viewModel.updateReadingVoiceConfig(it) },
                                    onNavigateToProfile = { navController.navigate(KudulaScreen.USER_PROFILE.route) },
                                    onNavigateToPlans = { navController.navigate(KudulaScreen.PLANS.route) }
                                )
                            }

                            // Learning Timeline (KLSE Engine)
                            composable(KudulaScreen.TIMELINE.route) {
                                LearningTimelineScreen(
                                    state = learningSessionState,
                                    onSyncClick = { learningSessionViewModel.syncOfflineData() },
                                    onNewReflectionClick = { navController.navigate(KudulaScreen.REFLECTION_JOURNAL.route) },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // Diário Reflexivo Cognitivo
                            composable(KudulaScreen.REFLECTION_JOURNAL.route) {
                                CognitiveReflectionScreen(
                                    state = learningSessionState,
                                    onSaveReflection = { text, mood, energy, confidence, tags, question ->
                                        learningSessionViewModel.saveReflection(text, mood, energy, confidence, tags, question)
                                        viewModel.recordReflectionAdded(mood)
                                    },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // Planos Kudula (F0.6)
                            composable(KudulaScreen.PLANS.route) {
                                val planManager = remember { com.example.data.plan.PlanManager.getInstance(context) }
                                val currentPlan by planManager.currentPlan.collectAsStateWithLifecycle()
                                PlansScreen(
                                    currentPlan = currentPlan,
                                    onSelectPlan = { newPlan ->
                                        planManager.activatePlan(newPlan)
                                    },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }

                        // Discrete Sync Indicator (Never blocks user)
                        AnimatedVisibility(
                            visible = isSyncing,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = KudulaNavySurface.copy(alpha = 0.95f),
                                border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.6f)),
                                shadowElevation = 6.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(13.dp),
                                        color = KudulaCyanGlow,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = syncStatusMessage ?: "Sincronizando...",
                                        color = KudulaCyanGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // More Modules Bottom Sheet
                if (showMoreSheet) {
                    MoreModulesSheet(
                        onSelectScreen = { screen ->
                            showMoreSheet = false
                            if (screen == KudulaScreen.VOICE_NOTES) {
                                showVoiceNotesSheet = true
                            } else {
                                navController.navigate(screen.route) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        onDismiss = { showMoreSheet = false }
                    )
                }

                // Language Dialog
                if (showLanguageDialog) {
                    AlertDialog(
                        onDismissRequest = { showLanguageDialog = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                        title = { Text("Selecionar Idioma", fontWeight = FontWeight.Bold, color = Color.White) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SupportedLanguage.entries.forEach { lang ->
                                    Button(
                                        onClick = {
                                            viewModel.setLanguage(lang)
                                            showLanguageDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (state.language == lang) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("lang_dialog_${lang.name.lowercase()}")
                                    ) {
                                        Text("${lang.flag}  ${lang.displayName}")
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showLanguageDialog = false }) {
                                Text("Fechar")
                            }
                        }
                    )
                }

                // Mascot Dialog
                if (showMascotDialog) {
                    AlertDialog(
                        onDismissRequest = { showMascotDialog = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(36.dp)) {
                                    MascotOwlAvatar(mood = state.mascotMood, eyeGlow = 1f)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Companheiro Kudu", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = state.mascotMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "«O Kudu reage dinamicamente às tuas vitórias, hesitações de microtelemetria e fadiga para garantir que permaneces no teu Estado de Fluxo (Flow).»",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showMascotDialog = false
                                    navController.navigate(KudulaScreen.AI_TUTOR.route) {
                                        launchSingleTop = true
                                    }
                                }
                            ) {
                                Text("Conversar com o Kudu")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showMascotDialog = false }) {
                                Text("Fechar")
                            }
                        }
                    )
                }

                // Priority 8 — Investor Demo Walkthrough Dialog
                if (state.isInvestorDemoVisible) {
                    InvestorDemoDialog(
                        currentStep = state.investorDemoStep,
                        loadingState = state.aiLoadingState,
                        onStepClick = { viewModel.setInvestorDemoStep(it) },
                        onRunStep = { viewModel.runInvestorDemoStep(it) },
                        onDismiss = { viewModel.closeInvestorDemo() }
                    )
                }

                // Priority 7 — Smart Celebration Feedback Dialog
                state.celebrationTitle?.let { title ->
                    SmartCelebrationDialog(
                        title = title,
                        message = state.celebrationMessage ?: "",
                        onDismiss = { viewModel.dismissCelebration() }
                    )
                }

                // Progressive Invisible Login Dialog (F0.6)
                if (authState.showProgressiveLoginPrompt) {
                    ProgressiveLoginDialog(
                        reason = authState.progressivePromptReason,
                        onContinueWithGoogle = { authViewModel.continueWithGoogle(context) },
                        onContinueWithEmail = { email, pass -> authViewModel.continueWithEmail(email, pass) },
                        onDismiss = { authViewModel.dismissProgressiveLogin() }
                    )
                }

                // Kudu Voice Notes Bottom Sheet (F0.5+)
                if (showVoiceNotesSheet) {
                    VoiceNoteBottomSheet(
                        uiState = voiceNotesState,
                        onStartRecording = { voiceNotesViewModel.startRecording() },
                        onStopRecording = {
                            voiceNotesViewModel.stopAndProcessRecording()
                            syncManager.onUploadCompleted()
                        },
                        onCancelRecording = { voiceNotesViewModel.cancelRecording() },
                        onPlayAudioResponse = { voiceNotesViewModel.playAudioResponse() },
                        onDeleteNote = { voiceNotesViewModel.deleteNote(it) },
                        onDismiss = { showVoiceNotesSheet = false }
                    )
                }
            }
            }
        }
    }
}
