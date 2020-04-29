package com.centurylink.biwf.screens.subscription

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.repos.CancelSubscriptionDetailsRepository
import com.centurylink.biwf.utility.EventLiveData
import java.util.*
import javax.inject.Inject

class CancelSubscriptionDetailsViewModel @Inject constructor(
    private val cancelSubscriptionDetailsRepository: CancelSubscriptionDetailsRepository
) : BaseViewModel() {

    private var cancellationDate: Date? = null
    private var cancellationReason: String = ""
    private var cancellationReasonExplanation: String = ""
    private var ratingValue: Float? = 0F
    private var cancellationComments: String = ""

    val cancelSubscriptionDateEvent: EventLiveData<Date> = MutableLiveData()

    val performSubmitEvent: EventLiveData<Date> = MutableLiveData()

    val changeDateEvent: EventLiveData<Unit> = MutableLiveData()

    val errorEvents: EventLiveData<String> = MutableLiveData()

    val displayReasonSelectionEvent: EventLiveData<Boolean> = MutableLiveData()

    fun onRatingChanged(rating: Float) {
        ratingValue = rating
    }

    fun onCancellationReason(cancellationReasonData: String) {
        cancellationReason = cancellationReasonData
        if (cancellationReasonData.equals("Other", true)) {
            displayReasonSelectionEvent.emit(true)
        } else {
            displayReasonSelectionEvent.emit(false)
        }
    }

    fun onCancellationCommentsChanged(cancellationCommentsData: String) {
        cancellationComments = cancellationCommentsData
    }

    fun onCancellationDateSelected(cancellationDateInfo: Date) {
        cancellationDate = cancellationDateInfo
        cancelSubscriptionDateEvent.emit(cancellationDate!!)
    }

    fun onOtherCancellationChanged(commentsOnOthers: String) {
        cancellationReasonExplanation = commentsOnOthers
    }

    fun onDateChange() {
        changeDateEvent.emit(Unit)
    }

    fun onSubmitCancellation() {
        if (cancellationDate == null) {
            errorEvents.emit("Error")
        } else {
            performSubmitEvent.emit(cancellationDate!!)
        }
    }

    fun performCancellationCall() {
        cancelSubscriptionDetailsRepository.submitCancellation(
            cancellationDate!!,
            cancellationReason!!, ratingValue!!, cancellationComments!!
        )
    }
}