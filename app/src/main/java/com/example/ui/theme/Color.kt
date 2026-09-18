package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val MysticGold = Color(0xFFE5C158)
val MysticGoldDark = Color(0xFFC4A036)
val DeepIndigo = Color(0xFF120C1F)
val RichPurple = Color(0xFF2C1E4A)
val SurfacePurple = Color(0xFF1E1433)
val LavenderText = Color(0xFFE2D9F3)
val MutedLavender = Color(0xFFB3A6CC)

val DarkColorScheme = darkColorScheme(
    primary = MysticGold,
    onPrimary = Color(0xFF1A1200),
    primaryContainer = RichPurple,
    onPrimaryContainer = MysticGold,
    secondary = MutedLavender,
    onSecondary = DeepIndigo,
    background = DeepIndigo,
    onBackground = LavenderText,
    surface = SurfacePurple,
    onSurface = LavenderText,
    surfaceVariant = RichPurple,
    onSurfaceVariant = MutedLavender
)

val EmeraldColorScheme = darkColorScheme(
    primary = Color(0xFF50C878),
    onPrimary = Color(0xFF0A2614),
    primaryContainer = Color(0xFF0F3820),
    onPrimaryContainer = Color(0xFF86E8A8),
    secondary = Color(0xFF8FA89B),
    onSecondary = Color(0xFF0A1F14),
    background = Color(0xFF0A1810),
    onBackground = Color(0xFFE2F3EC),
    surface = Color(0xFF132B1C),
    onSurface = Color(0xFFE2F3EC)
)

val VelvetColorScheme = darkColorScheme(
    primary = Color(0xFFFF6B6B),
    onPrimary = Color(0xFF380808),
    primaryContainer = Color(0xFF4A1515),
    onPrimaryContainer = Color(0xFFFFB2B2),
    secondary = Color(0xFFD4A5A5),
    onSecondary = Color(0xFF2E0F0F),
    background = Color(0xFF1A0A0A),
    onBackground = Color(0xFFF9EBEB),
    surface = Color(0xFF2B1212),
    onSurface = Color(0xFFF9EBEB)
)

val SolarColorScheme = darkColorScheme(
    primary = Color(0xFFFFB300),
    onPrimary = Color(0xFF332500),
    primaryContainer = Color(0xFF4D3800),
    onPrimaryContainer = Color(0xFFFFE082),
    secondary = Color(0xFFFFD54F),
    onSecondary = Color(0xFF332D00),
    background = Color(0xFF1F1A0A),
    onBackground = Color(0xFFFFFDE7),
    surface = Color(0xFF332B0D),
    onSurface = Color(0xFFFFFDE7)
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5A3E85),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADFFD),
    onPrimaryContainer = Color(0xFF20133F),
    secondary = Color(0xFF6B5780),
    onSecondary = Color.White,
    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFF3EDF7),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F)
)
