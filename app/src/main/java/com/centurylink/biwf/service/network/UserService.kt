package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.user.UpdatedPassword
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {
    @GET(EnvironmentPath.API_USER_DETAILS_PATH)
    suspend fun qetUserInfo(): FiberServiceResult<UserInfo>

    @POST(EnvironmentPath.API_UPDATE_PASSWORD_PATH)
    suspend fun updatePassword(
        @Path(EnvironmentPath.USER_ID) id: String,
        @Body updatedPassword: UpdatedPassword
    ): FiberServiceResult<Unit>

    @GET(EnvironmentPath.API_USER_ID_PATH)
    suspend fun getCompleteUserDetails(@Path(EnvironmentPath.USER_ID) id: String): FiberServiceResult<UserDetails>
}
