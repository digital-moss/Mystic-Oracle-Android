package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageSaver {

    suspend fun downloadCardImage(
        context: Context,
        imageUrl: String,
        cardName: String,
        fallbackResId: Int? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadBitmap(context, imageUrl, fallbackResId)
                ?: return@withContext false

            val sanitizedName = cardName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val filename = "MysticOracle_${sanitizedName}_${System.currentTimeMillis()}.jpg"

            var success = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MysticOracle")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                        success = true
                    }
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(imagesDir, "MysticOracle").apply { if (!exists()) mkdirs() }
                val imageFile = File(appDir, filename)
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    success = true
                }
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Saved $cardName to Pictures/MysticOracle", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Could not save card image", Toast.LENGTH_SHORT).show()
                }
            }
            success
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Save error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }

    suspend fun shareCardImage(
        context: Context,
        imageUrl: String,
        cardName: String,
        fallbackResId: Int? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadBitmap(context, imageUrl, fallbackResId) ?: return@withContext
            val cachePath = File(context.cacheDir, "shared_cards").apply { if (!exists()) mkdirs() }
            val file = File(cachePath, "share_${cardName.replace(" ", "_")}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Tarot Card: $cardName")
                putExtra(Intent.EXTRA_TEXT, "Drawn from Mystic Oracle: $cardName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(shareIntent, "Share $cardName Art"))
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Share failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun loadBitmap(context: Context, imageUrl: String, fallbackResId: Int?): Bitmap? {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            } else if (fallbackResId != null) {
                BitmapFactory.decodeResource(context.resources, fallbackResId)
            } else null
        } catch (_: Exception) {
            if (fallbackResId != null) {
                BitmapFactory.decodeResource(context.resources, fallbackResId)
            } else null
        }
    }
}
