package kg.kloop.android.gigaturnip.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun TaskDetails(navController: NavHostController) {
    val args = navController.currentBackStackEntry?.arguments
    val id = args?.getString("id", "No id")!!
    val title = args.getString("title", "No title")!!
    val description = args.getString("description", "No description")!!
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = id, style = MaterialTheme.typography.caption)
        Text(modifier = Modifier.padding(16.dp), text = title, style = MaterialTheme.typography.h5)
        Text(text = description, style = MaterialTheme.typography.subtitle2)

    }
}
