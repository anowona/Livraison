package com.example.livraison.network

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Defines the API for fetching routing data from OSRM (Open Source Routing Machine).
 */
interface RoutingApi {
    @GET("/route/v1/driving/{coordinates}?overview=full&geometries=polyline")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String
    ): RouteResponse
}

data class RouteResponse(val routes: List<Route>)
data class Route(val geometry: String)
