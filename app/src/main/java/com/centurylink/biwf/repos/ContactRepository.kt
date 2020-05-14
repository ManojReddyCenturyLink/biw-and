package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.model.contact.UpdatedCallsandTextMarketing
import com.centurylink.biwf.model.contact.UpdatedMarketingEmails
import com.centurylink.biwf.service.network.ContactApiService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val preferences: Preferences,
    private val contactApiService: ContactApiService
) {

    private fun getContactId(): String? {
        return preferences.getValueByID(Preferences.CONTACT_ID)
    }

    suspend fun getContactDetails(): Either<String, ContactDetails> {
        val result: FiberServiceResult<ContactDetails> =
            contactApiService.getContactDetails(getContactId()!!)
        result.fold(
            ifLeft = {},
            ifRight = {
                it
            }
        )
        return result.mapLeft { it.message?.message.toString() }
    }

    suspend fun setMarketingEmails(emailValue: Boolean) {
        val updatedMarketingEmails = UpdatedMarketingEmails(emailValue)
        contactApiService.submitMarketingEmail(
            getContactId()!!,
            updatedMarketingEmails
        )
    }

    suspend fun setMarketingCallsAndText(emailValue: Boolean) {
        val updatedCallsandTextMarketing = UpdatedCallsandTextMarketing(emailValue)
        contactApiService.submitMarketingCalls(
            getContactId()!!,
            updatedCallsandTextMarketing
        )
    }
}