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
