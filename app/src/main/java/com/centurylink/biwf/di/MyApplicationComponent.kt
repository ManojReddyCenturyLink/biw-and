package com.centurylink.biwf.di

import com.centurylink.biwf.screens.login.LoginActivity
import dagger.Component

@Component
interface MyApplicationComponent {
    // Must add
    fun inject(activity: LoginActivity)
}