package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SupabaseApiService {
    @POST("orders")
    suspend fun insertOrder(@Body order: SupabaseOrderDto): Response<Any>
}

class SupabaseOrderDataSource {
    private val apiService: SupabaseApiService by lazy {
        SupabaseClientProvider.retrofit.create(SupabaseApiService::class.java)
    }

    suspend fun uploadOrder(order: SupabaseOrderDto): Boolean {
        return try {
            val response = apiService.insertOrder(order)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
