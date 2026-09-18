package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.example.util.ShakeDetector
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YesNoOracleScreen(
    onSaveReading: (ReadingEntity) -> Unit
) {
    val context = LocalContext.current
    var question by remember { mutableStateOf("") }
    var enableChance by remember { mutableStateOf(false) }
    var chanceSliderValue by remember { mutableStateOf(50f) } // 0.000 to 100.000
    var resultOutcome by remember { mutableStateOf<Pair<String, Double>?>(null) }
    var saved by remember { mutableStateOf(false) }
    var showCustomCoinDialog by remember { mutableStateOf(false) }

    // Coin toss animation rotation
    var coinTossAnimTarget by remember { mutableStateOf(0f) }
    val coinRotation by animateFloatAsState(
        targetValue = coinTossAnimTarget,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "coinTossAnim"
    )

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

    val formattedChance = String.format(Locale.US, "%.3f", chanceSliderValue)

    fun consultOracle() {
        triggerHapticFeedback()
        coinTossAnimTarget += 720f
        val roll = Random.nextDouble(0.0, 100.0)
        val outcome = if (enableChance) {
            if (chanceSliderValue <= 19.0001f) {
                "Fuck No!"
            } else if (chanceSliderValue >= 88.9999f) {
                "Fuck Yeah!!"
            } else {
                if (roll <= chanceSliderValue) "YES" else "NO"
            }
        } else {
            if (Random.nextBoolean()) "YES" else "NO"
        }
        resultOutcome = outcome to roll
        saved = false
    }

    // Photo pickers for heads and tails
    val headsPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { DeckManager.coinHeadsArtUrl = it.toString() }
    }
    val tailsPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { DeckManager.coinTailsArtUrl = it.toString() }
    }

    DisposableEffect(Unit) {
        val detector = ShakeDetector(context) {
            consultOracle()
        }
        detector.start()
        onDispose {
            detector.stop()
        }
    }

    // Custom Coin Dialog
    if (showCustomCoinDialog) {
        var headsInput by remember { mutableStateOf(DeckManager.coinHeadsArtUrl) }
        var tailsInput by remember { mutableStateOf(DeckManager.coinTailsArtUrl) }

        AlertDialog(
            onDismissRequest = { showCustomCoinDialog = false },
            title = { Text("Customize Both Coin Sides") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Upload custom artwork or enter URLs for both sides of your Yes/No coin:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // Heads (YES / FUCK YEAH)
                    OutlinedTextField(
                        value = headsInput,
                        onValueChange = {
                            headsInput = it
                            DeckManager.coinHeadsArtUrl = it
                        },
                        label = { Text("Heads (Yes) Side Image") },
                        trailingIcon = {
                            IconButton(onClick = { headsPicker.launch("image/*") }) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Upload Heads Art")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Tails (NO / FUCK NO)
                    OutlinedTextField(
                        value = tailsInput,
                        onValueChange = {
                            tailsInput = it
                            DeckManager.coinTailsArtUrl = it
                        },
                        label = { Text("Tails (No) Side Image") },
                        trailingIcon = {
                            IconButton(onClick = { tailsPicker.launch("image/*") }) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Upload Tails Art")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Heads Preview", style = MaterialTheme.typography.labelSmall)
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (DeckManager.coinHeadsArtUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = DeckManager.coinHeadsArtUrl,
                                        contentDescription = "Heads",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tails Preview", style = MaterialTheme.typography.labelSmall)
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.error, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (DeckManager.coinTailsArtUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = DeckManager.coinTailsArtUrl,
                                        contentDescription = "Tails",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomCoinDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Yes/No Oracle & Coin",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ask any question of fate or shake your device to toss your custom coin. Optionally specify probability odds.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                IconButton(
                    onClick = { showCustomCoinDialog = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Customize Coin Sides",
                        tint = MaterialTheme.colorScheme.primary
                    )
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = question,
                        onValueChange = {
                            question = it
                            saved = false
                        },
                        label = { Text("Your Question for the Oracle") },
                        placeholder = { Text("e.g., Should I embark on this new journey?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Percent,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Percentage Option",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "0-19%: \"Fuck No!\" | 89-100%: \"Fuck Yeah!!\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Switch(
                            checked = enableChance,
                            onCheckedChange = { enableChance = it }
                        )
                    }

                    if (enableChance) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Target Success Probability", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "$formattedChance%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Slider(
                                value = chanceSliderValue,
                                onValueChange = { chanceSliderValue = it },
                                valueRange = 0f..100f,
                                steps = 10000
                            )

                            // Quick Preset Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                FilterChip(
                                    selected = chanceSliderValue <= 19f,
                                    onClick = { chanceSliderValue = 10f },
                                    label = { Text("10% (Fuck No!)") }
                                )
                                FilterChip(
                                    selected = chanceSliderValue == 50f,
                                    onClick = { chanceSliderValue = 50f },
                                    label = { Text("50%") }
                                )
                                FilterChip(
                                    selected = chanceSliderValue >= 89f,
                                    onClick = { chanceSliderValue = 95f },
                                    label = { Text("95% (Fuck Yeah!!)") }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { consultOracle() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Help, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Consult Oracle / Toss Coin (or Shake)")
                    }
                }
            }
        }

        if (resultOutcome != null) {
            item {
                val (outcome, roll) = resultOutcome!!
                val isPositive = outcome == "YES" || outcome == "Fuck Yeah!!"
                val coinArtUrl = if (isPositive) DeckManager.coinHeadsArtUrl else DeckManager.coinTailsArtUrl

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPositive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 3D Animated Coin
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .graphicsLayer {
                                    rotationY = coinRotation
                                    cameraDistance = 12f * density
                                }
                                .clip(CircleShape)
                                .border(
                                    4.dp,
                                    if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    CircleShape
                                )
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { consultOracle() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (coinArtUrl.isNotBlank()) {
                                AsyncImage(
                                    model = coinArtUrl,
                                    contentDescription = outcome,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPositive) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = outcome,
                                    modifier = Modifier.size(64.dp),
                                    tint = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Text(
                            text = if (question.isNotBlank()) "Question: \"$question\"" else "Oracle Decree",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isPositive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )

                        Text(
                            text = outcome,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        if (enableChance) {
                            val formattedRoll = String.format(Locale.US, "%.3f", roll)
                            Text(
                                text = "Percentage Option: $formattedChance% | Fate Roll: $formattedRoll%",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isPositive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (!saved) {
                                    val qTitle = if (question.isNotBlank()) question else "Yes/No Consultation"
                                    val desc = buildString {
                                        append("Answer: $outcome\n")
                                        if (enableChance) {
                                            append("Percentage Option: $formattedChance%\n")
                                            append("Fate Roll: ${String.format(Locale.US, "%.3f", roll)}%")
                                        } else {
                                            append("Standard 50/50 Coin Toss")
                                        }
                                    }
                                    onSaveReading(
                                        ReadingEntity(
                                            type = "YES_NO",
                                            title = qTitle,
                                            description = desc
                                        )
                                    )
                                    saved = true
                                }
                            },
                            enabled = !saved,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (saved) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (saved) "Saved to Journal ✓" else "Save Reading to Journal")
                        }
                    }
                }
            }
        }
    }
}
