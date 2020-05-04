package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.testrest.ContactList
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * This interface is just temporary, to prove a proof-of-concept.
 */
@Deprecated("Temporary interface for P.O.C.")
interface TestRestServices {
    @GET("query/")
    fun query(@Query("q") soqlQuery: String): Single<ContactList>
}
