# Alarma rápida — Proyecto Android completo

Proyecto Kotlin + Jetpack Compose + Material 3 + MVVM + Room + AlarmManager.
Compatible con Android 10 (API 29) hasta Android 15 (API 35), probado en mente para Redmi Note 15 Pro 5G (HyperOS).

## Por qué no hay un .apk ya generado
Este proyecto se escribió en un entorno sin Android SDK ni conexión a internet, así que
no fue posible compilar el APK aquí. Debes compilarlo tú una sola vez con Android Studio
(gratis, 10-15 minutos). Después de instalarlo, la app funciona 100% offline.

## Opción A (recomendada si no quieres instalar Android Studio): compilar el APK gratis en GitHub

Este proyecto ya incluye un archivo `.github/workflows/build.yml` que compila el APK
automáticamente en los servidores de GitHub cada vez que subes el código. Tú no
instalas nada, no programas nada.

1. Crea una cuenta gratis en https://github.com (si no tienes una).
2. Arriba a la derecha → **+** → **New repository**. Ponle un nombre (ej. `alarma-rapida`),
   déjalo en **Public** o **Private**, no marques ninguna opción extra → **Create repository**.
3. En la página del repo vacío, busca el enlace **uploading an existing file**.
4. Descomprime el `.zip` de este proyecto en tu computadora. Arrastra **todo el contenido**
   de la carpeta `AlarmApp` (no la carpeta en sí, sino lo que hay dentro: `app`, `gradle`,
   `.github`, `build.gradle.kts`, etc.) a la ventana de GitHub para subirlo.
   - Si tu navegador no te deja arrastrar carpetas completas, instala **GitHub Desktop**
     (https://desktop.github.com), abre el proyecto, y usa "Publish repository" — es más
     confiable para subir carpetas completas con subcarpetas.
5. Escribe un mensaje de commit (ej. "primer envío") → **Commit changes**.
6. Ve a la pestaña **Actions** del repositorio. Verás un workflow llamado
   **"Compilar APK"** ejecutándose automáticamente (círculo amarillo girando).
   Tarda entre 5 y 10 minutos la primera vez.
7. Cuando termine (✅ verde), entra a esa ejecución → baja hasta **Artifacts** →
   descarga **AlarmaRapida-APK** (es un .zip que contiene el `.apk` adentro).
8. Descomprime ese .zip en tu computadora, obtienes `app-debug.apk`.
9. Envía ese `.apk` a tu Redmi Note 15 Pro (por cable, Google Drive, Telegram a ti mismo, etc.),
   ábrelo en el teléfono y toca **Instalar** (activa "Instalar apps de origen desconocido"
   para la app que uses para abrirlo, ej. Archivos o Chrome).

Con esto obtienes un `.apk` real generado en la nube, sin instalar Android Studio.

## Opción B: compilar localmente con Android Studio

1. Instala **Android Studio** (la versión más reciente): https://developer.android.com/studio
2. Abre Android Studio → **Open** → selecciona la carpeta `AlarmApp` (la que contiene `settings.gradle.kts`).
3. Espera el **Gradle Sync** (barra de progreso abajo). La primera vez descarga dependencias, necesita internet.
   - Si te pregunta por el Gradle Wrapper, deja que Android Studio lo cree automáticamente.
4. Conecta tu Redmi Note 15 Pro por USB con "Depuración USB" activada
   (Ajustes > Acerca del teléfono > toca 7 veces "Versión de MIUI/HyperOS" para activar Opciones de desarrollador,
   luego Ajustes > Opciones de desarrollador > Depuración USB).
5. Arriba, selecciona tu teléfono en el menú de dispositivos y pulsa el botón ▶ (Run).
   - Esto instala y abre la app directamente.
6. Para generar el archivo `.apk` instalable manualmente:
   **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
   Al terminar aparece un enlace "locate" → el archivo está en
   `app/build/outputs/apk/debug/app-debug.apk`.
7. Copia ese `.apk` a tu teléfono (cable, Drive, etc.) y ábrelo para instalarlo
   (activa "Instalar apps desconocidas" para el explorador de archivos que uses).

## Configuración necesaria en tu Redmi Note 15 Pro (HyperOS) para que las alarmas nunca fallen

HyperOS/MIUI mata procesos en segundo plano de forma agresiva. Dentro de la app, en
**Configuración → Permisos y batería**, toca:

- **Permitir alarmas exactas** (obligatorio, Android 12+).
- **Ignorar optimización de batería** (evita que HyperOS cierre la app).

Además, manualmente en el sistema:

1. Ajustes del teléfono → Apps → Administrar apps → Alarma rápida → **Batería** → selecciona
   **Sin restricciones**.
2. En la misma pantalla → **Inicio automático** → actívalo.
3. Ajustes → Notificaciones → Alarma rápida → activa todas las notificaciones
   (necesario para el permiso `POST_NOTIFICATIONS` y para la pantalla completa).
4. Si usas bloqueo de apps/pantalla de bloqueo con seguridad reforzada, agrega la app
   a la lista blanca en Seguridad → Bloqueo de apps.

Sin estos pasos, HyperOS puede "optimizar" (cerrar) la app antes de que suene la alarma.

## Estructura del proyecto

```
AlarmApp/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── kotlin/com/miapp/alarmas/
│   │   │   ├── AlarmApp.kt              (Application)
│   │   │   ├── data/                    (Room, DataStore, Repositorios)
│   │   │   ├── alarm/                   (AlarmManager, Receivers, Foreground Service, Notificaciones)
│   │   │   ├── ui/                      (Compose: lista, crear/editar, configuración, pantalla sonando)
│   │   │   └── util/                    (Tiempos, permisos)
│   │   └── res/                         (colores, temas, iconos)
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Funcionalidades incluidas

- Lista de alarmas con diseño idéntico a la referencia (tarjetas, switch azul, FAB +, barra inferior).
- Crear/editar alarma: hora, minutos, AM/PM, repetición por día, una sola vez, nombre, vibración, sonido, snooze.
- **Sonido personalizado**: en "Crear alarma" y en "Configuración → Cambiar sonido de alarma" se abre
  el selector oficial de Android (Storage Access Framework) filtrado a audio, permite elegir
  cualquier MP3/WAV/OGG/FLAC/M4A desde Descargas, Música, almacenamiento interno o SD, y guarda
  el permiso de lectura de forma permanente (`takePersistableUriPermission`).
- Suena con pantalla apagada, teléfono bloqueado, app cerrada o eliminada de recientes, gracias a
  `AlarmManager.setExactAndAllowWhileIdle()` + `BroadcastReceiver` + `Foreground Service`.
- Restaura todas las alarmas automáticamente tras reiniciar el teléfono (`BOOT_COMPLETED`).
- Pantalla de alarma sonando a pantalla completa sobre el bloqueo, con botones DETENER y POSPONER.
- Configuración: sonido, volumen de prueba, vibración, duración del snooze, tema claro/oscuro/sistema,
  formato 12/24 horas.
- Base de datos Room: todo persiste al cerrar la app.
- Permisos solicitados solo cuando son necesarios (notificaciones, alarmas exactas, batería).
