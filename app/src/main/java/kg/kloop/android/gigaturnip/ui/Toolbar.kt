package kg.kloop.android.gigaturnip.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kg.kloop.android.gigaturnip.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun Toolbar(
    title: String,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onNotificationsClick: () -> Unit,
    onLogOutClick: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(text = title, style = MaterialTheme.typography.h5)
        },
        navigationIcon = {
            IconButton(onClick = { scope.launch { if (drawerState.isClosed) drawerState.open() } }) {
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
