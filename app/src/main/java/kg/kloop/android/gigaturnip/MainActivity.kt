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
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kg.kloop.android.gigaturnip.ui.auth.LoginScreen
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
//        val scope = rememberCoroutineScope()

        Scaffold(
            scaffoldState = scaffoldState,
            drawerContent = { AppDrawer(user) }
        )
        { innerPadding ->
            MainNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel,
                scaffoldState = scaffoldState
            )
        }
    }

}



