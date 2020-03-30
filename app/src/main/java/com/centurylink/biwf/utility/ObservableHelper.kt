package com.centurylink.biwf.utility

import io.reactivex.rxjava3.subjects.BehaviorSubject

class ObservableData<T>(val defaultValue: T) {
    var value: T = defaultValue!!
        set(value) {
            if (value != "") {
                field = value
                observable.onNext(value)
            }
        }
    val observable = BehaviorSubject.createDefault(value)
}