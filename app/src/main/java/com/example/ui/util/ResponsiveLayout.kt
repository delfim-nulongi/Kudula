package com.example.ui.util

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard WindowSizeClass representation for Kudula OS.
 * Supports Compact (smartphones, e.g. Nubia Z2350 @ 360-393dp),
 * Medium (foldables, small tablets @ 600-839dp),
 * and Expanded (large tablets, desktop @ >= 840dp).
 */
enum class KudulaWindowSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

/**
 * Responsive screen dimensions and layout metric helpers.
 */
@Immutable
data class ScreenWindowInfo(
    val widthDp: Dp,
    val heightDp: Dp,
    val sizeClass: KudulaWindowSizeClass,
    val isNarrowCompact: Boolean,   // <= 370dp (e.g. Nubia Z2350 baseline test device @ 360dp)
    val isStandardCompact: Boolean, // 371dp .. 420dp (e.g. Pixel 7/8/9 @ 393-412dp)
    val isCompact: Boolean,         // < 600dp
    val isMedium: Boolean,          // 600dp .. 839dp
    val isExpanded: Boolean         // >= 840dp
) {
    /**
     * Adaptive horizontal content padding to maximize usable reading and tap space
     * on compact screens (e.g. 12dp on 360dp devices instead of wasting 40dp with 20dp padding).
     */
    val screenHorizontalPadding: Dp
        get() = when {
            isNarrowCompact -> 12.dp
            isStandardCompact -> 16.dp
            isMedium -> 24.dp
            else -> 32.dp
        }

    /**
     * Adaptive card interior padding.
     */
    val cardContentPadding: Dp
        get() = when {
            isNarrowCompact -> 12.dp
            isStandardCompact -> 16.dp
            else -> 20.dp
        }

    /**
     * Spacing between interactive chips and quick actions.
     */
    val itemSpacing: Dp
        get() = when {
            isNarrowCompact -> 6.dp
            isStandardCompact -> 8.dp
            else -> 12.dp
        }
}

val LocalScreenWindowInfo = staticCompositionLocalOf<ScreenWindowInfo> {
    ScreenWindowInfo(
        widthDp = 360.dp,
        heightDp = 800.dp,
        sizeClass = KudulaWindowSizeClass.COMPACT,
        isNarrowCompact = true,
        isStandardCompact = false,
        isCompact = true,
        isMedium = false,
        isExpanded = false
    )
}

@Composable
fun rememberScreenWindowInfo(): ScreenWindowInfo {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    return remember(screenWidth, screenHeight) {
        val sizeClass = when {
            screenWidth < 600.dp -> KudulaWindowSizeClass.COMPACT
            screenWidth < 840.dp -> KudulaWindowSizeClass.MEDIUM
            else -> KudulaWindowSizeClass.EXPANDED
        }

        val isNarrow = screenWidth <= 370.dp
        val isStdCompact = screenWidth > 370.dp && screenWidth < 600.dp

        ScreenWindowInfo(
            widthDp = screenWidth,
            heightDp = screenHeight,
            sizeClass = sizeClass,
            isNarrowCompact = isNarrow,
            isStandardCompact = isStdCompact,
            isCompact = sizeClass == KudulaWindowSizeClass.COMPACT,
            isMedium = sizeClass == KudulaWindowSizeClass.MEDIUM,
            isExpanded = sizeClass == KudulaWindowSizeClass.EXPANDED
        )
    }
}
