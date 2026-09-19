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
import com.example.model.Hexagram
import com.example.model.IChingData
import com.example.util.ShakeDetector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IChingScreen(
    onSaveReading: (ReadingEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Simple Draw, 1 = Consult, 2 = Library
    var searchQuery by remember { mutableStateOf("") }
    var questionInput by remember { mutableStateOf("") }
    var castedHexagram by remember { mutableStateOf<Hexagram?>(null) }
    var castedLines by remember { mutableStateOf<List<Int>>(emptyList()) }

    // Simple Draw State
    var simpleDrawHexagram by remember { mutableStateOf(IChingData.hexagrams.random()) }
    var simpleDrawFlipped by remember { mutableStateOf(false) }
    var simpleDrawReversed by remember { mutableStateOf(false) }
    var fullHexagramDialog by remember { mutableStateOf<Hexagram?>(null) }

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

    fun castHexagram() {
        castedHexagram = IChingData.hexagrams.random()
        castedLines = (1..6).map { (6..9).random() }
    }

    // Shake detector for reshuffle in Simple Draw and casting in Consult
    DisposableEffect(Unit) {
        val detector = ShakeDetector(context) {
            triggerHapticFeedback()
            simpleDrawHexagram = IChingData.hexagrams.random()
            simpleDrawFlipped = false
            simpleDrawReversed = if (DeckManager.noReversals) false else kotlin.random.Random.nextBoolean()
            if (selectedTab == 1) {
                castHexagram()
            }
        }
        detector.start()
        onDispose {
            detector.stop()
        }
    }

    // Helper to get lines (bottom to top) for a trigram
    fun getTrigramLines(trigram: String): List<Boolean> {
        return when (trigram) {
            "Heaven" -> listOf(true, true, true)
            "Earth" -> listOf(false, false, false)
            "Thunder" -> listOf(true, false, false)
            "Water" -> listOf(false, true, false)
            "Mountain" -> listOf(false, false, true)
            "Wind" -> listOf(false, true, true)
            "Fire" -> listOf(true, false, true)
            "Lake" -> listOf(true, true, false)
            else -> listOf(true, false, true)
        }
    }

    fun getHexagramLines(hex: Hexagram): List<Boolean> {
        // lower trigram (lines 1, 2, 3) + upper trigram (lines 4, 5, 6)
        val lower = getTrigramLines(hex.trigramLower)
        val upper = getTrigramLines(hex.trigramUpper)
        return lower + upper
    }

    // Full Hexagram Dialog
    if (fullHexagramDialog != null) {
        val hex = fullHexagramDialog!!
        val lines = getHexagramLines(hex)
        AlertDialog(
            onDismissRequest = { fullHexagramDialog = null },
            confirmButton = {
                TextButton(onClick = { fullHexagramDialog = null }) {
                    Text("Close")
                }
            },
            title = {
                Text(
                    text = "Hexagram #${hex.number}: ${hex.name} (${hex.chineseName})",
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Lines render (top to bottom: line 6 down to line 1)
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                            .graphicsLayer(
                                rotationZ = if (simpleDrawReversed && selectedTab == 0) 180f else 0f
                            ),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        lines.reversed().forEach { isYang ->
                            Row(
                                modifier = Modifier.width(140.dp).height(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (isYang) {
                                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                                } else {
                                    Box(modifier = Modifier.width(62.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                                    Box(modifier = Modifier.width(62.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                                }
                            }
                        }
                    }

                    Text(
                        text = "Upper: ${hex.trigramUpper} | Lower: ${hex.trigramLower}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Judgment:\n${hex.judgment}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "The Image:\n${hex.image}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "I Ching (Book of Changes)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Simple Draw") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Consult Oracle") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Library (64)") }
            )
        }

        when (selectedTab) {
            // TAB 0: SIMPLE DRAW
            0 -> {
                val density = androidx.compose.ui.platform.LocalDensity.current.density
                val flipRotationY by animateFloatAsState(
                    targetValue = if (simpleDrawFlipped) 180f else 0f,
                    animationSpec = tween(500, easing = FastOutSlowInEasing),
                    label = "ichingFlip"
                )

                // Spring reversal animation: flips past 180 and settles back
                val reversalRotationZ by animateFloatAsState(
                    targetValue = if (simpleDrawReversed) 180f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "ichingReversalSpring"
                )

                val activeLines = getHexagramLines(simpleDrawHexagram)

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
                                        text = "Shake phone to reshuffle 64 hexagrams",
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

                    // Interactive Hexagram Card
                    item {
                        Box(
                            modifier = Modifier
                                .width(240.dp)
                                .height(340.dp)
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
                                        fullHexagramDialog = simpleDrawHexagram
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (flipRotationY < 90f) {
                                // CARD BACK (Taiji Yin-Yang artwork)
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        if (DeckManager.iChingBackArtUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = DeckManager.iChingBackArtUrl,
                                                contentDescription = "I-Ching Back Art",
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
                                                    text = "Tap to Draw Hexagram",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "or shake to reshuffle 64 cards",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // CARD FACE (flipped 180° around Y)
                                Card(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { rotationY = 180f },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = "Hexagram #${simpleDrawHexagram.number}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )

                                            // Draw the 6 lines (rendered top to bottom: line 6 down to line 1)
                                            Column(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                activeLines.reversed().forEach { isYang ->
                                                    Row(
                                                        modifier = Modifier.width(130.dp).height(10.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        if (isYang) {
                                                            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                                                        } else {
                                                            Box(modifier = Modifier.width(58.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                                                            Box(modifier = Modifier.width(58.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                                                        }
                                                    }
                                                }
                                            }

                                            Text(
                                                text = simpleDrawHexagram.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = simpleDrawHexagram.chineseName,
                                                style = MaterialTheme.typography.titleSmall,
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
                                                    contentDescription = "Full Hexagram",
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
                                Text(if (simpleDrawFlipped) "Face Down" else "Flip Up")
                            }

                            Button(
                                onClick = {
                                    triggerHapticFeedback()
                                    simpleDrawHexagram = IChingData.hexagrams.random()
                                    simpleDrawFlipped = false
                                    simpleDrawReversed = if (DeckManager.noReversals) false else kotlin.random.Random.nextBoolean()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reshuffle 64 Cards")
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
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        // Removed title
                                        Text(
                                            text = "Status: ${if (simpleDrawReversed) "Zōng Guà / Inverted (180°)" else "Upright Orientation"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                FilledTonalButton(
                                    onClick = {
                                        triggerHapticFeedback()
                                        simpleDrawReversed = !simpleDrawReversed
                                    }
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (simpleDrawReversed) "Flip Upright" else "Flip Inverted")
                                }

                                if (simpleDrawReversed) {
                                    Text(
                                        text = "Zōng Guà Learning Insight: In classical I Ching hermeneutics, rotating a hexagram 180° reveals its companion perspective (how the situation appears from the other person's viewpoint or reverse angle).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // Hexagram Judgment & Meaning Card
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
                                                text = "Hexagram #${simpleDrawHexagram.number}: ${simpleDrawHexagram.name}",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${simpleDrawHexagram.chineseName} • Upper: ${simpleDrawHexagram.trigramUpper} / Lower: ${simpleDrawHexagram.trigramLower}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(onClick = { fullHexagramDialog = simpleDrawHexagram }) {
                                            Icon(Icons.Default.Fullscreen, contentDescription = "View Full Hexagram")
                                        }
                                    }

                                    Divider()

                                    Text(
                                        text = "The Judgment:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = simpleDrawHexagram.judgment,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "The Image:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = simpleDrawHexagram.image,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )

                                    Button(
                                        onClick = {
                                            onSaveReading(
                                                ReadingEntity(
                                                    type = "ICHING",
                                                    title = "I Ching: #${simpleDrawHexagram.number} ${simpleDrawHexagram.name} (${if (simpleDrawReversed) "Inverted" else "Upright"})",
                                                    description = "Hexagram #${simpleDrawHexagram.number}: ${simpleDrawHexagram.name} (${simpleDrawHexagram.chineseName})\nOrientation: ${if (simpleDrawReversed) "Inverted" else "Upright"}\nJudgment: ${simpleDrawHexagram.judgment}\nImage: ${simpleDrawHexagram.image}"
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                    ) {
                                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Save Hexagram to Notes")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: CONSULT ORACLE (COIN CAST)
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
                                    text = "Cast a Hexagram",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                    value = questionInput,
                                    onValueChange = { questionInput = it },
                                    label = { Text("Your Question or Focus") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Button(
                                    onClick = { castHexagram() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Consult Oracle (Cast 3 Coins)")
                                }
                            }
                        }
                    }

                    if (castedHexagram != null) {
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
                                        Column {
                                            Text(
                                                text = "Hexagram #${castedHexagram!!.number}: ${castedHexagram!!.name} (${castedHexagram!!.chineseName})",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            if (questionInput.isNotBlank()) {
                                                Text(
                                                    text = "Focus: $questionInput",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                        IconButton(onClick = {
                                            val desc = "Hexagram #${castedHexagram!!.number} - ${castedHexagram!!.name}\nJudgment: ${castedHexagram!!.judgment}\nImage: ${castedHexagram!!.image}"
                                            onSaveReading(
                                                ReadingEntity(
                                                    type = "ICHING",
                                                    title = "I Ching: ${castedHexagram!!.name}",
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

                                    // Render lines (bottom up)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                            .clickable { fullHexagramDialog = castedHexagram },
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        castedLines.reversed().forEach { lineVal ->
                                            val isYin = lineVal % 2 == 0
                                            Row(
                                                modifier = Modifier.width(160.dp).height(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                if (isYin) {
                                                    Box(modifier = Modifier.width(70.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                                                    Box(modifier = Modifier.width(70.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                                                } else {
                                                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                                                }
                                            }
                                        }
                                    }

                                    Text(
                                        text = "Judgment:\n${castedHexagram!!.judgment}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Image:\n${castedHexagram!!.image}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: LIBRARY (ALL 64 HEXAGRAMS)
            2 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search all 64 Hexagrams") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val filteredHexagrams = remember(searchQuery) {
                        if (searchQuery.isBlank()) IChingData.hexagrams
                        else IChingData.hexagrams.filter {
                            it.name.contains(searchQuery, true) ||
                            it.chineseName.contains(searchQuery, true) ||
                            it.judgment.contains(searchQuery, true) ||
                            it.number.toString() == searchQuery.trim()
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredHexagrams) { hex ->
                            val lines = getHexagramLines(hex)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { fullHexagramDialog = hex },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Mini hexagram icon
                                    Column(
                                        modifier = Modifier
                                            .width(44.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        lines.reversed().forEach { isYang ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                if (isYang) {
                                                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary))
                                                } else {
                                                    Box(modifier = Modifier.width(16.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                                                    Box(modifier = Modifier.width(16.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "#${hex.number}. ${hex.name}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = hex.chineseName,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = hex.judgment,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2
                                        )
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
