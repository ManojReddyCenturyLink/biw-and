package com.centurylink.biwf.utility

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.centurylink.biwf.widgets.OnlineStatusBar
import com.google.android.material.tabs.TabLayout

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

    fun LiveData<Boolean>.bindToVisibility(upperTabBar: TabLayout, lowerTabBar: TabLayout, onlineStatusBar: OnlineStatusBar) {
        observe {
            upperTabBar.visibility = if (it) View.INVISIBLE else View.VISIBLE
            lowerTabBar.visibility = if (it) View.VISIBLE else View.GONE
            onlineStatusBar.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
    }
}