package com.example.tarot

import android.content.Context
import java.io.File
import java.util.zip.ZipInputStream

enum class TarotDeck(val displayName: String) {
    RYDER_WAITE("Rider-Waite"),
    ETHEREAL_VISIONS("Ethereal Visions")
}

object BundledTarotDecks {
    private const val assetPath = "decks/ethereal-visions.zip"

    fun ensureEtherealVisionsExtracted(context: Context): File {
        val deckDirectory = File(context.filesDir, "bundled_decks/ethereal_visions")
        val marker = File(deckDirectory, ".complete")
        if (marker.exists()) return deckDirectory

        deckDirectory.deleteRecursively()
        deckDirectory.mkdirs()
        context.assets.open(assetPath).use { assetStream ->
            ZipInputStream(assetStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val fileName = entry.name.substringAfterLast('/')
                    if (!entry.isDirectory && fileName.matches(Regex("\\d+\\.jpg"))) {
                        val outputFile = File(deckDirectory, fileName)
                        outputFile.outputStream().use { output -> zip.copyTo(output) }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        val imageCount = deckDirectory.listFiles { file -> file.extension.equals("jpg", true) }?.size ?: 0
        check(imageCount == 79) { "Ethereal Visions deck is incomplete: expected 79 images, found $imageCount" }
        marker.createNewFile()
        return deckDirectory
    }

    fun imageFile(deckDirectory: File?, cardIndex: Int): File? {
        if (deckDirectory == null || cardIndex !in 0..78) return null
        val image = File(deckDirectory, "$cardIndex.jpg")
        return image.takeIf { it.exists() }
    }
}
