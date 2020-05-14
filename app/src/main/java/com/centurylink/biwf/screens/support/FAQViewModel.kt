package com.centurylink.biwf.screens.support


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.FAQCoordinatorDestinations
import com.centurylink.biwf.model.support.FAQ
import com.centurylink.biwf.model.support.QuestionFAQ
import com.centurylink.biwf.model.support.Videofaq
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.repos.FAQRepository
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.EventLiveData
import javax.inject.Inject

class FAQViewModel @Inject constructor(
    faqRepository: FAQRepository
) : BaseViewModel() {

    val errorEvents: EventLiveData<String> = MutableLiveData()
    val faqVideoData: MutableLiveData<List<Videofaq>> = MutableLiveData()
    val faqQuestionsData: MutableLiveData<HashMap<String, String>> = MutableLiveData()
    private var questionMap: HashMap<String, String> = HashMap()
    private var faqListDetails: LiveData<Resource<FAQ>> = faqRepository.getFAQDetails()
    val myState = EventFlow<FAQCoordinatorDestinations>()

    fun getFAQDetails() = faqListDetails

    fun sortQuestionsAndVideos(videolist: List<Videofaq>, questionList: List<QuestionFAQ>) {
        faqVideoData.value = videolist
        questionMap = questionList.associateTo(HashMap(), { it.name to it.description })
        faqQuestionsData.value = questionMap
    }

    fun navigateToScheduleCallback() {
        myState.latestValue = FAQCoordinatorDestinations.SCHEDULE_CALLBACK
    }
}
