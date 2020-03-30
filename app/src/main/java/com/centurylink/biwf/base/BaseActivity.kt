package com.centurylink.biwf.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.centurylink.biwf.utility.LiveDataObserver
import dagger.android.AndroidInjection

/**
 * Base class for holding common functionalities that will be used across screens.
 */
abstract class BaseActivity : AppCompatActivity(), LiveDataObserver {

    override val liveDataLifecycleOwner: LifecycleOwner get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
    }
}