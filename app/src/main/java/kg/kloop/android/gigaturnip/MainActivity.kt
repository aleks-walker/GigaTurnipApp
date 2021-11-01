package kg.kloop.android.gigaturnip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DrawerValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kg.kloop.android.gigaturnip.ui.Toolbar
import kg.kloop.android.gigaturnip.ui.auth.LoginScreen
import kg.kloop.android.gigaturnip.ui.notifications.NotificationsScreen
import kg.kloop.android.gigaturnip.ui.theme.GigaTurnipTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel by viewModels<MainActivityViewModel>()
            GigaTurnipTheme {
                MainScreen(viewModel, navController)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainActivityViewModel, navController: NavHostController) {
    val user = viewModel.user.observeAsState()
    if (user.value == null) {
        LoginScreen { currentUser -> viewModel.setUser(currentUser) }
    } else {
        val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                Toolbar(
                    stringResource(R.string.app_name),
                    scaffoldState.drawerState,
                    scope,
                    onLogOutClick = { logOut(viewModel) },
                    onNotificationsClick = {
                        navController.navigate(NotificationsScreen.NotificationsList.route) {
                            launchSingleTop = true
                        }
                    }
                )
            },
            scaffoldState = scaffoldState,
            drawerContent = { AppDrawer(user) }
        )
        { innerPadding ->
            MainNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel
            )
        }
    }

}

private fun logOut(viewModel: MainActivityViewModel) {
    FirebaseAuth.getInstance().signOut()
    viewModel.setUser(null)
}




