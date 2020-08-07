@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.centurylink.biwf.service.impl.integration

import com.centurylink.biwf.mockintegrationserver.EmbeddedServer
import com.centurylink.biwf.model.sumup.SumUpInput
import com.centurylink.biwf.model.sumup.SumUpResult
import com.centurylink.biwf.service.impl.integration.model.AppointmentPath
import com.centurylink.biwf.service.impl.integration.model.DevicesPath
import com.centurylink.biwf.service.impl.integration.model.FaqPath
import com.centurylink.biwf.service.impl.integration.model.GetSlotsPath
import com.centurylink.biwf.service.impl.integration.model.NotificationPath
import com.centurylink.biwf.service.impl.integration.model.ReSchedulePath
import com.centurylink.biwf.service.impl.integration.model.SumUpParams
import com.centurylink.biwf.service.impl.integration.model.UsageDetailsPath
import com.centurylink.biwf.service.impl.integration.model.WifiPath
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
                .getResourceAsStream("api-response/notifications.json")
                .copyTo(this)
        }
    }

    get<AppointmentPath> {
        call.respondOutputStream {
            javaClass.classLoader!!
                .getResourceAsStream("api-response/appointments.json")
                .copyTo(this)
        }
    }

    get<FaqPath> {
        call.respondOutputStream {
            javaClass.classLoader!!
                .getResourceAsStream("api-response/faqnosection.json")
                .copyTo(this)
        }
    }

    get<DevicesPath> {
        call.respondOutputStream {
            javaClass.classLoader!!
                .getResourceAsStream("api-response/devicedetails.json")
                .copyTo(this)
        }
    }

    get<UsageDetailsPath> {
        call.respondOutputStream {
            javaClass.classLoader!!
                .getResourceAsStream("api-response/usagedetails.json")
                .copyTo(this)
        }
    }

    get<GetSlotsPath> {
        call.respondOutputStream {
            javaClass.classLoader!!
                .getResourceAsStream("api-response/getslots.json")
                .copyTo(this)
        }
    }

    post<ReSchedulePath> {
        call.respondOutputStream {
            javaClass.classLoader!!
                .getResourceAsStream("api-response/appointmentchange.json")
                .copyTo(this)
        }
    }

    get<WifiPath> {
        call.respondOutputStream {
            javaClass.classLoader!!
                .getResourceAsStream("api-response/scaninfo.json")
                .copyTo(this)
        }
    }
}



