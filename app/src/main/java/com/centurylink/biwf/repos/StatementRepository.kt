package com.centurylink.biwf.repos

import com.centurylink.biwf.service.network.ApiServices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatementRepository @Inject constructor(
    private val apiServices: ApiServices
) {

}