package com.centurylink.biwf.repos

import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val preferences: Preferences
) {

    fun storeContactId(contactId: String) {
        preferences.saveContactId(contactId)
    }

    fun getContactId(): String? {
        return preferences.getValueByID(Preferences.CONTACT_ID)
    }


}