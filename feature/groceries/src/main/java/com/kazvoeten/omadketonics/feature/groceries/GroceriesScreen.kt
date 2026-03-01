package com.kazvoeten.omadketonics.feature.groceries

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val BgColor = Color(0xFF0F172A)
private val CardBg = Color(0xFF1E293B)
private val CardBorder = Color(0xFF334155)
private val Primary = Color(0xFF2DD4BF)
private val TextMain = Color(0xFFF8FAFC)
private val TextMuted = Color(0xFF94A3B8)
private val Success = Color(0xFF22C55E)

@Composable
fun GroceriesRoute(
    viewModel: GroceriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is GroceriesEffect.Message -> snackbarHostState.showSnackbar(effect.value)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(24.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .background(Primary),
                    )
                    Text(
                        text = "GROCERY LIST",
                        color = TextMain,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }

            item {
                AddItemBox(
                    query = state.query,
                    enabled = state.isViewingCurrentWeek,
                    onQueryChanged = { viewModel.onEvent(GroceriesUiEvent.QueryChanged(it)) },
                    onAdd = { viewModel.onEvent(GroceriesUiEvent.AddManualItem) },
                )
            }

            if (!state.isViewingCurrentWeek) {
                item {
                    Text(
                        text = "Historical weeks are read-only.",
                        color = TextMuted,
                        fontSize = 11.sp,
                    )
                }
            }

            if (state.query.isNotBlank()) {
                item {
                    SearchResultsCard(
                        state = state,
                        enabled = state.isViewingCurrentWeek,
                        onAddFromSearch = { name -> viewModel.onEvent(GroceriesUiEvent.AddItemFromSearch(name)) },
                        onAddManual = { viewModel.onEvent(GroceriesUiEvent.AddManualItem) },
                    )
                }
            }

            if (state.sections.isEmpty()) {
                item {
                    AppCard {
                        Text(
                            text = "No groceries for this week.",
                            color = TextMuted,
                            fontSize = 13.sp,
                        )
                    }
                }
            } else {
                item {
                    AppCard {
                        state.sections.forEachIndexed { sectionIndex, section ->
                            SectionHeader(type = section.type, title = section.title)
                            section.items.forEachIndexed { itemIndex, item ->
                                GroceryRow(
                                    item = item,
                                    enabled = state.isViewingCurrentWeek,
                                    onToggle = {
                                        viewModel.onEvent(
                                            GroceriesUiEvent.ToggleItem(
                                                itemName = item.key,
                                                currentlyChecked = item.checked,
                                            ),
                                        )
                                    },
                                )
                                if (itemIndex != section.items.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(CardBorder.copy(alpha = 0.55f)),
                                    )
                                }
                            }
                            if (sectionIndex != state.sections.lastIndex) {
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }
                }

                item {
                    Surface(
                        color = Color.Transparent,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (state.isViewingCurrentWeek) 1f else 0.5f)
                            .then(
                                if (state.isViewingCurrentWeek) {
                                    Modifier.clickable { viewModel.onEvent(GroceriesUiEvent.ClearCompleted) }
                                } else {
                                    Modifier
                                },
                            ),
                    ) {
                        Text(
                            text = "Clear Completed",
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 98.dp),
            snackbar = { data ->
                Surface(
                    color = Color.White,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    shadowElevation = 8.dp,
                ) {
                    Text(
                        text = data.visuals.message,
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    )
                }
            },
        )
    }
}

@Composable
private fun AddItemBox(
    query: String,
    enabled: Boolean,
    onQueryChanged: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = CardBg,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.weight(1f),
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChanged,
                enabled = enabled,
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = TextMuted,
                    )
                },
                placeholder = {
                    Text(
                        text = "Add milk, eggs, tofu...",
                        color = TextMuted,
                        fontSize = 14.sp,
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = TextMain,
                    unfocusedTextColor = TextMain,
                    disabledTextColor = TextMuted,
                    focusedLeadingIconColor = TextMuted,
                    unfocusedLeadingIconColor = TextMuted,
                    disabledLeadingIconColor = TextMuted,
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = { onAdd() },
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Surface(
            color = Primary,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            modifier = Modifier
                .size(50.dp)
                .alpha(if (enabled) 1f else 0.45f)
                .then(if (enabled) Modifier.clickable(onClick = onAdd) else Modifier),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add item",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchResultsCard(
    state: GroceriesUiState,
    enabled: Boolean,
    onAddFromSearch: (String) -> Unit,
    onAddManual: () -> Unit,
) {
    AppCard {
        Text(
            text = "API Suggestions",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))

        when {
            state.isSearching -> {
                Text(
                    text = "Searching OpenFoodFacts...",
                    color = TextMuted,
                    fontSize = 12.sp,
                )
            }

            !state.searchError.isNullOrBlank() -> {
                Text(
                    text = state.searchError ?: "Search failed",
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "API is slow right now. Add \"${state.query.trim()}\" manually",
                    color = Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .alpha(if (enabled) 1f else 0.5f)
                        .then(if (enabled) Modifier.clickable(onClick = onAddManual) else Modifier),
                )
            }

            state.searchResults.isEmpty() -> {
                Text(
                    text = "No API matches. Add manually with +.",
                    color = TextMuted,
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Add \"${state.query.trim()}\"",
                    color = Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .alpha(if (enabled) 1f else 0.5f)
                        .then(if (enabled) Modifier.clickable(onClick = onAddManual) else Modifier),
                )
            }

            else -> {
                state.searchResults.forEachIndexed { index, result ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = result.name,
                                color = TextMain,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val brandPart = result.brand?.takeIf { it.isNotBlank() } ?: "Unknown brand"
                            Text(
                                text = "$brandPart | ${result.caloriesPer100g} kcal /100g",
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ADD",
                            color = Primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .alpha(if (enabled) 1f else 0.5f)
                                .then(
                                    if (enabled) {
                                        Modifier.clickable { onAddFromSearch(result.name) }
                                    } else {
                                        Modifier
                                    },
                                ),
                        )
                    }

                    if (index != state.searchResults.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(CardBorder.copy(alpha = 0.55f)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    type: GrocerySectionType,
    title: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
        Text(
            text = when (type) {
                GrocerySectionType.Produce -> "\uD83C\uDF43"
                GrocerySectionType.ProteinDairy -> "\uD83E\uDD5A"
                GrocerySectionType.Pantry -> "\uD83D\uDCE6"
            },
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun GroceryRow(
    item: GroceryItemUi,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.55f)
            .then(if (enabled) Modifier.clickable(onClick = onToggle) else Modifier)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .border(
                    width = 2.dp,
                    color = if (item.checked) Success else CardBorder,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                )
                .background(if (item.checked) Success else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (item.checked) {
                Text(
                    text = "\u2713",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }

        Text(
            text = item.name,
            color = if (item.checked) TextMuted else TextMain,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Surface(
            color = BgColor,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
        ) {
            Text(
                text = item.quantityLabel,
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun AppCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = CardBg,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content,
        )
    }
}

