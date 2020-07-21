package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.assia.ModemIdResponse
import com.centurylink.biwf.model.user.UpdatedPassword
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
    @GET("sobjects/User")
    suspend fun qetUserInfo(): FiberServiceResult<UserInfo>

    @POST("sobjects/User/{user-id}/password")
    suspend fun updatePassword(
        @Path("user-id") id: String,
        @Body updatedPassword: UpdatedPassword
    ): FiberServiceResult<Unit>

    @GET("sobjects/User/{user-id}")
    suspend fun getCompleteUserDetails(@Path("user-id") id: String): FiberServiceResult<UserDetails>

    @GET("query")
    suspend fun getModemInfo(@Query("q") id: String): FiberServiceResult<ModemIdResponse>
}