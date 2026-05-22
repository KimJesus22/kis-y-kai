package com.example.data.remote

import com.example.data.Product
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SupabaseMenuApiService {
    @GET("products")
    suspend fun getProducts(
        @Query("select") select: String = "*",
        @Query("order") order: String = "sort_order.asc"
    ): Response<List<SupabaseProductDto>>
}

class SupabaseMenuDataSource {
    private val apiService: SupabaseMenuApiService by lazy {
        SupabaseClientProvider.retrofit.create(SupabaseMenuApiService::class.java)
    }

    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = apiService.getProducts()
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                val products = list.map { dto ->
                    Product(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        price = dto.price,
                        hasSauces = dto.hasSauces,
                        emoji = dto.emoji,
                        available = dto.available,
                        sortOrder = dto.sortOrder
                    )
                }
                Result.success(products)
            } else {
                Result.failure(Exception("HTTP error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
