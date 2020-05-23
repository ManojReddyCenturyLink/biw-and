package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.cases.CaseCreate
import com.centurylink.biwf.model.cases.Cases
import com.centurylink.biwf.service.network.CaseApiService
import com.centurylink.biwf.utility.DateUtils
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
        cancellationDate: Date, cancellationReason: String?, cancellationReasonExpln: String?,
        rating: Float?, comments: String?
    ): String {
        val caseCreate = CaseCreate(
            accountId = getAccountId() ?: "",
            contactId = getContactId() ?: "",
            cancellationReason = cancellationReason ?: "",
            cancelReasonComments = cancellationReasonExpln ?: "",
            cancellationDateHolder = DateUtils.toSimpleString(
                cancellationDate,
                DateUtils.STANDARD_FORMAT
            ),
            notes = comments ?: "",
            experience = String.format("%.0f", rating)
        )
        val result: FiberServiceResult<Unit> =
            caseApiService.submitCaseForSubscription(caseCreate)
        return result.fold(
            ifLeft = { it.message?.message.toString() },
            ifRight = { "" }
        )
    }

    suspend fun getCaseId(): Either<String, Cases> {
        val result: FiberServiceResult<Cases> = caseApiService.getCaseNumber()
        return result.mapLeft { it.message?.message.toString() }
    }
}
