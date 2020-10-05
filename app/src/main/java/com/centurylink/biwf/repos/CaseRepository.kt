package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.cases.CaseCreate
import com.centurylink.biwf.model.cases.CaseResponse
import com.centurylink.biwf.model.cases.Cases
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.service.network.CaseApiService
import com.centurylink.biwf.utility.DateUtils
import com.centurylink.biwf.utility.EnvironmentPath
import com.centurylink.biwf.utility.preferences.Preferences
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class interacts with Case API Services. This Repository class
 * gets the data from the network . It handles all the Case related information from the Salesforce
 * backend  and the View models can consume the case related information and display in the Activity
 * or Fragments.
 *
 * @property preferences Instance for storing the value in shared preferences.
 * @property caseApiService Instance for interacting with the Sales force Case API.
 * @constructor Create  Case repository.
 */
@Singleton
class CaseRepository @Inject constructor(
    private val preferences: Preferences,
    private val caseApiService: CaseApiService
) {

    /**
     * This method is used to get the Account Id that is stored in the  Shared Preferences
     * @return The Account Id.
     */
    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    /**
     * This method is used to get the Contact Id that is stored in the  Shared Preferences
     * @return The Contact Id.
     */
    private fun getContactId(): String? {
        return preferences.getValueByID(Preferences.CONTACT_ID)
    }


    /**
     * The Suspend function used for the purpose of DeActivating the requests
     *
     * @param cancellationDate The Cancellation Date provided by the user.
     * @param cancellationReason Cancellation Reason provided by the user.
     * @param cancellationReasonExpln The Cancellation comments provided by the user.
     * @param rating The rating provided by the user.
     * @param comments The Comments Provided by the user.
     * @param recordTypeId The Rescord Id.
     * @return the Case Response if the API is success else the Error message is shown.
     */
    suspend fun createDeactivationRequest(
        cancellationDate: Date,
        cancellationReason: String?,
        cancellationReasonExpln: String?,
        rating: Float?, comments: String?,
        recordTypeId: String
    ): Either<String, CaseResponse> {
        val caseCreate = CaseCreate(
            contactId = getContactId() ?: "",
            cancellation_Reason__c = cancellationReason ?: "",
            cancelReason_Comments__c = cancellationReasonExpln ?: "",
            cancellation_Date_Holder__c = DateUtils.toSimpleString(
                cancellationDate,
                DateUtils.STANDARD_FORMAT
            ),
            notes__c = comments ?: "",
            experience__c = String.format("%.0f", rating),
            recordTypeId = recordTypeId
        )
        val result: FiberServiceResult<CaseResponse> =
            caseApiService.submitCaseForSubscription(caseCreate)
        return result.mapLeft { it.message?.message.toString() }
    }

    /**
     * The Suspend function used for the purpose of getting the case Id.
     *
     * @return the Case instance if the API is success else the Error message is shown.
     */
    suspend fun getCaseId(): Either<String, Cases> {
        val result: FiberServiceResult<Cases> = caseApiService.getCaseNumber()
        return result.mapLeft { it.message?.message.toString() }
    }

    /**
     * Gets the record Type Id of the User.
     *
     * @return Success and the error message if there is any issue.
     */
    suspend fun getRecordTypeId(): Either<String, String> {
        val result: FiberServiceResult<RecordId> =
            caseApiService.getRecordTpeId(EnvironmentPath.RECORD_TYPE_ID_QUERY)
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            val id = it.records.elementAtOrElse(0) { null }?.Id
            if (id.isNullOrEmpty()) {
                Either.Left("Record Id  Records is Empty")
            } else {
                Either.Right(id)
            }
        }
    }
}
