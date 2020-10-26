package com.centurylink.biwf.service.impl.integration.model

import io.ktor.locations.Location

@Location("/sobject/faq/{value1}")
data class FaqPath(val value1: String)
