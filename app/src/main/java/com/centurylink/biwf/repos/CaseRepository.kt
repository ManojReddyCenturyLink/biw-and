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

@Singleton
class CaseRepository @Inject constructor(
    private val preferences: Preferences,
    private val caseApiService: CaseApiService
) {

    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    private fun getContactId(): String? {
        return preferences.getValueByID(Preferences.CONTACT_ID)
    }


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

    suspend fun getCaseId(): Either<String, Cases> {
        val result: FiberServiceResult<Cases> = caseApiService.getCaseNumber()
        return result.mapLeft { it.message?.message.toString() }
    }

    suspend fun getRecordTypeId(): Either<String, String> {
        val result: FiberServiceResult<RecordId> = caseApiService.getRecordTpeId(EnvironmentPath.RECORD_TYPE_ID_QUERY)
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
