package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
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
import com.example.model.Hexagram
import com.example.model.IChingData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IChingScreen(
    onSaveReading: (ReadingEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Cast, 1 = Library
    var searchQuery by remember { mutableStateOf("") }
    var questionInput by remember { mutableStateOf("") }
    var castedHexagram by remember { mutableStateOf<Hexagram?>(null) }
    var castedLines by remember { mutableStateOf<List<Int>>(emptyList()) }

    var sessionKey by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(120_000) // 2 minutes
            sessionKey = System.currentTimeMillis()
            castedHexagram = IChingData.hexagrams.random()
            castedLines = (1..6).map { (6..9).random() }
        }
    }

    LaunchedEffect(sessionKey) {
        if (castedHexagram == null) {
            castedHexagram = IChingData.hexagrams.random()
            castedLines = (1..6).map { (6..9).random() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "I Ching (Book of Changes)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Consult Oracle") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Hexagram Library") }
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
                                onClick = {
                                    castedHexagram = IChingData.hexagrams.random()
                                    castedLines = (1..6).map { (6..9).random() }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cast Coins & Reveal Hexagram")
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
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    castedLines.reversed().forEachIndexed { idx, lineVal ->
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
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Hexagrams") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                val filteredHexagrams = remember(searchQuery) {
                    if (searchQuery.isBlank()) IChingData.hexagrams
                    else IChingData.hexagrams.filter { it.name.contains(searchQuery, true) || it.judgment.contains(searchQuery, true) || it.chineseName.contains(searchQuery, true) }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredHexagrams) { hex ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "#${hex.number} ${hex.name} (${hex.chineseName})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${hex.trigramUpper} over ${hex.trigramLower}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Text(
                                    text = hex.judgment,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
