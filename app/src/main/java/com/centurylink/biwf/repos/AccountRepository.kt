package com.centurylink.biwf.repos

interface AccountRepository {

    fun login(email: String, password: String, rememberMeFlag: Boolean): Any
}

class AccountRepositoryImpl() : AccountRepository {

    override fun login(email: String, password: String, rememberMeFlag: Boolean): Boolean {
        return true
    }
}