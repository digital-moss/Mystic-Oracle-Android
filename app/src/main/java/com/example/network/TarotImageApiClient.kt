package com.example.network

import android.content.Context
import com.example.R
import com.example.data.CardAlignmentManager
import com.example.data.DeckManager
import com.example.model.TarotCard
import com.example.model.TarotData
import com.example.tarot.BundledTarotDecks
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WikiSearchResponse(
    val query: WikiQuery?
)

data class WikiQuery(
    val pages: Map<String, WikiPage>?
)

data class WikiPage(
    val title: String?,
    val imageinfo: List<WikiImageInfo>?
)

data class WikiImageInfo(
    val url: String?
)

interface WikimediaImageApi {
    @GET("w/api.php?action=query&generator=search&gsrnamespace=6&prop=imageinfo&iiprop=url&format=json")
    suspend fun searchImages(
        @Query("gsrsearch") query: String
    ): WikiSearchResponse

    companion object {
        private const val BASE_URL = "https://commons.wikimedia.org/"

        val api: WikimediaImageApi by lazy {
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "MysticOracle/2.0 (Android; seeker@mysticoracle.app)")
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(logging)
                .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(WikimediaImageApi::class.java)
        }
    }
}

object TarotImageRepository {
    private val customOverrides = mutableMapOf<String, String>()
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        try {
            val prefs = context.getSharedPreferences("mystic_card_art_overrides", Context.MODE_PRIVATE)
            for ((key, value) in prefs.all) {
                if (value is String) {
                    customOverrides[key] = value
                }
            }
        } catch (_: Exception) {}

        BundledTarotDecks.discover(context)
            .map { it.id }
            .plus("hermetic_tarot")
            .forEach { deckId -> BundledTarotDecks.ensureDeckExtracted(context, deckId) }
    }

    fun getCardPlaceholderRes(cardName: String): Int {
        val lower = cardName.lowercase()
        return when {
            lower.contains("fool") -> R.drawable.img_tarot_fool
            lower.contains("magician") -> R.drawable.img_tarot_magician
            lower.contains("priestess") -> R.drawable.img_tarot_high_priestess
            lower.contains("empress") -> R.drawable.img_tarot_empress
            lower.contains("emperor") || lower.contains("hierophant") || lower.contains("chariot") -> R.drawable.img_tarot_emperor
            lower.contains("star") || lower.contains("moon") || lower.contains("temperance") || lower.contains("wheel") || lower.contains("world") -> R.drawable.img_tarot_star
            lower.contains("sun") || lower.contains("judgement") || lower.contains("strength") || lower.contains("devil") || lower.contains("tower") || lower.contains("death") || lower.contains("hanged") || lower.contains("justice") || lower.contains("hermit") || lower.contains("lovers") -> R.drawable.img_tarot_sun
            lower.contains("wand") -> R.drawable.img_tarot_wands
            lower.contains("cup") -> R.drawable.img_tarot_cups
            lower.contains("sword") -> R.drawable.img_tarot_swords
            lower.contains("pentacle") || lower.contains("coin") -> R.drawable.img_tarot_pentacles
            else -> R.drawable.img_tarot_card_face
        }
    }

    fun getCardPlaceholderRes(card: TarotCard): Int {
        return getCardPlaceholderRes(card.name)
    }

    val presetCardImages = listOf(
        "Rider-Waite-Smith Classic" to "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg",
        "Tarot de Marseille" to "https://commons.wikimedia.org/wiki/Special:FilePath/Tarots_de_Marseille.jpg",
        "Sola Busca (1491)" to "https://commons.wikimedia.org/wiki/Special:FilePath/Sola_Busca_tarot_card_05.jpg",
        "Mystic Magician" to "https://upload.wikimedia.org/wikipedia/commons/d/de/Rider-Waite-Smith_Tarot_01_Magician.jpg"
    )

    // Dedicated mapping for all 78 cards in Rider-Waite-Smith using searge/tarot assets
    private val rwsCardFilenames = mapOf(
        // Major Arcana
        "0. The Fool" to "major_00_Fool.jpg",
        "I. The Magician" to "major_01_Magician.jpg",
        "II. The High Priestess" to "major_02_High_Priestess.jpg",
        "III. The Empress" to "major_03_Empress.jpg",
        "IV. The Emperor" to "major_04_Emperor.jpg",
        "V. The Hierophant" to "major_05_Hierophant.jpg",
        "VI. The Lovers" to "major_06_Lovers.jpg",
        "VII. The Chariot" to "major_07_Chariot.jpg",
        "VIII. Strength" to "major_08_Strength.jpg",
        "IX. The Hermit" to "major_09_Hermit.jpg",
        "X. Wheel of Fortune" to "major_10_Wheel_of_Fortune.jpg",
        "XI. Justice" to "major_11_Justice.jpg",
        "XII. The Hanged Man" to "major_12_Hanged_Man.jpg",
        "XIII. Death" to "major_13_Death.jpg",
        "XIV. Temperance" to "major_14_Temperance.jpg",
        "XV. The Devil" to "major_15_Devil.jpg",
        "XVI. The Tower" to "major_16_Tower.jpg",
        "XVII. The Star" to "major_17_Star.jpg",
        "XVIII. The Moon" to "major_18_Moon.jpg",
        "XIX. The Sun" to "major_19_Sun.jpg",
        "XX. Judgement" to "major_20_Judgement.jpg",
        "XXI. The World" to "major_21_World.jpg",

        // Suit of Wands
        "Ace of Wands" to "wands01.jpg",
        "Two of Wands" to "wands02.jpg",
        "Three of Wands" to "wands03.jpg",
        "Four of Wands" to "wands04.jpg",
        "Five of Wands" to "wands05.jpg",
        "Six of Wands" to "wands06.jpg",
        "Seven of Wands" to "wands07.jpg",
        "Eight of Wands" to "wands08.jpg",
        "Nine of Wands" to "wands09.jpg",
        "Ten of Wands" to "wands10.jpg",
        "Page of Wands" to "wands11.jpg",
        "Knight of Wands" to "wands12.jpg",
        "Queen of Wands" to "wands13.jpg",
        "King of Wands" to "wands14.jpg",

        // Suit of Cups
        "Ace of Cups" to "cups01.jpg",
        "Two of Cups" to "cups02.jpg",
        "Three of Cups" to "cups03.jpg",
        "Four of Cups" to "cups04.jpg",
        "Five of Cups" to "cups05.jpg",
        "Six of Cups" to "cups06.jpg",
        "Seven of Cups" to "cups07.jpg",
        "Eight of Cups" to "cups08.jpg",
        "Nine of Cups" to "cups09.jpg",
        "Ten of Cups" to "cups10.jpg",
        "Page of Cups" to "cups11.jpg",
        "Knight of Cups" to "cups12.jpg",
        "Queen of Cups" to "cups13.jpg",
        "King of Cups" to "cups14.jpg",

        // Suit of Swords
        "Ace of Swords" to "swords01.jpg",
        "Two of Swords" to "swords02.jpg",
        "Three of Swords" to "swords03.jpg",
        "Four of Swords" to "swords04.jpg",
        "Five of Swords" to "swords05.jpg",
        "Six of Swords" to "swords06.jpg",
        "Seven of Swords" to "swords07.jpg",
        "Eight of Swords" to "swords08.jpg",
        "Nine of Swords" to "swords09.jpg",
        "Ten of Swords" to "swords10.jpg",
        "Page of Swords" to "swords11.jpg",
        "Knight of Swords" to "swords12.jpg",
        "Queen of Swords" to "swords13.jpg",
        "King of Swords" to "swords14.jpg",

        // Suit of Pentacles
        "Ace of Pentacles" to "pents01.jpg",
        "Two of Pentacles" to "pents02.jpg",
        "Three of Pentacles" to "pents03.jpg",
        "Four of Pentacles" to "pents04.jpg",
        "Five of Pentacles" to "pents05.jpg",
        "Six of Pentacles" to "pents06.jpg",
        "Seven of Pentacles" to "pents07.jpg",
        "Eight of Pentacles" to "pents08.jpg",
        "Nine of Pentacles" to "pents09.jpg",
        "Ten of Pentacles" to "pents10.jpg",
        "Page of Pentacles" to "pents11.jpg",
        "Knight of Pentacles" to "pents12.jpg",
        "Queen of Pentacles" to "pents13.jpg",
        "King of Pentacles" to "pents14.jpg"
    )

    // Direct High-Resolution CDN mirrors for instant loading
    private val directMajorUrls = mapOf(
        "Fool" to "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg",
        "Magician" to "https://upload.wikimedia.org/wikipedia/commons/d/de/Rider-Waite-Smith_Tarot_01_Magician.jpg",
        "Priestess" to "https://upload.wikimedia.org/wikipedia/commons/8/88/Rider-Waite-Smith_Tarot_02_High_Priestess.jpg",
        "Empress" to "https://upload.wikimedia.org/wikipedia/commons/d/d2/Rider-Waite-Smith_Tarot_03_Empress.jpg",
        "Emperor" to "https://upload.wikimedia.org/wikipedia/commons/c/c3/Rider-Waite-Smith_Tarot_04_Emperor.jpg",
        "Hierophant" to "https://upload.wikimedia.org/wikipedia/commons/8/8d/Rider-Waite-Smith_Tarot_05_Hierophant.jpg",
        "Lovers" to "https://upload.wikimedia.org/wikipedia/commons/3/3a/Rider-Waite-Smith_Tarot_06_Lovers.jpg",
        "Chariot" to "https://upload.wikimedia.org/wikipedia/commons/9/9b/Rider-Waite-Smith_Tarot_07_Chariot.jpg",
        "Strength" to "https://upload.wikimedia.org/wikipedia/commons/f/f5/Rider-Waite-Smith_Tarot_08_Strength.jpg",
        "Hermit" to "https://upload.wikimedia.org/wikipedia/commons/4/4d/Rider-Waite-Smith_Tarot_09_Hermit.jpg",
        "Wheel" to "https://upload.wikimedia.org/wikipedia/commons/3/3c/Rider-Waite-Smith_Tarot_10_Wheel_of_Fortune.jpg",
        "Justice" to "https://upload.wikimedia.org/wikipedia/commons/e/e0/Rider-Waite-Smith_Tarot_11_Justice.jpg",
        "Hanged" to "https://upload.wikimedia.org/wikipedia/commons/2/2b/Rider-Waite-Smith_Tarot_12_Hanged_Man.jpg",
        "Death" to "https://upload.wikimedia.org/wikipedia/commons/d/d7/Rider-Waite-Smith_Tarot_13_Death.jpg",
        "Temperance" to "https://upload.wikimedia.org/wikipedia/commons/f/f8/Rider-Waite-Smith_Tarot_14_Temperance.jpg",
        "Devil" to "https://upload.wikimedia.org/wikipedia/commons/5/55/Rider-Waite-Smith_Tarot_15_The_Devil.jpg",
        "Tower" to "https://upload.wikimedia.org/wikipedia/commons/5/53/Rider-Waite-Smith_Tarot_16_The_Tower.jpg",
        "Star" to "https://upload.wikimedia.org/wikipedia/commons/d/db/Rider-Waite-Smith_Tarot_17_The_Star.jpg",
        "Moon" to "https://upload.wikimedia.org/wikipedia/commons/7/7f/Rider-Waite-Smith_Tarot_18_The_Moon.jpg",
        "Sun" to "https://upload.wikimedia.org/wikipedia/commons/1/17/Rider-Waite-Smith_Tarot_19_The_Sun.jpg",
        "Judgement" to "https://upload.wikimedia.org/wikipedia/commons/d/dd/Rider-Waite-Smith_Tarot_20_Judgement.jpg",
        "World" to "https://upload.wikimedia.org/wikipedia/commons/f/ff/Rider-Waite-Smith_Tarot_21_World.jpg"
    )

    fun setCardImageUrl(cardName: String, url: String, deckId: String = DeckManager.currentDeckId, context: Context? = null) {
        val targetContext = context ?: appContext
        val deckKey = "${deckId}___${cardName}"
        customOverrides[deckKey] = url
        customOverrides[cardName] = url

        // Also bind to CardAlignmentManager so the entire engine reflects it
        CardAlignmentManager.setCustomPhotoForCard(cardName, url, targetContext)

        try {
            targetContext?.getSharedPreferences("mystic_card_art_overrides", Context.MODE_PRIVATE)
                ?.edit()
                ?.putString(deckKey, url)
                ?.putString(cardName, url)
                ?.apply()
        } catch (_: Exception) {}
    }

    fun deleteCardImageUrl(cardName: String, deckId: String = DeckManager.currentDeckId, context: Context? = null) {
        val targetContext = context ?: appContext
        val deckKey = "${deckId}___${cardName}"
        customOverrides.remove(deckKey)
        customOverrides.remove(cardName)

        // Also reset in CardAlignmentManager
        CardAlignmentManager.resetSlotPhotoForCard(cardName, targetContext)

        try {
            targetContext?.getSharedPreferences("mystic_card_art_overrides", Context.MODE_PRIVATE)
                ?.edit()
                ?.remove(deckKey)
                ?.remove(cardName)
                ?.apply()
        } catch (_: Exception) {}
    }

    fun resetCardImageUrl(cardName: String, deckId: String = DeckManager.currentDeckId, context: Context? = null) {
        deleteCardImageUrl(cardName, deckId, context)
    }

    fun hasCustomCardArt(cardName: String, deckId: String = DeckManager.currentDeckId): Boolean {
        val deckKey = "${deckId}___${cardName}"
        return customOverrides.containsKey(deckKey) ||
               customOverrides.containsKey(cardName) ||
               CardAlignmentManager.hasCustomPhotoForCard(cardName)
    }

    fun getBaseCardImageUrl(cardName: String, deckId: String = DeckManager.currentDeckId): String {
        // Every built-in deck resolves to its own bundled asset directory first.
        appContext?.let { context ->
            TarotData.cards.firstOrNull { it.name == cardName }?.let { card ->
                BundledTarotDecks.cardImageFile(context, deckId, card)
                    ?.let { return "file://${it.absolutePath}" }
            }
        }

        // 2. Otherwise check custom overrides first (deck specific, then generic)
        val customKey = "${deckId}___${cardName}"
        if (customOverrides.containsKey(customKey)) {
            return customOverrides[customKey]!!
        }
        val altKey = "${deckId}_$cardName"
        if (customOverrides.containsKey(altKey)) {
            return customOverrides[altKey]!!
        }
        if (customOverrides.containsKey(cardName)) {
            return customOverrides[cardName]!!
        }

        // 3. Special handling by remote deck presets
        when (deckId) {
            "marseille", "github_mixvlad" -> {
                val cleanName = cardName.replace(Regex("^[IVXLCDM0-9]+[.\\s]+"), "").trim()
                return "https://commons.wikimedia.org/wiki/Special:FilePath/Tarot_de_Marseille_Nicolas_Conver_${cleanName.replace(" ", "_")}.jpg"
            }
            "sola_busca" -> {
                val cleanName = cardName.replace(Regex("^[IVXLCDM0-9]+[.\\s]+"), "").trim()
                return "https://commons.wikimedia.org/wiki/Special:FilePath/Sola_Busca_tarot_card_${cleanName.replace(" ", "_")}.jpg"
            }
            "mystic_gold", "astrolabe", "decanates" -> {
                for ((key, url) in directMajorUrls) {
                    if (cardName.contains(key, ignoreCase = true)) {
                        return url
                    }
                }
            }
            "alchemical", "visconti_sforza" -> {
                val cleanName = cardName.replace(Regex("^[IVXLCDM0-9]+[.\\s]+"), "").trim()
                return "https://commons.wikimedia.org/wiki/Special:FilePath/Tarots_de_Marseille.jpg"
            }
            "albano", "github_metabismuth", "github_luciellaes", "github_krates" -> {
                val exactFilename = rwsCardFilenames[cardName]
                if (exactFilename != null) {
                    for ((key, url) in directMajorUrls) {
                        if (cardName.contains(key, ignoreCase = true)) {
                            return url
                        }
                    }
                    return "https://commons.wikimedia.org/wiki/Special:FilePath/$exactFilename"
                }
            }
        }

        // 4. Fallback for other decks matching from full dataset
        val exactFilename = rwsCardFilenames[cardName]
        if (exactFilename != null) {
            val localFile = java.io.File(appContext?.filesDir, "tarot_big_images/$exactFilename")
            if (localFile.exists()) {
                return "file://${localFile.absolutePath}"
            }
            return "https://raw.githubusercontent.com/searge/tarot/master/assets/img/big/$exactFilename"
        }

        return "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg"
    }

    fun getCardImageUrl(cardName: String, deckId: String = DeckManager.currentDeckId): String {
        // 1. Deck-specific override
        val customKey = "${deckId}___${cardName}"
        if (customOverrides.containsKey(customKey)) {
            return customOverrides[customKey]!!
        }

        // 2. Check CardAlignmentManager first for swapped, shifted, or custom-bound photos
        val alignedUrl = CardAlignmentManager.getPhotoUrlForCard(cardName, deckId)
        if (alignedUrl != null) {
            return alignedUrl
        }

        // 3. Generic custom override
        if (customOverrides.containsKey(cardName)) {
            return customOverrides[cardName]!!
        }

        return getBaseCardImageUrl(cardName, deckId)
    }

    fun getCardImageModel(cardName: String, deckId: String = DeckManager.currentDeckId): Any {
        val url = getCardImageUrl(cardName, deckId)
        if (url.startsWith("file://")) {
            val path = url.removePrefix("file://")
            val file = java.io.File(path)
            if (file.exists()) {
                return file
            }
        }
        return url
    }
}
