package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import com.example.model.TarotCard
import com.example.network.TarotImageRepository

@Composable
fun SpreadLayout(
    spreadType: String,
    cardsWithPositions: List<Pair<String, Pair<TarotCard, Boolean>>>,
    onCardClick: (TarotCard) -> Unit
) {
    val positions = when {
        spreadType.contains("Celtic", true) -> listOf(
            0.50f to 0.48f, 0.50f to 0.48f, 0.50f to 0.82f, 0.50f to 0.14f,
            0.26f to 0.48f, 0.74f to 0.48f, 0.90f to 0.18f, 0.90f to 0.40f,
            0.90f to 0.62f, 0.90f to 0.84f
        )
        spreadType.contains("Horseshoe", true) -> listOf(
            0.08f to 0.58f, 0.22f to 0.34f, 0.40f to 0.18f,
            0.60f to 0.18f, 0.78f to 0.34f, 0.92f to 0.58f, 0.50f to 0.78f
        )
        spreadType.contains("Relationship", true) -> listOf(
            0.24f to 0.22f, 0.76f to 0.22f, 0.24f to 0.54f,
            0.76f to 0.54f, 0.50f to 0.82f
        )
        spreadType.contains("Five-Card", true) -> listOf(
            0.50f to 0.16f, 0.50f to 0.50f, 0.50f to 0.84f,
            0.22f to 0.50f, 0.78f to 0.50f
        )
        else -> listOf(0.20f to 0.50f, 0.50f to 0.50f, 0.80f to 0.50f)
    }
    val boardHeight = if (spreadType.contains("Celtic", true)) 420.dp else 260.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(boardHeight)
                .padding(12.dp)
        ) {
            val cardWidth = 58.dp
            val cardHeight = 88.dp
            cardsWithPositions.forEachIndexed { index, (_, pair) ->
                val (card, isReversed) = pair
                val position = positions.getOrElse(index) { 0.5f to 0.5f }
                AsyncImage(
                    model = TarotImageRepository.getCardImageUrl(card.name),
                    contentDescription = card.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(cardWidth)
                        .height(cardHeight)
                        .offset(
                            x = (maxWidth - cardWidth) * position.first,
                            y = (maxHeight - cardHeight) * position.second
                        )
                        .clip(RoundedCornerShape(7.dp))
                        .clickable { onCardClick(card) }
                        .graphicsLayer {
                            rotationZ = when {
                                spreadType.contains("Celtic", true) && index == 1 -> 90f
                                isReversed -> 180f
                                else -> 0f
                            }
                        }
                )
            }
        }
    }
}
