# 🍗 Alitas Kis y Kei - Aplicación Android

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white)

Una aplicación móvil nativa y moderna para Android diseñada para gestionar pedidos de comida rápida especializada en alitas y boneless. Desarrollada con los últimos estándares de la industria FoodTech, ofreciendo una experiencia de usuario (UX) sumamente fluida, diseño responsivo y un backend en la nube controlado remotamente mediante Supabase.

---

## ✨ Características Principales

*   **Menú Interactivo y Control Dinámico de Stock (Supabase):**
    *   Sincronización en tiempo real del estado de disponibilidad de los productos usando la columna `available` de la tabla `products` en Supabase.
    *   **Producto Agotado:** Si un producto está marcado como no disponible (`available = false`), la app reduce de forma responsiva la opacidad de la card a un 55%, muestra un distintivo badge rojo suave ("Agotado por hoy"), deshabilita el botón de agregar rápidamente y bloquea el acceso al cuadro de personalización de salsas.
    *   **Resiliencia Offline Inteligente:** En caso de fallas de conexión o de acceso a la API de Supabase, la app reacciona de forma segura usando el catálogo y precios locales precargados, manteniendo todos los productos disponibles para no interrumpir las ventas habituales.
*   **Personalización Avanzada de Pedidos:**
    *   Sistema interactivo de selección de salsas mediante *Chips* táctiles con indicadores de nivel de picante (Suave, Medio, Picante).
    *   Posibilidad de agregar requerimientos especiales o notas personalizadas a cada platillo.
    *   Actualización dinámica de volumen en la barra flotante rápida.
*   **Sistema de Favoritos:** Guarda tus platillos preferidos en la memoria local (SharedPreferences) presionando el botón de corazón rápido, garantizando acceso directo instantáneo.
*   **Carrito Inteligente & Cross-Selling:**
    *   Desglose y gestión de productos añadidos al carrito.
    *   Cálculo automático de subtotales, cargos de envío/empaque y total general.
    *   Carrusel integrado de sugerencias inteligentes para incentivar compras cruzadas (por ejemplo: agregar papas fritas o waffles si se detecta un carrito con boneless solo).
*   **Seguimiento de Pedidos (Live Tracking):**
    *   Flujo temporal interactivo (Timeline) con estados en tiempo real: *Pedido recibido, En preparación, Listo para salir, En camino* y *Entregado*.
    *   Mapa interactivo simulado y panel de telemetría de ruta del repartidor en vivo.
*   **Panel de Estadísticas CRM de Ventas:**
    *   Acceso incorporado en tiempo real pulsando sobre el logotipo principal de la marca en la cabecera del menú.
    *   Carga, consolida y analiza el historial de todos los pedidos registrados en Supabase para obtener cálculos automáticos al instante sin necesidad de un servidor intermedio o backend dedicado.

---

## 🛠 Arquitectura y Stack Tecnológico

El proyecto está diseñado bajo los principios de **Clean Architecture** (acoplamiento libre) con el patrón de diseño Presentation-Data **MVVM (Model-View-ViewModel)**.

### Tecnologías Core
*   **Kotlin (100%):** Tipos estáticos seguros y sintaxis moderna.
*   **Jetpack Compose:** Interfaz de usuario declarativa para crear componentes fluidos, transiciones hermosas y renderizado óptimo.
*   **Coroutines & Flow:** Mecanismos concurrentes optimizados para el consumo y reactividad de estados asíncronos.
*   **Retrofit & OKHttp:** Motor de red optimizado para realizar las llamadas REST automáticas a Supabase (Auth headers libres).

### Persistencia y Datos
*   **Room Database:** Almacenamiento local SQLite para registrar carritos y el historial offline del cliente.
*   **Supabase Engine (REST API):** Utilizado para sincronizar el menú y centralizar la recepción remota de pedidos (`orders`) y catálogo de productos (`products`).

---

## 📂 Organización del Proyecto

El código fuente respeta la estructura modular de Android por capas:

```text
app/src/main/java/com/example/
├── data/
│   ├── local/              # Room Database, DAOs y entidades locales
│   ├── remote/             # Supabase Config, Models (DTOs), API interfaces y DataSources
│   └── FoodRepository.kt   # Origen único de la verdad (Coordina Local + Supabase)
├── ui/
│   ├── FoodViewModel.kt    # El cerebro del estado y la lógica de negocio (Stats, Pedidos, Menú)
│   ├── screens/
│   │   ├── MenuScreen.kt       # Catálogo interactivo, Detalle, Favoritos e interfaz de Stats dialog
│   │   ├── CartScreen.kt       # Carrito de compras, sugerencias y checkout
│   │   └── TrackingScreen.kt   # Seguimiento animado y canvas del mapa del Rider
│   └── theme/              # Centralización de color schemes (M3), tipografías y formas
└── MainActivity.kt         # Launcher, control del edge-to-edge y ruteador principal
```

---

## 🗄️ Supabase Backend: Base de Datos & Consultas SQL (CRM)

Si deseas visualizar el rendimiento de ventas directamente desde el editor de código SQL de **Supabase Studio** sin usar la app móvil, ejecuta este paquete de scripts en tu base de datos pública.

### 1. Tablas Requeridas en Supabase
Asegúrate de que tus tablas cuenten con las siguientes estructuras:

```sql
-- Tabla de Productos con bandera de Disponibilidad
create table if not exists public.products (
    id integer primary key,
    name text not null,
    description text,
    price double precision not null,
    image_url text,
    available boolean default true
);

-- Tabla de Pedidos / Compras
create table if not exists public.orders (
    id bigint generated by default as identity primary key,
    local_order_id text not null,
    customer_name text not null,
    customer_phone text not null,
    municipality text,
    address text,
    notes text,
    payment_method text,
    subtotal double precision not null,
    delivery_fee double precision not null,
    total double precision not null,
    status text default 'Pendiente',
    items_json jsonb not null, -- Matriz de items: [{"productName": "...", "quantity": 2, ...}]
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);
```

### 2. Consultas Directas de Análisis (DQL)

#### Pedidos de Hoy
```sql
select count(*) as total_pedidos_hoy
from public.orders
where created_at::date = current_date;
```

#### Total Vendido Hoy
```sql
select coalesce(sum(total), 0) as total_vendido_hoy
from public.orders
where created_at::date = current_date;
```

#### Ticket Promedio por Pedido
```sql
select coalesce(avg(total), 0) as ticket_promedio_hoy
from public.orders
where created_at::date = current_date;
```

#### Volumen de Pedidos por Municipio (Demanda Geográfica)
```sql
select 
    coalesce(nullif(trim(municipality), ''), 'No Especificado') as municipio,
    count(*) as total_pedidos
from public.orders
group by municipio
order by total_pedidos desc;
```

#### Productos Más Vendidos (Líderes de Ventas)
Este query analiza recursivamente la clave `items_json` para contar las porciones individuales vendidas por cada platillo:
```sql
select 
    item->>'productName' as producto,
    sum((item->>'quantity')::integer) as unidades_vendidas
from public.orders,
lateral jsonb_array_elements(items_json) as item
group by producto
order by unidades_vendidas desc;
```

---

### 3. Automatización mediante Vistas en Supabase
Para acceder a reportes resumidos con un solo `SELECT *`, crea estas vistas ejecutables en **Supabase SQL Editor**:

#### A. Vista de Resumen Diario (`daily_sales_summary`)
```sql
create or replace view public.daily_sales_summary as
select 
    created_at::date as fecha,
    count(*) as pedidos_totales,
    coalesce(sum(total), 0) as ingresos_totales,
    coalesce(avg(total), 0) as ticket_promedio
from public.orders
group by created_at::date
order by fecha desc;
```
* **Uso:** `select * from public.daily_sales_summary;`

#### B. Vista de Productos Estrella (`top_products`)
```sql
create or replace view public.top_products as
select 
    item->>'productName' as producto,
    sum((item->>'quantity')::integer) as total_unidades
from public.orders,
lateral jsonb_array_elements(items_json) as item
group by producto
order by total_unidades desc;
```
* **Uso:** `select * from public.top_products;`

---

## 🚀 Cómo Empezar

### Requisitos Técnicos
*   Instalar **[Android Studio Iguana+](https://developer.android.com/studio)** o superior.
*   Java Development Kit **(JDK) 17**.
*   Un dispositivo Android físico o virtual (EMU) con API Level 24+ para pruebas funcionales.

### Pasos de Instalación
1.  Clona este repositorio de forma local:
    ```bash
    git clone https://github.com/tu-usuario/alitas-kis-and-kei.git
    ```
2.  Abre el proyecto desde Android Studio.
3.  Ve a `settings.gradle.kts` o revisa que tu gradle Gradle Sync descargue las dependencias correctamente.
4.  Si cuentas con credenciales de Supabase de desarrollo:
    *   Crea un archivo `.env` en base a `.env.example`.
    *   Ingresa tu url `SUPABASE_URL` y tu clave anon/public key `SUPABASE_KEY` (Estas son consumidas nativamente por BuildConfig).
5.  Conecta tu dispositivo, compila e instala presionantdo **Run "app" (▶)**.

---
> Desarrollado con 🍗 para los amantes del sabor y la tecnología móvil resiliente.
