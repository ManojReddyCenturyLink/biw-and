package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.user.UpdatedPassword
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import com.centurylink.biwf.service.resp.ErrorResponse
import com.centurylink.biwf.service.resp.ResponseResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {
    @GET("sobjects/User")
    suspend fun qetUserInfo(): UserInfo

    @POST("sobjects/User/{user-id}/password")
    suspend fun updatePassword(@Path("user-id") id: String, @Body updatedPassword: UpdatedPassword)
            : ResponseResult<UserInfo, ErrorResponse>


    @GET("sobjects/User/{user-id}")
    suspend fun getCompleteUserDetails(@Path("user-id") id: String):
            ResponseResult<UserDetails, ErrorResponse>
}