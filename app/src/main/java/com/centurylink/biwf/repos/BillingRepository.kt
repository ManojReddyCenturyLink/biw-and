package com.centurylink.biwf.repos

import com.centurylink.biwf.service.network.AccountApiService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    private val preferences: Preferences,
    private val accountApiService: AccountApiService
)