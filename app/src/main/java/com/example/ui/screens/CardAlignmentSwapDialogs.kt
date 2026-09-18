package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.AlignmentDiagnostics
import com.example.data.CardAlignmentManager
import com.example.data.CardAlignmentSlot
import com.example.network.TarotImageRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapPhotoDialog(
    sourceSlot: CardAlignmentSlot,
    allSlots: List<CardAlignmentSlot>,
    onDismiss: () -> Unit,
    onSwap: (targetSlotIndex: Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredSlots = remember(searchQuery, allSlots) {
        if (searchQuery.isBlank()) allSlots.filter { it.slotIndex != sourceSlot.slotIndex }
        else allSlots.filter {
            it.slotIndex != sourceSlot.slotIndex &&
                    (it.card.name.contains(searchQuery, ignoreCase = true) ||
                            it.slotIndex.toString().contains(searchQuery) ||
                            it.card.arcana.contains(searchQuery, ignoreCase = true))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.85f)
            .testTag("swap_photo_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Swap Photo Binding",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select card to swap photo with #${sourceSlot.slotIndex} ${sourceSlot.card.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search card name or index...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSlots, key = { it.slotIndex }) { slot ->
                        val photoUrl = TarotImageRepository.getCardImageUrl(slot.card.name)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSwap(slot.slotIndex) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = slot.card.name,
                                    modifier = Modifier
                                        .size(width = 44.dp, height = 66.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(R.drawable.img_tarot_card_face),
                                    error = painterResource(R.drawable.img_tarot_card_face)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "#${slot.slotIndex} • ${slot.card.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Current Photo: ${CardAlignmentManager.getPhotoSourceCardName(slot)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.SwapHoriz,
                                    contentDescription = "Swap with this card",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapInfoDialog(
    sourceSlot: CardAlignmentSlot,
    allSlots: List<CardAlignmentSlot>,
    onDismiss: () -> Unit,
    onSwap: (targetSlotIndex: Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredSlots = remember(searchQuery, allSlots) {
        if (searchQuery.isBlank()) allSlots.filter { it.slotIndex != sourceSlot.slotIndex }
        else allSlots.filter {
            it.slotIndex != sourceSlot.slotIndex &&
                    (it.card.name.contains(searchQuery, ignoreCase = true) ||
                            it.slotIndex.toString().contains(searchQuery) ||
                            it.card.element.contains(searchQuery, ignoreCase = true) ||
                            it.card.astrology.contains(searchQuery, ignoreCase = true))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.85f)
            .testTag("swap_info_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Swap Metadata Binding",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select card to swap info with #${sourceSlot.slotIndex} ${sourceSlot.card.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, element, zodiac...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSlots, key = { it.slotIndex }) { slot ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSwap(slot.slotIndex) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "#${slot.slotIndex} • ${slot.card.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Element: ${slot.card.element} • Astrology: ${slot.card.astrology} • Planet: ${slot.card.planet}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.SwapVert,
                                    contentDescription = "Swap metadata",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomPhotoUrlDialog(
    slot: CardAlignmentSlot,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    var urlInput by remember { mutableStateOf(slot.customPhotoUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Card Photo URL") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Assign a custom image URL or local URI for #${slot.slotIndex} ${slot.card.name}",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Image URL (https://... or content://)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(urlInput.ifBlank { null }) }) {
                Text("Apply Photo")
            }
        },
        dismissButton = {
            if (slot.customPhotoUrl != null) {
                TextButton(onClick = { onSave(null) }) {
                    Text("Clear Custom Photo", color = MaterialTheme.colorScheme.error)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun AlignmentDiagnosticsDialog(
    diagnostics: AlignmentDiagnostics,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (diagnostics.isCleanDefault) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (diagnostics.isCleanDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text("Database Alignment Health")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Total Card Slots: ${diagnostics.totalSlots}",
                    fontWeight = FontWeight.SemiBold
                )
                Text("• Custom Photo Assignments: ${diagnostics.modifiedPhotoCount}")
                Text("• Modified Metadata Records: ${diagnostics.modifiedInfoCount}")

                if (diagnostics.duplicatePhotoIndices.isNotEmpty()) {
                    Text(
                        text = "⚠️ Duplicate Photo Mappings: Cards with photo sources [${diagnostics.duplicatePhotoIndices.joinToString()}] are assigned more than once.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (diagnostics.orphanedPhotoIndices.isNotEmpty()) {
                    Text(
                        text = "ℹ️ Unused Photo Sources: ${diagnostics.orphanedPhotoIndices.size} photos are currently unmapped.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (diagnostics.missingAttributeWarnings.isNotEmpty()) {
                    Text(
                        text = "Warnings:\n" + diagnostics.missingAttributeWarnings.take(5).joinToString("\n"),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (diagnostics.isCleanDefault) {
                    Text(
                        text = "All 78 tarot cards are mapped 1:1 with standard database correspondences and artwork.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
