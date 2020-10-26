package com.centurylink.biwf.service.impl.integration.model

import io.ktor.locations.Location

@Location("/sobject/reschedule/{value1}")
data class ReSchedulePath(val value1: String)
