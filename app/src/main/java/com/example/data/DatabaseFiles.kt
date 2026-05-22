package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ----------------------------------------------------
// ENTITIES
// ----------------------------------------------------

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val sauce: String, // "BBQ", "Buffalo", "Mango Habanero", "Lemon Pepper", "Natural"
    val note: String = ""
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String, // e.g. #AKK-1234
    val itemsJson: String, // String representation of ordered items
    val customerName: String,
    val phone: String,
    val deliveryMethod: String, // "ENVIO" or "RECOGER"
    val municipality: String, // "VALLE_SANTIAGO", "CORTAZAR", "JARAL_PROGRESO"
    val address: String,
    val paymentMethod: String, // "EFECTIVO" or "TRANSFERENCIA"
    val cashPayWith: Double = 0.0,
    val status: String, // "RECIBIDO", "PREPARANDO", "LISTO", "EN_CAMINO", "ENTREGADO"
    val timestamp: Long,
    val deliveryFee: Double,
    val distanceKm: Double,
    val estimatedTimeMinutes: Int,
    val currentCourierLat: Double,
    val currentCourierLng: Double,
    val destinationLat: Double,
    val destinationLng: Double
)

// ----------------------------------------------------
// DAOS
// ----------------------------------------------------

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(cartItem: CartItemEntity)

    @Update
    suspend fun updateItem(cartItem: CartItemEntity)

    @Delete
    suspend fun deleteItem(cartItem: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :productId AND sauce = :sauce")
    suspend fun deleteItemPreCheck(productId: String, sauce: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status, currentCourierLat = :lat, currentCourierLng = :lng WHERE orderId = :orderId")
    suspend fun updateOrderCourierStatus(orderId: String, status: String, lat: Double, lng: Double)
}

// ----------------------------------------------------
// CONVERTER OR HELPER
// ----------------------------------------------------

@Database(entities = [CartItemEntity::class, OrderEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
}
