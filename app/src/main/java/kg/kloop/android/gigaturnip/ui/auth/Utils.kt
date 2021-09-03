package kg.kloop.android.gigaturnip.ui.auth

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import java.util.*


fun getTokenSynchronously(): String? {
    val user = FirebaseAuth.getInstance().currentUser
    if (user != null) {
        val result = Tasks.await(user.getIdToken(true));
        return Objects.requireNonNull(result).token!!
    }
    return null
}