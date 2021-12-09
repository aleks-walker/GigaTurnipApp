package kg.kloop.android.gigaturnip.ui.tasks

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import kg.kloop.android.gigaturnip.domain.Notification
import kg.kloop.android.gigaturnip.repository.FakeGigaTurnipRepositoryImpl
import kg.kloop.android.gigaturnip.util.Constants.CAMPAIGN_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class TasksViewModelTest {

    lateinit var viewModel: TasksViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(TestCoroutineDispatcher())

        val repository = FakeGigaTurnipRepositoryImpl().apply {
            notificationsList = mutableListOf(
                Notification(
                    1,
                    "asdf",
                    "asdf",
                    1,
                    "",
                    "",
                    1,
                    1
                )
            )
        }
        viewModel = TasksViewModel(
            repository,
            SavedStateHandle().apply { set(CAMPAIGN_ID, "1") }
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `check if notification count equal or greater than zero, returns true`() {
        runBlockingTest {
            val count = viewModel.getNotificationsCount("")
            assertThat(count).isEqualTo(1)
        }
    }
}