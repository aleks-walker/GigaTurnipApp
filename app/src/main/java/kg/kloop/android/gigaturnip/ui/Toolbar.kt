package kg.kloop.android.gigaturnip.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kg.kloop.android.gigaturnip.R

@Composable
fun Toolbar(
    title: String,
    onNotificationsClick: () -> Unit,
    onLogOutClick: () -> Unit,
    openDrawer: () -> Unit,
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(text = title, style = MaterialTheme.typography.h5)
        },
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(Icons.Default.Menu, "menu")
            }
        }, actions = {
            IconButton(onClick = { onNotificationsClick() }) {
                Icon(Icons.Default.Notifications, "notifications")
            }
            Menu(
                modifier = Modifier.wrapContentSize(),
                onLogOutClick = onLogOutClick
            )
        })

}

@Composable
fun DetailsToolbar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    onBack: () -> Unit,
    elevation: Dp = 4.dp
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        elevation = elevation,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.h5,
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "arrow back")
            }
        }, actions = actions
    )
}

@Composable
private fun Menu(modifier: Modifier, onLogOutClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(onClick = onLogOutClick) {
                Text(
                    stringResource(id = R.string.logout),
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}

fun LazyListState.isScrolled(): Boolean =
    this.firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0