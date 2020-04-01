package com.centurylink.biwf.base

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * Base class for Fragments all the Fragments class must derive this Base Class.
 */
abstract class BaseFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }
}