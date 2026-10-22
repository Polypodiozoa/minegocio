# Mi Negocio — registro de diseño

> Este documento guarda **por qué** la app es como es: para quién se hizo, qué se decidió,
> cómo se ve y qué queda pendiente. El manual de uso está en [`LEEME.md`](LEEME.md) y los
> problemas conocidos en [`AUDITORIA.md`](AUDITORIA.md).

## 1. Para quién es

Un **micronegocio colombiano atendido por una sola persona**: tienda de barrio, papelería,
tienda de ropa, ferretería pequeña. No hay empleados, no hay servidor y muchas veces no hay
internet confiable.

Lo que esa persona necesita del sistema, en sus palabras:

- «Que no se me pierda la plata»: saber qué vendió hoy, cuánto ganó y quién le debe.
- «Que no me toque escribir mucho»: precios y cantidades grandes, sin fórmulas.
- «Que el fiado quede anotado»: a quién le fió, cuánto, para cuándo y cuánto ha abonado.
- «Que no me pida internet ni permisos raros»: el dispositivo es del negocio, no de Google. La cámara solo se pide cuando se abre el lector de códigos.

Dispositivo de referencia: **Amazon Fire HD 8** (800 × 1280 px, 213 dpi, Android 9), en
horizontal y vertical, con letra del sistema hasta 1,3×.

## 2. Principios de diseño

1. **Todo pasa en el dispositivo.** Sin cuentas, sin nube, sin internet. El único permiso es la cámara, y solo mientras el usuario escanea un código. La base SQLite y las fotos
   viven en el almacenamiento privado de la app. La única salida son las copias que el
   usuario decide guardar y los informes que decide compartir.
2. **Pesos enteros, sin centavos.** En Colombia el peso no se usa con centavos en el día a
   día. Todos los montos son `long` en pesos; el único porcentaje es el impuesto opcional.
3. **Un número grande manda.** Cada pantalla tiene una cifra principal (el total de la venta,
   el saldo del cliente, la utilidad del mes) y el resto es secundario.
4. **Nada se borra por accidente.** Las acciones destructivas piden confirmación, las ventas y
   compras se **anulan** (no se borran) y un producto con historial no se puede eliminar.
5. **El error se dice en el campo, no en un aviso que se va.** Los formularios marcan el
   campo con el problema y lo anuncian al lector de pantalla.
6. **Español de Colombia, sin jerga contable.** «Te deben», «fiado», «abono», «plata», no
   «cuentas por cobrar» ni «pasivo corriente».

## 3. Mapa de pantallas

30 actividades. El inicio es un tablero con **12 accesos grandes** en dos columnas (icono de 34 dp y etiqueta de 18 sp) y dos botones de cabecera: **Datos** y **Administrador**.

| Acceso | Pantalla | Para qué |
|---|---|---|
| Nueva venta | `VentaActivity` | Registrar una venta de contado o fiada. |
| Nueva compra | `CompraActivity` | Cargar mercancía: suma existencias y actualiza el costo. |
| Ventas | `VentasActivity` → `VentaDetalleActivity` | Historial, detalle, comprobante PDF y anulación. |
| Compras | `ComprasActivity` → `CompraDetalleActivity` | Historial de compras y anulación. |
| Inventario | `InventarioActivity` → `ProductoActivity` | Productos, existencias, filtros y ficha con foto. |
| Clientes | `ClientesActivity` → `ClienteActivity` / `ClienteDetalleActivity` | Ficha, cupo de fiado, deudas e historial. |
| Deudas | `DeudasActivity` → `PagoActivity` | Cartera de fiados: vencidas, por vencer y abonos. |
| Proveedores | `ProveedoresActivity` → `ProveedorActivity` / `ProveedorDetalleActivity` | Contacto e historial de compras. |
| Gastos | `GastosActivity` → `GastoActivity` | Gastos del negocio con comprobante. |
| Informes | `InformesActivity` | Períodos, utilidad, cartera, exportación a Excel/CSV. |
| Perfil | `PerfilActivity` | Cuenta actual, resumen de ventas/gastos, edición y menú propio. |
| Datos / Mostrador | `MostradorActivity` | QR, llave Bre-B, cuenta, banco y titular; módulos visibles y orden configurables. |
| Ajustes | `AjustesActivity` | Datos del negocio, impuesto, tamaño de texto, modo del lector, datos de pago, copia y borrado. |

Apoyo: `SelectorActivity` (elegir producto, cliente o proveedor), `EscanerActivity`
(leer un código de barras o QR), `AccesosActivity` (selector verde de personas),
`UsuariosActivity`/`UsuarioActivity` (alta y edición), `MenuPerfilActivity` (orden del menú
por cuenta) y `FotoActivity` (ver una foto o comprobante a pantalla completa).

## 3.1 Personas y accesos

El negocio puede compartir una tablet entre varias personas. La cuenta actual es la que
firma; no es un login remoto. En la parte superior de Inicio hay un botón con icono de
persona y el nombre actual que abre el **Administrador de Accesos**.

- **Verde bosque** (`#245B43`) para las páginas de administración (accesos, perfil, personas
  y menú), frente al azul de la terminal. Es un verde oscuro, no fluorescente.
- **Tarjetas** de personas con inicial, nombre, rol y “En uso”; tocar una cambia la cuenta.
- **Rol informativo**: Administrador o Empleado. No hay PIN; el selector es de confianza para
  un dispositivo compartido, no una barrera de seguridad.
- **Historial**: una cuenta con ventas no se borra, se desactiva; los registros conservan su
  nombre.
- **Menú por cuenta:** el orden de «¿Qué quieres hacer?» se guarda en `usuarios.menu`. Nueva
  venta y Nueva compra mantienen el borde de 2 dp.
- **Inicio:** el nombre del negocio aparece debajo de la fecha; el texto fijo de módulos se
  retiró. El botón de usuario queda arriba a la derecha.

## 3.2 El mostrador

El botón **Datos** de la cabecera abre una página para mostrarle al cliente cómo pagar. Es
una página deslizable, con el QR primero y los datos después:

1. **QR de pagos** grande, con foto elegida por el negocio o un QR de ejemplo generado.
2. **Llave Bre-B**, número de cuenta, banco/identificador y titular.
3. Una guía de pago centrada en Bre-B y el número de cuenta como respaldo.

Los datos se configuran en **Ajustes → Datos para recibir pagos**, antes de “Borrar todos los
datos”: foto del QR, llave, cuenta, banco y titular. No hay métodos. Si faltan, se muestran
valores de ejemplo
evidentes (`11111111-2222-3333-aaaa-a1b2c3d4e5f6`, `1111 2222 3333 4444`,
`Banco de ejemplo`) y cada uno lleva su etiqueta **“Dato de ejemplo”**. La foto se guarda en la carpeta privada; no se envía.

## 3.3 Lector de códigos

El lector nace de dos tareas concretas: **cargar el inventario** sin teclear cada referencia y
**agregar un producto a la venta** sin buscarlo en una lista. Por eso aparece en la ficha del
producto, en Inventario y en Nueva venta, no en una pantalla aislada.

- **Código de barras por defecto**; QR se elige en Ajustes o dentro del lector y se recuerda.
- **Cámara en vivo** con guía, estado visible y linterna si el dispositivo la tiene.
- **Salidas alternas:** código escrito a mano y foto del código. Así el lector no bloquea la
  venta si el permiso se niega, la cámara falla o el código está impreso en un papel.
- **Un código = una referencia.** Al guardar un producto se comprueba que el código no esté
  asignado a otro; si lo está, se avisa en el campo.
- **Lectura local:** ZXing 3.5.3 (Apache 2.0), sin internet ni servicios externos. No se
  guardan fotogramas; la foto elegida se procesa en memoria.
- **Accesibilidad:** botones de 52–56 dp, modo activo escrito, estado anunciado y alternativa
  manual para quien no pueda sostener la cámara.

![Inicio con datos de ejemplo](capturas/01-inicio.png)
![Nueva venta](capturas/03-nueva-venta.png)
![Cartera de fiados](capturas/15-deudas.png)
![Informes](capturas/07-informes.png)

## 4. Sistema visual

### Color

La paleta es una sola: azul petróleo para la marca y la acción, ámbar para lo que exige
atención, verde y rojo solo para estados. **Todos los pares de texto cumplen WCAG AA**
(mínimo 4,5:1; se mide con la fórmula de luminancia relativa):

| Uso | Texto | Fondo | Contraste |
|---|---|---|---|
| Texto principal | `#14202B` | `#FFFFFF` | 16,52:1 |
| Texto secundario | `#44525C` | `#FFFFFF` | 8,05:1 |
| Texto secundario sobre gris | `#44525C` | `#F2F5F7` | 7,36:1 |
| Botón primario | `#FFFFFF` | `#0B4F6C` | 8,94:1 |
| Texto sobre azul claro | `#0B4F6C` | `#DCEAF2` | 7,28:1 |
| Acento / advertencia | `#B45309` | `#FFFFFF` | 5,02:1 |
| Éxito | `#176B3A` | `#DDEFE4` | 5,48:1 |
| Error | `#A3231C` | `#F7DDDB` | 5,80:1 |
| Aviso | `#7A4A00` | `#FFF0C9` | 6,61:1 |

El borde de los campos (`#C3CDD4`) da 1,61:1 sobre blanco: por debajo del 3:1 que pide
WCAG 1.4.11 para el contorno de un control. Es una corrección pendiente (ver `AUDITORIA.md`).

### Tipografía

Una sola familia (sans-serif del sistema) y una escala corta, sin tamaños menores de 14 sp:

| Estilo | Tamaño | Uso |
|---|---|---|
| `ValorGrande` | 34 sp | Cifra principal del resumen |
| `ValorMedio` | 24 sp | Totales secundarios |
| `TituloSeccion` | 20 sp | Título de tarjeta o sección |
| `Campo`, `BotonPrimario`, `Valor`, `TextoLista` | 18 sp | Campos, botones, filas |
| `BotonTexto` | 17 sp | Acciones de texto |
| `Etiqueta` | 16 sp | Rótulo de un campo |
| `Ayuda` | 15 sp | Texto de apoyo |
| `EstadoBadge` | 14 sp | Chips de estado |

La app tiene además su propio **tamaño de texto** (Normal / Grande 1,15× / Muy grande 1,3×)
que se multiplica con el del sistema, en Ajustes.

### Componentes

- **Tarjeta** (`Tarjeta`): fondo blanco, radio 16 dp, padding 16 dp, separación 14 dp.
- **Campo** (`Campo`): 56 dp de alto, 18 sp, texto de ayuda arriba y `labelFor` cuando aplica.
- **Botón primario/secundario/peligro**: 58 dp de alto, 18 sp en negrita, sin mayúsculas
  forzadas, sin sombra animada.
- **Botón de texto**: 52 dp.
- **Fila de lista** (`fila_lista.xml`): título de 18 sp, subtítulo de 15 sp y valor a la
  derecha; el estado va como chip de texto, nunca solo por color.
- **Badge de estado**: 14 sp en negrita con fondo de color suave.

### Iconografía

Iconos vectoriales de 24 × 24 dp (estilo Material, relleno), teñidos con el azul de marca y
dibujados a 34 dp en el inicio. Cada módulo tiene un icono distinto: camión para Compras,
edificio para Proveedores, caja para Inventario, gente para Clientes, calendario para Deudas,
signo de pesos para Gastos y barras para Informes. Los iconos de cantidad (+/−) y de quitar
llevan descripción para el lector de pantalla.

## 5. Flujos

### Venta

1. **Cliente** (opcional; obligatorio si es fiada).
2. **Productos** (descuenta inventario) o **Concepto** libre (no toca inventario).
3. Cantidad con +/−, descuento e impuesto opcional.
4. **Forma de pago**: Efectivo, Transferencia, Tarjeta o **Crédito**.
5. Si es crédito: **fecha límite** (30 días por defecto, no anterior a hoy).
6. Confirmación con total, vence y ganancia estimada → se registra en una transacción.

### Fiado y abono

- La venta fiada suma al **saldo del cliente** y aparece en **Deudas** con su vencimiento.
- El **cupo de fiado** por cliente avisa (no bloquea) cuando se va a superar.
- El abono se registra desde la deuda o desde la ficha del cliente; se reparte entre las
  deudas por vencimiento más próximo, salvo que se elija una venta concreta.
- Un cliente puede tener saldo a favor si paga de más.

### Compra

Proveedor, productos con costo unitario, impuesto, forma de pago y foto/PDF de la factura.
Suma existencias y actualiza el costo del producto.

### Informe y exportación

Períodos: hoy, esta semana, este mes, mes anterior, este año, todo. Indicadores: ventas,
ingreso, costo de lo vendido, utilidad bruta, impuestos cobrados y pagados, gastos, utilidad
neta, cartera y top de productos. Exporta **Excel** (Resumen, Ventas, Productos, Deudas,
Gastos) y **CSV**.

### Copia y restauración

Un `.zip` con la base, las fotos y un `LEEME.txt`, guardado donde el usuario elija. La
restauración valida el ZIP, bloquea rutas maliciosas y conserva una copia de la base anterior.

## 6. Modelo de datos

SQLite en WAL, 12 tablas:

| Tabla | Qué guarda |
|---|---|
| `ajustes` | Nombre y NIT del negocio, impuesto por defecto, preferencias. |
| `productos` | Nombre, categoría, código, unidad, precio, costo, existencia, mínimo, foto, activo. |
| `clientes` | Contacto, notas, foto y **cupo de fiado**. |
| `proveedores` | Contacto, notas, foto. |
| `ventas` / `venta_items` | Cabecera (subtotal, descuento, impuesto, total, costo, forma de pago, estado, vencimiento, anulada) y líneas con el costo del momento. |
| `compras` / `compra_items` | Igual, del lado de la compra. |
| `pagos` / `pago_aplicaciones` | Abonos del cliente y a qué venta se aplicó cada peso. |
| `gastos` | Descripción, categoría, monto, fecha, comprobante. |
| `adjuntos` | Fotos y documentos ligados a una venta, compra, gasto o pago. |

Los montos son enteros (`INTEGER`) en pesos. Las ventas guardan el **costo unitario del
momento de la venta**, para que la utilidad histórica no cambie cuando sube el costo.

## 7. Accesibilidad

Lo que ya está:

- Contraste AA en todo el texto (ver tabla), texto de 14 a 34 sp, campos de 56 dp y botones
  de 52 a 58 dp.
- Descripciones en los botones de icono (más, menos, quitar), anuncios al cambiar la
  cantidad y diálogos con botones de 17 sp.
- Los formularios marcan el campo con error y lo enfocan.
- Tamaño de texto propio de la app, además del del sistema.

Lo que falta (detalle en `AUDITORIA.md`): asociar con `labelFor` los selectores y los campos
de los diálogos; subir a 48 dp los botones de diálogo (hoy 39 dp por un `setMinHeight` en
píxeles) y el de «Ver comprobante» (44 dp); oscurecer el borde de los campos.

## 8. Decisiones y descartes

| Decisión | Por qué |
|---|---|
| Java nativo sin Gradle | Compila en segundos con `javac` + `d8` y no exige Android Studio en un equipo que no lo tiene. |
| SQLite a mano | Una sola persona, un solo proceso; Room no aporta y suma dependencias. |
| Sin internet ni cuentas | Requisito del usuario y de los datos: no hay servidor, no hay nube, no hay permisos. |
| Pesos enteros | Evita errores de coma flotante en dinero. |
| Impuesto opcional por venta | Muchos micronegocios no cobran IVA; el que lo cobra lo activa. |
| Anular en vez de borrar | El historial es la contabilidad del negocio. |
| Copia en un `.zip` que elige el usuario | El dispositivo puede perderse o venderse; la copia sale de él. |
| Iconos propios de 24 dp | Sin librerías de iconos: el APK pesa 255 KB y no hay dependencias. |

## 9. Problemas conocidos

Resumen; el detalle con archivo y línea está en [`AUDITORIA.md`](AUDITORIA.md).

Corregido en la 1.4:

- **Cartera con dos fórmulas** → ahora los abonos se reparten de nuevo en cada cambio y lo
  que sobra es un saldo a favor que la próxima venta fiada consume.
- **Restauración no atómica** y borrado de fotos cuando la copia no las trae.
- **«Eliminar todos los datos»** deja copias internas.
- Al anular una compra no se restauraba el costo anterior.
- Los formularios de cliente, producto, proveedor, gasto y pago no avisaban al salir.
- Etiquetas y tamaños táctiles de los diálogos, y borde de los campos.

Sigue pendiente:

- Si la base no abre, la app no tiene pantalla de recuperación (hay que borrar datos o
  reinstalar y restaurar una copia desde Ajustes, que tampoco abre).

## 10. Publicación en Google Play (pendiente)

La app se va a publicar en Play después de los ciclos de prueba. Lo que falta para eso:

| Requisito | Estado |
|---|---|
| **targetSdk** | **Hecho en 1.9**: compila y apunta a **API 35**. Falta probarlo en un equipo con Android 15 (aquí solo hay uno con Android 9). |
| **Firma** | El APK va firmado con la llave de **desarrollo**. Para Play hace falta una llave de subida propia (la crea el dueño, no el agente) y activar Play App Signing. |
| **Formato** | **Hecho en 1.9**: `./empaquetar-aab.sh` produce `minegocio.aab`, validado con bundletool. |
| **Ficha** | Textos y gráfico listos en `tienda/` (nombre, descripciones, categoría, gráfico 1024 × 500). Faltan capturas de teléfono y el correo de soporte. |
| **Privacidad** | Texto listo en `tienda/privacidad.md` y respuestas en `tienda/seguridad-datos.md`. Falta publicarlo en una URL. |
| **Clasificación** | Cuestionario de contenido y público objetivo (negocios y adultos; no es para niños). |
| **Pruebas cerradas** | Las cuentas nuevas deben pasar una prueba cerrada con testers antes de publicar. |

## 11. Historial

| Versión | Fecha | Cambio |
|---|---|---|
| 1.0 | 2026-10-18 | Primera versión: inventario, ventas, compras, clientes, proveedores, gastos, informes. |
| 1.1 | 2026-10-18 | Cartera de fiados, cupos, abonos, rentabilidad, Excel y alertas. |
| 1.2 | 2026-10-18 | Iconos de Compras y Proveedores separados. |
| 1.3 | 2026-10-18 | «Ingresos (con impuesto)» pasa a «Ingreso» en Informes y Excel. |
| 1.4 | 2026-10-18 | Correcciones de la auditoría: cartera, restauración, copias, costo al anular compra, aviso al salir y accesibilidad. |
| 1.5 | 2026-10-18 | Pendientes: base dañada se aparta y avisa, aviso de copia grande, abono con confirmación, exportación en segundo plano y bloqueo de doble toque. |
| 1.6 | 2026-10-18 | Restauración sin tope fijo: se compara con el espacio libre real. Menos espacio en caché al restaurar. |
| 1.7 | 2026-10-18 | Claridad de cliente y concepto libre, tamaño de texto en toda la app, formas de pago sin Nequi/Daviplata y costo unitario visible. |
| 1.8 | 2026-10-18 | «Mi Negocio» con mayúsculas en app y PDF; «dispositivo» en vez de «tablet»; checklist de Google Play. |
| 1.9 | 2026-10-18 | targetSdk 35, empaquetado AAB y textos de tienda y privacidad. |
| 1.10 | 2026-10-21 | Lector de códigos de barras (predeterminado) y QR, integrado en producto, inventario y venta; permiso de cámara acotado al lector. |
| 1.11 | 2026-10-21 | Personas y accesos con selector verde, perfil, autoría de ventas/compras/pagos/gastos, menú propio por cuenta, nombre del negocio bajo la fecha y bordes principales. |
| 1.12 | 2026-10-21 | El mostrador con QR, llave Bre-B, cuenta, banco, titular, métodos configurables y datos de ejemplo. |
| 1.13 | 2026-10-21 | Mensaje de la app: «Hecha para negocios Colombianos». |
| 1.14 | 2026-10-22 | Foto del QR de pagos: lectura, regeneración limpia en blanco y negro y guardado local. |
| 1.15 | 2026-10-22 | Etiquetas «Dato de ejemplo» por dato en El mostrador; sin aviso global. |
| 1.16 | 2026-10-22 | Etiquetas a la derecha y edición directa del dato desde El mostrador, sincronizada con Ajustes. |
| 1.17 | 2026-10-22 | Etiqueta «Tienes datos de ejemplo», botones Editar que desaparecen y confirmación en dos pasos. |
| 1.18 | 2026-10-22 | QR rotulado como Bre-B en El mostrador, Ajustes y vista ampliada. |
| 1.19 | 2026-10-22 | Se quita «aaaaaa» del banco de ejemplo. |
| 1.20 | 2026-10-22 | Se retiran Métodos, Nequi y Daviplata; solo QR, llave y cuenta de respaldo. |
| 1.21 | 2026-10-22 | El QR se titula solo «QR»; se retira «de Bre-B» del código. |
| 1.22 | 2026-10-22 | Configurar QR desde El mostrador una sola vez, con limpieza; después en Ajustes. |
| 1.23 | 2026-10-22 | Título «Mostrador», botón central Completar datos y módulos ocultables/reordenables. |
| 1.24 | 2026-10-22 | Permiso de cámara para el QR y corrección de «desde el mostrador». |
