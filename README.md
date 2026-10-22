# Mi Negocio

App Android para llevar el día a día de un negocio pequeño en Colombia: inventario con
fotos, ventas (de contado y fiadas), compras, clientes, proveedores, gastos, cartera de
fiados, informes y reportes en Excel. **Funciona sin internet**. Solo pide permiso de
**cámara** cuando se abre el lector de códigos; no usa ubicación, contactos ni
almacenamiento general.

- **Versión:** 1.10 (versionCode 11) · **targetSdk 35**
- **Paquete:** `com.polypodiozoa.minegocio`
- **Android:** 7.0 (API 24) o superior. Probada en una tablet Amazon Fire HD 8 (API 28).
- **Privacidad:** no recoge ni envía datos; no declara el permiso de internet.

## Qué se comparte y qué se sube a Play

| Archivo | Para qué | Cómo se usa |
|---|---|---|
| [`minegocio.apk`](minegocio.apk) · 531 KB | **Instalar a mano o compartir** con alguien (WhatsApp, Drive, USB). | Se abre en el dispositivo y se acepta el origen desconocido. |
| [`minegocio.aab`](minegocio.aab) · 529 KB | **Subir a Google Play Console.** Es un *Android App Bundle*: un paquete de entrega, no un instalador. | Play lo convierte en el APK que corresponde a cada dispositivo. **No se puede instalar directamente.** |

- APK SHA-256: `14c5cb0747954102af94b4855f9bcdd5173b8e375b33e54ee5e66c18ca77478d`
- AAB SHA-256: `8d441cf97a267ef5510bf3e15e1c5d60667a21a13e53a651c94d77fec0c4f322`

El APK del repositorio va firmado con la llave de **desarrollo** (sirve para probar y para
compartir). La versión de Play la firma la llave de subida del dueño; si se instala encima de
la de desarrollo, Android pide desinstalar antes (y eso borra los datos): hacer copia primero.

## Lector de códigos

- **Código de barras por defecto** (EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93,
  ITF y Codabar).
- **QR opcional**, cambiable en **Ajustes → Lector de códigos** o dentro del propio lector.
  La elección queda guardada.
- En **Inventario**, el botón “Escanear código para buscar o agregar” abre el producto si el
  código existe o crea uno nuevo con el código ya escrito.
- En **Nueva venta**, “Escanear código y agregar producto” busca el producto y lo suma a la
  venta; si no existe, ofrece crearlo.
- En la ficha del producto, “Escanear código de barras o QR” llena el código.
- Si no hay cámara o no se da permiso, se puede **escribir el código** o **elegir una foto**
  del código. La lectura es local (ZXing) y no guarda ni envía imágenes.

## Documentos

| Archivo | Qué contiene |
|---|---|
| [`DISENO.md`](DISENO.md) | Registro de diseño: usuarios, principios, pantallas, sistema visual, flujos, modelo de datos, decisiones y estado de la publicación en Play. |
| [`AUDITORIA.md`](AUDITORIA.md) | Revisión del 18/10/2026 y actualización del lector (21/10/2026): cálculos, accesibilidad, robustez y seguridad de datos. |
| [`LEEME.md`](LEEME.md) | Manual completo: instalación, cada módulo, lector, compilación y restauración. |
| [`LICENCIAS.md`](LICENCIAS.md) | Licencia de ZXing (Apache 2.0) y créditos. |
| [`tienda/`](tienda/) | Ficha para Play, política de privacidad, respuestas de Seguridad de los datos y gráfico destacado 1024 × 500. |

## Compilar

Requiere el SDK de Android (**platforms;android-35** y **build-tools;35.0.0**) y JDK. No usa
Gradle. ZXing está en `herramientas/zxing-core-3.5.3.jar` y se incluye solo en el DEX.

```bash
VERSION_CODE=11 VERSION_NAME=1.10 ./compilar.sh        # → minegocio.apk (firmado)
VERSION_CODE=11 VERSION_NAME=1.10 ./empaquetar-aab.sh  # → minegocio.aab (para Play)
```

El AAB se arma con `aapt2` en formato proto y `herramientas/bundletool.jar`; se valida con
`bundletool validate`.

## Instalar el APK

1. Copiar `minegocio.apk` al dispositivo.
2. Abrirlo desde **Archivos** y aceptar la instalación de origen desconocido.
3. La primera vez abre vacía; en **Ajustes → Cargar datos de ejemplo** se puede recorrer con
   datos de prueba y luego borrarlos con **Eliminar todos los datos**.
4. Al abrir el lector por primera vez, Android pide permiso de cámara. Se puede negar y usar
   el código escrito o una imagen.

## Datos

- Base SQLite en el almacenamiento privado de la app, fotos y adjuntos en `files/adjuntos/`.
- **Copia de seguridad:** Ajustes → *Crear copia de seguridad* genera un `.zip` con la base,
  las fotos y un `LEEME.txt`, y se guarda donde el usuario elija.
- **Restaurar:** Ajustes → *Restaurar una copia*.
- Desinstalar la app borra todo. La copia es la única forma de recuperarlo.
