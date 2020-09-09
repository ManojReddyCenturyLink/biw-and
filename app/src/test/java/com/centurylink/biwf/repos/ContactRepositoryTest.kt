package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.service.network.ContactApiService
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ContactRepositoryTest : BaseRepositoryTest() {

    private lateinit var contactRepository: ContactRepository

    @MockK(relaxed = true)
    private lateinit var contactApiService: ContactApiService

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var contactDetails: ContactDetails

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        val contactDetailString = readJson("contact.json")
        contactDetails = fromJson(contactDetailString)
        contactRepository = ContactRepository(mockPreferences, contactApiService)
    }

    @Test
    fun testGetContactDetailsSuccess() {
        runBlockingTest {
            launch {
                coEvery { contactApiService.getContactDetails(any()) } returns Either.Right(
                    contactDetails
                )
                val contactIfo = contactRepository.getContactDetails()
                Assert.assertEquals(contactIfo.map { it.name }, Either.Right("Pravin Kumar"))
                Assert.assertEquals(contactIfo.map { it.Id }, Either.Right("003f000001Q5bRAAAZ"))
            }
        }
    }

    @Test
    fun testGetContactDetailsError() {
        runBlockingTest {
            launch {
                coEvery { contactApiService.getContactDetails(any()) } returns Either.Left(
                    fiberHttpError
                )
                val contactIfo = contactRepository.getContactDetails()
                Assert.assertEquals(contactIfo.mapLeft { it }, Either.Left(Constants.ERROR))
            }
        }
    }

    @Test
    fun setMarketingEmailsSuccess() {
        runBlockingTest {
            launch {
                coEvery {
                    contactApiService.submitMarketingEmail(
                        any(),
                        any()
                    )
                } returns Either.Right(Unit)
                val contactIfo = contactRepository.setMarketingEmails(false)
                Assert.assertEquals(contactIfo.map { it }, contactIfo.map { it })
            }
        }
    }

    @Test
    fun setMarketingEmailsError() {
        runBlockingTest {
            launch {
                coEvery {
                    contactApiService.submitMarketingEmail(
                        any(),
                        any()
                    )
                } returns Either.Left(fiberHttpError)
                val contactIfo = contactRepository.setMarketingEmails(false)
                Assert.assertEquals(contactIfo.map { it }, contactIfo.map { it })
            }
        }
    }

    @Test
    fun setMarketingCallsAndTextSuccess() {
        runBlockingTest {
            launch {
                coEvery {
                    contactApiService.submitMarketingCalls(
                        any(),
                        any()
                    )
                } returns Either.Right(Unit)
                val contactIfo = contactRepository.setMarketingCallsAndText(false,"1234567890")
                Assert.assertEquals(contactIfo.map { it }, contactIfo.map { it })
            }
        }
    }

    @Test
    fun setMarketingCallsAndTextError() {
        runBlockingTest {
            launch {
                coEvery {
                    contactApiService.submitMarketingCalls(
                        any(),
                        any()
                    )
                } returns Either.Left(fiberHttpError)
                val contactIfo = contactRepository.setMarketingCallsAndText(false,"1234567890")
                Assert.assertEquals(contactIfo.map { it }, contactIfo.map { it })
            }
        }
    }
}