package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CardAlignmentSlot
import com.example.model.TarotCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditCardInfoDialog(
    slot: CardAlignmentSlot,
    onDismiss: () -> Unit,
    onSave: (TarotCard) -> Unit,
    onReset: () -> Unit
) {
    val card = slot.card

    var nameInput by remember { mutableStateOf(card.name) }
    var idInput by remember { mutableStateOf(card.id.toString()) }
    var arcanaInput by remember { mutableStateOf(card.arcana) }
    var suitInput by remember { mutableStateOf(card.suit ?: "None") }
    var elementInput by remember { mutableStateOf(card.element) }
    var planetInput by remember { mutableStateOf(card.planet) }
    var astrologyInput by remember { mutableStateOf(card.astrology) }
    var numerologyInput by remember { mutableStateOf(card.numerology) }
    var symbologyInput by remember { mutableStateOf(card.symbology.joinToString(", ")) }
    var termsInput by remember { mutableStateOf(card.defaultTerms.joinToString(", ")) }
    var uprightInput by remember { mutableStateOf(card.uprightMeaning) }
    var reversedInput by remember { mutableStateOf(card.reversedMeaning) }
    var descriptionInput by remember { mutableStateOf(card.description) }

    val quickElements = listOf("Air", "Fire", "Water", "Earth", "Spirit")
    val quickPlanets = listOf("Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto")
    val quickZodiacs = listOf("Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces")
    val suitOptions = listOf("None", "Wands", "Cups", "Swords", "Pentacles")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.92f)
            .testTag("edit_card_info_dialog")
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Edit Card Metadata",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Slot #${slot.slotIndex} • Fine-tune database alignments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Card ID & Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = idInput,
                            onValueChange = { idInput = it },
                            label = { Text("ID / Index") },
                            modifier = Modifier
                                .width(90.dp)
                                .testTag("card_id_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Card Name") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_name_input"),
                            singleLine = true
                        )
                    }

                    // 2. Arcana & Numerology
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Arcana", style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = arcanaInput == "Major",
                                    onClick = { arcanaInput = "Major"; suitInput = "None" },
                                    label = { Text("Major") }
                                )
                                FilterChip(
                                    selected = arcanaInput == "Minor",
                                    onClick = { arcanaInput = "Minor"; if (suitInput == "None") suitInput = "Wands" },
                                    label = { Text("Minor") }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = numerologyInput,
                            onValueChange = { numerologyInput = it },
                            label = { Text("Numerology / Key") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_numerology_input"),
                            singleLine = true
                        )
                    }

                    // 3. Suit (for Minor Arcana)
                    if (arcanaInput == "Minor") {
                        Column {
                            Text("Suit", style = MaterialTheme.typography.labelMedium)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                suitOptions.filter { it != "None" }.forEach { s ->
                                    FilterChip(
                                        selected = suitInput == s,
                                        onClick = { suitInput = s },
                                        label = { Text(s) }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Element
                    Column {
                        Text("Element", style = MaterialTheme.typography.labelMedium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            quickElements.forEach { el ->
                                FilterChip(
                                    selected = elementInput.equals(el, ignoreCase = true),
                                    onClick = { elementInput = el },
                                    label = { Text(el) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = elementInput,
                            onValueChange = { elementInput = it },
                            label = { Text("Custom Element") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_element_input"),
                            singleLine = true
                        )
                    }

                    // 5. Planet
                    Column {
                        Text("Planet Correspondence", style = MaterialTheme.typography.labelMedium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            quickPlanets.forEach { p ->
                                FilterChip(
                                    selected = planetInput.contains(p, ignoreCase = true),
                                    onClick = { planetInput = p },
                                    label = { Text(p) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = planetInput,
                            onValueChange = { planetInput = it },
                            label = { Text("Planet Alignment") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_planet_input"),
                            singleLine = true
                        )
                    }

                    // 6. Astrological Alignment
                    Column {
                        Text("Astrological Alignment", style = MaterialTheme.typography.labelMedium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            quickZodiacs.forEach { z ->
                                FilterChip(
                                    selected = astrologyInput.contains(z, ignoreCase = true),
                                    onClick = { astrologyInput = z },
                                    label = { Text(z) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = astrologyInput,
                            onValueChange = { astrologyInput = it },
                            label = { Text("Astrological Correspondence") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_astrology_input"),
                            singleLine = true
                        )
                    }

                    // 7. Symbology Tags
                    OutlinedTextField(
                        value = symbologyInput,
                        onValueChange = { symbologyInput = it },
                        label = { Text("Symbology Tags (comma-separated)") },
                        supportingText = { Text("e.g., White Dog, Cliff, Sun, Infinite Altar") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_symbology_input"),
                        minLines = 2
                    )

                    // 8. Key Meaning Terms / Tags
                    OutlinedTextField(
                        value = termsInput,
                        onValueChange = { termsInput = it },
                        label = { Text("Keyword Terms / Tags (comma-separated)") },
                        supportingText = { Text("e.g., Beginnings, Spontaneity, Potential, Intuition") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_terms_input"),
                        minLines = 2
                    )

                    // 9. Upright & Reversed Meanings
                    OutlinedTextField(
                        value = uprightInput,
                        onValueChange = { uprightInput = it },
                        label = { Text("Upright Meaning") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = reversedInput,
                        onValueChange = { reversedInput = it },
                        label = { Text("Reversed Meaning") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    // 10. Description
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Visual Archetype Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onReset,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Default")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val parsedId = idInput.trim().toIntOrNull() ?: slot.slotIndex
                                val updatedCard = card.copy(
                                    id = parsedId,
                                    name = nameInput.trim(),
                                    arcana = arcanaInput.trim(),
                                    suit = if (arcanaInput == "Major" || suitInput == "None") null else suitInput.trim(),
                                    element = elementInput.trim(),
                                    planet = planetInput.trim(),
                                    astrology = astrologyInput.trim(),
                                    numerology = numerologyInput.trim(),
                                    symbology = symbologyInput.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    defaultTerms = termsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    uprightMeaning = uprightInput.trim(),
                                    reversedMeaning = reversedInput.trim(),
                                    description = descriptionInput.trim()
                                )
                                onSave(updatedCard)
                            },
                            modifier = Modifier.testTag("save_card_info_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Alignment")
                        }
                    }
                }
            }
        }
    }
}
