package com.centurylink.biwf.repos

import android.util.Log
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

    suspend fun setMarketingEmails(emailValue: Boolean)  {
        val updatedMarketingEmails = UpdatedMarketingEmails(emailValue)
        contactApiService.submitMarketingEmail(
                getContactId()!!,
                updatedMarketingEmails)
    }

    suspend fun setMarketingCallsAndText(emailValue: Boolean){
        val updatedCallsandTextMarketing = UpdatedCallsandTextMarketing(emailValue)
        contactApiService.submitMarketingCalls(
            getContactId()!!,
            updatedCallsandTextMarketing)
    }
}