@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.centurylink.biwf.service.impl.integration

import com.centurylink.biwf.mockintegrationserver.EmbeddedServer
import com.centurylink.biwf.model.sumup.SumUpInput
import com.centurylink.biwf.model.sumup.SumUpResult
import com.centurylink.biwf.service.impl.integration.model.NotificationPath
import com.centurylink.biwf.service.impl.integration.model.SumUpParams
import io.ktor.application.call
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondOutputStream
import timber.log.Timber

/**
 * This is the actual Integration Server based on the [EmbeddedServer].
 *
 * Implement incoming request here (get, patch, etc) and return the appropriate response.
 */
val IntegrationServer: EmbeddedServer = EmbeddedServer(10101) {
    trace { Timber.d(it.buildText()) }

    post<SumUpParams> { params ->
        val bodyInput = call.receive<SumUpInput>()
        call.respond(SumUpResult(params.value1 + params.value2 + bodyInput.value3))
    }

    get<NotificationPath> {
        call.respondOutputStream {
            javaClass.classLoader!!
                .getResourceAsStream("api-response/notification.json")
                .copyTo(this)
        }
    }
}



