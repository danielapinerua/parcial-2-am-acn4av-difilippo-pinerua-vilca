# Informe Técnico de Entrega de Proyecto: Patitas Felices

**Proyecto:** "Patitas Felices" – Aplicación Móvil de Gestión y Adopción de Mascotas  
**Integrantes:** Di Filippo, Pinerua, Vilca  
**Comisión / Curso:** ACN4AV  
**Plataforma / Lenguaje:** Android Nativo (Java) | Backend: Firebase (Auth & Firestore)  

---

## 1. Introducción y Propósito del Proyecto

El presente proyecto fue desarrollado en el marco de Programación Móvil. Nace con el propósito de resolver una problemática social: conectar de forma organizada y transparente a refugios, rescatistas y adoptantes para fomentar la adopción responsable de perros y gatos rescatados.

Como equipo de desarrollo, buscamos construir una aplicación nativa en Java robusta, que no solo cumpla con una interfaz intuitiva y atractiva, sino que implemente buenas prácticas de ingeniería de software:
- Separación de responsabilidades en capas lógicas claras.
- Manejo asíncrono y concurrente de operaciones de red para evitar congelamientos de la pantalla (*Application Not Responding* - ANR).
- Optimización agresiva en el uso de memoria nativa al manipular imágenes de alta resolución.
- Control estricto de concurrencia al realizar transacciones de adopción en la nube.

---

## 2. Descripción Funcional y Flujo de la Aplicación

La aplicación se articula en torno a dos roles de usuario: **Adoptante** y **Rescatista/Publicador**.

```
[ Inicio / HomeActivity ]
       │
       ├──► [ LoginActivity / SignupActivity ] (Firebase Auth)
       │
       └──► [ Catálogo General: PetListActivity ]
                 │
                 ├── [ Filtro Perros / Gatos / Todos ]
                 │
                 ├──► [ Detalle Mascota: PetDetailActivity ]
                 │          │
                 │          └──► [ Botón Adoptar ] ──► [ ConfirmationActivity ]
                 │
                 ├──► [ Formulario Mascota: AddPetActivity / EditPetActivity ]
                 │
                 └──► [ Mis Adopciones: AdoptionsActivity ]
```

### Principales Módulos del Sistema:
1. **Presentación e Inicio (`HomeActivity`):** Pantalla de aterrizaje con la propuesta de valor (*"Adoptá, no compres"*), accesos directos por categorías y llamado a la acción editorial.
2. **Autenticación (`LoginActivity`, `SignupActivity`):** Permite registrar e iniciar sesión utilizando Firebase Authentication. Mantiene la sesión activa para evitar logins recurrentes y gestiona el cierre seguro (`btnLogout`).
3. **Catálogo y Filtrado (`PetListActivity`):** Lista las mascotas disponibles. Permite filtrar instantáneamente por especie (`DOG` o `CAT`) consultando la caché en memoria del servicio para no saturar la red.
4. **Detalle y Adopción (`PetDetailActivity`, `ConfirmationActivity`):** Ficha técnica con foto ampliada, descripción, edad, raza y estado. El botón "Adoptar" dispara una transacción atómica que cambia el estado a adoptado y asocia el usuario.
5. **Historial de Adopciones (`AdoptionsActivity`):** Pantalla donde el usuario autenticado puede consultar exclusivamente las mascotas que él mismo adoptó, mostrando etiquetas de estado destacadas.
6. **Gestión de Mascotas (`AddPetActivity`, `EditPetActivity`):** Formulario para subir o editar mascotas. Aplica validaciones de negocio: solo el usuario que creó una publicación puede editarla (`ERROR_NOT_OWNER`), y una vez adoptada queda bloqueada para modificaciones (`ERROR_PET_ADOPTED`).

---

## 3. ¿Cómo está construido el Proyecto? (Arquitectura Técnica)

Organizamos el código fuente en paquetes por responsabilidades (`package-by-layer`), logrando un desacoplamiento donde la interfaz gráfica desconoce los detalles de la base de datos o de las peticiones HTTP.

```
ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca
├── activities/       # Capa de Presentación (UI y Ciclo de Vida de Android)
├── constants/        # Enums y constantes globales de negocio (PetType)
├── DTO/              # Data Transfer Objects inmutables (PetDTO)
├── models/           # Entidades del dominio (Pet)
├── network/          # Conectividad externa y código legado (PetJsonParser)
├── repository/       # Persistencia pura en Firestore y consultas atómicas
├── service/          # Lógica de negocio, validaciones, sesión y caché
├── tasks/            # Tareas asíncronas en segundo plano (Worker Threads)
└── util/             # Procesamiento gráfico, decodificadores y mappers
```

### 3.1 Explicación de las Capas Principales

#### A. Capa de Presentación (`activities`)
Cada actividad se encarga de inflar sus vistas, capturar interacciones del usuario y renderizar estados visuales. Implementamos **Material Design 3**, respetando la barra de estado inmersiva (*Edge-to-Edge*) y adaptando los márgenes con `ViewCompat.setOnApplyWindowInsetsListener`.

> **Decisión de herencia:** Notamos que crear y editar una mascota compartían casi los mismos campos de entrada y validaciones. En lugar de duplicar código en XML y Java, creamos la clase base abstracta `PetFormActivity`, de la cual extienden `AddPetActivity` y `EditPetActivity`.

#### B. Capa de Negocio (`service / PetService`)
Funciona como el cerebro de la aplicación bajo el patrón **Singleton**.
- **Control de Sesión:** Antes de cualquier alta o modificación, valida la existencia de un usuario activo mediante `requireUid()`.
- **Caché en Memoria (`petList`):** Mantiene una lista en RAM tras la primera carga. Esto permite que los filtros por especie (`getPetsByType`) respondan de forma instantánea sin generar peticiones innecesarias a la nube.
- **Reglas de Negocio:** Valida campos obligatorios no vacíos (`ERROR_EMPTY_FIELDS`), límites de tamaño de imagen (`ERROR_IMAGE_TOO_LARGE`) y permisos de edición (`validateOwnership`).

#### C. Capa de Persistencia (`repository / PetRepository`)
Centraliza todas las lecturas y escrituras hacia **Cloud Firestore**. Se comunica con el servicio exclusivamente mediante interfaces de callback genéricas (`Callback<T>`), asegurando que no contenga ninguna dependencia visual ni reglas de interfaz.

---

## 4. Base de Datos NoSQL y Decisiones de Persistencia

Para la persistencia elegimos **Cloud Firestore**, organizando los datos en dos colecciones distintas para optimizar el rendimiento y la cuota:

```
Cloud Firestore
├── pets (Colección principal)
│    └── {petId} ──► { name, breed, age, type, description,
│                      adopted, adoptedBy, adoptedAt, createdAt,
│                      createdBy, imageVersion }
│
└── pet_images (Colección complementaria)
     └── {petId} ──► { imageData: "Base64...", updatedBy }
```

### ¿Por qué dividimos las colecciones?
1. **Ahorro de ancho de banda:** Al solicitar el catálogo general (`fetchAll`), el dispositivo solo descarga texto plano de la colección `pets`. Si guardáramos las fotos en el mismo documento, cada carga de lista consumiría decenas de megabytes.
2. **Escrituras Atómicas con `WriteBatch`:** Al dar de alta o modificar una mascota con foto, utilizamos `db.batch()` para escribir en `pets` y en `pet_images` al mismo tiempo. Si alguna falla, ninguna se aplica, manteniendo la integridad referencial.
3. **Prevención de Condiciones de Carrera (`runTransaction`):** Para evitar que dos usuarios adopten la misma mascota al mismo tiempo, el método `adopt()` ejecuta una transacción atómica en el servidor. Si el campo `adopted` ya está en `true`, la transacción aborta y notifica al cliente con el mensaje de error correspondiente.

---

## 5. Manejo de Imágenes y Rendimiento (Memoria RAM)

Uno de los mayores desafíos en Android es el consumo de memoria al cargar fotos tomadas por cámaras modernas (que pueden pesar entre 5 y 15 MB). Para prevenir errores de memoria (*OutOfMemoryError*), implementamos una estrategia en dos clases clave:

### 5.1 Pipeline de Compresión Local (`PetImageCodec`)
Antes de subir una imagen seleccionada de la galería:
- **Inspección de Dimensiones:** Usamos `inJustDecodeBounds = true` para conocer el ancho y alto del archivo sin cargarlo en memoria.
- **Submuestreo Dinámico:** Reducimos la imagen a un máximo de 800 píxeles (`MAX_IMAGE_DIMENSION_PX`).
- **Compresión Adaptativa:** Fijamos una compresión inicial JPEG al 70%. Si el peso supera el tope de 200 KB (`MAX_IMAGE_BYTES`), un bucle `while` reduce la calidad en pasos de a 10 hasta alcanzar un tamaño seguro.
- **Liberación de Memoria Nativa:** Invocamos explícitamente `bitmap.recycle()` tan pronto terminan las operaciones, devolviendo la memoria gráfica al sistema.

### 5.2 Descarga Asíncrona en Segundo Plano (`ImageDownloadTask`)
Para imágenes que provienen de URLs remotas:
- Se utiliza un `AsyncTask` dedicado que ejecuta la llamada HTTP en el hilo de fondo (`doInBackground`) mediante `HttpURLConnection`, con timeouts definidos de conexión (10s) y lectura (15s).
- La decodificación en memoria se ajusta al ancho exacto del componente destino (`targetWidthPx`), y el resultado se inyecta en el hilo principal de la UI (`onPostExecute`).

---

## 6. Evolución del Proyecto y Código Legado

Al revisar la estructura del paquete `network/`, se encuentra la clase `PetJsonParser.java` marcada con el comentario `//SIN USO`.

**Justificación técnica para el docente:**  
En la etapa inicial del trabajo práctico, el sistema fue planteado para consumir una API REST tradicional que devolvía un array JSON (`"pets"`). Al avanzar hacia una solución completa con autenticación por usuario, control de permisos de edición y necesidad de concurrencia en tiempo real para las adopciones, decidimos migrar la persistencia a **Firebase (Auth + Firestore)**. Se preservó dicha clase en el repositorio como evidencia del proceso evolutivo del proyecto.

---

## 7. Interfaz Gráfica, Estilos y Accesibilidad

Se diseñó la interfaz siguiendo las pautas de **Material Design 3 (M3)**, cuidando la experiencia de usuario y la accesibilidad:

- **Tokens de Color Centralizados (`colors.xml`):**
  - Color distintivo de marca (`accent`): `#7C5CFC` (violeta moderno).
  - Fondo cálido de contraste (`background`): `#FDFCFB`.
  - Superficies de tarjeta (`surface`): `#FFFFFF`.
  - Estados exitosos (`success`): `#388E3C`.
- **Métricas Estandarizadas (`dimens.xml`):** Basadas en la cuadrícula canónica de 8dp (`margin_small`, `margin_medium`, `margin_large`) y curvatura de esquinas uniforme de 12dp en tarjetas.
- **Internacionalización y Accesibilidad (`strings.xml`):** Inexistencia de cadenas literales hardcodeadas en código Java. Todos los botones e imágenes incorporan atributos `contentDescription` (`logo_description`, `pet_image_description`, `confirmation_image_description`) para compatibilidad con lectores de pantalla (TalkBack).

---

## 8. Conclusiones y Aprendizajes

El desarrollo de **Patitas Felices** permitió integrar de manera práctica los conceptos fundamentales de la materia:
1. **Ciclo de vida y componentes Android:** Comprensión profunda del manejo de actividades, transiciones con flags de navegación (`FLAG_ACTIVITY_REORDER_TO_FRONT`, `CLEAR_TASK`) y enlace con layouts.
2. **Programación asíncrona:** Separación del hilo de interfaz de usuario respecto al cómputo pesado (red, IO en disco y renderizado gráfico).
3. **Arquitectura y patrones:** Implementación efectiva de Singleton, DTO, Repository, Service y Mappers para mantener un proyecto ordenado y escalable.
4. **Seguridad y bases de datos NoSQL:** Modelado documental eficiente y aplicación de transacciones atómicas para garantizar la consistencia de los datos en la nube.