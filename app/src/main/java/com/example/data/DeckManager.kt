package com.example.data

import androidx.compose.runtime.getValue
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
    var noReversals by mutableStateOf(false) // When true, drawings only yield upright cards

    // Custom back art URLs (with tasteful mystical defaults)
    var tarotBackArtUrl by mutableStateOf("https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Rider_Waite_Tarot_Deck_Back.jpg/360px-Rider_Waite_Tarot_Deck_Back.jpg")
    var runeBackArtUrl by mutableStateOf("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Vegvisir.svg/512px-Vegvisir.svg.png")
    var iChingBackArtUrl by mutableStateOf("https://upload.wikimedia.org/wikipedia/commons/thumb/1/17/Yin_yang.svg/512px-Yin_yang.svg.png")

    // Yes/No Coin custom art (with mystical sun/moon defaults)
    var coinHeadsArtUrl by mutableStateOf("https://upload.wikimedia.org/wikipedia/commons/thumb/c/c2/Sun_symbol_%28bold%29.svg/512px-Sun_symbol_%28bold%29.svg.png")
    var coinTailsArtUrl by mutableStateOf("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e1/Full_Moon_Luc_Viatour.jpg/512px-Full_Moon_Luc_Viatour.jpg")

    // Shake & Haptics settings
    var shakeSensitivity by mutableStateOf("Medium") // Low, Medium, High
    var hapticsEnabled by mutableStateOf(true)

    // Google Drive Sync state
    var isGoogleDriveConnected by mutableStateOf(false)
    var googleAccountEmail by mutableStateOf("")
    var lastSyncTimestamp by mutableStateOf<Long?>(null)
    var syncStatusMessage by mutableStateOf("Not synced")
}
