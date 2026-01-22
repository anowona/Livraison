package com.example.livraison.network

import com.example.livraison.model.Product
import retrofit2.http.GET

interface DeliveryApi {
    @GET("products")
    suspend fun getProducts(): List<Product>
}
