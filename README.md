# Mi Negocio

App Android para llevar el día a día de un negocio pequeño en Colombia: inventario con
fotos, ventas (de contado y fiadas), compras, clientes, proveedores, gastos, cartera de
fiados, informes, reportes en Excel y **varias personas en el mismo dispositivo**. Funciona
sin internet. Solo pide permiso de **cámara** cuando se abre el lector de códigos; no usa
ubicación, contactos ni almacenamiento general.

- **Versión:** 1.23 (versionCode 24) · **targetSdk 35**
- **Paquete:** `com.polypodiozoa.minegocio`
- **Android:** 7.0 (API 24) o superior. Probada en una tablet Amazon Fire HD 8 (API 28).
- **Privacidad:** no recoge ni envía datos; no declara el permiso de internet.

## Qué se comparte y qué se sube a Play

| Archivo | Para qué | Cómo se usa |
|---|---|---|
| [`minegocio.apk`](minegocio.apk) · 568 KB | **Instalar a mano o compartir** con alguien (WhatsApp, Drive, USB). | Se abre en el dispositivo y se acepta el origen desconocido. |
| [`minegocio.aab`](minegocio.aab) · 569 KB | **Subir a Google Play Console.** Es un *Android App Bundle*: un paquete de entrega, no un instalador. | Play lo convierte en el APK que corresponde a cada dispositivo. **No se puede instalar directamente.** |

- APK SHA-256: `cb4093b52c72ed2d7a4887f259a14cbc5a2ac93382894860ec21b94c55d2618f`
- AAB SHA-256: `5cf1c224dd8c4b8ac38dc3ea2dd3b175d79233c267b14cbb0f7db8406dc05f0e`

El APK del repositorio va firmado con la llave de **desarrollo** (sirve para probar y para
compartir). La versión de Play la firma la llave de subida del dueño; si se instala encima de
la de desarrollo, Android pide desinstalar antes (y eso borra los datos): hacer copia primero.

## Personas, accesos y menú

- **Varias cuentas en una tablet o teléfono.** Cada persona tiene nombre, rol y su propio
  orden de botones. La cuenta actual firma las ventas, compras, abonos y gastos.
- **Botón de usuario arriba a la derecha** con icono de persona y el nombre actual. Abre el
  **Administrador de Accesos** verde.
- **Administrador de Accesos:** tarjetas de las personas; tocar una cambia la cuenta. Desde
  ahí se agregan o editan personas.
- **Perfil:** tarjeta principal con el resumen de la cuenta (ventas de hoy/mes y gastos),
  edición del perfil, cambio de persona y **Personalizar mi menú**.
- **Menú por cuenta:** cada persona ordena los botones de «¿Qué quieres hacer?» con Subir /
  Bajar. El orden se guarda solo para esa cuenta. Nueva venta y Nueva compra conservan el
  borde más grueso.
- **Páginas de administración en verde bosque** (accesos, perfil, personas y menú), separadas
  del azul de la terminal de ventas. No son verdes fluorescentes.
- **Nombre del negocio:** se escribe en Ajustes y aparece en Inicio debajo de la fecha. Si
  queda vacío, el inicio muestra «Mi Negocio».
- **Historial:** las personas con ventas no se borran; se desactivan para conservar el
  historial. La autoría se ve en las listas, en los detalles y en el Excel.

## El mostrador (botón Datos)

- En la parte superior, al lado de **Administrador**, está el botón **Datos**.
- Muestra primero el **QR**; si no hay foto, genera un QR de ejemplo claramente marcado.
- Un botón central **Completar datos** reúne la edición de QR, llave, cuenta, banco y titular.
  Solo desaparece cuando no quedan datos de ejemplo en los módulos visibles.
- En **Ajustes → Datos para recibir pagos → Personalizar Mostrador** se elige qué módulos se
  ven (QR, llave, cuenta, cómo pagar) y su orden; también se pueden ocultar.
- Al bajar: **llave Bre-B**, **número de cuenta**, **banco/identificador** y **titular**.
  **No hay lista de métodos**: el pago se centra en Bre-B y, si falla, en el número de cuenta.
- Una guía breve explica escanear el QR, dictar la llave y usar la cuenta como respaldo.
- Todo se configura en **Ajustes → Datos para recibir pagos**, antes de “Borrar todos los
  datos”. Si falta información, se muestran valores de ejemplo:
  `@EJEMPLO2567`, `#4004EJEMPLO2145`, `Banco de ejemplo`.
- La foto del QR se guarda en el almacenamiento privado y no se envía a ningún servidor.
- Si falta algún dato, arriba aparece **“Tienes datos de ejemplo”** y cada dato tiene un
  botón **Editar** a la derecha. Al confirmar el dato, ese botón desaparece y el valor queda
  guardado también en Ajustes; para cambiarlo después se usa Ajustes.
- **Tomar foto del QR**: la app lee el QR de la foto, lo vuelve a generar limpio en blanco y
  negro a 1200 × 1200 px y lo deja listo para El mostrador. También se puede elegir una imagen.
  Funciona mejor de cerca, sin reflejos y con los cuatro bordes visibles.

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
| [`AUDITORIA.md`](AUDITORIA.md) | Revisiones del 18 y 21/10/2026: cálculos, accesibilidad, lector, multiusuario, robustez y seguridad de datos. |
| [`LEEME.md`](LEEME.md) | Manual completo: instalación, módulos, personas, lector, compilación y restauración. |
| [`LICENCIAS.md`](LICENCIAS.md) | Licencia de ZXing (Apache 2.0) y créditos. |
| [`tienda/`](tienda/) | Ficha para Play, política de privacidad, respuestas de Seguridad de los datos y gráfico destacado 1024 × 500. |

## Compilar

Requiere el SDK de Android (**platforms;android-35** y **build-tools;35.0.0**) y JDK. No usa
Gradle. ZXing está en `herramientas/zxing-core-3.5.3.jar` y se incluye solo en el DEX.

```bash
VERSION_CODE=24 VERSION_NAME=1.23 ./compilar.sh        # → minegocio.apk (firmado)
VERSION_CODE=24 VERSION_NAME=1.23 ./empaquetar-aab.sh  # → minegocio.aab (para Play)
```

El AAB se arma con `aapt2` en formato proto y `herramientas/bundletool.jar`; se valida con
`bundletool validate`.

## Instalar el APK

1. Copiar `minegocio.apk` al dispositivo.
2. Abrirlo desde **Archivos** y aceptar la instalación de origen desconocido.
3. La primera vez abre con la cuenta **Administrador**. En el botón de usuario se agregan las
   demás personas; en **Ajustes → Cargar datos de ejemplo** se puede recorrer con datos de
   prueba y luego borrarlos con **Eliminar todos los datos**.
4. Al abrir el lector por primera vez, Android pide permiso de cámara. Se puede negar y usar
   el código escrito o una imagen.

## Datos

- Base SQLite en el almacenamiento privado de la app, fotos y adjuntos en `files/adjuntos/`.
- **Copia de seguridad:** Ajustes → *Crear copia de seguridad* genera un `.zip` con la base,
  las fotos y un `LEEME.txt`, y se guarda donde el usuario elija.
- **Restaurar:** Ajustes → *Restaurar una copia*.
- **Entre versiones:** una copia de 1.10 (v2) se restauró en 1.15 y migró a v3; una copia de
  1.15 se restauró en 1.15 sin perder datos. Una copia nueva no se puede abrir en una app
  vieja: actualiza primero.
- Desinstalar la app borra todo. La copia es la única forma de recuperarlo.
