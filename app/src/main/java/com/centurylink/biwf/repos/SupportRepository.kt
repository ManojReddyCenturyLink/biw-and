package com.centurylink.biwf.repos

import androidx.lifecycle.LiveData
import com.centurylink.biwf.model.TroubleshootingModel
import com.centurylink.biwf.model.support.FaqModel
import com.centurylink.biwf.network.NetworkResource
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.service.network.ApiServices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportRepository @Inject constructor(
    private val apiServices: ApiServices
) {
    fun getFAQDetails(): LiveData<Resource<FaqModel>> {
        return object : NetworkResource<FaqModel>() {
            override fun createCall(): LiveData<Resource<FaqModel>> {
                return apiServices.getFaqDetails()
            }
        }.asLiveData()
    }

    fun getTroubleshootingDetails(): LiveData<Resource<TroubleshootingModel>> {
        return object : NetworkResource<TroubleshootingModel>() {
            override fun createCall(): LiveData<Resource<TroubleshootingModel>> {
                return apiServices.getTroubleshootingDetails()
            }
        }.asLiveData()
    }
}