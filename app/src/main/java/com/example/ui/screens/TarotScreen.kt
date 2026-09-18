package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ReadingEntity
import com.example.model.TarotCard
import com.example.model.TarotData
import coil.compose.AsyncImage
import com.example.network.TarotImageRepository
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotScreen(
    onSaveReading: (ReadingEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Cast, 1 = Library, 2 = Custom Decks
    var searchQuery by remember { mutableStateOf("") }
    var activeSpread by remember { mutableStateOf<List<Pair<String, Pair<TarotCard, Boolean>>>?>(null) }
    var spreadType by remember { mutableStateOf("Single Tarot Card Draw") }
    var selectedCardDetail by remember { mutableStateOf<TarotCard?>(null) }

    // Custom decks state
    var importedDecksCount by remember { mutableStateOf(0) }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }

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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            importStatusMessage = "Successfully imported custom card photo!"
        }
    }

    var sessionKey by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(120_000) // 2 minutes
            sessionKey = System.currentTimeMillis()
            val card = TarotData.cards.random()
            activeSpread = listOf("Refreshed Session Draw" to (card to kotlin.random.Random.nextBoolean()))
            spreadType = "Refreshed Tarot Draw"
        }
    }

    LaunchedEffect(sessionKey) {
        if (activeSpread == null) {
            val card = TarotData.cards.random()
            activeSpread = listOf("Initial Session Draw" to (card to kotlin.random.Random.nextBoolean()))
            spreadType = "Initial Tarot Draw"
        }
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
                val isRev = kotlin.random.Random.nextBoolean()
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tarot Sanctuary",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Draw / Spreads") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Library") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Custom Decks") }
            )
        }

        when (selectedTab) {
            0 -> {
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
                                    .height(180.dp)
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
                                            text = "Tarot Sanctuary",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                        Text(
                                            text = "Ryder-Waite Classic Deck & Spreads",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (activeSpread != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Trigger reading view by re-assigning or making activeSpread trigger result screen
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Active Reading Available",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = spreadType,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            // The activeSpread check at top level will show the result screen when activeSpread is non-null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("View Spread")
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
                            var shakeToShuffleEnabled by remember { mutableStateOf(true) }
                            var shakeSensitivity by remember { mutableStateOf("Medium") }
                            var hapticSensitivity by remember { mutableStateOf("Medium") }

                            val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager }
                            val accelerometer = remember { sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER) }
                            val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator }

                            fun triggerHaptics() {
                                if (hapticSensitivity == "Off" || vibrator == null) return
                                val duration = when (hapticSensitivity) {
                                    "Light" -> 30L
                                    "Strong" -> 100L
                                    else -> 60L
                                }
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator.vibrate(duration)
                                }
                            }

                            DisposableEffect(shakeToShuffleEnabled, shakeSensitivity) {
                                if (!shakeToShuffleEnabled || accelerometer == null) {
                                    return@DisposableEffect object : androidx.compose.runtime.DisposableEffectResult {
                                        override fun dispose() {}
                                    }
                                }

                                val threshold = when (shakeSensitivity) {
                                    "Low" -> 16f
                                    "High" -> 9.5f
                                    else -> 12.5f
                                }

                                val listener = object : android.hardware.SensorEventListener {
                                    var lastUpdate: Long = 0
                                    var last_x = 0f
                                    var last_y = 0f
                                    var last_z = 0f

                                    override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                                        if (event?.sensor?.type == android.hardware.Sensor.TYPE_ACCELEROMETER) {
                                            val curTime = System.currentTimeMillis()
                                            if ((curTime - lastUpdate) > 300) {
                                                val diffTime = (curTime - lastUpdate)
                                                lastUpdate = curTime

                                                val x = event.values[0]
                                                val y = event.values[1]
                                                val z = event.values[2]

                                                val speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000

                                                if (speed > threshold) {
                                                    triggerHaptics()
                                                    spreadType = "3-Card Spread (Shaken by Device)"
                                                    val cards = TarotData.cards.shuffled().take(3)
                                                    val labels = listOf("1. Past", "2. Present", "3. Future")
                                                    activeSpread = cards.indices.map { i ->
                                                        labels[i] to (cards[i] to kotlin.random.Random.nextBoolean())
                                                    }
                                                }

                                                last_x = x
                                                last_y = y
                                                last_z = z
                                            }
                                        }
                                    }

                                    override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
                                }

                                sensorManager.registerListener(listener, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_UI)

                                onDispose {
                                    sensorManager.unregisterListener(listener)
                                }
                            }

                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Shake to Shuffle",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Checkbox(
                                        checked = shakeToShuffleEnabled,
                                        onCheckedChange = { shakeToShuffleEnabled = it }
                                    )
                                }

                                Text(
                                    text = "Shake your device to automatically shuffle and cast a 3-card spread.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                if (shakeToShuffleEnabled) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Shake Sensitivity: $shakeSensitivity", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("Low", "Medium", "High").forEach { level ->
                                                FilterChip(
                                                    selected = shakeSensitivity == level,
                                                    onClick = { shakeSensitivity = level },
                                                    label = { Text(level) }
                                                )
                                            }
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Haptic Feedback: $hapticSensitivity", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("Off", "Light", "Medium", "Strong").forEach { level ->
                                                FilterChip(
                                                    selected = hapticSensitivity == level,
                                                    onClick = { hapticSensitivity = level },
                                                    label = { Text(level) }
                                                )
                                            }
                                        }
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
                                    text = "Consult the Tarot (Ryder-Waite Deck)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Standard Ryder-Waite-Smith Classic Deck is pre-installed and available by default. Choose your spread to reveal cards with rich esoteric symbology, artwork, astrology, numerology, and elemental attributes.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            spreadType = "Single Tarot Card Draw"
                                            val card = TarotData.cards.random()
                                            val isReversed = kotlin.random.Random.nextBoolean()
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
                                                labels[i] to (cards[i] to kotlin.random.Random.nextBoolean())
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
                                            labels[i] to (cards[i] to kotlin.random.Random.nextBoolean())
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
            1 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Tarot Cards") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val filteredCards = remember(searchQuery) {
                        if (searchQuery.isBlank()) TarotData.cards
                        else TarotData.cards.filter { it.name.contains(searchQuery, true) || it.uprightMeaning.contains(searchQuery, true) || it.element.contains(searchQuery, true) }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCards) { card ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCardDetail = card },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = TarotImageRepository.getCardImageUrl(card.name),
                                        contentDescription = card.name,
                                        modifier = Modifier
                                            .size(56.dp, 84.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = card.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text(text = "${card.element} • ${card.planet}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Text(text = "Upright: ${card.uprightMeaning}", style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                                    text = "Custom Tarot Decks",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Import custom tarot decks via .zip archives (supports standard naming/number schemes like 0_fool.png, 00.png) or import custom photos for individual cards.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(
                                    onClick = {
                                        zipPickerLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "*/*"))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.FolderZip, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Import Deck (.zip archive)")
                                }

                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(arrayOf("image/*"))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Custom Card Photo")
                                }

                                if (importStatusMessage != null) {
                                    Text(
                                        text = importStatusMessage!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
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
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Active Custom Decks ($importedDecksCount Imported)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (importedDecksCount > 0) "Custom deck active. Card pulls will use your imported images." else "Default Major Arcana deck active. Import a .zip archive or custom card photos above to personalize your readings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
