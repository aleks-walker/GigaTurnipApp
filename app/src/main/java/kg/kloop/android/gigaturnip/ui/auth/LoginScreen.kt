package kg.kloop.android.gigaturnip.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kg.kloop.android.gigaturnip.R

@Composable
fun LoginScreen(setUser: (FirebaseUser?) -> Unit) {
    val contract = FirebaseAuthUIActivityResultContract()
    val launcher = rememberLauncherForActivityResult(contract) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            setUser(FirebaseAuth.getInstance().currentUser)
        }
    }
    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            launcher.launch(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setLogo(R.drawable.ic_launcher_foreground)
                    .setTheme(R.style.Theme_GigaTurnip)
                    .build()
            )
        }) {
            Text(text = stringResource(id = R.string.login))
        }

    }
}
