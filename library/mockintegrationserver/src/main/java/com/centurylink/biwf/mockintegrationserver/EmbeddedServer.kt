package com.centurylink.biwf.mockintegrationserver

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.locations.Locations
import io.ktor.response.respondText
import io.ktor.routing.HttpMethodRouteSelector
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

/**
 * An embedded server, based on Ktor, running locally (`localhost`), that
 * can be used to (temporarily) fetch data if the actual back-end server is not
 * yet ready.
 */
class EmbeddedServer private constructor(
    val baseUrl: String,
    private val server: ApplicationEngine
) {
    private var hasStarted: Boolean = false

    /**
     * Starts the server.
     * Can be called multiple times. If the server already is running, it won't be started again.
     */
    fun start() {
        synchronized(this) {
            if (!hasStarted) {
                hasStarted = true
                server.start(wait = false)
            }
        }
    }

    /**
     * Stops the server.
     * Can be called multiple times. If the server is not running, it won't be stop again.
     */
    fun stop() {
        synchronized(this) {
            if (hasStarted) {
                hasStarted = false
                server.stop(0, 0)
            }
        }
    }

    companion object {
        /**
         * Creates a local embedded server on the given [port] number.
         */
        operator fun invoke(port: Int, routingConfig: Routing.() -> Unit): EmbeddedServer {
            return embeddedServer(factory = Netty, host = "localhost", port = port) {
                install(Locations)

                install(ContentNegotiation) {
                    gson()
                }

                routing {
                    val getAllRoutes = { children.asSequence().flatten().toStrings() }

                    get("/") { call.respondText { "Say 'Hello' to Ktor Netty Server" } }

                    get("/routes") {
                        call.respondText(contentType = ContentType("text", "html")) {
                            """
                                <html><body style="font-size:18px;"><p>
                                ${getAllRoutes().joinToString(separator = "</p><p>")}
                                </p></body></html>
                            """.trimIndent()
                        }
                    }

                    routingConfig()
                }
            }.let {
                EmbeddedServer("http://localhost:$port/", it)
            }
        }
    }
}

private fun Sequence<Route>.flatten(): Sequence<Route> =
    flatMap { sequenceOf(it) + it.children.asSequence().flatten() }
        .filter { it.selector is HttpMethodRouteSelector }

private fun Sequence<Route>.toStrings() = map {
    when (it.parent) {
        null -> {
            it.toString()
        }
        else -> {
            val method = it.selector as HttpMethodRouteSelector
            "${method.method.value} ${it.parent}"
        }
    }
}
