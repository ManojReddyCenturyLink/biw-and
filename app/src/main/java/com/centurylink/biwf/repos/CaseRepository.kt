package com.centurylink.biwf.repos

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.cases.CaseCreate
import com.centurylink.biwf.service.network.CaseApiService
import com.centurylink.biwf.utility.preferences.Preferences

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaseRepository @Inject constructor(
    private val preferences: Preferences,
    private val caseApiService: CaseApiService
) {

    private fun getAccountId(): String? {
        return preferences.getValueByID(com.centurylink.biwf.utility.preferences.Preferences.ACCOUNT_ID)
    }

    suspend fun setServiceCallsAndTexts(callValue: Boolean): String {
        val caseCreate = CaseCreate()
        val result: FiberServiceResult<Unit> = caseApiService.submitCaseForSubscription(caseCreate)
        return result.fold(
            ifLeft = { it.message?.message.toString() },
            ifRight = { "" }
        )
    }
}
