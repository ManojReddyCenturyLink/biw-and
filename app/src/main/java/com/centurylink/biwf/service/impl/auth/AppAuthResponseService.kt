package com.centurylink.biwf.service.impl.auth

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.coroutineScope
import com.centurylink.biwf.screens.login.LoginActivity
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import dagger.android.AndroidInjection
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppAuthResponseService : LifecycleService(), AuthServiceHost {
    override val hostContext: Context = this

    @Inject
    internal lateinit var authServiceFactory: AuthServiceFactory<*>

    private val authService: AuthService<*> by lazy { authServiceFactory.create(this) }

    override fun onCreate() {
        super.onCreate()

        AndroidInjection.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent != null) {
            lifecycle.coroutineScope.launch {
                val response = authService.handleResponse(intent)
                LoginActivity.reportLoginResult(this@AppAuthResponseService, response)
            }
        }

        return START_NOT_STICKY
    }
}
