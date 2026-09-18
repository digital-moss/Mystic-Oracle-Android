package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Mystic Oracle", appName)
  }

  @Test
  fun `test 64 hexagrams complete`() {
    assertEquals(64, com.example.model.IChingData.hexagrams.size)
  }

  @Test
  fun `test 78 tarot cards complete`() {
    assertEquals(78, com.example.model.TarotData.cards.size)
  }

  @Test
  fun `test 24 runes complete`() {
    assertEquals(24, com.example.model.RuneData.runes.size)
  }

  @Test
  fun `test deck manager initial state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.DeckManager.initPreferences(context)
    org.junit.Assert.assertNotNull(com.example.data.DeckManager.currentDeckId)
    org.junit.Assert.assertTrue(com.example.data.DeckManager.availableDecks.isNotEmpty())
  }
}
