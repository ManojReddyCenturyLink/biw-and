package com.centurylink.biwf

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.service.impl.network.EitherCallAdapterFactory
import com.centurylink.biwf.service.impl.network.EitherConverterFactory
import com.centurylink.biwf.service.impl.network.FiberErrorConverterFactory
import com.centurylink.biwf.service.impl.network.PrimitiveTypeConverterFactory
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Rule
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class BaseServiceTest : BaseTest() {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    lateinit var mockWebServer: MockWebServer

    lateinit var retrofit: Retrofit

    private val primitiveTypeConverters = PrimitiveTypeConverterFactory()

    fun createServer() {
        mockWebServer = MockWebServer()
        retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addCallAdapterFactory(EitherCallAdapterFactory())
            .addConverterFactory(EitherConverterFactory())
            .addConverterFactory(FiberErrorConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(primitiveTypeConverters)
            .build()
    }

    fun enqueueResponse(fileName: String, headers: Map<String, String> = emptyMap()) {
        val inputStream = javaClass.classLoader!!
            .getResourceAsStream("api-response/$fileName")
        val source = inputStream.source().buffer()
        val mockResponse = MockResponse()
        for ((key, value) in headers) {
            mockResponse.addHeader(key, value)
        }
        mockWebServer.enqueue(
            mockResponse
                .setBody(source.readString(Charsets.UTF_8))
        )
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }
}