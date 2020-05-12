package com.centurylink.biwf.utility

import com.centurylink.biwf.utility.BehaviorStateFlow.Optional.None
import com.centurylink.biwf.utility.BehaviorStateFlow.Optional.Value
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect

/**
 * A [kotlinx.coroutines.flow.Flow] that is conflated, has state, i.e. it always remembers the last value it has emitted and
 * that allows its value to be changed.
 *
 * When a value is changed, this [kotlinx.coroutines.flow.Flow] will emit that new value.
 *
 * This class is somewhat different than the standard [MutableStateFlow], since [MutableStateFlow]
 * must have an initial value, while this [BehaviorStateFlow] can start without an initial value.
 *
 * @param initialValue The optional initial value of this [MutableStateFlow].
 */
@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE")
class BehaviorStateFlow<T> private constructor(initialValue: Optional<T>) : MutableStateFlow<T> {
    constructor() : this(None)

    constructor(initialValue: T) : this(Value(initialValue))

    private val delegate = MutableStateFlow(initialValue)

    override var value: T
        get() = when (val optional = delegate.value) {
            is None -> throw IllegalStateException("Value not yet set")
            is Value -> optional.value
        }
        set(value) {
            delegate.value = Value(value)
        }

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<T>) {
        delegate.collect {
            if (it is Value<T>) collector.emit(it.value)
        }
    }

    private sealed class Optional<out T> {
        data class Value<out T>(val value: T) : Optional<T>()
        object None : Optional<Nothing>()
    }
}
