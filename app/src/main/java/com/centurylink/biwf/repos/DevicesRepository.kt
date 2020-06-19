package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.service.network.IntegrationRestServices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicesRepository @Inject constructor(
    private val deviceApiService: IntegrationRestServices
){
    suspend fun getDevicesDetails(): Either<String, AccountDetails> {
        val result: FiberServiceResult<AccountDetails> =
            accountApiService.getAccountDetails(getAccountId()!!)
        return result.mapLeft { it.message?.message.toString() }
    }
}