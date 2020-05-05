package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.model.contact.UpdatedCallsandTextMarketing
import com.centurylink.biwf.model.contact.UpdatedMarketingEmails
import retrofit2.http.*

interface ContactApiService {

    @GET("sobjects/Contact/{contact-id}")
    suspend fun getContactDetails(@Query("q") soqlQuery: String): ContactDetails

    @PATCH("sobjects/Contact/{contact-id}")
    suspend fun submitMarketingEmail(@Path("contact-id") id: String, @Body updatedPassword: UpdatedMarketingEmails)

    @PATCH("sobjects/Contact/{contact-id}")
    suspend fun submitMarketingCalls(@Path("contact-id") id: String, @Body updatedPassword: UpdatedCallsandTextMarketing)
}