package com.centurylink.biwf.service.impl.integration.model

import io.ktor.locations.Location

@Location("/sumUp/{value1}")
data class SumUpParams(
    val value1: Int,
    val value2: Int
)
