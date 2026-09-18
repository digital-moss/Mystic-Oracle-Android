package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.data.DeckManager

@Composable
fun MyApplicationTheme(
    themeName: String = "Mystic Purple",
    fontName: String = DeckManager.selectedFontName,
    fontSizeScale: Float = DeckManager.fontSizeScale,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val colorScheme = when (themeName) {
        "Emerald Forest" -> EmeraldColorScheme
        "Midnight Velvet" -> VelvetColorScheme
        "Solar Gold" -> SolarColorScheme
        "Celestial Light" -> LightColorScheme
        "Obsidian Void" -> ObsidianColorScheme
        "Amethyst Rose" -> AmethystRoseColorScheme
        "Ocean Mystic" -> OceanMysticColorScheme
        else -> DarkColorScheme // "Mystic Purple"
    }

    val dynamicTypography = remember(fontName, fontSizeScale, DeckManager.customFontPath) {
        val fontFamily = FontHelper.getActiveFontFamily(context)
        FontHelper.createTypography(fontFamily, fontSizeScale)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography,
        content = content
    )
}
