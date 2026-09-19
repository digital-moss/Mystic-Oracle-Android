package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DeckManager
import com.example.data.ReadingEntity
import com.example.model.TarotCard
import com.example.model.TarotData
import com.example.network.TarotImageRepository
import com.example.util.HapticUtil
import com.example.util.ShakeDetector
import coil.compose.AsyncImage
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotScreen(
    onSaveReading: (ReadingEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Simple Draw, 1 = Spreads, 2 = Library, 3 = Decks & Art
    var searchQuery by remember { mutableStateOf("") }
    var activeSpread by remember { mutableStateOf<List<Pair<String, Pair<TarotCard, Boolean>>>?>(null) }
    var spreadType by remember { mutableStateOf("Single Tarot Card Draw") }
    var selectedCardDetail by remember { mutableStateOf<TarotCard?>(null) }
    var fullCardDialogCard by remember { mutableStateOf<TarotCard?>(null) }

    // Simple Draw State
    var simpleDrawCard by remember { mutableStateOf(TarotData.cards.random()) }
    var simpleDrawFlipped by remember { mutableStateOf(false) }
    var simpleDrawReversed by remember { mutableStateOf(false) }

    // Custom decks state
    var importedDecksCount by remember { mutableStateOf(0) }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }

    fun triggerHapticFeedback() {
        HapticUtil.performHaptic(context)
    }

    // Shake to reshuffle for Simple Draw & Readings
    DisposableEffect(DeckManager.shakeToShuffleEnabled) {
        if (!DeckManager.shakeToShuffleEnabled) {
            return@DisposableEffect onDispose {}
        }
        val detector = ShakeDetector(context) {
            triggerHapticFeedback()
            simpleDrawCard = TarotData.cards.random()
            simpleDrawFlipped = false
            simpleDrawReversed = if (DeckManager.noReversals) false else kotlin.random.Random.nextBoolean()
        }
        detector.start()
        onDispose {
            detector.stop()
        }
    }

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
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
                importedDecksCount++
                importStatusMessage = "Successfully imported custom deck with $count card images!"
            } catch (e: Exception) {
                importStatusMessage = "Failed to import zip: ${e.localizedMessage}"
            }
        }
    }

    // Full Card Image Dialog (Interactive Zoomable, Pan, Download & Share)
    if (fullCardDialogCard != null) {
        ZoomableCardDialog(
            cardName = fullCardDialogCard!!.name,
            imageUrl = TarotImageRepository.getCardImageUrl(fullCardDialogCard!!.name),
            fallbackResId = getCardImageRes(fullCardDialogCard!!),
            isReversedInitially = simpleDrawReversed && selectedTab == 0,
            subtitle = "${fullCardDialogCard!!.arcana} Arcana • ${fullCardDialogCard!!.element}",
            onDismiss = { fullCardDialogCard = null }
        )
    }

    if (selectedCardDetail != null) {
        TarotCardDetailScreen(
            card = selectedCardDetail!!,
            onBack = { selectedCardDetail = null }
        )
        return
    }

    if (activeSpread != null) {
        TarotReadingResultScreen(
            spreadType = spreadType,
            cardsWithPositions = activeSpread!!,
            onSaveReading = onSaveReading,
            onSelectCard = { selectedCardDetail = it },
            onContinueDrawing = {
                val nextCard = TarotData.cards.random()
                val isRev = if (DeckManager.noReversals) false else kotlin.random.Random.nextBoolean()
                val currentList = activeSpread.orEmpty().toMutableList()
                currentList.add("Card ${currentList.size + 1}: Continuation" to (nextCard to isRev))
                activeSpread = currentList
            },
            onBack = { activeSpread = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tarot Sanctuary",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Active: ${DeckManager.availableDecks.find { it.id == DeckManager.currentDeckId }?.name ?: "Classic"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Simple Draw") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Spreads") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Card Collection (78)") }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Decks & Art") }
            )
        }

        when (selectedTab) {
            // TAB 0: SIMPLE DRAW
            0 -> {
                val density = androidx.compose.ui.platform.LocalDensity.current.density
                val flipRotationY by animateFloatAsState(
                    targetValue = if (simpleDrawFlipped) 180f else 0f,
                    animationSpec = tween(500, easing = FastOutSlowInEasing),
                    label = "simpleDrawFlip"
                )

                // Spring reversal animation: flips past 180 and settles back
                val reversalRotationZ by animateFloatAsState(
                    targetValue = if (simpleDrawReversed) 180f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "simpleReversalSpring"
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Control Bar: No Reversals Toggle & Reshuffle Hint
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = "Shake",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Shake phone to reshuffle",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "No Reversals",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Switch(
                                        checked = DeckManager.noReversals,
                                        onCheckedChange = { checked ->
                                            DeckManager.noReversals = checked
                                            if (checked) simpleDrawReversed = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Interactive Card Flip Container
                    item {
                        Box(
                            modifier = Modifier
                                .width(240.dp)
                                .height(380.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .graphicsLayer {
                                    rotationY = flipRotationY
                                    rotationZ = if (flipRotationY >= 90f) reversalRotationZ else 0f
                                    cameraDistance = 14f * density
                                }
                                .clickable {
                                    if (!simpleDrawFlipped) {
                                        simpleDrawFlipped = true
                                        triggerHapticFeedback()
                                    } else {
                                        // When already flipped, clicking the card shows the full card image dialog
                                        fullCardDialogCard = simpleDrawCard
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (flipRotationY < 90f) {
                                // CARD BACK
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        if (DeckManager.tarotBackArtUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = DeckManager.tarotBackArtUrl,
                                                contentDescription = "Card Back Art",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.TouchApp,
                                                    contentDescription = "Tap",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Text(
                                                    text = "Tap to Flip",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                                Text(
                                                    text = "or shake to reshuffle",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // CARD FACE (flipped 180° around Y)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { rotationY = 180f }
                                ) {
                                    AsyncImage(
                                        model = TarotImageRepository.getCardImageUrl(simpleDrawCard.name),
                                        contentDescription = simpleDrawCard.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Subtle indicator to view full card
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ZoomIn,
                                                contentDescription = "Full Card",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                "Zoom",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card Action Controls (Flip Back, Draw Another)
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    simpleDrawFlipped = !simpleDrawFlipped
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.FlipCameraAndroid,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (simpleDrawFlipped) "Turn Face Down" else "Flip Up")
                            }

                            Button(
                                onClick = {
                                    triggerHapticFeedback()
                                    simpleDrawCard = TarotData.cards.random()
                                    simpleDrawFlipped = false
                                    simpleDrawReversed = if (DeckManager.noReversals) false else kotlin.random.Random.nextBoolean()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reshuffle Deck")
                            }
                        }
                    }

                    // Reversal Toggle
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        // Removed title
                                        Text(
                                            text = "Status: ${if (simpleDrawReversed) "Reversed (180°)" else "Upright"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            triggerHapticFeedback()
                                            simpleDrawReversed = !simpleDrawReversed
                                        }
                                    ) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (simpleDrawReversed) "Set Upright" else "Reverse")
                                    }
                                }

                                if (simpleDrawReversed) {
                                    Text(
                                        text = "Reversal Learning Insight: Inverted tarot archetypes reflect internal reflection, energetic delays, subconscious shadows, or the need to integrate opposite lessons.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // Card Meaning & Esoteric Data Card
                    if (simpleDrawFlipped) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = simpleDrawCard.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${simpleDrawCard.arcana} Arcana • ${simpleDrawCard.suit ?: "Major"} • ${simpleDrawCard.element}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(onClick = { fullCardDialogCard = simpleDrawCard }) {
                                            Icon(Icons.Default.Fullscreen, contentDescription = "View Full Card")
                                        }
                                    }

                                    Divider()

                                    Text(
                                        text = if (simpleDrawReversed) "Reversed Meaning:" else "Upright Meaning:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (simpleDrawReversed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (simpleDrawReversed) simpleDrawCard.reversedMeaning else simpleDrawCard.uprightMeaning,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "Description:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = simpleDrawCard.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Astrology: ${simpleDrawCard.astrology}", style = MaterialTheme.typography.labelSmall)
                                        Text("Numerology: ${simpleDrawCard.numerology}", style = MaterialTheme.typography.labelSmall)
                                    }

                                    Button(
                                        onClick = {
                                            onSaveReading(
                                                ReadingEntity(
                                                    type = "TAROT",
                                                    title = "Simple Draw: ${simpleDrawCard.name} (${if (simpleDrawReversed) "Reversed" else "Upright"})",
                                                    description = "Card: ${simpleDrawCard.name}\nOrientation: ${if (simpleDrawReversed) "Reversed" else "Upright"}\nMeaning: ${if (simpleDrawReversed) simpleDrawCard.reversedMeaning else simpleDrawCard.uprightMeaning}\nDescription: ${simpleDrawCard.description}"
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                    ) {
                                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Save Draw to Notes")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: SPREADS
            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_tarot_banner),
                                    contentDescription = "Tarot Sanctuary",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Column {
                                        Text(
                                            text = "Classical Tarot Spreads",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Cast structured spreads using all 78 Rider-Waite-Smith cards",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Choose a Divination Spread",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            spreadType = "Single Tarot Card Draw"
                                            val card = TarotData.cards.random()
                                            val isReversed = if (DeckManager.noReversals) false else kotlin.random.Random.nextBoolean()
                                            activeSpread = listOf("Insight" to (card to isReversed))
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Single Card")
                                    }
                                    Button(
                                        onClick = {
                                            spreadType = "3-Card Spread (Past, Present, Future)"
                                            val cards = TarotData.cards.shuffled().take(3)
                                            val labels = listOf("1. Past", "2. Present", "3. Future")
                                            activeSpread = cards.indices.map { i ->
                                                labels[i] to (cards[i] to (if (DeckManager.noReversals) false else kotlin.random.Random.nextBoolean()))
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Text("3-Card Spread")
                                    }
                                }

                                Button(
                                    onClick = {
                                        spreadType = "Celtic Cross Spread (10 Cards)"
                                        val cards = TarotData.cards.shuffled().take(10)
                                        val labels = listOf(
                                            "1. Present (Heart of Matter)",
                                            "2. Challenge (Crossing)",
                                            "3. Past (Foundation)",
                                            "4. Future (Crown/Goal)",
                                            "5. Above (Conscious)",
                                            "6. Below (Subconscious)",
                                            "7. Advice (Your Approach)",
                                            "8. External (Environment)",
                                            "9. Hopes & Fears",
                                            "10. Outcome (Resolution)"
                                        )
                                        activeSpread = cards.indices.map { i ->
                                            labels[i] to (cards[i] to (if (DeckManager.noReversals) false else kotlin.random.Random.nextBoolean()))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Text("Celtic Cross Spread (10 Cards)")
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: CARD COLLECTION (78 CARDS)
            2 -> {
                TarotCardCollectionView(
                    cards = TarotData.cards,
                    onCardClick = { card -> selectedCardDetail = card },
                    onCardImageClick = { card -> fullCardDialogCard = card }
                )
            }

            // TAB 3: DECKS & ART
            3 -> {
                TarotDeckBrowserView(
                    onDismissOrBack = { selectedTab = 0 }
                )
            }
        }
    }
}
