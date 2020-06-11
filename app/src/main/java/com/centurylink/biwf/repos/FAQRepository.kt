package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.service.network.FaqApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FAQRepository @Inject constructor(
    private val faqService: FaqApiService
) {
    suspend fun getFAQQuestionDetails(recordTypeId: String): Either<String, Faq> {
        if (recordTypeId.isNullOrEmpty()) {
            Either.Left("RecordType Id is Empty")
        }
        val query =
            "SELECT ArticleNumber, ArticleTotalViewCount, Article_Content__c, Article_Url__c, Id, Language, Section__c, Title FROM Knowledge__kav WHERE IsDeleted=false AND PublishStatus='Online' AND ValidationStatus='Validated'AND RecordTypeId='%s'"
        val finalQuery = String.format(query, recordTypeId)
        val result: FiberServiceResult<Faq> = faqService.getFaqDetails(finalQuery)
        return result.mapLeft { it.message?.message.toString() }
    }

    suspend fun getKnowledgeRecordTypeId(): Either<String, String> {
        val query =
            "SELECT Id FROM RecordType WHERE SobjectType = 'Knowledge__kav' AND DeveloperName ='Fiber'"
        val result: FiberServiceResult<RecordId> = faqService.getRecordTpeId(query)
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
