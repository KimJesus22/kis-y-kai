package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CartItemEntity
import com.example.ui.FoodViewModel
import com.example.ui.components.EmptyStateContent
import com.example.ui.theme.*
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

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val subtotal = cartList.sumOf { it.price * it.quantity }
    val total = subtotal + deliveryDetails.deliveryFee

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Elegant, Clean Top Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp).testTag("back_button_cart")
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Mi Carrito",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (cartList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateContent(
                        title = "Tu carrito está vacío",
                        subtitle = "Agrega unas alitas, boneless o papas para empezar tu pedido.",
                        buttonText = "Ver menú",
                        icon = Icons.Default.RemoveShoppingCart,
                        onButtonClick = onNavigateBack,
                        testTag = "empty_cart_state"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("cart_items_scroll"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Item Title Section
                    item {
                        Text(
                            text = "RESUMEN DE COMIDA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Cart Items list
                    items(cartList) { item ->
                        CartListItemRow(
                            item = item,
                            onRemove = {
                                scope.launch { viewModel.removeFromCart(item) }
                            }
                        )
                    }

                    // Separation Line & Next Header
                    item {
                        Divider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "CONFIGURACIÓN DE ENTREGA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Delivery Method Selection (Delivery vs Pickup)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val activeBorderOption1 = if (deliveryMethod == "ENVIO") MaterialTheme.colorScheme.primary else Color.Transparent
                            val activeBorderOption2 = if (deliveryMethod == "RECOGER") MaterialTheme.colorScheme.primary else Color.Transparent

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(elevation = if (deliveryMethod == "ENVIO") 3.dp else 1.dp, shape = RoundedCornerShape(24.dp))
                                    .border(
                                        2.dp,
                                        activeBorderOption1,
                                        RoundedCornerShape(24.dp)
                                    )
                                    .clickable { viewModel.setDeliveryMethod("ENVIO") }
                                    .testTag("method_envio"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (deliveryMethod == "ENVIO") MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (deliveryMethod == "ENVIO") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeliveryDining,
                                            contentDescription = "Delivery",
                                            tint = if (deliveryMethod == "ENVIO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Envío a Domicilio",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (deliveryMethod == "ENVIO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Valle o Cortazar",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(elevation = if (deliveryMethod == "RECOGER") 3.dp else 1.dp, shape = RoundedCornerShape(24.dp))
                                    .border(
                                        2.dp,
                                        activeBorderOption2,
                                        RoundedCornerShape(24.dp)
                                    )
                                    .clickable { viewModel.setDeliveryMethod("RECOGER") }
                                    .testTag("method_recoger"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (deliveryMethod == "RECOGER") MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (deliveryMethod == "RECOGER") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = "Pickup",
                                            tint = if (deliveryMethod == "RECOGER") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Pasar a Recoger",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (deliveryMethod == "RECOGER") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Sin cargo extra",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Contact Details Name & Phone - Styled in White Card with Large radiuses
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(26.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(26.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = "Contact",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Datos de contacto",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { viewModel.setCustomerName(it) },
                                    label = { Text("Nombre Completo") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_customer_name"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    ),
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = "person", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                    }
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { viewModel.setCustomerPhone(it) },
                                    label = { Text("Teléfono de contacto / WhatsApp") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_customer_phone"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    ),
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Phone, contentDescription = "phone", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                    }
                                )
                            }
                        }
                    }

                    // Shipping details Panel (Address, select municipality)
                    if (deliveryMethod == "ENVIO") {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(26.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(26.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(26.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "Municipio de Entrega:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Strictly restricted Valle vs Cortazar selector row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        listOf("VALLE_SANTIAGO", "CORTAZAR").forEach { muni ->
                                            val isSelected = municipality == muni
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                        RoundedCornerShape(14.dp)
                                                    )
                                                    .clickable { viewModel.setSelectedMunicipality(muni) }
                                                    .padding(vertical = 14.dp)
                                                    .testTag("muni_select_$muni"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (muni == "VALLE_SANTIAGO") "Valle de Santiago" else "Cortazar",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))
                                    Text(
                                        text = "Dirección de Entrega Completa:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = address,
                                        onValueChange = { viewModel.setDeliveryAddress(it) },
                                        placeholder = { Text("Calle, Número, Colonia, Referencias...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(86.dp)
                                            .testTag("input_address"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                        )
                                    )

                                    // Dynamic Maps distance indicators
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PinDrop,
                                                    contentDescription = "Distancia",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Cálculo de Distancia (Ruta Física)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Jaral a ${if (municipality == "VALLE_SANTIAGO") "Valle" else "Cortazar"}: ~${deliveryDetails.distanceKm} km",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Envío aprox: ${deliveryDetails.estimatedTransitMinutes} mins (+15 min cocina)",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Pick-up store details - Local Store Jaral details info
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(26.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(26.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(26.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "store",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Dirección de Tienda Principal",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Jaral del Progreso, Guanajuato. Local Principal (Te daremos indicaciones personalizadas al estar listo tu pedido).",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tiempo medio de preparación: ~15 minutos",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Separation Line & Payment Method Section
                    item {
                        Divider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "MÉTODO DE PAGO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Payment Method Panel - Styled beautifully in Stitch theme
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(26.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(26.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    val activePayOption1Border = if (paymentMethod == "EFECTIVO") MaterialTheme.colorScheme.primary else Color.Transparent
                                    val activePayOption2Border = if (paymentMethod == "TRANSFERENCIA") MaterialTheme.colorScheme.primary else Color.Transparent

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .shadow(elevation = if (paymentMethod == "EFECTIVO") 2.dp else 0.dp, shape = RoundedCornerShape(20.dp))
                                            .border(
                                                2.dp,
                                                activePayOption1Border,
                                                RoundedCornerShape(20.dp)
                                            )
                                            .clickable { viewModel.setPaymentMethod("EFECTIVO") }
                                            .testTag("pay_cash"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (paymentMethod == "EFECTIVO") MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.background
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = "Cash",
                                                tint = if (paymentMethod == "EFECTIVO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Efectivo\nContra Entrega",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = if (paymentMethod == "EFECTIVO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .shadow(elevation = if (paymentMethod == "TRANSFERENCIA") 2.dp else 0.dp, shape = RoundedCornerShape(20.dp))
                                            .border(
                                                2.dp,
                                                activePayOption2Border,
                                                RoundedCornerShape(20.dp)
                                            )
                                            .clickable { viewModel.setPaymentMethod("TRANSFERENCIA") }
                                            .testTag("pay_transfer"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (paymentMethod == "TRANSFERENCIA") MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.background
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalance,
                                                contentDescription = "Transfer",
                                                tint = if (paymentMethod == "TRANSFERENCIA") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Transferencia\nBancaria",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = if (paymentMethod == "TRANSFERENCIA") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                if (paymentMethod == "EFECTIVO") {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "¿Con cuánto billete pagarás? (Cambio repartidor):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = cashPayWith,
                                        onValueChange = { viewModel.setCashPayWith(it) },
                                        placeholder = { Text("Ej. Con un billete de $200 o $500", fontSize = 12.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_cash_with"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                        )
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "Info",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Datos para Transferencia BBVA",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Banco: BBVA Bancomer\nBeneficiario: Martín Mendoza K.\nCLABE: 0121 8001 2345 6789 01\nConcepto: Tu nombre y Alitas",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 16.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "*Nota: Al finalizar el pedido deberás enviar el comprobante en WhatsApp directamente a Ulices.",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Pricing Totals Block destacado en una card inferior premium
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(26.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(26.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Subtotal alimentos", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "$${subtotal.toInt()} MXN", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (deliveryMethod == "RECOGER") "Cargo por entrega" else "Cargo de envío (${if (municipality == "VALLE_SANTIAGO") "Valle" else "Cortazar"})",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (deliveryMethod == "RECOGER") "Gratis" else "$${deliveryDetails.deliveryFee.toInt()} MXN",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (deliveryMethod == "RECOGER") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Divider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TOTAL ESTIMADO",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$${total.toInt()} MXN",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Stick checkout button - Pill shaped CTA
        if (cartList.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                val isAddressValid = deliveryMethod == "RECOGER" || address.isNotBlank()
                Button(
                    onClick = {
                        viewModel.checkout(onComplete = { orderId ->
                            onNavigateToTracking()
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("checkout_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAddressValid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    shape = CircleShape,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    enabled = isAddressValid
                ) {
                    Text(
                        text = "Confirmar Pedido • $${total.toInt()} MXN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CartListItemRow(
    item: CartItemEntity,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (item.productId) {
                        "alitas_papas" -> "🪶"
                        "boneless" -> "🍗"
                        "boneless_papas" -> "🍗"
                        "papas_orden" -> "🍟"
                        else -> "🍽️"
                    },
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Salsa: ${item.sauce}  •  Cant: x${item.quantity}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Nota: ${item.note}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = "$${(item.price * item.quantity).toInt()}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onRemove() }
                    .testTag("remove_item_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
