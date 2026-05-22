package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseCartItemDto(
    @Json(name = "product_id") val productId: String,
    @Json(name = "product_name") val productName: String,
    @Json(name = "price") val price: Double,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "sauce") val sauce: String,
    @Json(name = "note") val note: String
)

@JsonClass(generateAdapter = true)
data class SupabaseOrderDto(
    @Json(name = "local_order_id") val localOrderId: String,
    @Json(name = "order_code") val orderCode: String,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "delivery_method") val deliveryMethod: String,
    @Json(name = "municipality") val municipality: String?,
    @Json(name = "address") val address: String,
    @Json(name = "payment_method") val paymentMethod: String,
    @Json(name = "cash_pay_with") val cashPayWith: Double?,
    @Json(name = "status") val status: String,
    @Json(name = "items_json") val itemsJson: List<SupabaseCartItemDto>,
    @Json(name = "subtotal") val subtotal: Double,
    @Json(name = "delivery_fee") val deliveryFee: Double,
    @Json(name = "total") val total: Double,
    @Json(name = "distance_km") val distanceKm: Double,
    @Json(name = "estimated_time_minutes") val estimatedTimeMinutes: Int,
    @Json(name = "destination_lat") val destinationLat: Double,
    @Json(name = "destination_lng") val destinationLng: Double,
    @Json(name = "courier_lat") val courierLat: Double,
    @Json(name = "courier_lng") val courierLng: Double
)
