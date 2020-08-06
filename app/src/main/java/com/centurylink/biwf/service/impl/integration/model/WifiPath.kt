package com.centurylink.biwf.service.impl.integration.model

import io.ktor.locations.Location

@Location("/sobject/wifi/{value1}")
data class WifiPath(val value1: String)