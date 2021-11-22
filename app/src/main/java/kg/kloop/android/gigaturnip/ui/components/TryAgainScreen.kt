package kg.kloop.android.gigaturnip.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kg.kloop.android.gigaturnip.R

@Composable
fun TryAgainScreen(
    text: String = stringResource(id = R.string.error_occured),
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(modifier = Modifier.padding(bottom = 8.dp), text = text)
        Button(
            onClick = { onRefresh() },
        ) {
            Text(
                text = stringResource(id = R.string.try_again),
                textAlign = TextAlign.Center
            )
        }
    }
}

