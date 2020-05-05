package com.centurylink.biwf.screens.home

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.HomeCoordinatorDestinations
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.shouldEqual
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class HomeViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        viewModel = HomeViewModel(mock())
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
}