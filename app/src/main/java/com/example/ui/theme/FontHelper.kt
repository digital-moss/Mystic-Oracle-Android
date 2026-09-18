package com.example.ui.theme

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DeckManager
import java.io.File

object FontHelper {

    val CinzelFont = FontFamily(Font(R.font.cinzel, FontWeight.Normal))
    val PlayfairFont = FontFamily(Font(R.font.playfair_display, FontWeight.Normal))
    val CormorantFont = FontFamily(Font(R.font.cormorant_garamond, FontWeight.Normal))

    fun getActiveFontFamily(context: Context): FontFamily {
        return try {
            when (DeckManager.selectedFontName) {
                "Playfair Royal" -> PlayfairFont
                "Cormorant Antiqua" -> CormorantFont
                "Default Sans" -> FontFamily.Default
                "Serif Standard" -> FontFamily.Serif
                "Monospace Classic" -> FontFamily.Monospace
                "Custom Font" -> {
                    val customPath = DeckManager.customFontPath
                    if (customPath != null) {
                        val file = File(customPath)
                        if (file.exists() && file.length() > 0) {
                            FontFamily(Font(file, FontWeight.Normal))
                        } else {
                            CinzelFont
                        }
                    } else {
                        CinzelFont
                    }
                }
                else -> CinzelFont // Default "Cinzel Mystic"
            }
        } catch (_: Throwable) {
            CinzelFont
        }
    }

    fun createTypography(fontFamily: FontFamily, scale: Float = 1.0f): Typography {
        val s = scale.coerceIn(0.85f, 1.35f)
        return Typography(
            displayLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (57 * s).sp,
                lineHeight = (64 * s).sp
            ),
            displayMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (45 * s).sp,
                lineHeight = (52 * s).sp
            ),
            displaySmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (36 * s).sp,
                lineHeight = (44 * s).sp
            ),
            headlineLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = (32 * s).sp,
                lineHeight = (40 * s).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = (28 * s).sp,
                lineHeight = (36 * s).sp
            ),
            headlineSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = (24 * s).sp,
                lineHeight = (32 * s).sp
            ),
            titleLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (22 * s).sp,
                lineHeight = (28 * s).sp
            ),
            titleMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (16 * s).sp,
                lineHeight = (24 * s).sp
            ),
            titleSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (14 * s).sp,
                lineHeight = (20 * s).sp
            ),
            bodyLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = (16 * s).sp,
                lineHeight = (24 * s).sp,
                letterSpacing = 0.5.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = (14 * s).sp,
                lineHeight = (20 * s).sp
            ),
            bodySmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = (12 * s).sp,
                lineHeight = (16 * s).sp
            ),
            labelLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (14 * s).sp,
                lineHeight = (20 * s).sp
            ),
            labelMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (12 * s).sp,
                lineHeight = (16 * s).sp
            ),
            labelSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (11 * s).sp,
                lineHeight = (16 * s).sp
            )
        )
    }
}
