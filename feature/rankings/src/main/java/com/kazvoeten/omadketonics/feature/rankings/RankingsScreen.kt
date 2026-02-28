package com.kazvoeten.omadketonics.feature.rankings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazvoeten.omadketonics.ui.components.StarRow

@Composable
fun RankingsRoute(
    viewModel: RankingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("Meal Rankings", fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("Top rated meals are prioritized for next week", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        itemsIndexed(state.items, key = { _, item -> item.recipeId }) { index, item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("#${index + 1} ${item.name}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    StarRow(
                        rating = item.rating,
                        size = 20.dp,
                        onRate = { rating -> viewModel.onEvent(RankingsUiEvent.SetRating(item.recipeId, rating)) },
                    )
                }
            }
        }
    }
}
