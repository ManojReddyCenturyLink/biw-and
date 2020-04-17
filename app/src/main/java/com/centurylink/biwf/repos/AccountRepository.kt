package com.centurylink.biwf.repos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.model.Account
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
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
}