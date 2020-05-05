package com.centurylink.biwf.repos

import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.model.contact.UpdatedCallsandTextMarketing
import com.centurylink.biwf.model.contact.UpdatedMarketingEmails
import com.centurylink.biwf.service.network.ContactApiService
import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val preferences: Preferences,
    private val contactApiService: ContactApiService
) {

    fun storeContactId(contactId: String) {
        preferences.saveContactId(contactId)
    }

    private fun getContactId(): String? {
        return preferences.getValueByID(Preferences.CONTACT_ID)
    }


    fun getContactDetails(): Flow<ContactDetails> = flow {
        val contactInfo = contactApiService.getContactDetails(getContactId()!!)
        emit(contactInfo)
    }

    fun setMarketingEmails(emailValue: Boolean): Flow<Unit> = flow {
        val updatedMarketingEmails = UpdatedMarketingEmails(emailValue)
        emit(
            contactApiService.submitMarketingEmail(
                getContactId()!!,
                updatedMarketingEmails
            )
        )
    }

    fun setMarketingCallsAndText(emailValue: Boolean): Flow<Unit> = flow {
        val updatedCallsandTextMarketing = UpdatedCallsandTextMarketing(emailValue)
        emit(
            contactApiService.submitMarketingCalls(
                getContactId()!!,
                updatedCallsandTextMarketing
            )
        )
    }
}