package kg.kloop.android.gigaturnip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(): ViewModel() {

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token

    private val _campaignId = MutableLiveData<String>()
    val campaignId: LiveData<String> = _campaignId

    init {
        _user.value = FirebaseAuth.getInstance().currentUser
        _user.value?.getIdToken(false)?.addOnSuccessListener {
            _token.value = it.token!!
            Timber.d("TOKEN: ${_token.value?.subSequence(0..20)}")
        }
    }


    fun setUser(value: FirebaseUser?) = _user.postValue(value)

    fun getUserToken() = token

    fun setCampaignId(value: String) {
        _campaignId.value = value
    }

//    fun getUserToken() = liveData {
//        emit(user.value?.getIdToken(false)?.result?.token)
////        emit(user.value?.getIdToken(false)?.addOnSuccessListener {
////            _token.value = it.token!!
////        })
//    }
}