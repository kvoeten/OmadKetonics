package com.kazvoeten.omadketonics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazvoeten.omadketonics.feature.groceries.GroceriesRoute
import com.kazvoeten.omadketonics.feature.plan.PlanRoute
import com.kazvoeten.omadketonics.feature.progress.ProgressRoute
import com.kazvoeten.omadketonics.feature.rankings.RankingsRoute
import com.kazvoeten.omadketonics.feature.recipes.RecipesRoute
import com.kazvoeten.omadketonics.model.OmadTab

private val RootBackground = Color.Black
private val RootPanel = Color(0xFF121214)
private val RootBorder = Color(0xFF2A2A30)
private val RootPrimary = Color(0xFFC4D0FF)
private val RootMuted = Color(0xFF8E8E99)
private val RootText = Color.White

private data class BottomTab(
    val tab: OmadTab,
    val label: String,
    val icon: ImageVector,
)

private val bottomTabs = listOf(
    BottomTab(OmadTab.Plan, "Plan", Icons.Rounded.CalendarMonth),
    BottomTab(OmadTab.Groceries, "List", Icons.Rounded.LocalGroceryStore),
    BottomTab(OmadTab.Recipes, "Recipes", Icons.AutoMirrored.Rounded.MenuBook),
    BottomTab(OmadTab.Rankings, "Ranks", Icons.Rounded.FormatListNumbered),
    BottomTab(OmadTab.Progress, "Progress", Icons.AutoMirrored.Rounded.TrendingUp),
)

@Composable
fun OmadAppRoot(
    bootstrapViewModel: AppBootstrapViewModel = hiltViewModel(),
    weekNavViewModel: WeekNavViewModel = hiltViewModel(),
) {
    val ready by bootstrapViewModel.ready.collectAsStateWithLifecycle()
    val weekNav by weekNavViewModel.state.collectAsStateWithLifecycle()
    var activeTab by rememberSaveable { mutableStateOf(OmadTab.Plan) }

    if (!ready) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = RootBackground,
        topBar = {
            Surface(
                color = RootPanel,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WeekArrow(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Previous week",
                            enabled = weekNav.hasOlderWeek,
                            onClick = weekNavViewModel::goToOlderWeek,
                        )

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = weekNav.title.replace("Week Of", "Week of"),
                                color = RootText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = RootMuted,
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        WeekArrow(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Next week",
                            enabled = weekNav.hasNewerWeek,
                            onClick = weekNavViewModel::goToNewerWeek,
                        )
                    }

                    Text(
                        text = weekNav.subtitle,
                        color = RootMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp),
                    )
                }
            }
            HorizontalDivider(color = RootBorder)
        },
        bottomBar = {
            Surface(
                color = RootPanel,
                border = BorderStroke(1.dp, RootBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 10.dp, top = 12.dp, end = 10.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    bottomTabs.forEach { tab ->
                        val selected = activeTab == tab.tab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = tab.tab },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (selected) RootPrimary else RootMuted,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = tab.label,
                                color = if (selected) RootPrimary else RootMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RootBackground)
                .padding(innerPadding),
        ) {
            when (activeTab) {
                OmadTab.Plan -> PlanRoute()
                OmadTab.Groceries -> GroceriesRoute()
                OmadTab.Recipes -> RecipesRoute()
                OmadTab.Rankings -> RankingsRoute()
                OmadTab.Progress -> ProgressRoute()
            }
        }
    }
}

@Composable
private fun WeekArrow(
    imageVector: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color = Color.Transparent, shape = CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = if (enabled) RootMuted else RootBorder,
            modifier = Modifier.size(20.dp),
        )
    }
}
