package com.centurylink.biwf.service.impl.integration.model

import io.ktor.locations.Location

@Location("/sobject/notification/{value1}")
data class NotificationPath (val value1: String)