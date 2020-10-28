package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.model.contact.UpdatedCallsandTextMarketing
import com.centurylink.biwf.model.contact.UpdatedMarketingEmails
import com.centurylink.biwf.service.network.ContactApiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ContactApiServiceTest : BaseServiceTest() {

    private lateinit var contactApiService: ContactApiService

    @Before
    fun setup() {
        createServer()
        contactApiService = retrofit.create(ContactApiService::class.java)
    }

    @Test
    fun testGetContactsDetailsSuccess() = runBlocking {
        enqueueResponse("contact.json")
        val posts: FiberServiceResult<ContactDetails> = contactApiService.getContactDetails("12233")
        Assert.assertEquals(posts.map { it.name }, Either.Right("Pravin Kumar"))
        Assert.assertEquals(posts.map { it.Id }, Either.Right("003f000001Q5bRAAAZ"))
    }

    @Test
    fun testGetContactDetailsError() = runBlocking {
        val posts: FiberServiceResult<ContactDetails> = contactApiService.getContactDetails("12233")
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }

    @Test
    fun testSubmitMarketingCallsError() = runBlocking {
        val updatedCallsandTextMarketing = UpdatedCallsandTextMarketing(true, "1234567890")
        val posts: FiberServiceResult<Unit> =
            contactApiService.submitMarketingCalls("12233", updatedCallsandTextMarketing)
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }

    @Test
    fun testSubmitMarkertingEmailsError() = runBlocking {
        val updatedMarketingEmails = UpdatedMarketingEmails(true)
        val posts: FiberServiceResult<Unit> =
            contactApiService.submitMarketingEmail("12233", updatedMarketingEmails)
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }
}
