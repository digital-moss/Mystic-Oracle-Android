package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.TarotCard
import com.example.network.TarotImageRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotCardCollectionView(
    cards: List<TarotCard>,
    onCardClick: (TarotCard) -> Unit,
    onCardImageClick: (TarotCard) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedElement by remember { mutableStateOf("All") }
    var selectedArcanaOrSuit by remember { mutableStateOf("All") }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var isGridView by remember { mutableStateOf(false) }

    val popularTags = remember {
        listOf(
            "Beginnings", "Manifestation", "Intuition", "Transformation",
            "Courage", "Love", "Wisdom", "Power", "Balance", "Abundance"
        )
    }

    val elementalOptions = remember {
        listOf("All", "Air", "Fire", "Water", "Earth", "Spirit")
    }

    val arcanaAndSuitOptions = remember {
        listOf("All", "Major", "Minor", "Wands", "Cups", "Swords", "Pentacles")
    }

    // Filter cards based on search query, element, arcana/suit, and tags
    val filteredCards = remember(cards, searchQuery, selectedElement, selectedArcanaOrSuit, selectedTag) {
        val q = searchQuery.trim().lowercase()
        cards.filter { card ->
            // 1. Element filter
            val matchesElement = when (selectedElement) {
                "All" -> true
                else -> card.element.equals(selectedElement, ignoreCase = true)
            }

            // 2. Arcana / Suit filter
            val matchesArcanaOrSuit = when (selectedArcanaOrSuit) {
                "All" -> true
                "Major" -> card.arcana.equals("Major", ignoreCase = true)
                "Minor" -> card.arcana.equals("Minor", ignoreCase = true)
                else -> card.suit?.equals(selectedArcanaOrSuit, ignoreCase = true) == true
            }

            // 3. Tag filter
            val matchesTag = if (selectedTag == null) {
                true
            } else {
                card.defaultTerms.any { it.equals(selectedTag, ignoreCase = true) } ||
                card.symbology.any { it.contains(selectedTag!!, ignoreCase = true) }
            }

            // 4. Query filter (Card Name, Tags, Elements, Symbology, Suit, Arcana, Meanings)
            val matchesQuery = if (q.isEmpty()) {
                true
            } else {
                card.name.lowercase().contains(q) ||
                card.element.lowercase().contains(q) ||
                card.defaultTerms.any { it.lowercase().contains(q) } ||
                card.symbology.any { it.lowercase().contains(q) } ||
                (card.suit?.lowercase()?.contains(q) == true) ||
                card.arcana.lowercase().contains(q) ||
                card.astrology.lowercase().contains(q) ||
                card.planet.lowercase().contains(q) ||
                card.uprightMeaning.lowercase().contains(q) ||
                card.reversedMeaning.lowercase().contains(q) ||
                card.numerology.lowercase().contains(q)
            }

            matchesElement && matchesArcanaOrSuit && matchesTag && matchesQuery
        }
    }

    val hasActiveFilters = searchQuery.isNotEmpty() ||
            selectedElement != "All" ||
            selectedArcanaOrSuit != "All" ||
            selectedTag != null

    fun resetFilters() {
        searchQuery = ""
        selectedElement = "All"
        selectedArcanaOrSuit = "All"
        selectedTag = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("tarot_card_collection_view"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Search Bar at Top
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by card name, tag, or element...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search cards",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.testTag("clear_card_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search query"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_collection_search_bar")
        )

        // 2. Elemental Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Element:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            elementalOptions.forEach { element ->
                val isSelected = selectedElement.equals(element, ignoreCase = true)
                val (label, iconStr) = when (element) {
                    "Air" -> "Air" to "💨"
                    "Fire" -> "Fire" to "🔥"
                    "Water" -> "Water" to "💧"
                    "Earth" -> "Earth" to "🌿"
                    "Spirit" -> "Spirit" to "✨"
                    else -> "All" to "🔮"
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedElement = if (isSelected && element != "All") "All" else element },
                    label = { Text("$iconStr $label") },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = getElementColor(element).copy(alpha = 0.25f),
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.testTag("filter_element_${element.lowercase()}")
                )
            }
        }

        // 3. Arcana & Suit Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Type:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            arcanaAndSuitOptions.forEach { option ->
                val isSelected = selectedArcanaOrSuit.equals(option, ignoreCase = true)
                val label = when (option) {
                    "Major" -> "Major Arcana"
                    "Minor" -> "Minor Arcana"
                    "Wands" -> "🪄 Wands"
                    "Cups" -> "🏆 Cups"
                    "Swords" -> "⚔️ Swords"
                    "Pentacles" -> "🪙 Pentacles"
                    else -> "All Types"
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedArcanaOrSuit = if (isSelected && option != "All") "All" else option },
                    label = { Text(label) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_type_${option.lowercase()}")
                )
            }
        }

        // 4. Popular Tags Quick Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tags:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            popularTags.forEach { tag ->
                val isSelected = selectedTag.equals(tag, ignoreCase = true)
                SuggestionChip(
                    onClick = {
                        selectedTag = if (isSelected) null else tag
                    },
                    label = { Text("#$tag", fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tag_chip_${tag.lowercase()}")
                )
            }
        }

        // 5. Results Counter and View Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Showing ${filteredCards.size} of ${cards.size} Cards",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (hasActiveFilters) {
                    TextButton(
                        onClick = { resetFilters() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Clear Filters",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            IconButton(
                onClick = { isGridView = !isGridView },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = if (isGridView) "Switch to list view" else "Switch to grid view",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 6. Cards View (List or Grid) or Empty State
        if (filteredCards.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterListOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "No Cards Match Your Filters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try clearing search keywords or selecting 'All' for elements and types.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { resetFilters() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset All Filters")
                    }
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredCards, key = { it.id }) { card ->
                    GridCardItem(
                        card = card,
                        onCardClick = { onCardClick(card) },
                        onImageClick = { onCardImageClick(card) },
                        onTagClick = { tag -> selectedTag = tag }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredCards, key = { it.id }) { card ->
                    ListCardItem(
                        card = card,
                        onCardClick = { onCardClick(card) },
                        onImageClick = { onCardImageClick(card) },
                        onTagClick = { tag -> selectedTag = tag }
                    )
                }
            }
        }
    }
}

@Composable
private fun ListCardItem(
    card: TarotCard,
    onCardClick: () -> Unit,
    onImageClick: () -> Unit,
    onTagClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("card_item_${card.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork Thumbnail with Zoom Button
            Box(
                modifier = Modifier
                    .size(56.dp, 84.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onImageClick() }
            ) {
                AsyncImage(
                    model = TarotImageRepository.getCardImageModel(card.name),
                    contentDescription = card.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(topStart = 6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom image",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Elemental Badge
                    ElementBadge(element = card.element)
                }

                // Arcana, Planet & Astrology
                Text(
                    text = "${card.arcana} Arcana • ${card.planet} • ${card.astrology}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )

                // Meaning preview
                Text(
                    text = "Upright: ${card.uprightMeaning}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Tags Chips
                if (card.defaultTerms.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        card.defaultTerms.take(3).forEach { term ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .clickable { onTagClick(term) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = term,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridCardItem(
    card: TarotCard,
    onCardClick: () -> Unit,
    onImageClick: () -> Unit,
    onTagClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("grid_card_item_${card.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onImageClick() }
            ) {
                AsyncImage(
                    model = TarotImageRepository.getCardImageModel(card.name),
                    contentDescription = card.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    ElementBadge(element = card.element)
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(topStart = 8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom image",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = card.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${card.arcana} • ${card.astrology}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )

            if (card.defaultTerms.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    card.defaultTerms.take(2).forEach { term ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable { onTagClick(term) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = term,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ElementBadge(element: String, modifier: Modifier = Modifier) {
    val (iconStr, color) = when (element.lowercase()) {
        "air" -> "💨" to Color(0xFF0288D1)
        "fire" -> "🔥" to Color(0xFFE64A19)
        "water" -> "💧" to Color(0xFF1976D2)
        "earth" -> "🌿" to Color(0xFF388E3C)
        "spirit" -> "✨" to Color(0xFF7B1FA2)
        else -> "🔮" to Color(0xFF757575)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(text = iconStr, fontSize = 11.sp)
        Text(
            text = element,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}

private fun getElementColor(element: String): Color {
    return when (element.lowercase()) {
        "air" -> Color(0xFF0288D1)
        "fire" -> Color(0xFFE64A19)
        "water" -> Color(0xFF1976D2)
        "earth" -> Color(0xFF388E3C)
        "spirit" -> Color(0xFF7B1FA2)
        else -> Color(0xFF9E9E9E)
    }
}
