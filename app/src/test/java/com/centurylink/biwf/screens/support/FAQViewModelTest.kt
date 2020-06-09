package com.centurylink.biwf.screens.support

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.repos.CaseRepository
import com.centurylink.biwf.repos.FAQRepository
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.mockito.MockitoAnnotations

class FAQViewModelTest : ViewModelBaseTest() {

    @MockK
    lateinit var faqRepository: FAQRepository

    @MockK
    lateinit var caseRepository: CaseRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: FAQViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        viewModel = FAQViewModel(faqRepository,caseRepository)
        //TODO Need to Revisit after Livedata cleanup
    }
}