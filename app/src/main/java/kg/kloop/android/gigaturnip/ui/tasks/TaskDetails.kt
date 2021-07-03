package kg.kloop.android.gigaturnip.ui.tasks

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun TaskDetails(
    navController: NavHostController,
    viewModel: TaskDetailsViewModel = hiltViewModel()
) {

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

        val taskStage by viewModel.getTaskStage(id.toInt()).observeAsState()

        Text(text = id, style = MaterialTheme.typography.caption)
        Text(
            modifier = Modifier.padding(16.dp),
            text = taskStage?.name.toString(),
            style = MaterialTheme.typography.h5
        )
        Text(text = taskStage?.description.toString(), style = MaterialTheme.typography.subtitle2)

        val result = remember { mutableStateOf<Uri?>(null) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            result.value = it
        }
        Button(modifier = Modifier
            .padding(16.dp),
            onClick = { launcher.launch("video/*") }) {
            Text(text = "Compress")
        }
        result.value?.let { uri ->
            val fileDestination: String by viewModel.getCompressedFilePath(uri, "download/")
                .observeAsState("compression in progress")
            SelectionContainer() {
                Text(text = uri.toString())
            }
            Text(text = fileDestination)
        }

    }
}
