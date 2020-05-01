package com.centurylink.biwf.service.impl.network

import com.centurylink.biwf.service.network.ServicesFactory
import retrofit2.Retrofit
import kotlin.reflect.KClass

/**
 * Implements [ServicesFactory] for the [Retrofit] library.
 */
class RetrofitServicesFactory(private val retrofit: Retrofit) : ServicesFactory {
    override fun <T : Any> create(serviceClass: KClass<out T>): T =
        retrofit.create(serviceClass.java)
}

val Retrofit.asFactory: RetrofitServicesFactory get() = RetrofitServicesFactory(this)
