package kg.kloop.android.gigaturnip

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.ui.Toolbar
import kg.kloop.android.gigaturnip.ui.campaigns.CampaignsScreen
import kg.kloop.android.gigaturnip.ui.campaigns.CampaignsScreenView
import kg.kloop.android.gigaturnip.ui.tasks.TaskDetails
import kg.kloop.android.gigaturnip.ui.tasks.TasksCreatable
import kg.kloop.android.gigaturnip.ui.tasks.TasksScreen
import kg.kloop.android.gigaturnip.ui.tasks.TasksScreenView
import kg.kloop.android.gigaturnip.ui.theme.GigaTurnipTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContent {
            GigaTurnipTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    val user = viewModel.user.observeAsState()
    if (user.value != null) {
        CampaignsScreenView(navController = navController)
        val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
        val scope = rememberCoroutineScope()
        Scaffold(
            topBar = {
                Toolbar("GigaTurnip", scaffoldState.drawerState, scope) {
                    FirebaseAuth.getInstance().signOut()
                    viewModel.setUser(null)
                }
            },
            scaffoldState = scaffoldState,
        )
        { innerPadding ->
            MainNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )

        }

    } else {
        LogIn { currentUser -> viewModel.setUser(currentUser) }
    }

}

@Composable
private fun LogIn(setUser: (FirebaseUser?) -> Unit) {
    val contract = FirebaseAuthUIActivityResultContract()
    val launcher = rememberLauncherForActivityResult(contract) { result ->
        if (result.resultCode == RESULT_OK) {
            setUser(FirebaseAuth.getInstance().currentUser)
        }
    }
    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            launcher.launch(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setLogo(R.drawable.ic_launcher_foreground)
                    .setTheme(R.style.Theme_GigaTurnip)
                    .build()
            )
        }) {
            Text(text = stringResource(id = R.string.login))
        }

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
            composable(TasksScreen.InProgress.route) {
                TasksScreenView(
                    navigateToDetails = navigateToDetails(navController),
                    onFabClick = { navController.navigate(TasksScreen.Creatable.route) }
                )
            }
        }
        composable(
            route = TasksScreen.Details.route.plus("/{id}/{stage_id}"),
            arguments = listOf(navArgument("id") { type = NavType.StringType },
                navArgument("stage_id") { type = NavType.StringType })
        ) { TaskDetails(navController) }

        composable(TasksScreen.Creatable.route) {
            TasksCreatable(navController = navController)
        }
    }

}

private fun navigateToDetails(navController: NavHostController) = { task: Task ->
    navController.navigate(
        TasksScreen.Details.route
            .plus("/${task.id}/${task.stage.id}")
    )
}
