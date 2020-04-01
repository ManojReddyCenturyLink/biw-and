package com.centurylink.biwf.utility

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

interface LiveDataObserver {

    val liveDataLifecycleOwner: LifecycleOwner

    fun <T> LiveData<T>.observe(observer: (T) -> Unit) {
        observe(liveDataLifecycleOwner, Observer { observer(it) })
    }
    // Handles the event and consumes it. If there are multiple observers, only the first observer will receive the event
    fun <T> EventLiveData<T>.handleEvent(observer: (T) -> Unit) {
        observe { liveDataValue ->
            liveDataValue.getContentIfNotHandled()?.let { event ->
                observer(event)
            }
        }
    }
}