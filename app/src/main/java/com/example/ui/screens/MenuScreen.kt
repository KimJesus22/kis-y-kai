package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val outlineColor = if (isSelected) selectedColor else Color(0xFFE8DED5)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sauce_chip_${option.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) selectedColor else unselectedBg
        ),
        border = BorderStroke(1.2.dp, outlineColor)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = option.emoji,
                    fontSize = 20.sp
                )
                Column {
                    Text(
                        text = option.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) Color.White else Color(0xFF111111)
                    )
                    Text(
                        text = option.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF6B5F5A)
                    )
                }
            }

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
                        contentDescription = "Check",
                        tint = selectedColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
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
    var showAdminStatsDialog by remember { mutableStateOf(false) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var isFavoriteByCustomer by remember { mutableStateOf(false) }

    val prefs = remember(context) { context.getSharedPreferences("food_favorites", Context.MODE_PRIVATE) }
    var favoriteIds by remember {
        mutableStateOf(
            prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()
        )
    }
    val favoritedProducts = remember(favoriteIds, viewModel.products) {
        viewModel.products.filter { it.id in favoriteIds }
    }

    val totalCartCount = cartList.sumOf { it.quantity }
    val cartSubtotal = cartList.sumOf { it.price * it.quantity }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F3EE)) // Premium cream-claro background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("menu_food_list"),
            contentPadding = PaddingValues(bottom = 150.dp) // Generous bottom space to float the bottom bar
        ) {
            // 1. PREMIUM BRAND HEADER (CREAM BACKGROUND INTEGRATED)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom high-fidelity circular orange badge logo
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.5.dp, Color(0xFFFF7A00), CircleShape)
                                .clickable {
                                    showAdminStatsDialog = true
                                    viewModel.loadSupabaseStats()
                                }
                                .testTag("stats_logo_trigger"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🍗",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "KIS y KEI",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1B1B1B),
                                    lineHeight = 8.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "ALITAS",
                                    fontSize = 5.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF7A00),
                                    lineHeight = 6.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Branding text details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alitas Kis y Kei",
                                color = Color(0xFF1B1B1B), // Dark text to stand out premiumly
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                                modifier = Modifier.testTag("brand_title")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Alitas, boneless y papas recién hechas",
                                color = Color(0xFF6B6B6B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Delivery details & ratings row mimicking DiDi Food exactly
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "4.8 (230+)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1B1B)
                                )
                                Text(text = "•", color = Color(0xFF8C847E), fontSize = 11.sp)
                                Text(
                                    text = "🛵 25–35 min",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF6B6B6B)
                                )
                                Text(text = "•", color = Color(0xFF8C847E), fontSize = 11.sp)
                                Text(
                                    text = "Abierto",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        // Circular buttons stacked next to the right side of the header
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circular History Order button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .shadow(elevation = 1.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { showHistoryDialog = true }
                                    .testTag("history_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Historial de Pedidos",
                                    tint = Color(0xFF6B6B6B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Notification alert button with red status dot matching picture
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .shadow(elevation = 1.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { showNotificationSheet = true }
                                    .testTag("notification_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (notifications.any { !it.isRead }) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = if (notifications.any { !it.isRead }) Color(0xFFFF5100) else Color(0xFF6B6B6B),
                                    modifier = Modifier.size(18.dp)
                                )
                                if (notifications.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(Color(0xFFFF5100), CircleShape)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }

                            // Favorite Button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .shadow(elevation = 1.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable {
                                        isFavoriteByCustomer = !isFavoriteByCustomer
                                        val favMsg = if (isFavoriteByCustomer) "¡Agregado a tus favoritos! ❤️" else "Eliminado de tus favoritos"
                                        android.widget.Toast.makeText(context, favMsg, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .testTag("favorite_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isFavoriteByCustomer) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorito",
                                    tint = if (isFavoriteByCustomer) Color(0xFFE53935) else Color(0xFF6B6B6B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. LARGE ORANGE PROMO BANNER with styled food illustration
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF8A00), Color(0xFFFF5200))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text(
                                    text = "¡Alitas y Boneless Crujientes! 🔥",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bañados al instante con salsa BBQ, Búfalo, Mango Habanero o Lemon Pepper.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.95f),
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // High quality double-bubble illustrative display representing wings and fries
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🍗🍟✨",
                                    fontSize = 32.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Promos Slider Row (Mini Cards)
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "PROMOS DE HOY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFFFF6D00),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                )

                val promos = remember {
                    listOf(
                        PromoItem(
                            title = "Envío gratis 🛵",
                            description = "En pedidos mayores a $250 MXN.",
                            emoji = "🛵",
                            infoText = "¡Ordena más de $250 pesos y tu envío es gratis! 🛵"
                        ),
                        PromoItem(
                            title = "Mango Habanero ☄️",
                            description = "Prueba delicioso Boneless con Mango Habanero.",
                            emoji = "☄️",
                            infoText = "La mejor combinación picante y dulce. ¡Agrégalos en el menú! +🌶️"
                        ),
                        PromoItem(
                            title = "Combo Antojo 🍗",
                            description = "Alitas con papas + orden de papas fritas.",
                            emoji = "🍗",
                            infoText = "El dúo ideal para hoy. ¡Añade tus favoritos! 🍗🍟"
                        ),
                        PromoItem(
                            title = "Recoge en local 🛍️",
                            description = "Selecciona Recoger al pagar y ahorra el envío.",
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(promos) { promo ->
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .height(78.dp)
                                .clickable {
                                    android.widget.Toast.makeText(context, promo.infoText, android.widget.Toast.LENGTH_LONG).show()
                                }
                                .testTag("promo_card_${promo.title.replace(" ", "_")}"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(
                                1.dp,
                                Color(0xFFE8DED5)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0xFFFF8A00).copy(alpha = 0.08f), CircleShape)
                                        .border(1.dp, Color(0xFFFF8A00).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = promo.emoji,
                                        fontSize = 16.sp
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = promo.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B1B1B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = promo.description,
                                        fontSize = 10.sp,
                                        color = Color(0xFF7A6B5E),
                                        lineHeight = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. SECCIÓN "NUESTRO MENÚ" LABELED HEADER WITH FORK ICON
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF7A00)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🍴",
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nuestro menú",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1B1B1B)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0xFFE8DED5))
                    )
                }
            }

            // 3.5 SECCIÓN "TUS FAVORITOS" (Opcional, si hay productos marcados)
            if (favoritedProducts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFECEB)), // Suave rosa-rojo para el fondo del corazón
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Tus Favoritos",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tus favoritos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1B1B1B)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0xFFE8DED5))
                        )
                    }
                }

                items(favoritedProducts) { product ->
                    FoodListItem(
                        product = product,
                        isFavorite = true,
                        onFavoriteToggle = {
                            val newFavorites = favoriteIds - product.id
                            favoriteIds = newFavorites
                            prefs.edit().putStringSet("favorite_ids", newFavorites).apply()
                        },
                        onClick = {
                            if (product.available) {
                                showDetailDialogForProduct = product
                                viewModel.selectedItemQuantity.value = 1
                                viewModel.selectedItemSauce.value = "BBQ"
                                viewModel.selectedItemNote.value = ""
                            }
                        }
                    )
                }
            }

            // 4. VERTICAL PRODUCT LIST (Strictly showing original 4 products)
            items(viewModel.products) { product ->
                val isFav = product.id in favoriteIds
                FoodListItem(
                    product = product,
                    isFavorite = isFav,
                    onFavoriteToggle = {
                        val newFavorites = if (isFav) {
                            favoriteIds - product.id
                        } else {
                            favoriteIds + product.id
                        }
                        favoriteIds = newFavorites
                        prefs.edit().putStringSet("favorite_ids", newFavorites).apply()
                    },
                    onClick = {
                        if (product.available) {
                            showDetailDialogForProduct = product
                            viewModel.selectedItemQuantity.value = 1
                            viewModel.selectedItemSauce.value = "BBQ"
                            viewModel.selectedItemNote.value = ""
                        }
                    }
                )
            }
        }

        // 5. STICKY COLLAPSIBLE checkout pill floating at the very bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tracking Active Order Alert Bar floating just above checkout
                if (activeOrderId != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .height(44.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32)) // Warm forest green
                            .border(1.dp, Color(0xFFC8E6C9), CircleShape)
                            .clickable(onClick = onNavigateToTracking)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🛵",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "¡Tienes un pedido activo! Síguelo aquí en tiempo real ⚡",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Styled shopping cart checkout sticky bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp))
                        .clickable { onNavigateToCart() }
                        .testTag("floating_cart_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF7A00)) // Strong bright orange
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // White circle bag container
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Carrito",
                                tint = Color(0xFFFF7A00),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Items indicator and dynamic label
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Carrito",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // White rounded count badge
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .padding(horizontal = 7.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$totalCartCount",
                                        color = Color(0xFFFF7A00),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Text(
                                text = if (totalCartCount > 0) {
                                    "Total: $${cartSubtotal.toInt()} MXN"
                                } else {
                                    "Agrega productos a tu pedido"
                                },
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Fine divider line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.White.copy(alpha = 0.4f))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Checkout CTA Label text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Ver pedido",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Ver pedido",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. ACTION SHEETS & DIALOGS
        // Admin Sales Stats Dialog
        if (showAdminStatsDialog) {
            AdminSalesStatsDialog(
                viewModel = viewModel,
                onDismiss = { showAdminStatsDialog = false }
            )
        }

        // Order History Dialog
        if (showHistoryDialog) {
            val dateFormater = remember { SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()) }
            Dialog(onDismissRequest = { showHistoryDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE8DED5))
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
                                    tint = Color(0xFFFF7A00)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mis Pedidos Anteriores",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1B1B)
                                )
                            }
                            IconButton(onClick = { showHistoryDialog = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color(0xFF6B6B6B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFE8DED5))
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
                                            dateFormater.format(Date(ord.timestamp))
                                        } catch (e: Exception) {
                                            "Fecha desconocida"
                                        }
                                    }
                                    
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
                                        } catch (e: Exception) {
                                            // fallback
                                        }
                                        if (totalSum <= 0.0) 120.0 else totalSum
                                    }
                                    val orderTotalCost = subtotal + ord.deliveryFee

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                1.dp,
                                                Color(0xFFE8DED5),
                                                RoundedCornerShape(16.dp)
                                            ),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCF9F8))
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
                                                    color = Color(0xFFFF7A00)
                                                )
                                                
                                                val statusColor = when (ord.status) {
                                                    "ENTREGADO" -> Color(0xFF2E7D32)
                                                    "LISTO" -> Color(0xFF00796B)
                                                    "EN_CAMINO" -> Color(0xFF1565C0)
                                                    "PREPARANDO" -> Color(0xFFE65100)
                                                    else -> Color(0xFFFF7A00)
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
                                                    tint = Color(0xFF6B6B6B),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = formattedDate,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF6B6B6B)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = Color(0xFFE8DED5))
                                            Spacer(modifier = Modifier.height(8.dp))

                                            val itemsList = remember(ord.itemsJson) {
                                                ord.itemsJson.split("; ").filter { it.isNotBlank() }
                                            }
                                            itemsList.forEach { line ->
                                                Text(
                                                    text = "• $line",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF1B1B1B),
                                                    modifier = Modifier.padding(vertical = 1.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = Color(0xFFE8DED5))
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
                                                        color = Color(0xFF6B6B6B)
                                                    )
                                                    Text(
                                                        text = "Pagas en: ${if (ord.paymentMethod == "EFECTIVO") "Efectivo" else "Transferencia"}",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF6B6B6B)
                                                    )
                                                    Text(
                                                        text = "Total: $${orderTotalCost.toInt()} MXN",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF1B1B1B)
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.repeatOrder(ord) {
                                                            showHistoryDialog = false
                                                            android.widget.Toast.makeText(
                                                                context, 
                                                                "¡Pedido repetido! Revisa tu carrito... 🛒", 
                                                                android.widget.Toast.LENGTH_LONG
                                                            ).show()
                                                            onNavigateToCart()
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFFF8A00).copy(alpha = 0.08f),
                                                        contentColor = Color(0xFFFF7A00)
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

        // Product Customization Dialog (Preserves salsas, note special & add callbacks)
        showDetailDialogForProduct?.let { product ->
            FoodCustomizerDialog(
                product = product,
                viewModel = viewModel,
                onDismiss = { showDetailDialogForProduct = null },
                onAddConfirmed = {
                    showDetailDialogForProduct = null
                    android.widget.Toast.makeText(context, "¡Añadido con éxito! 🛒", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Alerts Dialog Custom Sheet
        if (showNotificationSheet) {
            Dialog(onDismissRequest = { showNotificationSheet = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .height(420.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE8DED5))
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
                                    tint = Color(0xFFFF7A00)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Alertas de Pedido",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1B1B)
                                )
                            }
                            IconButton(onClick = { showNotificationSheet = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color(0xFF6B6B6B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE8DED5))
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
                                        tint = Color(0xFF6B6B6B).copy(alpha = 0.5f),
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
                                            .border(1.dp, Color(0xFFE8DED5), RoundedCornerShape(16.dp)),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCF9F8)),
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
                                                    color = Color(0xFF1B1B1B),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = notif.time,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF6B6B6B),
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = notif.text,
                                                fontSize = 11.sp,
                                                color = Color(0xFF6B6B6B),
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
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    val isAvailable = product.available
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(elevation = if (isAvailable) 1.dp else 0.dp, shape = RoundedCornerShape(20.dp))
            .border(
                BorderStroke(
                    1.dp,
                    if (isAvailable) Color(0xFFE8DED5) else Color(0xFFE0E0E0)
                ),
                RoundedCornerShape(20.dp)
            )
            .then(
                if (isAvailable) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier.alpha(0.55f)
                }
            )
            .testTag("food_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food emoji box with premium padding of 80x80 dp which displays beautiful large emojis
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF7F3EE))
                    .border(1.dp, Color(0xFFE8DED5), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (product.id) {
                        "alitas_papas" -> "🍗🍟"
                        "boneless" -> "🍗"
                        "boneless_papas" -> "🍖🍟"
                        "papas_orden" -> "🍟"
                        else -> "🍽️"
                    },
                    fontSize = 36.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!isAvailable) {
                        Box(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .background(Color(0xFFFFEBEE), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Agotado",
                                color = Color(0xFFC62828),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = product.description,
                    fontSize = 11.sp,
                    color = Color(0xFF6B6B6B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${product.price.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF7A00) // Vibrant orange pricing text
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MXN",
                        fontSize = 10.sp,
                        color = Color(0xFF8C847E),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de Corazón (Favorito)
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("favorite_toggle_${product.id}")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle Favorito",
                    tint = if (isFavorite) Color(0xFFE53935) else Color(0xFF8C847E),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Premium circular orange Plus floating action button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isAvailable) Color(0xFFFF7A00) else Color(0xFFE0E0E0))
                    .shadow(if (isAvailable) 1.dp else 0.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isAvailable) "Añadir al carrito" else "Agotado",
                    tint = if (isAvailable) Color.White else Color(0xFF9E9E9E),
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
    val scrollState = androidx.compose.foundation.rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp))
                .border(BorderStroke(1.dp, Color(0xFFE8DED5)), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F3EE))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Drag Handle
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCEBFB3))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(4.dp))

                // 2. Centered visual header with close button left
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(elevation = 1.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onDismiss() }
                            .testTag("dialog_dismiss_customizer"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF1B1B1B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "Añadir al pedido",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111111),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // Scrollable container with rounded top borders
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .verticalScroll(scrollState)
                        .padding(bottom = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. PRODUCT HERO GRAPHIC
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFFAF5), Color(0xFFFCEFE3))
                                )
                            )
                            .border(1.dp, Color(0xFFE8DED5), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(94.dp)
                                    .shadow(elevation = 2.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (product.id) {
                                        "alitas_papas" -> "🍗🍟"
                                        "boneless" -> "🍗"
                                        "boneless_papas" -> "🍖🍟"
                                        "papas_orden" -> "🍟"
                                        else -> "🍽️"
                                    },
                                    fontSize = 44.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Alitas Kis y Kei • Sabor Fresh",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9E7E6E),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // 4. TITLES & DESCRIPTION
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = product.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF111111),
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = product.description,
                            fontSize = 13.sp,
                            color = Color(0xFF5F5F5F),
                            lineHeight = 17.sp
                        )
                    }

                    // 5. QUANTITY & PRICE ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFFFF7F2))
                                .border(BorderStroke(1.2.dp, Color(0xFFFFD1B3)), CircleShape)
                                .padding(horizontal = 4.dp, vertical = 3.dp)
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) viewModel.selectedItemQuantity.value = quantity - 1 },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Menos",
                                    tint = Color(0xFFFF7A00),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "$quantity",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF111111),
                                modifier = Modifier
                                    .padding(horizontal = 14.dp)
                                    .testTag("dialog_qty")
                            )

                            IconButton(
                                onClick = { if (quantity < 10) viewModel.selectedItemQuantity.value = quantity + 1 },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Más",
                                    tint = Color(0xFFFF7A00),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = "$${(product.price * quantity).toInt()} MXN",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF7A00)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        color = Color(0xFFE8DED5),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    // 6. ELIGE TU SALSA
                    if (product.hasSauces) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp)
                        ) {
                            Text(
                                text = "Elige tu salsa",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF111111)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val sauceOptions = remember {
                                listOf(
                                    SalsaOption("BBQ", "BBQ", "Suave 🌶️", "🍖"),
                                    SalsaOption("Buffalo", "Búfalo", "Medio 🌶️🌶️", "🌶️"),
                                    SalsaOption("Mango Habanero", "Mango Habanero", "Picante 🌶️🌶️🌶️", "🥭"),
                                    SalsaOption("Lemon Pepper", "Lemon Pepper", "Suave / Cítrica 🌶️", "🍋"),
                                    SalsaOption("Natural (Sin salsa)", "Natural", "Sin picante", "✨")
                                )
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
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

                        HorizontalDivider(
                            color = Color(0xFFE8DED5),
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    // 7. NOTA ESPECIAL
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Notas",
                                tint = Color(0xFFFF7A00),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Nota especial (Salsas extra, indicaciones)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF111111)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        val maxChars = 120
                        if (note != null) {
                            OutlinedTextField(
                                value = note,
                                onValueChange = {
                                    if (it.length <= maxChars) {
                                        viewModel.selectedItemNote.value = it
                                    }
                                },
                                placeholder = {
                                    Text(
                                        text = "Ej. Aderezo ranch extra, papas bien doraditas...",
                                        fontSize = 12.sp,
                                        color = Color(0xFF5F5F5F).copy(alpha = 0.6f)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("note_input"),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFFCF9F8),
                                    unfocusedContainerColor = Color(0xFFFCF9F8),
                                    focusedBorderColor = Color(0xFFFF7A00),
                                    unfocusedBorderColor = Color(0xFFE8DED5),
                                    focusedTextColor = Color(0xFF111111),
                                    unfocusedTextColor = Color(0xFF111111)
                                ),
                                supportingText = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = "${note.length}/$maxChars",
                                            fontSize = 10.sp,
                                            color = Color(0xFF5F5F5F)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // 8. FIXED BOTTOM CONTROL ACTION BAR
                HorizontalDivider(color = Color(0xFFE8DED5))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF7A00),
                            containerColor = Color(0xFFFFF7F2)
                        ),
                        border = BorderStroke(1.2.dp, Color(0xFFFF7A00))
                    ) {
                        Text(
                            text = "Cancelar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.addToCart(product)
                            }
                            onAddConfirmed()
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(52.dp)
                            .testTag("dialog_confirm_add"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00)),
                        shape = CircleShape,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Bolsa",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Añadir al pedido",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSalesStatsDialog(
    viewModel: FoodViewModel,
    onDismiss: () -> Unit
) {
    val stats = viewModel.salesStats
    val isLoading = viewModel.isLoadingStats
    val error = viewModel.statsError

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Estadísticas de Venta 📊",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1B1B)
                        )
                        Text(
                            text = "Métricas desde Supabase",
                            fontSize = 12.sp,
                            color = Color(0xFF6B6B6B)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF5F5F5), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF1B1B1B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                Spacer(modifier = Modifier.height(14.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFFFF7A00))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Descargando datos...",
                                fontSize = 14.sp,
                                color = Color(0xFF6B6B6B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else if (error != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "⚠️ Error de conexión",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                fontSize = 12.sp,
                                color = Color(0xFF6B6B6B),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadSupabaseStats() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00)),
                                shape = CircleShape
                            ) {
                                Text("Reintentar", color = Color.White)
                            }
                        }
                    }
                } else if (stats != null) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Section: Today KPIs
                        Text(
                            text = "PEDIDOS DE HOY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF7A00),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                emoji = "📦",
                                title = "Pedidos",
                                value = stats.ordersToday.toString(),
                                bgColor = Color(0xFFFFE0B2)
                            )
                            StatCard(
                                modifier = Modifier.weight(1.1f),
                                emoji = "💰",
                                title = "Total Hoy",
                                value = String.format(Locale.US, "$%.2f", stats.totalRevenueToday),
                                bgColor = Color(0xFFE8F5E9)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        StatCard(
                            modifier = Modifier.fillMaxWidth(),
                            emoji = "🎫",
                            title = "Ticket Promedio",
                            value = String.format(Locale.US, "$%.2f", stats.averageTicketToday),
                            bgColor = Color(0xFFE3F2FD)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Section: Leaderboards
                        Text(
                            text = "RANGOS Y TOP RECONOCIMIENTOS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF7A00),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Product Leaderboard
                        LeaderboardItem(
                            emoji = "👑",
                            title = "Producto más vendido",
                            name = stats.bestSellingProduct ?: "Ninguno todavía",
                            countText = if (stats.bestSellingProductCount > 0) "${stats.bestSellingProductCount} uds" else ""
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Municipality Leaderboard
                        LeaderboardItem(
                            emoji = "🛵",
                            title = "Municipio de mayor demanda",
                            name = stats.topMunicipality ?: "Ninguno todavía",
                            countText = if (stats.topMunicipalityCount > 0) "${stats.topMunicipalityCount} ped" else ""
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Section: Cumulative / Historical
                        Text(
                            text = "TOTALES HISTÓRICOS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF7A00),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total pedidos", fontSize = 11.sp, color = Color(0xFF888888))
                                Text(stats.totalOrdersAllTime.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B1B1B))
                            }
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("Total acumulado", fontSize = 11.sp, color = Color(0xFF888888))
                                Text(String.format(Locale.US, "$%.2f", stats.totalRevenueAllTime), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { viewModel.loadSupabaseStats() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00)),
                            shape = CircleShape
                        ) {
                            Text("Cargar estadísticas", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    emoji: String,
    title: String,
    value: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCF9F8)),
        border = BorderStroke(1.dp, Color(0xFFE8DED5))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 10.sp, color = Color(0xFF888888), fontWeight = FontWeight.SemiBold)
                Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF1B1B1B))
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    emoji: String,
    title: String,
    name: String,
    countText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFCF9F8), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE8DED5), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFFFF3E0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 9.sp, color = Color(0xFF888888), fontWeight = FontWeight.Bold)
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (countText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFECB3), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = countText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7F5F00)
                )
            }
        }
    }
}
