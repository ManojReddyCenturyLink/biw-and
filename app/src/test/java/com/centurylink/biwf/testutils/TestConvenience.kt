package com.centurylink.biwf.testutils

import androidx.lifecycle.LiveData
import com.centurylink.biwf.utility.EventLiveData
import org.amshove.kluent.shouldNotBeNull

inline fun <T> T.assert(assertion: (T) -> Unit) {
    assertion(this)
}

inline fun <T : Any> T?.nonNullAssert(assertion: (T) -> Unit) {
    this.shouldNotBeNull()
    assertion(this)
}

fun <T> EventLiveData<T>.event(): T? {
    return value?.peekContent()
}

val <T> LiveData<T>.transformedValue: T?
    get() {
        observeForever { }
        return this.value
    }
