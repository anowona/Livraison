package com.example.livraison.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val FIREBASE_BASE_URL = "https://us-central1-your-project-id.cloudfunctions.net/"
    private const val OSRM_BASE_URL = "http://router.project-osrm.org/"

    val firebaseApi: DeliveryApi by lazy {
        Retrofit.Builder()
            .baseUrl(FIREBASE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeliveryApi::class.java)
    }

    val routingApi: RoutingApi by lazy {
        Retrofit.Builder()
            .baseUrl(OSRM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RoutingApi::class.java)
    }
}
