package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Product
import com.example.ui.FoodViewModel
import com.example.ui.components.EmptyStateContent
import com.example.ui.theme.HoneyGold
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPinkDark
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

private data class PromoItem(
    val title: String,
    val description: String,
    val emoji: String,
    val infoText: String
)

private data class SalsaOption(
    val id: String,
    val name: String,
    val label: String,
    val emoji: String
)

@Composable
private fun SalsaChip(
    option: SalsaOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor = Color(0xFFFF6D00)
    val unselectedBg = Color(0xFFFCF9F8)
    val outlineColor = if (isSelected) selectedColor else Color(0xFFE2BFB0)

    Card(
        modifier = modifier
            .testTag("sauce_chip_${option.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) selectedColor else unselectedBg
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, outlineColor)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = option.emoji,
                fontSize = 15.sp
            )
            Column {
                Text(
                    text = option.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color(0xFF1B1C1C)
                )
                Text(
                    text = option.label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF594136).copy(alpha = 0.75f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: FoodViewModel,
    onNavigateToCart: () -> Unit,
    onNavigateToTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cartList by viewModel.cartItems.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val activeOrderId by viewModel.activeOrderId.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()

    var showDetailDialogForProduct by remember { mutableStateOf<Product?>(null) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    val totalCartCount = cartList.sumOf { it.quantity }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. CLEAN MODERN HEADER - WHITE/CREAM WITH WARM TEXT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alitas Kis y Kei",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                            modifier = Modifier.testTag("brand_title")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Alitas, boneless y papas recién hechas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Order history button
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                .clickable { showHistoryDialog = true }
                                .testTag("history_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Historial de Pedidos",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Notification alert button styled as a clean card pill
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                .clickable { showNotificationSheet = !showNotificationSheet }
                                .testTag("notification_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (notifications.any { !it.isRead }) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = if (notifications.any { !it.isRead }) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            if (notifications.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. WARM PROMOTION BANNER IN FLUID CARDS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Alitas y Boneless Crujientes! 🔥",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bañados al instante con salsa BBQ, Búfalo, Mango Habanero o Lemon Pepper.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Promos de hoy - Horizontal Scroll Card representation
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "PROMOS DE HOY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
            )

            val promos = remember {
                listOf(
                    PromoItem(
                        title = "Envío gratis 🛵",
                        description = "En pedidos mayores a $250 MXN.",
                        emoji = "🛵",
                        infoText = "¡Ordena más de $250 pesos y tu envío es gratis! 🛵🔥"
                    ),
                    PromoItem(
                        title = "Mango Habanero ☄️",
                        description = "Recomendado: Boneless con Mango Habanero.",
                        emoji = "☄️",
                        infoText = "La emblemática combinación picante y dulce. ¡Agrégalos desde tu menú! 🌶️✨"
                    ),
                    PromoItem(
                        title = "Combo Antojo 🍗",
                        description = "Alitas con papas + orden de papas.",
                        emoji = "🍗",
                        infoText = "El dueto ideal. Pide tus alitas y papas preferidas hoy mismo. 🍗🍟"
                    ),
                    PromoItem(
                        title = "Ahorra el envío 🛍️",
                        description = "Recoge en tienda y ahorra el envío.",
                        emoji = "🛍️",
                        infoText = "¡Selecciona 'Recoger' al pagar para ahorrarte el envío! 🛍️"
                    )
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(promos) { promo ->
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .height(86.dp)
                            .clickable {
                                android.widget.Toast.makeText(context, promo.infoText, android.widget.Toast.LENGTH_LONG).show()
                            }
                            .testTag("promo_card_${promo.title.replace(" ", "_")}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = promo.emoji,
                                    fontSize = 20.sp
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = promo.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = promo.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 3. MENU SECTION LABEL
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "NUESTRO MENÚ REAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            // 4. MAIN LIST (STRICTLY FEEDING THE 4 REAL PRODUCTS)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("menu_food_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(viewModel.products) { product ->
                    FoodListItem(
                        product = product,
                        onClick = {
                            showDetailDialogForProduct = product
                            viewModel.selectedItemQuantity.value = 1
                            viewModel.selectedItemSauce.value = "BBQ"
                            viewModel.selectedItemNote.value = ""
                        }
                    )
                }
            }
        }

        // 5. FLOATING PILL ACTION BAR FOR NAVIGATION
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (activeOrderId != null) {
                    Button(
                        onClick = onNavigateToTracking,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("floating_track_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = CircleShape,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = "Rastreo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mi Rastrero",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = onNavigateToCart,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(56.dp)
                        .testTag("floating_cart_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (totalCartCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
                    ),
                    shape = CircleShape,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    enabled = totalCartCount > 0
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Carrito",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (totalCartCount > 0) "Ver Carrito" else "Carrito Vacío",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (totalCartCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$totalCartCount",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. ACTION SHEETS & DIALOGS
        // Order History Modal Dialog
        if (showHistoryDialog) {
            val dateFormater = remember { java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault()) }
            Dialog(onDismissRequest = { showHistoryDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Historial",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mis Pedidos Anteriores",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { showHistoryDialog = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        if (allOrders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyStateContent(
                                    title = "Aún no tienes pedidos",
                                    subtitle = "Tu primer antojo está a un toque.",
                                    buttonText = "Pedir ahora",
                                    icon = Icons.Default.ShoppingBag,
                                    onButtonClick = { showHistoryDialog = false },
                                    testTag = "empty_history_state"
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(allOrders) { ord ->
                                    val formattedDate = remember(ord.timestamp) {
                                        try {
                                            dateFormater.format(java.util.Date(ord.timestamp))
                                        } catch (e: Exception) {
                                            "Fecha desconocida"
                                        }
                                    }
                                    
                                    // Calculate subtotal
                                    val subtotal = remember(ord.itemsJson) {
                                        var totalSum = 0.0
                                        try {
                                            val parts = ord.itemsJson.split("; ")
                                            for (p in parts) {
                                                if (p.isBlank()) continue
                                                val priceText = p.substringAfterLast(" - $", "").replace(" MXN", "").trim()
                                                val priceNum = priceText.toDoubleOrNull()
                                                if (priceNum != null) {
                                                    totalSum += priceNum
                                                }
                                            }
                                        } catch (e: java.lang.Exception) {
                                            // fallback parsing
                                        }
                                        if (totalSum <= 0.0) 120.0 else totalSum
                                    }
                                    val orderTotalCost = subtotal + ord.deliveryFee

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                                RoundedCornerShape(16.dp)
                                            ),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = ord.orderId,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                
                                                // Simplified localized Spanish Status labels
                                                val statusColor = when (ord.status) {
                                                    "ENTREGADO" -> Color(0xFF2E7D32)
                                                    "LISTO" -> Color(0xFF00796B)
                                                    "EN_CAMINO" -> Color(0xFF1565C0)
                                                    "PREPARANDO" -> Color(0xFFE65100)
                                                    else -> MaterialTheme.colorScheme.secondary
                                                }
                                                val statusLabel = when (ord.status) {
                                                    "ENTREGADO" -> "Entregado ✅"
                                                    "LISTO" -> "Listo en tienda 🛍️"
                                                    "EN_CAMINO" -> "En camino 🛵"
                                                    "PREPARANDO" -> "En plancha 🔥"
                                                    else -> "Recibido 📝"
                                                }
                                                
                                                Text(
                                                    text = statusLabel,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = statusColor,
                                                    modifier = Modifier
                                                        .background(statusColor.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = "Fecha",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = formattedDate,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Item lines list representation
                                            val itemsList = remember(ord.itemsJson) {
                                                ord.itemsJson.split("; ").filter { it.isNotBlank() }
                                            }
                                            itemsList.forEach { line ->
                                                Text(
                                                    text = "• $line",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(vertical = 1.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Envío: $${ord.deliveryFee.toInt()} | Subtotal: $${subtotal.toInt()}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "Pagas en: ${if (ord.paymentMethod == "EFECTIVO") "Efectivo" else "Transferencia"}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "Total: $${orderTotalCost.toInt()} MXN",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 14.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.repeatOrder(ord) {
                                                            showHistoryDialog = false
                                                            // Provide visual sound toast
                                                            android.widget.Toast.makeText(
                                                                context, 
                                                                "¡Pedido repetido con éxito! Navegando al carrito... 🛒", 
                                                                android.widget.Toast.LENGTH_LONG
                                                            ).show()
                                                            onNavigateToCart()
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(36.dp).testTag("repeat_order_button_${ord.orderId}")
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Refresh,
                                                            contentDescription = "Pedir nuevo",
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Pedir de nuevo",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Product Customization Dialog
        showDetailDialogForProduct?.let { product ->
            FoodCustomizerDialog(
                product = product,
                viewModel = viewModel,
                onDismiss = { showDetailDialogForProduct = null },
                onAddConfirmed = {
                    showDetailDialogForProduct = null
                    android.widget.Toast.makeText(context, "¡Añadido al carrito con éxito! 🛒", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Notifications Modal Dialog
        if (showNotificationSheet) {
            Dialog(onDismissRequest = { showNotificationSheet = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .height(420.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Alertas de Pedido",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { showNotificationSheet = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        if (notifications.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Vacías",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No tienes notificaciones por el momento",
                                        color = TextMuted,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(notifications) { notif ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text(
                                                    text = notif.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = notif.time,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = notif.text,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodListItem(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .testTag("food_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food emoji box matching the warm design with outline accents
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (product.id) {
                        "alitas_papas" -> "🪶🍟"
                        "boneless" -> "🍗"
                        "boneless_papas" -> "🍗🍟"
                        "papas_orden" -> "🍟"
                        else -> "🍽"
                    },
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${product.price.toInt()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MXN",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Custom modern circle plus button with primary color scheme
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir al carrito",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodCustomizerDialog(
    product: Product,
    viewModel: FoodViewModel,
    onDismiss: () -> Unit,
    onAddConfirmed: () -> Unit
) {
    val quantity by viewModel.selectedItemQuantity.collectAsState()
    val sauce by viewModel.selectedItemSauce.collectAsState()
    val note by viewModel.selectedItemNote.collectAsState()
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Añadir al Pedido",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = product.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                // Quantity selector block styled with soft round capsules
                Text(
                    text = "Cantidad:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) viewModel.selectedItemQuantity.value = quantity - 1 },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Menos",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "$quantity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .testTag("dialog_qty")
                        )

                        IconButton(
                            onClick = { if (quantity < 10) viewModel.selectedItemQuantity.value = quantity + 1 },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Más",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = "$${(product.price * quantity).toInt()} MXN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (product.hasSauces) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Elige tu salsa:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val sauceOptions = remember {
                        listOf(
                            SalsaOption("BBQ", "BBQ", "Dulce", "🍖"),
                            SalsaOption("Buffalo", "Búfalo", "Picante", "🌶️"),
                            SalsaOption("Mango Habanero", "Mango Habanero", "Dulce/Picosa", "🥭"),
                            SalsaOption("Lemon Pepper", "Lemon Pepper", "Cítrica", "🍋"),
                            SalsaOption("Natural (Sin salsa)", "Natural", "Suave", "✨")
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        sauceOptions.forEach { opt ->
                            val isSelected = sauce == opt.id
                            SalsaChip(
                                option = opt,
                                isSelected = isSelected,
                                onClick = { viewModel.selectedItemSauce.value = opt.id }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Nota especial (Salsas extra, indicaciones):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { viewModel.selectedItemNote.value = it },
                    placeholder = { Text("Ej. Aderezo ranch extra, papas bien doraditas...", fontSize = 12.sp, color = TextMuted.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.addToCart(product)
                            }
                            onAddConfirmed()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("dialog_confirm_add"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = CircleShape
                    ) {
                        Text("Añadir", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
