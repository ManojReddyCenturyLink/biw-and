package com.centurylink.biwf.utility

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * This [Flow] emits an event one of its collectors when [postValue] is being called.
 *
 * It does not remember or cache the value being emitted and it will notify only one of its collectors,
 * if any.
 */
@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE")
class EventFlow<T> private constructor(private val channel: Channel<T>) : Flow<T> by channel.receiveAsFlow() {

    constructor() : this(Channel())

    /**
     * Emits the value assigned this property as an event and returns immediately.
     * The emission will take place on the [CoroutineScope]-receiver.
     *
     * Note that an [IllegalStateException] will be raised when this property is read.
     */
    var CoroutineScope.value: T
        get() { throw IllegalStateException("Cannot read EventFlow value") }
        set(value) {
            launch { this@EventFlow.channel.send(value) }
        }

    /**
     * Emits the value assigned this property as an event.
     *
     * Suspends until the emitted event has been received/collected.
     */
    suspend fun postValue(value: T) {
        channel.send(value)
    }
}
