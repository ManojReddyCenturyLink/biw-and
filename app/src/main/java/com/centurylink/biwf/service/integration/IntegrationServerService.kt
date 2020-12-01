package com.centurylink.biwf.service.integration

import timber.log.Timber

/**
 * Starts and stops the embedded Integration Server.
 */
interface IntegrationServerService {
    /**
     * Base URL of the embedded Integration Server.
     */
    val baseUrl: String get() = "http:/unknown-host/"

    /**
     * Starts the embedded Integration Server.
     * This method can be called multiple times without restarting the server.
     */
    fun start() {
        Timber.e("This function  Starts the embedded Integration Server.")
    }

    /**
     * Stops the embedded Integration Server.
     */
    fun stop() {
        Timber.e("This function Stops the embedded Integration Server. ")
    }
}
