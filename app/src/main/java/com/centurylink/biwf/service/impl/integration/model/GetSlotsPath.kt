package com.centurylink.biwf.service.impl.integration.model

import io.ktor.locations.Location

@Location("/sobject/getslots/{value1}")
data class GetSlotsPath(val value1: String)
