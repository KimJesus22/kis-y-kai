package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val infoText: String
)

private data class SalsaOption(
    val id: String,
    val name: String,
    val label: String,
    val description: String
)

@Composable
fun ProductImagePlaceholder(productId: String, modifier: Modifier = Modifier) {
    val gradientBrush = remember(productId) {
        when (productId) {
            "alitas_papas" -> Brush.linearGradient(
                colors = listOf(Color(0xFFFFF2E6), Color(0xFFFFD9B3))
            )
            "boneless" -> Brush.linearGradient(
                colors = listOf(Color(0xFFFFFAF5), Color(0xFFFFE5CC))
            )
            "boneless_papas" -> Brush.linearGradient(
                colors = listOf(Color(0xFFFFF5EB), Color(0xFFFFE0C2))
            )
            "papas_orden" -> Brush.linearGradient(
                colors = listOf(Color(0xFFFFFBEA), Color(0xFFFFF0B3))
            )
            else -> Brush.linearGradient(
                colors = listOf(Color(0xFFF7F3EE), Color(0xFFE8DED5))
            )
        }
    }

    val accentColor = remember(productId) {
        when (productId) {
            "alitas_papas" -> Color(0xFFFF7A00)
            "boneless" -> Color(0xFFFF5200)
            "boneless_papas" -> Color(0xFFFF6D00)
            "papas_orden" -> Color(0xFFFFB300)
            else -> Color(0xFF8C847E)
        }
    }

    val abbreviation = remember(productId) {
        when (productId) {
            "alitas_papas" -> "A & P"
            "boneless" -> "BNS"
            "boneless_papas" -> "B & P"
            "papas_orden" -> "PAP"
            else -> "AL"
        }
    }

    val imageUrl = remember(productId) {
        when (productId) {
            "alitas_papas" -> "https://images.unsplash.com/photo-1527477396000-e27163b481c2?auto=format&fit=crop&w=260&h=260&q=80"
            "boneless" -> "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?auto=format&fit=crop&w=260&h=260&q=80"
            "boneless_papas" -> "https://images.unsplash.com/photo-1569337795-c261e479603f?auto=format&fit=crop&w=260&h=260&q=80"
            "papas_orden" -> "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?auto=format&fit=crop&w=260&h=260&q=80"
            else -> ""
        }
    }

    Box(
        modifier = modifier
            .border(1.dp, Color(0xFFE8DED5), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFCF9F8)),
        contentAlignment = Alignment.Center
    ) {
        // First draw placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val strokeWidth = 1.5.dp.toPx()
                val gridColor = accentColor.copy(alpha = 0.08f)

                // Draw subtle diagonal branding grid
                for (i in -4..8) {
                    val offset = i * (width / 5)
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(offset, 0f),
                        end = androidx.compose.ui.geometry.Offset(offset + width, height),
                        strokeWidth = strokeWidth
                    )
                }

                // Draw nested circular lines representing chef curves
                drawCircle(
                    color = accentColor.copy(alpha = 0.06f),
                    radius = width * 0.35f,
                    center = androidx.compose.ui.geometry.Offset(width / 2f, height / 2f)
                )

                drawCircle(
                    color = accentColor.copy(alpha = 0.12f),
                    radius = width * 0.25f,
                    center = androidx.compose.ui.geometry.Offset(width / 2f, height / 2f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            Text(
                text = abbreviation,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = accentColor.copy(alpha = 0.4f),
                letterSpacing = 0.5.sp
            )
        }

        // Overlap with real Coil Image
        if (imageUrl.isNotEmpty()) {
            coil.compose.AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun BrandLogo(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Color(0xFFFF7A00), CircleShape)
            .clickable(onClick = onClick)
            .testTag("stats_logo_trigger"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
             Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = "Kis & Kei",
                tint = Color(0xFFFF7A00),
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = "K & K",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111111),
                lineHeight = 10.sp
            )
        }
    }
}

@Composable
private fun SalsaChip(
    optionName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor = Color(0xFFFF7A00)
    val unselectedBg = Color(0xFFFCF9F8)
    val outlineColor = if (isSelected) selectedColor else Color(0xFFE8DED5)

    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (isSelected) selectedColor else unselectedBg,
        border = BorderStroke(1.2.dp, outlineColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                )
            }
            Text(
                text = optionName,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF111111)
            )
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
            .background(Color(0xFFFCF9F8))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("menu_food_list"),
            contentPadding = PaddingValues(bottom = 140.dp) // Generous padding to avoid overlap with cart bar
        ) {
            // 1. COMPACT BRAND HEADER
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrandLogo(
                        modifier = Modifier.size(52.dp),
                        onClick = {
                            showAdminStatsDialog = true
                            viewModel.loadSupabaseStats()
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alitas Kis y Kei",
                            color = Color(0xFF111111),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.testTag("brand_title")
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "4.8 • 25–35 min",
                                color = Color(0xFF6B6B6B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showHistoryDialog = true },
                            modifier = Modifier.size(36.dp).background(Color.White, CircleShape).shadow(1.dp, CircleShape)
                        ) {
                            Icon(Icons.Default.History, null, tint = Color(0xFF111111), modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { showNotificationSheet = true },
                            modifier = Modifier.size(36.dp).background(Color.White, CircleShape).shadow(1.dp, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (notifications.any { !it.isRead }) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (notifications.any { !it.isRead }) Color(0xFFFF7A00) else Color(0xFF111111),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. PROMO BANNER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF6D00))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "¡Alitas Crujientes!",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "Bañadas en tu salsa favorita.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier.size(width = 90.dp, height = 70.dp).clip(RoundedCornerShape(12.dp))
                        ) {
                            coil.compose.AsyncImage(
                                model = "https://images.unsplash.com/photo-1608039829572-78524f79c4c7?auto=format&fit=crop&w=260&h=200&q=80",
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // 3. PRODUCT LIST
            item {
                Text(
                    text = "Nuestro menú",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111111),
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(viewModel.products) { product ->
                FoodListItem(
                    product = product,
                    isFavorite = product.id in favoriteIds,
                    onFavoriteToggle = {
                        val newFavorites = if (product.id in favoriteIds) favoriteIds - product.id else favoriteIds + product.id
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

        // 4. FIXED BOTTOM CART BAR
        if (totalCartCount > 0 || activeOrderId != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (activeOrderId != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToTracking() },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.DirectionsBike, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sigue tu pedido en tiempo real", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (totalCartCount > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(56.dp).clickable { onNavigateToCart() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5200)),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$totalCartCount", color = Color(0xFFFF5200), fontWeight = FontWeight.Black, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Ver carrito", color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                            Text("$${cartSubtotal.toInt()} MXN", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // DIALOGS
        if (showAdminStatsDialog) {
            AdminSalesStatsDialog(viewModel = viewModel, onDismiss = { showAdminStatsDialog = false })
        }

        if (showHistoryDialog) {
            HistoryDialog(allOrders = allOrders, viewModel = viewModel, onDismiss = { showHistoryDialog = false }, onNavigateToCart = onNavigateToCart)
        }

        showDetailDialogForProduct?.let { product ->
            FoodCustomizerDialog(
                product = product,
                viewModel = viewModel,
                onDismiss = { showDetailDialogForProduct = null },
                onAddConfirmed = {
                    showDetailDialogForProduct = null
                    android.widget.Toast.makeText(context, "¡Añadido!", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showNotificationSheet) {
            NotificationsDialog(notifications = notifications, onDismiss = { showNotificationSheet = false })
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(enabled = product.available, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImagePlaceholder(
                productId = product.id,
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF111111))
                Text(product.description, fontSize = 12.sp, color = Color(0xFF6B6B6B), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                Text("$${product.price.toInt()} MXN", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF7A00))
            }

            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color(0xFFE53935) else Color(0xFF8C847E)
                )
            }
        }
    }
}

@Composable
fun HistoryDialog(allOrders: List<com.example.data.OrderEntity>, viewModel: FoodViewModel, onDismiss: () -> Unit, onNavigateToCart: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Mis pedidos", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(16.dp))
                if (allOrders.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tienes pedidos aún", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(allOrders) { ord ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCF9F8)),
                                border = BorderStroke(1.dp, Color(0xFFE8DED5))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(ord.orderId, fontWeight = FontWeight.Bold, color = Color(0xFFFF7A00))
                                    Text(ord.status, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.repeatOrder(ord) {
                                                onDismiss()
                                                onNavigateToCart()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00).copy(alpha = 0.1f), contentColor = Color(0xFFFF7A00)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Pedir de nuevo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
fun NotificationsDialog(notifications: List<com.example.ui.SimulatedNotification>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().height(400.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Notificaciones", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(16.dp))
                if (notifications.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sin notificaciones", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(notifications) { notif ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCF9F8))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(notif.text, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
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
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.96f).fillMaxHeight(0.94f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // HEADER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterStart).size(40.dp).background(Color(0xFFF5F5F5), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF111111), modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "Añadir al pedido",
                        modifier = Modifier.align(Alignment.Center),
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color(0xFF111111)
                    )
                }

                // CONTENT
                Column(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {
                    // IMAGE HERO
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        ProductImagePlaceholder(
                            productId = product.id,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        // TITLE & DESCRIPTION
                        Text(
                            text = product.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF111111)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = product.description,
                            fontSize = 14.sp,
                            color = Color(0xFF6B6B6B),
                            lineHeight = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        // PRICE
                        Text(
                            text = "$${product.price.toInt()} MXN",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF7A00)
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Spacer(modifier = Modifier.height(24.dp))

                        // QUANTITY SELECTOR
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cantidad", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.background(Color(0xFFF7F7F7), CircleShape).padding(4.dp)
                            ) {
                                IconButton(
                                    onClick = { if (quantity > 1) viewModel.selectedItemQuantity.value = quantity - 1 },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Remove, null, tint = Color(0xFFFF7A00))
                                }
                                Text(
                                    "$quantity",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                IconButton(
                                    onClick = { if (quantity < 10) viewModel.selectedItemQuantity.value = quantity + 1 },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Color(0xFFFF7A00))
                                }
                            }
                        }

                        // SAUCE SELECTOR
                        if (product.hasSauces) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text("Elige tu salsa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val options = listOf("BBQ", "Buffalo", "Mango Habanero", "Lemon Pepper", "Natural")
                                options.forEach { opt ->
                                    SalsaChip(
                                        optionName = opt,
                                        isSelected = sauce == opt,
                                        onClick = { viewModel.selectedItemSauce.value = opt }
                                    )
                                }
                            }
                        }

                        // SPECIAL NOTE
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EditNote, null, tint = Color(0xFFFF7A00), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nota especial", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = note,
                            onValueChange = { if (it.length <= 120) viewModel.selectedItemNote.value = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            placeholder = { Text("Ej. Sin cebolla, papas bien doradas...", fontSize = 14.sp) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF7A00),
                                unfocusedBorderColor = Color(0xFFE8DED5)
                            ),
                            supportingText = {
                                Text(
                                    "${note.length}/120",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End,
                                    fontSize = 11.sp
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // FOOTER ACTIONS
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = CircleShape,
                            border = BorderStroke(1.5.dp, Color(0xFFFF7A00)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF7A00))
                        ) {
                            Text("Cancelar", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                scope.launch { viewModel.addToCart(product) }
                                onAddConfirmed()
                            },
                            modifier = Modifier.weight(1.5f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00)),
                            shape = CircleShape,
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingBag, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Agregar $${(product.price * quantity).toInt()}",
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSalesStatsDialog(viewModel: FoodViewModel, onDismiss: () -> Unit) {
    val stats = viewModel.salesStats
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Estadísticas de venta", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                if (viewModel.isLoadingStats) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (stats != null) {
                    Text("Pedidos hoy: ${stats.ordersToday}")
                    Text("Ingresos hoy: $${stats.totalRevenueToday}")
                    Text("Ticket promedio: $${stats.averageTicketToday}")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
fun StatCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, bgColor: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Text(title, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun LeaderboardItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, name: String, countText: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 10.sp, color = Color.Gray)
            Text(name, fontWeight = FontWeight.Bold)
        }
        Text(countText, fontWeight = FontWeight.Black, color = Color(0xFFFF7A00))
    }
}
