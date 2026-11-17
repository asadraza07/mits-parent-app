package com.mitsschools.parent.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class ThemeMode(val label: String) { SYSTEM("System"), LIGHT("Light"), DARK("Dark") }

/** Colours sampled from the official MITS Schools logo. */
object Brand {
    val Red = Color(0xFFA61F26)
    val RedDeep = Color(0xFF7E151B)
    val Charcoal = Color(0xFF2C3B42)
    val CharcoalDeep = Color(0xFF1F2A30)
    val Gold = Color(0xFFE3A841)
    val GoldLight = Color(0xFFF1B964)
    val Cream = Color(0xFFFFF4DF)
}

/** Feature tints, carried over from the flowchart's dashboard tiles. */
@Immutable
data class FeatureColors(
    val calendar: Color, val calendarBg: Color,
    val weekly: Color, val weeklyBg: Color,
    val daily: Color, val dailyBg: Color,
    val noticeBg: Color, val noticeBorder: Color,
    val holiday: Color, val exam: Color, val event: Color, val meeting: Color,
    val success: Color,
)

private val LightFeatures = FeatureColors(
    calendar = Color(0xFF2F6CB3), calendarBg = Color(0xFFE8F0FA),
    weekly = Color(0xFF2E7D4F), weeklyBg = Color(0xFFE6F3EA),
    daily = Color(0xFFA94438), dailyBg = Color(0xFFFBECEA),
    noticeBg = Color(0xFFFFF8EC), noticeBorder = Color(0xFFE3B66A),
    holiday = Color(0xFF2E7D4F), exam = Color(0xFFA61F26), event = Color(0xFF2F6CB3), meeting = Color(0xFFB7791F),
    success = Color(0xFF2E7D4F),
)

private val DarkFeatures = FeatureColors(
    calendar = Color(0xFF9EC3F0), calendarBg = Color(0xFF1E2B3A),
    weekly = Color(0xFF8FD3A7), weeklyBg = Color(0xFF1C2C23),
    daily = Color(0xFFF2A79C), dailyBg = Color(0xFF34211F),
    noticeBg = Color(0xFF2E2719), noticeBorder = Color(0xFF8C6A2C),
    holiday = Color(0xFF8FD3A7), exam = Color(0xFFFF8A80), event = Color(0xFF9EC3F0), meeting = Color(0xFFF1B964),
    success = Color(0xFF8FD3A7),
)

val LocalFeatureColors = staticCompositionLocalOf { LightFeatures }
val LocalIsDark = staticCompositionLocalOf { false }

private val LightColors = lightColorScheme(
    primary = Brand.Red,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410004),
    secondary = Brand.Charcoal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE6EB),
    onSecondaryContainer = Color(0xFF13232A),
    tertiary = Color(0xFF855A00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE3B0),
    onTertiaryContainer = Color(0xFF2A1800),
    background = Color(0xFFF6F6F3),
    onBackground = Color(0xFF1A1C1D),
    surface = Color(0xFFF6F6F3),
    onSurface = Color(0xFF1A1C1D),
    surfaceVariant = Color(0xFFE3E6E7),
    onSurfaceVariant = Color(0xFF50595D),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF1F1EE),
    surfaceContainer = Color(0xFFECECE9),
    surfaceContainerHigh = Color(0xFFE6E6E3),
    surfaceContainerHighest = Color(0xFFE0E0DD),
    outline = Color(0xFF7A8387),
    outlineVariant = Color(0xFFD5D9DB),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB3AC),
    onPrimary = Color(0xFF680008),
    primaryContainer = Color(0xFF8C1A20),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFBFCBD1),
    onSecondary = Color(0xFF1F2A30),
    secondaryContainer = Color(0xFF34444C),
    onSecondaryContainer = Color(0xFFDCE6EB),
    tertiary = Brand.GoldLight,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF5E4100),
    onTertiaryContainer = Color(0xFFFFE3B0),
    background = Color(0xFF12181B),
    onBackground = Color(0xFFE2E6E8),
    surface = Color(0xFF12181B),
    onSurface = Color(0xFFE2E6E8),
    surfaceVariant = Color(0xFF3F484C),
    onSurfaceVariant = Color(0xFFBFC8CC),
    surfaceContainerLowest = Color(0xFF1A2226),
    surfaceContainerLow = Color(0xFF1E272B),
    surfaceContainer = Color(0xFF232C30),
    surfaceContainerHigh = Color(0xFF2B3438),
    surfaceContainerHighest = Color(0xFF353E42),
    outline = Color(0xFF899296),
    outlineVariant = Color(0xFF3B4448),
)

private val MitsTypography = Typography().run {
    copy(
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val MitsShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun MitsTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(
        LocalFeatureColors provides if (dark) DarkFeatures else LightFeatures,
        LocalIsDark provides dark,
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = MitsTypography,
            shapes = MitsShapes,
            content = content,
        )
    }
}

/** Charcoal used for headers; slightly deeper in dark mode. */
@Composable
fun headerColor(): Color = if (LocalIsDark.current) Brand.CharcoalDeep else Brand.Charcoal
