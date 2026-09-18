package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class TarotDeckPreset(
    val id: String,
    val name: String,
    val description: String,
    val sampleImageUrl: String
)

object DeckManager {
    val availableDecks = listOf(
        TarotDeckPreset(
            id = "rider_waite",
            name = "Rider-Waite-Smith 1909 Classic",
            description = "The timeless traditional esoteric masterpiece illustrated by Pamela Colman Smith.",
            sampleImageUrl = "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg"
        ),
        TarotDeckPreset(
            id = "marseille",
            name = "Tarot de Marseille (1760)",
            description = "Historic Nicolas Conver woodblock archetype featuring bold primary colors and medieval imagery.",
            sampleImageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Tarots_de_Marseille.jpg"
        ),
        TarotDeckPreset(
            id = "sola_busca",
            name = "Sola Busca Renaissance (1491)",
            description = "The world's earliest complete 78-card deck featuring Italian Renaissance alchemical engravings.",
            sampleImageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Sola_Busca_tarot_card_05.jpg"
        ),
        TarotDeckPreset(
            id = "mystic_gold",
            name = "Mystic Golden Dawn",
            description = "Ornate golden borders, sacred geometry, and hermetic astrological decan correspondences.",
            sampleImageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/de/Rider-Waite-Smith_Tarot_01_Magician.jpg"
        )
    )

    var currentDeckId by mutableStateOf("rider_waite")

    // Reversal settings
    var noReversals by mutableStateOf(false)

    // Custom back art URLs
    var tarotBackArtUrl by mutableStateOf("https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Rider_Waite_Tarot_Deck_Back.jpg/360px-Rider_Waite_Tarot_Deck_Back.jpg")
    var runeBackArtUrl by mutableStateOf("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Vegvisir.svg/512px-Vegvisir.svg.png")
    var iChingBackArtUrl by mutableStateOf("https://upload.wikimedia.org/wikipedia/commons/thumb/1/17/Yin_yang.svg/512px-Yin_yang.svg.png")

    // Yes/No Coin custom art
    var coinHeadsArtUrl by mutableStateOf("https://upload.wikimedia.org/wikipedia/commons/thumb/c/c2/Sun_symbol_%28bold%29.svg/512px-Sun_symbol_%28bold%29.svg.png")
    var coinTailsArtUrl by mutableStateOf("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e1/Full_Moon_Luc_Viatour.jpg/512px-Full_Moon_Luc_Viatour.jpg")

    // Shake & Haptics settings
    var shakeToShuffleEnabled by mutableStateOf(true)
    var shakeSensitivity by mutableStateOf("Medium") // Low, Medium, High
    var hapticsEnabled by mutableStateOf(true)

    // UI & Typography Customization
    var selectedFontName by mutableStateOf("Cinzel Mystic")
    var customFontPath by mutableStateOf<String?>(null)
    var fontSizeScale by mutableFloatStateOf(1.0f)
    var cardStyle by mutableStateOf("Classic Rounded") // "Classic Rounded", "Gothic Ornate", "Gold Minimalist", "Sacred Glow"
    var ambientGlowEnabled by mutableStateOf(true)

    // Google Login & Account State
    var isLoggedIn by mutableStateOf(false)
    var userDisplayName by mutableStateOf("Mystic Seeker")
    var googleAccountEmail by mutableStateOf("")
    var userPhotoUrl by mutableStateOf<String?>(null)
    var isGoogleDriveConnected by mutableStateOf(false)
    var lastSyncTimestamp by mutableStateOf<Long?>(null)
    var syncStatusMessage by mutableStateOf("Ready to sync")

    fun initPreferences(context: Context) {
        val prefs = context.getSharedPreferences("mystic_oracle_prefs", Context.MODE_PRIVATE)
        currentDeckId = prefs.getString("currentDeckId", "rider_waite") ?: "rider_waite"
        noReversals = prefs.getBoolean("noReversals", false)
        shakeToShuffleEnabled = prefs.getBoolean("shakeToShuffleEnabled", true)
        shakeSensitivity = prefs.getString("shakeSensitivity", "Medium") ?: "Medium"
        hapticsEnabled = prefs.getBoolean("hapticsEnabled", true)
        selectedFontName = prefs.getString("selectedFontName", "Cinzel Mystic") ?: "Cinzel Mystic"
        customFontPath = prefs.getString("customFontPath", null)
        fontSizeScale = prefs.getFloat("fontSizeScale", 1.0f)
        cardStyle = prefs.getString("cardStyle", "Classic Rounded") ?: "Classic Rounded"
        ambientGlowEnabled = prefs.getBoolean("ambientGlowEnabled", true)

        isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        userDisplayName = prefs.getString("userDisplayName", "Mystic Seeker") ?: "Mystic Seeker"
        googleAccountEmail = prefs.getString("googleAccountEmail", "") ?: ""
        userPhotoUrl = prefs.getString("userPhotoUrl", null)
        isGoogleDriveConnected = prefs.getBoolean("isGoogleDriveConnected", isLoggedIn)
        val lastSync = prefs.getLong("lastSyncTimestamp", 0L)
        if (lastSync > 0L) lastSyncTimestamp = lastSync
        syncStatusMessage = prefs.getString("syncStatusMessage", if (isLoggedIn) "Cloud synced" else "Ready to sync") ?: "Ready to sync"
    }

    fun savePreferences(context: Context) {
        val prefs = context.getSharedPreferences("mystic_oracle_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("currentDeckId", currentDeckId)
            putBoolean("noReversals", noReversals)
            putBoolean("shakeToShuffleEnabled", shakeToShuffleEnabled)
            putString("shakeSensitivity", shakeSensitivity)
            putBoolean("hapticsEnabled", hapticsEnabled)
            putString("selectedFontName", selectedFontName)
            putString("customFontPath", customFontPath)
            putFloat("fontSizeScale", fontSizeScale)
            putString("cardStyle", cardStyle)
            putBoolean("ambientGlowEnabled", ambientGlowEnabled)

            putBoolean("isLoggedIn", isLoggedIn)
            putString("userDisplayName", userDisplayName)
            putString("googleAccountEmail", googleAccountEmail)
            putString("userPhotoUrl", userPhotoUrl)
            putBoolean("isGoogleDriveConnected", isGoogleDriveConnected)
            putLong("lastSyncTimestamp", lastSyncTimestamp ?: 0L)
            putString("syncStatusMessage", syncStatusMessage)
            apply()
        }
    }

    fun loginWithGoogle(context: Context, email: String, name: String? = null, photoUrl: String? = null) {
        isLoggedIn = true
        googleAccountEmail = email
        userDisplayName = name ?: email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
        userPhotoUrl = photoUrl
        isGoogleDriveConnected = true
        lastSyncTimestamp = System.currentTimeMillis()
        syncStatusMessage = "Connected to Google Drive"
        savePreferences(context)
    }

    fun logoutGoogle(context: Context) {
        isLoggedIn = false
        googleAccountEmail = ""
        userDisplayName = "Mystic Seeker"
        userPhotoUrl = null
        isGoogleDriveConnected = false
        syncStatusMessage = "Signed out"
        savePreferences(context)
    }
}
