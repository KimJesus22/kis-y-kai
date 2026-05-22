package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlin.math.roundToInt

// ----------------------------------------------------
// UI STATES AND NOTIFICATIONS
// ----------------------------------------------------

data class SimulatedNotification(
    val title: String,
    val text: String,
    val time: String,
    val isRead: Boolean = false
)

class FoodViewModel(
    private val repository: FoodRepository,
    private val appContext: Context
) : ViewModel() {

    // Product list (static catalog)
    val products = repository.menuProducts

    // Shopping Cart reactive stream
    val cartItems = repository.cartItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All historical orders reactive stream
    val allOrders = repository.allOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Form states
    var customerName = MutableStateFlow("")
        private set
    var customerPhone = MutableStateFlow("")
        private set
    var deliveryMethod = MutableStateFlow("ENVIO") // "ENVIO" or "RECOGER"
        private set
    var selectedMunicipality = MutableStateFlow("VALLE_SANTIAGO") // "VALLE_SANTIAGO" or "CORTAZAR"
        private set
    var deliveryAddress = MutableStateFlow("")
        private set
    var paymentMethod = MutableStateFlow("EFECTIVO") // "EFECTIVO" or "TRANSFERENCIA"
        private set
    var cashPayWith = MutableStateFlow("")
        private set

    // Active order being tracked
    var activeOrderId = MutableStateFlow<String?>(null)
        private set

    // Real-time Driver GPS active state
    var isRiderGpsActive = MutableStateFlow(false)
        private set

    // Notification Feed (Simulates push delivery states)
    private val _notifications = MutableStateFlow<List<SimulatedNotification>>(emptyList())
    val notifications: StateFlow<List<SimulatedNotification>> = _notifications.asStateFlow()

    // Active tracking order flow
    val activeOrder: StateFlow<OrderEntity?> = activeOrderId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repository.getOrderById(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Live calculations for shipping total
    val deliveryFeeAndDetails = combine(deliveryMethod, selectedMunicipality) { method, muni ->
        if (method == "RECOGER") {
            repository.calculateDeliveryDetails("RECOGER")
        } else {
            repository.calculateDeliveryDetails(muni)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.calculateDeliveryDetails("VALLE_SANTIAGO")
    )

    // Item Selection States
    val selectedItemQuantity = MutableStateFlow(1)
    val selectedItemSauce = MutableStateFlow("BBQ")
    val selectedItemNote = MutableStateFlow("")

    private var trackingSimulationJob: Job? = null

    // ----------------------------------------------------
    // ACTIONS
    // ----------------------------------------------------

    fun setCustomerName(v: String) { customerName.value = v }
    fun setCustomerPhone(v: String) { customerPhone.value = v }
    fun setDeliveryMethod(v: String) { 
        deliveryMethod.value = v 
        if (v == "RECOGER") {
            deliveryAddress.value = "Pasar a recoger en Tienda Central - Jaral del Progreso, Gto"
        } else {
            deliveryAddress.value = ""
        }
    }
    fun setSelectedMunicipality(v: String) { selectedMunicipality.value = v }
    fun setDeliveryAddress(v: String) { deliveryAddress.value = v }
    fun setPaymentMethod(v: String) { paymentMethod.value = v }
    fun setCashPayWith(v: String) { cashPayWith.value = v }

    fun addNotification(title: String, text: String) {
        val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val timeStr = formatter.format(java.util.Date())
        val list = _notifications.value.toMutableList()
        list.add(0, SimulatedNotification(title, text, timeStr))
        _notifications.value = list

        // Deliver actual high-priority system status notification acting as a push notice
        NotificationHelper.showNotification(appContext, title, text)
    }

    suspend fun addToCart(product: Product) {
        val qty = selectedItemQuantity.value
        val sauce = if (product.hasSauces) selectedItemSauce.value else "Natural"
        val note = selectedItemNote.value
        repository.addToCart(product, qty, sauce, note)
        
        // Reset selections
        selectedItemQuantity.value = 1
        selectedItemSauce.value = "BBQ"
        selectedItemNote.value = ""
    }

    suspend fun removeFromCart(item: CartItemEntity) {
        repository.removeCartItem(item)
    }

    suspend fun clearCart() {
        repository.clearCart()
    }

    fun repeatOrder(order: OrderEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            // Clear current cart items first for a clean repeat
            repository.clearCart()
            
            val text = order.itemsJson
            if (text.isNotBlank()) {
                val itemStrings = text.split("; ")
                for (itemStr in itemStrings) {
                    if (itemStr.isBlank()) continue
                    try {
                        val parts = itemStr.split(" [", limit = 2)
                        if (parts.size >= 2) {
                            val productName = parts[0].trim()
                            val rest = parts[1]
                            
                            val sauce = rest.substringBefore("]").trim()
                            val quantityPart = rest.substringAfter("(x", "").substringBefore(")")
                            val quantity = quantityPart.toIntOrNull() ?: 1
                            
                            val product = repository.menuProducts.find {
                                it.name.trim().equals(productName, ignoreCase = true)
                            }
                            if (product != null) {
                                repository.addToCart(product, quantity, sauce, "")
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            addNotification(
                title = "Pedido Repetido 🔄",
                text = "Se han agregado al carrito los productos del pedido ${order.orderId}."
            )
            onComplete()
        }
    }

    private var isCheckingOut = false

    fun checkout(onComplete: (String) -> Unit) {
        if (isCheckingOut) return
        isCheckingOut = true
        viewModelScope.launch {
            try {
                val cartList = cartItems.value
                if (cartList.isEmpty()) {
                    isCheckingOut = false
                    return@launch
                }

                val payWith = cashPayWith.value.toDoubleOrNull() ?: 0.0
                
                val order = repository.createOrder(
                    customerName = customerName.value.ifBlank { "Cliente Especial" },
                    phone = customerPhone.value.ifBlank { "411-XXXXXXX" },
                    deliveryMethod = deliveryMethod.value,
                    municipality = if (deliveryMethod.value == "RECOGER") "JARAL_PROGRESO" else selectedMunicipality.value,
                    address = deliveryAddress.value.ifBlank { "Dirección Conocida" },
                    paymentMethod = paymentMethod.value,
                    cashPayWith = payWith,
                    cartItems = cartList
                )

                activeOrderId.value = order.orderId
                
                // Push Notification
                addNotification(
                    title = "Pedido Recibido 📝",
                    text = "¡Alitas Kis y Kei ha recibido tu orden ${order.orderId}! Iniciamos la preparación."
                )

                // Clear Cart
                repository.clearCart()

                // Start life cycle simulation
                startTrackingSimulation(order.orderId)

                withContext(Dispatchers.Main) {
                    onComplete(order.orderId)
                }
            } finally {
                isCheckingOut = false
            }
        }
    }

    // ----------------------------------------------------
    // REPARTIDOR TRACKING SIMULATION ENGINE
    // ----------------------------------------------------

    fun forceStateAdvance() {
        // Fast forward the courier simulation
        val order = activeOrder.value ?: return
        viewModelScope.launch {
            val nextStatus = when (order.status) {
                "RECIBIDO" -> "PREPARANDO"
                "PREPARANDO" -> "LISTO"
                "LISTO" -> {
                    if (order.deliveryMethod == "RECOGER") "ENTREGADO" else "EN_CAMINO"
                }
                "EN_CAMINO" -> "ENTREGADO"
                else -> "RECIBIDO"
            }
            advanceSimulationTo(order.orderId, nextStatus)
        }
    }

    private suspend fun advanceSimulationTo(orderId: String, nextStatus: String) {
        val order = repository.getOrderById(orderId).first() ?: return
        val storeCoords = Pair(repository.latStore, repository.lngStore)
        val destCoords = Pair(order.destinationLat, order.destinationLng)

        when (nextStatus) {
            "PREPARANDO" -> {
                repository.updateOrderStatus(orderId, "PREPARANDO")
                addNotification(
                    title = "En Cocina 👨‍🍳",
                    text = "Tus alitas y boneless están siendo fritos y bañados en salsa. ¡Huele delicioso!"
                )
            }
            "LISTO" -> {
                repository.updateOrderStatus(orderId, "LISTO")
                if (order.deliveryMethod == "RECOGER") {
                    addNotification(
                        title = "¡Listo para Recoger! 🛍️",
                        text = "Tu orden de Kis y Kei está calientita en mostrador. ¡Pasa por ella!"
                    )
                } else {
                    addNotification(
                        title = "¡Pedido Empacado! 📦",
                        text = "Tu comida ha sido empacada herméticamente y asignada al repartidor."
                    )
                }
            }
            "EN_CAMINO" -> {
                repository.updateOrderStatus(orderId, "EN_CAMINO")
                addNotification(
                    title = "¡Repartidor en Camino! 🛵",
                    text = "El repartidor ha salido de Jaral del Progreso rumbo a tu domicilio. Síguelo en vivo."
                )
            }
            "ENTREGADO" -> {
                repository.updateOrderCourierStatus(
                    orderId, "ENTREGADO",
                    destCoords.first, destCoords.second
                )
                addNotification(
                    title = "¡Entregado! 🎉",
                    text = "¡Buen provecho! Muchas gracias por comprar en Alitas Kis y Kei. Regresa pronto."
                )
            }
        }
    }

    private fun startTrackingSimulation(orderId: String) {
        trackingSimulationJob?.cancel()
        trackingSimulationJob = viewModelScope.launch {
            // Stage 1: Received -> Prep (10s)
            delay(10000)
            advanceSimulationTo(orderId, "PREPARANDO")

            // Stage 2: Prep -> Ready (12s)
            delay(12000)
            advanceSimulationTo(orderId, "LISTO")

            // Stage 3: Ready -> En camino (6s)
            delay(6000)
            val order = repository.getOrderById(orderId).first() ?: return@launch
            if (order.deliveryMethod == "RECOGER") {
                // Pick up stays in LISTO until pick-up complete
                delay(12000)
                advanceSimulationTo(orderId, "ENTREGADO")
                return@launch
            }

            advanceSimulationTo(orderId, "EN_CAMINO")

            // Stage 4: En camino movement loop (6 Steps from Store to Destination)
            val startLat = repository.latStore
            val startLng = repository.lngStore
            val endLat = order.destinationLat
            val endLng = order.destinationLng
            val steps = 8

            for (i in 1..steps) {
                delay(5000) // update every 5 seconds
                val fraction = i.toDouble() / steps
                val currentLat = startLat + (endLat - startLat) * fraction
                val currentLng = startLng + (endLng - startLng) * fraction

                val updatedStatus = if (i == steps) "ENTREGADO" else "EN_CAMINO"
                repository.updateOrderCourierStatus(orderId, updatedStatus, currentLat, currentLng)

                if (i == steps) {
                    addNotification(
                        title = "¡Llegó el Repartidor! 🛵",
                        text = "El repartidor está afuera de tu ubicación. ¡Sal a recibir tus alitas!"
                    )
                    delay(3000)
                    advanceSimulationTo(orderId, "ENTREGADO")
                } else {
                    val remainingFraction = 1.0 - fraction
                    val totalDist = order.distanceKm
                    val currentRemainingDist = (totalDist * remainingFraction * 10).roundToInt() / 10.0
                    val currentRemainingTime = (order.estimatedTimeMinutes * remainingFraction).roundToInt().coerceAtLeast(1)
                    
                    addNotification(
                        title = "Repartidor en Tránsito 🏁",
                        text = "El repartidor se encuentra a aprox. $currentRemainingDist km de tu domicilio. Clima despejado."
                    )
                }
            }
        }
    }

    private var locationListener: android.location.LocationListener? = null

    fun toggleRiderGps(active: Boolean) {
        isRiderGpsActive.value = active
        if (active) {
            startRiderGpsTracking()
        } else {
            stopRiderGpsTracking()
        }
    }

    private fun startRiderGpsTracking() {
        val orderId = activeOrderId.value ?: return
        // Cancel standard timeline artificial tracking job since we are manually delivering via real GPS coordinate injection!
        trackingSimulationJob?.cancel()

        // Set state to EN_CAMINO if it is currently in a preparatory state
        viewModelScope.launch {
            val order = repository.getOrderById(orderId).first()
            if (order != null && (order.status == "RECIBIDO" || order.status == "PREPARANDO" || order.status == "LISTO")) {
                advanceSimulationTo(orderId, "EN_CAMINO")
            }
        }

        val locManager = appContext.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager ?: return
        
        // Ensure standard updates are cleaned before initiating new tracking
        stopRiderGpsTracking()

        val listener = object : android.location.LocationListener {
            override fun onLocationChanged(location: android.location.Location) {
                viewModelScope.launch {
                    val currentId = activeOrderId.value
                    if (currentId != null) {
                        repository.updateCourierLocation(currentId, location.latitude, location.longitude)
                        
                        // Check if we are extremely close (less than 100 meters) to the delivery destination
                        val destLoc = repository.getOrderById(currentId).first()
                        if (destLoc != null) {
                            val dist = repository.calculateHaversineDistance(
                                location.latitude, location.longitude,
                                destLoc.destinationLat, destLoc.destinationLng
                            )
                            if (dist < 0.1) { 
                                advanceSimulationTo(currentId, "ENTREGADO")
                                stopRiderGpsTracking()
                                isRiderGpsActive.value = false
                            }
                        }
                    }
                }
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        
        locationListener = listener
        
        try {
            if (locManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                locManager.requestLocationUpdates(
                    android.location.LocationManager.GPS_PROVIDER,
                    2000L,
                    1f,
                    listener,
                    android.os.Looper.getMainLooper()
                )
            }
            if (locManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                locManager.requestLocationUpdates(
                    android.location.LocationManager.NETWORK_PROVIDER,
                    2000L,
                    1f,
                    listener,
                    android.os.Looper.getMainLooper()
                )
            }
            
            // Pop immediate fix if location is already locked
            val lastGps = locManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
            val lastNet = locManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            val bestFix = lastGps ?: lastNet
            if (bestFix != null) {
                viewModelScope.launch {
                    repository.updateCourierLocation(orderId, bestFix.latitude, bestFix.longitude)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRiderGpsTracking() {
        val locManager = appContext.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
        val listener = locationListener
        if (locManager != null && listener != null) {
            try {
                locManager.removeUpdates(listener)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        locationListener = null
    }

    override fun onCleared() {
        trackingSimulationJob?.cancel()
        stopRiderGpsTracking()
        super.onCleared()
    }
}

// ----------------------------------------------------
// FACTORY
// ----------------------------------------------------

class FoodViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "kis_kei_db"
        ).fallbackToDestructiveMigration()
            .build()
            
        val repository = FoodRepository(database.cartDao(), database.orderDao())
        
        @Suppress("UNCHECKED_CAST")
        return FoodViewModel(repository, context.applicationContext) as T
    }
}
