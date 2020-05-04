package com.centurylink.biwf.service.network

import kotlin.reflect.KClass

/**
 * Creates Services of a given Class.
 */
interface ServicesFactory {
    /**
     * Creates a Service instance of type [T], represented by [serviceClass].
     */
    fun <T : Any> create(serviceClass: KClass<out T>): T
}

/**
 * Synthetic sugaring to create a Service.
 */
inline fun <reified T : Any> ServicesFactory.create(): T = create(T::class)
