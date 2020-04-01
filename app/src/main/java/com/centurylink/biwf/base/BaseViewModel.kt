package com.centurylink.biwf.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centurylink.biwf.utility.EventLiveData
import com.centurylink.biwf.utility.LiveEvent

abstract class BaseViewModel : ViewModel() {
    // Works exactly the same way as MutableLiveData.value
    // This allows all the subclasses' live data to be declared as LiveData<T> type instead of MutableLiveData<T> so
    // that their values can't be changed externally but still can internally
    // see https://github.com/IntrepidPursuits/skotlinton-android/pull/33#discussion_r275908063
    protected var <T : Any?> LiveData<T>.latestValue: T?
        get() = this.value
        set(value) {
            (this as MutableLiveData<T>).value = value
        }

    protected fun <T : Any> EventLiveData<T>.emit(event: T) {
        this.latestValue = LiveEvent(event)
    }

    protected val LiveData<String>.valueOrEmpty: String
        get() = this.value ?: ""
}
