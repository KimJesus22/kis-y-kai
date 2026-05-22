package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderEntity
import com.example.ui.FoodViewModel
import com.example.ui.components.CourierMapCanvas
import coil.compose.AsyncImage

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
    var showConfirmationCard by remember { mutableStateOf(true) }

    // Paleta de colores de la marca que coincide con las especificaciones de diseño visual
    val primaryOrange = Color(0xFFFF6D00)
    val lightBorderColor = Color(0xFFE8DED5)
    val premiumCreamBg = Color(0xFFFCF9F8)
    val darkTextColor = Color(0xFF111111)
    val mutedTextColor = Color(0xFF6B5F5A)
    val secondaryPanelBg = Color(0xFFFFF4EC)
    val successGreen = Color(0xFF2E7D32)

    // Lanzador de solicitud de permisos para el seguimiento de la ubicación GPS
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

    // Lanzador de solicitud de permisos para POST_NOTIFICATIONS (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(context, "Notificaciones habilitadas con éxito", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notificaciones desactivadas. Te perderás alertas importantes.", Toast.LENGTH_LONG).show()
        }
    }

    // Solicitar automáticamente el permiso de notificaciones al inicio con un breve retraso
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
            .background(premiumCreamBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Sección de Encabezado
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
                    // Botón circular de cerrar/atrás
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(elevation = 1.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onNavigateHome() }
                            .testTag("back_button_tracking"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = darkTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rastreo de Pedido",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = darkTextColor,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = order?.orderId ?: "Buscando...",
                            fontSize = 14.sp,
                            color = primaryOrange,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Botón de insignia "Acelerar" (disparador de simulación de depuración)
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 1.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF0E6))
                            .border(1.dp, Color(0xFFFFCCAA), CircleShape)
                            .clickable { viewModel.forceStateAdvance() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("force_advance_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "sim",
                                tint = Color(0xFFFF5200),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Acelerar ⚡",
                                fontSize = 11.sp,
                                color = Color(0xFFFF5200),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            if (order == null) {
                val activeOrderId by viewModel.activeOrderId.collectAsState()
                if (activeOrderId == null) {
                    // Vista centrada de estado vacío cuando no existe ningún pedido
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                .border(1.dp, lightBorderColor, RoundedCornerShape(24.dp))
                                .testTag("empty_tracking_state"),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF4EC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = primaryOrange,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "No hay pedido en camino",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = darkTextColor,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Cuando confirmes un pedido, podrás rastrear aquí el avance del repartidor.",
                                    fontSize = 13.sp,
                                    color = mutedTextColor,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = onNavigateHome,
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryOrange),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("empty_back_to_menu"),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Text(
                                        text = "Volver a la Tienda 🏠",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Estado de Carga
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = primaryOrange)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Cargando ticket de orden...", color = mutedTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (showConfirmationCard && ord.status == "RECIBIDO") {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp))
                                    .border(1.dp, Color(0xFFFFD1B3), RoundedCornerShape(24.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9F5)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFEAD9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Check",
                                            tint = primaryOrange,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "¡Pedido recibido!",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = darkTextColor
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tu orden ya está en cocina. Puedes rastrear el avance o enviar el ticket por WhatsApp.",
                                        fontSize = 14.sp,
                                        color = mutedTextColor,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    
                                    Button(
                                        onClick = { showConfirmationCard = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryOrange),
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                    ) {
                                        Text(
                                            text = "Ver rastreo",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    OutlinedButton(
                                        onClick = { sendWhatsAppOrderText(context, ord) },
                                        shape = CircleShape,
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                                        border = BorderStroke(1.5.dp, Color(0xFF2E7D32)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Compartir",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Enviar por WhatsApp",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 1. TARJETA DE MAPA CANVAS (LIENZO)
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "MAPA EN TRÁNSITO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.3.sp,
                                color = Color(0xFFC94D00),
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                    .border(1.dp, lightBorderColor, RoundedCornerShape(24.dp)),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                ) {
                                    CourierMapCanvas(
                                        courierLat = ord.currentCourierLat,
                                        courierLng = ord.currentCourierLng,
                                        destLat = ord.destinationLat,
                                        destLng = ord.destinationLng,
                                        municipality = ord.municipality,
                                        status = ord.status,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .testTag("map_canvas")
                                    )

                                    // Etiqueta de información de estado superpuesta en el mapa (arriba a la izquierda)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEDE7F6))
                                            .border(1.dp, Color(0xFFD1C4E9), CircleShape)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF512DA8)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "i",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                            Text(
                                                text = when (ord.status) {
                                                    "RECIBIDO" -> "Localizando repartidor..."
                                                    "PREPARANDO" -> "Preparando en Kis & Kei..."
                                                    "LISTO" -> "Esperando recolección..."
                                                    "EN_CAMINO" -> "Repartidor en camino..."
                                                    "ENTREGADO" -> "Pedido entregado con éxito"
                                                    else -> "Localizando repartidor..."
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF512DA8)
                                            )
                                        }
                                    }

                                    // Botón de acción de centrado de brújula en la esquina inferior derecha (superposición visual)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(14.dp)
                                            .size(42.dp)
                                            .shadow(elevation = 2.dp, shape = CircleShape)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFFFD1B3), CircleShape)
                                            .clickable {
                                                Toast.makeText(context, "Centrando mapa...", Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NearMe,
                                            contentDescription = "Centrar",
                                            tint = primaryOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // CONTROLES DEL SIMULADOR DE GPS EN VIVO
                    if (ord.deliveryMethod == "ENVIO" && ord.status != "ENTREGADO") {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFFFEAE0), RoundedCornerShape(20.dp))
                                    .testTag("rider_gps_control_card"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDFB)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = "GPS",
                                            tint = primaryOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Modo Repartidor (GPS en Vivo) 🛵",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = darkTextColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Usa tu ubicación actual como la del chofer en reparto. Al mover la ubicación, el mapa reflejará tus coordenadas en vivo.",
                                        fontSize = 11.sp,
                                        color = mutedTextColor,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFFF4EC), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (riderGpsActive) successGreen else mutedTextColor.copy(alpha = 0.5f))
                                            )
                                            Text(
                                                text = if (riderGpsActive) "Transmitiendo GPS..." else "Transmisión inactiva",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (riderGpsActive) successGreen else mutedTextColor
                                            )
                                        }

                                        WithRiderGpsSwitch(
                                            riderGpsActive = riderGpsActive,
                                            onToggle = { checked ->
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
                                            primaryOrange = primaryOrange,
                                            lightBorderColor = lightBorderColor,
                                            mutedTextColor = mutedTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. TARJETA DE PROGRESO DE LA LÍNEA DE TIEMPO
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                .border(1.dp, lightBorderColor, RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Progreso del Pedido",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = darkTextColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val dynamicMessage = when (ord.status) {
                                    "RECIBIDO" -> "Tu pedido ya entró a cocina."
                                    "PREPARANDO" -> "Estamos cocinando tu antojo."
                                    "LISTO" -> "Tu pedido está listo para salir."
                                    "EN_CAMINO" -> "El repartidor va hacia ti."
                                    "ENTREGADO" -> "Pedido entregado con éxito."
                                    else -> "Procesando tu pedido..."
                                }
                                Text(
                                    text = dynamicMessage,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryOrange
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Línea de tiempo dinámica
                                OrderProgressStateRowTimeline(
                                    activeStatus = ord.status,
                                    method = ord.deliveryMethod,
                                    primaryOrange = primaryOrange,
                                    darkTextColor = darkTextColor,
                                    mutedTextColor = mutedTextColor
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                HorizontalDivider(
                                    color = Color(0xFFF2ECE7),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )

                                // Fila de resumen de ETA / Repartidor
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFFFF9F5))
                                        .border(1.dp, Color(0xFFFFE0CC), RoundedCornerShape(16.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Izquierda: Tiempo estimado
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFFEAD9)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = "ETA",
                                                    tint = primaryOrange,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Tiempo estimado",
                                                    fontSize = 11.sp,
                                                    color = mutedTextColor,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "${ord.estimatedTimeMinutes} min",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = darkTextColor
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .height(40.dp)
                                                .width(1.dp)
                                                .background(Color(0xFFFFD5BD))
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Derecha: Repartidor asignado
                                        Row(
                                            modifier = Modifier.weight(1.1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFFEAD9)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "Repartidor",
                                                    tint = primaryOrange,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Repartidor asignado",
                                                    fontSize = 11.sp,
                                                    color = mutedTextColor,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = when (ord.status) {
                                                        "ENTREGADO" -> "Entregado por Ulices"
                                                        "EN_CAMINO" -> "Ulices en camino"
                                                        "LISTO" -> "Ulices listo para salir"
                                                        else -> "Ulices asignado"
                                                    },
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = darkTextColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. TARJETA DE COORDINACIÓN POR WHATSAPP
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF5E9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E7D32).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Standard Chat bubble or phone vector representer
                                        Icon(
                                            imageVector = Icons.Default.Forum,
                                            contentDescription = "WhatsApp Coordinación",
                                            tint = Color(0xFF1B5E20),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        text = "Coordinación por WhatsApp",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "¡Manda el pedido directamente al WhatsApp del negocio de mi amigo! Así tendrá tu orden y podrá enviarte los detalles de cobro o confirmar tu entrega.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32),
                                    lineHeight = 17.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Botón de Enviar Ticket por WhatsApp que coincide con las especificaciones
                                Button(
                                    onClick = { sendWhatsAppOrderText(context, ord) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("whatsapp_message_button"),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Compartir",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Enviar Ticket por WhatsApp",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Forum,
                                            contentDescription = "Chat Bubble Symbol",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. TARJETA DE DETALLES DE ENTREGA / PAGO
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                                .border(1.dp, lightBorderColor, RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                // Fila de Título
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFFF4EC)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Assignment,
                                            contentDescription = "Datos de Entrega",
                                            tint = primaryOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        text = "Datos de Entrega / Pago",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = darkTextColor
                                    )
                                }

                                HorizontalDivider(
                                    color = Color(0xFFF2ECE7),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )

                                // Fila 1: Cliente: Nombre
                                InfoItemRow(
                                    icon = Icons.Default.Person,
                                    label = "Cliente",
                                    value = ord.customerName,
                                    primaryOrange = primaryOrange,
                                    darkTextColor = darkTextColor
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Fila 2: Teléfono: Teléfono
                                InfoItemRow(
                                    icon = Icons.Default.Phone,
                                    label = "Teléfono",
                                    value = ord.phone,
                                    primaryOrange = primaryOrange,
                                    darkTextColor = darkTextColor
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Fila 3: Dirección: Dirección
                                val hasBrackets = ord.address.contains("[")
                                val cleanAddressLabel = if (hasBrackets) {
                                    val base = ord.address.substringBefore("[").trim()
                                    if (base.isEmpty()) "Dirección Conocida" else base
                                } else {
                                    ord.address
                                }

                                val parsedTipAmountOnScreenForRider = run {
                                    try {
                                        val pattern = "Propina:\\s*\\$(\\d+)\\s*MXN".toRegex()
                                        val match = pattern.find(ord.address)
                                        match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                                    } catch (e: Exception) {
                                        0.0
                                    }
                                }

                                val parsedScheduledDelayOnScreenForRider = run {
                                    try {
                                        val pattern = "⏰ Programado:\\s*([^|\\]]+)".toRegex()
                                        val match = pattern.find(ord.address)
                                        match?.groupValues?.get(1)?.trim()
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                InfoItemRow(
                                    icon = Icons.Default.LocationOn,
                                    label = "Dirección",
                                    value = "$cleanAddressLabel (${if (ord.deliveryMethod == "RECOGER") "Pasar a recoger en Jaral del Progreso" else ord.municipality.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }})",
                                    primaryOrange = primaryOrange,
                                    darkTextColor = darkTextColor
                                )

                                if (parsedScheduledDelayOnScreenForRider != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    InfoItemRow(
                                        icon = Icons.Default.Schedule,
                                        label = "Horario",
                                        value = "Programado ($parsedScheduledDelayOnScreenForRider)",
                                        primaryOrange = primaryOrange,
                                        darkTextColor = darkTextColor
                                    )
                                }

                                if (parsedTipAmountOnScreenForRider > 0.0) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    InfoItemRow(
                                        icon = Icons.Default.Payments,
                                        label = "Propina repartidor",
                                        value = "$${parsedTipAmountOnScreenForRider.toInt()} MXN ¡Gracias! ❤️",
                                        primaryOrange = primaryOrange,
                                        darkTextColor = darkTextColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Fila 4: Método de Pago
                                InfoItemRow(
                                    icon = Icons.Default.CreditCard,
                                    label = "Método de Pago",
                                    value = if (ord.paymentMethod == "EFECTIVO") "Pago contra entrega (Efectivo)" else "Transferencia Bancaria",
                                    primaryOrange = primaryOrange,
                                    darkTextColor = darkTextColor
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Detalles de pago en efectivo/cambio si el método de pago es Efectivo
                                if (ord.paymentMethod == "EFECTIVO") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFFF1F0))
                                            .border(1.dp, Color(0xFFFFD1D1), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = "Pago efectivo",
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = if (ord.cashPayWith > 0.0) {
                                                    "Paga con: $${ord.cashPayWith.toInt()} MXN (Llevar cambio)"
                                                } else {
                                                    "Paga con cantidad exacta"
                                                },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD32F2F)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                }

                                HorizontalDivider(
                                    color = Color(0xFFF2ECE7),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )

                                // Fila: Título de Alimentos Ordenados
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFF4EC)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Restaurant,
                                            contentDescription = "Alimentos Ordenados",
                                            tint = primaryOrange,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = "Alimentos Ordenados:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryOrange
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Analizar alimentos y renderizar mini tarjetas
                                val parts = ord.itemsJson.split("; ").filter { it.isNotBlank() }
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    parts.forEach { part ->
                                        ProductItemRowBubble(
                                            itemText = part,
                                            primaryOrange = primaryOrange
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Scrolling bottom margin
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // Botón fijo moderno de volver al inicio en la parte inferior, diseñado como una hermosa tarjeta naranja tipo píldora
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onNavigateHome,
                colors = ButtonDefaults.buttonColors(containerColor = primaryOrange),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("back_to_menu_sticky"),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Volver a la Tienda",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// Hermosa fila de ayuda para particulares de entrega
@Composable
fun InfoItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    primaryOrange: Color,
    darkTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF4EC)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = primaryOrange,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$label: ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkTextColor
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = darkTextColor,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// Hermosas tarjetas de comida horizontales por artículo pedido, cargadas dinámicamente con una foto de alitas de Unsplash de calidad como marcador de posición
@Composable
fun ProductItemRowBubble(
    itemText: String,
    primaryOrange: Color
) {
    // URL de imagen de alitas de pollo BBQ de Unsplash
    val alitasUrl = "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&q=80&w=200"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF9F6))
            .border(1.dp, Color(0xFFF2E6DD), RoundedCornerShape(20.dp))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEAD9))
                    .border(1.dp, Color(0xFFFFD1B3), CircleShape)
            ) {
                AsyncImage(
                    model = alitasUrl,
                    contentDescription = "Comida",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = itemText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111),
                lineHeight = 16.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun WithRiderGpsSwitch(
    riderGpsActive: Boolean,
    onToggle: (Boolean) -> Unit,
    primaryOrange: Color,
    lightBorderColor: Color,
    mutedTextColor: Color
) {
    Switch(
        checked = riderGpsActive,
        onCheckedChange = onToggle,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = primaryOrange,
            uncheckedThumbColor = mutedTextColor,
            uncheckedTrackColor = lightBorderColor
        ),
        modifier = Modifier.testTag("rider_gps_switch")
    )
}

@Composable
fun OrderProgressStateRowTimeline(
    activeStatus: String,
    method: String,
    primaryOrange: Color,
    darkTextColor: Color,
    mutedTextColor: Color
) {
    val statuses = if (method == "RECOGER") {
        listOf("RECIBIDO", "PREPARANDO", "LISTO", "ENTREGADO")
    } else {
        listOf("RECIBIDO", "PREPARANDO", "LISTO", "EN_CAMINO", "ENTREGADO")
    }

    val displayNames = if (method == "RECOGER") {
        listOf("Pedido recibido", "En preparación", "Listo para recoger", "Entregado")
    } else {
        listOf("Pedido recibido", "En preparación", "Listo para salir", "En camino", "Entregado")
    }

    val icons = if (method == "RECOGER") {
        listOf(Icons.Default.Receipt, Icons.Default.DinnerDining, Icons.Default.Fastfood, Icons.Default.CheckCircle)
    } else {
        listOf(Icons.Default.Receipt, Icons.Default.DinnerDining, Icons.Default.Fastfood, Icons.Default.DeliveryDining, Icons.Default.CheckCircle)
    }

    val subtitles = if (method == "RECOGER") {
        listOf(
            "Ya tenemos tu orden. En unos momentos comenzamos a prepararla.",
            "Estamos preparando tu pedido con todo el sabor.",
            "Tu pedido está listo y esperando en el mostrador.",
            "Disfruta tu pedido. ¡Gracias por elegir Alitas Kis y Kei!"
        )
    } else {
        listOf(
            "Ya tenemos tu orden. En unos momentos comenzamos a prepararla.",
            "Estamos preparando tu pedido con todo el sabor.",
            "Tu pedido está listo y esperando al repartidor.",
            "Tus alitas ya van rumbo a tu ubicación calientitas 🔥",
            "Disfruta tu pedido. ¡Gracias por elegir Alitas Kis y Kei!"
        )
    }

    val activeIdx = statuses.indexOf(activeStatus).coerceAtLeast(0)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        for (i in statuses.indices) {
            val isPassed = i < activeIdx || (activeStatus == "ENTREGADO" && statuses[i] == "ENTREGADO")
            val isCurrent = i == activeIdx && activeStatus != "ENTREGADO"
            val isNotLast = i < statuses.size - 1

            // Solid orange line if the CURRENT step is passed, and the next step is also passed or current
            val isPassedToNext = i < activeIdx

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Nodo de progreso izquierdo y creador de línea vertical de la línea de tiempo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(26.dp)
                ) {
                    Spacer(modifier = Modifier.height(3.dp))

                    // El nodo de círculo doble o el nodo de verificación lleno se basa en el estado activo
                    if (isPassed) {
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
                    } else if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(2.dp, primaryOrange, CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(primaryOrange)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(1.5.dp, Color(0xFFCCCCCC), CircleShape)
                                .background(Color.White)
                        )
                    }

                    if (isNotLast) {
                        if (isPassedToNext) {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .width(2.5.dp)
                                    .background(primaryOrange)
                            )
                        } else {
                            // Línea de tiempo punteada
                            Canvas(
                                modifier = Modifier
                                    .weight(1f)
                                    .width(2.5.dp)
                            ) {
                                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                drawLine(
                                    color = Color(0xFFD4C8C2),
                                    start = androidx.compose.ui.geometry.Offset(size.width / 2, 0f),
                                    end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                                    pathEffect = pathEffect,
                                    strokeWidth = 2.5.dp.toPx()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Representador de icono visual del elemento izquierdo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isPassed || isCurrent) Color(0xFFFFF4EC) else Color(0xFFFCF9F8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icons[i],
                        contentDescription = null,
                        tint = if (isPassed || isCurrent) primaryOrange else Color(0xFF9E9E9E),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Títulos de encabezado e información de texto de subtítulos
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = displayNames[i],
                            fontSize = 15.sp,
                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                            color = if (isPassed || isCurrent) darkTextColor else Color(0xFF9E9E9E)
                        )

                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFEFE3))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVO",
                                    fontSize = 9.sp,
                                    color = primaryOrange,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subtitles[i],
                        fontSize = 12.sp,
                        color = if (isPassed || isCurrent) mutedTextColor else Color(0xFF9E9E9E),
                        lineHeight = 16.sp
                    )
                }
            }
        }
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

    val parsedTipAmount = run {
        try {
            val pattern = "Propina:\\s*\\$(\\d+)\\s*MXN".toRegex()
            val match = pattern.find(order.address)
            match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    val parsedScheduledDelay = run {
        try {
            val pattern = "⏰ Programado:\\s*([^|\\]]+)".toRegex()
            val match = pattern.find(order.address)
            match?.groupValues?.get(1)?.trim()
        } catch (e: Exception) {
            null
        }
    }

    val finalAddressLabel = run {
        if (order.address.contains("[")) {
            val baseAddress = order.address.substringBefore("[").trim()
            if (baseAddress.isEmpty()) "Dirección Conocida" else baseAddress
        } else {
            if (order.address.isBlank()) "No especificado" else order.address
        }
    }

    val total = subtotal + order.deliveryFee + parsedTipAmount

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

    val deliveryDetailsSection = buildString {
        append("Método: $methodLabel\n")
        append("        Municipio: $municipalityLabel\n")
        append("        Dirección: $finalAddressLabel\n")
        if (parsedScheduledDelay != null) {
            append("        Tiempo de Entrega: Programado ($parsedScheduledDelay)\n")
        } else {
            append("        Tiempo de Entrega: Lo antes posible (ASAP)\n")
        }
        append("        Distancia aprox: $kmLabel\n")
        append("        Tiempo estimado: $timeLabel")
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
        $deliveryDetailsSection

        💳 *Pago*
        Método: $payMethodLabel
        Paga con: $cashPayInfo

        💰 *Total*
        Comida: $$${subtotal.toInt()}
        Envío: $$${order.deliveryFee.toInt()}
        ${if (parsedTipAmount > 0.0) "Propina Repartidor: \$${parsedTipAmount.toInt()} MXN\n        " else ""}Total: $$${total.toInt()}

        🕒 Estado actual: $statusLabel
    """.trimIndent()
}

fun sendWhatsAppOrderText(context: Context, order: OrderEntity) {
    try {
        val formattedMsg = buildWhatsAppTicket(order)

        // Crear el intent de WhatsApp dirigido directamente a Ulices (4731841964)
        val phoneNo = "524731841964" // 52 country code for Mexico + 10 digits
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNo&text=" + Uri.encode(formattedMsg))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir WhatsApp para enviar el pedido", Toast.LENGTH_LONG).show()
    }
}
