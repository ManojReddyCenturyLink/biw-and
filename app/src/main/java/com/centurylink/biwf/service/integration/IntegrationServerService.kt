package com.centurylink.biwf.service.integration

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
    fun start() {}

    /**
     * Stops the embedded Integration Server.
     */
    fun stop() {}
}
