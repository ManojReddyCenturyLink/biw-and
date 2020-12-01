package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.modem.ModemIdResponse
import com.centurylink.biwf.service.network.response.ModemIdService
import com.centurylink.biwf.utility.EnvironmentPath
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ModemId Repository   - This class interacts with Modem API Services. This Repository class
 * gets the data from the network .
 *
 * @property modemIdService ModemIdService  Instance for interacting with the Sales force Modem API.
 * @constructor Create  ModemId Repository
 */
@Singleton
class ModemIdRepository @Inject constructor(
    private val modemIdService: ModemIdService,
    private val preferences: Preferences
) {

    /**
     * Gets the modem Id of the Modem.
     *
     * @return Success and the error message if there is any issue.
     */
    suspend fun getModemTypeId(): Either<String, String> {
        val accountId = getAccountId()
        if (accountId.isNullOrEmpty()) {
            return Either.Left("Account ID is not available")
        }
        val finalQuery = String.format(EnvironmentPath.MODEM_ID_QUERY, accountId)
        val result: FiberServiceResult<ModemIdResponse> =
            modemIdService.getModemId(finalQuery)
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            val id = it.records.elementAtOrElse(0) { null }?.modemNumberC
            if (id.isNullOrEmpty()) {
                Either.Left("Modem Id is Empty")
            } else {
                Either.Right(id)
            }
        }
    }

    /**
     * This method is used to get the Account Id that is stored in the  Shared Preferences
     *
     * @return The Account Id of the user.
     */
    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }
}
