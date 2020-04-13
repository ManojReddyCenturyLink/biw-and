package com.centurylink.biwf.screens.support

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.support.FAQ
import com.centurylink.biwf.model.support.QuestionFAQ
import com.centurylink.biwf.model.support.Videofaq
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.repos.FAQRepository
import com.centurylink.biwf.utility.EventLiveData
import java.util.stream.Collectors
import javax.inject.Inject

class FAQViewModel @Inject constructor(
    private val faqRepository: FAQRepository
) : BaseViewModel(){

    val errorEvents: EventLiveData<String> = MutableLiveData()
    private val faqVideoData:MutableLiveData<List<Videofaq>> = MutableLiveData()
    private val faqQuestionsData:MutableLiveData<HashMap<String,String>> = MutableLiveData()
    private var questionMap: HashMap<String,String> = HashMap<String,String>()
    private var faqListDetails: LiveData<Resource<FAQ>> =
        faqRepository.getFAQDetails()

    fun getFAQDetails() = faqListDetails

    fun sortQuestionsAndVideos(videolist: List<Videofaq>,questionList:List<QuestionFAQ>) {
        faqVideoData.value=videolist
        questionMap = questionList.associateTo(HashMap(),{it.name to it.description})
        Log.i("Pravin","Question Map "+questionMap.size)
        faqQuestionsData.value=questionMap
    }

    fun getVideoFAQLiveData(): MutableLiveData<List<Videofaq>> {
        return faqVideoData
    }

    fun getQuestionFAQLiveData(): MutableLiveData<HashMap<String,String>> {
        return faqQuestionsData
    }
}