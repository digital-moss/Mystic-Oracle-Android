package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.DeckManager
import com.example.data.ReadingEntity
import com.example.model.Rune
import com.example.model.RuneData
import com.example.util.ShakeDetector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuneDeckScreen(
    onSaveReading: (ReadingEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Simple Draw, 1 = Spreads, 2 = Library
    var searchQuery by remember { mutableStateOf("") }
    var activeSpread by remember { mutableStateOf<List<Rune>?>(null) }
    var spreadType by remember { mutableStateOf("Single Rune") }

    // Simple Draw State
    var simpleDrawRune by remember { mutableStateOf(RuneData.runes.random()) }
    var simpleDrawFlipped by remember { mutableStateOf(false) }
    var simpleDrawReversed by remember { mutableStateOf(false) }
    var fullRuneDialogRune by remember { mutableStateOf<Rune?>(null) }

    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    fun triggerHapticFeedback() {
        if (!DeckManager.hapticsEnabled || vibrator == null) return
        val duration = when (DeckManager.shakeSensitivity) {
            "Low" -> 80L
            "High" -> 35L
            else -> 55L
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    // Shake detector for Simple Draw & Cast
    DisposableEffect(Unit) {
        val detector = ShakeDetector(context) {
            triggerHapticFeedback()
            simpleDrawRune = RuneData.runes.random()
            simpleDrawFlipped = false
            simpleDrawReversed = if (DeckManager.noReversals) false else kotlin.random.Random.nextBoolean()
            if (selectedTab == 1) {
                spreadType = "Shake-Cast 3-Rune Spread"
                activeSpread = RuneData.runes.shuffled().take(3)
            }
        }
        detector.start()
        onDispose {
            detector.stop()
        }
    }

    // Full Rune Dialog (fully viewable when clicked on)
    if (fullRuneDialogRune != null) {
        val r = fullRuneDialogRune!!
        AlertDialog(
            onDismissRequest = { fullRuneDialogRune = null },
            confirmButton = {
                TextButton(onClick = { fullRuneDialogRune = null }) {
                    Text("Close")
                }
            },
            title = {
                Text(
                    text = "${r.name} (${r.phonetic})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .graphicsLayer(
                                rotationZ = if (simpleDrawReversed && selectedTab == 0) 180f else 0f
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = r.symbol,
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = "Element: ${r.element}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Upright: ${r.meaning}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Merkstave (Reversed): ${r.reversedMeaning}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Norse Rune Sanctuary",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Simple Draw") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Cast Spreads") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Rune Library") }
            )
        }

        when (selectedTab) {
            // TAB 0: SIMPLE DRAW
            0 -> {
                val density = androidx.compose.ui.platform.LocalDensity.current.density
                val flipRotationY by animateFloatAsState(
                    targetValue = if (simpleDrawFlipped) 180f else 0f,
                    animationSpec = tween(500, easing = FastOutSlowInEasing),
                    label = "runeFlip"
                )

                // Spring reversal animation: flips past 180 and settles back
                val reversalRotationZ by animateFloatAsState(
                    targetValue = if (simpleDrawReversed) 180f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "runeReversalSpring"
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Control Bar
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

                    // Interactive Rune Stone / Card Flip
                    item {
                        Box(
                            modifier = Modifier
                                .width(220.dp)
                                .height(320.dp)
                                .clip(RoundedCornerShape(24.dp))
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
                                        fullRuneDialogRune = simpleDrawRune
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (flipRotationY < 90f) {
                                // RUNE BACK (Vegvísir ancient compass art)
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        if (DeckManager.runeBackArtUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = DeckManager.runeBackArtUrl,
                                                contentDescription = "Rune Back Art",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f))
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
                                                    tint = Color.White,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Text(
                                                    text = "Tap Rune to Cast",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "or shake to reshuffle",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // RUNE FACE (flipped 180° around Y)
                                Card(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { rotationY = 180f },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = simpleDrawRune.symbol,
                                                fontSize = 90.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = simpleDrawRune.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Phonetic: ${simpleDrawRune.phonetic}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

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
                                                    contentDescription = "Full Rune",
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
                    }

                    // Action Controls
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { simpleDrawFlipped = !simpleDrawFlipped },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (simpleDrawFlipped) "Turn Face Down" else "Flip Up")
                            }

                            Button(
                                onClick = {
                                    triggerHapticFeedback()
                                    simpleDrawRune = RuneData.runes.random()
                                    simpleDrawFlipped = false
                                    simpleDrawReversed = if (DeckManager.noReversals) false else kotlin.random.Random.nextBoolean()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reshuffle Runes")
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
                                            text = "Status: ${if (simpleDrawReversed) "Merkstave / Inverted (180°)" else "Upright"}",
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
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (simpleDrawReversed) "Flip Upright" else "Flip Merkstave")
                                    }
                                }

                                if (simpleDrawReversed) {
                                    Text(
                                        text = "Merkstave Learning Insight: An inverted rune indicates energy turned inward, blocked creative flow, over-indulgence, or a warning of potential heedlessness.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // Rune Meaning Card
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
                                                text = simpleDrawRune.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Element: ${simpleDrawRune.element} • Phonetic: ${simpleDrawRune.phonetic}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(onClick = { fullRuneDialogRune = simpleDrawRune }) {
                                            Icon(Icons.Default.Fullscreen, contentDescription = "View Full Rune")
                                        }
                                    }

                                    Divider()

                                    Text(
                                        text = if (simpleDrawReversed) "Merkstave (Reversed) Interpretation:" else "Upright Interpretation:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (simpleDrawReversed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (simpleDrawReversed) simpleDrawRune.reversedMeaning else simpleDrawRune.meaning,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Button(
                                        onClick = {
                                            onSaveReading(
                                                ReadingEntity(
                                                    type = "RUNE",
                                                    title = "Rune Draw: ${simpleDrawRune.name} (${if (simpleDrawReversed) "Merkstave" else "Upright"})",
                                                    description = "Rune: ${simpleDrawRune.name} (${simpleDrawRune.symbol})\nOrientation: ${if (simpleDrawReversed) "Merkstave" else "Upright"}\nInterpretation: ${if (simpleDrawReversed) simpleDrawRune.reversedMeaning else simpleDrawRune.meaning}\nElement: ${simpleDrawRune.element}"
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                    ) {
                                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Save Rune Draw to Notes")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: CAST SPREADS
            1 -> {
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
                                    text = "Cast the Runes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Draw runes from the ancient Elder Futhark to gain insight into your path, challenges, and future.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            spreadType = "Single Rune"
                                            activeSpread = listOf(RuneData.runes.random())
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Single Draw")
                                    }
                                    Button(
                                        onClick = {
                                            spreadType = "Three-Rune Spread (Past, Present, Future)"
                                            activeSpread = RuneData.runes.shuffled().take(3)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Text("3-Rune Spread")
                                    }
                                }
                            }
                        }
                    }

                    if (activeSpread != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                                        Text(
                                            text = spreadType,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        IconButton(onClick = {
                                            val desc = activeSpread!!.joinToString("\n\n") { "${it.name} (${it.symbol}): ${it.meaning}" }
                                            onSaveReading(
                                                ReadingEntity(
                                                    type = "RUNE",
                                                    title = spreadType,
                                                    description = desc
                                                )
                                            )
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.BookmarkAdd,
                                                contentDescription = "Save Reading",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    activeSpread!!.forEachIndexed { index, rune ->
                                        val positionLabel = when (activeSpread!!.size) {
                                            3 -> when (index) { 0 -> "Past (Urd)"; 1 -> "Present (Verdandi)"; else -> "Future (Skuld)" }
                                            else -> "Guidance"
                                        }
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { fullRuneDialogRune = rune }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = rune.symbol, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = "$positionLabel: ${rune.name} (${rune.phonetic})", fontWeight = FontWeight.Bold)
                                                    Text(text = rune.meaning, style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: RUNE LIBRARY
            2 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Runes") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val filteredRunes = remember(searchQuery) {
                        if (searchQuery.isBlank()) RuneData.runes
                        else RuneData.runes.filter { it.name.contains(searchQuery, true) || it.meaning.contains(searchQuery, true) || it.element.contains(searchQuery, true) }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredRunes) { rune ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { fullRuneDialogRune = rune },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = rune.symbol, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = rune.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text(text = "Element: ${rune.element}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Upright: ${rune.meaning}", style = MaterialTheme.typography.bodySmall)
                                        Text(text = "Merkstave: ${rune.reversedMeaning}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
