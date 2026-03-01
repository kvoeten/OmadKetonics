package com.kazvoeten.omadketonics.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RecipeImage(
    imageUri: String?,
    fallbackIcon: String,
    modifier: Modifier = Modifier,
    iconFontSize: TextUnit = 42.sp,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = imageUri) {
        value = loadBitmap(context, imageUri)
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = fallbackIcon.ifBlank { "\uD83C\uDF7D\uFE0F" },
                fontSize = iconFontSize,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private suspend fun loadBitmap(
    context: Context,
    imageUri: String?,
): android.graphics.Bitmap? = withContext(Dispatchers.IO) {
    if (imageUri.isNullOrBlank()) return@withContext null
    runCatching {
        val stream = resolveInputStream(context, imageUri) ?: return@runCatching null
        stream.use {
            BitmapFactory.decodeStream(it)
        }
    }.getOrNull()
}

private fun resolveInputStream(
    context: Context,
    imageUri: String,
): java.io.InputStream? {
    return when {
        imageUri.startsWith("content://") || imageUri.startsWith("file://") -> {
            context.contentResolver.openInputStream(Uri.parse(imageUri))
        }

        else -> {
            val file = File(imageUri)
            if (file.exists()) {
                file.inputStream()
            } else {
                context.contentResolver.openInputStream(Uri.parse(imageUri))
            }
        }
    }
}
