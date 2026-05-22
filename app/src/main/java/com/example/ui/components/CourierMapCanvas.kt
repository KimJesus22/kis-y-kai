package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.HoneyGold
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalTextApi::class)
@Composable
fun CourierMapCanvas(
    courierLat: Double,
    courierLng: Double,
    destLat: Double,
    destLng: Double,
    municipality: String,
    status: String,
    modifier: Modifier = Modifier
) {
    // Coordenadas de referencia
    val storeLat = 20.3705
    val storeLng = -101.0638

    // Animaciones de parpadeo para balizas e iconos
    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val outlineVal = MaterialTheme.colorScheme.outline
    val outlineVariantVal = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariantVal = MaterialTheme.colorScheme.onSurfaceVariant

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            val width = size.width
            val height = size.height

            // Ayudante de mapeo de coordenadas: mapeo de lat/lng a coordenadas de pantalla (X, Y)
            // Límites que abarcan Jaral del Progreso, Valle de Santiago y Cortazar
            val minLat = 20.3500
            val maxLat = 20.5000
            val minLng = -101.2100
            val maxLng = -100.9300

            fun mapToScreen(lat: Double, lng: Double): Offset {
                // Si las coordenadas son NaN, omitidas, por defecto o cero, usar las coordenadas de la tienda en Jaral como respaldo
                val isLatInvalid = lat.isNaN() || lat.isInfinite() || lat < 1.0
                val isLngInvalid = lng.isNaN() || lng.isInfinite() || lng > -10.0
                val safeLat = if (isLatInvalid) storeLat else lat
                val safeLng = if (isLngInvalid) storeLng else lng

                // Restricción de sujeción (clamping) para evitar errores de asignación de Skia con coordenadas extremas
                val clampedLat = safeLat.coerceIn(minLat, maxLat)
                val clampedLng = safeLng.coerceIn(minLng, maxLng)

                // Latitud: maxLat mapea a 0 (superior), minLat mapea a height (inferior)
                val y = height - ((clampedLat - minLat) / (maxLat - minLat) * height).toFloat()
                // Longitud: minLng mapea a 0 (izquierda), maxLng mapea a width (derecha)
                val x = ((clampedLng - minLng) / (maxLng - minLng) * width).toFloat()

                // Comprobación de seguridad final para evitar devolver offsets NaN/Infinitos
                val safeX = if (x.isNaN() || x.isInfinite()) 0f else x
                val safeY = if (y.isNaN() || y.isInfinite()) 0f else y

                return Offset(safeX, safeY)
            }

            val pStore = mapToScreen(storeLat, storeLng)
            val pValle = mapToScreen(20.3926, -101.1915) // Valle de Santiago
            val pCortazar = mapToScreen(20.4802, -100.9613) // Cortazar
            val pCourier = mapToScreen(courierLat, courierLng)
            val pDest = mapToScreen(destLat, destLng)

            // 1. Dibujar cuadrícula / marcas de coordenadas (cuadrícula cartográfica estética)
            for (i in 1..4) {
                val gridY = (height / 5) * i
                val gridX = (width / 5) * i
                drawLine(
                    color = outlineVariantVal.copy(alpha = 0.4f),
                    start = Offset(0f, gridY),
                    end = Offset(width, gridY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
                drawLine(
                    color = outlineVariantVal.copy(alpha = 0.4f),
                    start = Offset(gridX, 0f),
                    end = Offset(gridX, height),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            // 2. Dibujar carreteras principales
            // Carretera Jaral -> Valle de Santiago
            drawLine(
                color = outlineVal.copy(alpha = 0.35f),
                start = pStore,
                end = pValle,
                strokeWidth = 14f
            )
            drawLine(
                color = HoneyGold.copy(alpha = 0.7f),
                start = pStore,
                end = pValle,
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
            )

            // Carretera Jaral -> Cortazar
            drawLine(
                color = outlineVal.copy(alpha = 0.35f),
                start = pStore,
                end = pCortazar,
                strokeWidth = 14f
            )
            drawLine(
                color = HoneyGold.copy(alpha = 0.7f),
                start = pStore,
                end = pCortazar,
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
            )

            // 3. Dibujar rastro histórico de entrega activa
            if (status == "EN_CAMINO" || status == "ENTREGADO") {
                drawLine(
                    color = NeonPink,
                    start = pStore,
                    end = pCourier,
                    strokeWidth = 6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                )
            }

            // 4. Dibujar círculo pulsante de referencia de la tienda (landmark)
            drawCircle(
                color = NeonPink.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = pStore
            )
            drawCircle(
                color = NeonPink,
                radius = 12f,
                center = pStore
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = pStore
            )

            // 5. Dibujar punto de referencia de destino del cliente activo (landmark)
            if (municipality != "JARAL_PROGRESO") {
                drawCircle(
                    color = ElectricBlue.copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = pDest
                )
                drawCircle(
                    color = ElectricBlue,
                    radius = 12f,
                    center = pDest
                )
                drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = pDest
                )
            }

            // 6. Dibujar otros municipios si no están activos (atenuados)
            if (municipality != "VALLE_SANTIAGO") {
                drawCircle(color = onSurfaceVariantVal.copy(alpha = 0.4f), radius = 6f, center = pValle)
            }
            if (municipality != "CORTAZAR") {
                drawCircle(color = onSurfaceVariantVal.copy(alpha = 0.4f), radius = 6f, center = pCortazar)
            }

            // 7. Dibujar icono de estado del repartidor
            if (status == "EN_CAMINO") {
                drawCircle(
                    color = HoneyGold.copy(alpha = 0.3f),
                    radius = 30f,
                    center = pCourier
                )
                drawCircle(
                    color = HoneyGold,
                    radius = 11f,
                    center = pCourier
                )
                drawCircle(
                    color = CharcoalDark,
                    radius = 8f,
                    center = pCourier
                )
            }

            // Dibujar textos cartográficos
            drawText(
                textMeasurer = textMeasurer,
                text = "Kis & Kei Local (Jaral)",
                style = TextStyle(
                    color = NeonPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                topLeft = Offset(pStore.x - 60f, pStore.y + 18f)
            )

            if (municipality == "VALLE_SANTIAGO") {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "Cliente (Valle)",
                    style = TextStyle(
                        color = ElectricBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    topLeft = Offset(pValle.x - 30f, pValle.y - 35f)
                )
            } else {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "Valle de Santiago",
                    style = TextStyle(color = onSurfaceVariantVal.copy(alpha = 0.6f), fontSize = 9.sp),
                    topLeft = Offset(pValle.x - 35f, pValle.y - 25f)
                )
            }

            if (municipality == "CORTAZAR") {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "Cliente (Cortazar)",
                    style = TextStyle(
                        color = ElectricBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    topLeft = Offset(pCortazar.x - 45f, pCortazar.y - 35f)
                )
            } else {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "Cortazar",
                    style = TextStyle(color = onSurfaceVariantVal.copy(alpha = 0.6f), fontSize = 9.sp),
                    topLeft = Offset(pCortazar.x - 15f, pCortazar.y - 25f)
                )
            }

            if (status == "EN_CAMINO") {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "REPARTIDOR 🛵",
                    style = TextStyle(
                        color = HoneyGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    ),
                    topLeft = Offset(pCourier.x - 35f, pCourier.y - 30f)
                )
            }
        }

        // Superposición del tablero de estado (status board)
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "info",
                    tint = HoneyGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = when (status) {
                        "RECIBIDO" -> "Localizando repartidor..."
                        "PREPARANDO" -> "Comida en plancha"
                        "LISTO" -> "Pedido listo para entrega"
                        "EN_CAMINO" -> "Repartidor en carretera"
                        "ENTREGADO" -> "Repartidor en domicilio"
                        else -> "Ubicación disponible"
                    },
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Rosa de los vientos en la esquina inferior
        Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = "Compass",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        )
    }
}
