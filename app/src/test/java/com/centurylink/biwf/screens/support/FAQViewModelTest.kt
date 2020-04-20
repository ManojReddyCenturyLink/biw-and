package com.centurylink.biwf.screens.support

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.model.support.FAQ
import com.centurylink.biwf.model.support.QuestionFAQ
import com.centurylink.biwf.model.support.Videofaq
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.network.Status
import com.centurylink.biwf.repos.FAQRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.amshove.kluent.shouldEqual
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class FAQViewModelTest : ViewModelBaseTest() {

    @MockK
    lateinit var faqRepository: FAQRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: FAQViewModel

    val result = MediatorLiveData<Resource<FAQ>>()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mockFAQ()
        mockQuestionAndAnswers()
        var faqSource: FAQ = mockFAQ()
        result.value = Resource(Status.SUCCESS,faqSource,"");
        every {(faqRepository.getFAQDetails())}.returns(result)
        viewModel = FAQViewModel(faqRepository)
    }

    @Test
    fun  onFAQSuccess(){
        var data : LiveData<Resource<FAQ>> = viewModel.getFAQDetails()
        data.value!!.status shouldEqual(Status.SUCCESS)
    }

    @Test
    fun `retrieve Video FAQ with ViewModel and Repository returns an data`(){
        with(viewModel){
            getFAQDetails()
            faqVideoData.value = mockVideoList()
        }
        Assert.assertTrue(viewModel.faqVideoData.value?.size==mockVideoList().size)
    }

    @Test
    fun `retrieve Question FAQ with ViewModel and Repository returns an data`(){
        with(viewModel){
            getFAQDetails()
            faqQuestionsData.value =mockQuestionAndAnswers()
        }
        Assert.assertTrue(viewModel.faqQuestionsData.value!!.containsKey("Query1"))
        Assert.assertTrue(viewModel.faqQuestionsData.value!!.containsValue("Q1"))
    }

    @Test
    fun `sort Question FAQ with ViewModel and Repository returns an data video and Question`(){
        with(viewModel){
            sortQuestionsAndVideos(mockVideoList(),mockQuestionList())
            faqQuestionsData.value =mockQuestionAndAnswers()
            faqVideoData.value= mockVideoList()
        }
        Assert.assertTrue(viewModel.faqVideoData.value?.size==mockVideoList().size)
        Assert.assertTrue(viewModel.faqQuestionsData.value!!.containsKey("Query2"))
        Assert.assertTrue(viewModel.faqQuestionsData.value!!.containsValue("Q2"))
    }

    fun mockVideoList(): MutableList<Videofaq> {
        return mutableListOf(
            Videofaq(1, "video1", "V1", "", "5:00", ""),
            Videofaq(2, "video2", "V2", "", "6:00", ""),
            Videofaq(3, "video3", "V3", "", "7:00", ""),
            Videofaq(4, "video4", "V4", "", "8:00", "")
        )
    }

    fun mockQuestionList(): MutableList<QuestionFAQ> {
        return mutableListOf(
            QuestionFAQ(1, "Query1", "Q1"),
            QuestionFAQ(1, "Query2", "Q2"),
            QuestionFAQ(1, "Query3", "Q3")
        )
    }

    fun mockQuestionAndAnswers():HashMap<String,String>{
       return mockQuestionList().associateTo(HashMap(), { it.name to it.description })
    }

    fun mockFAQ(): FAQ {
        return FAQ(mockVideoList(), mockQuestionList())
    }
}