package com.centurylink.biwf.service.impl.integration.model

import io.ktor.locations.Location

@Location("/sobject/appointment/{value1}")
data class AppointmentPath (val value1: String)