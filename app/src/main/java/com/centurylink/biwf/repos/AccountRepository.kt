package com.centurylink.biwf.repos

import android.util.Log

interface AccountRepository{

    fun login(email: String, password: String) : Any
}

class AccountRepositoryImpl():AccountRepository {
    override fun login(email: String, password: String): Any {
         Log.d("Findo","user email is : $email and password is : $password")
        return true
    }
}