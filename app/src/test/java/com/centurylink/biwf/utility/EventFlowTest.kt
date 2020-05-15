package com.centurylink.biwf.utility

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withTimeout
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class EventFlowTest {

    @Test
    fun `Collecting values from empty EventFlow will get nothing`() = runBlockingTest {
        val eventFlow = EventFlow<Int>()

        val result = mutableListOf<Int>()
        try {
            withTimeout(100) {
                eventFlow.collect { result += it }
            }
        } catch (e: CancellationException) {
        }

        delay(200)
        assertThat(result.size, `is`(0))
    }

    @Test
    fun `Collect multiple values from EventFlow`() = runBlockingTest {
        val eventFlow = EventFlow<Int>()

        val result = mutableListOf<Int>()
        launch {
            withTimeout(500) {
                eventFlow.collect { result += it }
            }
        }

        delay(100)

        launch {
            for (i in 1..3) {
                eventFlow.postValue(i)
                delay(10)
            }
        }

        delay(500)
        assertThat(result, `is`(listOf(1, 2, 3)))
    }

    @Test
    fun `EventFlow waits for Collector`() = runBlockingTest {
        val eventFlow = EventFlow<Int>()

        launch {
            for (i in 1..3) {
                eventFlow.postValue(i)
                delay(10)
            }
        }

        delay(100)

        val result = mutableListOf<Int>()
        launch {
            withTimeout(500) {
                eventFlow.collect { result += it }
            }
        }

        delay(600)
        assertThat(result, `is`(listOf(1, 2, 3)))
    }

    @Test
    fun `Collect values from multiple Collectors on EventFlow only once`() = runBlockingTest {
        val eventFlow = EventFlow<Int>()

        val result = mutableListOf<Int>()
        launch {
            withTimeout(500) {
                eventFlow.collect { result += it }
            }
        }

        launch {
            withTimeout(500) {
                eventFlow.collect { result += it }
            }
        }

        delay(100)

        launch {
            for (i in 1..3) {
                eventFlow.postValue(i)
                delay(10)
            }
        }

        delay(500)
        assertThat(result, `is`(listOf(1, 2, 3)))
    }
}
