# Mi Negocio

App Android para llevar el día a día de un negocio pequeño en Colombia: inventario con
fotos, ventas (de contado y fiadas), compras, clientes, proveedores, gastos, cartera de
fiados, informes y reportes en Excel. **Funciona sin internet** y no pide permisos de
cámara, ubicación, contactos ni almacenamiento: todo queda en el dispositivo.

- **Versión:** 1.9 (versionCode 10) · **targetSdk 35**
- **Paquete:** `com.polypodiozoa.minegocio`
- **Android:** 7.0 (API 24) o superior. Probada en una tablet Amazon Fire HD 8 (API 28).
- **Privacidad:** no recoge ni envía datos; no declara el permiso de internet.

## Qué se comparte y qué se sube a Play

| Archivo | Para qué | Cómo se usa |
|---|---|---|
| [`minegocio.apk`](minegocio.apk) · 262 KB | **Instalar a mano o compartir** con alguien (WhatsApp, Drive, USB). | Se abre en el dispositivo y se acepta el origen desconocido. |
| [`minegocio.aab`](minegocio.aab) · 260 KB | **Subir a Google Play Console.** Es un *Android App Bundle*: un paquete de entrega, no un instalador. | Play lo convierte en el APK que corresponde a cada dispositivo. **No se puede instalar directamente.** |

- APK SHA-256: `09440eb7532ab9f295a9bab4c0272d8803765d0fd5fd85e93f11a9d637dea561`
- AAB SHA-256: `b4469c4094a648edd675b563736c1b10c9ee37125ce385d1fa68bf23f43923a2`

El APK del repositorio va firmado con la llave de **desarrollo** (sirve para probar y para
compartir). La versión de Play la firma la llave de subida del dueño; si se instala encima de
la de desarrollo, Android pide desinstalar antes (y eso borra los datos): hacer copia primero.

## Documentos

| Archivo | Qué contiene |
|---|---|
| [`DISENO.md`](DISENO.md) | Registro de diseño: usuarios, principios, pantallas, sistema visual, flujos, modelo de datos, decisiones y estado de la publicación en Play. |
| [`AUDITORIA.md`](AUDITORIA.md) | Revisión del 18/10/2026: cálculos, accesibilidad, robustez y seguridad de datos, con el estado de cada hallazgo. |
| [`LEEME.md`](LEEME.md) | Manual completo: instalación, cada módulo, compilación y restauración. |
| [`tienda/`](tienda/) | Ficha para Play (nombre, descripciones), política de privacidad, respuestas de Seguridad de los datos y gráfico destacado 1024 × 500. |

## Compilar

Requiere el SDK de Android (**platforms;android-35** y **build-tools;35.0.0**) y JDK. No usa
Gradle.

```bash
VERSION_CODE=10 VERSION_NAME=1.9 ./compilar.sh        # → minegocio.apk (firmado)
VERSION_CODE=10 VERSION_NAME=1.9 ./empaquetar-aab.sh  # → minegocio.aab (para Play)
```

El AAB se arma con `aapt2` en formato proto y `herramientas/bundletool.jar`; se valida con
`bundletool validate`.

## Instalar el APK

1. Copiar `minegocio.apk` al dispositivo.
2. Abrirlo desde **Archivos** y aceptar la instalación de origen desconocido.
3. La primera vez abre vacía; en **Ajustes → Cargar datos de ejemplo** se puede recorrer con
   datos de prueba y luego borrarlos con **Eliminar todos los datos**.

## Datos

- Base SQLite en el almacenamiento privado de la app, fotos y adjuntos en `files/adjuntos/`.
- **Copia de seguridad:** Ajustes → *Crear copia de seguridad* genera un `.zip` con la base,
  las fotos y un `LEEME.txt`, y se guarda donde el usuario elija.
- **Restaurar:** Ajustes → *Restaurar una copia*.
- Desinstalar la app borra todo. La copia es la única forma de recuperarlo.
