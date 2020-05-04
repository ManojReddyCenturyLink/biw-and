package com.centurylink.biwf.base

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.centurylink.biwf.utility.LiveDataObserver
import dagger.android.support.AndroidSupportInjection

/**
 * Base class for Fragments all the Fragments class must derive this Base Class.
 */
abstract class BaseFragment : Fragment(), LiveDataObserver {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

}