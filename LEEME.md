# Mi Negocio 1.24 — inventario, ventas, fiados, personas, mostrador y reportes para negocios colombianos

Aplicación Android nativa para llevar el día a día de un negocio pequeño en Colombia:
inventario con fotos, ventas, compras, clientes, proveedores, gastos, **cartera de fiados**,
impuestos opcionales, abonos, informes, **reportes en Excel** y **lector de códigos de barras
o QR** y **varias personas en el mismo dispositivo**. Funciona sin internet. Solo pide permiso de cámara al abrir el lector; no usa ubicación, contactos ni almacenamiento general.

- **APK instalado en el dispositivo de prueba:** `minegocio.apk` (564 KB)
- **SHA-256 del APK:** `e3cdc7103f8cc97f9532cc8b4a323335e814df78492432681a9ec6d2dce284f5`
- **AAB de Play:** `minegocio.aab` (570 KB), SHA-256
  `efc85a77c819eb61ebd1e9b91ead5578eaf4881333ba2ab971187231c3c09327`
- **Paquete:** `com.polypodiozoa.minegocio`
- **Versión:** 1.24 (versionCode 25)
- **Tablet de prueba:** Amazon Fire HD 8 `KFONWI`, Fire OS 9 / Android 9 (API 28),
  800 × 1280 px, densidad 213 dpi.
- **Requisito mínimo:** Android 7.0 (API 24) · **targetSdk:** 35 (Android 15).
- **Estado:** instalada, probada y actualizada el 21 de octubre de 2026. Al final se borraron los datos
  de prueba; la app quedó limpia, con **Ajustes → Cargar datos de ejemplo** para recorrerla.

## Novedades de la versión 1.24 — cámara del QR y texto

- Corregido **“desde el mostrador”** (minúscula) en el mensaje de Configurar QR.
- **Tomar foto** ahora pide permiso de cámara antes de abrir la cámara del sistema. Antes, si
  el permiso estaba revocado, la app se cerraba con `SecurityException`.
- Si se niega el permiso, avisa y sugiere elegir una imagen. Si la cámara no devuelve la foto,
  también avisa.
- Verificado: con el permiso revocado apareció el diálogo de Android; al permitir, se abrió la
  cámara sin cerrar la app.

## Novedades de la versión 1.23 — botón central y Mostrador personalizable

- El título de la pantalla dice **“Mostrador”**, no “El mostrador”.
- Los botones individuales de Editar desaparecen. En su lugar hay un botón central
  **“Completar datos”** con la nota **“Tienes datos de ejemplo. Completa tu información.”**
  Ese botón reúne QR, llave, cuenta, banco y titular, y solo desaparece cuando ya no quedan
  datos de ejemplo en los módulos visibles.
- En Ajustes → Datos para recibir pagos → **Personalizar Mostrador** se puede elegir qué
  módulos se ven y su orden: QR, llave Bre-B, cuenta/banco/titular y cómo pagar.
- Si se oculta un módulo, no aparece en Mostrador y no se exige su dato para que desaparezca
  el botón central.

## Novedades de la versión 1.22 — configurar QR una vez

- Si no hay QR propio, El mostrador muestra **Configurar QR** con el aviso de que se hace
  una sola vez; después se cambia en Ajustes.
- Se puede **tomar foto** o **elegir imagen**; la app aplica el mismo proceso de limpieza
  (lee, regenera en blanco y negro y guarda).
- Al guardar, el botón desaparece y el QR queda listo. Si falla, no reemplaza nada.

## Novedades de la versión 1.21 — solo QR

- El QR se titula simplemente **“QR”**; si falta configurarlo, **“QR de ejemplo”**.
- Se quitó **“de Bre-B”** del QR en El mostrador, Ajustes y la vista ampliada. Bre-B queda
  como nombre de la llave, no del código.
- La guía dice “Escanea el QR” y luego “dicta la llave Bre-B”.

## Novedades de la versión 1.20 — sin métodos de pago

- Se eliminó **Métodos disponibles** de El mostrador y de Ajustes.
- Fuera **Nequi** y **Daviplata** de la app: sus cuentas ya funcionan con Bre-B.
- El flujo queda centrado en el **QR** y la **llave Bre-B**; el **número de cuenta** queda
  como respaldo si algo falla.
- La guía de pago se reduce a escanear, dictar la llave y usar la cuenta si falla.

## Novedades de la versión 1.19 — banco de ejemplo claro

- Se quitó el relleno sin sentido **“Banco de ejemplo · aaaaaa”**.
- El dato ficticio ahora dice simplemente **“Banco de ejemplo”**.

## Novedades de la versión 1.18 — el QR es Bre-B

- El QR de El mostrador se titula **“QR de Bre-B”**; si falta configurarlo, **“QR de Bre-B de
  ejemplo”**.
- En Ajustes, la vista previa tiene la etiqueta **“QR de Bre-B”** y la ayuda correspondiente.
- Al abrir el QR en grande, la barra superior dice **“QR de Bre-B”**, no “Comprobante”.
- La descripción accesible del QR también dice “Código QR de Bre-B”.

## Novedades de la versión 1.17 — confirmar antes de ocultar

- Arriba de El mostrador aparece **“Tienes datos de ejemplo”** si falta algún dato.
- Cada dato tiene un botón **Editar** a la derecha; ya no dice “Dato de ejemplo”.
- El diálogo avisa que **el botón Editar desaparecerá al confirmar** y que después se cambia
  en Ajustes.
- Si se cierra el diálogo o se toca afuera, no cambia nada.
- Al escribir un dato y tocar **Confirmar**, aparece un segundo aviso; solo al confirmarlo se
  guarda y desaparece el botón.
- El cambio queda también en Ajustes. Para modificarlo otra vez se usa Ajustes.

## Novedades de la versión 1.16 — editar desde El mostrador

- Las etiquetas **“Dato de ejemplo”** van a la **derecha** del dato, no encima.
- Ejemplos nuevos: llave **`@EJEMPLO2567`**, cuenta **`#4004EJEMPLO2145`**.
- Al tocar **“Dato de ejemplo”** se abre un diálogo para escribir el dato en ese momento.
  Se guarda en la misma configuración que Ajustes, así que aparece allí sin pasos extra.
- Aplica a llave, cuenta, banco, titular y métodos.

## Novedades de la versión 1.15 — etiquetas de ejemplo

- En **El mostrador**, cada dato que está en ejemplo lleva su propia etiqueta
  **“Dato de ejemplo”**: llave, cuenta, banco, titular y métodos. El QR dice “QR de ejemplo”.
- Se quitó el aviso grande que abarcaba toda la página.

## Novedades de la versión 1.14 — foto del QR

- **Tomar foto del QR** desde Ajustes, además de elegir una imagen.
- La app **lee el QR**, lo vuelve a generar limpio en blanco y negro a **1200 × 1200 px** y lo
  guarda como imagen para El mostrador. Así se eliminan sombras, fondo, inclinación y ruido.
- Si la foto no se puede leer, avisa y pide acercarse, evitar reflejos y mostrar los cuatro
  bordes. No reemplaza el QR anterior si falla.
- No hay permiso nuevo: se usa la cámara del sistema.

## Novedades de la versión 1.13 — texto de la app

- El mensaje de la versión cambia de «Hecha para micronegocios colombianos» a
  **«Hecha para negocios Colombianos.»**. Es el único cambio de esta versión.

## Novedades de la versión 1.12 — El mostrador

- **Botón Datos** en la parte superior, al lado de Administrador. Abre **El mostrador**, una
  página deslizable para mostrarle al cliente cómo pagar.
- **Primero el QR de pagos**, con foto personalizable; si no hay foto se genera un QR de
  ejemplo marcado como tal.
- Debajo: **llave Bre-B**, **número de cuenta**, **banco/identificador** y **titular**.
- ~~Métodos disponibles configurables~~: se retiraron en 1.20; Bre-B cubre Nequi y Daviplata.
- **Ajustes → Datos para recibir pagos**, antes de “Borrar todos los datos”: campos, foto del
  QR y ayuda. Si falta información, El mostrador muestra datos de ejemplo evidentes
  (`@EJEMPLO2567`, `#4004EJEMPLO2145`, `Banco de ejemplo`).
- La foto del QR y los datos se guardan solo en el dispositivo; no se envían.

## Novedades de la versión 1.11 — personas y accesos

- **Varias cuentas en una tablet o teléfono**, cada una con nombre y rol. La cuenta actual
  firma ventas, compras, abonos y gastos; su nombre aparece en listas, detalles y Excel.
- **Botón de usuario arriba a la derecha** con icono de persona y el nombre actual. Lleva al
  **Administrador de Accesos**.
- **Administrador de Accesos verde** con tarjetas de las personas: tocar una cambia la cuenta.
  Desde ahí se agregan y editan personas.
- **Perfil** como botón principal: resumen de ventas de hoy/mes, gastos, edición del perfil,
  cambio de persona y **Personalizar mi menú**.
- **Menú propio por cuenta:** cada persona ordena los botones de «¿Qué quieres hacer?»; el
  orden queda guardado solo para ella. Nueva venta y Nueva compra mantienen el borde grueso.
- **Nombre del negocio debajo de la fecha** en Inicio; se quitó el texto fijo
  «Inventario, ventas, compras y clientes en un solo lugar».
- **Páginas de administración verdes** (accesos, perfil, personas y menú), en verde bosque,
  separadas del azul de la terminal.
- **Base de datos v3** con migración automática desde v2: crea «Administrador» y asigna las
  ventas, compras, pagos y gastos existentes a esa cuenta.
- **Límite de seguridad:** no hay PIN ni contraseña por persona; el selector es para
  compartir el dispositivo, no para impedir que alguien cambie de cuenta.

## Novedades de la versión 1.10 — lector de códigos

- **Lector de códigos de barras** (EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93,
  ITF y Codabar) **por defecto**.
- **Modo QR cambiable** en **Ajustes → Lector de códigos** o dentro del lector; la elección
  queda guardada.
- **Inventario:** el botón “Escanear código para buscar o agregar” abre el producto si existe
  o crea uno nuevo con el código ya escrito.
- **Venta:** “Escanear código y agregar producto” busca el producto y lo suma; si no existe,
  ofrece crearlo.
- **Ficha del producto:** botón para llenar el código escaneado, con aviso si ya pertenece a
  otro producto.
- **Sin cámara o sin permiso:** se puede escribir el código o **elegir una foto** del código.
  La lectura es local con ZXing; no se guardan ni envían imágenes.
- **Permiso nuevo:** `android.permission.CAMERA`, solicitado solo al abrir el lector. La app
  sigue sin permiso de internet ni de almacenamiento general.

## Novedades de la versión 1.9 — lista para Google Play

- **Compila contra API 35** (`platforms;android-35` y `build-tools;35.0.0`), el targetSdk que
  Play exige hoy. En Android 15 se mantiene el aspecto de siempre mientras no se manejen los
  insets (se dejó el opt-out de edge-to-edge).
- **AAB:** `./empaquetar-aab.sh` genera `minegocio.aab` con aapt2 en formato proto y
  bundletool. Validado con `bundletool validate` y probado instalando el APK que sale del
  bundle.
- **Carpeta `tienda/`** con la ficha (nombre, descripciones, categoría), la política de
  privacidad y las respuestas de Seguridad de los datos; más el gráfico destacado de
  1024 × 500.

## Novedades de la versión 1.8 — nombre y lenguaje para publicar

- **«Mi Negocio»** con las dos mayúsculas, en la app y en el comprobante en PDF (antes el
  nombre por defecto era «Mi negocio»).
- **«Dispositivo» en vez de «tablet»** en todos los textos de la app: la aplicación no es solo
  para tabletas, también corre en teléfonos.
- Se agregó al registro de diseño la lista de lo que falta para **Google Play** (targetSdk,
  firma, AAB, ficha y privacidad).

## Novedades de la versión 1.7 — lo que pidió la prueba en el dispositivo

- **Cliente de la venta:** la sección se llama así, el botón dice **«Elegir cliente»** (antes
  «Consumidor final») y debajo explica el estado: sin cliente «Ahora es consumidor final…»;
  con cliente «Este es el cliente de la venta. Toca el nombre para cambiarlo».
- **Concepto libre:** el botón dice «Agregar concepto libre (sin inventario)» y explica qué es
  («algo que cobras y no está en el inventario: un domicilio, la mano de obra, un servicio»).
  En compras, «Agregar gasto o concepto (sin inventario)» con su propia explicación.
- **Tamaño del texto en toda la app:** antes solo cambiaba dentro de Ajustes; ahora cada
  pantalla se vuelve a crear al cambiar el tamaño y el menú principal también lo toma.
- **Formas de pago:** «Nequi / Daviplata» se quitó; quedan Efectivo, Transferencia, Tarjeta y
  Crédito (una transferencia cubre Nequi y Daviplata).
- **Costo unitario en la compra:** el rótulo «Costo c/u» se cortaba; ahora el campo lleva
  debajo un texto pequeño «Costo unitario», siempre visible.
- **Proveedor:** el botón vacío dice «Elegir proveedor» (antes «Sin proveedor»).

## Novedades de la versión 1.6 — la copia siempre se puede restaurar

- **Sin tope fijo de tamaño.** Una copia que la app no puede restaurar no sirve: se quitó el
  límite de 1 GB / 5.000 archivos. Ahora, antes de restaurar, la app mira cuánto ocupa la
  copia y cuánto espacio libre hay, y si no alcanza lo dice con números («necesita 850 MB y
  quedan 400 MB») sin tocar nada. El tope de 50.000 archivos queda solo contra un ZIP
  malicioso.
- **Menos espacio al restaurar:** las fotos se mueven desde la caché en vez de copiarse, y al
  terminar se borra la copia de las fotos anteriores (antes quedaba ocupando el doble).
- Se quitó el aviso de «copia demasiado grande» de la exportación, porque ya no hay límite.

## Novedades de la versión 1.5 — pendientes de la auditoría

- **Base dañada:** si la base no abre o está corrupta, la app **no la borra**: la aparta como
  `base-danada-<fecha>.db`, arranca vacía y explica qué pasó con un acceso directo a
  «Restaurar una copia».
- **Copia muy grande:** antes de exportar avisa si el ZIP va a superar 1 GB o 5.000 archivos
  (el límite que la app puede restaurar), para que la copia no se descubra inservible después.
- **Abono con confirmación:** muestra cliente, monto y forma de pago antes de registrar, y
  avisa si se está pagando más de lo que se debe.
- **Excel y CSV en segundo plano:** la generación ya no bloquea la pantalla; se ve un aviso
  de progreso y el resultado aparece al terminar.
- **Doble toque:** «Aprobar y registrar» y «Guardar abono» no pueden registrar dos veces.
- **Eliminar todos los datos** borra también las bases apartadas por daño.

## Novedades de la versión 1.4 — correcciones de la auditoría

- **Cartera de fiados:** Inicio, Informes y Deudas ya dan la misma cifra. Un abono es plata
  que entró y no pertenece a una venta concreta: al anular una venta o al registrar otra
  venta fiada, los abonos se reparten de nuevo entre las deudas abiertas (por vencimiento
  más próximo) y lo que sobra queda como **saldo a favor**, que la próxima venta fiada
  consume automáticamente.
- **Restauración más segura:** la copia se valida con `PRAGMA quick_check` y se reemplaza con
  un renombrado atómico; si algo falla, la base actual queda intacta. Una copia sin carpeta
  de fotos ya **no borra** las fotos que están en el dispositivo.
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

## Personas y accesos

- **Cuenta actual:** se muestra arriba a la derecha en Inicio. Tocar el botón abre el
  **Administrador de Accesos**.
- **Cambiar de persona:** tocar su tarjeta verde. La app vuelve a Inicio con el nombre y el
  menú de esa persona. Cada cuenta conserva su propio orden.
- **Agregar o editar:** “Agregar persona” y “Editar personas”. Se puede elegir rol
  Administrador o Empleado (informativo). Una persona con historial no se borra: se desactiva.
- **Perfil:** botón principal con el resumen de la cuenta; desde allí se personaliza el menú
  o se edita el nombre.
- **Autoría:** las ventas, compras, abonos y gastos guardan la cuenta que los registró y
  muestran “Atendió” o “Registró” en el detalle. El Excel de ventas y gastos incluye la
  columna de la persona.
- **Borrar todos los datos** elimina la operación pero conserva las cuentas y el orden de sus
  menús; así el negocio no tiene que volver a crear a su gente.
- **Privacidad:** las cuentas son locales; no hay nube, login remoto ni envío de nombres.

## El mostrador

- **Entrada:** botón **Datos** en la cabecera del inicio, al lado de **Administrador**.
- **Orden de la página:** QR primero; al deslizar, llave Bre-B, cuenta, banco, titular y
  “Cómo pagar”. No hay métodos disponibles.
- **QR:** se elige una imagen desde Ajustes; se guarda en el almacenamiento privado. Si no
  hay foto, se genera un QR de ejemplo con la llave de ejemplo o con la llave real si ya está
  configurada. Si hay foto, se puede tocar para verla en grande.
- **Datos de ejemplo:** cuando falta un dato de un módulo visible, se muestra arriba
  **“Tienes datos de ejemplo. Completa tu información.”** y un botón central **Completar
  datos**. Al tocarlo se elige qué completar: QR, llave, cuenta, banco o titular. El botón
  desaparece cuando ya no quedan datos de ejemplo en los módulos visibles.
- **Personalización:** en Ajustes → Datos para recibir pagos → **Personalizar Mostrador** se
  elige qué módulos se muestran y su orden. Se pueden ocultar QR, llave, cuenta o guía.
- **Después de completar:** los datos se cambian desde Ajustes; no hay botones individuales
  en Mostrador.
- **Edición con confirmación:** al tocar Editar se abre un campo con el aviso de que el botón
  desaparecerá. Si se cierra o se toca afuera, no cambia nada. Al escribir y confirmar, aparece
  un segundo aviso; solo al confirmarlo se guarda y desaparece el botón. Para cambiarlo otra
  vez se entra a Ajustes.
- **Configuración:** Ajustes → **Datos para recibir pagos**. Incluye vista previa del QR,
  **“Tomar foto del QR”**, “Elegir imagen del QR”, “Quitar QR”, llave Bre-B, cuenta, banco y
  titular. No hay casillas de métodos.
- **Foto del QR:** la app decodifica la foto, reemplaza la imagen por un QR negro sobre blanco
  de 1200 × 1200 px y lo guarda en la carpeta privada. Es una copia funcional limpia; no
  conserva el logo o los colores del banco.
- **Privacidad:** no se pide cámara para esta página; la foto elegida se copia a la carpeta
  privada de la app y no se comparte.

## Inventario actualizado

- **Todos (N)**, **Agotados (N)** y **Por agotarse (N)** son botones de filtro.
- “Agotado” = existencia 0 o menos.
- “Por agotarse” = existencia positiva igual o menor al mínimo configurado.
- El resumen muestra cuántos productos hay, cuántos están agotados y cuántos están por agotarse.
- Inicio muestra una alerta con los productos que necesitan reposición.
- Si intentas vender sin existencia suficiente, la app avisa y permite continuar solo con
  confirmación (o de acuerdo con el ajuste “Permitir vender sin existencias”).

## Lector de códigos de barras y QR

- **Modo predeterminado:** código de barras (EAN-13, EAN-8, UPC-A, UPC-E, Code 128,
  Code 39, Code 93, ITF y Codabar).
- **Cambio a QR:** **Ajustes → Lector de códigos → Código QR**, o el botón **QR** dentro del
  lector. La elección se guarda y se usa en la próxima lectura.
- **Inventario:** “Escanear código para buscar o agregar”. Si el código existe, abre el
  producto; si no, abre “Nuevo producto” con el código ya escrito.
- **Venta:** “Escanear código y agregar producto”. Busca el producto y lo suma; si no existe,
  ofrece crearlo y, al guardarlo, lo agrega a la venta.
- **Producto:** “Escanear código de barras o QR” llena el campo “Código o referencia”. Si el
  código ya pertenece a otro producto, avisa antes de guardar.
- **Respaldo del lector:** si no hay cámara, no se concede el permiso o el código está en una
  foto, se puede **escribir el código** o **elegir una imagen**. La imagen se decodifica en el
  dispositivo y no se guarda.
- **Privacidad:** la cámara solo se abre dentro del lector; no se guardan fotogramas ni se
  envían a ningún servidor. El permiso se pide en ese momento y se puede negar.
- **Rendimiento:** la lectura corre en un hilo aparte, prueba giros de 90°, 180° y 270° y
  descarta fotogramas mientras decodifica para no bloquear la vista previa.

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
- Para **tomar una foto** se usa la cámara del sistema (no requiere el permiso de la app).
- El **lector de códigos** sí pide permiso de cámara al abrirse; se puede negar y usar el
  código escrito o una imagen. No se piden permisos de almacenamiento general.

## Privacidad y seguridad

- **Sin permiso de internet.**
- **Solo `android.permission.CAMERA`**, necesario para el lector; se pide en tiempo de
  ejecución al abrir el lector. Verificado con `aapt2 dump permissions`.
- Sin cuentas, nube, analítica, publicidad ni envío de datos.
- Base SQLite privada; fotos en `files/adjuntos/`.
- La copia de seguridad es un ZIP que crea el usuario; **contiene datos personales y
  comprobantes**, trátalo como información sensible.
- La restauración reemplaza todos los datos y reinicia la app. Antes guarda una copia interna
  del estado anterior por si falla.
- Firma con la **llave de desarrollo estándar de Android** (`~/.android/debug.keystore`,
  `androiddebugkey`). Sirve para este dispositivo; para Play Store genera una llave de producción.
- Para distribuir, ajusta `MINEGOCIO_KEYSTORE`, `MINEGOCIO_KEY_PASS` y `MINEGOCIO_KEY_ALIAS`.

### Copia y restauración

- **Ajustes → Crear copia de seguridad** → `minegocio-AAAAMMDD-HHMM.zip` (base + fotos + PDF).
- **Ajustes → Restaurar una copia** → elegir el ZIP → confirmar → reinicia.
- La copia actual conserva la base con esquema 3 (`usuarios`, `usuario_id` en
  ventas/compras/pagos/gastos, `clientes.cupo`, `ventas.fecha_limite`, `pago_aplicaciones`).
- **Compatibilidad probada el 22/10/2026:**
  - Copia real de **1.10 (esquema v2)** restaurada en **1.15**: aparecieron las 3 ventas,
    1 compra, 1 pago y 1 gasto; la base se migró a v3 y todo quedó a nombre de
    **Administrador**.
  - Copia creada en **1.15 (v3)** y restaurada en **1.15**: los datos y la cuenta se
    conservaron; el archivo se verificó con `PRAGMA user_version=3`.
- **Copia nueva en app vieja:** no. Una copia v3 no se puede abrir en 1.10; hay que
  actualizar la app primero. Una copia antigua sí se puede restaurar en una app nueva.
- La copia incluye base, fotos, adjuntos y el QR de pago; trátala como información sensible.
- Si la copia no se puede leer o el QR está incompleto, la app avisa y no reemplaza los datos
  actuales sin confirmación.

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

## Verificación de copias (22-10-2026)

- `backup-v2.zip` (1.10, base v2) → **1.15**: restauración correcta; 3 ventas, 1 compra,
  1 pago y 1 gasto; migración a v3; ventas asignadas a Administrador.
- `minegocio-20261022-0008.zip` (1.15, base v3) → **1.15**: restauración correcta; datos y
  cuenta Administrador conservados; `PRAGMA user_version=3`.
- La copia v3 incluye las tablas de usuarios y autoría; una copia v2 no trae cuentas creadas
  después.
- **No se probó** una copia v1 de la versión 1.0 ni una copia v3 abierta en una app 1.10
  (esta última no es compatible por diseño).

## Verificación 1.24 (22-10-2026)

- **Texto:** el diálogo de Configurar QR dice “desde el mostrador”, no “desde El mostrador”.
- **Cámara:** se revocó el permiso de cámara; al tocar **Tomar foto** apareció el diálogo de
  Android. Al pulsar **Permitir**, se abrió la cámara del sistema y la app siguió funcionando.
- **Negación:** si se niega, la app muestra el aviso para usar **Elegir imagen**.
- APK 1.24 (25) firmado; AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** datos de prueba borrados; la app quedó con Administrador y sin datos de
  negocio.

## Verificación 1.23 (22-10-2026)

- **Título:** la barra superior dice **“Mostrador”**.
- **Botón central:** con datos vacíos aparece la nota y **Completar datos**; al tocarlo se
  abre el menú con QR, llave, cuenta, banco y titular. No hay botones “Editar” por dato.
- **Personalización:** desde Ajustes se abrió **Personalizar Mostrador**, se ocultó QR y se
  movió “Cómo pagar” arriba de “Cuenta”; al guardar y volver, QR desapareció y el orden se
  respetó. El botón central siguió visible por los datos de ejemplo restantes.
- APK 1.23 (24) firmado; AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** datos de prueba borrados; la app quedó con Administrador y sin datos de
  negocio.

## Verificación 1.22 (22-10-2026)

- **El mostrador:** con QR de ejemplo aparece **Configurar QR** y el aviso de que se hace una
  sola vez; el diálogo ofrece **Tomar foto**, **Elegir imagen** y **Cancelar**.
- **Proceso de limpieza:** se eligió una imagen QR local; la app la procesó, la guardó y el
  botón **Configurar QR** desapareció. El QR quedó titulado **“QR”** y se puede abrir en
  grande.
- **Ajustes:** el QR guardado queda disponible para cambiarlo después desde Datos para
  recibir pagos.
- APK 1.22 (23) firmado; AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** datos y foto de prueba borrados; la app quedó con Administrador y sin
  datos de negocio.

## Verificación 1.21 (22-10-2026)

- **El mostrador:** el QR sin configurar dice **“QR de ejemplo”**; con datos o foto dice
  **“QR”**. No aparece “QR de Bre-B”.
- **Ajustes:** etiqueta **“QR”** antes de la vista previa.
- **Vista ampliada:** la barra superior dice **“QR”** y la descripción accesible “Código QR”.
- La llave sigue rotulada **Llave Bre-B**.
- APK 1.21 (22) firmado; AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** datos de prueba borrados; la app quedó con Administrador y sin datos de
  negocio.

## Verificación 1.20 (22-10-2026)

- **El mostrador** ya no muestra “Métodos disponibles” ni Nequi/Daviplata.
- **Ajustes** ya no muestra casillas de métodos ni “Otros métodos”.
- La página queda con QR, llave, cuenta/banco/titular y una guía que usa la cuenta como
  respaldo.
- APK 1.20 (21) firmado; AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** datos de prueba borrados; la app quedó con Administrador y sin datos de
  negocio.

## Verificación 1.19 (22-10-2026)

- **El mostrador** muestra **“Banco: Banco de ejemplo”**, sin la cadena `aaaaaa`.
- APK 1.19 (20) firmado; AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** datos de prueba borrados; la app quedó con Administrador y sin datos de
  negocio.

## Verificación 1.18 (22-10-2026)

- **El mostrador:** el QR sin configurar dice **“QR de Bre-B de ejemplo”**; con datos o foto
  dice **“Escanea el QR de Bre-B”**.
- **Ajustes:** etiqueta visible **“QR de Bre-B”** antes de la vista previa y ayuda que lo
  identifica como Bre-B.
- **Vista ampliada:** la barra superior recibe el título **“QR de Bre-B”** y la descripción
  accesible “Código QR de Bre-B”; no se etiqueta como comprobante.
- APK 1.18 (19) firmado; AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** datos de prueba borrados; la app quedó con Administrador y sin datos de
  negocio.

## Verificación 1.17 (22-10-2026)

- **Etiqueta superior:** “Tienes datos de ejemplo” visible con todos los datos vacíos.
- **Botones Editar:** llave, cuenta, banco, titular y métodos con un botón **Editar** a la
  derecha; ya no aparece “Dato de ejemplo” encima ni al lado.
- **Aviso:** el diálogo de edición dice que el botón desaparecerá al confirmar y que después
  se cambia en Ajustes.
- **Confirmación en dos pasos:** se escribió `LLAVE1`, se tocó Confirmar, apareció el segundo
  aviso y al confirmarlo el valor cambió y el botón Editar de la llave desapareció.
- **Cerrar/afuera:** el diálogo no guarda nada mientras no se confirme el segundo aviso.
- APK 1.17 (18) firmado; AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** datos de prueba borrados; la app quedó con Administrador y sin datos de
  negocio.

## Verificación 1.16 (22-10-2026)

- **Etiquetas a la derecha:** llave `@EJEMPLO2567`, cuenta `#4004EJEMPLO2145`, banco,
  titular y métodos con su etiqueta “Dato de ejemplo” al lado derecho.
- **Edición directa:** se tocó “Dato de ejemplo” de la llave, se escribió `MILLAVE123`, se
  guardó y el valor apareció de inmediato en El mostrador; al abrir Ajustes, el campo
  **Llave Bre-B** ya mostraba `MILLAVE123`.
- APK 1.16 (17) firmado; AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** datos de prueba borrados; la app quedó con Administrador y sin datos de
  negocio.

## Verificación 1.15 (22-10-2026)

- **El mostrador** con todos los datos vacíos muestra “Dato de ejemplo” junto a llave,
  cuenta, banco, titular y métodos, y “QR de ejemplo” en el QR. No aparece el aviso grande.
- APK 1.15 (16) firmado; AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** datos y fotos de prueba borrados; la app quedó con Administrador y sin
  datos de negocio.

## Verificación 1.14 (22-10-2026)

- **Foto del QR:** se eligió una imagen QR local en Ajustes; la app la procesó y mostró un QR
  limpio de 1200 × 1200 px en la vista previa. Al guardar y abrir **El mostrador**, la imagen
  limpia apareció en grande.
- **Botones:** “Tomar foto del QR” y “Elegir imagen del QR” visibles en Ajustes.
- **Fallo controlado:** si el QR no se puede leer, se conserva el anterior y se pide una foto
  más cercana; no se guarda una imagen rota.
- APK 1.14 (15) firmado; AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** datos y fotos de prueba borrados; la app quedó con Administrador y sin
  datos de negocio.

## Verificación 1.12 (21-10-2026)

- **El mostrador** abierto desde el botón **Datos**; QR primero y datos de ejemplo visibles:
  llave `11111111-2222-3333-aaaa-a1b2c3d4e5f6`, cuenta `1111 2222 3333 4444`,
  banco `Banco de ejemplo` y aviso de configuración.
- **Ajustes → Datos para recibir pagos** visible antes de “Borrar todos los datos”, con
  vista previa del QR, campos de Bre-B/cuenta/banco/titular, cuatro métodos y “Otros métodos”.
- Se guardó la llave `MI-LLAVE-123` y una **foto QR** de prueba: El mostrador mostró la foto,
  la llave y el aviso de que los demás campos seguían en ejemplo.
- APK 1.12 (13) firmado; AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** datos de prueba borrados; la app quedó con la cuenta Administrador y
  sin datos de negocio.

## Verificación 1.11 (21-10-2026)

- Migración real v2 → v3 sobre una copia de la versión 1.10 con 3 ventas, 1 compra, 1 pago y
  1 gasto: `PRAGMA user_version=3`, cuenta «Administrador» creada y todas las filas con
  `usuario_id=1`. Las ventas migradas muestran «Administrador».
- Cambio de cuenta probado con Administrador, Nacho, Sonia y Andrea; el nombre del botón
  superior y el menú cambian. Venta #4 registrada como **Nacho** y mostrada como
  “Atendió: Nacho”.
- Menú personalizable: “Bajar” a Nueva venta la intercambió con Nueva compra y el orden quedó
  guardado; las dos conservan el borde grueso.
- Nombre del negocio “Papelería Somor” visible debajo de la fecha; el texto fijo anterior ya
  no aparece.
- Páginas de accesos, perfil, personas y menú en verde bosque (#245B43) con fondos claros;
  terminal de ventas en azul.
- APK 1.11 (12), AAB validado; solo permiso de cámara; sin internet.
- **Limpieza final:** se borraron los datos y las copias de prueba; la app quedó con la
  cuenta Administrador y sin datos de negocio.

## Verificación 1.10 (21-10-2026)

- Instalación `adb install -r` → `Success`; versión 1.10, versionCode 11, targetSdk 35.
- APK con **531 KB** y solo el permiso `android.permission.CAMERA`; sin internet.
- **Lector de códigos de barras** probado con una imagen EAN-13 (`4006381333931`) en
  Inventario: abrió el producto existente con el código escrito.
- **Lector QR** probado con una imagen QR (`LAP-012`): agregó “Caja de lápices x12” a una
  venta; el total y la ganancia se actualizaron.
- **Modo predeterminado:** Ajustes mostró QR después de cambiarlo dentro del lector; al
  volver a “Código de barras” y abrir el lector, quedó activo Barras.
- **Permiso denegado:** se negó la cámara y el lector mostró el aviso, ocultó la linterna y
  dejó disponibles el código manual y la imagen.
- **Código manual:** se escribió `MANUAL-123`, “Usar” cerró el lector y devolvió el código.
- **Cámara:** la actividad abrió la cámara (proceso `cameraserver` activo) y mostró la guía;
  la lente estaba tapada en el escritorio, así que la lectura se validó con imágenes y no con
  un código físico.
- **Decodificador ZXing:** prueba JVM con EAN-13 y QR; EAN leído, QR leído y QR rechazado en
  modo barras.
- **AAB:** `bundletool validate` correcto; 529 KB.
- **Pendiente honesto:** no se pudo probar el escaneo con un código impreso porque la cámara
  de la tablet está sobre el escritorio/tapada. El flujo de cámara, permiso, imagen y retorno
  sí quedó probado.

## Verificación histórica (18-10-2026)

En el dispositivo por USB:

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
| ![Iconos de Compras y Proveedores separados](capturas/23-inicio-proveedores.png) | ![Lector de códigos](capturas/24-escaner.png) |
| ![Venta con producto escaneado](capturas/25-venta-escaneada.png) | ![Ajustes del lector](capturas/26-ajustes-lector.png) |
| ![Lector sin permiso de cámara](capturas/27-escaner-sin-permiso.png) | ![Administrador de Accesos](capturas/28-accesos.png) |
| ![Perfil de la cuenta](capturas/29-perfil.png) | ![Orden del menú por cuenta](capturas/30-menu-cuenta.png) |
| ![Inicio con nombre del negocio](capturas/31-inicio-negocio.png) | ![Venta con la persona que atendió](capturas/32-venta-atendio.png) |
| ![El mostrador con QR y datos](capturas/33-mostrador.png) | ![Datos para recibir pagos](capturas/34-ajustes-pagos.png) |
| ![Inicio con el botón Datos](capturas/35-inicio-datos.png) | ![QR limpio generado desde una foto](capturas/36-qr-limpio.png) |
| ![QR limpio en El mostrador](capturas/37-mostrador-qr-limpio.png) | ![Aviso al editar un dato](capturas/38-editar-dato.png) |
| ![Confirmar el dato](capturas/39-confirmar-dato.png) | ![Configurar QR una vez](capturas/40-configurar-qr.png) |
| ![Completar datos desde el botón central](capturas/41-completar-datos.png) | ![Personalizar Mostrador](capturas/42-personalizar-mostrador.png) |
| ![Mostrador personalizado](capturas/43-mostrador-personalizado.png) | |

## Compilar e instalar

Requisitos: Android SDK en `~/android-sdk` con **build-tools/35.0.0** y
**platforms/android-35**, OpenJDK 21 y ZXing en
`herramientas/zxing-core-3.5.3.jar`. No hace falta Gradle ni Android Studio.

```bash
cd ~/proyectos/minegocio-tablet
VERSION_CODE=25 VERSION_NAME=1.24 ./compilar.sh
adb install -r minegocio.apk
```

El script compila recursos, Java, DEX (incluye ZXing), alinea, firma con la llave de
desarrollo y verifica. Para cambiar la versión:
`VERSION_CODE=26 VERSION_NAME=1.25 ./compilar.sh`.

Para Play:

```bash
VERSION_CODE=25 VERSION_NAME=1.24 ./empaquetar-aab.sh
```

Genera `minegocio.aab` y lo valida con `bundletool`.

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
├── minegocio.aab
├── LICENCIAS.md
├── herramientas/     # ZXing, bundletool
├── capturas/
├── res/
│   ├── layout/          # pantallas, filas, filtros y tarjetas
│   ├── drawable/        # iconos y fondos
│   └── values/          # colores, estilos y textos
└── src/com/polypodiozoa/minegocio/
    ├── datos/           # SQLite v3, usuarios, deudas, adjuntos, respaldo
    ├── modelo/Modelo.java
    ├── ui/              # actividades (incluye DeudasActivity y EscanerActivity)
    └── util/            # COP, fechas, imágenes, Excel, PDF, lector ZXing, UI
```

## Límites conocidos

- **No facturación electrónica DIAN** ni retenciones/nómina.
- **No sincronización en la nube** ni multiusuario; un dispositivo, una base local.
- **Compras a crédito no generan cuentas por pagar**; sí se controlan ventas fiadas y abonos.
- Los abonos se reparten por vencimiento (o por la venta elegida), no con un plan de cuotas
  personalizado.
- Para producción: llave propia, política de datos personales (Ley 1581 de Colombia) e
  integración DIAN si se requiere.

## Cómo revertir

- Desinstalar la app borra sus datos locales; no afecta al resto del dispositivo.
- Guarda el APK anterior y una copia ZIP; instala con la misma llave de firma y restaura.
- No se modifican ajustes del sistema ni archivos del usuario. Los únicos archivos externos
  son los que el usuario exporta (PDF, CSV, XLSX, ZIP).
