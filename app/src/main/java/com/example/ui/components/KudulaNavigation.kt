package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.model.SupportedLanguage
import com.example.model.UserCognitiveProfile
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo
import com.example.viewmodel.KudulaScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KudulaTopBar(
    profile: UserCognitiveProfile,
    currentScreen: KudulaScreen,
    currentLanguage: SupportedLanguage,
    onLanguageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onMascotClick: () -> Unit,
    isMascotActive: Boolean,
    onInvestorDemoClick: () -> Unit = {},
    onToggleFlowMode: () -> Unit = {},
    isFlowModeHighIpa: Boolean = true,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact

    TopAppBar(
        modifier = modifier.testTag("kudula_top_bar"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onMascotClick() }
            ) {
                KudulaFaviconLogo(
                    size = if (isNarrow) 28.dp else 32.dp,
                    showAuraGlow = false
                )
                Spacer(modifier = Modifier.width(if (isNarrow) 8.dp else 12.dp))
                Text(
                    text = "Kudula",
                    style = if (isNarrow) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        actions = {
            // Streak Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = KudulaNavyContainer,
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = if (isNarrow) 5.dp else 6.dp,
                        vertical = 3.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔥", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${profile.streakDays}d",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = KudulaGold,
                        fontSize = 10.sp
                    )
                }
            }

            // Language Flag Container
            Surface(
                onClick = onLanguageClick,
                shape = CircleShape,
                color = KudulaNavyContainer,
                modifier = Modifier
                    .size(30.dp)
                    .testTag("language_selector_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = currentLanguage.flag, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.width(3.dp))

            // User Profile Avatar Container
            Surface(
                onClick = onProfileClick,
                shape = CircleShape,
                color = KudulaElectricBlue.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.7f)),
                modifier = Modifier
                    .size(30.dp)
                    .testTag("user_profile_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = profile.name.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Surface(
                onClick = onSettingsClick,
                shape = CircleShape,
                color = KudulaNavyContainer,
                modifier = Modifier
                    .size(30.dp)
                    .testTag("settings_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Definições",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = KudulaNavyDark.copy(alpha = 0.96f)
        )
    )
}

@Composable
fun KudulaBottomNavigationBar(
    currentScreen: KudulaScreen,
    onSelectScreen: (KudulaScreen) -> Unit,
    onOpenMoreMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact
    val labelFontSize = if (isNarrow) 9.sp else 11.sp

    NavigationBar(
        modifier = modifier.testTag("kudula_bottom_navigation"),
        containerColor = KudulaNavyDark.copy(alpha = 0.98f),
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            Triple(KudulaScreen.HOME, "Início", Icons.Filled.Home to Icons.Outlined.Home),
            Triple(KudulaScreen.LIBRARY, "Biblioteca", Icons.Filled.LocalLibrary to Icons.Outlined.LocalLibrary),
            Triple(KudulaScreen.AI_TUTOR, "Tutor IA", Icons.Filled.AutoAwesome to Icons.Outlined.AutoAwesome),
            Triple(KudulaScreen.IKC_DASHBOARD, "IKC", Icons.Filled.Analytics to Icons.Outlined.Analytics),
            Triple(KudulaScreen.CAREER_HUB, "Carreiras", Icons.Filled.Work to Icons.Outlined.WorkOutline)
        )

        navItems.forEach { (screen, label, icons) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(screen) },
                alwaysShowLabel = !isNarrow || isSelected,
                icon = {
                    Icon(
                        imageVector = if (isSelected) icons.first else icons.second,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = labelFontSize,
                        maxLines = 1,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = navigationItemColors()
            )
        }

        val isMoreActive = isMoreScreen(currentScreen)
        NavigationBarItem(
            selected = isMoreActive,
            onClick = onOpenMoreMenu,
            alwaysShowLabel = !isNarrow || isMoreActive,
            icon = {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "Módulos",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    text = "Módulos",
                    fontSize = labelFontSize,
                    maxLines = 1,
                    fontWeight = if (isMoreActive) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navigationItemColors()
        )
    }
}

private fun isMoreScreen(screen: KudulaScreen): Boolean {
    return screen in listOf(
        KudulaScreen.TIMELINE,
        KudulaScreen.REFLECTION_JOURNAL,
        KudulaScreen.YOUTUBE_INTELLIGENCE,
        KudulaScreen.READING_WORKSPACE,
        KudulaScreen.SMART_REVISION,
        KudulaScreen.FUTURE_SELF,
        KudulaScreen.PROOF_OF_LEARNING,
        KudulaScreen.COMMUNITY,
        KudulaScreen.SETTINGS
    )
}

@Composable
private fun navigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = KudulaCyanGlow,
    selectedTextColor = KudulaCyanGlow,
    indicatorColor = KudulaElectricBlue.copy(alpha = 0.25f),
    unselectedIconColor = TextTertiary,
    unselectedTextColor = TextTertiary
)
