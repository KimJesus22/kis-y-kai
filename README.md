# 🍗 Alitas Kis y Kei - Aplicación Android

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)

Una aplicación móvil moderna y nativa para Android, diseñada para gestionar pedidos de comida rápida especializada en alitas y boneless. Desarrollada con los últimos estándares de la industria, ofreciendo una experiencia de usuario (UX) fluida, diseño responsivo y un seguimiento interactivo de pedidos.

## ✨ Características Principales

* **Menú Interactivo:** Navegación fluida entre productos con descripciones claras, precios y etiquetas de promoción.
* **Personalización Avanzada de Pedidos:** 
  * Sistema de selección de salsas mediante *Chips* con indicadores visuales divertidos de nivel de picante (Suave, Medio, Picante).
  * Posibilidad de agregar requerimientos especiales o notas a cada producto.
  * Ajuste dinámico de cantidades con actualización en tiempo real en la vista rápida.
* **Sistema de Favoritos:** Guarda tus platillos preferidos en la memoria local del dispositivo mediante un solo clic en el menú, para un acceso hiper-rápido en tus futuros antojos.
* **Carrito Inteligente:**
  * Gestión completa de los elementos listos para ser ordenados.
  * Cálculo algorítmico y automático de subtotales.
  * Sistema de sugerencias (Cross-selling) automático basado en el contenido de tu carrito para potenciar combos.
* **Seguimiento de Pedidos (Live Tracking):**
  * Flujo temporal interactivo (Timeline) con copys y estados amigables: *Pedido recibido, En preparación, Listo para salir, En camino* y *Entregado*.
  * Mapa y telemetría (módulos visuales/simulados) para dar visibilidad al progreso logístico o de preparación en tienda.
* **Modos de Adquisición:** Soporte bifurcado para Delivery puro ("Envío") y Pickup ("Recoger en tienda").

## 🛠 Arquitectura y Stack Tecnológico

El proyecto está construido bajo los principios de *Clean Architecture* con acoplamiento suelto, e implementa el patrón **MVVM (Model-View-ViewModel)**.

### Tecnologías Core
* **100% Kotlin:** Código resiliente apoyado en el sistema de tipos seguros de Kotlin.
* **Jetpack Compose:** El toolkit declarativo de Google para la construcción de la UI, permitiendo construir módulos limpios y responsivos.
* **Coroutines & Flow:** Mecanismos eficientes de multi-threading para el manejo de estados reactivos en tiempo real.
* **ViewModel & StateFlow:** Gestión eficiente del estado de la UI persistiendo inyecciones ante cambios de configuración de Android.

### Modelo de Datos (Data y Persistencia)
* **Room Database:** Implementación avanzada y eficiente sobre SQLite para almacenar localmente los historiales de pedidos, estatus del tracking y el esqueleto de tu carrito.
* **SharedPreferences:** Empleadas estratégicamente para manejar operaciones de tipo L1 Caché relativas a preferencias veloces y la indexación de "Favoritos" del menú.

### UI / UX Visual
* **Material Design 3 (M3):** Elementos de diseño curados, con un uso intensivo de *Shape schemes*, *Cards* elevadas y combinaciones de colores vibrantes enfocadas en *FoodTech*.
* **Tipos y Fuentes Custom:** Integración de pesos tipográficos para alta jerarquía y fácil escaneo.

## 📂 Organización Estructural

El código fuente respeta la estructura por dominios del frontend:

```text
app/src/main/java/com/example/
├── data/
│   ├── local/              # Base de Datos local Room, DAOs y Endpoints de persistencia
│   └── FoodRepository.kt   # Interfaz de dominio e Invocación (Single Source of Truth)
├── ui/
│   ├── state/              # Modelos de dominio y Data Classes (Ej. Product)
│   ├── FoodViewModel.kt    # Manejador del Brain/State principal del frontend 
│   ├── screens/
│   │   ├── MenuScreen.kt       # Componente Raíz (Productos, Filtros y Favoritos)
│   │   ├── CartScreen.kt       # Pantalla Check-out y motor de Sugerencias
│   │   └── TrackingScreen.kt   # Animaciones de envío y Canvas Map Dashboard
│   └── theme/              # Variables CSS-like (Color, Type, Shape, Theme)
└── MainActivity.kt         # Launcher, Routing Node y Configuración de Sistema base
```

## 🚀 Cómo Empezar

### Requisitos Técnicos
* Instalar **[Android Studio](https://developer.android.com/studio)** (Se recomienda *Iguana* o la versión estable más reciente).
* Java Development Kit **(JDK) 17**.
* Un entorno de debugueo (Dispositivo físico configurado por ADB) o un Emulador SDK API 24+.

### Pasos de Instalación
1. Clona este repositorio localmente usando tu terminal:
   ```bash
   git clone https://github.com/tu-username/alitas-kis-and-kei.git
   ```
2. Ejecuta Android Studio y selecciona `File -> Open`, apuntando al directorio raíz del proyecto.
3. Permite que el sistema **Gradle Maven** indexe y descargue la librería de dependencias de Compose y Room.
4. Despliega un Android Virtual Device (AVD).
5. Compila e instala clickeando en la opción **Run "app" (▶)** situada en la navbar.

## 🎯 Próximos Pasos (Roadmap)
- [ ] Integración de un SDK gateway para permitir micropagos (Ej. MercadoPago / Stripe).
- [ ] Migrar el mock de local DB a una instancia BaaS mediante **Firebase Firestore** o Supabase (Cloud Database).
- [ ] Sumar Autenticación de usuario con **Google Sign-In API**.
- [ ] Adicionar Google Maps SDK nativo para trackeos asíncronos reales del rider.

---
> Desarrollado con 🍗 para los más grandes fanáticos del pollo.
