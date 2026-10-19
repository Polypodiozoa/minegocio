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
- «Que no me pida internet ni permisos raros»: la tablet es del negocio, no de Google.

Dispositivo de referencia: **Amazon Fire HD 8** (800 × 1280 px, 213 dpi, Android 9), en
horizontal y vertical, con letra del sistema hasta 1,3×.

## 2. Principios de diseño

1. **Todo pasa en la tablet.** Sin cuentas, sin nube, sin permisos. La base SQLite y las fotos
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

23 actividades. El inicio es un tablero con **11 accesos grandes** (dos columnas, icono de
34 dp y etiqueta de 18 sp):

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
| Ajustes | `AjustesActivity` | Datos del negocio, impuesto, tamaño de texto, copia y borrado. |

Apoyo: `SelectorActivity` (elegir producto, cliente o proveedor) y `FotoActivity` (ver una
foto o comprobante a pantalla completa).

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
4. **Forma de pago**: Efectivo, Transferencia, Nequi/Daviplata, Tarjeta o **Crédito**.
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
| Copia en un `.zip` que elige el usuario | La tablet puede perderse o venderse; la copia sale del dispositivo. |
| Iconos propios de 24 dp | Sin librerías de iconos: el APK pesa 255 KB y no hay dependencias. |

## 9. Problemas conocidos

Resumen; el detalle con archivo y línea está en [`AUDITORIA.md`](AUDITORIA.md).

- **Cartera con dos fórmulas**: Inicio/Informes usan abonos totales y Deudas usa lo aplicado
  por venta; al anular una venta fiada con abonos, las dos cifras se contradicen.
- **Restauración no atómica** y borra las fotos actuales si la copia no las trae.
- **«Eliminar todos los datos»** deja la base anterior de una restauración en `files/`.
- Al anular una compra no se restaura el costo anterior del producto.
- Los formularios de cliente, producto, proveedor, gasto y pago no avisan al salir.

## 10. Historial

| Versión | Fecha | Cambio |
|---|---|---|
| 1.0 | 2026-10-18 | Primera versión: inventario, ventas, compras, clientes, proveedores, gastos, informes. |
| 1.1 | 2026-10-18 | Cartera de fiados, cupos, abonos, rentabilidad, Excel y alertas. |
| 1.2 | 2026-10-18 | Iconos de Compras y Proveedores separados. |
| 1.3 | 2026-10-18 | «Ingresos (con impuesto)» pasa a «Ingreso» en Informes y Excel. |
| 1.4 | pendiente | Correcciones de la auditoría (cartera, restauración, accesibilidad). |
