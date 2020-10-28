package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.payment.PaymentDetails
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ZuoraPaymentService {

    @GET(EnvironmentPath.SALES_FORCE_QUERY_SLASH)
    suspend fun getZuoraPaymentDetails(@Query(EnvironmentPath.SALES_FORCE_QUERY_VALUE) id: String): FiberServiceResult<PaymentList>

    @GET(EnvironmentPath.API_PAYMENT_DETAILS_PATH)
    suspend fun getPaymentDetails(@Path(EnvironmentPath.INVOICE_ID) id: String): FiberServiceResult<PaymentDetails>
}
