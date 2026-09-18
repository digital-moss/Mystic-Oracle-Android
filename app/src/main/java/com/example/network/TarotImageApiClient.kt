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
            val client = OkHttpClient.Builder().addInterceptor(logging).build()

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

    val presetCardImages = mapOf(
        "Fool (Classic)" to "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg",
        "Magician (Classic)" to "https://upload.wikimedia.org/wikipedia/commons/d/de/Rider-Waite-Smith_Tarot_01_Magician.jpg",
        "High Priestess (Classic)" to "https://upload.wikimedia.org/wikipedia/commons/8/88/Rider-Waite-Smith_Tarot_02_High_Priestess.jpg",
        "Empress (Classic)" to "https://upload.wikimedia.org/wikipedia/commons/d/d2/Rider-Waite-Smith_Tarot_03_Empress.jpg",
        "Emperor (Classic)" to "https://upload.wikimedia.org/wikipedia/commons/c/c3/Rider-Waite-Smith_Tarot_04_Emperor.jpg",
        "Star (Classic)" to "https://upload.wikimedia.org/wikipedia/commons/d/db/Rider-Waite-Smith_Tarot_17_The_Star.jpg",
        "Sun (Classic)" to "https://upload.wikimedia.org/wikipedia/commons/1/17/Rider-Waite-Smith_Tarot_19_The_Sun.jpg",
        "Mystic Gold Sigil" to "https://upload.wikimedia.org/wikipedia/commons/3/3c/Rider-Waite-Smith_Tarot_10_Wheel_of_Fortune.jpg",
        "Cosmic Wheel" to "https://upload.wikimedia.org/wikipedia/commons/f/ff/Rider-Waite-Smith_Tarot_21_World.jpg"
    )

    private val directUrls = mapOf(
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

    fun getCardImageUrl(cardName: String): String {
        if (customOverrides.containsKey(cardName)) {
            return customOverrides[cardName]!!
        }
        for ((key, url) in directUrls) {
            if (cardName.contains(key, ignoreCase = true)) {
                return url
            }
        }
        return "https://upload.wikimedia.org/wikipedia/commons/9/90/Rider-Waite-Smith_Tarot_00_Fool.jpg"
    }
}
