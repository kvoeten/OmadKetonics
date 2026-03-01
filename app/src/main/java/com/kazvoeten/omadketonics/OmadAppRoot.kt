package com.kazvoeten.omadketonics

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kazvoeten.omadketonics.feature.groceries.GroceriesRoute
import com.kazvoeten.omadketonics.feature.plan.PlanRoute
import com.kazvoeten.omadketonics.feature.progress.ProgressRoute
import com.kazvoeten.omadketonics.feature.rankings.RankingsRoute
import com.kazvoeten.omadketonics.feature.recipes.RecipesRoute
import com.kazvoeten.omadketonics.model.OmadTab
import com.kazvoeten.omadketonics.model.ProgressDeepLinkMetric
import androidx.compose.runtime.DisposableEffect

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
private const val HealthConnectProviderPackage = "com.google.android.apps.healthdata"

@Composable
fun OmadAppRoot(
    bootstrapViewModel: AppBootstrapViewModel = hiltViewModel(),
    weekNavViewModel: WeekNavViewModel = hiltViewModel(),
    healthConnectViewModel: AppHealthConnectViewModel = hiltViewModel(),
) {
    val ready by bootstrapViewModel.ready.collectAsStateWithLifecycle()
    val weekNav by weekNavViewModel.state.collectAsStateWithLifecycle()
    val healthConnectState by healthConnectViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = MaterialTheme.colorScheme
    val rootBackground = colors.background
    val rootPanel = colors.surface
    val rootBorder = colors.outline
    val rootPrimary = colors.primary
    val rootMuted = colors.onSurfaceVariant
    val rootText = colors.onSurface
    var activeTab by rememberSaveable { mutableStateOf(OmadTab.Plan) }
    var pendingRecipeToEdit by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingProgressMetric by rememberSaveable { mutableStateOf<ProgressDeepLinkMetric?>(null) }
    var pendingProgressOpenLogger by rememberSaveable { mutableStateOf(false) }
    val requiredPermissions = healthConnectViewModel.requiredPermissions()

    val permissionsLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        Log.d("HealthConnect", "Startup permission result: granted=${granted.size} required=${requiredPermissions.size}")
        healthConnectViewModel.onEvent(AppHealthConnectEvent.PermissionsResult(granted))
        if (!granted.containsAll(requiredPermissions)) {
            runCatching {
                context.startActivity(
                    HealthConnectClient.getHealthConnectManageDataIntent(
                        context,
                        HealthConnectProviderPackage,
                    ),
                )
            }.onFailure {
                Toast.makeText(context, "Open Health Connect settings to grant permissions", Toast.LENGTH_SHORT).show()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                healthConnectViewModel.onEvent(AppHealthConnectEvent.RefreshPermissionState)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
        containerColor = rootBackground,
        topBar = {
            when (activeTab) {
                OmadTab.Plan -> {
                    Surface(
                        color = rootPanel,
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
                                    mutedColor = rootMuted,
                                    disabledColor = rootBorder,
                                    onClick = weekNavViewModel::goToOlderWeek,
                                )

                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = weekNav.title.replace("Week Of", "Week of"),
                                        color = rootText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarMonth,
                                        contentDescription = null,
                                        tint = rootMuted,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }

                                WeekArrow(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = "Next week",
                                    enabled = weekNav.hasNewerWeek,
                                    mutedColor = rootMuted,
                                    disabledColor = rootBorder,
                                    onClick = weekNavViewModel::goToNewerWeek,
                                )
                            }

                            Text(
                                text = weekNav.subtitle,
                                color = rootMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 5.dp),
                            )
                        }
                    }
                }

                OmadTab.Progress -> {
                    SimpleTopBar(
                        title = "HEALTH DASHBOARD",
                        rightIcon = Icons.Rounded.Sync,
                        rootPanel = rootPanel,
                        rootText = rootText,
                        rootMuted = rootMuted,
                    )
                }

                else -> {
                    val title = bottomTabs.firstOrNull { it.tab == activeTab }?.label?.uppercase() ?: activeTab.label.uppercase()
                    SimpleTopBar(
                        title = title,
                        rootPanel = rootPanel,
                        rootText = rootText,
                        rootMuted = rootMuted,
                    )
                }
            }
            HorizontalDivider(color = rootBorder)
        },
        bottomBar = {
            Surface(
                color = rootPanel,
                border = BorderStroke(1.dp, rootBorder),
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
                                .clickable {
                                    activeTab = tab.tab
                                    if (tab.tab != OmadTab.Recipes) {
                                        pendingRecipeToEdit = null
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (selected) rootPrimary else rootMuted,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = tab.label,
                                color = if (selected) rootPrimary else rootMuted,
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
                .background(rootBackground)
                .padding(innerPadding),
        ) {
            when (activeTab) {
                OmadTab.Plan -> PlanRoute(
                    onEditRecipe = { recipeId ->
                        pendingRecipeToEdit = recipeId
                        activeTab = OmadTab.Recipes
                    },
                    onOpenProgressMetric = { metric, openLogger ->
                        pendingProgressMetric = metric
                        pendingProgressOpenLogger = openLogger
                        activeTab = OmadTab.Progress
                    },
                )
                OmadTab.Groceries -> GroceriesRoute()
                OmadTab.Recipes -> RecipesRoute(
                    openEditorRecipeId = pendingRecipeToEdit,
                    onOpenEditorConsumed = { pendingRecipeToEdit = null },
                )
                OmadTab.Rankings -> RankingsRoute()
                OmadTab.Progress -> ProgressRoute(
                    openMetric = pendingProgressMetric,
                    openActivityLogger = pendingProgressOpenLogger,
                    onDeepLinkConsumed = {
                        pendingProgressMetric = null
                        pendingProgressOpenLogger = false
                    },
                )
            }
        }

        if (healthConnectState.showPermissionPrompt) {
            AlertDialog(
                onDismissRequest = { healthConnectViewModel.onEvent(AppHealthConnectEvent.DismissPrompt) },
                title = { Text("Connect Health Connect") },
                text = {
                    Text(
                        "Enable permissions to sync sleep, exercise, and nutrition automatically. " +
                            "You can still use the app without this and connect later.",
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            Log.d("HealthConnect", "Startup connect tapped")
                            healthConnectViewModel.onEvent(AppHealthConnectEvent.DismissPrompt)
                            runCatching {
                                permissionsLauncher.launch(requiredPermissions)
                            }.onFailure {
                                runCatching {
                                    context.startActivity(
                                        HealthConnectClient.getHealthConnectManageDataIntent(
                                            context,
                                            HealthConnectProviderPackage,
                                        ),
                                    )
                                }.onFailure {
                                    Toast.makeText(context, "Unable to open Health Connect settings", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    ) {
                        Text("Connect")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { healthConnectViewModel.onEvent(AppHealthConnectEvent.DismissPrompt) }) {
                        Text("Not now")
                    }
                },
            )
        }
    }
}

@Composable
private fun SimpleTopBar(
    title: String,
    rootPanel: Color,
    rootText: Color,
    rootMuted: Color,
    rightIcon: ImageVector? = null,
) {
    Surface(
        color = rootPanel,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(24.dp))
            Text(
                text = title,
                color = rootText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (rightIcon != null) {
                Icon(
                    imageVector = rightIcon,
                    contentDescription = null,
                    tint = rootMuted,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Box(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun WeekArrow(
    imageVector: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    mutedColor: Color,
    disabledColor: Color,
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
            tint = if (enabled) mutedColor else disabledColor,
            modifier = Modifier.size(20.dp),
        )
    }
}
