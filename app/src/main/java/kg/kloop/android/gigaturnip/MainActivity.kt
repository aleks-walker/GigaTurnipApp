package kg.kloop.android.gigaturnip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import kg.kloop.android.gigaturnip.ui.campaigns.CampaignsScreen
import kg.kloop.android.gigaturnip.ui.campaigns.CampaignsScreenView
import kg.kloop.android.gigaturnip.ui.tasks.*
import kg.kloop.android.gigaturnip.ui.theme.GigaTurnipTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GigaTurnipTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    CampaignsScreenView(navController = navController)
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = { Toolbar("AppBar", scaffoldState.drawerState, scope) },
        drawerContent = { Text(text = "Drawer") },
        scaffoldState = scaffoldState,
    )
    { innerPadding ->
        MainNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )

    }
}

@Composable
fun MainNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController,
        startDestination = CampaignsScreen.CampaignScreen.route,
        modifier = modifier
    ) {
        composable(CampaignsScreen.CampaignScreen.route) { CampaignsScreenView(navController) }
        navigation(TasksScreen.InProgress.route, TasksScreen.TasksList.route) {
            composable(TasksScreen.InProgress.route) { TasksScreenView() }
        }
    }

}

@Composable
fun Toolbar(title: String, drawerState: DrawerState, scope: CoroutineScope) {
    TopAppBar(
        title = {
            Text(text = title)
        },
        navigationIcon = {
            IconButton(onClick = { scope.launch { if (drawerState.isClosed) drawerState.open() } }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_dehaze_24),
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp)
                )
            }
        })
}
