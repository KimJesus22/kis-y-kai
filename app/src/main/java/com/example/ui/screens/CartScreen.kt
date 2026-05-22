package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val subtotal = cartList.sumOf { it.price * it.quantity }
    val total = subtotal + deliveryDetails.deliveryFee

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

    // Logica de recomendacion simple
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
            else -> viewModel.products.find { it.id == "alitas_papas" } // Fallback generic
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
            // Elegant top navigation and title bar
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
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(elevation = 1.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = textPrimary,
                            modifier = Modifier
                                .size(18.dp)
                                .testTag("back_button_cart")
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Mi Carrito",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = textPrimary,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            if (cartList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 1.dp, shape = RoundedCornerShape(28.dp))
                            .border(BorderStroke(1.2.dp, borderSoft), RoundedCornerShape(28.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF2E6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🛍️",
                                    fontSize = 44.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Tu carrito está vacío",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Agrega unas alitas, boneless o papas para empezar tu pedido.",
                                fontSize = 13.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(28.dp))
                            Button(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("empty_cart_state"),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryOrange),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "Ver menú",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (recommendedProduct != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        RecommendationCard(
                            recommendedProduct = recommendedProduct,
                            onClickAdd = {
                                if (recommendedProduct.hasSauces) {
                                    viewModel.selectedItemQuantity.value = 1
                                    viewModel.selectedItemSauce.value = "BBQ"
                                    viewModel.selectedItemNote.value = ""
                                    showCustomizerForProduct = recommendedProduct
                                } else {
                                    scope.launch {
                                        viewModel.addToCart(recommendedProduct)
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("cart_items_scroll"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Food Summary Section
                    item {
                        SectionHeader(icon = Icons.Default.ShoppingBag, title = "RESUMEN DE COMIDA")
                    }

                    items(cartList) { item ->
                        CartListItemRow(
                            item = item,
                            onRemove = {
                                scope.launch { viewModel.removeFromCart(item) }
                            }
                        )
                    }

                    if (recommendedProduct != null) {
                        item {
                            RecommendationCard(
                                recommendedProduct = recommendedProduct,
                                onClickAdd = {
                                    if (recommendedProduct.hasSauces) {
                                        viewModel.selectedItemQuantity.value = 1
                                        viewModel.selectedItemSauce.value = "BBQ"
                                        viewModel.selectedItemNote.value = ""
                                        showCustomizerForProduct = recommendedProduct
                                    } else {
                                        scope.launch {
                                            viewModel.addToCart(recommendedProduct)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // 2. Delivery Configuration Section
                    item {
                        SectionHeader(icon = Icons.Default.DeliveryDining, title = "CONFIGURACIÓN DE ENTREGA")
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Envio a Domicilio Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                    .border(
                                        BorderStroke(
                                            if (isEnvio) 2.dp else 1.2.dp,
                                            if (isEnvio) primaryOrange else borderSoft
                                        ),
                                        RoundedCornerShape(24.dp)
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(if (isEnvio) Color(0xFFFFFBF9) else Color.White)
                                    .clickable { viewModel.setDeliveryMethod("ENVIO") }
                                    .testTag("method_envio")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp, horizontal = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFF2E6)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeliveryDining,
                                            contentDescription = null,
                                            tint = primaryOrange,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Envío a Domicilio",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Valle, Cortazar o\nJaral del Progreso",
                                        fontSize = 11.sp,
                                        color = textSecondary,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 14.sp
                                    )
                                }

                                if (isEnvio) {
                                    Box(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(primaryOrange)
                                            .align(Alignment.TopEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            // Pasar a Recoger Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                    .border(
                                        BorderStroke(
                                            if (isRecoger) 2.dp else 1.2.dp,
                                            if (isRecoger) primaryOrange else borderSoft
                                        ),
                                        RoundedCornerShape(24.dp)
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(if (isRecoger) Color(0xFFFFFBF9) else Color.White)
                                    .clickable { viewModel.setDeliveryMethod("RECOGER") }
                                    .testTag("method_recoger")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp, horizontal = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFFAF5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = textSecondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Pasar a Recoger",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Sin cargo extra\n",
                                        fontSize = 11.sp,
                                        color = textSecondary,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 14.sp
                                    )
                                }

                                if (isRecoger) {
                                    Box(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(primaryOrange)
                                            .align(Alignment.TopEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Conditional Shipping details (For ENVIO)
                    if (isEnvio) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                    .border(BorderStroke(1.2.dp, borderSoft), RoundedCornerShape(24.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    // Title with PinDrop Icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFF2E6)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PinDrop,
                                                contentDescription = null,
                                                tint = primaryOrange,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Municipio de Entrega",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = textPrimary
                                        )
                                    }

                                    // Valle de Santiago & Cortazar Row Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        listOf("VALLE_SANTIAGO", "CORTAZAR").forEach { muni ->
                                            val isSelected = municipality == muni
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(56.dp)
                                                    .border(
                                                        BorderStroke(
                                                            if (isSelected) 2.dp else 1.2.dp,
                                                            if (isSelected) primaryOrange else borderSoft
                                                        ),
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        if (isSelected) {
                                                            Brush.horizontalGradient(
                                                                colors = listOf(Color(0xFFFF8A00), Color(0xFFFF5200))
                                                            )
                                                        } else {
                                                            Brush.horizontalGradient(
                                                                colors = listOf(Color.White, Color.White)
                                                            )
                                                        }
                                                    )
                                                    .clickable { viewModel.setSelectedMunicipality(muni) }
                                                    .testTag("muni_select_$muni"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center,
                                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
                                                ) {
                                                    if (isSelected) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clip(CircleShape)
                                                                .background(Color.White),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                          Icon(
                                                              imageVector = Icons.Default.Check,
                                                              contentDescription = null,
                                                              tint = primaryOrange,
                                                              modifier = Modifier.size(12.dp)
                                                          )
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                    }
                                                    Text(
                                                        text = if (muni == "VALLE_SANTIAGO") "Valle de Santiago" else "Cortazar",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (isSelected) Color.White else textPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Jaral del Progreso Button (Full Width below)
                                    val isJaralSelected = municipality == "JARAL_PROGRESO"
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .border(
                                                BorderStroke(
                                                    if (isJaralSelected) 2.dp else 1.2.dp,
                                                    if (isJaralSelected) primaryOrange else borderSoft
                                                ),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isJaralSelected) {
                                                    Brush.horizontalGradient(
                                                        colors = listOf(Color(0xFFFF8A00), Color(0xFFFF5200))
                                                    )
                                                } else {
                                                    Brush.horizontalGradient(
                                                        colors = listOf(Color.White, Color.White)
                                                    )
                                                }
                                            )
                                            .clickable { viewModel.setSelectedMunicipality("JARAL_PROGRESO") }
                                            .testTag("muni_select_JARAL_PROGRESO"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
                                        ) {
                                            if (isJaralSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = primaryOrange,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Text(
                                                text = "Jaral del Progreso",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isJaralSelected) Color.White else textPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Title with Home Icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFF2E6)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = null,
                                                tint = primaryOrange,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Dirección de entrega completa",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = textPrimary
                                        )
                                    }

                                    // Multiline Address Textfield with Characters Counter
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = address,
                                            onValueChange = {
                                                if (it.length <= 200) {
                                                    viewModel.setDeliveryAddress(it)
                                                }
                                            },
                                            placeholder = {
                                                Text(
                                                    text = "Calle 5 de Mayo #123, Colonia Centro,\nValle de Santiago, Guanajuato, C.P. 38400",
                                                    fontSize = 14.sp,
                                                    color = textSecondary.copy(alpha = 0.5f),
                                                    lineHeight = 20.sp
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .testTag("input_address"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                focusedBorderColor = primaryOrange,
                                                unfocusedBorderColor = borderSoft,
                                                focusedTextColor = textPrimary,
                                                unfocusedTextColor = textPrimary
                                            ),
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                fontSize = 14.sp,
                                                lineHeight = 20.sp
                                            )
                                        )

                                        // Characters Counter (e.g. 72/200)
                                        Text(
                                            text = "${address.length}/200",
                                            fontSize = 12.sp,
                                            color = textSecondary.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(bottom = 12.dp, end = 16.dp),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Cálculo de distancia (Ruta física) Card
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9F5)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(BorderStroke(1.2.dp, Color(0xFFFFD1B3)), RoundedCornerShape(20.dp)),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(18.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Left: big orange pin inside circle
                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFFFEAE0)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PinDrop,
                                                        contentDescription = null,
                                                        tint = primaryOrange,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(14.dp))

                                                // Right: dynamic distance and estimated times
                                                Column {
                                                    Text(
                                                        text = "Cálculo de distancia (Ruta física)",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = orangeDarkText,
                                                        letterSpacing = 0.3.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    val muniLabel = when (municipality) {
                                                        "VALLE_SANTIAGO" -> "Valle"
                                                        "CORTAZAR" -> "Cortazar"
                                                        else -> "Jaral"
                                                    }
                                                    Text(
                                                        text = "Jaral a $muniLabel: ~${deliveryDetails.distanceKm} km",
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = textPrimary
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Schedule,
                                                            contentDescription = null,
                                                            tint = orangeDarkText,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Envío aprox: ${deliveryDetails.estimatedTransitMinutes} mins (+15 min cocina)",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = textSecondary
                                                        )
                                                    }
                                                }
                                            }

                                            HorizontalDivider(
                                                color = Color(0xFFFFE3D1),
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(vertical = 12.dp)
                                            )

                                            // Info note
                                            Row(
                                                verticalAlignment = Alignment.Top,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = orangeDarkText,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "También disponible desde Jaral del Progreso.\nLa distancia se calcula desde nuestro punto en Jaral.",
                                                    fontSize = 12.sp,
                                                    color = textSecondary,
                                                    lineHeight = 16.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Pick-up sucursal details card
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                    .border(BorderStroke(1.2.dp, borderSoft), RoundedCornerShape(24.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(primaryOrange.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = primaryOrange,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Dirección de Tienda Principal",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = textPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Jaral del Progreso, Guanajuato. Local Principal (Te daremos indicaciones personalizadas al estar listo tu pedido).",
                                        fontSize = 13.sp,
                                        color = textSecondary,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Tiempo medio de preparación: ~15 minutos",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // 4. Contact Information Section
                    item {
                        SectionHeader(icon = Icons.Default.Person, title = "DATOS DE CONTACTO")
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                .border(BorderStroke(1.2.dp, borderSoft), RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { viewModel.setCustomerName(it) },
                                    placeholder = { Text("Nombre completo", color = textSecondary.copy(alpha = 0.6f)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_customer_name"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFFCF9F8),
                                        unfocusedContainerColor = Color(0xFFFCF9F8),
                                        focusedBorderColor = primaryOrange,
                                        unfocusedBorderColor = borderSoft,
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = primaryOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { viewModel.setCustomerPhone(it) },
                                    placeholder = { Text("Teléfono de contacto / WhatsApp", color = textSecondary.copy(alpha = 0.6f)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_customer_phone"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFFCF9F8),
                                        unfocusedContainerColor = Color(0xFFFCF9F8),
                                        focusedBorderColor = primaryOrange,
                                        unfocusedBorderColor = borderSoft,
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = primaryOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // 5. Payment Details Section
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                .border(BorderStroke(1.2.dp, borderSoft), RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                // Title and Icon
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFF2E6)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = primaryOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "MÉTODO DE PAGO",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp,
                                        color = orangeDarkText
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Option 1: Efectivo Contra Entrega
                                    val cashBg = if (isCash) Color(0xFFFFFBF9) else Color.White
                                    val cashBorder = if (isCash) primaryOrange else borderSoft
                                    val cashTextColor = if (isCash) orangeDarkText else textPrimary
                                    val cashIconColor = if (isCash) primaryOrange else textSecondary

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(130.dp)
                                            .border(
                                                BorderStroke(if (isCash) 2.dp else 1.2.dp, cashBorder),
                                                RoundedCornerShape(24.dp)
                                            )
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(cashBg)
                                            .clickable { viewModel.setPaymentMethod("EFECTIVO") }
                                            .testTag("pay_cash")
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = null,
                                                tint = cashIconColor,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Efectivo",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = cashTextColor,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "Contra Entrega",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = cashTextColor,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        // Badge at top right
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(12.dp)
                                        ) {
                                            if (isCash) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(primaryOrange),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .border(BorderStroke(1.2.dp, borderSoft), CircleShape)
                                                        .background(Color.Transparent)
                                                )
                                            }
                                        }
                                    }

                                    // Option 2: Transferencia Bancaria
                                    val transferBg = if (isTransfer) Color(0xFFFFFBF9) else Color.White
                                    val transferBorder = if (isTransfer) primaryOrange else borderSoft
                                    val transferTextColor = if (isTransfer) orangeDarkText else textPrimary
                                    val transferIconColor = if (isTransfer) primaryOrange else textSecondary

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(130.dp)
                                            .border(
                                                BorderStroke(if (isTransfer) 2.dp else 1.2.dp, transferBorder),
                                                RoundedCornerShape(24.dp)
                                            )
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(transferBg)
                                            .clickable { viewModel.setPaymentMethod("TRANSFERENCIA") }
                                            .testTag("pay_transfer")
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalance,
                                                contentDescription = null,
                                                tint = transferIconColor,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Transferencia",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = transferTextColor,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "Bancaria",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = transferTextColor,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        // Badge at top right
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(12.dp)
                                        ) {
                                            if (isTransfer) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(primaryOrange),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .border(BorderStroke(1.2.dp, borderSoft), CircleShape)
                                                        .background(Color.Transparent)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (isCash) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        text = "¿Con cuánto billete pagarás? (Cambio repartidor):",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = cashPayWith,
                                        onValueChange = { viewModel.setCashPayWith(it) },
                                        placeholder = {
                                            Text(
                                                text = "Ej. Con un billete de $200 o $500",
                                                fontSize = 14.sp,
                                                color = textSecondary.copy(alpha = 0.6f)
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .testTag("input_cash_with"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedBorderColor = primaryOrange,
                                            unfocusedBorderColor = borderSoft,
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary
                                        ),
                                        singleLine = true
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF9)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(BorderStroke(1.2.dp, Color(0xFFFFD1B3)), RoundedCornerShape(20.dp)),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(18.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = primaryOrange,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "Datos para Transferencia BBVA",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = orangeDarkText
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Banco: BBVA Bancomer\nBeneficiario: Martín Mendoza K.\nCLABE: 0121 8001 2345 6789 01\nConcepto: Tu nombre y Alitas",
                                                fontSize = 13.sp,
                                                color = textSecondary,
                                                lineHeight = 20.sp
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "*Nota: Al finalizar el pedido deberás enviar el comprobante en WhatsApp directamente a Ulices.",
                                                fontSize = 12.sp,
                                                color = Color(0xFFD84315),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. Sticky Pricing Summary Block
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                .border(BorderStroke(1.2.dp, borderSoft), RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Subtotal alimentos",
                                        fontSize = 16.sp,
                                        color = textSecondary
                                    )
                                    Text(
                                        text = "$${subtotal.toInt()} MXN",
                                        fontSize = 16.sp,
                                        color = textPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                 val label = if (isRecoger) {
                                        "Cargo de envío (Recoger)"
                                    } else {
                                        val mName = when (municipality) {
                                            "VALLE_SANTIAGO" -> "Valle"
                                            "CORTAZAR" -> "Cortazar"
                                            else -> "Jaral"
                                        }
                                        "Cargo de envío ($mName)"
                                    }
                                    val valueStr = if (isRecoger || deliveryDetails.deliveryFee == 0.0) {
                                        "Gratis"
                                    } else {
                                        "$${deliveryDetails.deliveryFee.toInt()} MXN"
                                    }
                                    Text(
                                        text = label,
                                        fontSize = 16.sp,
                                        color = textSecondary
                                    )
                                    Text(
                                        text = valueStr,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                }
                                HorizontalDivider(
                                    color = borderSoft,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TOTAL ESTIMADO",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "$${total.toInt()} MXN",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = primaryOrange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fixed checkout button container at bottom
        if (cartList.isNotEmpty()) {
            val isAddressValid = deliveryMethod == "RECOGER" || address.isNotBlank()

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        if (isAddressValid) {
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFF8A00), Color(0xFFFF5200))
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(primaryOrange.copy(alpha = 0.5f), primaryOrange.copy(alpha = 0.5f))
                            )
                        }
                    )
                    .clickable(enabled = isAddressValid) {
                        viewModel.checkout(onComplete = { orderId ->
                            onNavigateToTracking()
                        })
                    }
                    .navigationBarsPadding()
                    .height(64.dp)
                    .testTag("checkout_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PinDrop,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Confirmar Pedido • $${total.toInt()} MXN",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF2E6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFF7A00),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            color = Color(0xFFC94D00)
        )
    }
}

@Composable
fun CartListItemRow(
    item: CartItemEntity,
    onRemove: () -> Unit
) {
    val primaryOrange = Color(0xFFFF7A00)
    val lightBorderColor = Color(0xFFE8DED5)
    val darkTextColor = Color(0xFF111111)
    val mutedTextColor = Color(0xFF6B5F5A)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp))
            .border(BorderStroke(1.2.dp, lightBorderColor), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food image placeholder with a soft beautiful peach gradient
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFFAF5), Color(0xFFFCEFE3))
                        )
                    )
                    .border(1.dp, Color(0xFFE8DED5), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (item.productId) {
                        "alitas_papas" -> "🍗🍟"
                        "boneless" -> "🍗"
                        "boneless_papas" -> "🍖🍟"
                        "papas_orden" -> "🍟"
                        else -> "🍽️"
                    },
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Salsa: ${item.sauce}  •  Cant: x${item.quantity}",
                    fontSize = 12.sp,
                    color = mutedTextColor
                )
                if (item.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nota: ${item.note}",
                        fontSize = 11.sp,
                        color = Color(0xFFFF7A00),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$${(item.price * item.quantity).toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = primaryOrange
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF2E6))
                    .clickable { onRemove() }
                    .testTag("remove_item_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = primaryOrange,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun RecommendationCard(
    recommendedProduct: com.example.data.Product,
    onClickAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp))
            .border(BorderStroke(1.2.dp, Color(0xFFE8DED5)), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Recomendado",
                    tint = Color(0xFFFF6D00),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Combina tu pedido",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111111)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Agrega unas papas para completar tu antojo.",
                fontSize = 13.sp,
                color = Color(0xFF6B5F5A),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recommendedProduct.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                    Text(
                        text = "$${recommendedProduct.price.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF6D00)
                    )
                }
                Button(
                    onClick = onClickAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00)),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Agregar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
