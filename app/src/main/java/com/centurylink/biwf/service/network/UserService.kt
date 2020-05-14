package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.user.UpdatedPassword
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {
    @GET("sobjects/User")
    suspend fun qetUserInfo(): UserInfo

    @POST("sobjects/User/{user-id}/password")
    suspend fun updatePassword(@Path("user-id") id: String, @Body updatedPassword: UpdatedPassword): FiberServiceResult<Unit>

    @GET("sobjects/User/{user-id}")
    suspend fun getCompleteUserDetails(@Path("user-id") id: String): UserDetails
}