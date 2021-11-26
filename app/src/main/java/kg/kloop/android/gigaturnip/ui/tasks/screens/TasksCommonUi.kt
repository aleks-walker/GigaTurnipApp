package kg.kloop.android.gigaturnip.ui.tasks.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kg.kloop.android.gigaturnip.R
import kg.kloop.android.gigaturnip.domain.Task
import kg.kloop.android.gigaturnip.ui.theme.DarkRed
import kg.kloop.android.gigaturnip.util.toTimeAgoFormat

@Composable
fun TasksList(
    onDetailsClick: (Task) -> Unit,
    tasks: List<Task>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = onRefresh
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            items(tasks) { task ->
                TaskCard(task, onClick = { onDetailsClick(task) })
            }
        }
    }
}

@Composable
private fun TaskCard(task: Task, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1F)) {
                Text(
                    text = task.stage.name,
                    style = MaterialTheme.typography.h5,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SelectionContainer() {
                    Text(
                        text = "id: ${task.id}",
                        style = MaterialTheme.typography.subtitle2
                    )
                }
                Text(
                    text = task.createdAt.toTimeAgoFormat(),
                    style = MaterialTheme.typography.subtitle2
                )
                Text(
                    text = task.stage.description,
                    style = MaterialTheme.typography.body1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (task.isReopened) {
                Text(
                    text = stringResource(id = R.string.returned).lowercase(),
                    color = DarkRed
                )
            }
        }
    }
}
