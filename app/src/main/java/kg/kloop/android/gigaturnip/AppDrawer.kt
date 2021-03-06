package kg.kloop.android.gigaturnip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

@Composable
fun AppDrawer(user: FirebaseUser?) {
    SelectionContainer {
        Column() {
            Text(
                modifier = Modifier.padding(8.dp),
                text = user?.displayName.toString(),
                style = MaterialTheme.typography.h5
            )
            Text(
                modifier = Modifier.padding(8.dp),
                text = user?.email.toString(),
                style = MaterialTheme.typography.h5
            )
        }

    }
}
