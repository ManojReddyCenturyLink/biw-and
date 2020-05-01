package com.centurylink.biwf.repos

import com.centurylink.biwf.service.network.ApiServices
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancelSubscriptionDetailsRepository @Inject constructor(
    private val apiServices: ApiServices
) {
    fun submitCancellation(
        cancellation: Date, cancellationReason: String?,
        rating: Float?, comments: String?
    ): Boolean {
        return true
    }
}