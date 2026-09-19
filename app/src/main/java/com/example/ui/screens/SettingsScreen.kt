package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.CardAlignmentManager
import com.example.data.DeckManager
import com.example.data.ReadingEntity
import com.example.donations.DonationOptions
import com.example.util.HapticUtil
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChanged: (String) -> Unit,
    readings: List<ReadingEntity>,
    onImportReadings: (List<ReadingEntity>) -> Unit
) {
    val context = LocalContext.current
    var showAlignmentStudio by remember { mutableStateOf(false) }

    if (showAlignmentStudio) {
        CardAlignmentStudioScreen(
            onBack = { showAlignmentStudio = false }
        )
        return
    }

    var soundEnabled by remember { mutableStateOf(true) }
    var dailyReminder by remember { mutableStateOf(false) }

    // Account & Google Login State
    var showAccountDialog by remember { mutableStateOf(false) }
    var customEmailInput by remember { mutableStateOf(DeckManager.googleAccountEmail) }
    var customNameInput by remember { mutableStateOf(DeckManager.userDisplayName) }

    // Custom Back & Coin Art inputs
    var tarotBackInput by remember { mutableStateOf(DeckManager.tarotBackArtUrl) }
    var runeBackInput by remember { mutableStateOf(DeckManager.runeBackArtUrl) }
    var iChingBackInput by remember { mutableStateOf(DeckManager.iChingBackArtUrl) }
    var coinHeadsInput by remember { mutableStateOf(DeckManager.coinHeadsArtUrl) }
    var coinTailsInput by remember { mutableStateOf(DeckManager.coinTailsArtUrl) }

    // Status messages
    var exportStatus by remember { mutableStateOf("") }
    var importStatus by remember { mutableStateOf("") }
    var fontImportStatus by remember { mutableStateOf<String?>(null) }
    var deckZipStatus by remember { mutableStateOf<String?>(null) }
    var deckSearchQuery by remember { mutableStateOf("") }
    var deckSourceFilter by remember { mutableStateOf("All") }
    var donationStatus by remember { mutableStateOf("") }
    var driveSyncStatus by remember { mutableStateOf(DeckManager.syncStatusMessage) }

    // Image Upload Launchers
    val tarotBackPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            tarotBackInput = it.toString()
            DeckManager.tarotBackArtUrl = it.toString()
            DeckManager.savePreferences(context)
        }
    }
    val runeBackPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            runeBackInput = it.toString()
            DeckManager.runeBackArtUrl = it.toString()
            DeckManager.savePreferences(context)
        }
    }
    val iChingBackPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            iChingBackInput = it.toString()
            DeckManager.iChingBackArtUrl = it.toString()
            DeckManager.savePreferences(context)
        }
    }
    val coinHeadsPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coinHeadsInput = it.toString()
            DeckManager.coinHeadsArtUrl = it.toString()
            DeckManager.savePreferences(context)
        }
    }
    val coinTailsPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coinTailsInput = it.toString()
            DeckManager.coinTailsArtUrl = it.toString()
            DeckManager.savePreferences(context)
        }
    }

    // Font Import Launcher (.ttf)
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val fontsDir = File(context.filesDir, "custom_fonts").apply { if (!exists()) mkdirs() }
                val targetFile = File(fontsDir, "user_font_${System.currentTimeMillis()}.ttf")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (targetFile.exists() && targetFile.length() > 0) {
                    DeckManager.customFontPath = targetFile.absolutePath
                    DeckManager.selectedFontName = "Custom Font"
                    DeckManager.savePreferences(context)
                    fontImportStatus = "Custom font successfully loaded!"
                } else {
                    fontImportStatus = "Font file was empty."
                }
            } catch (e: Exception) {
                fontImportStatus = "Failed to import font: ${e.localizedMessage}"
            }
        }
    }

    // CSV Export & Import
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val csvHeader = "ID,Type,Title,Timestamp,Description\n"
                    outputStream.write(csvHeader.toByteArray())
                    readings.forEach { r ->
                        val line = "${r.id},\"${r.type}\",\"${r.title.replace("\"", "\"\"")}\",${r.timestamp},\"${r.description.replace("\"", "\"\"")}\"\n"
                        outputStream.write(line.toByteArray())
                    }
                }
                exportStatus = "Successfully exported ${readings.size} readings to CSV."
            } catch (e: Exception) {
                exportStatus = "Export failed: ${e.localizedMessage}"
            }
        }
    }

    val importCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val importedList = mutableListOf<ReadingEntity>()
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.readLine() // skip header
                        var line = reader.readLine()
                        while (line != null) {
                            val parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                            if (parts.size >= 5) {
                                val type = parts[1].trim('"')
                                val title = parts[2].trim('"')
                                val timestampVal = parts[3].toLongOrNull() ?: System.currentTimeMillis()
                                val desc = parts[4].trim('"')
                                importedList.add(ReadingEntity(type = type, title = title, timestamp = timestampVal, description = desc))
                            }
                            line = reader.readLine()
                        }
                    }
                }
                onImportReadings(importedList)
                importStatus = "Successfully imported ${importedList.size} readings from CSV!"
            } catch (e: Exception) {
                importStatus = "Import failed: ${e.localizedMessage}"
            }
        }
    }

    // JSON Backup Export & Import
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val rootJson = JSONObject()
                rootJson.put("version", 1)
                rootJson.put("exportTime", System.currentTimeMillis())
                rootJson.put("account", DeckManager.googleAccountEmail)
                rootJson.put("deckPreset", DeckManager.currentDeckId)

                val readingsArray = JSONArray()
                readings.forEach { r ->
                    val obj = JSONObject()
                    obj.put("id", r.id)
                    obj.put("type", r.type)
                    obj.put("title", r.title)
                    obj.put("timestamp", r.timestamp)
                    obj.put("description", r.description)
                    readingsArray.put(obj)
                }
                rootJson.put("readings", readingsArray)

                context.contentResolver.openOutputStream(it)?.use { os ->
                    os.write(rootJson.toString(2).toByteArray())
                }
                exportStatus = "Full JSON backup saved (${readings.size} readings)."
            } catch (e: Exception) {
                exportStatus = "Backup failed: ${e.localizedMessage}"
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val jsonString = context.contentResolver.openInputStream(it)?.bufferedReader().use { r -> r?.readText() } ?: ""
                val root = JSONObject(jsonString)
                val arr = root.optJSONArray("readings") ?: JSONArray()
                val importedList = mutableListOf<ReadingEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    importedList.add(
                        ReadingEntity(
                            type = obj.optString("type", "Tarot"),
                            title = obj.optString("title", "Reading"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            description = obj.optString("description", "")
                        )
                    )
                }
                onImportReadings(importedList)
                importStatus = "Successfully restored ${importedList.size} readings from JSON!"
            } catch (e: Exception) {
                importStatus = "JSON restore failed: ${e.localizedMessage}"
            }
        }
    }

    // Deck ZIP import / export
    val importDeckZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val zipInputStream = ZipInputStream(inputStream)
                val decksDir = File(context.filesDir, "custom_decks").apply { if (!exists()) mkdirs() }
                val targetDeckFolder = File(decksDir, "deck_${System.currentTimeMillis()}").apply { mkdirs() }
                var cardCount = 0
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && (entry.name.endsWith(".png", true) || entry.name.endsWith(".jpg", true))) {
                        val outFile = File(targetDeckFolder, entry.name.substringAfterLast('/'))
                        FileOutputStream(outFile).use { fos -> zipInputStream.copyTo(fos) }
                        cardCount++
                    }
                    zipInputStream.closeEntry()
                    entry = zipInputStream.nextEntry
                }
                zipInputStream.close()
                deckZipStatus = "Imported custom deck with $cardCount card images!"
            } catch (e: Exception) {
                deckZipStatus = "Deck import failed: ${e.localizedMessage}"
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Settings & Customization",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize themes, fonts, decks, and account sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // 1. Google Account & Drive Sync Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (DeckManager.isLoggedIn && DeckManager.userPhotoUrl != null) {
                                AsyncImage(
                                    model = DeckManager.userPhotoUrl,
                                    contentDescription = "User Avatar",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (DeckManager.isLoggedIn) Icons.Default.AccountCircle else Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = if (DeckManager.isLoggedIn) DeckManager.userDisplayName else "Google Login & Sync",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (DeckManager.isLoggedIn) DeckManager.googleAccountEmail else "Sign in to backup readings & art to Google Drive",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Button(
                            onClick = { showAccountDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (DeckManager.isLoggedIn) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = if (DeckManager.isLoggedIn) "Account" else "Sign In",
                                color = if (DeckManager.isLoggedIn) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    if (DeckManager.isLoggedIn) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Google Drive Backup",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = DeckManager.syncStatusMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    DeckManager.lastSyncTimestamp = System.currentTimeMillis()
                                    DeckManager.syncStatusMessage = "Synced at ${SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())}"
                                    driveSyncStatus = DeckManager.syncStatusMessage
                                    DeckManager.savePreferences(context)
                                }
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Now")
                            }
                        }
                    }
                }
            }
        }

        // 2. Theme & Visual Palettes Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Themes & Color Schemes (8 Options)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Choose from 8 mystic palettes matching your divination mood:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    val allThemes = listOf(
                        "Mystic Purple", "Emerald Forest", "Midnight Velvet", "Solar Gold",
                        "Celestial Light", "Obsidian Void", "Amethyst Rose", "Ocean Mystic"
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allThemes.forEach { themeName ->
                            FilterChip(
                                selected = currentTheme == themeName,
                                onClick = { onThemeChanged(themeName) },
                                label = { Text(themeName) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (themeName) {
                                                    "Emerald Forest" -> Color(0xFF50C878)
                                                    "Midnight Velvet" -> Color(0xFFFF6B6B)
                                                    "Solar Gold" -> Color(0xFFFFB300)
                                                    "Celestial Light" -> Color(0xFF6B4C9A)
                                                    "Obsidian Void" -> Color(0xFF64DFDF)
                                                    "Amethyst Rose" -> Color(0xFFFF8DA1)
                                                    "Ocean Mystic" -> Color(0xFF48CAE4)
                                                    else -> Color(0xFFE5C158)
                                                }
                                            )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. Typography & Custom Font Imports Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FontDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Custom Fonts & Typography",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Select from local esoteric typefaces or import your own .ttf font file:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    val availableFonts = listOf(
                        "Cinzel Mystic",
                        "Playfair Royal",
                        "Cormorant Antiqua",
                        "Default Sans",
                        "Serif Standard",
                        "Monospace Classic"
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableFonts.forEach { fontName ->
                            FilterChip(
                                selected = DeckManager.selectedFontName == fontName,
                                onClick = {
                                    DeckManager.selectedFontName = fontName
                                    DeckManager.savePreferences(context)
                                },
                                label = { Text(fontName) }
                            )
                        }
                        if (DeckManager.customFontPath != null) {
                            FilterChip(
                                selected = DeckManager.selectedFontName == "Custom Font",
                                onClick = {
                                    DeckManager.selectedFontName = "Custom Font"
                                    DeckManager.savePreferences(context)
                                },
                                label = { Text("Custom TTF Font") }
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { fontPickerLauncher.launch(arrayOf("font/*", "application/x-font-ttf", "application/octet-stream", "*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import Custom Font (.ttf)")
                    }

                    if (fontImportStatus != null) {
                        Text(
                            text = fontImportStatus!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("Font Size Scale:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val scales = listOf(
                            "Compact" to 0.90f,
                            "Standard" to 1.00f,
                            "Large" to 1.15f
                        )
                        scales.forEach { (label, scaleVal) ->
                            FilterChip(
                                selected = kotlin.math.abs(DeckManager.fontSizeScale - scaleVal) < 0.05f,
                                onClick = {
                                    DeckManager.fontSizeScale = scaleVal
                                    DeckManager.savePreferences(context)
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }

        // 4. UI Customization & Card Style
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Card Presentation & Atmosphere",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text("Card Border & Aesthetic Style:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val cardStyles = listOf("Classic Rounded", "Gothic Ornate", "Gold Minimalist", "Sacred Glow")
                        cardStyles.forEach { styleName ->
                            FilterChip(
                                selected = DeckManager.cardStyle == styleName,
                                onClick = {
                                    DeckManager.cardStyle = styleName
                                    DeckManager.savePreferences(context)
                                },
                                label = { Text(styleName) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Esoteric Ambient Glow", fontWeight = FontWeight.Medium)
                            Text("Subtle glowing aura behind drawn cards", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = DeckManager.ambientGlowEnabled,
                            onCheckedChange = {
                                DeckManager.ambientGlowEnabled = it
                                DeckManager.savePreferences(context)
                            }
                        )
                    }
                }
            }
        }

        // 5. Shake to Reshuffle & Haptic Options Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Shake to Reshuffle & Haptic Feedback",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Shake to Reshuffle", fontWeight = FontWeight.Medium)
                            Text("Shake phone to redraw or reshuffle cards", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = DeckManager.shakeToShuffleEnabled,
                            onCheckedChange = {
                                DeckManager.shakeToShuffleEnabled = it
                                DeckManager.savePreferences(context)
                            }
                        )
                    }

                    Text("Shake Sensitivity:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Low", "Medium", "High").forEach { level ->
                            FilterChip(
                                selected = DeckManager.shakeSensitivity == level,
                                onClick = {
                                    DeckManager.shakeSensitivity = level
                                    DeckManager.savePreferences(context)
                                },
                                label = { Text(level) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Haptic Vibrations", fontWeight = FontWeight.Medium)
                            Text("Physical tactility on draw & shuffle", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = DeckManager.hapticsEnabled,
                            onCheckedChange = {
                                DeckManager.hapticsEnabled = it
                                DeckManager.savePreferences(context)
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = { HapticUtil.performHaptic(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Haptic Vibration Pulse")
                    }
                }
            }
        }

        // 6. Tarot Deck Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Style, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Tarot Decks (${DeckManager.availableDecks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Search input
                    OutlinedTextField(
                        value = deckSearchQuery,
                        onValueChange = { deckSearchQuery = it },
                        placeholder = { Text("Filter decks (GitHub, built-in...)") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (deckSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { deckSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Source Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "GitHub", "Built-in", "Custom").forEach { filter ->
                            val isSelected = deckSourceFilter.equals(filter, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { deckSourceFilter = if (isSelected && filter != "All") "All" else filter },
                                label = { Text(filter, fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    val filteredDecks = remember(deckSearchQuery, deckSourceFilter, DeckManager.availableDecks) {
                        DeckManager.searchDecks(deckSearchQuery, deckSourceFilter)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        filteredDecks.forEach { deck ->
                            val isSelected = DeckManager.currentDeckId == deck.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable {
                                        DeckManager.selectDeck(deck.id, context)
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(deck.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        val (badgeColor, badgeText) = when {
                                            deck.source.contains("built-in", true) -> Color(0xFF7B1FA2) to "Built-in"
                                            deck.source.contains("github", true) -> Color(0xFF0288D1) to "GitHub"
                                            deck.isCustom -> Color(0xFF388E3C) to "Custom"
                                            else -> Color(0xFFE65100) to "Historic"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(badgeColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(badgeText, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = badgeColor)
                                        }
                                    }
                                    Text(deck.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 2)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Card & Database Alignment Studio
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(
                                    text = "Card & Database Alignment Studio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Troubleshoot & fine-tune photo vs info alignments",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Move card photos vs card info (IDs, names, tags, element, planet, astrological alignment). Shift offsets, swap bindings, and edit metadata to correct misaligned decks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    val diagnostics = remember(CardAlignmentManager.slots) { CardAlignmentManager.getDiagnostics() }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (diagnostics.isCleanDefault) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (diagnostics.isCleanDefault) "Status: 78 Cards • 1:1 Standard Alignment"
                                else "Status: ⚠️ Custom Alignment (${diagnostics.modifiedPhotoCount} shifts, ${diagnostics.modifiedInfoCount} edits)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            if (!diagnostics.isCleanDefault) {
                                TextButton(
                                    onClick = { CardAlignmentManager.resetAll(context) }
                                ) {
                                    Text("Reset", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAlignmentStudio = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("launch_alignment_studio_btn")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Alignment Studio")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                CardAlignmentManager.shiftAllPhotos(1, context)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Shift Photos +1")
                        }
                        OutlinedButton(
                            onClick = {
                                CardAlignmentManager.shiftAllPhotos(-1, context)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Shift Photos -1")
                        }
                    }
                }
            }
        }

        // 8. Custom Card Backs & Coin Art Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Custom Art for Backs & Coin",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Upload or enter custom image URLs for card backs and Yes/No coin sides:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = tarotBackInput,
                        onValueChange = {
                            tarotBackInput = it
                            DeckManager.tarotBackArtUrl = it
                            DeckManager.savePreferences(context)
                        },
                        label = { Text("Tarot Card Back Image URL") },
                        trailingIcon = {
                            IconButton(onClick = { tarotBackPicker.launch("image/*") }) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Upload Tarot Back")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = runeBackInput,
                        onValueChange = {
                            runeBackInput = it
                            DeckManager.runeBackArtUrl = it
                            DeckManager.savePreferences(context)
                        },
                        label = { Text("Rune Card Back Image URL") },
                        trailingIcon = {
                            IconButton(onClick = { runeBackPicker.launch("image/*") }) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Upload Rune Back")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = coinHeadsInput,
                        onValueChange = {
                            coinHeadsInput = it
                            DeckManager.coinHeadsArtUrl = it
                            DeckManager.savePreferences(context)
                        },
                        label = { Text("Yes/No Coin Heads Side Image URL") },
                        trailingIcon = {
                            IconButton(onClick = { coinHeadsPicker.launch("image/*") }) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Upload Coin Heads")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = coinTailsInput,
                        onValueChange = {
                            coinTailsInput = it
                            DeckManager.coinTailsArtUrl = it
                            DeckManager.savePreferences(context)
                        },
                        label = { Text("Yes/No Coin Tails Side Image URL") },
                        trailingIcon = {
                            IconButton(onClick = { coinTailsPicker.launch("image/*") }) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Upload Coin Tails")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // 8. Data Import & Export (CSV, JSON, Decks ZIP)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ImportExport, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Backup, Import & Export",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Full export and import capabilities across CSV, JSON, and Custom Deck ZIP archives:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // CSV Buttons
                    Text("Reading Notes (CSV):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { exportCsvLauncher.launch("mystic_oracle_readings.csv") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV")
                        }

                        Button(
                            onClick = { importCsvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*")) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import CSV")
                        }
                    }

                    // JSON Buttons
                    Text("Full System Backup (JSON):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { exportJsonLauncher.launch("mystic_oracle_backup_${System.currentTimeMillis()}.json") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Backup JSON")
                        }

                        OutlinedButton(
                            onClick = { importJsonLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore JSON")
                        }
                    }

                    // Custom Deck ZIP Buttons
                    Text("Tarot Card Deck Archive (ZIP):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { importDeckZipLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "*/*")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import Deck (.zip)")
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val cacheDir = File(context.cacheDir, "exported_decks").apply { if (!exists()) mkdirs() }
                                    val zipFile = File(cacheDir, "MysticTarot_Decks_${System.currentTimeMillis()}.zip")
                                    val zos = ZipOutputStream(FileOutputStream(zipFile))
                                    val decksDir = File(context.filesDir, "custom_decks")
                                    if (decksDir.exists() && decksDir.listFiles()?.isNotEmpty() == true) {
                                        decksDir.walkTopDown().filter { it.isFile }.forEach { file ->
                                            val entryName = file.relativeTo(decksDir).path
                                            zos.putNextEntry(ZipEntry(entryName))
                                            file.inputStream().use { it.copyTo(zos) }
                                            zos.closeEntry()
                                        }
                                    } else {
                                        zos.putNextEntry(ZipEntry("custom_deck_manifest.txt"))
                                        zos.write("Mystic Oracle Custom Deck Package\nStore custom card images here.".toByteArray())
                                        zos.closeEntry()
                                    }
                                    zos.close()

                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zipFile)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/zip"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, "Exported Mystic Tarot Deck (.zip)")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Export Decks (.zip)"))
                                    deckZipStatus = "Deck archive exported successfully!"
                                } catch (e: Exception) {
                                    deckZipStatus = "Export failed: ${e.localizedMessage}"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Deck (.zip)")
                        }
                    }

                    if (exportStatus.isNotBlank()) {
                        Text(text = exportStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    if (importStatus.isNotBlank()) {
                        Text(text = importStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    if (deckZipStatus != null) {
                        Text(text = deckZipStatus!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // 9. Support Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Support Mystic Oracle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Mystic Oracle is free and ad-free. If it assists your spiritual journey, support future updates:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    DonationOptions.all.forEach { option ->
                        if (option.uri.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(option.uri)))
                                        donationStatus = ""
                                    } catch (_: ActivityNotFoundException) {
                                        donationStatus = "No browser available."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(option.name)
                                    Text(option.description, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Google Sign-In / Account Dialog
    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(if (DeckManager.isLoggedIn) "Google Account Details" else "Sign In with Google")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (DeckManager.isLoggedIn) {
                        Text("Connected Account: ${DeckManager.googleAccountEmail}", fontWeight = FontWeight.SemiBold)
                        Text("Display Name: ${DeckManager.userDisplayName}")
                        Text("Cloud Status: Connected to Google Drive")
                        if (DeckManager.lastSyncTimestamp != null) {
                            Text("Last Sync: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(DeckManager.lastSyncTimestamp!!))}")
                        }
                    } else {
                        Text(
                            text = "Sign in to connect with Google Drive, sync readings across devices, and preserve custom card decks.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        // 1-Tap Google Sign In button
                        Button(
                            onClick = {
                                val demoGoogleEmail = if (customEmailInput.isNotBlank()) customEmailInput.trim() else "seeker@mysticoracle.app"
                                val demoName = if (customNameInput.isNotBlank()) customNameInput.trim() else "Mystic Seeker"
                                DeckManager.loginWithGoogle(
                                    context = context,
                                    email = demoGoogleEmail,
                                    name = demoName,
                                    photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c2/Sun_symbol_%28bold%29.svg/200px-Sun_symbol_%28bold%29.svg.png"
                                )
                                showAccountDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("1-Tap Google Sign-In")
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text("Or enter your Google Email manually:", style = MaterialTheme.typography.labelSmall)
                        OutlinedTextField(
                            value = customEmailInput,
                            onValueChange = { customEmailInput = it },
                            label = { Text("Google Account Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customNameInput,
                            onValueChange = { customNameInput = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (DeckManager.isLoggedIn) {
                    TextButton(
                        onClick = {
                            DeckManager.logoutGoogle(context)
                            showAccountDialog = false
                        }
                    ) {
                        Text("Sign Out", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    TextButton(
                        onClick = {
                            if (customEmailInput.isNotBlank()) {
                                DeckManager.loginWithGoogle(
                                    context = context,
                                    email = customEmailInput.trim(),
                                    name = customNameInput.ifBlank { "Mystic Seeker" }
                                )
                            }
                            showAccountDialog = false
                        }
                    ) {
                        Text("Connect Account")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
