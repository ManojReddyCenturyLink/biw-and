package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.contact.ContactDetails
import retrofit2.http.GET
import retrofit2.http.Path

interface BillingApIDetails {

    @GET("sobjects/Contact/{contact-id}")
    suspend fun getContactDetails(@Path("contact-id") id: String): BillingApIDetails
}