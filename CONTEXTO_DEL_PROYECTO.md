# CONTEXTO DEL PROYECTO: Alitas Kis y Kei (Rastreo en Vivo)

Este documento sirve como contexto técnico completo para **ChatGPT** (u otros modelos de IA) con el fin de adaptar la interfaz de usuario (UI) al estilo/diseño temático de **Stitch** sin alterar el flujo del carrito de compras, la base de datos local Room ni la simulación matemática de geolocalización o el rastreo GPS en vivo del repartidor.

---

## 🚀 CÓMO OBTENER EL CÓDIGO FUENTE REAL DEL PROYECTO
> **NOTA PARA EL USUARIO:** Dado que el código completo de la app es muy extenso, **no intentes copiarlo y pegarlo manualmente** si ChatGPT te lo pide. En su lugar, ve a la esquina superior derecha en **Google AI Studio**, da clic en los ajustes o menú de descarga, y selecciona **Exportar Proyecto como ZIP** (Export as ZIP). De esta forma podrás subir directamente los archivos `.kt` a ChatGPT para que aplique los cambios con precisión quirúrgica.

---

## 1. 📁 ESTRUCTURA ACTUAL DE ARCHIVOS IMPORTANTES

### 📦 Capa de Datos (Data Layer)
*   `com.example.data.DatabaseFiles.kt`:
    *   **Entidades:** `CartItemEntity` (id autogenerado, productId, productName, price, quantity, sauce, note) para el carrito. `OrderEntity` (orderId, itemsJson, customerName, phone, deliveryMethod, municipality, address, paymentMethod, cashPayWith, status, timestamp, deliveryFee, distanceKm, estimatedTimeMinutes, currentCourierLat, currentCourierLng, destinationLat, destinationLng) para los pedidos generados.
    *   **DAOs:** `CartDao` (Room para consultar, insertar, actualizar, eliminar y vaciar el carrito). `OrderDao` (Room para consultar pedidos por ID y actualizar el estado o coordenadas del repartidor).
    *   **Base de Datos:** `AppDatabase` (RoomDatabase, versión 1, destructiva al migrar).
*   `com.example.data.FoodRepository.kt`:
    *   **Modelos de Kotlin:** `Product` y `CartItem`.
    *   **Lista Estática de Productos:** `menuProducts` (contiene los 4 productos reales de la cartela con sus precios e indicaciones de si manejan salsas).
    *   **Cálculos de geolocalización:** Implementación de la fórmula de *Haversine* (`calculateHaversineDistance`) multiplicada por un factor de ruteo físico de `1.28` para calcular la distancia real por carreteras entre la Tienda Central (Jaral del Progreso) y los municipios de entrega (Valle de Santiago y Cortazar). Calcula tarifas de envío (`deliveryFee`) redondeadas matemáticamente a múltiplos de $5 pesos, tiempo de tránsito o cocina.

### 📦 Capa de Interfaz y Lógica (UI & ViewModel Layer)
*   `com.example.MainActivity.kt`: Instancia de `NavHost` central. Expone la navegación Compose entre tres rutas literales fijas: `"menu"`, `"cart"`, y `"tracking"`. Inicializa el `FoodViewModel` usando un factory personalizado.
*   `com.example.ui.FoodViewModel.kt`: Centraliza todo el estado reactivo del negocio:
    *   `cartItems`: StateFlow reactivo alimentado desde la base de datos Room.
    *   `deliveryFeeAndDetails`: StateFlow dinámico que combina el método de entrega ("ENVIO"/"RECOGER") y el municipio de entrega ("VALLE_SANTIAGO"/"CORTAZAR") para actualizar el costo y tiempos estimados inmediatamente según el formulario.
    *   `activeOrder`: Flujo reactivo del pedido en rastreo actual.
    *   *Simulación:* Motor asíncrono Coroutines (`startTrackingSimulation`) que recrea las etapas del pedido (Recibido -> Preparando (10s) -> Listo (12s) -> En Camino (6s) -> Entregado). Si es envío, interpola matemáticamente las coordenadas del repartidor en 8 tramos de 5 segundos desde Jaral hasta el destino exacto.
    *   *GPS Real:* Integración real con `LocationManager` (`toggleRiderGps`). Cuando se activa el modo repartidor, suspende la simulación y lee el GPS físico del dispositivo para actualizar la ubicación del repartidor en la base de datos en tiempo real.
*   `com.example.ui.NotificationHelper.kt`: Lanza notificaciones push reales del sistema operativo utilizando canales de notificación para simular alertas prioritarias cuando el estatus cambia.

### 🎨 Capa de Presentación (Screens & Styling)
*   `com.example.ui.theme.Color.kt` y `Theme.kt`: Paleta de colores basados en Material Theme 3. El esquema actual utiliza `NeonPink` (Primario), `HoneyGold` (Secundario), `ElectricBlue` (Terciario/Información), `ObsidianCard` (Cards) y colores de alerta según el estado.
*   `com.example.ui.screens.MenuScreen.kt`:
    *   Encabezado de marca degradado.
    *   Banner promocional dinámico.
    *   Lista scrolleable `LazyColumn` que genera los items del menú.
    *   `FoodCustomizerDialog`: Diálogo modal que se abre al tocar un producto. Permite seleccionar cantidad (+/-), nota especial, y salsa (BBQ, Búfalo, Mango Habanero, Lemon Pepper, Natural). Agrega el item a la BD.
    *   Menú flotante o sticky inferior para ingresar al Carrito (muestra badge rojo del conteo de piezas) o ver pedido en rastreo activo.
    *   Diálogo flotante con el historial de alertas push de pedidos.
*   `com.example.ui.screens.CartScreen.kt`:
    *   Lista de productos agregados con opción de eliminar.
    *   Selector visual bilingüe (Envío a Domicilio / Recoger en Tienda).
    *   Campos de formulario: Nombre del cliente, teléfono/WhatsApp.
    *   Si es Envío: Selector estricto de municipio (Valle de Santiago / Cortazar) y campo para la dirección de entrega. Despliega un panel informativo con el cálculo preciso de la distancia calculada y el tiempo aproximado.
    *   Si es Recogida: Panel con la ubicación física de la sucursal de Jaral del Progreso.
    *   Método de Pago: Efectivo contra entrega (pide con qué billete pagará) o Transferencia bancaria (muestra datos CLABE BBVA con nota de envío de comprobante).
    *   Desglose del Subtotal de comida, Cargo de envío y Total estimado. Button sticky inferior para confirmar el pedido (limpia el carrito e inicia el seguimiento).
*   `com.example.ui.screens.TrackingScreen.kt`:
    *   Contiene el lienzo cartográfico interactivo `CourierMapCanvas`.
    *   Módulo repartidor: Switch para transmitir coordenadas de GPS reales del móvil actual en el mapa en vivo.
    *   Cronología o historial del estado ("Progreso del Pedido") adaptando descripciones detalladas con base en "Recibido", "En Plancha (Cocina)", "Listo", "En Camino" y "Entregado".
    *   Botón para compartir/enviar el ticket formateado con emojis directamente al WhatsApp del comercio.
    *   Botón depurador "Acelerar ⚡" para pruebas rápidas de transiciones de estados de simulación.
*   `com.example.ui.components.CourierMapCanvas.kt`:
    *   Un lienzo personalizado estructurado con `Native DrawScope Canvas` de Compose.
    *   Mapea matemáticamente coordenadas de latitudes y longitudes geográficas `[20.35, 20.50]` y lo traduce a pixeles en pantalla adaptándose a cualquier ancho/alto de móvil.
    *   Dibuja retículas de mapas, las carreteras de comunicación que enlazan Jaral con Valle de Santiago y Cortazar, trazados de ruta, un marcador de pulso brillante para la tienda de origen, la zona del destinatario y una moto/repartidor deslizándose de manera fluida y responsiva.

---

## 2. ⚙️ CÓMO FUNCIONA ACTUALMENTE LA APP

1.  **Navegación:** Controlada estrictamente por `androidx.navigation.compose`. Se inicia en `"menu"`, el botón inferior te dirige a `"cart"` y al completar los campos de compra y presionar *Confirmar*, se vacía el carrito en primer plano, se crea la orden en la BD y se navega a `"tracking"` limpiando la pila intermedia para que al dar click atrás regrese al menú principal.
2.  **Lógica del Carrito:** Todo corre sobre Room. Si ingresas dos productos idénticos con la misma salsa, el sistema suma la cantidad en el mismo item reactivamente sin duplicar filas incómodas.
3.  **Cálculos de Precios/Envío:** El precio base de envío de Valle de Santiago es de `$35` y Cortazar `$45`. Se le suma `$1.2 pesos por kilómetro` de carretera estimado y se redondea hacia arriba en múltiplos de `$5 pesos` para simplificar el cobro físico en mostrador.
4.  **Rastreo Activo:** La sincronización con el repartidor tiene doble entrada:
    *   *Simulación Automática:* Si no tienes permisos de geolocalización o estás en emulador, corre un temporizador asíncrono que avanza las etapas y recalcula posiciones estimadas progresivamente.
    *   *GPS Físico (Rider Mode):* Si un repartidor real abre su pedido en el Tracking Screen, activa el switch, concede permisos de ubicación, la simulación artificial se detiene del todo y el dispositivo comienza a mandar coordenadas GPS verdaderas para telemetría interactiva móvil-a-móvil.

---

## 3. 🍔 NUESTROS 4 PRODUCTOS ACTUALES REALES
*(Están definidos estáticamente en `FoodRepository.kt`. ChatGPT **solo** debe usar este catálogo limitado, jamás inventar nuevos o colocar ítems de relleno en la maqueta de Stitch).*

1.  **Alitas con papas (8 Pzas)**
    *   **Precio:** $120.00 MXN
    *   **Descripción:** "Crujientes alitas marinadas con acompañamiento de papas a la francesa."
    *   **Sauces:** Sí maneja salsas.
    *   **Identificador en Código:** `"alitas_papas"`
    *   **Emoji/Icono Actual:** `"🪶🍟"` (en `MenuScreen.kt` mediante comprobación condicional de ID).

2.  **Boneless (8 Pzas)**
    *   **Precio:** $100.00 MXN
    *   **Descripción:** "Trocitos de pechuga de pollo empanizados y bañados en tu salsa favorita."
    *   **Sauces:** Sí maneja salsas.
    *   **Identificador en Código:** `"boneless"`
    *   **Emoji/Icono Actual:** `"🍗"`

3.  **Boneless c/papas (8 Pzas)**
    *   **Precio:** $120.00 MXN
    *   **Descripción:** "Boneless premium acompañados de nuestras papas fritas sazonadas."
    *   **Sauces:** Sí maneja salsas.
    *   **Identificador en Código:** `"boneless_papas"`
    *   **Emoji/Icono Actual:** `"🍗🍟"`

4.  **Orden de papas**
    *   **Precio:** $35.00 MXN
    *   **Descripción:** "Deliciosa porción de papas a la francesa, doraditas y sazonadas."
    *   **Sauces:** No maneja salsas (se asume Natural).
    *   **Identificador en Código:** `"papas_orden"`
    *   **Emoji/Icono Actual:** `"🍟"`

---

## ⚠️ 4. DIAGNÓSTICO DE CUESTIONES TÉCNICAS E IMPORTS

*   **¿Compila la App?** **Sí.** El proyecto compila al 100% libre de errores sintácticos, faltas de llaves o imports.
*   **Lógica Cartográfica Intocable:** En `CourierMapCanvas.kt`, las variables `storeLat`, `storeLng`, `minLat`, `maxLat`, `minLng`, `maxLng` y las coordenadas de Valle de Santiago (`20.3926`, `-101.1915`) y Cortazar (`20.4802`, `-100.9613`) **son físicas reales y necesarias para trazar la retícula** del lienzo. El modelo que adapte la UI no debe eliminar o modificar esta matriz matemática o el convertidor `mapToScreen`, de lo contrario el repartidor dejará de graficarse o provocará desbordamientos de Skia.

---

## 👽 5. OBJETIVO FINAL: Adaptación Estilo "Stitch"

Queremos redecorar estéticamente toda la interfaz para darle una experiencia alegre, moderna inspirada en **Stitch (Lilo & Stitch)**.

### 🎨 Directrices para ChatGPT:
1.  **Colores Sugeridos (`Color.kt` / `Theme.kt`):**
    *   Sustituir el morado `NeonPink` por un azul Stitch vibrante y tecnológico (ej. `#0C5C9E` o azul cerúleo hawaiano).
    *   Usar acentos azules claros o espaciales (`#42A5F5` o `#E0F7FA`).
    *   Sustituir el amarillo `HoneyGold` por un rosa hawaiano/hibisco alegre o amarillo arena cálido.
    *   Cambiar los fondos por oscuros ambientados con degradados espaciales, o un tema claro sumamente limpio con bordes redondeados orgánicos y fluidos.
2.  **Tipografías y bordes:** Formas muy redondeadas en botones y tarjetas de productos (ej. `RoundedCornerShape(24.dp)` o esquinas asimétricas hawaianas) dándole un toque juguetón pero pulido.
3.  **No inventar datos:** Trabajar únicamente modificando la maquetación Compose de `MenuScreen.kt`, `CartScreen.kt`, `TrackingScreen.kt`, `Color.kt` y `Theme.kt`.
4.  **Respetar la navegación:** El enrutador (`NavHost` en `MainActivity.kt`) y la inyección del ViewModel con su base de datos Room no se deben reconstruir. Las modificaciones deben ser archivo por archivo.
