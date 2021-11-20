package kg.kloop.android.gigaturnip.ui.auth

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import java.util.*


fun getTokenSynchronously(onError: () -> Unit = {}): String? {
    val user = FirebaseAuth.getInstance().currentUser
    user?.let {
        try {
            val result = Tasks.await(user.getIdToken(true))
            return Objects.requireNonNull(result).token
        } catch (e: Exception) {
            onError()
            e.printStackTrace()
        }
    }
    return null
}