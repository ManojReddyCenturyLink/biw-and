package com.centurylink.biwf.network

import com.centurylink.biwf.model.user.UpdatedPassword
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import retrofit2.http.*

interface UserService {
    @GET("sobjects/User")
    suspend fun qetUserInfo(): UserInfo

    @POST("sobjects/User/{user-id}/password")
    suspend fun updatePassword(@Path("user-id") id:String, @Body updatedPassword:UpdatedPassword)

    @GET("sobjects/User/{user-id}")
    suspend fun getCompleteUserDetails(@Path("user-id") id:String):UserDetails
}