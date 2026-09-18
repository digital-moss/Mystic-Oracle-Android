package com.example.network

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

    val presetCardImages = listOf(
        "Rider-Waite-Smith Classic" to "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg",
        "Tarot de Marseille" to "https://commons.wikimedia.org/wiki/Special:FilePath/Tarots_de_Marseille.jpg",
        "Sola Busca (1491)" to "https://commons.wikimedia.org/wiki/Special:FilePath/Sola_Busca_tarot_card_05.jpg",
        "Mystic Magician" to "https://upload.wikimedia.org/wikipedia/commons/d/de/Rider-Waite-Smith_Tarot_01_Magician.jpg"
    )

    // Dedicated mapping for all 78 cards in Rider-Waite-Smith
    private val rwsCardFilenames = mapOf(
        // Major Arcana
        "0. The Fool" to "Rider-Waite-Smith_Tarot_00_Fool.jpg",
        "I. The Magician" to "Rider-Waite-Smith_Tarot_01_Magician.jpg",
        "II. The High Priestess" to "Rider-Waite-Smith_Tarot_02_High_Priestess.jpg",
        "III. The Empress" to "Rider-Waite-Smith_Tarot_03_Empress.jpg",
        "IV. The Emperor" to "Rider-Waite-Smith_Tarot_04_Emperor.jpg",
        "V. The Hierophant" to "Rider-Waite-Smith_Tarot_05_Hierophant.jpg",
        "VI. The Lovers" to "Rider-Waite-Smith_Tarot_06_Lovers.jpg",
        "VII. The Chariot" to "Rider-Waite-Smith_Tarot_07_Chariot.jpg",
        "VIII. Strength" to "Rider-Waite-Smith_Tarot_08_Strength.jpg",
        "IX. The Hermit" to "Rider-Waite-Smith_Tarot_09_Hermit.jpg",
        "X. Wheel of Fortune" to "Rider-Waite-Smith_Tarot_10_Wheel_of_Fortune.jpg",
        "XI. Justice" to "Rider-Waite-Smith_Tarot_11_Justice.jpg",
        "XII. The Hanged Man" to "Rider-Waite-Smith_Tarot_12_Hanged_Man.jpg",
        "XIII. Death" to "Rider-Waite-Smith_Tarot_13_Death.jpg",
        "XIV. Temperance" to "Rider-Waite-Smith_Tarot_14_Temperance.jpg",
        "XV. The Devil" to "Rider-Waite-Smith_Tarot_15_The_Devil.jpg",
        "XVI. The Tower" to "Rider-Waite-Smith_Tarot_16_The_Tower.jpg",
        "XVII. The Star" to "Rider-Waite-Smith_Tarot_17_The_Star.jpg",
        "XVIII. The Moon" to "Rider-Waite-Smith_Tarot_18_The_Moon.jpg",
        "XIX. The Sun" to "Rider-Waite-Smith_Tarot_19_The_Sun.jpg",
        "XX. Judgement" to "Rider-Waite-Smith_Tarot_20_Judgement.jpg",
        "XXI. The World" to "Rider-Waite-Smith_Tarot_21_World.jpg",

        // Suit of Wands
        "Ace of Wands" to "Wands01.jpg",
        "Two of Wands" to "Wands02.jpg",
        "Three of Wands" to "Wands03.jpg",
        "Four of Wands" to "Wands04.jpg",
        "Five of Wands" to "Wands05.jpg",
        "Six of Wands" to "Wands06.jpg",
        "Seven of Wands" to "Wands07.jpg",
        "Eight of Wands" to "Wands08.jpg",
        "Nine of Wands" to "Wands09.jpg",
        "Ten of Wands" to "Wands10.jpg",
        "Page of Wands" to "Wands11.jpg",
        "Knight of Wands" to "Wands12.jpg",
        "Queen of Wands" to "Wands13.jpg",
        "King of Wands" to "Wands14.jpg",

        // Suit of Cups
        "Ace of Cups" to "Cups01.jpg",
        "Two of Cups" to "Cups02.jpg",
        "Three of Cups" to "Cups03.jpg",
        "Four of Cups" to "Cups04.jpg",
        "Five of Cups" to "Cups05.jpg",
        "Six of Cups" to "Cups06.jpg",
        "Seven of Cups" to "Cups07.jpg",
        "Eight of Cups" to "Cups08.jpg",
        "Nine of Cups" to "Cups09.jpg",
        "Ten of Cups" to "Cups10.jpg",
        "Page of Cups" to "Cups11.jpg",
        "Knight of Cups" to "Cups12.jpg",
        "Queen of Cups" to "Cups13.jpg",
        "King of Cups" to "Cups14.jpg",

        // Suit of Swords
        "Ace of Swords" to "Swords01.jpg",
        "Two of Swords" to "Swords02.jpg",
        "Three of Swords" to "Swords03.jpg",
        "Four of Swords" to "Swords04.jpg",
        "Five of Swords" to "Swords05.jpg",
        "Six of Swords" to "Swords06.jpg",
        "Seven of Swords" to "Swords07.jpg",
        "Eight of Swords" to "Swords08.jpg",
        "Nine of Swords" to "Swords09.jpg",
        "Ten of Swords" to "Swords10.jpg",
        "Page of Swords" to "Swords11.jpg",
        "Knight of Swords" to "Swords12.jpg",
        "Queen of Swords" to "Swords13.jpg",
        "King of Swords" to "Swords14.jpg",

        // Suit of Pentacles
        "Ace of Pentacles" to "Pents01.jpg",
        "Two of Pentacles" to "Pents02.jpg",
        "Three of Pentacles" to "Pents03.jpg",
        "Four of Pentacles" to "Pents04.jpg",
        "Five of Pentacles" to "Pents05.jpg",
        "Six of Pentacles" to "Pents06.jpg",
        "Seven of Pentacles" to "Pents07.jpg",
        "Eight of Pentacles" to "Pents08.jpg",
        "Nine of Pentacles" to "Pents09.jpg",
        "Ten of Pentacles" to "Pents10.jpg",
        "Page of Pentacles" to "Pents11.jpg",
        "Knight of Pentacles" to "Pents12.jpg",
        "Queen of Pentacles" to "Pents13.jpg",
        "King of Pentacles" to "Pents14.jpg"
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

    fun setCardImageUrl(cardName: String, url: String) {
        customOverrides[cardName] = url
    }

    fun resetCardImageUrl(cardName: String) {
        customOverrides.remove(cardName)
    }

    fun getCardImageUrl(cardName: String, deckId: String = com.example.data.DeckManager.currentDeckId): String {
        // 1. Check custom overrides first
        val customKey = "${deckId}_$cardName"
        if (customOverrides.containsKey(customKey)) {
            return customOverrides[customKey]!!
        }
        if (customOverrides.containsKey(cardName)) {
            return customOverrides[cardName]!!
        }

        // 2. Special handling by Deck Preset
        when (deckId) {
            "marseille" -> {
                // Historic Tarot de Marseille images
                val cleanName = cardName.replace(Regex("^[IVXLCDM0-9]+[.\\s]+"), "").trim()
                return "https://commons.wikimedia.org/wiki/Special:FilePath/Tarot_de_Marseille_Nicolas_Conver_${cleanName.replace(" ", "_")}.jpg"
            }
            "sola_busca" -> {
                // Historic 1491 Sola Busca Renaissance deck
                val cleanName = cardName.replace(Regex("^[IVXLCDM0-9]+[.\\s]+"), "").trim()
                return "https://commons.wikimedia.org/wiki/Special:FilePath/Sola_Busca_tarot_card_${cleanName.replace(" ", "_")}.jpg"
            }
            "mystic_gold" -> {
                // High contrast mystical golden dawn correspondences
                for ((key, url) in directMajorUrls) {
                    if (cardName.contains(key, ignoreCase = true)) {
                        return url
                    }
                }
            }
        }

        // 3. Exact matching from the full 78 Rider-Waite-Smith dataset
        val exactFilename = rwsCardFilenames[cardName]
        if (exactFilename != null) {
            // For major arcana, use direct CDN if available for speed
            for ((key, url) in directMajorUrls) {
                if (cardName.contains(key, ignoreCase = true)) {
                    return url
                }
            }
            return "https://commons.wikimedia.org/wiki/Special:FilePath/$exactFilename"
        }

        // 4. Fuzzy fallback matching for variations in names
        for ((nameKey, filename) in rwsCardFilenames) {
            if (cardName.contains(nameKey, ignoreCase = true) || nameKey.contains(cardName, ignoreCase = true)) {
                return "https://commons.wikimedia.org/wiki/Special:FilePath/$filename"
            }
        }

        for ((key, url) in directMajorUrls) {
            if (cardName.contains(key, ignoreCase = true)) {
                return url
            }
        }

        return "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg"
    }
}
