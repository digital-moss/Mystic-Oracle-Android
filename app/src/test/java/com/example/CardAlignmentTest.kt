package com.example

import com.example.data.CardAlignmentManager
import com.example.model.TarotData
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CardAlignmentTest {

    @Before
    fun setUp() {
        CardAlignmentManager.ensureInitialized()
        CardAlignmentManager.resetAll()
    }

    @Test
    fun testInitialization78Cards() {
        assertEquals(78, TarotData.cards.size)
        assertEquals(78, CardAlignmentManager.slots.size)
        val diagnostics = CardAlignmentManager.getDiagnostics()
        assertTrue(diagnostics.isCleanDefault)
        assertEquals(0, diagnostics.modifiedPhotoCount)
        assertEquals(0, diagnostics.modifiedInfoCount)
    }

    @Test
    fun testSwapPhotos() {
        val initialPhoto0 = CardAlignmentManager.slots[0].photoSourceIndex
        val initialPhoto1 = CardAlignmentManager.slots[1].photoSourceIndex

        CardAlignmentManager.swapPhotos(0, 1)

        assertEquals(initialPhoto1, CardAlignmentManager.slots[0].photoSourceIndex)
        assertEquals(initialPhoto0, CardAlignmentManager.slots[1].photoSourceIndex)

        val diagnostics = CardAlignmentManager.getDiagnostics()
        assertFalse(diagnostics.isCleanDefault)
        assertEquals(2, diagnostics.modifiedPhotoCount)
    }

    @Test
    fun testShiftAllPhotos() {
        CardAlignmentManager.shiftAllPhotos(1)

        // Slot 0 should now have photo from slot 77
        assertEquals(77, CardAlignmentManager.slots[0].photoSourceIndex)
        // Slot 1 should have photo from slot 0
        assertEquals(0, CardAlignmentManager.slots[1].photoSourceIndex)

        // Shift back
        CardAlignmentManager.shiftAllPhotos(-1)
        assertEquals(0, CardAlignmentManager.slots[0].photoSourceIndex)
        assertEquals(1, CardAlignmentManager.slots[1].photoSourceIndex)
    }

    @Test
    fun testUpdateCardMetadata() {
        val originalCard = CardAlignmentManager.slots[0].card
        val modifiedCard = originalCard.copy(
            element = "Aether",
            planet = "Cosmos",
            astrology = "Ophiuchus",
            defaultTerms = listOf("Transcendence", "Awakening")
        )

        CardAlignmentManager.updateCardInfo(0, modifiedCard)

        assertEquals("Aether", CardAlignmentManager.slots[0].card.element)
        assertEquals("Cosmos", CardAlignmentManager.slots[0].card.planet)
        assertEquals("Ophiuchus", CardAlignmentManager.slots[0].card.astrology)

        // Also check TarotData.cards reflects it
        assertEquals("Aether", TarotData.cards[0].element)

        // Reset
        CardAlignmentManager.resetSlotCardInfo(0)
        assertEquals(originalCard.element, CardAlignmentManager.slots[0].card.element)
    }

    @Test
    fun testExportAndImportJson() {
        CardAlignmentManager.swapPhotos(0, 5)
        val json = CardAlignmentManager.exportAlignmentJson()
        assertTrue(json.contains("\"photoSourceIndex\": 5"))

        CardAlignmentManager.resetAll()
        assertEquals(0, CardAlignmentManager.slots[0].photoSourceIndex)

        val imported = CardAlignmentManager.importAlignmentJson(json)
        assertTrue(imported)
        assertEquals(5, CardAlignmentManager.slots[0].photoSourceIndex)
    }
}
