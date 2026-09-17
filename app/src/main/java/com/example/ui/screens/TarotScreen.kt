package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReadingEntity
import com.example.model.TarotCard
import com.example.model.TarotData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotScreen(
    onSaveReading: (ReadingEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Cast, 1 = Library
    var searchQuery by remember { mutableStateOf("") }
    var activeSpread by remember { mutableStateOf<List<Pair<TarotCard, Boolean>>?>(null) }
    var spreadType by remember { mutableStateOf("Single Tarot Card Draw") }

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
                text = { Text("Draw & Readings") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Tarot Library") }
            )
        }

        if (selectedTab == 0) {
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
                                text = "Consult the Tarot",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Draw cards from the Major Arcana to uncover deep psychological insight and spiritual guidance.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        spreadType = "Single Tarot Draw"
                                        val card = TarotData.cards.random()
                                        val isReversed = kotlin.random.Random.nextBoolean()
                                        activeSpread = listOf(card to isReversed)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Single Card")
                                }
                                Button(
                                    onClick = {
                                        spreadType = "3-Card Spread (Past, Present, Future)"
                                        val cards = TarotData.cards.shuffled().take(3)
                                        activeSpread = cards.map { it to kotlin.random.Random.nextBoolean() }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("3-Card Spread")
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
                                        val desc = activeSpread!!.joinToString("\n\n") { (card, rev) ->
                                            val pos = if (rev) "Reversed" else "Upright"
                                            "${card.name} ($pos): ${if (rev) card.reversedMeaning else card.uprightMeaning}"
                                        }
                                        onSaveReading(
                                            ReadingEntity(
                                                type = "TAROT",
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

                                activeSpread!!.forEachIndexed { index, (card, isReversed) ->
                                    val positionLabel = when (activeSpread!!.size) {
                                        3 -> when (index) { 0 -> "Past"; 1 -> "Present"; else -> "Future" }
                                        else -> "Insight"
                                    }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "$positionLabel: ${card.name}",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                                Badge(containerColor = if (isReversed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) {
                                                    Text(text = if (isReversed) "Reversed" else "Upright")
                                                }
                                            }
                                            Text(
                                                text = if (isReversed) card.reversedMeaning else card.uprightMeaning,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = card.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
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
                    else TarotData.cards.filter { it.name.contains(searchQuery, true) || it.uprightMeaning.contains(searchQuery, true) }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCards) { card ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = card.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Upright: ${card.uprightMeaning}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Reversed: ${card.reversedMeaning}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}
