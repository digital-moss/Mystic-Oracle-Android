package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.tarot.BundledTarotDecks

data class TarotDeckPreset(
    val id: String,
    val name: String,
    val description: String,
    val sampleImageUrl: String,
    val source: String = "Built-in", // "GitHub", "Built-in", "Custom"
    val author: String = "Traditional",
    val websiteUrl: String? = null,
    val repoUrl: String? = null,
    val cardCount: Int = 78,
    val isCustom: Boolean = false
)

object DeckManager {
    var builtInDecks by mutableStateOf(listOf(
        TarotDeckPreset(
            id = "rider_waite_tarot",
            name = "Rider-Waite-Smith",
            description = "The verified 78-card Rider-Waite-Smith deck bundled with the app.",
            sampleImageUrl = "file:///android_asset/rider_waite_tarot/major_00_Fool.jpg",
            source = "Bundled asset",
            author = "Pamela Colman Smith & A.E. Waite"
        )
    ))

    var customDecks by mutableStateOf<List<TarotDeckPreset>>(emptyList())

    val availableDecks: List<TarotDeckPreset>
        get() = builtInDecks + customDecks

    var currentDeckId by mutableStateOf("rider_waite_tarot")

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
        val discoveredDecks = BundledTarotDecks.discover(context).map { deck ->
            TarotDeckPreset(
                id = deck.id,
                name = deck.id.removeSuffix("_tarot").split('_').joinToString(" ") { word ->
                    word.replaceFirstChar { it.uppercase() }
                },
                description = "${deck.imageCount}-image deck bundled with the app.",
                sampleImageUrl = "file:///android_asset/${deck.assetFolder}/${firstAssetImage(context, deck.assetFolder)}",
                source = "Bundled asset",
                cardCount = deck.imageCount.coerceAtMost(78)
            )
        }
        builtInDecks = discoveredDecks + TarotDeckPreset(
            id = "hermetic_tarot",
            name = "Hermetic Tarot",
            description = "The Hermetic Tarot deck bundled with its original card back.",
            sampleImageUrl = "file:///android_asset/decks/hermetic-tarot.zip",
            source = "Bundled asset",
            author = "Godfrey Dowson"
        )
        val prefs = context.getSharedPreferences("mystic_oracle_prefs", Context.MODE_PRIVATE)
        val savedDeckId = prefs.getString("currentDeckId", "rider_waite_tarot") ?: "rider_waite_tarot"
        currentDeckId = when (savedDeckId) {
            "rider_waite" -> "rider_waite_tarot"
            "ethereal_tarot" -> "ethereal_visions_tarot"
            else -> savedDeckId
        }.takeIf { id -> availableDecks.any { it.id == id } } ?: builtInDecks.first().id
        tarotBackArtUrl = BundledTarotDecks.backImageFile(context, currentDeckId)?.toURI()?.toString() ?: run {
            "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Rider_Waite_Tarot_Deck_Back.jpg/360px-Rider_Waite_Tarot_Deck_Back.jpg"
        }
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

    private fun firstAssetImage(context: Context, folder: String): String {
        return (context.assets.list(folder) ?: emptyArray())
            .firstOrNull { it.endsWith(".jpg", true) || it.endsWith(".jpeg", true) || it.endsWith(".png", true) }
            ?: "0.jpg"
    }

    fun selectDeck(deckId: String, context: Context? = null) {
        currentDeckId = deckId
        tarotBackArtUrl = context?.let { BundledTarotDecks.backImageFile(it, deckId)?.toURI()?.toString() } ?: run {
            "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Rider_Waite_Tarot_Deck_Back.jpg/360px-Rider_Waite_Tarot_Deck_Back.jpg"
        }
        context?.let { ctx ->
            savePreferences(ctx)
            if (hapticsEnabled) {
                com.example.util.HapticUtil.performHaptic(ctx)
            }
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
            currentDeckId = builtInDecks.firstOrNull()?.id ?: "rider_waite_tarot"
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
