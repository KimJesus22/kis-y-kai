package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseProductDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "price") val price: Double,
    @Json(name = "has_sauces") val hasSauces: Boolean = true,
    @Json(name = "emoji") val emoji: String? = null,
    @Json(name = "available") val available: Boolean = true,
    @Json(name = "sort_order") val sortOrder: Int = 0
)
