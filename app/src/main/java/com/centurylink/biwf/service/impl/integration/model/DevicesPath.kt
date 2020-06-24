package com.centurylink.biwf.service.impl.integration.model

import io.ktor.locations.Location

@Location("/sobject/devices/{value1}")
data class DevicesPath(val value1: String)