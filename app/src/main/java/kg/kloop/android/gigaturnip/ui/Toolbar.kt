package kg.kloop.android.gigaturnip.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kg.kloop.android.gigaturnip.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun Toolbar(title: String, drawerState: DrawerState, scope: CoroutineScope) {
    TopAppBar(
        title = {
            Text(text = title, style = MaterialTheme.typography.h5)
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
