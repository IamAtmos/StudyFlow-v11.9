package com.studyflow.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.studyflow.app.ui.*
import com.studyflow.app.ui.theme.*
import com.studyflow.app.viewmodel.StudyViewModel
import com.studyflow.app.viewmodel.TimerViewModel

class MainActivity : ComponentActivity() {
    private val timerViewModel: TimerViewModel by viewModels()
    private val studyViewModel: StudyViewModel  by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this) // before setContent — no flash of wrong colors
        setContent {
            StudyFlowTheme {
                RequestPermissions()
                StudyFlowApp(timerViewModel, studyViewModel)
            }
        }
    }
}

@Composable
private fun RequestPermissions() {
    val context = LocalContext.current
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Composable
fun StudyFlowApp(timerViewModel: TimerViewModel, studyViewModel: StudyViewModel) {
    val navController  = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route
    val topLevel       = setOf("timer", "study", "weekly")

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (currentRoute in topLevel) {
                BottomNav(currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = "study",
            modifier         = Modifier.padding(padding),
        ) {
            composable("timer")  { TimerScreen(timerViewModel) }
            composable("study")  {
                StudyScreen(
                    viewModel          = studyViewModel,
                    onHistoryClick     = { navController.navigate("history") },
                    onDailyBoardClick  = { navController.navigate("dailyboard") },
                )
            }
            composable("weekly") { WeeklyScreen(studyViewModel) }
            composable("history") {
                Column(Modifier.fillMaxSize().background(Background)) {
                    SubScreenHeader(title = "History") { navController.popBackStack() }
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        HistoryScreen(studyViewModel)
                    }
                }
            }
            composable("dailyboard") {
                Column(Modifier.fillMaxSize().background(Background)) {
                    SubScreenHeader(title = "Daily Board") { navController.popBackStack() }
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        DailyBoardScreen(
                            viewModel = studyViewModel,
                            onStartStudySession = { item ->
                                studyViewModel.startStudySessionForItem(item)
                                navController.popBackStack()
                            },
                        )
                    }
                }
            }
        }
    }
}

/** Shared header for pushed sub-screens (History, Daily Board): icon back
 *  button + title, with a hairline divider beneath — consistent, minimal. */
@Composable
private fun SubScreenHeader(title: String, onBack: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().background(Background)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                Icons.Outlined.ArrowBack, contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp).clickable { onBack() },
            )
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        }
        Divider(color = TextSecondary.copy(alpha = 0.08f), thickness = 1.dp)
    }
}

// ─── Bottom nav: icon-only, with a small glowing indicator bar ───────────────

@Composable
private fun BottomNav(currentRoute: String?, onNav: (String) -> Unit) {
    Column {
        Divider(color = TextSecondary.copy(alpha = 0.08f), thickness = 1.dp)
        NavigationBar(containerColor = Surface, tonalElevation = 0.dp) {
            NavigationBarItem(
                selected = currentRoute == "timer",
                onClick  = { onNav("timer") },
                icon     = { NavIcon(currentRoute == "timer", Icons.Outlined.Timer, Icons.Filled.Timer) },
                label    = null,
                colors   = navColors(),
            )
            NavigationBarItem(
                selected = currentRoute == "study" || currentRoute == null,
                onClick  = { onNav("study") },
                icon     = { NavIcon(currentRoute == "study" || currentRoute == null, Icons.Outlined.MenuBook, Icons.Filled.MenuBook) },
                label    = null,
                colors   = navColors(),
            )
            NavigationBarItem(
                selected = currentRoute == "weekly",
                onClick  = { onNav("weekly") },
                icon     = { NavIcon(currentRoute == "weekly", Icons.Outlined.BarChart, Icons.Filled.BarChart) },
                label    = null,
                colors   = navColors(),
            )
        }
    }
}

@Composable
private fun NavIcon(selected: Boolean, outlined: androidx.compose.ui.graphics.vector.ImageVector, filled: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = if (selected) filled else outlined,
            contentDescription = null,
            tint = if (selected) Primary else TextSecondary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = 2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (selected) Primary else androidx.compose.ui.graphics.Color.Transparent),
        )
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor   = Primary,
    unselectedIconColor = TextSecondary,
    indicatorColor      = androidx.compose.ui.graphics.Color.Transparent,
)
