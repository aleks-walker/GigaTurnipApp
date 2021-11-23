package kg.kloop.android.gigaturnip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
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
                ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                    MainScreen(viewModel, navController)
                }
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
        MainNavGraph(
            navController = navController,
            viewModel = viewModel,
        )
    }
}



