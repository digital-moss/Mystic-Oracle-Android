package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ReadingEntity
import com.example.model.TarotCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotReadingResultScreen(
    spreadType: String,
    cardsWithPositions: List<Pair<String, Pair<TarotCard, Boolean>>>,
    onSaveReading: (ReadingEntity) -> Unit,
    onSelectCard: (TarotCard) -> Unit,
    onContinueDrawing: () -> Unit,
    onBack: () -> Unit
) {
    var readingEnded by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    // Synthesize chronological meaning sum up across all cards
    val synthesizedSummary = remember(cardsWithPositions) {
        val count = cardsWithPositions.size
        val elements = cardsWithPositions.map { it.second.first.element }.distinct()
        val uprightCount = cardsWithPositions.count { !it.second.second }
        val reversedCount = cardsWithPositions.count { it.second.second }
        
        buildString {
            append("Chronological Synthesis ($count Cards Drawn):\n\n")
            append("• Flow & Progression: Your reading evolved through $count distinct stages, reflecting an active journey from initial foundational energies to current resolution.\n")
            append("• Elemental Balance: Prominent elements include ${elements.joinToString(", ")}.\n")
            append("• Orientation Balance: $uprightCount Upright, $reversedCount Reversed.\n\n")
            append("Combined Core Meanings:\n")
            cardsWithPositions.forEachIndexed { idx, (pos, pair) ->
                val (card, rev) = pair
                val orient = if (rev) "Reversed" else "Upright"
                append("${idx + 1}. [$pos] ${card.name} ($orient): ${if (rev) card.reversedMeaning else card.uprightMeaning}\n")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(spreadType) },
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
                    colors = CardDefaults.cardColors(
                        containerColor = if (readingEnded) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
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
                            Text(
                                text = if (readingEnded) "Reading Concluded & Synthesized" else "Active Tarot Reading Flow",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (readingEnded) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (readingEnded && saved) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("Saved to Journal")
                                }
                            }
                        }

                        Text(
                            text = if (readingEnded) 
                                "All drawn cards have been collected chronologically and synthesized into a holistic reading summary below. This reading is now securely saved in your journal."
                            else 
                                "You can continue drawing new cards chronologically, tap any card for deep esoteric symbology, or end the reading to synthesize the complete meaning.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (readingEnded) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )

                        if (!readingEnded) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onContinueDrawing,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Continue Drawing")
                                }

                                Button(
                                    onClick = {
                                        readingEnded = true
                                        if (!saved) {
                                            onSaveReading(
                                                ReadingEntity(
                                                    type = "TAROT",
                                                    title = "$spreadType (${cardsWithPositions.size} Cards)",
                                                    description = synthesizedSummary
                                                )
                                            )
                                            saved = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("End Reading")
                                }
                            }
                        }
                    }
                }
            }

            if (readingEnded) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Chronological Synthesis Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = synthesizedSummary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Drawn Cards (Chronological Order)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(cardsWithPositions.size) { index ->
                val (positionLabel, pair) = cardsWithPositions[index]
                val (card, isReversed) = pair

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCard(card) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${index + 1}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = positionLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Badge(containerColor = if (isReversed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) {
                                    Text(text = if (isReversed) "Reversed" else "Upright")
                                }
                            }
                            Text(
                                text = card.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isReversed) card.reversedMeaning else card.uprightMeaning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Elem: ${card.element}", style = MaterialTheme.typography.labelSmall)
                                Text(text = "Planet: ${card.planet}", style = MaterialTheme.typography.labelSmall)
                                Text(text = "Astrology: ${card.astrology}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
