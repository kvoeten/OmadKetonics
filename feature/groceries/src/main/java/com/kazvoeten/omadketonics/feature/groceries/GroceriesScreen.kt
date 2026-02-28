package com.kazvoeten.omadketonics.feature.groceries

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GroceriesRoute(
    viewModel: GroceriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Groceries", fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("Aggregated from displayed week meals", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (state.sections.isEmpty()) {
            item {
                Text("No groceries for this week.")
            }
        }

        items(state.sections, key = { it.category.name }) { section ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(section.category.name.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    section.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = state.isViewingCurrentWeek) {
                                    viewModel.onEvent(
                                        GroceriesUiEvent.ToggleItem(
                                            itemName = item.name,
                                            currentlyChecked = item.checked,
                                        ),
                                    )
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(if (item.checked) "[x]" else "[ ]")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.name,
                                textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                            )
                        }
                    }
                }
            }
        }
    }
}
