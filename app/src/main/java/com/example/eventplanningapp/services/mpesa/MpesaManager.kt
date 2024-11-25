package com.example.eventplanningapp.services.mpesa

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.awaitResponse

object MpesaManager {

    private const val TAG = "MpesaManager"

    private const val CONSUMER_KEY = "ZY4WuVa1LXFz2QhAAwFffzl6GHhxNp70D5goRhdxMqoFg6OZ"
    private const val CONSUMER_SECRET = "ZHa90s6bfzoG3cNGwld0JQeTad50HQN746VLWcwcbnV0gaMvDtah39GkeCmNgsVJ"
    private const val BUSINESS_SHORT_CODE = "174379"
    private const val PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
    private const val CALLBACK_URL = "https://your-callback-url.com/callback" // Replace with your valid callback URL

    private fun getAuthorizationHeader(): String {
        val credentials = "$CONSUMER_KEY:$CONSUMER_SECRET"
        return "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
    }

    private fun getCurrentTimestamp(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    private fun generatePassword(timestamp: String): String {
        val credentials = "$BUSINESS_SHORT_CODE$PASSKEY$timestamp"
        return Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
    }

    private fun formatPhoneNumber(input: String): String {
        return if (input.startsWith("0")) {
            "254${input.substring(1)}"
        } else {
            input
        }
    }

    private suspend fun getAccessToken(): String {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<MpesaAccessTokenResponse> = MpesaClient.service.getAccessToken(
                    getAuthorizationHeader()
                ).awaitResponse()

                if (response.isSuccessful) {
                    val accessToken = response.body()?.access_token
                    Log.d(TAG, "Access Token: $accessToken")
                    return@withContext accessToken ?: throw Exception("Access token missing in response")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    throw Exception("Failed to fetch access token: HTTP ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching access token: ${e.message}", e)
                throw e
            }
        }
    }

    suspend fun initiateStkPush(phoneNumber: String, amount: Int, accountReference: String, transactionDesc: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Step 1: Get Access Token
                val accessToken = getAccessToken()

                // Step 2: Generate Timestamp and Password
                val timestamp = getCurrentTimestamp()
                val password = generatePassword(timestamp)

                // Step 3: Format Phone Number
                val formattedPhoneNumber = formatPhoneNumber(phoneNumber)

                // Step 4: Create STK Push Payload
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
                    TransactionDesc = transactionDesc
                )

                // Step 5: Send STK Push Request
                val response = MpesaClient.service.stkPushRaw(
                    accessToken = "Bearer $accessToken",
                    stkPushRequest = stkPushRequest
                ).awaitResponse()

                val rawResponseBody = response.errorBody()?.string() ?: response.body()?.string()
                Log.d(TAG, "Raw STK Push Response: $rawResponseBody")

                if (response.isSuccessful && !rawResponseBody.isNullOrBlank()) {
                    val jsonResponse = JSONObject(rawResponseBody)
                    return@withContext jsonResponse.optString("CustomerMessage", "Payment initiated successfully!")
                } else {
                    throw Exception("STK Push failed: HTTP ${response.code()} - $rawResponseBody")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initiating STK push: ${e.message}", e)
                throw e
            }
        }
    }
}
