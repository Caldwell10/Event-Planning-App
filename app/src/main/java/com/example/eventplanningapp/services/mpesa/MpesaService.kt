package com.example.eventplanningapp.services.mpesa

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class MpesaStkPushRequest(
    val BusinessShortCode: String,
    val Password: String,
    val Timestamp: String,
    val TransactionType: String = "CustomerPayBillOnline",
    val Amount: Int,
    val PartyA: String,
    val PartyB: String,
    val PhoneNumber: String,
    val CallBackURL: String,
    val AccountReference: String,
    val TransactionDesc: String
)

data class MpesaAccessTokenResponse(
    val access_token: String,
    val expires_in: String
)

interface MpesaService {
    @POST("oauth/v1/generate?grant_type=client_credentials")
    fun getAccessToken(
        @Header("Authorization") basicAuth: String
    ): Call<MpesaAccessTokenResponse>

    @POST("mpesa/stkpush/v1/processrequest")
    fun stkPushRaw(
        @Header("Authorization") accessToken: String,
        @Body stkPushRequest: MpesaStkPushRequest
    ): Call<ResponseBody>
}
