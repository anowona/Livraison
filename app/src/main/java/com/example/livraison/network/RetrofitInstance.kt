package com.example.livraison.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: DeliveryApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://example.com/") // peut rester un faux URL pour la démo
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeliveryApi::class.java)
    }
}
