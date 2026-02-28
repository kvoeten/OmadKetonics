package com.kazvoeten.omadketonics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

private enum class MacroBarSize { Sm, Md, Lg }

@Composable
fun MacroBar(
    protein: Int,
    carbs: Int,
    fat: Int,
    showLabels: Boolean = false,
    size: MacroVisualSize = MacroVisualSize.Medium,
) {
    val internalSize = when (size) {
        MacroVisualSize.Small -> MacroBarSize.Sm
        MacroVisualSize.Medium -> MacroBarSize.Md
        MacroVisualSize.Large -> MacroBarSize.Lg
    }

    val total = max(1, protein + carbs + fat).toFloat()
    val carbsPct = (carbs / total) * 100f
    val proteinPct = (protein / total) * 100f
    val fatPct = (fat / total) * 100f

    val textSize = when (internalSize) {
        MacroBarSize.Sm -> 9.sp
        MacroBarSize.Md -> 10.sp
        MacroBarSize.Lg -> 11.sp
    }
    val threshold = if (internalSize == MacroBarSize.Sm) 15f else 8f
    val barHeight = when (internalSize) {
        MacroBarSize.Sm -> 14.dp
        MacroBarSize.Md -> 16.dp
        MacroBarSize.Lg -> 24.dp
    }

    if (showLabels) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Carbs", color = Color(0xFF059669), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Pro", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Fat", color = Color(0xFFF43F5E), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(RoundedCornerShape(999.dp))
            .border(1.dp, Color(0x99F3F4F6), RoundedCornerShape(999.dp)),
    ) {
        MacroSegment(
            weight = carbsPct,
            text = carbs.toString(),
            showText = carbsPct >= threshold,
            bgColor = Color(0xFFA7F3D0),
            textColor = Color(0xFF065F46),
            textSize = textSize,
            leadingBorder = false,
        )
        MacroSegment(
            weight = proteinPct,
            text = protein.toString(),
            showText = proteinPct >= threshold,
            bgColor = Color(0xFFFDE68A),
            textColor = Color(0xFF92400E),
            textSize = textSize,
            leadingBorder = true,
        )
        MacroSegment(
            weight = fatPct,
            text = fat.toString(),
            showText = fatPct >= threshold,
            bgColor = Color(0xFFFDA4AF),
            textColor = Color(0xFF9F1239),
            textSize = textSize,
            leadingBorder = true,
        )
    }
}

@Composable
private fun RowScope.MacroSegment(
    weight: Float,
    text: String,
    showText: Boolean,
    bgColor: Color,
    textColor: Color,
    textSize: TextUnit,
    leadingBorder: Boolean,
) {
    Box(
        modifier = Modifier
            .weight(max(0.0001f, weight))
            .fillMaxHeight()
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        if (leadingBorder) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color(0x66FFFFFF)),
            )
        }
        if (showText) {
            Text(
                text = text,
                color = textColor,
                fontSize = textSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    lineHeight = textSize,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
            )
        }
    }
}

enum class MacroVisualSize {
    Small,
    Medium,
    Large,
}

@Composable
fun StarRow(
    rating: Int,
    size: Dp,
    onRate: ((Int) -> Unit)?,
) {
    Row {
        (1..5).forEach { star ->
            Icon(
                imageVector = if (star <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = null,
                tint = if (star <= rating) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                modifier = Modifier
                    .size(size)
                    .then(if (onRate != null) Modifier.clickable { onRate(star) } else Modifier),
            )
        }
    }
}
