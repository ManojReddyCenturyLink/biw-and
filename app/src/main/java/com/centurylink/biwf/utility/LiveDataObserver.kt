package com.centurylink.biwf.utility

import android.view.View
import android.widget.CheckBox
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.centurylink.biwf.widgets.OnlineStatusBar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface LiveDataObserver {

    val lifecycleOwner: LifecycleOwner

    fun <T> LiveData<T>.observe(observer: (T) -> Unit) {
        observe(lifecycleOwner, Observer { observer(it) })
    }

    // Handles the event and consumes it. If there are multiple observers, only the first observer will receive the event
    fun <T> EventLiveData<T>.handleEvent(observer: (T) -> Unit) {
        observe { liveDataValue ->
            liveDataValue.getContentIfNotHandled()?.let { event ->
                observer(event)
            }
        }
    }

    fun LiveData<Boolean>.bindToVisibility(
        upperTabBar: TabLayout,
        lowerTabBar: TabLayout,
        onlineStatusBar: OnlineStatusBar
    ) {
        observe {
            upperTabBar.visibility = if (it) View.INVISIBLE else View.VISIBLE
            lowerTabBar.visibility = if (it) View.VISIBLE else View.GONE
            onlineStatusBar.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
    }

    fun LiveData<Boolean>.bindToSwitch(switch: SwitchMaterial) {
        observe {
            switch.isChecked = it
        }
    }

    fun LiveData<Boolean>.bindToSwitch(switch: Switch) {
        observe {
            switch.isChecked = it
        }
    }
    fun LiveData<String>.bindToTextView(textView: TextView) {
        observe {
            textView.text = it
        }
    }

    fun LiveData<Boolean>.bindToCheckBox(checkBox: CheckBox) {
        observe {
            checkBox.isChecked = it
        }
    }


    /**
     * Observes this [Flow] instance by calling [observe] each time
     * a value is emitted.
     *
     * The [observe] lambda is a `suspend` lambda. This can come in handy for certain
     * UI extension functions that are suspending functions, but don't try to abuse it by doing
     * stuff that the ViewModel should do :-).
     *
     * Note that values are only emitted when the Activity is at least in a STARTED state.
     */
     fun <T> Flow<T>.observe(observe: suspend (T) -> Unit) {
        lifecycleOwner.lifecycleScope.launchWhenStarted {
            @Suppress("EXPERIMENTAL_API_USAGE")
            this@observe
                .onEach(observe)
                .launchIn(this)
        }
    }
}
