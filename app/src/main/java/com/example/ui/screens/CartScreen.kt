package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CartItemEntity
import com.example.ui.FoodViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: FoodViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartList by viewModel.cartItems.collectAsState()
    val name by viewModel.customerName.collectAsState()
    val phone by viewModel.customerPhone.collectAsState()
    val deliveryMethod by viewModel.deliveryMethod.collectAsState()
    val municipality by viewModel.selectedMunicipality.collectAsState()
    val address by viewModel.deliveryAddress.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val cashPayWith by viewModel.cashPayWith.collectAsState()
    val deliveryDetails by viewModel.deliveryFeeAndDetails.collectAsState()

    val scheduledMethod by viewModel.scheduledMethod.collectAsState()
    val scheduledDelay by viewModel.scheduledDelay.collectAsState()
    val scheduledNote by viewModel.scheduledNote.collectAsState()

    val tipMethod by viewModel.tipMethod.collectAsState()
    val customTipValue by viewModel.customTipValue.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var attemptedSubmit by remember { mutableStateOf(false) }

    val subtotal = cartList.sumOf { it.price * it.quantity }
    val tipAmount = when (tipMethod) {
        "10" -> 10.0
        "15" -> 15.0
        "20" -> 20.0
        "CUSTOM" -> customTipValue.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
    val total = subtotal + deliveryDetails.deliveryFee + tipAmount

    val isEnvio = deliveryMethod == "ENVIO"
    val isRecoger = deliveryMethod == "RECOGER"
    val isCash = paymentMethod == "EFECTIVO"
    val isTransfer = paymentMethod == "TRANSFERENCIA"

    val creamBg = Color(0xFFFCF9F8)
    val textPrimary = Color(0xFF111111)
    val textSecondary = Color(0xFF6B5F5A)
    val primaryOrange = Color(0xFFFF6D00)
    val orangeDarkText = Color(0xFFC94D00)
    val borderSoft = Color(0xFFE8DED5)

    // Lógica de recomendación simple
    val recommendedProduct = remember(cartList) {
        val hasAlitas = cartList.any { it.productId == "alitas_papas" }
        val hasBoneless = cartList.any { it.productId == "boneless" || it.productId == "boneless_papas" }
        val hasPapas = cartList.any { it.productId == "papas_orden" }

        when {
            cartList.isEmpty() -> viewModel.products.find { it.id == "alitas_papas" }
            hasBoneless && !hasPapas -> viewModel.products.find { it.id == "papas_orden" }
            hasAlitas && !hasBoneless -> viewModel.products.find { it.id == "boneless" }
            cartList.size >= 2 && !hasPapas -> viewModel.products.find { it.id == "papas_orden" }
            !hasPapas -> viewModel.products.find { it.id == "papas_orden" }
            else -> viewModel.products.find { it.id == "alitas_papas" } // Opción por defecto genérica
        }
    }

    var showCustomizerForProduct by remember { mutableStateOf<com.example.data.Product?>(null) }

    showCustomizerForProduct?.let { product ->
        FoodCustomizerDialog(
            product = product,
            viewModel = viewModel,
            onDismiss = { showCustomizerForProduct = null },
            onAddConfirmed = { showCustomizerForProduct = null }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(creamBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Navegación superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.background(Color.White, CircleShape).shadow(1.dp, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Atrás", tint = textPrimary)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Mi Carrito",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = textPrimary
                    )
                }
            }

            if (cartList.isEmpty()) {
                // ESTADO VACÍO PROFESIONAL
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = Color(0xFFE0E0E0)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Tu carrito está vacío", fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("¡Agrega algo delicioso para comenzar!", color = Color.Gray, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryOrange),
                        shape = CircleShape,
                        modifier = Modifier.height(50.dp).padding(horizontal = 32.dp)
                    ) {
                        Text("Ver menú", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. RESUMEN DE COMIDA
                    item { SectionHeader(Icons.Default.Fastfood, "RESUMEN DE COMIDA") }
                    items(cartList) { item ->
                        CartListItemRow(item = item, onRemove = { scope.launch { viewModel.removeFromCart(item) } })
                    }

                    // 2. CONFIGURACIÓN DE ENTREGA
                    item { SectionHeader(Icons.Default.DeliveryDining, "CONFIGURACIÓN DE ENTREGA") }
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    DeliveryMethodChip("Envío", isEnvio, onClick = { viewModel.setDeliveryMethod("ENVIO") }, modifier = Modifier.weight(1f))
                                    DeliveryMethodChip("Recoger", isRecoger, onClick = { viewModel.setDeliveryMethod("RECOGER") }, modifier = Modifier.weight(1f))
                                }
                                
                                if (isEnvio) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Municipio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val munis = listOf("VALLE_SANTIAGO", "CORTAZAR", "JARAL_PROGRESO")
                                        munis.forEach { m ->
                                            FilterChip(
                                                selected = municipality == m,
                                                onClick = { viewModel.setSelectedMunicipality(m) },
                                                label = { Text(m.replace("_", " ")) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryOrange.copy(0.1f), selectedLabelColor = primaryOrange)
                                            )
                                        }
                                    }
                                    
                                    OutlinedTextField(
                                        value = address,
                                        onValueChange = { if (it.length <= 200) viewModel.setDeliveryAddress(it) },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Dirección completa") },
                                        isError = attemptedSubmit && address.isBlank(),
                                        placeholder = { Text("Calle, número, colonia...") },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    if (attemptedSubmit && address.isBlank()) {
                                        Text("La dirección es obligatoria para envío", color = Color.Red, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 3. DATOS DE CONTACTO
                    item { SectionHeader(Icons.Default.Person, "DATOS DE CONTACTO") }
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { viewModel.setCustomerName(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Nombre completo") },
                                    isError = attemptedSubmit && name.isBlank(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                if (attemptedSubmit && name.isBlank()) {
                                    Text("El nombre es obligatorio", color = Color.Red, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { viewModel.setCustomerPhone(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Teléfono / WhatsApp") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    isError = attemptedSubmit && phone.isBlank(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                if (attemptedSubmit && phone.isBlank()) {
                                    Text("El teléfono es obligatorio", color = Color.Red, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // 4. MÉTODO DE PAGO
                    item { SectionHeader(Icons.Default.Payments, "MÉTODO DE PAGO") }
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    PaymentMethodChip("Efectivo", isCash, onClick = { viewModel.setPaymentMethod("EFECTIVO") }, modifier = Modifier.weight(1f))
                                    PaymentMethodChip("Transferencia", isTransfer, onClick = { viewModel.setPaymentMethod("TRANSFERENCIA") }, modifier = Modifier.weight(1f))
                                }
                                
                                if (isCash) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedTextField(
                                        value = cashPayWith,
                                        onValueChange = { viewModel.setCashPayWith(it) },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("¿Con cuánto pagas? (Opcional)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        placeholder = { Text("Ej. 500") }
                                    )
                                    if (cashPayWith.isBlank()) {
                                        Text("Si indicas el monto, el repartidor llevará cambio.", color = orangeDarkText, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }
                            }
                        }
                    }

                    // 5. RESUMEN TOTAL
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SummaryRow("Subtotal alimentos", "$${subtotal.toInt()}")
                                SummaryRow("Costo de envío", if (deliveryDetails.deliveryFee > 0) "$${deliveryDetails.deliveryFee.toInt()}" else "Gratis")
                                if (tipAmount > 0) SummaryRow("Propina", "$${tipAmount.toInt()}")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TOTAL", fontWeight = FontWeight.Black, fontSize = 18.sp)
                                    Text("$${total.toInt()} MXN", fontWeight = FontWeight.Black, fontSize = 18.sp, color = primaryOrange)
                                }
                            }
                        }
                    }
                }
            }
        }

        // BOTÓN PRINCIPAL FIJO
        if (cartList.isNotEmpty()) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Box(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                    Button(
                        onClick = {
                            attemptedSubmit = true
                            val isValid = name.isNotBlank() && phone.isNotBlank() && (!isEnvio || address.isNotBlank())
                            if (isValid) {
                                viewModel.checkout { orderId, isSynced ->
                                    onNavigateToTracking()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryOrange),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Confirmar Pedido • $${total.toInt()} MXN",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun DeliveryMethodChip(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(44.dp).clip(RoundedCornerShape(12.dp)).clickable { onClick() },
        color = if (isSelected) Color(0xFFFF6D00) else Color(0xFFF5F5F5),
        shape = RoundedCornerShape(12.dp),
        border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Black, fontSize = 14.sp)
        }
    }
}

@Composable
fun PaymentMethodChip(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(44.dp).clip(RoundedCornerShape(12.dp)).clickable { onClick() },
        color = if (isSelected) Color(0xFFFF6D00) else Color(0xFFF5F5F5),
        shape = RoundedCornerShape(12.dp),
        border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Black, fontSize = 14.sp)
        }
    }
}

@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
        Icon(icon, null, tint = Color(0xFFFF6D00), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray, letterSpacing = 1.sp)
    }
}

@Composable
fun CartListItemRow(item: CartItemEntity, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = when (item.productId) {
                    "alitas_papas" -> "🍗🍟"
                    "boneless" -> "🍗"
                    "boneless_papas" -> "🍖🍟"
                    "papas_orden" -> "🍟"
                    else -> "🍽️"
                }, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("${item.sauce} x${item.quantity}", color = Color.Gray, fontSize = 13.sp)
            }
            Text("$${(item.price * item.quantity).toInt()}", fontWeight = FontWeight.Black, color = Color(0xFFFF6D00))
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, null, tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun RecommendationCard(recommendedProduct: com.example.data.Product, onClickAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7F2)),
        border = BorderStroke(1.dp, Color(0xFFFFD1B3)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("¿Algo más?", fontWeight = FontWeight.Black, color = Color(0xFFC94D00))
                Text(recommendedProduct.name, fontSize = 14.sp)
            }
            Button(
                onClick = onClickAdd,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00)),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("Agregar +", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
