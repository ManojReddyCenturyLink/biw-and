package com.centurylink.biwf.screens.home

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.HomeCoordinatorDestinations
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldEqual
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class HomeViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: HomeViewModel

    @MockK
    private lateinit var userRepository: UserRepository
    @MockK
    private lateinit var mockPreferences: Preferences

    @Before
    fun setup() {
        every { mockPreferences.getHasSeenDialog() } returns true
        viewModel = HomeViewModel(mockk(), userRepository, mockPreferences, mockk())
    }

    @Test
    fun onSupportClicked_navigateToSupportScreen() = runBlockingTest {
        launch {
            viewModel.onSupportClicked()
        }

        Assert.assertEquals(
            "Support Screen wasn't Launched",
            HomeCoordinatorDestinations.SUPPORT,
            viewModel.myState.first()
        )
    }

    @Test
    fun onStart_displayNonOnboardedTabBar() {
        // Will rename this test once we have this feature in place
        viewModel.activeUserTabBarVisibility.value shouldEqual false
    }
}
