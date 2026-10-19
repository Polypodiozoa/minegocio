# Mi Negocio 1.4 — inventario, ventas, fiados y reportes para micronegocios colombianos

Aplicación Android nativa para llevar el día a día de un negocio pequeño en Colombia:
inventario con fotos, ventas, compras, clientes, proveedores, gastos, **cartera de fiados**,
impuestos opcionales, abonos, informes y **reportes en Excel**. Funciona sin internet y no pide
permisos de cámara, ubicación, contactos ni almacenamiento.

- **APK instalado en la tablet:** `minegocio.apk` (256 KB)
- **SHA-256:** `aef585ba891bf3e6f00246645849db3b827521c334a5ecebcc8d4404f4b5c8f7`
- **Paquete:** `com.polypodiozoa.minegocio`
- **Versión:** 1.4 (versionCode 5)
- **Tablet de prueba:** Amazon Fire HD 8 `KFONWI`, Fire OS 9 / Android 9 (API 28),
  800 × 1280 px, densidad 213 dpi.
- **Requisito mínimo:** Android 7.0 (API 24).
- **Estado:** instalada, probada y actualizada el 18 de octubre de 2026. Al final se borraron los datos
  de prueba; la app quedó limpia, con **Ajustes → Cargar datos de ejemplo** para recorrerla.

## Novedades de la versión 1.4 — correcciones de la auditoría

- **Cartera de fiados:** Inicio, Informes y Deudas ya dan la misma cifra. Un abono es plata
  que entró y no pertenece a una venta concreta: al anular una venta o al registrar otra
  venta fiada, los abonos se reparten de nuevo entre las deudas abiertas (por vencimiento
  más próximo) y lo que sobra queda como **saldo a favor**, que la próxima venta fiada
  consume automáticamente.
- **Restauración más segura:** la copia se valida con `PRAGMA quick_check` y se reemplaza con
  un renombrado atómico; si algo falla, la base actual queda intacta. Una copia sin carpeta
  de fotos ya **no borra** las fotos que están en la tablet.
- **Copia de seguridad:** si la exportación falla a mitad, se borra el archivo incompleto.
- **Eliminar todos los datos** borra también las copias internas de una restauración y
  `errores.log`, y deja la base compactada (`VACUUM`).
- **Anular una compra** devuelve el costo del producto al de la última compra vigente.
- **Salir sin guardar:** las pantallas de cliente, producto, proveedor, gasto y abono avisan
  antes de descartar lo escrito.
- **Accesibilidad:** los selectores de forma de pago, período, categoría y estado tienen
  etiqueta asociada; los campos de los diálogos también; los botones de diálogo quedan en
  52 dp reales (antes 39 dp por un valor en píxeles) y «Ver comprobante» en 48 dp; el borde
  de los campos pasa a un contraste de 3,5:1.

## Novedades de la versión 1.3

- **Informes:** la línea «Ingresos (con impuesto)» pasa a llamarse **«Ingreso»**, tanto en la
  pantalla como en el resumen del reporte de Excel. El impuesto cobrado sigue en su propia
  línea y el pagado en compras en la suya.

## Novedades de la versión 1.2

- **Iconos del inicio separados.** «Compras» y «Proveedores» usaban el mismo camión y las dos
  tarjetas se veían iguales. Proveedores pasa a un **edificio** (`res/drawable/ic_edificio.xml`):
  se lee como la empresa a la que se le compra y no se confunde con el camión de Compras ni con
  la caja de Inventario. Compras conserva el camión.

## Novedades de la versión 1.1

- **Cartera de fiados** con saldo por venta, cliente, fecha de venta, **fecha límite de pago**,
  abonos aplicados, días vencidos y filtros **Todas / Vencidas / Por vencer**.
- **Vencimiento obligatorio en ventas a crédito**, con fecha sugerida a 30 días y selector de fecha.
- **Cupo de fiado por cliente**: máximo autorizado, cupo disponible y aviso si una venta a
  crédito lo supera (se puede continuar con confirmación).
- **Abonos dirigidos a una venta fiada** o abonos generales repartidos entre las deudas más
  urgentes (primero vencimiento más próximo). El saldo por venta se calcula con esas aplicaciones.
- **Resumen de cuenta por cliente**: total comprado, total abonado, cupo, cupo disponible,
  deudas pendientes e historial de ventas y abonos.
- **Inventario con filtros**: Todos, **Agotados** y **Por agotarse**, con conteos en los botones
  y en el resumen. Las filas siguen mostrando “Bajo”/“Agotado”.
- **Rentabilidad en la venta**: ganancia estimada y margen en el formulario y en el detalle;
  ganancia por línea; **ganancia por unidad** en la ficha del producto.
- **Informe en Excel (.xlsx)** diario, semanal, mensual, del mes anterior, anual o de todo el
  tiempo, con hojas **Resumen, Ventas, Productos, Deudas y Gastos**. Se mantiene la exportación
  CSV de ventas.
- **Alertas en Inicio** para deudas vencidas y existencias bajas.
- Base de datos **versión 2** con migración automática desde la versión 1.

## Lo que hace

| Módulo | Para qué sirve |
|---|---|
| **Inicio** | Ventas de hoy/mes, utilidad, por cobrar, alertas de existencias y deudas vencidas, últimas ventas y accesos grandes. |
| **Inventario** | Productos con nombre, categoría, código, unidad, precio, costo, existencias, mínimo y foto. Filtros de agotados y por agotarse. Búsqueda. Ganancia por unidad. |
| **Ventas** | Cliente o consumidor final, productos o concepto libre, cantidades, descuento, impuesto opcional, forma de pago (incluido crédito), vencimiento, notas, comprobante y confirmación. |
| **Deudas** | Cartera de ventas fiadas: total por cobrar, vencido, por vencer, filtros y saldo por venta. Exporta la cartera a Excel. |
| **Compras** | Proveedor, productos con costo unitario, impuesto, pago, notas y foto/PDF de factura. Suma existencias y actualiza costo. |
| **Clientes** | Perfil con foto, contacto, notas, **cupo de fiado**, saldo, resumen de cuenta, deudas e historial. |
| **Proveedores** | Contacto, foto, notas e historial de compras. |
| **Gastos** | Descripción, categoría, monto, pago, notas y comprobante. |
| **Informes** | Períodos, ventas, costo, utilidad, impuestos, gastos, utilidad neta, compras, cartera, top de productos con ganancia, formas de pago y Excel/CSV. |
| **Ajustes** | Datos del negocio/NIT, impuesto por defecto, permitir ventas sin existencia, tamaño de texto, copia/restauración, datos de ejemplo y borrado. |

## Ventas fiadas y deudas

1. En una venta se elige **Crédito** como forma de pago.
2. La app exige un cliente y muestra la **fecha límite de pago** (30 días por defecto). Se puede
   cambiar con el selector de fecha; no se permiten fechas anteriores a hoy.
3. Si el cliente tiene **cupo de fiado** y la venta lo supera, aparece una confirmación con el
   cupo disponible antes de continuar.
4. Al aprobar, la venta queda como crédito y aparece en **Deudas** y en el perfil del cliente.
5. Desde el detalle de la venta o desde Deudas se puede **registrar un abono a esa venta**.
   El abono se reparte primero a la venta elegida y, si sobra, a las demás deudas pendientes.
6. Un abono general desde el perfil del cliente se reparte entre las ventas fiadas por
   vencimiento más próximo.
7. La cartera muestra **Vencida**, **Por vencer** (próximos 7 días), **Vigente** o **Sin fecha**.
8. Cuando una deuda queda saldada, desaparece de la cartera y la venta se marca
   **Crédito pagado** en el historial.

Los saldos se calculan por venta con la tabla `pago_aplicaciones`; el saldo general del cliente
sigue siendo la suma de ventas a crédito menos todos sus abonos.

## Clientes: perfil, cupo y resumen

El perfil permite editar nombre, teléfono, correo, dirección, notas, foto y **cupo de fiado**.
La ficha del cliente muestra:

- **Total comprado** (todas las ventas no anuladas).
- **Total abonado**.
- **Cupo de fiado** y **cupo disponible** (cupo − saldo pendiente).
- **Deudas pendientes** con vencimiento, abono y saldo, con acceso a la venta.
- Historial completo de ventas y abonos, y acceso a registrar abono o nueva venta.

## Inventario actualizado

- **Todos (N)**, **Agotados (N)** y **Por agotarse (N)** son botones de filtro.
- “Agotado” = existencia 0 o menos.
- “Por agotarse” = existencia positiva igual o menor al mínimo configurado.
- El resumen muestra cuántos productos hay, cuántos están agotados y cuántos están por agotarse.
- Inicio muestra una alerta con los productos que necesitan reposición.
- Si intentas vender sin existencia suficiente, la app avisa y permite continuar solo con
  confirmación (o de acuerdo con el ajuste “Permitir vender sin existencias”).

## Rentabilidad

- **Ganancia estimada de la venta** = total cobrado − impuesto cobrado − costo de los productos.
- **Margen** = ganancia / (subtotal − descuento).
- En el formulario de venta se ve la ganancia por línea, el costo, la ganancia total y el margen
  antes de aprobar.
- En el detalle de la venta quedan el costo, la ganancia y el margen.
- En la ficha del producto se muestra **Ganancia por unidad** y su margen; sirve para el caso
  típico de comprar al por mayor y vender por unidad.
- En Informes y Excel, cada producto muestra costo, precio, ganancia unitaria, margen, unidades
  vendidas, ingresos, costo y utilidad del período.

## Impuestos y precios

- Todos los valores están en **pesos colombianos (COP)**, sin centavos, con formato
  `$ 12.500` y descripción hablada “12.500 pesos”.
- El impuesto es **opcional por transacción**, con porcentaje editable (19 %, 5 %, 0 %…).
- La utilidad bruta descuenta el impuesto cobrado y el costo; la utilidad neta también los gastos.
- **No es facturación electrónica DIAN**. El PDF es un comprobante interno de venta.

## Informes y Excel

Períodos: **Hoy, Esta semana, Este mes, Mes anterior, Este año, Todo el tiempo**.

El informe muestra: ventas realizadas, ingresos, costo, utilidad bruta y margen, impuestos,
gastos, utilidad neta, compras, impuestos de compras, por cobrar, deuda vencida, por vencer en
7 días y deudas abiertas. Incluye top de productos con ganancia y ventas por forma de pago.

**Exportar informe del período a Excel** crea un `.xlsx` con cinco hojas:

1. **Resumen**: indicadores, cartera actual, formas de pago y productos más vendidos.
2. **Ventas**: fecha, cliente, pago, estado, vencimiento, subtotal, descuento, impuesto, total,
   costo, ganancia, margen, abonado, saldo y productos.
3. **Productos**: existencia, mínimo, estado, costo, precio, ganancia unitaria, margen, vendidos,
   ingresos, costo y utilidad del período.
4. **Deudas**: cliente, teléfono, fecha, vencimiento, días, total, abonado, saldo y estado.
5. **Gastos**: fecha, categoría, descripción, monto, pago y notas.

También existe **Exportar deudas a Excel** en la pantalla Deudas y **Exportar solo ventas (CSV)**
en Informes. Los archivos se guardan con el selector de documentos de Android, en Downloads,
Drive, USB o donde el usuario elija.

Los XLSX se generan sin librerías externas (ZIP + XML OOXML) y se validaron abriéndolos con
LibreOffice. El informe de prueba tenía 5 hojas y 7.510 bytes; la cartera, 2.769 bytes.

## Fotos, comprobantes y archivos

- **Tomar foto** con la cámara del sistema o **elegir imagen/PDF** desde Archivos.
- Se adjuntan a ventas, compras, abonos, gastos; foto principal en productos, clientes y proveedores.
- Los archivos se copian al almacenamiento privado de la app y se pueden ver, abrir o quitar.
- No se piden permisos de cámara ni de almacenamiento: se usan intentos del sistema y el
  proveedor de archivos propio.

## Privacidad y seguridad

- **Sin permiso de internet** (verificado con `aapt2 dump permissions`: no lista permisos).
- Sin cuentas, nube, analítica, publicidad ni envío de datos.
- Base SQLite privada; fotos en `files/adjuntos/`.
- La copia de seguridad es un ZIP que crea el usuario; **contiene datos personales y
  comprobantes**, trátalo como información sensible.
- La restauración reemplaza todos los datos y reinicia la app. Antes guarda una copia interna
  del estado anterior por si falla.
- Firma con la **llave de desarrollo estándar de Android** (`~/.android/debug.keystore`,
  `androiddebugkey`). Sirve para esta tablet; para Play Store genera una llave de producción.
- Para distribuir, ajusta `MINEGOCIO_KEYSTORE`, `MINEGOCIO_KEY_PASS` y `MINEGOCIO_KEY_ALIAS`.

### Copia y restauración

- **Ajustes → Crear copia de seguridad** → `minegocio-AAAAMMDD-HHMM.zip` (base + fotos + PDF).
- **Ajustes → Restaurar una copia** → elegir el ZIP → confirmar → reinicia.
- La copia de la versión 1.1 conserva la base con esquema 2 (`clientes.cupo`,
  `ventas.fecha_limite`, `pago_aplicaciones`).
- Verificado: la copia v2 incluyó las tablas y datos nuevos; la restauración se probó con el
  flujo completo de la app (en la versión anterior) y el mismo procedimiento copia el archivo
  completo de la base v2.

## Accesibilidad

- **Texto escalable**: cuerpo 16–18 sp, títulos 20–34 sp, botones 18 sp. Ajustes:
  Sistema / Grande (+15 %) / Muy grande (+30 %), respetando la escala de Android.
- **Objetivos táctiles** de 48–58 dp; filas de 64–80 dp.
- **Contraste WCAG**: texto principal/blanco 16,52:1; secundario/blanco 8,05:1; blanco/azul
  8,94:1; verde 5,48:1; ámbar 6,61:1; rojo 5,80:1. Todos superan AA.
- **No se usa solo el color**: estados escritos (“Vencida”, “Por vencer”, “Vigente”,
  “Crédito pagado”, “Agotado”, “Bajo”).
- **Etiquetas y descripciones** en botones de icono, filas, importes hablados y anuncios.
- **Sin gestos obligatorios**; funciona en vertical y horizontal.
- **Nota honesta:** esta Fire OS no tiene TalkBack instalado; se validaron etiquetas con
  `uiautomator`, contraste calculado y prueba de texto grande, no con lector de pantalla real.

## Verificación realizada (18-10-2026)

En la tablet por USB:

- Instalación `adb install -r` → `Success`; versión 1.1, versionCode 2, actualizada a las 19:43.
- Inicio con alerta de deuda vencida de **$ 36.850** y “Por cobrar $ 36.850”.
- Deudas: cartera con vencimiento, saldo y filtros; abono dirigido a la venta #2 de $ 36.850;
  saldo del cliente a $ 0 y alerta desaparecida.
- Venta fiada #4 por **$ 7.140** (bolsa, IVA 19 %, vencimiento 17 nov 2026): aparece en cartera,
  en el perfil del cliente y en el Excel.
- Cliente Carlos: total comprado $ 143.990, abonado $ 136.850, cupo $ 300.000, disponible
  $ 292.860, deuda #4 de $ 7.140.
- Inventario: filtros Todos (6), Agotados (0), Por agotarse (0), con mensajes correctos;
  estados “Bajo”/“Agotado” y conteos en el resumen.
- Producto Aretes: ganancia por unidad **$ 70.000 (39 %)** con precio $ 180.000 y costo $ 110.000.
- Venta: ganancia y margen visibles por línea y en el resumen; venta #4 con ganancia $ 3.500
  y margen 58 %.
- Informes: 4 ventas, ingresos $ 412.830, costo $ 201.400, utilidad bruta $ 153.100,
  impuestos $ 58.330, gastos $ 350.000, utilidad neta −$ 196.900, por cobrar $ 7.140,
  deudas abiertas 1; top de productos con ganancia.
- Excel: `reporte-este-mes-20261018.xlsx` con 5 hojas (Resumen, Ventas, Productos, Deudas,
  Gastos), 7.510 bytes; validado con LibreOffice. La hoja Ventas incluye productos, costo,
  ganancia y margen; Deudas incluye saldo y totales; Productos incluye ganancia unitaria.
- Cartera Excel: `deudas-20261018-1934.xlsx`, 2.769 bytes, con cliente, teléfono, vencimiento,
  días, total, abonado y saldo.
- Cámara: se abrió la cámara del sistema, se tomó una foto y volvió al formulario.
- Copia de seguridad: ZIP con base v2, fotos/PDF y `LEEME.txt`; base verificada con
  `PRAGMA user_version=2`, 4 ventas, 2 pagos y aplicaciones de pago.
- Texto “Grande” y rotación horizontal probados sin cortes.
- **Limpieza final:** se borraron datos de prueba y archivos exportados de Downloads. La app
  quedó instalada y vacía.

## Capturas

| | |
|---|---|
| ![Inicio con alerta de deuda](capturas/01-inicio.png) | ![Inventario con filtros](capturas/02-inventario.png) |
| ![Nueva venta con ganancia](capturas/03-nueva-venta.png) | ![Detalle de venta fiada](capturas/04-detalle-venta.png) |
| ![Clientes](capturas/05-clientes.png) | ![Perfil, cupo y deudas](capturas/06-detalle-cliente.png) |
| ![Informes](capturas/07-informes.png) | ![Ajustes](capturas/08-ajustes.png) |
| ![Comprobante PDF](capturas/09-comprobante-pdf.png) | ![Visor de comprobante](capturas/10-visor-comprobante.png) |
| ![Horizontal](capturas/11-paisaje.png) | ![Inicio limpio](capturas/12-inicio-limpio.png) |
| ![Deudas vencidas](capturas/15-deudas.png) | ![Venta a crédito con vencimiento](capturas/16-venta-credito.png) |
| ![Reporte Excel](capturas/17-informe-excel.png) | ![Deuda vigente](capturas/20-deudas-vigente.png) |
| ![Crédito pagado](capturas/21-credito-pagado.png) | ![Rentabilidad por unidad](capturas/22-rentabilidad-producto.png) |
| ![Iconos de Compras y Proveedores separados](capturas/23-inicio-proveedores.png) | |

## Compilar e instalar

Requisitos: Android SDK en `~/android-sdk` con `build-tools/34.0.0` y `platforms/android-28`,
OpenJDK 21. No hace falta Gradle ni Android Studio.

```bash
cd ~/proyectos/minegocio-tablet
./compilar.sh
adb install -r minegocio.apk
```

El script compila recursos, Java, DEX, alinea, firma con la llave de desarrollo y verifica.
Para cambiar la versión: `VERSION_CODE=3 VERSION_NAME=1.2 ./compilar.sh`.

Desinstalar (borra datos locales):

```bash
adb uninstall com.polypodiozoa.minegocio
```

Crea una copia antes de desinstalar.

## Estructura del proyecto

```
minegocio-tablet/
├── AndroidManifest.xml
├── compilar.sh
├── minegocio.apk
├── capturas/
├── res/
│   ├── layout/          # pantallas, filas, filtros y tarjetas
│   ├── drawable/        # iconos y fondos
│   └── values/          # colores, estilos y textos
└── src/com/polypodiozoa/minegocio/
    ├── datos/           # SQLite v2, deudas, adjuntos, respaldo
    ├── modelo/Modelo.java
    ├── ui/              # actividades (incluye DeudasActivity)
    └── util/            # COP, fechas, imágenes, Excel, PDF, UI
```

## Límites conocidos

- **No facturación electrónica DIAN** ni retenciones/nómina.
- **No sincronización en la nube** ni multiusuario; una tablet, una base local.
- **No lector de códigos de barras**.
- **Compras a crédito no generan cuentas por pagar**; sí se controlan ventas fiadas y abonos.
- Los abonos se reparten por vencimiento (o por la venta elegida), no con un plan de cuotas
  personalizado.
- Para producción: llave propia, política de datos personales (Ley 1581 de Colombia) e
  integración DIAN si se requiere.

## Cómo revertir

- Desinstalar la app borra sus datos locales; no afecta al resto de la tablet.
- Guarda el APK anterior y una copia ZIP; instala con la misma llave de firma y restaura.
- No se modifican ajustes del sistema ni archivos del usuario. Los únicos archivos externos
  son los que el usuario exporta (PDF, CSV, XLSX, ZIP).
