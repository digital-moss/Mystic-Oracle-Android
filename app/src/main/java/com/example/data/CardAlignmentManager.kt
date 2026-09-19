package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.model.TarotCard
import com.example.model.TarotData
import com.example.network.TarotImageRepository
import org.json.JSONArray
import org.json.JSONObject

data class CardAlignmentSlot(
    val slotIndex: Int,                // 0..77
    val photoSourceIndex: Int,         // Default card index whose photo is bound to this slot (0..77)
    val customPhotoUrl: String? = null,// Optional custom URL / file URI for this slot
    val card: TarotCard                // Metadata for this slot (id, name, element, planet, astrology, tags, etc.)
)

data class AlignmentDiagnostics(
    val totalSlots: Int,
    val modifiedPhotoCount: Int,
    val modifiedInfoCount: Int,
    val isCleanDefault: Boolean,
    val duplicatePhotoIndices: List<Int>,
    val orphanedPhotoIndices: List<Int>,
    val missingAttributeWarnings: List<String>
)

object CardAlignmentManager {
    private const val PREFS_NAME = "mystic_card_alignment_prefs"
    private const val KEY_ALIGNMENT_JSON = "alignment_json_v1"

    private var appContext: Context? = null

    var slots by mutableStateOf<List<CardAlignmentSlot>>(emptyList())
        private set

    private var _alignedCards by mutableStateOf<List<TarotCard>>(emptyList())

    val alignedCards: List<TarotCard>
        get() {
            ensureInitialized()
            return _alignedCards
        }

    var isInitialized by mutableStateOf(false)
        private set

    var lastModifiedTimestamp by mutableStateOf<Long?>(null)
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
        ensureInitialized()
        loadPreferences(context)
    }

    fun ensureInitialized() {
        if (slots.isEmpty()) {
            val defaults = TarotData.defaultCards
            val initialSlots = defaults.mapIndexed { index, card ->
                CardAlignmentSlot(
                    slotIndex = index,
                    photoSourceIndex = index,
                    customPhotoUrl = null,
                    card = card.copy(id = index)
                )
            }
            slots = initialSlots
            _alignedCards = initialSlots.map { it.card }
            isInitialized = true
        }
    }

    private fun commitSlots(newSlots: List<CardAlignmentSlot>, context: Context? = null) {
        slots = newSlots
        _alignedCards = newSlots.map { it.card }
        lastModifiedTimestamp = System.currentTimeMillis()
        val targetContext = context ?: appContext
        targetContext?.let { savePreferences(it) }
    }

    // --- PHOTO ALIGNMENT OPERATIONS ---

    fun swapPhotos(slotA: Int, slotB: Int, context: Context? = null) {
        ensureInitialized()
        if (slotA !in slots.indices || slotB !in slots.indices || slotA == slotB) return

        val newSlots = slots.toMutableList()
        val a = newSlots[slotA]
        val b = newSlots[slotB]

        newSlots[slotA] = a.copy(
            photoSourceIndex = b.photoSourceIndex,
            customPhotoUrl = b.customPhotoUrl
        )
        newSlots[slotB] = b.copy(
            photoSourceIndex = a.photoSourceIndex,
            customPhotoUrl = a.customPhotoUrl
        )
        commitSlots(newSlots, context)
    }

    fun movePhoto(slotIndex: Int, direction: Int, context: Context? = null) {
        ensureInitialized()
        if (slotIndex !in slots.indices) return
        val targetSlot = (slotIndex + direction + slots.size) % slots.size
        swapPhotos(slotIndex, targetSlot, context)
    }

    fun shiftAllPhotos(offset: Int, context: Context? = null) {
        ensureInitialized()
        if (slots.isEmpty() || offset % slots.size == 0) return

        val size = slots.size
        val normalizedOffset = ((offset % size) + size) % size

        val newSlots = slots.mapIndexed { index, currentSlot ->
            // Source slot where the photo comes from
            val sourceIndex = (index - normalizedOffset + size) % size
            val sourceSlot = slots[sourceIndex]
            currentSlot.copy(
                photoSourceIndex = sourceSlot.photoSourceIndex,
                customPhotoUrl = sourceSlot.customPhotoUrl
            )
        }
        commitSlots(newSlots, context)
    }

    fun invertPhotoOrder(context: Context? = null) {
        ensureInitialized()
        if (slots.isEmpty()) return

        val size = slots.size
        val reversedPhotos = slots.reversed()
        val newSlots = slots.mapIndexed { index, currentSlot ->
            val sourceSlot = reversedPhotos[index]
            currentSlot.copy(
                photoSourceIndex = sourceSlot.photoSourceIndex,
                customPhotoUrl = sourceSlot.customPhotoUrl
            )
        }
        commitSlots(newSlots, context)
    }

    fun setCustomPhoto(slotIndex: Int, url: String?, context: Context? = null) {
        ensureInitialized()
        if (slotIndex !in slots.indices) return

        val cleanUrl = url?.trim()?.ifEmpty { null }
        val newSlots = slots.toMutableList()
        newSlots[slotIndex] = newSlots[slotIndex].copy(customPhotoUrl = cleanUrl)
        commitSlots(newSlots, context)
    }

    fun setCustomPhotoForCard(cardName: String, url: String?, context: Context? = null) {
        ensureInitialized()
        val slotIndex = slots.indexOfFirst { it.card.name.equals(cardName, ignoreCase = true) }
            .takeIf { it >= 0 }
            ?: slots.indexOfFirst { it.card.name.contains(cardName, ignoreCase = true) || cardName.contains(it.card.name, ignoreCase = true) }
        if (slotIndex >= 0) {
            setCustomPhoto(slotIndex, url, context)
        }
    }

    fun resetSlotPhoto(slotIndex: Int, context: Context? = null) {
        ensureInitialized()
        if (slotIndex !in slots.indices) return

        val newSlots = slots.toMutableList()
        newSlots[slotIndex] = newSlots[slotIndex].copy(
            photoSourceIndex = slotIndex,
            customPhotoUrl = null
        )
        commitSlots(newSlots, context)
    }

    fun resetSlotPhotoForCard(cardName: String, context: Context? = null) {
        ensureInitialized()
        val slotIndex = slots.indexOfFirst { it.card.name.equals(cardName, ignoreCase = true) }
            .takeIf { it >= 0 }
            ?: slots.indexOfFirst { it.card.name.contains(cardName, ignoreCase = true) || cardName.contains(it.card.name, ignoreCase = true) }
        if (slotIndex >= 0) {
            resetSlotPhoto(slotIndex, context)
        }
    }

    fun hasCustomPhotoForCard(cardName: String): Boolean {
        ensureInitialized()
        val slot = slots.firstOrNull { it.card.name.equals(cardName, ignoreCase = true) }
            ?: slots.firstOrNull { it.card.name.contains(cardName, ignoreCase = true) || cardName.contains(it.card.name, ignoreCase = true) }
        return !slot?.customPhotoUrl.isNullOrBlank() || (slot != null && slot.photoSourceIndex != slot.slotIndex)
    }

    fun resetAllPhotos(context: Context? = null) {
        ensureInitialized()
        val newSlots = slots.mapIndexed { index, slot ->
            slot.copy(photoSourceIndex = index, customPhotoUrl = null)
        }
        commitSlots(newSlots, context)
    }

    // --- CARD INFO / METADATA ALIGNMENT OPERATIONS ---

    fun swapCardInfo(slotA: Int, slotB: Int, context: Context? = null) {
        ensureInitialized()
        if (slotA !in slots.indices || slotB !in slots.indices || slotA == slotB) return

        val newSlots = slots.toMutableList()
        val a = newSlots[slotA]
        val b = newSlots[slotB]

        newSlots[slotA] = a.copy(card = b.card)
        newSlots[slotB] = b.copy(card = a.card)
        commitSlots(newSlots, context)
    }

    fun moveCardInfo(slotIndex: Int, direction: Int, context: Context? = null) {
        ensureInitialized()
        if (slotIndex !in slots.indices) return
        val targetSlot = (slotIndex + direction + slots.size) % slots.size
        swapCardInfo(slotIndex, targetSlot, context)
    }

    fun shiftAllCardInfo(offset: Int, context: Context? = null) {
        ensureInitialized()
        if (slots.isEmpty() || offset % slots.size == 0) return

        val size = slots.size
        val normalizedOffset = ((offset % size) + size) % size

        val newSlots = slots.mapIndexed { index, currentSlot ->
            val sourceIndex = (index - normalizedOffset + size) % size
            currentSlot.copy(card = slots[sourceIndex].card)
        }
        commitSlots(newSlots, context)
    }

    fun updateCardInfo(slotIndex: Int, updatedCard: TarotCard, context: Context? = null) {
        ensureInitialized()
        if (slotIndex !in slots.indices) return

        val newSlots = slots.toMutableList()
        newSlots[slotIndex] = newSlots[slotIndex].copy(card = updatedCard)
        commitSlots(newSlots, context)
    }

    fun resetSlotCardInfo(slotIndex: Int, context: Context? = null) {
        ensureInitialized()
        if (slotIndex !in slots.indices) return

        val defaultCard = TarotData.defaultCards.getOrNull(slotIndex)?.copy(id = slotIndex) ?: return
        val newSlots = slots.toMutableList()
        newSlots[slotIndex] = newSlots[slotIndex].copy(card = defaultCard)
        commitSlots(newSlots, context)
    }

    fun resetAllCardInfo(context: Context? = null) {
        ensureInitialized()
        val defaults = TarotData.defaultCards
        val newSlots = slots.mapIndexed { index, slot ->
            val defaultCard = defaults.getOrElse(index) { slot.card }.copy(id = index)
            slot.copy(card = defaultCard)
        }
        commitSlots(newSlots, context)
    }

    fun resetAll(context: Context? = null) {
        ensureInitialized()
        val defaults = TarotData.defaultCards
        val newSlots = defaults.mapIndexed { index, card ->
            CardAlignmentSlot(
                slotIndex = index,
                photoSourceIndex = index,
                customPhotoUrl = null,
                card = card.copy(id = index)
            )
        }
        commitSlots(newSlots, context)
    }

    // --- PHOTO URL RESOLUTION ---

    fun getPhotoUrlForCard(cardName: String, deckId: String = DeckManager.currentDeckId): String? {
        ensureInitialized()
        // Find slot by card name or partial match
        val slot = slots.firstOrNull { it.card.name.equals(cardName, ignoreCase = true) }
            ?: slots.firstOrNull { it.card.name.contains(cardName, ignoreCase = true) || cardName.contains(it.card.name, ignoreCase = true) }
            ?: return null

        // 1. Custom URL set for this slot
        if (!slot.customPhotoUrl.isNullOrBlank()) {
            return slot.customPhotoUrl
        }

        // 2. If photo source has been moved/swapped
        if (slot.photoSourceIndex != slot.slotIndex) {
            val sourceCardName = TarotData.defaultCards.getOrNull(slot.photoSourceIndex)?.name
            if (sourceCardName != null) {
                return TarotImageRepository.getBaseCardImageUrl(sourceCardName, deckId)
            }
        }

        return null
    }

    fun getPhotoSourceCardName(slot: CardAlignmentSlot): String {
        return TarotData.defaultCards.getOrNull(slot.photoSourceIndex)?.name ?: "Card #${slot.photoSourceIndex}"
    }

    // --- DIAGNOSTICS ---

    fun getDiagnostics(): AlignmentDiagnostics {
        ensureInitialized()
        var modifiedPhotos = 0
        var modifiedInfo = 0
        val photoIndexCounts = mutableMapOf<Int, Int>()
        val warnings = mutableListOf<String>()

        val defaults = TarotData.defaultCards

        slots.forEachIndexed { index, slot ->
            photoIndexCounts[slot.photoSourceIndex] = (photoIndexCounts[slot.photoSourceIndex] ?: 0) + 1

            if (slot.photoSourceIndex != index || !slot.customPhotoUrl.isNullOrBlank()) {
                modifiedPhotos++
            }

            val defaultCard = defaults.getOrNull(index)
            if (defaultCard != null && slot.card != defaultCard.copy(id = index)) {
                modifiedInfo++
            }

            if (slot.card.element.isBlank()) {
                warnings.add("Slot #${slot.slotIndex} (${slot.card.name}): Missing Element")
            }
            if (slot.card.planet.isBlank()) {
                warnings.add("Slot #${slot.slotIndex} (${slot.card.name}): Missing Planet")
            }
            if (slot.card.astrology.isBlank()) {
                warnings.add("Slot #${slot.slotIndex} (${slot.card.name}): Missing Astrological Alignment")
            }
        }

        val duplicates = photoIndexCounts.filter { it.value > 1 }.keys.toList()
        val orphaned = (0 until slots.size).filter { it !in photoIndexCounts.keys }.toList()

        return AlignmentDiagnostics(
            totalSlots = slots.size,
            modifiedPhotoCount = modifiedPhotos,
            modifiedInfoCount = modifiedInfo,
            isCleanDefault = (modifiedPhotos == 0 && modifiedInfo == 0),
            duplicatePhotoIndices = duplicates,
            orphanedPhotoIndices = orphaned,
            missingAttributeWarnings = warnings
        )
    }

    // --- PERSISTENCE & JSON EXPORT/IMPORT ---

    private fun loadPreferences(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = prefs.getString(KEY_ALIGNMENT_JSON, null)
            if (!jsonString.isNullOrBlank()) {
                importAlignmentJsonInternal(jsonString, saveAfter = false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun savePreferences(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = exportAlignmentJson()
            prefs.edit().putString(KEY_ALIGNMENT_JSON, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportAlignmentJson(): String {
        ensureInitialized()
        val root = JSONObject()
        root.put("version", 1)
        root.put("system", "tarot")
        root.put("totalSlots", slots.size)
        root.put("exportTimestamp", System.currentTimeMillis())

        val slotsArray = JSONArray()
        slots.forEach { slot ->
            val slotObj = JSONObject()
            slotObj.put("slotIndex", slot.slotIndex)
            slotObj.put("photoSourceIndex", slot.photoSourceIndex)
            if (slot.customPhotoUrl != null) {
                slotObj.put("customPhotoUrl", slot.customPhotoUrl)
            }

            val cardObj = JSONObject()
            cardObj.put("id", slot.card.id)
            cardObj.put("name", slot.card.name)
            cardObj.put("arcana", slot.card.arcana)
            cardObj.put("suit", slot.card.suit ?: JSONObject.NULL)
            cardObj.put("element", slot.card.element)
            cardObj.put("planet", slot.card.planet)
            cardObj.put("astrology", slot.card.astrology)
            cardObj.put("numerology", slot.card.numerology)
            cardObj.put("description", slot.card.description)
            cardObj.put("uprightMeaning", slot.card.uprightMeaning)
            cardObj.put("reversedMeaning", slot.card.reversedMeaning)

            val symbologyArray = JSONArray()
            slot.card.symbology.forEach { symbologyArray.put(it) }
            cardObj.put("symbology", symbologyArray)

            val termsArray = JSONArray()
            slot.card.defaultTerms.forEach { termsArray.put(it) }
            cardObj.put("defaultTerms", termsArray)

            slotObj.put("card", cardObj)
            slotsArray.put(slotObj)
        }
        root.put("slots", slotsArray)
        return root.toString(2)
    }

    fun importAlignmentJson(jsonString: String, context: Context? = null): Boolean {
        return importAlignmentJsonInternal(jsonString, saveAfter = true, context = context)
    }

    private fun importAlignmentJsonInternal(
        jsonString: String,
        saveAfter: Boolean,
        context: Context? = null
    ): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val slotsArray = root.optJSONArray("slots") ?: return false

            ensureInitialized()
            val newSlots = slots.toMutableList()

            for (i in 0 until slotsArray.length()) {
                val slotObj = slotsArray.getJSONObject(i)
                val slotIndex = slotObj.optInt("slotIndex", i)
                if (slotIndex !in newSlots.indices) continue

                val photoSource = slotObj.optInt("photoSourceIndex", slotIndex)
                val customUrl = if (slotObj.has("customPhotoUrl") && !slotObj.isNull("customPhotoUrl")) {
                    slotObj.getString("customPhotoUrl")
                } else null

                val cardObj = slotObj.optJSONObject("card")
                val card = if (cardObj != null) {
                    val symbology = mutableListOf<String>()
                    val symbArr = cardObj.optJSONArray("symbology")
                    if (symbArr != null) {
                        for (k in 0 until symbArr.length()) symbology.add(symbArr.getString(k))
                    }

                    val terms = mutableListOf<String>()
                    val termsArr = cardObj.optJSONArray("defaultTerms")
                    if (termsArr != null) {
                        for (k in 0 until termsArr.length()) terms.add(termsArr.getString(k))
                    }

                    TarotCard(
                        id = cardObj.optInt("id", slotIndex),
                        name = cardObj.optString("name", "Card $slotIndex"),
                        arcana = cardObj.optString("arcana", "Major"),
                        suit = if (cardObj.isNull("suit")) null else cardObj.optString("suit").ifEmpty { null },
                        uprightMeaning = cardObj.optString("uprightMeaning", ""),
                        reversedMeaning = cardObj.optString("reversedMeaning", ""),
                        description = cardObj.optString("description", ""),
                        astrology = cardObj.optString("astrology", ""),
                        numerology = cardObj.optString("numerology", ""),
                        element = cardObj.optString("element", ""),
                        planet = cardObj.optString("planet", ""),
                        symbology = symbology,
                        defaultTerms = terms
                    )
                } else {
                    newSlots[slotIndex].card
                }

                newSlots[slotIndex] = CardAlignmentSlot(
                    slotIndex = slotIndex,
                    photoSourceIndex = photoSource,
                    customPhotoUrl = customUrl,
                    card = card
                )
            }

            if (saveAfter) {
                commitSlots(newSlots, context)
            } else {
                slots = newSlots
                _alignedCards = newSlots.map { it.card }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
