package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.home.account.PersonalInfoActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PersonalInfoActivityInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributePersonalInfoActivityInjector(): PersonalInfoActivity
}
