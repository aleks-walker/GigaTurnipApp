package kg.kloop.android.gigaturnip.ui.campaigns

import kg.kloop.android.gigaturnip.repository.FakeGigaTurnipRepositoryImpl
import org.junit.Before
import org.junit.Test

class CampaignsViewModelTest {

    private lateinit var viewModel: CampaignsViewModel

    @Before
    fun setup() {
        viewModel = CampaignsViewModel(FakeGigaTurnipRepositoryImpl())
    }

    @Test
    fun `stuff`() {
        viewModel.refreshCampaigns()
    }
}