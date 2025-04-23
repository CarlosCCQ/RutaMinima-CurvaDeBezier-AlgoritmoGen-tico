# Ruta mínima, curva de Bézier y Backend (Hugging Face Space)

Este documento proporciona una descripción técnica detallada de los componentes de la aplicación Android y el servicio backend implementado en Hugging Face Space, que colaboran para resolver un problema de optimización de ruta (similar al Problema del Viajante de Comercio - TSP) y visualizar el resultado.

## 1. Aplicación Android

La aplicación Android permite al usuario interactuar con una interfaz gráfica para definir una serie de puntos y luego solicitar al backend el cálculo de una ruta óptima entre ellos.

### 1.1. `MainActivity.kt`

Este archivo contiene la clase principal de la actividad que gestiona la interfaz de usuario y la lógica de interacción con el usuario y el backend.

- `MainActivity : AppCompatActivity()`  
  Declara la clase principal que hereda de `AppCompatActivity`, proporcionando la funcionalidad base para una actividad de Android compatible con versiones anteriores.
- `binding: ActivityMainBinding`  
  Instancia de la clase de binding generada automáticamente por View Binding, utilizada para acceder a los elementos de la interfaz de usuario de forma segura y eficiente.
- `puntos = mutableListOf<Punto>()`  
  Lista mutable que almacena objetos `Punto`, representando las coordenadas (x, y) donde el usuario toca la pantalla.
- `rutaOptima = listOf<Int>()`  
  Lista que almacenará la secuencia de índices de los puntos que conforman la ruta óptima calculada por el backend.

**Funciones clave:**

- `onCreate(savedInstanceState: Bundle?)`: Inicializa vista, View Binding, modo edgeToEdge e interacción con UI.
- `imageView.setOnTouchListener`: Detecta toques, añade puntos y dibuja en `Canvas`.
- `binding.btnaccion.setOnClickListener`: Prepara datos para backend, envía solicitud con Retrofit, y procesa respuesta.
- `arregloPuntos(puntos: List<Punto>): IntArray`: Convierte lista de puntos en array de enteros.
- `dibujarRuta(canvas: Canvas, paint: Paint)`: Dibuja puntos y líneas entre ellos según `rutaOptima`.
- `dibujarBezierGeneral(canvas: Canvas, ruta: List<Punto>)`: Dibuja curva de Bézier que pasa por puntos.
- `calcularBezier(puntos: List<Punto>, t: Float): Punto`: Calcula un punto en curva de Bézier con algoritmo de De Casteljau.

### 1.2. `Modelo.kt`

Define las estructuras de datos utilizadas para la comunicación con la API del backend.

```kotlin
data class RequestData(val data: List<Float>, val data2: IntArray)
data class ResponseData(val prediction: List<Int>)
```

### 1.3. `Punto.kt`

Una clase simple para representar un punto en un espacio 2D.

```kotlin
class Punto(var x: Int, var y: Int)
```

Define una clase con dos propiedades mutables: `x` e `y`, ambas de tipo entero.

### 1.4. `RetrofitRuta.kt`

Configura la instancia de Retrofit para la comunicación con el backend.

```kotlin
object RetrofitRuta {
    private const val BASE_URL = "https://carlosccq-rutamascortaybezier.hf.space/"
    val api: RutaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RutaApi::class.java)
    }
}
```

### 1.5. `RutaApi.kt`

Define la interfaz para la API del backend utilizando Retrofit.

```kotlin
interface RutaApi {
    @POST("/predict/")
    fun predict(@Body request: RequestData): Call<ResponseData>
}
```

## 2. Backend (Hugging Face Space)

El backend es un servicio web implementado en Python que recibe los puntos del frontend, aplica un algoritmo genético para encontrar una ruta óptima y devuelve el orden de los puntos en esa ruta.

### 2.1. Tecnologías Utilizadas

- FastAPI
- Pydantic
- Uvicorn

### 2.2. `app.py`

Este archivo contiene la lógica principal del backend y la implementación del algoritmo genético para resolver el problema de la ruta óptima.

#### Funciones principales:

- **FastAPI App:** Se instancia una aplicación FastAPI (`app = FastAPI()`).
- **Modelos de Datos:**
  - `RequestData`: Modelo de entrada con parámetros del algoritmo y coordenadas de puntos.
  - `ResponseData`: Modelo de salida con la ruta óptima calculada.
- **Algoritmo Genético:**
  - `Individuo`: Representa una posible solución (ruta) y su distancia total.
  - `generar_población`: Genera una población inicial aleatoria de rutas.
  - `cruzar`: Realiza cruce entre dos rutas (crossover).
  - `mutar`: Aplica mutación aleatoria a una ruta con cierta probabilidad.
- **Endpoint `/predict/`:**
  - Recibe parámetros y puntos.
  - Ejecuta el algoritmo genético.
  - Devuelve la mejor ruta encontrada como una lista de índices de puntos.

### 2.3. `Dockerfile`

El `Dockerfile` define los pasos para crear un contenedor Docker capaz de ejecutar el backend en Hugging Face Spaces.

### 2.4. `requirements.txt`

Este archivo lista las dependencias necesarias para ejecutar la API.

```text
fastapi
uvicorn
pydantic
```

#### Funciones:

- **fastapi:** Framework para definir endpoints y lógica de negocio.
- **uvicorn:** Servidor ASGI ligero y rápido para aplicaciones FastAPI.
- **pydantic:** Validación de datos basada en tipos para los modelos de entrada/salida.

## 3. Flujo General del Sistema

1. El usuario define puntos en la app Android.
2. Se envía una solicitud HTTP POST al backend.
3. FastAPI procesa la solicitud y ejecuta un algoritmo genético.
4. El backend responde con el orden óptimo de puntos.
5. La app dibuja la ruta óptima con líneas y curva de Bézier.

