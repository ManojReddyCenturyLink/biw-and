package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.model.contact.UpdatedCallsandTextMarketing
import com.centurylink.biwf.model.contact.UpdatedMarketingEmails
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ContactApiService {

    @GET("sobjects/Contact/{contact-id}")
    suspend fun getContactDetails(@Path("contact-id") id: String): FiberServiceResult<ContactDetails>

    @PATCH("sobjects/Contact/{contact-id}")
    suspend fun submitMarketingEmail(
        @Path("contact-id") id: String,
        @Body updateMarketing: UpdatedMarketingEmails
    ): FiberServiceResult<Unit>

    @PATCH("sobjects/Contact/{contact-id}")
    suspend fun submitMarketingCalls(
        @Path("contact-id") id: String,
        @Body updatedCall: UpdatedCallsandTextMarketing
    ): FiberServiceResult<Unit>
}