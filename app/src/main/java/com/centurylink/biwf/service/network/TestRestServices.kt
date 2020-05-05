package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.testrest.ContactList
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * This interface is just temporary, to prove a proof-of-concept.
 */
@Deprecated("Temporary interface for P.O.C.")
interface TestRestServices {
    @GET("query/")
    suspend fun query(@Query("q") soqlQuery: String): ContactList
}
