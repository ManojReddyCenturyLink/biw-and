package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
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
            "SELECT ArticleNumber, ArticleTotalViewCount, Article_Content__c, Article_Url__c, Id, Language, Section__c, Title FROM Knowledge__kav WHERE IsDeleted=false AND PublishStatus='Online' AND ValidationStatus='Validated'AND RecordTypeId='012f0000000l1hsAAA'"
        val result: FiberServiceResult<Faq> = faqService.getFaqDetails(query)
        return result.mapLeft { it.message?.message.toString() }
    }
}
