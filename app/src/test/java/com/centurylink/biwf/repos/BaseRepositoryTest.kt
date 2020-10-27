package com.centurylink.biwf.repos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.model.FiberErrorMessage
import com.centurylink.biwf.model.FiberHttpError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.utils.io.charsets.Charset
import org.junit.Rule

abstract class BaseRepositoryTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    val fiberHttpError = FiberHttpError(
        100,
        listOf(FiberErrorMessage(errorCode = "1000", message = "Error"))
    )

    inline fun <reified T> fromJson(json: String): T {
        return Gson().fromJson(json, object : TypeToken<T>() {}.type)
    }

    fun readJson(fileName: String): String {
        val inputStream = javaClass.classLoader!!
            .getResourceAsStream("api-response/$fileName")
        return inputStream.readBytes().toString(Charset.defaultCharset())
    }
}
