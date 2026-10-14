package com.example.nativemicrosite.retrofit

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class TokenRequest(
    @SerializedName("countryCode") val countryCode: String,
    @SerializedName("dialCode") val dialCode: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("accessKey") val accessKey: String
)

data class TokenResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("datetime") val datetime: String,
    @SerializedName("data") val data: TokenData?
)

data class TokenData(
    @SerializedName("accessToken") val accessToken: String
)

interface ApiService {
    @POST("bkapi/auth/user/otp-less/signin")
    suspend fun regenerateToken(
        @Body body: TokenRequest
    ): Response<TokenResponse>
}
