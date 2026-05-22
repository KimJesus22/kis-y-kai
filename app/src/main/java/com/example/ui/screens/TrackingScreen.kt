package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderEntity
import com.example.ui.FoodViewModel
import com.example.ui.components.CourierMapCanvas
import com.example.ui.components.EmptyStateContent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.HoneyGold
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarningOrange

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    viewModel: FoodViewModel,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val order by viewModel.activeOrder.collectAsState()
    val context = LocalContext.current
    val riderGpsActive by viewModel.isRiderGpsActive.collectAsState()

    // Request permissions launcher for GPS location tracking
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.toggleRiderGps(true)
            Toast.makeText(context, "Modo Repartidor Activo: Transmitiendo GPS", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado para repartidor", Toast.LENGTH_LONG).show()
        }
    }

    // Request permissions launcher for POST_NOTIFICATIONS (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(context, "Notificaciones habilitadas con éxito", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notificaciones desactivadas. Te perderás alertas importantes.", Toast.LENGTH_LONG).show()
        }
    }

    // Automatically request Notification permission on startup with a brief delay to prevent lifecycle/focus race conditions
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            try {
                kotlinx.coroutines.delay(800)
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Elegant, Clean Top Header Bar consistent with the warm design
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
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
                            .clickable { onNavigateHome() }
                            .testTag("back_button_tracking"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rastreo de Pedido",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = order?.orderId ?: "Buscando...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Action to advance state manually (Acelerar simulación debug button styled clean)
                    Button(
                        onClick = { viewModel.forceStateAdvance() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .testTag("force_advance_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "sim",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Acelerar ⚡",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (order == null) {
                val activeOrderId by viewModel.activeOrderId.collectAsState()
                if (activeOrderId == null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateContent(
                            title = "No hay pedido en camino",
                            subtitle = "Cuando confirmes un pedido, podrás rastrear aquí el avance del repartidor.",
                            buttonText = "Ir al menú",
                            icon = Icons.Default.LocalShipping,
                            onButtonClick = onNavigateHome,
                            testTag = "empty_tracking_state"
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Cargando ticket de orden...", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                val ord = order!!

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("tracking_scroll_panel"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // 1. CHOSEN MAP CANVAS tracking
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "MAPA EN TRÁNSITO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        RoundedCornerShape(24.dp)
                                    ),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                CourierMapCanvas(
                                    courierLat = ord.currentCourierLat,
                                    courierLng = ord.currentCourierLng,
                                    destLat = ord.destinationLat,
                                    destLng = ord.destinationLng,
                                    municipality = ord.municipality,
                                    status = ord.status,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .testTag("map_canvas")
                                )
                            }
                        }
                    }

                    // 1.5 MÓDULO DE REPARTIDOR: GPS EN VIVO
                    if (ord.deliveryMethod == "ENVIO" && ord.status != "ENTREGADO") {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                                    .testTag("rider_gps_control_card"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MyLocation,
                                                contentDescription = "GPS",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            text = "Modo Repartidor (GPS en Vivo) 🛵",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Usa tu ubicación actual como la del chofer en reparto. Al mover la ubicación (o usar los controles del simulador Android), el mapa reflejará tus coordenadas en vivo.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (riderGpsActive) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(FreshGreen)
                                                )
                                                Text(
                                                    text = "Transmitiendo GPS...",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FreshGreen
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                                )
                                                Text(
                                                    text = "Transmisión inactiva",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = riderGpsActive,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    locationPermissionLauncher.launch(
                                                        arrayOf(
                                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                        )
                                                    )
                                                } else {
                                                    viewModel.toggleRiderGps(false)
                                                    Toast.makeText(context, "Transmisión GPS desactivada", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.testTag("rider_gps_switch")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. LIVE PROGRESS CARD (Stages of preparation)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(28.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(28.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Progreso del Pedido",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(18.dp))

                                // Stages representation
                                OrderProgressStateRow(
                                    activeStatus = ord.status,
                                    method = ord.deliveryMethod
                                )
                            }
                        }
                    }

                    // 3. WHATSAPP CONFIRMATION CTA - Styled in gorgeous warm green theme
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "whatsapp",
                                        tint = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "Coordinación por WhatsApp",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "¡Manda el pedido directamente al WhatsApp del negocio de mi amigo! Así tendrá tu orden y podrá enviarte los detalles de cobro o confirmar tu entrega.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF33691E),
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        sendWhatsAppOrderText(context, ord)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("whatsapp_message_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "share",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Enviar Ticket por WhatsApp 💬",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. DETAILED ADDRESS / INFO
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
                                    text = "Datos de Entrega / Pago",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Divider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = "C", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Cliente: ${ord.customerName}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = "P", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Teléfono: ${ord.phone}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(imageVector = Icons.Default.Home, contentDescription = "D", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Dirección: ${ord.address} (${if (ord.deliveryMethod == "RECOGER") "Pasar a recoger en Jaral del Progreso" else ord.municipality.replace("_", " ")})",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 16.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(imageVector = Icons.Default.CreditCard, contentDescription = "Pa", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Método de Pago: ${if (ord.paymentMethod == "EFECTIVO") "Pago contra entrega (Efectivo)" else "Transferencia Bancaria"}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (ord.paymentMethod == "EFECTIVO") {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                        Icon(imageVector = Icons.Default.Payments, contentDescription = "Ch", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (ord.cashPayWith > 0.0) {
                                                "Paga con: $${ord.cashPayWith.toInt()} MXN (Llevar cambio)"
                                            } else {
                                                "Paga con cantidad exacta"
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                Divider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )

                                Text(
                                    text = "Alimentos Ordenados:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ord.itemsJson,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Return Home sticky bottom button styled as modern Pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onNavigateHome,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("back_to_menu_sticky"),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Volver a la Tienda 🏠",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun OrderProgressStateRow(
    activeStatus: String,
    method: String
) {
    val statuses = listOf("RECIBIDO", "PREPARANDO", "LISTO", "EN_CAMINO", "ENTREGADO")
    val displayNames = listOf("Recibido", "En Plancha", "Listo", "En Camino", "Entregado")
    val icons = listOf(Icons.Default.Receipt, Icons.Default.DinnerDining, Icons.Default.Fastfood, Icons.Default.DeliveryDining, Icons.Default.CheckCircle)

    val activeIdx = statuses.indexOf(activeStatus).coerceAtLeast(0)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (i in statuses.indices) {
            // If Recoger is chosen, we omit EN_CAMINO stage logic
            if (method == "RECOGER" && statuses[i] == "EN_CAMINO") continue

            val isPassed = i <= activeIdx
            val isCurrent = i == activeIdx

            val label = if (method == "RECOGER" && statuses[i] == "LISTO") "Listo para Recoger" else displayNames[i]

            val accentColor = when {
                isCurrent -> if (activeStatus == "ENTREGADO") FreshGreen else MaterialTheme.colorScheme.secondary
                isPassed -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.background
            }

            val iconColor = if (isPassed) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle Node styled clean
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .border(
                            1.dp,
                            if (isPassed) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icons[i],
                        contentDescription = label,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text Description with Stitch AI styled warm container/chips
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                            color = if (isPassed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SoftAlertLabelColor(activeStatus))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ACTIVO",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (isCurrent) {
                        Text(
                            text = when (activeStatus) {
                                "RECIBIDO" -> "Estamos procesando tu orden de Kis y Kei en el sistema..."
                                "PREPARANDO" -> "El chef de alitas ya encendió la freidora y está dorando las alitas/papas con salsa BBQ/Búfalo..."
                                "LISTO" -> {
                                    if (method == "RECOGER") "¡Listo! Ya puedes venir por él a nuestro mostrador calientito en Jaral del Progreso."
                                    else "¡Listo! La comida está empacada térmicamente para salir en ruta."
                                }
                                "EN_CAMINO" -> "Repartidor de Kis y Kei en ruta activa. Puedes monitorear su GPS en vivo desde el mapa arriba."
                                "ENTREGADO" -> "¡Llegó con éxito! Muchas gracias por comprar con Alitas Kis y Kei. ¡Buen provecho!"
                                else -> "Cargando etapa..."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

fun SoftAlertLabelColor(status: String): Color {
    return when (status) {
        "RECIBIDO" -> Color(0xFF9E9A85) // soft earthy sage tint for Received status
        "PREPARANDO" -> WarningOrange  // Warm Orange
        "LISTO" -> FreshGreen          // Fresh Green
        "EN_CAMINO" -> HoneyGold       // Soft Red accent
        "ENTREGADO" -> FreshGreen
        else -> Color.Gray
    }
}

fun buildWhatsAppTicket(order: OrderEntity): String {
    val subtotal = run {
        var totalSum = 0.0
        try {
            val parts = order.itemsJson.split("; ")
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

    val total = subtotal + order.deliveryFee

    val parsedItems = try {
        val parts = order.itemsJson.split("; ").filter { it.isNotBlank() }
        parts.joinToString("\n") { part ->
            val qtyStart = part.indexOf("(x")
            val qtyEnd = part.indexOf(")", qtyStart)
            val qty = if (qtyStart != -1 && qtyEnd != -1) {
                part.substring(qtyStart + 2, qtyEnd)
            } else "1"

            val sauceStart = part.indexOf("[")
            val sauceEnd = part.indexOf("]", sauceStart)
            val sauce = if (sauceStart != -1 && sauceEnd != -1) {
                part.substring(sauceStart + 1, sauceEnd)
            } else "Natural"

            val limit = listOf(part.indexOf("["), part.indexOf("(x")).filter { it != -1 }.minOrNull() ?: part.length
            val productName = part.substring(0, limit).trim()

            val priceIndex = part.indexOf(" - $")
            val rawPrice = if (priceIndex != -1) {
                part.substring(priceIndex + 4).replace(" MXN", "").trim()
            } else ""
            val priceNum = rawPrice.toDoubleOrNull()?.toInt()?.toString() ?: rawPrice

            "• ${qty}x $productName\n  Salsa: $sauce\n  Nota: No especificado\n  Subtotal: $$priceNum"
        }
    } catch (e: Exception) {
        order.itemsJson.split("; ").filter { it.isNotBlank() }.joinToString("\n") { "• $it" }
    }

    val methodLabel = if (order.deliveryMethod == "RECOGER") "Recoger" else "Envío"
    val municipalityLabel = if (order.deliveryMethod == "RECOGER") "Jaral del Progreso" else order.municipality.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    val addressLabel = if (order.address.isNotBlank()) order.address else "No especificado"
    val kmLabel = if (order.deliveryMethod == "RECOGER") "No aplica" else "${order.distanceKm} km"
    val timeLabel = "${order.estimatedTimeMinutes} min"

    val payMethodLabel = if (order.paymentMethod == "EFECTIVO") "Efectivo" else "Transferencia"
    val cashPayInfo = if (order.paymentMethod == "EFECTIVO") {
        if (order.cashPayWith > 0.0) "$${order.cashPayWith.toInt()} MXN" else "Cantidad exacta"
    } else {
        "No aplica"
    }

    val statusLabel = when (order.status) {
        "ENTREGADO" -> "Entregado ✅"
        "LISTO" -> "Listo en tienda 🛍️"
        "EN_CAMINO" -> "En camino 🛵"
        "PREPARANDO" -> "En plancha 🔥"
        else -> "Recibido 📝"
    }

    val ulicesGreeting = if (order.deliveryMethod == "RECOGER") {
        "¡Hola Ulices! Estoy utilizando tu app, he realizado un pedido. Voy a ir de forma presencial por él (Recoger en Local). 🏃‍♂️🍗"
    } else {
        "¡Hola Ulices! Estoy utilizando tu app, he realizado un pedido. Quiero que se me envíe a mi domicilio. 🛵📦"
    }

    return """
        $ulicesGreeting

        🍗 *Alitas Kis y Kei*
        📍 Pedido generado desde la app

        👤 *Cliente*
        Nombre: ${order.customerName}
        Tel: ${order.phone}

        🛒 *Pedido*
        $parsedItems

        🚚 *Entrega*
        Método: $methodLabel
        Municipio: $municipalityLabel
        Dirección: $addressLabel
        Distancia aprox: $kmLabel
        Tiempo estimado: $timeLabel

        💳 *Pago*
        Método: $payMethodLabel
        Paga con: $cashPayInfo

        💰 *Total*
        Comida: $$${subtotal.toInt()}
        Envío: $$${order.deliveryFee.toInt()}
        Total: $$${total.toInt()}

        🕒 Estado actual: $statusLabel
    """.trimIndent()
}

fun sendWhatsAppOrderText(context: Context, order: OrderEntity) {
    try {
        val formattedMsg = buildWhatsAppTicket(order)

        // Create WhatsApp intent targeted directly to Ulices (4731841964)
        val phoneNo = "524731841964" // 52 country code for Mexico + 10 digits
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNo&text=" + Uri.encode(formattedMsg))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir WhatsApp para enviar el pedido", Toast.LENGTH_LONG).show()
    }
}
