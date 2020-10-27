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

/**
 * Contact repository - This class interacts with Contact API Services. This Repository class
 * gets the data from the network . It handles all the Contact related information from the Salesforce
 * backend  and the View models can consume the Contact related information and display in the Activity
 * or Fragments.
 *
 * @property preferences Instance for storing the value in shared preferences.
 * @property contactApiService ContactApiService Instance for interacting with the Sales force Contacts API
 * @constructor Create empty Contact repository
 */
@Singleton
class ContactRepository @Inject constructor(
    private val preferences: Preferences,
    private val contactApiService: ContactApiService
) {

    /**
     *This method is used to get the Contact Id that is stored in the  Shared Preferences.
     *@return Contact id
     */
    private fun getContactId(): String? {
        return preferences.getValueByID(Preferences.CONTACT_ID)
    }

    /**
     * The Suspend function used for the purpose of fetching the Contact Details from the Salesforce
     * backend.
     * @return ContactDetails if the API is success it returns the ContactDetails instance else
     * returns the error message.
     */
    suspend fun getContactDetails(): Either<String, ContactDetails> {
        val result: FiberServiceResult<ContactDetails> =
            contactApiService.getContactDetails(getContactId()!!)
        return result.mapLeft { it.message?.message.toString() }
    }

    /**
     * The Suspend function used for the purpose of Setting the preference of Marketing emails to the
     * Sales force backend.
     *
     * @param emailValue
     * @return The email Value from
     */
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

    /**
     * The Suspend function used for the purpose of Setting the preference of Marketing Calls to the
     *
     * @param emailValue The EmailValues
     * @param phoneNumber The Phone Number of the user for Marketing calls
     * @return The Success /Error String.
     */
    suspend fun setMarketingCallsAndText(emailValue: Boolean, phoneNumber: String): String {
        val updatedCallsandTextMarketing = UpdatedCallsandTextMarketing(emailValue, phoneNumber)
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
