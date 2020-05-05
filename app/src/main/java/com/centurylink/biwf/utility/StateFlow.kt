package com.centurylink.biwf.utility

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

/**
 * A [Flow] that is conflated, has state, i.e. it always remembers the last value it has emitted.
 */
interface StateFlow<out T : Any> : Flow<T> {
    /**
     * Current/Last value of this StateFlow.
     *
     * If no state is defined/set, accessing this property will throw a [IllegalStateException].
     */
    val value: T
}

/**
 * A [Flow] that is conflated, has state, i.e. it always remembers the last value it has emitted and
 * that allows its value to be changed.
 *
 * When a value is changed, this [Flow] will emit that new value.
 *
 * @param initialValue The initial value of this [StateFlow]. If `null`, there is no initial value.
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class MutableStateFlow<T : Any>(initialValue: T? = null) : StateFlow<T> {
    private val broadcast: ConflatedBroadcastChannel<T> =
        initialValue?.let { ConflatedBroadcastChannel(it) } ?: ConflatedBroadcastChannel()

    override var value: T
        get() = broadcast.value
        set(value: T) {
            broadcast.offer(value)
        }

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<T>) {
        broadcast.consumeEach { collector.emit(it) }
    }
}
