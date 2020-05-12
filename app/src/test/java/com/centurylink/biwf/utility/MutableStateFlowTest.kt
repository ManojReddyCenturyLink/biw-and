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
class MutableStateFlowTest {

    @Test(expected = IllegalStateException::class)
    fun `Getting value from empty StateFlow will throw IllegalStateException`() {
        val mutableStateFlow = BehaviorStateFlow<Int>()

        mutableStateFlow.value
    }

    @Test
    fun `Collecting values from empty StateFlow will get nothing`() = runBlockingTest {
        val mutableStateFlow = BehaviorStateFlow<Int>()

        val result = mutableListOf<Int>()
        try {
            withTimeout(100) {
                mutableStateFlow.collect { result += it }
            }
        } catch (e: CancellationException) {
        }

        assertThat(result.size, `is`(0))
    }

    @Test
    fun `Getting value from non-empty StateFlow`() {
        val mutableStateFlow = BehaviorStateFlow(3)

        assertThat(mutableStateFlow.value, `is`(3))
    }

    @Test
    fun `Collecting values from non-empty StateFlow`() = runBlockingTest {
        val mutableStateFlow = BehaviorStateFlow(5)

        val result = mutableListOf<Int>()
        try {
            withTimeout(100) {
                mutableStateFlow.collect { result += it }
            }
        } catch (e: CancellationException) {
        }

        assertThat(result, `is`(listOf(5)))
    }

    @Test
    fun `Collect multiple values from StateFlow`() = runBlockingTest {
        val mutableStateFlow = BehaviorStateFlow(100)

        val result = mutableListOf<Int>()
        launch {
            withTimeout(500) {
                mutableStateFlow.collect { result += it }
            }
        }

        for (i in 1..3) {
            mutableStateFlow.value = i
            delay(100)
        }

        assertThat(result, `is`(listOf(100, 1, 2, 3)))
    }

    @Test
    fun `Collect one values from StateFlow emitting the same value more than once`() =
        runBlockingTest {
            val mutableStateFlow = BehaviorStateFlow(100)

            val result = mutableListOf<Int>()
            launch {
                withTimeout(500) {
                    mutableStateFlow.collect { result += it }
                }
            }

            for (i in 1..3) {
                mutableStateFlow.value = 100
                delay(100)
            }

            assertThat(result, `is`(listOf(100)))
        }

    @Test
    fun `Collect multiple nullable values from StateFlow`() = runBlockingTest {
        val mutableStateFlow = BehaviorStateFlow<Int?>(100)

        val result = mutableListOf<Int?>()
        launch {
            withTimeout(500) {
                mutableStateFlow.collect { result += it }
            }
        }

        for (i in 1..3) {
            mutableStateFlow.value = if (i % 2 == 0) i else null
            delay(100)
        }

        assertThat(result, `is`(listOf(100, null, 2, null)))
    }
}
