package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.network.TarotImageRepository
import com.example.util.ImageSaver
import kotlinx.coroutines.launch

@Composable
fun ZoomableCardDialog(
    cardName: String,
    imageUrl: String,
    fallbackResId: Int? = null,
    isReversedInitially: Boolean = false,
    subtitle: String? = null,
    deckId: String = com.example.data.DeckManager.currentDeckId,
    onArtChanged: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentImageUrl by remember { mutableStateOf(imageUrl) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isReversed by remember { mutableStateOf(isReversedInitially) }
    var isSaving by remember { mutableStateOf(false) }

    var showReplaceDialog by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }
    var hasCustomArt by remember { mutableStateOf(TarotImageRepository.hasCustomCardArt(cardName, deckId)) }

    val resolvedPlaceholder = fallbackResId ?: TarotImageRepository.getCardPlaceholderRes(cardName)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val uriStr = uri.toString()
            TarotImageRepository.setCardImageUrl(cardName, uriStr, deckId, context)
            currentImageUrl = uriStr
            hasCustomArt = true
            onArtChanged?.invoke(uriStr)
            Toast.makeText(context, "Card art replaced for $cardName", Toast.LENGTH_SHORT).show()
        }
    }

    if (showReplaceDialog) {
        AlertDialog(
            onDismissRequest = { showReplaceDialog = false },
            title = { Text("Replace Art: $cardName") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select a photo from device, enter a custom URL, or pick from historical presets.")

                    Button(
                        onClick = {
                            showReplaceDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick from Photo Library")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        label = { Text("Custom Image URL (https://...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Or choose a preset style:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    TarotImageRepository.presetCardImages.forEach { (presetName, presetUrl) ->
                        OutlinedButton(
                            onClick = {
                                TarotImageRepository.setCardImageUrl(cardName, presetUrl, deckId, context)
                                currentImageUrl = presetUrl
                                hasCustomArt = true
                                onArtChanged?.invoke(presetUrl)
                                showReplaceDialog = false
                                Toast.makeText(context, "Applied $presetName", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(presetName, maxLines = 1)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customUrlInput.isNotBlank()) {
                            val clean = customUrlInput.trim()
                            TarotImageRepository.setCardImageUrl(cardName, clean, deckId, context)
                            currentImageUrl = clean
                            hasCustomArt = true
                            onArtChanged?.invoke(clean)
                            customUrlInput = ""
                            showReplaceDialog = false
                            Toast.makeText(context, "Custom art URL applied", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Apply URL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplaceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = cardName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (hasCustomArt) {
                                Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                                    Text("Custom Art", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Interactive Zoom & Pan Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.88f))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.4f
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 4.5f)
                                if (scale > 1f) {
                                    val maxOffsetX = (size.width * (scale - 1)) / 2f
                                    val maxOffsetY = (size.height * (scale - 1)) / 2f
                                    offset = Offset(
                                        x = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                        y = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = TarotImageRepository.getCardImageModel(cardName, deckId),
                        contentDescription = cardName,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                                rotationZ = if (isReversed) 180f else 0f
                            },
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(id = resolvedPlaceholder),
                        error = painterResource(id = resolvedPlaceholder)
                    )

                    // Zoom indicator badge & Reset control
                    if (scale > 1.05f) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${String.format("%.1f", scale)}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        scale = 1f
                                        offset = Offset.Zero
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.RestartAlt,
                                        contentDescription = "Reset Zoom",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Bar: Replace, Download, Delete, Share, Reverse
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Double-tap or pinch to zoom",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilterChip(
                            selected = isReversed,
                            onClick = { isReversed = !isReversed },
                            label = { Text(if (isReversed) "Reversed" else "Upright") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.FlipCameraAndroid,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    // Row 1: Replace Art & Download Art
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { showReplaceDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Replace Art")
                        }

                        Button(
                            onClick = {
                                if (!isSaving) {
                                    isSaving = true
                                    coroutineScope.launch {
                                        ImageSaver.downloadCardImage(context, currentImageUrl, cardName, resolvedPlaceholder)
                                        isSaving = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isSaving
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isSaving) "Saving..." else "Download")
                        }
                    }

                    // Row 2: Delete/Reset Art & Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                TarotImageRepository.deleteCardImageUrl(cardName, deckId, context)
                                val resetUrl = TarotImageRepository.getCardImageUrl(cardName, deckId)
                                currentImageUrl = resetUrl
                                hasCustomArt = false
                                onArtChanged?.invoke(resetUrl)
                                Toast.makeText(context, "Card art reset to default for $cardName", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (hasCustomArt) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (hasCustomArt) "Delete Art" else "Reset Default")
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    ImageSaver.shareCardImage(context, currentImageUrl, cardName, resolvedPlaceholder)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share")
                        }
                    }
                }
            }
        }
    }
}
