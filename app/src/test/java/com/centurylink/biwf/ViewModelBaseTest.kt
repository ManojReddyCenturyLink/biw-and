package com.centurylink.biwf

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.utils.io.charsets.Charset
import kotlinx.coroutines.flow.Flow
import org.junit.Rule

abstract class ViewModelBaseTest : ViewModel() {

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    inline fun <reified T> fromJson(json: String): T {
        return Gson().fromJson(json, object : TypeToken<T>() {}.type)
    }

    fun readJson(fileName: String): String {
        val inputStream = javaClass.classLoader!!
            .getResourceAsStream("api-response/$fileName")
        return inputStream.readBytes().toString(Charset.defaultCharset())
    }

    protected var <T : Any> Flow<T>.latestValue: T
        get() = (this as BehaviorStateFlow<T>).value
        set(value) {
            (this as BehaviorStateFlow<T>).value = value
        }

    protected var <T : Any> EventFlow<T>.latestValue: T
        get() {
            throw IllegalStateException("Cannot read EventFlow value")
        }
        set(value) {
            with(this) { viewModelScope.value = value }
        }
}