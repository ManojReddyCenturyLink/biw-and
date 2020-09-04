package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.model.contact.UpdatedCallsandTextMarketing
import com.centurylink.biwf.model.contact.UpdatedMarketingEmails
import com.centurylink.biwf.service.network.ContactApiService
import com.centurylink.biwf.utility.PhoneNumber
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
        return result.mapLeft { it.message?.message.toString() }
    }

    suspend fun setMarketingEmails(emailValue: Boolean): String {
        val updatedMarketingEmails = UpdatedMarketingEmails(emailValue)
        val result: FiberServiceResult<Unit> = contactApiService.submitMarketingEmail(
            getContactId()!!,
            updatedMarketingEmails
        )
        return result.fold(
            ifLeft = { it.message?.message.toString() },
            ifRight = { "" }
        )
    }

    suspend fun setMarketingCallsAndText(emailValue: Boolean,phoneNumber:String): String {
        val updatedCallsandTextMarketing = UpdatedCallsandTextMarketing(emailValue,phoneNumber)
        val result: FiberServiceResult<Unit> = contactApiService.submitMarketingCalls(
            getContactId()!!,
            updatedCallsandTextMarketing
        )
        return result.fold(
            ifLeft = { it.message?.message.toString() },
            ifRight = { "" }
        )
    }
}