package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.model.contact.UpdatedCallsandTextMarketing
import com.centurylink.biwf.model.contact.UpdatedMarketingEmails
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ContactApiService {

    @GET(EnvironmentPath.API_CONTACT_INFORMATION_PATH)
    suspend fun getContactDetails(@Path(EnvironmentPath.CONTACT_ID) id: String): FiberServiceResult<ContactDetails>

    @PATCH(EnvironmentPath.API_CONTACT_INFORMATION_PATH)
    suspend fun submitMarketingEmail(
        @Path(EnvironmentPath.CONTACT_ID) id: String,
        @Body updateMarketing: UpdatedMarketingEmails
    ): FiberServiceResult<Unit>

    @PATCH(EnvironmentPath.API_CONTACT_INFORMATION_PATH)
    suspend fun submitMarketingCalls(
        @Path(EnvironmentPath.CONTACT_ID) id: String,
        @Body updatedCall: UpdatedCallsandTextMarketing
    ): FiberServiceResult<Unit>
}