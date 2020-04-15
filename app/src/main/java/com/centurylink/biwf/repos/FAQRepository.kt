package com.centurylink.biwf.repos

import androidx.lifecycle.LiveData
import com.centurylink.biwf.model.support.FAQ
import com.centurylink.biwf.network.NetworkResource
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.network.api.ApiServices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FAQRepository @Inject constructor(
    private val apiServices: ApiServices
) {
    fun getFAQDetails():
            LiveData<Resource<FAQ>> {
        return object : NetworkResource<FAQ>() {
            override fun createCall(): LiveData<Resource<FAQ>> {
                return apiServices.getFAQDetails()
            }
        }.asLiveData()
    }
}
