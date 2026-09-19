package com.example.tarot

import android.content.Context
import java.io.File
import java.util.zip.ZipInputStream
import com.example.model.TarotCard
import com.example.model.TarotData

data class BundledDeck(
    val id: String,
    val assetFolder: String,
    val imageCount: Int
)

object BundledTarotDecks {
    private const val assetPath = "decks/ethereal-visions.zip"
    private const val hermeticAssetPath = "decks/hermetic-tarot.zip"

    fun discover(context: Context): List<BundledDeck> {
        return (context.assets.list("") ?: emptyArray())
            .filter { it.endsWith("_tarot", ignoreCase = true) }
            .mapNotNull { folder ->
                val imageCount = (context.assets.list(folder) ?: emptyArray())
                    .count { it.endsWith(".jpg", true) || it.endsWith(".jpeg", true) || it.endsWith(".png", true) }
                if (imageCount > 0) BundledDeck(folder, folder, imageCount) else null
            }
            .sortedBy { it.id }
    }

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
        val files = directory.listFiles { file ->
            file.extension.equals("jpg", true) || file.extension.equals("jpeg", true) || file.extension.equals("png", true)
        }?.toList().orEmpty()
        val numericFile = files.firstOrNull { it.nameWithoutExtension.toIntOrNull() == index }
        val fileName = numericFile ?: files.firstOrNull {
            it.nameWithoutExtension.replace(Regex("[^A-Za-z0-9]"), "")
                .contains(card.name.replace(Regex("[^A-Za-z0-9]"), ""), true)
        } ?: riderWaiteFileName(card.name)?.let { expectedName ->
            files.firstOrNull { it.name.equals(expectedName, true) }
        }
        return fileName?.takeIf { it.exists() }
    }

    fun backImageFile(context: Context, deckId: String): File? {
        val directory = ensureDeckExtracted(context, deckId) ?: return null
        return File(directory, "back.jpg").takeIf { it.exists() }
    }

    fun ensureDeckExtracted(context: Context, deckId: String): File? {
        val assetFolder = resolveAssetFolder(deckId)
        val (folder, assetZip) = when {
            assetFolder != null -> assetFolder to null
            deckId == "hermetic_tarot" -> null to hermeticAssetPath
            else -> return null
        }
        val directory = File(context.filesDir, "bundled_decks/$deckId")
        val sourceFolder = folder
        val sourceZip = assetZip
        val marker = File(directory, ".complete")
        if (marker.exists()) return directory

        directory.deleteRecursively()
        directory.mkdirs()
        if (sourceFolder != null) {
            (context.assets.list(sourceFolder) ?: emptyArray()).forEach { fileName ->
                context.assets.open("$sourceFolder/$fileName").use { input ->
                    File(directory, fileName).outputStream().use { output -> input.copyTo(output) }
                }
            }
        } else if (sourceZip != null) {
            context.assets.open(sourceZip).use { assetStream ->
                ZipInputStream(assetStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val fileName = entry.name.substringAfterLast('/')
                        if (!entry.isDirectory && fileName.matches(Regex("(back|\\d+)\\.(jpg|jpeg|png)"))) {
                            File(directory, fileName).outputStream().use { output -> zip.copyTo(output) }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
        }
        check((directory.listFiles { file ->
            file.extension.equals("jpg", true) ||
                file.extension.equals("jpeg", true) ||
                file.extension.equals("png", true)
        }?.size ?: 0) >= 78) {
            "Deck $deckId is missing card images"
        }
        marker.createNewFile()
        return directory
    }

    private fun resolveAssetFolder(deckId: String): String? {
        return when (deckId) {
            "rider_waite" -> "rider_waite_tarot"
            "ethereal_tarot" -> "ethereal_visions_tarot"
            "hermetic_tarot" -> null
            else -> deckId.takeIf { it.endsWith("_tarot", true) }
        }
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
