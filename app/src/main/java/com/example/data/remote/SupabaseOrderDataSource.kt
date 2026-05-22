package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApiService {
    @POST("orders")
    suspend fun insertOrder(@Body order: SupabaseOrderDto): Response<Any>

    @GET("orders")
    suspend fun getOrderStatusByLocalId(
        @Query("local_order_id") filter: String,
        @Query("select") select: String = "local_order_id,status"
    ): Response<List<SupabaseOrderStatusDto>>

    @GET("orders")
    suspend fun getAllOrders(
        @Query("select") select: String = "*"
    ): Response<List<SupabaseOrderDto>>
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

    suspend fun getRemoteOrderStatus(localOrderId: String): Result<String> {
        return try {
            val response = apiService.getOrderStatusByLocalId("eq.$localOrderId")
            if (response.isSuccessful) {
                val list = response.body()
                val status = list?.firstOrNull()?.status
                if (status != null) {
                    Result.success(status)
                } else {
                    Result.failure(Exception("Order not found or status is null"))
                }
            } else {
                Result.failure(Exception("HTTP error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRemoteOrders(): Result<List<SupabaseOrderDto>> {
        return try {
            val response = apiService.getAllOrders()
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                Result.success(list)
            } else {
                Result.failure(Exception("HTTP error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

