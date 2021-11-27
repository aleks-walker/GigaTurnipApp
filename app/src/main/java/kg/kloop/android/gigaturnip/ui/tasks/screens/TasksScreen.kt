package kg.kloop.android.gigaturnip.ui.tasks.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kg.kloop.android.gigaturnip.AppDrawer
import kg.kloop.android.gigaturnip.MainActivityViewModel
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.ui.Toolbar
import kg.kloop.android.gigaturnip.ui.components.TryAgainScreen
import kg.kloop.android.gigaturnip.ui.tasks.TasksViewModel
import kg.kloop.android.gigaturnip.ui.theme.ColorPalette
import kotlinx.coroutines.launch


sealed class TasksScreen(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object TasksList : TasksScreen("tasks_list", R.string.tasks_list, Icons.Filled.Home)
    object InProgress :
        TasksScreen("tasks_in_progress", R.string.in_progress, Icons.Filled.ArrowForward)

    object Finished : TasksScreen("tasks_finished", R.string.finished, Icons.Filled.Done)

    object Details : TasksScreen("task_details", R.string.details, Icons.Filled.ArrowDropDown)
    object Creatable : TasksScreen("task_creatable", R.string.creatable, Icons.Filled.ArrowDropDown)
}


@Composable
fun TasksScreenView(
    mainActivityViewModel: MainActivityViewModel,
    viewModel: TasksViewModel = hiltViewModel(),
    onFabClick: () -> Unit,
    navigateToDetails: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onLogOutClick: () -> Unit,
) {
    val user by mainActivityViewModel.user.observeAsState()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val coroutineScope = rememberCoroutineScope()
    val openDrawer: () -> Unit = {
        coroutineScope.launch { scaffoldState.drawerState.open() }
    }
    refreshOnce(viewModel)

    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()

    val items = listOf(
        TasksScreen.InProgress,
        TasksScreen.Finished,
    )
    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = { AppDrawer(user) },
        topBar = {
            Toolbar(
                title = stringResource(R.string.app_name),
                newNotificationsCount = uiState.newNotificationsCount,
                openDrawer = openDrawer,
                onLogOutClick = { onLogOutClick() },
                onNotificationsClick = { onNotificationsClick() }
            )
        },
        bottomBar = {
            TasksBottomNavigation(navController = navController, items = items)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onFabClick() },
                shape = RoundedCornerShape(50),
                backgroundColor = ColorPalette.primary
            ) {
                Icon(Icons.Filled.Add, "")
            }
        },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,
    )
    { innerPadding ->
        if (uiState.error) {
            TryAgainScreen(text = stringResource(id = R.string.error_occured)) {
                viewModel.refreshTasks()
            }
        } else {
            NavHost(
                navController,
                startDestination = TasksScreen.InProgress.route,
                Modifier.padding(innerPadding)
            ) {
                composable(TasksScreen.InProgress.route) {
                    TasksInProgress(
                        navigateToDetails,
                        uiState.inProgressTasks.sortedByDescending { it.id.toInt() },
                        uiState.loading,
                        onRefresh = { viewModel.refreshTasks() }
                    )
                }
                composable(TasksScreen.Finished.route) {
                    TasksFinished(
                        navigateToDetails,
                        uiState.finishedTasks.sortedByDescending { it.id.toInt() },
                        uiState.loading,
                        onRefresh = { viewModel.refreshTasks() }
                    )
                }
            }
        }
    }
}

@Composable
private fun refreshOnce(viewModel: TasksViewModel) {
    var refresh by remember { mutableStateOf(true) }
    if (refresh) {
        viewModel.refreshTasks()
        refresh = false
    }
}


@Composable
private fun TasksBottomNavigation(
    navController: NavHostController,
    items: List<TasksScreen>
) {
    return BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(stringResource(screen.resourceId)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
            )
        }
    }
}
