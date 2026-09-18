package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CardAlignmentManager
import com.example.data.CardAlignmentSlot
import com.example.network.TarotImageRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CardAlignmentStudioScreen(
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog States
    var slotForEdit by remember { mutableStateOf<CardAlignmentSlot?>(null) }
    var slotForSwapPhoto by remember { mutableStateOf<CardAlignmentSlot?>(null) }
    var slotForSwapInfo by remember { mutableStateOf<CardAlignmentSlot?>(null) }
    var slotForCustomPhoto by remember { mutableStateOf<CardAlignmentSlot?>(null) }
    var slotForZoom by remember { mutableStateOf<CardAlignmentSlot?>(null) }
    var showDiagnostics by remember { mutableStateOf(false) }
    var showResetAllConfirm by remember { mutableStateOf(false) }

    // Filter & Search State
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Major", "Wands", "Cups", "Swords", "Pentacles", "Modified")

    val slots = CardAlignmentManager.slots
    val diagnostics = remember(slots) { CardAlignmentManager.getDiagnostics() }

    // Export & Import Launchers
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val json = CardAlignmentManager.exportAlignmentJson()
                context.contentResolver.openOutputStream(it)?.use { os ->
                    os.write(json.toByteArray())
                }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Alignment matrix saved successfully")
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Export failed: ${e.localizedMessage}")
                }
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val json = context.contentResolver.openInputStream(it)?.bufferedReader().use { r -> r?.readText() } ?: ""
                val success = CardAlignmentManager.importAlignmentJson(json, context)
                coroutineScope.launch {
                    if (success) snackbarHostState.showSnackbar("Alignment matrix restored from file")
                    else snackbarHostState.showSnackbar("Failed to parse alignment configuration")
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Import error: ${e.localizedMessage}")
                }
            }
        }
    }

    // Filtered Cards
    val filteredSlots = remember(slots, searchQuery, selectedCategory) {
        slots.filter { slot ->
            val matchesCategory = when (selectedCategory) {
                "Major" -> slot.card.arcana == "Major"
                "Wands" -> slot.card.suit?.contains("Wands", true) == true
                "Cups" -> slot.card.suit?.contains("Cups", true) == true
                "Swords" -> slot.card.suit?.contains("Swords", true) == true
                "Pentacles" -> slot.card.suit?.contains("Pentacles", true) == true
                "Modified" -> slot.photoSourceIndex != slot.slotIndex || !slot.customPhotoUrl.isNullOrBlank()
                else -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim()
                slot.card.name.contains(q, ignoreCase = true) ||
                        slot.slotIndex.toString() == q ||
                        slot.card.element.contains(q, ignoreCase = true) ||
                        slot.card.planet.contains(q, ignoreCase = true) ||
                        slot.card.astrology.contains(q, ignoreCase = true) ||
                        slot.card.symbology.any { it.contains(q, ignoreCase = true) } ||
                        slot.card.defaultTerms.any { it.contains(q, ignoreCase = true) }
            }

            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Card & Database Alignment",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fine-tune photos vs card info correspondences",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Settings")
                    }
                },
                actions = {
                    IconButton(onClick = { showDiagnostics = true }) {
                        Icon(
                            if (diagnostics.isCleanDefault) Icons.Default.CheckCircle else Icons.Default.HealthAndSafety,
                            contentDescription = "Diagnostics",
                            tint = if (diagnostics.isCleanDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = { exportJsonLauncher.launch("mystic_alignment_backup.json") }) {
                        Icon(Icons.Default.Download, contentDescription = "Export Alignment")
                    }
                    IconButton(onClick = { importJsonLauncher.launch(arrayOf("application/json")) }) {
                        Icon(Icons.Default.Upload, contentDescription = "Import Alignment")
                    }
                    IconButton(onClick = { showResetAllConfirm = true }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Alignment")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Batch Troubleshooting Tools Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp)
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
                            Column {
                                Text(
                                    text = "Alignment Troubleshooter",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (diagnostics.isCleanDefault) "Default 1:1 database alignment active"
                                    else "⚠️ Custom mappings: ${diagnostics.modifiedPhotoCount} photo shifts, ${diagnostics.modifiedInfoCount} info edits",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (diagnostics.isCleanDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                            AssistChip(
                                onClick = { showDiagnostics = true },
                                label = { Text("Diagnostics") },
                                leadingIcon = {
                                    Icon(
                                        if (diagnostics.isCleanDefault) Icons.Default.Done else Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }

                        // Batch Photo Tools
                        Text(
                            text = "Batch Photo Shifts (fixes off-by-one or inverted decks):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    CardAlignmentManager.shiftAllPhotos(offset = 1, context = context)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("All card photos shifted +1 slot")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Shift +1")
                            }

                            OutlinedButton(
                                onClick = {
                                    CardAlignmentManager.shiftAllPhotos(offset = -1, context = context)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("All card photos shifted -1 slot")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Shift -1")
                            }

                            OutlinedButton(
                                onClick = {
                                    CardAlignmentManager.invertPhotoOrder(context = context)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Inverted photo order across all cards")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Invert")
                            }
                        }

                        // Secondary Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    CardAlignmentManager.resetAllPhotos(context = context)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("All photos reset to default alignment")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Photos")
                            }

                            TextButton(
                                onClick = {
                                    CardAlignmentManager.resetAllCardInfo(context = context)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("All card info reset to default")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Info")
                            }
                        }
                    }
                }
            }

            // 2. Search & Category Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter by ID, name, element, planet, astrology, tag...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alignment_search_input"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = {
                                    val count = when (cat) {
                                        "All" -> slots.size
                                        "Major" -> slots.count { it.card.arcana == "Major" }
                                        "Wands" -> slots.count { it.card.suit?.contains("Wands", true) == true }
                                        "Cups" -> slots.count { it.card.suit?.contains("Cups", true) == true }
                                        "Swords" -> slots.count { it.card.suit?.contains("Swords", true) == true }
                                        "Pentacles" -> slots.count { it.card.suit?.contains("Pentacles", true) == true }
                                        "Modified" -> diagnostics.modifiedPhotoCount + diagnostics.modifiedInfoCount
                                        else -> 0
                                    }
                                    Text("$cat ($count)")
                                }
                            )
                        }
                    }
                }
            }

            // 3. Card Items List
            items(filteredSlots, key = { it.slotIndex }) { slot ->
                val card = slot.card
                val photoUrl = TarotImageRepository.getCardImageUrl(card.name)
                val isPhotoModified = slot.photoSourceIndex != slot.slotIndex || !slot.customPhotoUrl.isNullOrBlank()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_alignment_row_${slot.slotIndex}"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isPhotoModified) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Slot #${slot.slotIndex}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = if (card.arcana == "Major") "Major Arcana" else "${card.suit}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isPhotoModified) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = "Reassigned Photo",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Dual Visual Panel: Photo vs Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // --- PHOTO SIDE ---
                            Column(
                                modifier = Modifier
                                    .weight(0.38f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 80.dp, height = 120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { slotForZoom = slot }
                                ) {
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Artwork for ${card.name}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(R.drawable.img_tarot_card_face),
                                        error = painterResource(R.drawable.img_tarot_card_face)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.ZoomIn,
                                            contentDescription = "Zoom",
                                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = if (slot.customPhotoUrl != null) "Custom Image"
                                    else CardAlignmentManager.getPhotoSourceCardName(slot),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Photo Control Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    IconButton(
                                        onClick = {
                                            CardAlignmentManager.movePhoto(slot.slotIndex, -1, context)
                                            coroutineScope.launch { snackbarHostState.showSnackbar("Photo moved up") }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move photo up", modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { slotForSwapPhoto = slot },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap photo", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    }

                                    IconButton(
                                        onClick = {
                                            CardAlignmentManager.movePhoto(slot.slotIndex, 1, context)
                                            coroutineScope.launch { snackbarHostState.showSnackbar("Photo moved down") }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move photo down", modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { slotForCustomPhoto = slot },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Custom URL", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            // --- CENTER CONNECTOR ---
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Link,
                                    contentDescription = "Bound to",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "paired",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // --- INFO SIDE ---
                            Column(
                                modifier = Modifier
                                    .weight(0.62f)
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = card.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Badges
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (card.element.isNotBlank()) {
                                        AssistChip(
                                            onClick = { slotForEdit = slot },
                                            label = { Text("💨 ${card.element}", style = MaterialTheme.typography.labelSmall) },
                                            modifier = Modifier.height(26.dp)
                                        )
                                    }
                                    if (card.planet.isNotBlank()) {
                                        AssistChip(
                                            onClick = { slotForEdit = slot },
                                            label = { Text("🪐 ${card.planet}", style = MaterialTheme.typography.labelSmall) },
                                            modifier = Modifier.height(26.dp)
                                        )
                                    }
                                    if (card.astrology.isNotBlank()) {
                                        AssistChip(
                                            onClick = { slotForEdit = slot },
                                            label = { Text("♈ ${card.astrology}", style = MaterialTheme.typography.labelSmall) },
                                            modifier = Modifier.height(26.dp)
                                        )
                                    }
                                }

                                // Tags Preview
                                if (card.defaultTerms.isNotEmpty()) {
                                    Text(
                                        text = "Tags: " + card.defaultTerms.take(3).joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = card.uprightMeaning,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )

                                // Info Controls Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { slotForEdit = slot },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .testTag("edit_info_btn_${slot.slotIndex}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit Info", style = MaterialTheme.typography.labelSmall)
                                    }

                                    OutlinedButton(
                                        onClick = { slotForSwapInfo = slot },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Swap", style = MaterialTheme.typography.labelSmall)
                                    }

                                    IconButton(
                                        onClick = {
                                            CardAlignmentManager.moveCardInfo(slot.slotIndex, -1, context)
                                            coroutineScope.launch { snackbarHostState.showSnackbar("Card metadata moved up") }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move info up", modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            CardAlignmentManager.moveCardInfo(slot.slotIndex, 1, context)
                                            coroutineScope.launch { snackbarHostState.showSnackbar("Card metadata moved down") }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move info down", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- DIALOGS ---

    // 1. Edit Card Info Dialog
    slotForEdit?.let { targetSlot ->
        EditCardInfoDialog(
            slot = targetSlot,
            onDismiss = { slotForEdit = null },
            onSave = { updatedCard ->
                CardAlignmentManager.updateCardInfo(targetSlot.slotIndex, updatedCard, context)
                slotForEdit = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Updated correspondences for ${updatedCard.name}")
                }
            },
            onReset = {
                CardAlignmentManager.resetSlotCardInfo(targetSlot.slotIndex, context)
                slotForEdit = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Reset ${targetSlot.card.name} metadata to default")
                }
            }
        )
    }

    // 2. Swap Photo Dialog
    slotForSwapPhoto?.let { targetSlot ->
        SwapPhotoDialog(
            sourceSlot = targetSlot,
            allSlots = slots,
            onDismiss = { slotForSwapPhoto = null },
            onSwap = { destinationSlotIndex ->
                CardAlignmentManager.swapPhotos(targetSlot.slotIndex, destinationSlotIndex, context)
                slotForSwapPhoto = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Swapped photo binding between #${targetSlot.slotIndex} and #$destinationSlotIndex")
                }
            }
        )
    }

    // 3. Swap Info Dialog
    slotForSwapInfo?.let { targetSlot ->
        SwapInfoDialog(
            sourceSlot = targetSlot,
            allSlots = slots,
            onDismiss = { slotForSwapInfo = null },
            onSwap = { destinationSlotIndex ->
                CardAlignmentManager.swapCardInfo(targetSlot.slotIndex, destinationSlotIndex, context)
                slotForSwapInfo = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Swapped metadata between #${targetSlot.slotIndex} and #$destinationSlotIndex")
                }
            }
        )
    }

    // 4. Custom Photo URL Dialog
    slotForCustomPhoto?.let { targetSlot ->
        CustomPhotoUrlDialog(
            slot = targetSlot,
            onDismiss = { slotForCustomPhoto = null },
            onSave = { customUrl ->
                CardAlignmentManager.setCustomPhoto(targetSlot.slotIndex, customUrl, context)
                slotForCustomPhoto = null
                coroutineScope.launch {
                    if (customUrl != null) snackbarHostState.showSnackbar("Applied custom photo URL to #${targetSlot.slotIndex}")
                    else snackbarHostState.showSnackbar("Cleared custom photo on #${targetSlot.slotIndex}")
                }
            }
        )
    }

    // 5. Diagnostics Dialog
    if (showDiagnostics) {
        AlignmentDiagnosticsDialog(
            diagnostics = diagnostics,
            onDismiss = { showDiagnostics = false }
        )
    }

    // 6. Reset All Confirm Dialog
    if (showResetAllConfirm) {
        AlertDialog(
            onDismissRequest = { showResetAllConfirm = false },
            title = { Text("Reset Entire Alignment Matrix?") },
            text = { Text("This will restore all 78 tarot cards and photo bindings to factory default database correspondences.") },
            confirmButton = {
                Button(
                    onClick = {
                        CardAlignmentManager.resetAll(context)
                        showResetAllConfirm = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("All card photos and correspondences reset to factory defaults")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 7. Zoomable Card Viewer on Tap
    slotForZoom?.let { zoomSlot ->
        val zoomUrl = TarotImageRepository.getCardImageUrl(zoomSlot.card.name)
        ZoomableCardDialog(
            cardName = zoomSlot.card.name,
            imageUrl = zoomUrl,
            fallbackResId = R.drawable.img_tarot_card_face,
            onDismiss = { slotForZoom = null }
        )
    }
}
