package kg.kloop.android.gigaturnip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(): ViewModel() {

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    private val _campaignId = MutableLiveData<String>()
    val campaignId: LiveData<String> = _campaignId

    init {
        getUser()
    }

    private fun getUser() {
        _user.value = FirebaseAuth.getInstance().currentUser
    }

    fun setUser(value: FirebaseUser?) = _user.postValue(value)

    fun setCampaignId(value: String) {
        _campaignId.value = value
    }
}