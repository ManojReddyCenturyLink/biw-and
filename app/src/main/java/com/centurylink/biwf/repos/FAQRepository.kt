package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.service.network.FaqApiService
import com.centurylink.biwf.utility.EnvironmentPath
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FAQ repository   - This class interacts with FAQ API Services. This Repository class
 * gets the data from the network . It handles all the FAQ related information from the Salesforce
 * backend  and the View models can consume the FAQ related information and display in the Activity
 * or Fragments.
 *
 * @property faqService FAQService  Instance for interacting with the Sales force FAQ API.
 * @constructor Create  Faq repository
 */
@Singleton
class FAQRepository @Inject constructor(
    private val faqService: FaqApiService
) {
    /**
     * This method is used to getting the FAQ Question Details
     *
     * @param recordTypeId The record type Id
     * @return  Faq instance on Success and error in case of failure.
     */
    suspend fun getFAQQuestionDetails(recordTypeId: String): Either<String, Faq> {
        if (recordTypeId.isNullOrEmpty()) {
            Either.Left("RecordType Id is Empty")
        }
        val finalQuery = String.format(EnvironmentPath.FAQ_QUESTION_DETAILS_QUERY, recordTypeId)
        val result: FiberServiceResult<Faq> = faqService.getFaqDetails(finalQuery)
        return result.mapLeft { it.message?.message.toString() }
    }

    /**
     * This method is used to getting the Knowledge RecordType Id.
     *
     * @return Success message  error message  in case of failure
     */
    suspend fun getKnowledgeRecordTypeId(): Either<String, String> {
        val result: FiberServiceResult<RecordId> =
            faqService.getRecordTypeId(EnvironmentPath.KNOWLEDGE_RECORD_TYPE_ID_QUERY)
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
