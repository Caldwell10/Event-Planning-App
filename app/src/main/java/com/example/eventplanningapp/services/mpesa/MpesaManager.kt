package com.example.eventplanningapp.services.mpesa

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

object MpesaManager {

    private const val BUSINESS_SHORT_CODE = "174379"
    private const val PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
    private const val CALLBACK_URL = "https://api-p5opgrin7a-uc.a.run.app"
    private const val TAG = "MpesaManager"

    // Function to create the authorization header for access token request
    private fun getAuthorizationHeader(consumerKey: String, consumerSecret: String): String {
        val credentials = "$consumerKey:$consumerSecret"
        return "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
    }

    // Function to get the current timestamp in the format yyyyMMddHHmmss
    private fun getCurrentTimestamp(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    // Function to format the phone number to the required format (e.g., 2547XXXXXXXX)
    private fun formatPhoneNumber(input: String): String {
        return if (input.startsWith("0")) {
            "254${input.substring(1)}"
        } else {
            input
        }
    }

    // Main function to initiate the payment process
    suspend fun initiatePayment(
        phoneNumber: String,
        amount: Int,
        accountReference: String,
        description: String
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                // Step 1: Get access token
                Log.d(TAG, "Fetching access token...")
                val tokenResponse = MpesaClient.service.getAccessToken(
                    getAuthorizationHeader(
                        "BCPNpQCgMY0RCpwGmSy4w45LAcIhgP8Olx38SVyteNzEL7Kl",
                        "pSolm3EXCwwDRkBbAeOAISFrr9eVcGz1ZAVgKQDI6GQmvzpxtR2DuisPhQPlJ4PK"
                    )
                ).awaitResponse()

                if (!tokenResponse.isSuccessful) {
                    val errorBody = tokenResponse.errorBody()?.string()
                    Log.e(TAG, "Failed to fetch access token: ${errorBody ?: "No error details"}")
                    throw Exception("Failed to fetch access token: ${errorBody ?: "No error details"}")
                }

                val accessToken = tokenResponse.body()?.access_token
                if (accessToken.isNullOrBlank()) {
                    throw Exception("Access token is null or empty")
                }
                Log.d(TAG, "Access Token: $accessToken")

                // Step 2: Generate timestamp and password
                val timestamp = getCurrentTimestamp()
                val password = Base64.encodeToString(
                    "$BUSINESS_SHORT_CODE$PASSKEY$timestamp".toByteArray(),
                    Base64.NO_WRAP
                )
                Log.d(TAG, "Generated Timestamp: $timestamp, Password: $password")

                // Step 3: Create STK Push request
                val formattedPhoneNumber = formatPhoneNumber(phoneNumber)
                val stkPushRequest = MpesaStkPushRequest(
                    BusinessShortCode = BUSINESS_SHORT_CODE,
                    Password = password,
                    Timestamp = timestamp,
                    Amount = amount,
                    PartyA = formattedPhoneNumber,
                    PartyB = BUSINESS_SHORT_CODE,
                    PhoneNumber = formattedPhoneNumber,
                    CallBackURL = CALLBACK_URL,
                    AccountReference = accountReference,
                    TransactionDesc = description
                )
                Log.d(TAG, "STK Push Request: $stkPushRequest")

                // Step 4: Make STK Push call
                val stkResponse = MpesaClient.service.stkPush(
                    accessToken = "Bearer $accessToken",
                    stkPushRequest = stkPushRequest
                ).awaitResponse()

                if (!stkResponse.isSuccessful) {
                    val errorBody = stkResponse.errorBody()?.string()
                    Log.e("MpesaManager", "STK Push failed. Error body: ${errorBody ?: "No error details"}")
                    throw Exception("STK Push failed. Response: ${errorBody ?: "No error details"}")
                }

                val responseBody = stkResponse.body()
                if (responseBody == null) {
                    Log.e("MpesaManager", "STK Push response is empty")
                    return@withContext "Error: Empty response from the server"
                }

                Log.d("MpesaManager", "STK Push Response: $responseBody")

                responseBody.CustomerMessage
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
                "Error: ${e.message}"
            }.toString()
        }
    }
}
