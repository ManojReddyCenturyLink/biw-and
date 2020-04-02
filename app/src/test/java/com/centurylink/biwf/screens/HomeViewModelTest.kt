package com.centurylink.biwf.screens

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.HomeCoordinatorDestinations
import com.centurylink.biwf.screens.home.HomeViewModel
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
        Assert.assertEquals("Support Screen wasn't Launched", HomeCoordinatorDestinations.SUPPORT, viewModel.myState.value)
    }
}