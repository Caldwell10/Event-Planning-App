package com.example.eventplanningapp.services.mpesa

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MpesaClient {
    private const val BASE_URL = "https://sandbox.safaricom.co.ke/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: MpesaService = retrofit.create(MpesaService::class.java)
}
