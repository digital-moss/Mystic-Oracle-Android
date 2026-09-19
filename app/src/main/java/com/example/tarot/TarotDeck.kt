package com.example.tarot

import android.content.Context
import java.io.File
import java.util.zip.ZipInputStream
import com.example.model.TarotCard
import com.example.model.TarotData

enum class TarotDeck(val displayName: String) {
    RYDER_WAITE("Rider-Waite"),
    ETHEREAL_VISIONS("Ethereal Visions")
}

object BundledTarotDecks {
    private const val assetPath = "decks/ethereal-visions.zip"
    private const val hermeticAssetPath = "decks/hermetic-tarot.zip"

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

    fun cardImageFile(context: Context, deckId: String, card: TarotCard): File? {
        val directory = ensureDeckExtracted(context, deckId) ?: return null
        val index = TarotData.cards.indexOf(card)
        val fileName = when (deckId) {
            "rider_waite" -> riderWaiteFileName(card.name)
            "ethereal_tarot", "hermetic_tarot" -> "$index.jpg"
            else -> null
        } ?: return null
        return File(directory, fileName).takeIf { it.exists() }
    }

    fun backImageFile(context: Context, deckId: String): File? {
        val directory = ensureDeckExtracted(context, deckId) ?: return null
        return File(directory, "back.jpg").takeIf { it.exists() }
    }

    fun ensureDeckExtracted(context: Context, deckId: String): File? {
        val (assetFolder, assetZip, directory) = when (deckId) {
            "rider_waite" -> Triple("rider_waite_tarot", null, File(context.filesDir, "bundled_decks/rider_waite"))
            "ethereal_tarot" -> Triple("ethereal_visions_tarot", null, File(context.filesDir, "bundled_decks/ethereal_visions"))
            "hermetic_tarot" -> Triple(null, hermeticAssetPath, File(context.filesDir, "bundled_decks/hermetic_tarot"))
            else -> return null
        }
        val marker = File(directory, ".complete")
        if (marker.exists()) return directory

        directory.deleteRecursively()
        directory.mkdirs()
        if (assetFolder != null) {
            (context.assets.list(assetFolder) ?: emptyArray()).forEach { fileName ->
                context.assets.open("$assetFolder/$fileName").use { input ->
                    File(directory, fileName).outputStream().use { output -> input.copyTo(output) }
                }
            }
        } else if (assetZip != null) {
            context.assets.open(assetZip).use { assetStream ->
                ZipInputStream(assetStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val fileName = entry.name.substringAfterLast('/')
                        if (!entry.isDirectory && fileName.matches(Regex("(back|\\d+)\\.jpg"))) {
                            File(directory, fileName).outputStream().use { output -> zip.copyTo(output) }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
        }
        check((directory.listFiles { file -> file.extension.equals("jpg", true) }?.size ?: 0) >= 78) {
            "Deck $deckId is missing card images"
        }
        marker.createNewFile()
        return directory
    }

    private fun riderWaiteFileName(cardName: String): String? {
        val majorNames = listOf(
            "Fool", "Magician", "High_Priestess", "Empress", "Emperor", "Hierophant",
            "Lovers", "Chariot", "Strength", "Hermit", "Wheel_of_Fortune", "Justice",
            "Hanged_Man", "Death", "Temperance", "Devil", "Tower", "Star", "Moon",
            "Sun", "Judgement", "World"
        )
        val index = TarotData.cards.indexOfFirst { it.name == cardName }
        if (index in 0..21) return "major_${index.toString().padStart(2, '0')}_${majorNames[index]}.jpg"
        val suit = cardName.substringAfter("of ", "").lowercase()
        val suitPrefix = when {
            suit.contains("wand") -> "wands"
            suit.contains("cup") -> "cups"
            suit.contains("sword") -> "swords"
            suit.contains("pentacle") -> "pents"
            else -> return null
        }
        val suitStart = 22 + listOf("Wands", "Cups", "Swords", "Pentacles").indexOfFirst { suit.contains(it.dropLast(1).lowercase()) } * 14
        return "$suitPrefix${(index - suitStart + 1).toString().padStart(2, '0')}.jpg"
    }

    fun ensureHermeticTarotExtracted(context: Context): File {
        val deckDirectory = File(context.filesDir, "bundled_decks/hermetic_tarot")
        val marker = File(deckDirectory, ".complete")
        if (marker.exists()) return deckDirectory

        deckDirectory.deleteRecursively()
        deckDirectory.mkdirs()
        context.assets.open(hermeticAssetPath).use { assetStream ->
            ZipInputStream(assetStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val fileName = entry.name.substringAfterLast('/')
                    if (!entry.isDirectory && fileName.matches(Regex("(back|\\d+)\\.jpg"))) {
                        File(deckDirectory, fileName).outputStream().use { output -> zip.copyTo(output) }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        val imageCount = deckDirectory.listFiles { file -> file.extension.equals("jpg", true) }?.size ?: 0
        check(imageCount == 80) { "Hermetic Tarot deck is incomplete: expected 80 images, found $imageCount" }
        marker.createNewFile()
        return deckDirectory
    }
}
