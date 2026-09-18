package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.TarotCard
import coil.compose.AsyncImage
import com.example.network.TarotImageRepository
import com.example.util.ImageSaver
import kotlinx.coroutines.launch

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
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var symbologyTags by remember { mutableStateOf(card.symbology.toMutableList()) }
    var commonMeaningsTags by remember { mutableStateOf(card.defaultTerms.toMutableList()) }

    var newSymbolInput by remember { mutableStateOf("") }
    var newMeaningInput by remember { mutableStateOf("") }

    var showSymbolDialog by remember { mutableStateOf(false) }
    var showMeaningDialog by remember { mutableStateOf(false) }
    var showImageSwapDialog by remember { mutableStateOf(false) }
    var showFullCardDialog by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }
    var currentImageUrl by remember { mutableStateOf(TarotImageRepository.getCardImageUrl(card.name)) }

    var isReversed by remember { mutableStateOf(false) }
    var isFlipped by remember { mutableStateOf(false) }
    var isSavingCard by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current.density

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val uriStr = uri.toString()
            TarotImageRepository.setCardImageUrl(card.name, uriStr)
            currentImageUrl = uriStr
        }
    }

    // Spring animation for reversal: flips a little past 180 degrees and settles back into place at 180 degrees
    val rotationZ by animateFloatAsState(
        targetValue = if (isReversed) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "reversalRotation"
    )

    val flipRotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "cardFlip"
    )

    if (showFullCardDialog) {
        ZoomableCardDialog(
            cardName = card.name,
            imageUrl = currentImageUrl,
            fallbackResId = getCardImageRes(card),
            isReversedInitially = isReversed,
            subtitle = "${card.arcana} Arcana • ${card.element} • ${card.planet}",
            onDismiss = { showFullCardDialog = false }
        )
    }

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Orientation: ${if (isReversed) "Reversed" else "Upright"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            FilterChip(
                                selected = isReversed,
                                onClick = { isReversed = !isReversed },
                                label = { Text(if (isReversed) "Set Upright" else "Reverse Card") }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .graphicsLayer(
                                    rotationZ = rotationZ,
                                    rotationY = flipRotationY,
                                    cameraDistance = 12f * density
                                )
                                .clickable {
                                    showFullCardDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (flipRotationY > 90f) {
                                // Card back view when flipped
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (com.example.data.DeckManager.tarotBackArtUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = com.example.data.DeckManager.tarotBackArtUrl,
                                            contentDescription = "Card Back",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = "Mystic Tarot\nTap to View",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                AsyncImage(
                                    model = currentImageUrl,
                                    contentDescription = card.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit,
                                    placeholder = painterResource(id = getCardImageRes(card)),
                                    error = painterResource(id = getCardImageRes(card))
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { isFlipped = !isFlipped },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isFlipped) "Face" else "Back")
                            }

                            FilledTonalButton(
                                onClick = { showFullCardDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Zoom & Pan")
                            }
                        }

                        // Download & Share Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!isSavingCard) {
                                        isSavingCard = true
                                        coroutineScope.launch {
                                            ImageSaver.downloadCardImage(context, currentImageUrl, card.name, getCardImageRes(card))
                                            isSavingCard = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSavingCard
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isSavingCard) "Saving..." else "Download")
                            }

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        ImageSaver.shareCardImage(context, currentImageUrl, card.name, getCardImageRes(card))
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share")
                            }
                        }

                        Text(
                            text = card.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import Art")
                            }

                            OutlinedButton(
                                onClick = { showImageSwapDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Presets/URL")
                            }
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
                    for (preset in TarotImageRepository.presetCardImages) {
                        TextButton(
                            onClick = {
                                TarotImageRepository.setCardImageUrl(card.name, preset.second)
                                currentImageUrl = preset.second
                                showImageSwapDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(preset.first)
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
