package kg.kloop.android.gigaturnip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.kloop.android.gigaturnip.domain.Campaign
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(): ViewModel() {

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    private val _campaign = MutableLiveData<Campaign>()
    val campaign: LiveData<Campaign> = _campaign

    init {
        getUser()
    }

    private fun getUser() {
        _user.value = FirebaseAuth.getInstance().currentUser
    }

    fun setUser(value: FirebaseUser?) = _user.postValue(value)

    fun setCampaign(value: Campaign) {
        _campaign.value = value
    }

    fun logOut() {
        FirebaseAuth.getInstance().signOut()
        _user.postValue(null)
    }
}