package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlin.math.*
import com.example.data.remote.SupabaseMenuDataSource

// ----------------------------------------------------
// MODELS
// ----------------------------------------------------

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val hasSauces: Boolean = true,
    val emoji: String? = null,
    val available: Boolean = true,
    val sortOrder: Int = 0
)

data class CartItem(
    val id: Int,
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val sauce: String,
    val note: String = ""
)

// ----------------------------------------------------
// REPOSITORY
// ----------------------------------------------------

class FoodRepository(
    private val cartDao: CartDao,
    private val orderDao: OrderDao
) {
    // 1. Static Menu Items from WhatsApp sign
    val menuProducts = listOf(
        Product(
            id = "alitas_papas",
            name = "Alitas con papas (8 Pzas)",
            description = "Crujientes alitas marinadas con acompañamiento de papas a la francesa.",
            price = 120.0,
            hasSauces = true
        ),
        Product(
            id = "boneless",
            name = "Boneless (8 Pzas)",
            description = "Trocitos de pechuga de pollo empanizados y bañados en tu salsa favorita.",
            price = 100.0,
            hasSauces = true
        ),
        Product(
            id = "boneless_papas",
            name = "Boneless c/papas (8 Pzas)",
            description = "Boneless premium acompañados de nuestras papas fritas sazonadas.",
            price = 120.0,
            hasSauces = true
        ),
        Product(
            id = "papas_orden",
            name = "Orden de papas",
            description = "Deliciosa porción de papas a la francesa, doraditas y sazonadas.",
            price = 35.0,
            hasSauces = false
        )
    )

    // Coordenadas de Referencia
    val latStore = 20.3705
    val lngStore = -101.0638 // Jaral del Progreso, Guanajuato (Local Central)

    // Valle de Santiago: ~16.5km en línea recta
    val latValle = 20.3926
    val lngValle = -101.1915

    // Cortazar: ~19km en línea recta
    val latCortazar = 20.4802
    val lngCortazar = -100.9613

    // 2. Cart Operations
    val cartItems: Flow<List<CartItemEntity>> = cartDao.getCartItems()

    suspend fun addToCart(product: Product, quantity: Int, sauce: String, note: String = "") = withContext(Dispatchers.IO) {
        // Check if an item with same productId and sauce already exists
        val currentItems = cartItems.first()
        val existing = currentItems.find { it.productId == product.id && it.sauce == sauce }

        if (existing != null) {
            cartDao.updateItem(
                existing.copy(
                    quantity = existing.quantity + quantity,
                    note = if (note.isNotEmpty()) "${existing.note} | $note" else existing.note
                )
            )
        } else {
            cartDao.insertItem(
                CartItemEntity(
                    productId = product.id,
                    productName = product.name,
                    price = product.price,
                    quantity = quantity,
                    sauce = sauce,
                    note = note
                )
            )
        }
    }

    suspend fun updateCartQuantity(itemId: Int, quantity: Int) = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            cartDao.clearCart() // Simple safety fallback should we wish to purge
        } else {
            // we will query and update if found
        }
    }

    suspend fun updateCartItemEntity(entity: CartItemEntity) = withContext(Dispatchers.IO) {
        cartDao.updateItem(entity)
    }

    suspend fun removeCartItem(entity: CartItemEntity) = withContext(Dispatchers.IO) {
        cartDao.deleteItem(entity)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart()
    }

    // 3. Distance and ETA calculations
    fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        // Multiplicador de ruta callejera promedio de 1.28 para simular distancia real de calles/carreteras
        return (r * c) * 1.28
    }

    fun getDestinationCoordinates(municipality: String): Pair<Double, Double> {
        return when (municipality.uppercase()) {
            "VALLE_SANTIAGO" -> Pair(latValle, lngValle)
            "CORTAZAR" -> Pair(latCortazar, lngCortazar)
            else -> Pair(latStore, lngStore) // Jaral del Progreso Pick up
        }
    }

    fun calculateDeliveryDetails(municipality: String): DeliveryDetails {
        if (municipality == "RECOGER") {
            return DeliveryDetails(
                distanceKm = 0.0,
                deliveryFee = 0.0,
                estimatedPrepMinutes = 15,
                estimatedTransitMinutes = 0,
                totalEstimatedMinutes = 15,
                success = true,
                message = "Listo para recoger en tienda"
            )
        } else if (municipality == "JARAL_PROGRESO") {
            return DeliveryDetails(
                distanceKm = 0.0,
                deliveryFee = 0.0,
                estimatedPrepMinutes = 15,
                estimatedTransitMinutes = 10,
                totalEstimatedMinutes = 25,
                success = true,
                message = "Envío disponible a Jaral del Progreso"
            )
        }

        val dest = getDestinationCoordinates(municipality)
        val km = calculateHaversineDistance(latStore, lngStore, dest.first, dest.second)
        
        // Fee calculation: base fee + $2.5 per km
        val baseFee = if (municipality == "VALLE_SANTIAGO") 35.0 else 45.0
        val fee = baseFee + (km * 1.2)
        
        // Transit time: ~1.5 minute per km + constant prep time (~15 mins)
        val transit = (km * 1.6).roundToInt().coerceAtLeast(10)
        val prep = 15

        return DeliveryDetails(
            distanceKm = (km * 10).roundToInt() / 10.0,
            deliveryFee = (fee / 5).roundToInt() * 5.0, // Redondear a múltiplos de 5
            estimatedPrepMinutes = prep,
            estimatedTransitMinutes = transit,
            totalEstimatedMinutes = prep + transit,
            success = true,
            message = "Envío disponible a ${municipality.replace("_", " ")}"
        )
    }

    // 4. Order Operations
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getOrderById(orderId: String): Flow<OrderEntity?> = orderDao.getOrderById(orderId)

    suspend fun createOrder(
        customerName: String,
        phone: String,
        deliveryMethod: String,
        municipality: String,
        address: String,
        paymentMethod: String,
        cashPayWith: Double,
        cartItems: List<CartItemEntity>
    ): OrderEntity = withContext(Dispatchers.IO) {
        val details = calculateDeliveryDetails(if (deliveryMethod == "RECOGER") "RECOGER" else municipality)
        
        val orderId = "#AKK-${(1000..9999).random()}"
        
        // Convert items to string list for simplified storage
        val itemsString = cartItems.joinToString(separator = "; ") { item ->
            "${item.productName} [${item.sauce}] (x${item.quantity}) - $${item.price * item.quantity}"
        }

        val dest = if (deliveryMethod == "RECOGER") Pair(latStore, lngStore) else getDestinationCoordinates(municipality)

        val newOrder = OrderEntity(
            orderId = orderId,
            itemsJson = itemsString,
            customerName = customerName,
            phone = phone,
            deliveryMethod = deliveryMethod,
            municipality = municipality,
            address = address,
            paymentMethod = paymentMethod,
            cashPayWith = cashPayWith,
            status = "RECIBIDO",
            timestamp = System.currentTimeMillis(),
            deliveryFee = details.deliveryFee,
            distanceKm = details.distanceKm,
            estimatedTimeMinutes = details.totalEstimatedMinutes,
            currentCourierLat = latStore,
            currentCourierLng = lngStore,
            destinationLat = dest.first,
            destinationLng = dest.second
        )

        orderDao.insertOrder(newOrder)
        newOrder
    }

    suspend fun updateOrderStatus(orderId: String, status: String) = withContext(Dispatchers.IO) {
        val orderFlow = getOrderById(orderId)
        val currentOrder = orderFlow.first()
        if (currentOrder != null) {
            orderDao.insertOrder(currentOrder.copy(status = status))
        }
    }

    suspend fun updateCourierLocation(orderId: String, lat: Double, lng: Double) = withContext(Dispatchers.IO) {
        val orderFlow = getOrderById(orderId)
        val currentOrder = orderFlow.first()
        if (currentOrder != null) {
            orderDao.insertOrder(currentOrder.copy(currentCourierLat = lat, currentCourierLng = lng))
        }
    }

    suspend fun updateOrderCourierStatus(orderId: String, status: String, lat: Double, lng: Double) = withContext(Dispatchers.IO) {
        orderDao.updateOrderCourierStatus(orderId, status, lat, lng)
    }

    suspend fun getUnsyncedOrders(): List<OrderEntity> = withContext(Dispatchers.IO) {
        orderDao.getUnsyncedOrders()
    }

    suspend fun updateOrderSyncStatus(orderId: String, isSynced: Boolean) = withContext(Dispatchers.IO) {
        orderDao.updateOrderSyncStatus(orderId, isSynced)
    }

    suspend fun fetchRemoteProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            val dataSource = SupabaseMenuDataSource()
            val result = dataSource.getProducts()
            if (result.isSuccess) {
                val remoteList = result.getOrNull()
                if (remoteList != null && remoteList.isNotEmpty()) {
                    return@withContext remoteList
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        menuProducts
    }
}

data class DeliveryDetails(
    val distanceKm: Double,
    val deliveryFee: Double,
    val estimatedPrepMinutes: Int,
    val estimatedTransitMinutes: Int,
    val totalEstimatedMinutes: Int,
    val success: Boolean,
    val message: String
)
