package com.centurylink.biwf.repos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.model.CommunicationPreferences
import com.centurylink.biwf.service.network.ApiServices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunicationRepository @Inject constructor(
    private val apiServices: ApiServices
) {

    fun getPreferences(): LiveData<CommunicationPreferences> {
        return MutableLiveData(
            CommunicationPreferences(
                biometricStatus = true,
                serviceCallsStatus = true,
                marketingEmailsStatus = true,
                marketingCallsAndTextStatus = true
            )
        )
    }

    fun changePreferences(preferences: CommunicationPreferences) {}
}