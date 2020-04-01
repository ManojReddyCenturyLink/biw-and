package com.centurylink.biwf.utility

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.di.DaggerApplicationComponent

class InitUtility {
    companion object {
        fun initDependencyInjection(app: BIWFApp) {
            DaggerApplicationComponent
                .builder()
                .applicationContext(app)
                .build().inject(app)
        }
    }
}