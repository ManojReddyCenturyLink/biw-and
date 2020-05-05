package com.centurylink.biwf.repos

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.model.Account
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.UpdatedServiceCallsAndTexts
import com.centurylink.biwf.service.network.AccountApiService
import com.centurylink.biwf.utility.MutableStateFlow

import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val preferences: Preferences,
    private val accountApiService: AccountApiService
) {

    fun login(email: String, password: String, rememberMeFlag: Boolean): Boolean {
        return true
    }

    fun getAccount(): LiveData<Account> {
        return MutableLiveData(
            Account(
                fullName = "Barry Allen",
                streetAddress = "2333 Candycane Lane",
                city = "Bellflower",
                state = "WA",
                zipcode = "90703",
                cellNumber = "(562) 416-1854",
                homeNumber = "(562) 865-7228",
                workNumber = "(562) 422-2144",
                emailAddress = "email@something.com",
                billingAddress = "1222 Bilington Way"
            )
        )
    }

    fun storeAccountId(accountId: String) {
        preferences.saveAccountId(accountId)
    }

    fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    fun getAccountDetails() : Flow<AccountDetails> = flow {
        val accountInfo = accountApiService.gerAccountDetails(getAccountId()!!)
        emit(accountInfo)
    }


    fun setServiceCallsAndTexts(emailValue: Boolean) :Flow<Unit> = flow {
        val updatedServiceCallsAndTexts = UpdatedServiceCallsAndTexts(emailValue)
        emit(
            accountApiService.submitServiceCallDetails(
                getAccountId()!!,
                updatedServiceCallsAndTexts
            )
        )
    }
}