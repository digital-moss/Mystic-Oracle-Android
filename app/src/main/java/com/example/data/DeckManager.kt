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
    val sampleImageUrl: String,
    val source: String = "Built-in", // "www.alabe.com/tarot", "GitHub", "Built-in", "Custom"
    val author: String = "Traditional",
    val websiteUrl: String? = null,
    val repoUrl: String? = null,
    val cardCount: Int = 78,
    val isCustom: Boolean = false
)

object DeckManager {
    val builtInDecks = listOf(
        TarotDeckPreset(
            id = "rider_waite",
            name = "Rider-Waite-Smith 1909 Classic",
            description = "The timeless traditional esoteric masterpiece illustrated by Pamela Colman Smith.",
            sampleImageUrl = "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg",
            source = "Built-in",
            author = "Pamela Colman Smith & A.E. Waite"
        ),
        TarotDeckPreset(
            id = "alabe_albano",
            name = "Albano-Waite Vibrant Tarot (1968)",
            description = "Frankie Albano's vivid psychotropic colorization with astrological decanate correspondences as featured on alabe.com.",
            sampleImageUrl = "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg",
            source = "www.alabe.com/tarot",
            author = "Frankie Albano",
            websiteUrl = "https://www.alabe.com/tarot"
        ),
        TarotDeckPreset(
            id = "alabe_astrolabe",
            name = "Astrolabe Golden Hermetic Tarot",
            description = "Astrolabe's sacred astrological synthesis pairing all 78 tarot keys with natal chart zodiac signs, planetary houses, and decans.",
            sampleImageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/de/Rider-Waite-Smith_Tarot_01_Magician.jpg",
            source = "www.alabe.com/tarot",
            author = "Astrolabe Esoteric Archives",
            websiteUrl = "https://www.alabe.com/tarot"
        ),
        TarotDeckPreset(
            id = "alabe_alchemical",
            name = "Astrolabe Alchemical Renaissance Tarot",
            description = "Astrolabe's historic Renaissance astromancy deck with classical Latin woodcuts and cosmological house significators.",
            sampleImageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Tarots_de_Marseille.jpg",
            source = "www.alabe.com/tarot",
            author = "Astrolabe Astromancy",
            websiteUrl = "https://www.alabe.com/tarot"
        ),
        TarotDeckPreset(
            id = "alabe_decanates",
            name = "Astrolabe 36 Decanates Wheel Tarot",
            description = "Esoteric astrological system mapping the 36 Minor Arcana decanates across the 12 zodiac constellations.",
            sampleImageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/de/Rider-Waite-Smith_Tarot_01_Magician.jpg",
            source = "www.alabe.com/tarot",
            author = "Astrolabe Divination",
            websiteUrl = "https://www.alabe.com/tarot"
        ),
        TarotDeckPreset(
            id = "github_metabismuth",
            name = "metabismuth/tarot-json (GitHub)",
            description = "High-resolution scanned Rider-Waite-Smith cards with full JSON open metadata hosted on GitHub.",
            sampleImageUrl = "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg",
            source = "GitHub",
            author = "metabismuth",
            repoUrl = "https://github.com/metabismuth/tarot-json"
        ),
        TarotDeckPreset(
            id = "github_mixvlad",
            name = "mixvlad/TarotCards Archive (GitHub)",
            description = "Verified open-source archive of historic public-domain decks including Marseille, Sola Busca, and 1909 RWS.",
            sampleImageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Tarots_de_Marseille.jpg",
            source = "GitHub",
            author = "mixvlad",
            repoUrl = "https://github.com/mixvlad/TarotCards"
        ),
        TarotDeckPreset(
            id = "github_krates",
            name = "krates98/tarotcardapi (GitHub)",
            description = "Modern 78-card REST API asset repository with high-contrast card art and fortune-telling interpretations.",
            sampleImageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/de/Rider-Waite-Smith_Tarot_01_Magician.jpg",
            source = "GitHub",
            author = "krates98",
            repoUrl = "https://github.com/krates98/tarotcardapi"
        ),
        TarotDeckPreset(
            id = "github_luciellaes",
            name = "luciellaes/rws-tarot-cc0 (GitHub)",
            description = "Pure CC0 public-domain 300x527 scans of the complete 78-card Rider-Waite-Smith deck.",
            sampleImageUrl = "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg",
            source = "GitHub",
            author = "luciellaes",
            repoUrl = "https://github.com/luciellaes/rws-tarot-cc0"
        ),
        TarotDeckPreset(
            id = "marseille",
            name = "Tarot de Marseille (1760)",
            description = "Historic Nicolas Conver woodblock archetype featuring bold primary colors and medieval imagery.",
            sampleImageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Tarots_de_Marseille.jpg",
            source = "Built-in",
            author = "Nicolas Conver"
        ),
        TarotDeckPreset(
            id = "sola_busca",
            name = "Sola Busca Renaissance (1491)",
            description = "The world's earliest complete 78-card deck featuring Italian Renaissance alchemical engravings.",
            sampleImageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Sola_Busca_tarot_card_05.jpg",
            source = "Built-in",
            author = "Mattia Serrati"
        ),
        TarotDeckPreset(
            id = "mystic_gold",
            name = "Mystic Golden Dawn",
            description = "Ornate golden borders, sacred geometry, and hermetic astrological decan correspondences.",
            sampleImageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/de/Rider-Waite-Smith_Tarot_01_Magician.jpg",
            source = "Built-in",
            author = "Hermetic Order of the Golden Dawn"
        ),
        TarotDeckPreset(
            id = "visconti_sforza",
            name = "Visconti-Sforza Pierpont Morgan (1451)",
            description = "The oldest surviving luxury gold-leaf hand-painted tarot cards from Renaissance Milan.",
            sampleImageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Tarots_de_Marseille.jpg",
            source = "Built-in",
            author = "Bonifacio Bembo"
        )
    )

    var customDecks by mutableStateOf<List<TarotDeckPreset>>(emptyList())

    val availableDecks: List<TarotDeckPreset>
        get() = builtInDecks + customDecks

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

        // Initialize Card & Database Alignment Manager and Card Art Repository
        CardAlignmentManager.init(context)
        com.example.network.TarotImageRepository.init(context)

        // Load custom imported decks
        loadCustomDecks(context)
    }

    fun selectDeck(deckId: String, context: Context? = null) {
        currentDeckId = deckId
        context?.let { ctx ->
            savePreferences(ctx)
            if (hapticsEnabled) {
                com.example.util.HapticUtil.performHaptic(ctx)
            }
        }
    }

    fun searchDecks(query: String, filterSource: String = "All"): List<TarotDeckPreset> {
        val q = query.trim().lowercase()
        return availableDecks.filter { deck ->
            val matchesSource = when (filterSource.lowercase()) {
                "all", "all decks" -> true
                "alabe.com/tarot", "alabe", "alabe.com" -> deck.source.contains("alabe", true) || deck.websiteUrl?.contains("alabe", true) == true
                "github", "github repos" -> deck.source.contains("github", true) || deck.repoUrl?.contains("github", true) == true
                "built-in", "historic", "historical", "classic" -> deck.source.equals("built-in", true)
                "custom", "custom imported", "my custom decks" -> deck.isCustom
                else -> true
            }
            val matchesQuery = q.isEmpty() ||
                deck.name.lowercase().contains(q) ||
                deck.description.lowercase().contains(q) ||
                deck.source.lowercase().contains(q) ||
                deck.author.lowercase().contains(q) ||
                (deck.repoUrl?.lowercase()?.contains(q) == true) ||
                (deck.websiteUrl?.lowercase()?.contains(q) == true)
            matchesSource && matchesQuery
        }
    }

    fun importCustomDeck(
        name: String,
        description: String,
        source: String,
        repoUrl: String? = null,
        websiteUrl: String? = null,
        sampleImageUrl: String? = null,
        context: Context
    ): TarotDeckPreset {
        val newId = "custom_${System.currentTimeMillis()}"
        val preset = TarotDeckPreset(
            id = newId,
            name = name,
            description = description,
            sampleImageUrl = sampleImageUrl ?: "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg",
            source = source,
            author = if (source.contains("github", true)) "GitHub Community" else "Custom Deck",
            websiteUrl = websiteUrl,
            repoUrl = repoUrl,
            isCustom = true
        )
        customDecks = customDecks + preset
        selectDeck(newId, context)
        saveCustomDecks(context)
        return preset
    }

    fun deleteCustomDeck(id: String, context: Context) {
        customDecks = customDecks.filter { it.id != id }
        if (currentDeckId == id) {
            currentDeckId = "rider_waite"
        }
        savePreferences(context)
        saveCustomDecks(context)
    }

    private fun saveCustomDecks(context: Context) {
        try {
            val array = org.json.JSONArray()
            for (deck in customDecks) {
                val obj = org.json.JSONObject().apply {
                    put("id", deck.id)
                    put("name", deck.name)
                    put("description", deck.description)
                    put("sampleImageUrl", deck.sampleImageUrl)
                    put("source", deck.source)
                    put("author", deck.author)
                    put("websiteUrl", deck.websiteUrl ?: "")
                    put("repoUrl", deck.repoUrl ?: "")
                    put("cardCount", deck.cardCount)
                    put("isCustom", true)
                }
                array.put(obj)
            }
            val prefs = context.getSharedPreferences("mystic_oracle_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("custom_decks_json", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadCustomDecks(context: Context) {
        try {
            val prefs = context.getSharedPreferences("mystic_oracle_prefs", Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("custom_decks_json", null) ?: return
            val array = org.json.JSONArray(jsonStr)
            val list = mutableListOf<TarotDeckPreset>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TarotDeckPreset(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        description = obj.optString("description", ""),
                        sampleImageUrl = obj.optString("sampleImageUrl", "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg"),
                        source = obj.optString("source", "Custom"),
                        author = obj.optString("author", "Custom"),
                        websiteUrl = obj.optString("websiteUrl", "").ifEmpty { null },
                        repoUrl = obj.optString("repoUrl", "").ifEmpty { null },
                        cardCount = obj.optInt("cardCount", 78),
                        isCustom = true
                    )
                )
            }
            customDecks = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
