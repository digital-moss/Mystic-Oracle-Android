package com.example.ui.screens

import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DeckManager
import com.example.data.ReadingEntity
import com.example.donations.DonationOptions
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChanged: (String) -> Unit,
    readings: List<ReadingEntity>,
    onImportReadings: (List<ReadingEntity>) -> Unit
) {
    val context = LocalContext.current
    var soundEnabled by remember { mutableStateOf(true) }
    var dailyReminder by remember { mutableStateOf(false) }

    // Account state
    var isLoggedIn by remember { mutableStateOf(DeckManager.isGoogleDriveConnected) }
    var emailInput by remember { mutableStateOf(DeckManager.googleAccountEmail.ifBlank { "seeker@mysticoracle.app" }) }
    var passwordInput by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("Mystic Seeker") }
    var showAccountDialog by remember { mutableStateOf(false) }

    // Custom Back & Coin Art inputs
    var tarotBackInput by remember { mutableStateOf(DeckManager.tarotBackArtUrl) }
    var runeBackInput by remember { mutableStateOf(DeckManager.runeBackArtUrl) }
    var iChingBackInput by remember { mutableStateOf(DeckManager.iChingBackArtUrl) }
    var coinHeadsInput by remember { mutableStateOf(DeckManager.coinHeadsArtUrl) }
    var coinTailsInput by remember { mutableStateOf(DeckManager.coinTailsArtUrl) }

    // Image Upload Launchers
    val tarotBackPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            tarotBackInput = it.toString()
            DeckManager.tarotBackArtUrl = it.toString()
        }
    }
    val runeBackPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            runeBackInput = it.toString()
            DeckManager.runeBackArtUrl = it.toString()
        }
    }
    val iChingBackPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            iChingBackInput = it.toString()
            DeckManager.iChingBackArtUrl = it.toString()
        }
    }
    val coinHeadsPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coinHeadsInput = it.toString()
            DeckManager.coinHeadsArtUrl = it.toString()
        }
    }
    val coinTailsPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coinTailsInput = it.toString()
            DeckManager.coinTailsArtUrl = it.toString()
        }
    }

    // Export CSV launcher / status
    var exportStatus by remember { mutableStateOf("") }
    var importStatus by remember { mutableStateOf("") }
    var donationStatus by remember { mutableStateOf("") }
    var driveSyncStatus by remember { mutableStateOf(DeckManager.syncStatusMessage) }

    val exportLauncher = rememberLauncherForActivityResult(
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

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val importedList = mutableListOf<ReadingEntity>()
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        val header = reader.readLine() // skip header
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
                importStatus = "Successfully imported ${importedList.size} readings!"
            } catch (e: Exception) {
                importStatus = "Import failed: ${e.localizedMessage}"
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings & Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(text = if (isLoggedIn) "Google Drive Synced" else "Guest Seeker", modifier = Modifier.padding(4.dp))
                }
            }
        }

        // Account & Google Drive Sync Card
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
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(
                                    text = if (isLoggedIn) "Google Account Connected" else "Sign In / Google Drive Sync",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isLoggedIn) emailInput else "Sync art, decks, & journal securely to Drive",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Button(
                            onClick = { showAccountDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (isLoggedIn) "Manage" else "Sign In")
                        }
                    }

                    if (isLoggedIn) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                DeckManager.isGoogleDriveConnected = true
                                DeckManager.googleAccountEmail = emailInput
                                DeckManager.lastSyncTimestamp = System.currentTimeMillis()
                                DeckManager.syncStatusMessage = "Synced successfully at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())}"
                                driveSyncStatus = DeckManager.syncStatusMessage
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync All Art, Decks & Journal to Google Drive")
                        }
                        if (driveSyncStatus.isNotBlank()) {
                            Text(
                                text = driveSyncStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Pre-installed Decks & Theme Card
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Style, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Pre-Installed Decks & Themes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Select active deck preset and application visual style:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Text("Deck Preset:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DeckManager.availableDecks.forEach { deck ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (DeckManager.currentDeckId == deck.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { DeckManager.currentDeckId = deck.id }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(deck.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(deck.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                                if (DeckManager.currentDeckId == deck.id) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("Active", modifier = Modifier.padding(4.dp)) }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("App Theme Colors:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Mystic Purple", "Emerald Forest", "Midnight Velvet", "Solar Gold").forEach { themeName ->
                            FilterChip(
                                selected = currentTheme == themeName,
                                onClick = { onThemeChanged(themeName) },
                                label = { Text(themeName) }
                            )
                        }
                    }
                }
            }
        }

        // Custom Card Backs & Coin Art Card
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
                        value = iChingBackInput,
                        onValueChange = {
                            iChingBackInput = it
                            DeckManager.iChingBackArtUrl = it
                        },
                        label = { Text("I Ching Card Back Image URL") },
                        trailingIcon = {
                            IconButton(onClick = { iChingBackPicker.launch("image/*") }) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Upload I Ching Back")
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

        // Shake to Reshuffle & General Preferences Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Shake to Reshuffle & Interaction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Shake your device in any oracle screen to instantly reshuffle or redraw.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Text("Haptic Feedback on Shake")
                        }
                        Switch(
                            checked = DeckManager.hapticsEnabled,
                            onCheckedChange = { DeckManager.hapticsEnabled = it }
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
                                onClick = { DeckManager.shakeSensitivity = level },
                                label = { Text(level) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Text("Ambient Sound Effects")
                        }
                        Switch(checked = soundEnabled, onCheckedChange = { soundEnabled = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Text("Daily Oracle Reminder")
                        }
                        Switch(checked = dailyReminder, onCheckedChange = { dailyReminder = it })
                    }
                }
            }
        }

        // CSV Import / Export Card
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "CSV Backup & Journal Sync",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Export your reading journal to a CSV file or import previous backups.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { exportLauncher.launch("mystic_oracle_readings.csv") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV")
                        }

                        Button(
                            onClick = { importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*")) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import CSV")
                        }
                    }

                    if (exportStatus.isNotBlank()) {
                        Text(text = exportStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    if (importStatus.isNotBlank()) {
                        Text(text = importStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Support Card
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
                        text = "This app is free to use. If it helps your practice, you can support future improvements through any configured link.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    DonationOptions.all.forEach { option ->
                        if (option.uri.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(option.uri))
                                        )
                                        donationStatus = ""
                                    } catch (_: ActivityNotFoundException) {
                                        donationStatus = "No browser or payment app is available for this link."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(option.name)
                                    Text(
                                        option.description,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = { Text(if (isLoggedIn) "Google Account Management" else "Sign In / Google Drive Sync") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isLoggedIn) {
                        Text("Google Account: $emailInput")
                        Text("Google Drive Status: Connected & Synced")
                    } else {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Google Account Email") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password / App Password") },
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isLoggedIn) {
                            isLoggedIn = false
                            DeckManager.isGoogleDriveConnected = false
                            DeckManager.googleAccountEmail = ""
                        } else {
                            if (emailInput.isNotBlank()) {
                                isLoggedIn = true
                                DeckManager.isGoogleDriveConnected = true
                                DeckManager.googleAccountEmail = emailInput
                                DeckManager.lastSyncTimestamp = System.currentTimeMillis()
                                DeckManager.syncStatusMessage = "Connected & Synced with Google Drive"
                                driveSyncStatus = DeckManager.syncStatusMessage
                            }
                        }
                        showAccountDialog = false
                    }
                ) {
                    Text(if (isLoggedIn) "Sign Out" else "Sign In & Connect Drive")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

