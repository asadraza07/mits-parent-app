package com.mitsschools.parent.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mitsschools.parent.data.model.SchoolClass
import com.mitsschools.parent.ui.AppViewModel
import com.mitsschools.parent.ui.screens.CalendarScreen
import com.mitsschools.parent.ui.screens.ClassSelectScreen
import com.mitsschools.parent.ui.screens.DailyDiaryScreen
import com.mitsschools.parent.ui.screens.DashboardScreen
import com.mitsschools.parent.ui.screens.LoginScreen
import com.mitsschools.parent.ui.screens.NotificationsScreen
import com.mitsschools.parent.ui.screens.ProfileScreen
import com.mitsschools.parent.ui.screens.WeeklyDiaryScreen

object Routes {
    const val LOGIN = "login"
    const val CLASSES = "classes"
    const val HOME = "home"
    const val DAILY = "daily"
    const val WEEKLY = "weekly"
    const val CALENDAR = "calendar"
    const val ALERTS = "alerts"
    const val PROFILE = "profile"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "Home", Icons.Rounded.Home),
    Tab(Routes.DAILY, "Daily", Icons.AutoMirrored.Rounded.MenuBook),
    Tab(Routes.WEEKLY, "Weekly", Icons.AutoMirrored.Rounded.EventNote),
    Tab(Routes.CALENDAR, "Calendar", Icons.Rounded.CalendarMonth),
    Tab(Routes.ALERTS, "Alerts", Icons.Rounded.Notifications),
)

@Composable
fun AppNavigation(vm: AppViewModel) {
    val nav = rememberNavController()
    val schoolClass by vm.selectedClass.collectAsStateWithLifecycle()
    val notifications by vm.notifications.collectAsStateWithLifecycle()
    val readIds by vm.readIds.collectAsStateWithLifecycle()
    val unread = notifications.count { it.id !in readIds }

    // Decided once at launch; later changes are handled by explicit navigation.
    val start = remember {
        when {
            vm.session.value == null -> Routes.LOGIN
            vm.selectedClass.value == null -> Routes.CLASSES
            else -> Routes.HOME
        }
    }

    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val showBottomBar = tabs.any { it.route == current }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = current == tab.route,
                            onClick = { nav.switchTab(tab.route) },
                            icon = {
                                if (tab.route == Routes.ALERTS && unread > 0) {
                                    BadgedBox(badge = { Badge { Text(unread.toString()) } }) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.fillMaxSize().padding(padding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(vm) {
                    val next = if (vm.selectedClass.value == null) Routes.CLASSES else Routes.HOME
                    nav.navigate(next) { popUpTo(nav.graph.id) { inclusive = true } }
                }
            }
            composable(Routes.CLASSES) {
                val canGoBack = nav.previousBackStackEntry != null
                ClassSelectScreen(
                    current = schoolClass,
                    onBack = if (canGoBack) ({ nav.popBackStack(); Unit }) else null,
                    onConfirm = { cls ->
                        vm.selectClass(cls)
                        if (canGoBack) {
                            nav.popBackStack()
                        } else {
                            nav.navigate(Routes.HOME) { popUpTo(nav.graph.id) { inclusive = true } }
                        }
                    },
                )
            }
            composable(Routes.HOME) {
                WithClass(schoolClass) { cls ->
                    DashboardScreen(
                        vm = vm,
                        schoolClass = cls,
                        onOpenNotifications = { nav.switchTab(Routes.ALERTS) },
                        onOpenDaily = { nav.switchTab(Routes.DAILY) },
                        onOpenWeekly = { nav.switchTab(Routes.WEEKLY) },
                        onOpenCalendar = { nav.switchTab(Routes.CALENDAR) },
                        onOpenProfile = { nav.navigate(Routes.PROFILE) },
                        onChangeClass = { nav.navigate(Routes.CLASSES) },
                    )
                }
            }
            composable(Routes.DAILY) { WithClass(schoolClass) { DailyDiaryScreen(vm, it) } }
            composable(Routes.WEEKLY) { WithClass(schoolClass) { WeeklyDiaryScreen(vm, it) } }
            composable(Routes.CALENDAR) { WithClass(schoolClass) { CalendarScreen(vm, it) } }
            composable(Routes.ALERTS) { NotificationsScreen(vm) }
            composable(Routes.PROFILE) {
                WithClass(schoolClass) { cls ->
                    ProfileScreen(
                        vm = vm,
                        schoolClass = cls,
                        onBack = { nav.popBackStack() },
                        onChangeClass = { nav.navigate(Routes.CLASSES) },
                        onLoggedOut = { nav.navigate(Routes.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } } },
                    )
                }
            }
        }
    }
}

/** Screens below the class picker need a class; during sign-out it can briefly be null. */
@Composable
private fun WithClass(
    schoolClass: SchoolClass?,
    content: @Composable (SchoolClass) -> Unit,
) {
    if (schoolClass != null) content(schoolClass)
}

private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
