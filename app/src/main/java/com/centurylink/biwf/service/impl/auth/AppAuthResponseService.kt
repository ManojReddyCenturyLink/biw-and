package com.centurylink.biwf.service.impl.auth

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.centurylink.biwf.screens.login.LoginActivity
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import dagger.android.AndroidInjection
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class AppAuthResponseService : Service(), AuthServiceHost {
    override val hostContext: Context = this

    @Inject
    internal lateinit var authServiceFactory: AuthServiceFactory<*>

    private val authService: AuthService<*> by lazy { authServiceFactory.create(this) }

    private val subscriptions = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()

        AndroidInjection.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        subscriptions.dispose()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent != null) {
            subscriptions.add(
                authService.handleResponse(intent)
                    .subscribe({ LoginActivity.reportLoginResult(this, it) }, Timber::e)
            )
        }

        return START_NOT_STICKY
    }
}
