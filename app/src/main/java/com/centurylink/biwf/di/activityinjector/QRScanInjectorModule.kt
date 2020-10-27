package com.centurylink.biwf.di.activityinjector

import com.centurylink.biwf.screens.qrcode.QrScanActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class QRScanInjectorModule {
    @ContributesAndroidInjector
    abstract fun contributeSupportActivityInjector(): QrScanActivity
}
