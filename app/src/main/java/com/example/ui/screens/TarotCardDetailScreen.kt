package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.TarotCard
import coil.compose.AsyncImage
import com.example.network.TarotImageRepository

fun getCardImageRes(card: TarotCard): Int {
    val name = card.name
    val suit = card.suit
    return when {
        name.contains("Fool", true) -> R.drawable.img_tarot_fool
        name.contains("Magician", true) -> R.drawable.img_tarot_magician
        name.contains("Priestess", true) -> R.drawable.img_tarot_high_priestess
        name.contains("Empress", true) -> R.drawable.img_tarot_empress
        name.contains("Emperor", true) -> R.drawable.img_tarot_emperor
        name.contains("Star", true) -> R.drawable.img_tarot_star
        name.contains("Sun", true) -> R.drawable.img_tarot_sun
        suit?.contains("Wands", true) == true -> R.drawable.img_tarot_wands
        suit?.contains("Cups", true) == true -> R.drawable.img_tarot_cups
        suit?.contains("Swords", true) == true -> R.drawable.img_tarot_swords
        suit?.contains("Pentacles", true) == true -> R.drawable.img_tarot_pentacles
        else -> R.drawable.img_tarot_card_face
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TarotCardDetailScreen(
    card: TarotCard,
    onBack: () -> Unit,
    imageModel: Any? = null
) {
    var symbologyTags by remember { mutableStateOf(card.symbology.toMutableList()) }
    var commonMeaningsTags by remember { mutableStateOf(card.defaultTerms.toMutableList()) }

    var newSymbolInput by remember { mutableStateOf("") }
    var newMeaningInput by remember { mutableStateOf("") }

    var showSymbolDialog by remember { mutableStateOf(false) }
    var showMeaningDialog by remember { mutableStateOf(false) }
    var showImageSwapDialog by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }
    var currentImageUrl by remember { mutableStateOf(TarotImageRepository.getCardImageUrl(card.name)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imageModel ?: currentImageUrl,
                                contentDescription = card.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = getCardImageRes(card)),
                                error = painterResource(id = getCardImageRes(card))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f))
                                    .padding(16.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text(text = "Ryder-Waite Classic • ${card.arcana}")
                                    }
                                    Text(
                                        text = card.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = androidx.compose.ui.graphics.Color.White
                                    )
                                    Text(
                                        text = "Element: ${card.element} | Planet: ${card.planet} | Num: ${card.numerology}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                        Text(
                            text = card.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        OutlinedButton(
                            onClick = { showImageSwapDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Swap Card Image (Custom URL / Preset)")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Esoteric Attributes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AttributeItem("Element", card.element)
                            AttributeItem("Planet", card.planet)
                            AttributeItem("Astrology", card.astrology)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AttributeItem("Numerology", card.numerology)
                            AttributeItem("Arcana", card.arcana)
                            AttributeItem("Suit", card.suit ?: "Major")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Meanings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Upright Meaning:", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text(text = card.uprightMeaning, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Reversed Meaning:", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                        Text(text = card.reversedMeaning, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Commonly Agreed Common Meanings & Keywords
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            Text(text = "Commonly Agreed Meanings & Keywords", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showMeaningDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Meaning Entry", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Text(
                            text = "Generally agreed common meanings for this card. Tap X to remove, or use + to add new entries.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            commonMeaningsTags.forEach { meaning ->
                                InputChip(
                                    selected = true,
                                    onClick = {},
                                    label = { Text(meaning) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove meaning",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    commonMeaningsTags.remove(meaning)
                                                    commonMeaningsTags = ArrayList(commonMeaningsTags)
                                                }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Symbology & Symbol Names Tags
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            Text(text = "Tarot Card Symbol Names & Symbology Tags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showSymbolDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Symbol Tag", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Text(
                            text = "Esoteric imagery tags for this card. Tap X on any tag to remove it, or use + to add custom symbol names.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            symbologyTags.forEach { tag ->
                                InputChip(
                                    selected = true,
                                    onClick = {},
                                    label = { Text(tag) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove symbol tag",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    symbologyTags.remove(tag)
                                                    symbologyTags = ArrayList(symbologyTags)
                                                }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSymbolDialog) {
        AlertDialog(
            onDismissRequest = { showSymbolDialog = false },
            title = { Text("Add Symbol Tag") },
            text = {
                OutlinedTextField(
                    value = newSymbolInput,
                    onValueChange = { newSymbolInput = it },
                    label = { Text("Symbol Name (e.g. White Horse)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSymbolInput.isNotBlank()) {
                            symbologyTags.add(newSymbolInput.trim())
                            symbologyTags = ArrayList(symbologyTags)
                            newSymbolInput = ""
                            showSymbolDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSymbolDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMeaningDialog) {
        AlertDialog(
            onDismissRequest = { showMeaningDialog = false },
            title = { Text("Add Common Meaning") },
            text = {
                OutlinedTextField(
                    value = newMeaningInput,
                    onValueChange = { newMeaningInput = it },
                    label = { Text("Common Meaning / Keyword") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newMeaningInput.isNotBlank()) {
                            commonMeaningsTags.add(newMeaningInput.trim())
                            commonMeaningsTags = ArrayList(commonMeaningsTags)
                            newMeaningInput = ""
                            showMeaningDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMeaningDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showImageSwapDialog) {
        AlertDialog(
            onDismissRequest = { showImageSwapDialog = false },
            title = { Text("Swap Card Image") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter any image URL or choose from preset illustrations to replace this card's image:")
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        label = { Text("Image URL (https://...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Or choose a preset illustration:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    TarotImageRepository.presetCardImages.forEach { (presetName, presetUrl) ->
                        TextButton(
                            onClick = {
                                TarotImageRepository.setCardImageUrl(card.name, presetUrl)
                                currentImageUrl = presetUrl
                                showImageSwapDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(presetName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customUrlInput.isNotBlank()) {
                            TarotImageRepository.setCardImageUrl(card.name, customUrlInput.trim())
                            currentImageUrl = customUrlInput.trim()
                            customUrlInput = ""
                            showImageSwapDialog = false
                        }
                    }
                ) {
                    Text("Apply URL")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            TarotImageRepository.resetCardImageUrl(card.name)
                            currentImageUrl = TarotImageRepository.getCardImageUrl(card.name)
                            showImageSwapDialog = false
                        }
                    ) {
                        Text("Reset Default")
                    }
                    TextButton(onClick = { showImageSwapDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun AttributeItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
