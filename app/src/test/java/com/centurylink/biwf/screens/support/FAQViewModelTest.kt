package com.centurylink.biwf.screens.support

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.model.support.FAQ
import com.centurylink.biwf.model.support.QuestionFAQ
import com.centurylink.biwf.model.support.Videofaq
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
        mockFAQ()
        mockQuestionAndAnswers()
        var faqSource: FAQ = mockFAQ()
        viewModel = FAQViewModel(faqRepository,caseRepository)
        //TODO Need to Revisit after Livedata cleanup
    }



    private fun mockVideoList(): MutableList<Videofaq> {
        return mutableListOf(
            Videofaq(1, "video1", "V1", "", "5:00", ""),
            Videofaq(2, "video2", "V2", "", "6:00", ""),
            Videofaq(3, "video3", "V3", "", "7:00", ""),
            Videofaq(4, "video4", "V4", "", "8:00", "")
        )
    }

    private fun mockQuestionList(): MutableList<QuestionFAQ> {
        return mutableListOf(
            QuestionFAQ(1, "Query1", "Q1"),
            QuestionFAQ(1, "Query2", "Q2"),
            QuestionFAQ(1, "Query3", "Q3")
        )
    }

    private fun mockQuestionAndAnswers():HashMap<String,String>{
       return mockQuestionList().associateTo(HashMap(), { it.name to it.description })
    }

    private fun mockFAQ(): FAQ {
        return FAQ(mockVideoList(), mockQuestionList())
    }
}