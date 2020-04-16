package com.centurylink.biwf.screens.home

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.HomeCoordinatorDestinations
import com.centurylink.biwf.screens.home.HomeViewModel
import org.amshove.kluent.shouldEqual
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class HomeViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        viewModel = HomeViewModel()
    }

    @Test
    fun onSupportClicked_navigateToSupportScreen() {
        viewModel.onSupportClicked()
        Assert.assertEquals(
            "Support Screen wasn't Launched",
            HomeCoordinatorDestinations.SUPPORT,
            viewModel.myState.value
        )
    }

    @Test
    fun onStart_displayNonOnboardedTabBar() {
        // Will rename this test once we have this feature in place
        viewModel.activeUserTabBarVisibility.value shouldEqual false
    }

    @Test
    fun onSupportLongClick_displayOnboardedTabBar() {
        // Will rename this test once we have this feature in place
        onStart_displayNonOnboardedTabBar()
        viewModel.onSupportLongClick_toggleToolbars()
        viewModel.activeUserTabBarVisibility.value shouldEqual true
    }
}