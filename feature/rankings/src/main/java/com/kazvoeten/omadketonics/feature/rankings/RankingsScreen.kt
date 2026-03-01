package com.kazvoeten.omadketonics.feature.rankings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazvoeten.omadketonics.ui.components.StarRow

private val RankPrimary = Color(0xFF2DD4BF)
private val RankCardColor = Color(0xFF1E293B)
private val RankBorder = Color(0xFF334155)
private val RankIconBg = Color(0xFF0F172A)
private val RankMuted = Color(0xFF94A3B8)

@Composable
fun RankingsRoute(
    viewModel: RankingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 24.dp)
                        .background(RankPrimary, RoundedCornerShape(4.dp)),
                )
                Text(
                    text = "Top Rankings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }

        itemsIndexed(state.items, key = { _, item -> item.recipeId }) { index, item ->
            RankCard(
                rank = index + 1,
                name = item.name,
                icon = item.icon,
                rating = item.rating,
                loggedCount = item.loggedCount,
                metaTag = item.metaTag,
                faded = index >= 3,
                onRate = { rating -> viewModel.onEvent(RankingsUiEvent.SetRating(item.recipeId, rating)) },
            )
        }

        item {
            Text(
                text = "Ratings help prioritize your weekly auto-generator.",
                color = RankMuted,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun RankCard(
    rank: Int,
    name: String,
    icon: String,
    rating: Int,
    loggedCount: Int,
    metaTag: String,
    faded: Boolean,
    onRate: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = RankCardColor, shape = RoundedCornerShape(24.dp))
            .border(width = 1.dp, color = RankBorder, shape = RoundedCornerShape(24.dp))
            .alpha(if (faded) 0.82f else 1f)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "#$rank",
            color = RankPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.size(width = 34.dp, height = 24.dp),
        )

        Box(
            modifier = Modifier
                .size(50.dp)
                .background(color = RankIconBg, shape = RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = icon, fontSize = 24.sp)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            StarRow(
                rating = rating,
                size = 18.dp,
                onRate = onRate,
            )
            Text(
                text = "Logged $loggedCount times | $metaTag",
                color = RankMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
