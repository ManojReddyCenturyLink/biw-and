package com.centurylink.biwf.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.EventLiveData
import com.centurylink.biwf.utility.LiveEvent
import kotlinx.coroutines.flow.Flow

abstract class BaseViewModel : ViewModel() {
    // Works exactly the same way as MutableLiveData.value
    // This allows all the subclasses' live data to be declared as LiveData<T> type instead of MutableLiveData<T> so
    // that their values can't be changed externally but still can internally
    // see https://github.com/IntrepidPursuits/skotlinton-android/pull/33#discussion_r275908063
    @Deprecated("Use `Flow`s and `fun Flow<T>.latestValue` instead.")
    protected var <T : Any?> LiveData<T>.latestValue: T?
        get() = this.value
        set(value) {
            (this as MutableLiveData<T>).value = value
        }

    /**
     * Either reads the latest value from this [Flow] or
     * changes the latest value of this [Flow].
     *
     * Note that this [Flow] has a [BehaviorStateFlow] backing it.
     */
    protected var <T : Any> Flow<T>.latestValue: T
        get() = (this as BehaviorStateFlow<T>).value
        set(value) {
            (this as BehaviorStateFlow<T>).value = value
        }

    protected var <T: Any> EventFlow<T>.latestValue: T
        get() { throw IllegalStateException("Cannot read EventFlow value") }
        set(value) {
            with (this) { viewModelScope.value = value }
        }

    protected fun <T : Any> EventLiveData<T>.emit(event: T) {
        this.latestValue = LiveEvent(event)
    }
}
