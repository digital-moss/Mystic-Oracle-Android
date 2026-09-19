package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.DeckManager
import com.example.data.TarotDeckPreset
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotDeckBrowserView(
    onDismissOrBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSourceFilter by remember { mutableStateOf("All") }
    var showImportDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // ZIP File Picker
    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val zipInputStream = ZipInputStream(inputStream)
                    val decksDir = File(context.filesDir, "custom_decks")
                    if (!decksDir.exists()) decksDir.mkdirs()

                    val deckFolder = File(decksDir, "deck_${System.currentTimeMillis()}")
                    deckFolder.mkdirs()

                    var count = 0
                    var zipEntry = zipInputStream.nextEntry
                    while (zipEntry != null) {
                        if (!zipEntry.isDirectory && (zipEntry.name.endsWith(".png", true) || zipEntry.name.endsWith(".jpg", true))) {
                            val outFile = File(deckFolder, zipEntry.name.substringAfterLast('/'))
                            FileOutputStream(outFile).use { fos ->
                                zipInputStream.copyTo(fos)
                            }
                            count++
                        }
                        zipInputStream.closeEntry()
                        zipEntry = zipInputStream.nextEntry
                    }
                    zipInputStream.close()

                    if (count > 0) {
                        DeckManager.importCustomDeck(
                            name = "Imported Deck (${deckFolder.name.takeLast(6)})",
                            description = "Custom deck containing $count card images imported from local ZIP archive.",
                            source = "Custom ZIP",
                            sampleImageUrl = "file://${File(deckFolder, deckFolder.list()?.firstOrNull() ?: "").absolutePath}",
                            context = context
                        )
                        statusMessage = "Successfully imported deck with $count cards!"
                    } else {
                        statusMessage = "No PNG or JPG images found in ZIP."
                    }
                }
            } catch (e: Exception) {
                statusMessage = "ZIP Import error: ${e.localizedMessage}"
            }
        }
    }

    val sourceFilters = remember {
        listOf("All", "alabe.com/tarot", "GitHub Repos", "Historical", "My Custom")
    }

    val displayedDecks = remember(searchQuery, selectedSourceFilter, DeckManager.availableDecks) {
        DeckManager.searchDecks(searchQuery, selectedSourceFilter)
    }

    val activeDeck = DeckManager.availableDecks.find { it.id == DeckManager.currentDeckId }
        ?: DeckManager.builtInDecks.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("tarot_deck_browser_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search decks: GitHub, alabe.com, historic...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search decks",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.testTag("clear_deck_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear deck search"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("deck_search_bar")
        )

        // Source Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            sourceFilters.forEach { filter ->
                val isSelected = selectedSourceFilter.equals(filter, ignoreCase = true)
                val icon = when (filter) {
                    "alabe.com/tarot" -> "🔮"
                    "GitHub Repos" -> "🐙"
                    "Historical" -> "🏛️"
                    "My Custom" -> "📂"
                    else -> "✨"
                }

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedSourceFilter = if (isSelected && filter != "All") "All" else filter
                    },
                    label = { Text("$icon $filter") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_deck_source_${filter.replace(" ", "_").lowercase()}")
                )
            }
        }

        // Active Deck Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp, 66.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    AsyncImage(
                        model = activeDeck.sampleImageUrl,
                        contentDescription = activeDeck.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Active Deck",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = activeDeck.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Source: ${activeDeck.source} • ${activeDeck.cardCount} cards",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showImportDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_import_custom_deck")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import", fontSize = 12.sp)
                }
            }
        }

        // Status Message Banner if any
        AnimatedVisibility(visible = statusMessage != null) {
            statusMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { statusMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Decks List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Quick action: ZIP Import & Export row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { zipPickerLauncher.launch("application/zip") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_import_zip_deck")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import ZIP Deck", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_import_github_url")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GitHub / Web URL", fontSize = 12.sp)
                    }
                }
            }

            if (displayedDecks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "No Decks Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try searching 'alabe', 'github', or clearing your query.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(displayedDecks, key = { it.id }) { deck ->
                    DeckCardItem(
                        deck = deck,
                        isActive = deck.id == DeckManager.currentDeckId,
                        onSelect = {
                            DeckManager.selectDeck(deck.id, context)
                            statusMessage = "Activated deck: ${deck.name}"
                        },
                        onOpenUrl = { url ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open browser: $url", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = if (deck.isCustom) {
                            {
                                DeckManager.deleteCustomDeck(deck.id, context)
                                statusMessage = "Deleted custom deck: ${deck.name}"
                            }
                        } else null
                    )
                }
            }
        }
    }

    // Dialog for Importing Custom Decks from GitHub or www.alabe.com/tarot
    if (showImportDialog) {
        ImportDeckDialog(
            onDismiss = { showImportDialog = false },
            onImport = { name, desc, source, url ->
                val imported = DeckManager.importCustomDeck(
                    name = name,
                    description = desc,
                    source = source,
                    repoUrl = if (source.contains("github", true)) url else null,
                    websiteUrl = if (source.contains("alabe", true) || url.contains("alabe", true)) url else null,
                    context = context
                )
                showImportDialog = false
                statusMessage = "Imported & activated '${imported.name}'!"
            }
        )
    }
}

@Composable
private fun DeckCardItem(
    deck: TarotDeckPreset,
    isActive: Boolean,
    onSelect: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("deck_item_${deck.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Deck Preview Image
            Box(
                modifier = Modifier
                    .size(60.dp, 90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = deck.sampleImageUrl,
                    contentDescription = deck.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = deck.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Source & Author Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (badgeText, badgeColor) = when {
                        deck.source.contains("alabe", true) -> "www.alabe.com/tarot" to Color(0xFF7B1FA2)
                        deck.source.contains("github", true) -> "GitHub Repo" to Color(0xFF0288D1)
                        deck.isCustom -> "Custom Deck" to Color(0xFF388E3C)
                        else -> "Historical" to Color(0xFFE65100)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                            color = badgeColor
                        )
                    }

                    Text(
                        text = "• ${deck.author}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Deck Description
                Text(
                    text = deck.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isActive) {
                        Button(
                            onClick = onSelect,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_select_deck_${deck.id}")
                        ) {
                            Text("Select Deck", fontSize = 12.sp)
                        }
                    }

                    deck.websiteUrl?.let { url ->
                        OutlinedButton(
                            onClick = { onOpenUrl(url) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Visit website",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("alabe.com", fontSize = 11.sp)
                        }
                    }

                    deck.repoUrl?.let { url ->
                        OutlinedButton(
                            onClick = { onOpenUrl(url) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "View repo",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GitHub", fontSize = 11.sp)
                        }
                    }

                    onDelete?.let { delAction ->
                        IconButton(
                            onClick = delAction,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete custom deck",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportDeckDialog(
    onDismiss: () -> Unit,
    onImport: (name: String, desc: String, source: String, url: String) -> Unit
) {
    var deckName by remember { mutableStateOf("") }
    var deckDesc by remember { mutableStateOf("") }
    var deckSource by remember { mutableStateOf("GitHub") }
    var deckUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Import Custom Tarot Deck",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Import from GitHub repositories or select from online archives at www.alabe.com/tarot.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Preset Suggestions
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            deckName = "Astrolabe Albano-Waite"
                            deckDesc = "Astrolabe vibrant colorized tarot deck from alabe.com with astrological decanate associations."
                            deckSource = "www.alabe.com/tarot"
                            deckUrl = "https://www.alabe.com/tarot"
                        },
                        label = { Text("alabe.com/tarot", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = {
                            deckName = "metabismuth/tarot-json"
                            deckDesc = "Rider-Waite-Smith dataset with 350x600px scans and full JSON metadata from GitHub."
                            deckSource = "GitHub"
                            deckUrl = "https://github.com/metabismuth/tarot-json"
                        },
                        label = { Text("metabismuth GitHub", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = {
                            deckName = "mixvlad/TarotCards"
                            deckDesc = "Open source multi-deck archive including historic public-domain decks."
                            deckSource = "GitHub"
                            deckUrl = "https://github.com/mixvlad/TarotCards"
                        },
                        label = { Text("mixvlad GitHub", fontSize = 11.sp) }
                    )
                }

                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text("Deck Name") },
                    placeholder = { Text("e.g. Astrolabe Albano or Custom RWS") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deckUrl,
                    onValueChange = {
                        deckUrl = it
                        if (it.contains("alabe", true)) {
                            deckSource = "www.alabe.com/tarot"
                        } else if (it.contains("github", true)) {
                            deckSource = "GitHub"
                        }
                    },
                    label = { Text("GitHub Repo / Source URL") },
                    placeholder = { Text("e.g. https://github.com/owner/repo or https://www.alabe.com/tarot") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deckDesc,
                    onValueChange = { deckDesc = it },
                    label = { Text("Description & Correspondences") },
                    placeholder = { Text("Astrological decans, art style, historical origin...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = deckName.ifBlank { "Custom Deck" }
                    val finalDesc = deckDesc.ifBlank { "Custom deck imported from $deckSource" }
                    onImport(finalName, finalDesc, deckSource, deckUrl)
                },
                enabled = deckName.isNotBlank() || deckUrl.isNotBlank()
            ) {
                Text("Import & Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
