# Auditoría de Mi Negocio 1.3 — 18 de octubre de 2026

Revisión pedida por Alejandro: **cálculos, accesibilidad, usabilidad, bugs, seguridad de
datos y fallas**. Método: lectura completa del código (8.729 líneas Java, 75 layouts,
manifiesto), reproducción de la lógica de saldos contra el mismo SQL de la app, cálculo de
contrastes WCAG con la fórmula de luminancia relativa y verificación del APK instalado.

**No se cargaron datos de ejemplo ni se usó TalkBack real** sobre el dispositivo, para no tocar
los datos del negocio. Las cifras de cartera salen de ejecutar las consultas reales sobre
una base con el mismo esquema.

## Estado al 18/10/2026, noche — corregido hasta la versión 1.6

| Hallazgo | Estado |
|---|---|
| C1 · dos «por cobrar» | **Corregido.** `recalcularAplicaciones` reparte de nuevo los abonos al anular una venta, al registrar una venta fiada y al borrar un abono; `porCobrar` suma los saldos por venta. Probado en el dispositivo: tras anular una venta fiada con abono, Inicio y Deudas dan 0; el abono queda como saldo a favor de $100.000 y una venta fiada nueva de $10.115 lo consume (queda en $89.885). |
| C2 · restauración no atómica | **Corregido.** Validación con `PRAGMA quick_check`, escritura en `minegocio.db.nueva` y reemplazo con `renameTo`. |
| C3 · fotos borradas | **Corregido.** Sin carpeta `adjuntos/` en la copia, no se tocan las fotos actuales. |
| A1 · «borrar todo» deja copias | **Corregido** (incluye `VACUUM`). |
| A3 · costo al anular compra | **Corregido.** Vuelve al de la última compra vigente. |
| A4 · salir sin guardar | **Corregido** en cliente, producto, proveedor, gasto y abono. |
| A5 · sobrepago | **Corregido.** El saldo a favor se aplica a la próxima venta fiada. |
| AC1 · etiquetas | **Corregido** con `labelFor` en selectores, notas y diálogos. |
| AC2 · 48 dp | **Corregido** (52 dp reales en diálogos, 48 dp en «Ver comprobante»). |
| AC3 · borde 1,61:1 | **Corregido** a 3,51:1 (`#7C8B95`). |
| M3 · borrado sin transacción | **Corregido** en `eliminarPago`. |
| A2 · base ilegible | **Corregido en 1.5.** La base dañada se aparta (no se borra), la app arranca vacía y avisa con acceso a «Restaurar una copia». |
| A6 · copia > 1 GB | **Corregido en 1.6.** Se quitó el tope fijo: la restauración compara el tamaño real de la copia con el espacio libre y avisa con números si no alcanza. |
| M1, M2, M4 | **Corregidos en 1.5.** Abono con confirmación, Excel/CSV en segundo plano y bloqueo de doble toque. |

## Verificado sin hallazgo

- **Ventas y compras:** subtotal, descuento, impuesto (`Math.round(base × pct/100)`), costo,
  utilidad y margen correctos; montos en `long`, sin desbordes realistas.
- **Fechas:** rangos de hoy, semana (lunes a hoy), mes, mes anterior, año y «todo el tiempo»
  correctos, en hora de Bogotá y sin DST.
- **Parseo:** `Formatos.parsePesos/parseDecimal/parseInt` nunca lanzan; aceptan `$`, puntos y
  comas; claman cantidades a 1.000.000.
- **Escrituras:** ventas, compras y pagos usan `beginTransaction/commit` con `finally`;
  claves foráneas activas; cursores con `try-with-resources`.
- **Copia:** bloqueo de *zip slip* (rutas con `..`, absolutas, carpetas anidadas), lista
  blanca de nombres, límite de 1 GB y 5.000 archivos, validación de cabecera SQLite y
  rollback ante error de E/S.
- **Datos locales:** `allowBackup=false`, sin permiso de internet, `FileProvider` no
  exportado, sin `Log` con datos del negocio, archivos temporales con límite de 60 MB.
- **Estabilidad:** ninguna caída de la app en el registro del dispositivo; las dos
  `FATAL EXCEPTION` del día son de otra aplicación (inyección de eventos).
- **Contraste de texto:** todos los pares ≥ 4,5:1 (16,52 / 8,05 / 8,94 / 5,02 / 5,48 / 5,80).

## Crítico

### C1. Dos «por cobrar» distintos que pueden contradecirse

- **Dónde:** `BD.java:478-481` (`porCobrar`, suma de abonos) frente a `BD.java:998-1030`
  (`deudas`, saldo por venta con `pago_aplicaciones`).
- **Escenario:** dos fiados de 100.000; el cliente abona 100.000 al primero; se anula esa
  venta. `anularVenta` (`BD.java:716-739`) borra las aplicaciones (`:734`) pero no reparte
  ese abono entre las demás deudas.
- **Evidencia (mismas consultas, base de prueba):**

  | Momento | Inicio «Por cobrar» | Deudas |
  |---|---|---|
  | Antes de anular | 100.000 | 100.000 |
  | Después de anular | **0** | **100.000** |

  La ficha del cliente muestra «Al día» y debajo la deuda de 100.000.
- **Corrección:** un solo criterio. Calcular `porCobrar` como suma de los saldos por venta y
  `saldoCliente` como esa suma menos los abonos sin aplicar; al anular una venta, volver a
  repartir todos los abonos del cliente entre sus deudas abiertas.

### C2. La restauración escribe sobre la base viva

- **Dónde:** `Respaldo.java:153` (`copiarArchivo(nuevaBase, baseActual)`), tras borrar el
  `-wal` en `:134-136`.
- **Escenario:** batería, disco lleno o el sistema matando la app a mitad de la copia → base
  truncada. El rollback de `:155-165` solo cubre `IOException`; una muerte de proceso no pasa
  por ahí. El mensaje de error (`AjustesActivity:227`) dice que los datos no se modificaron.
- **Corrección:** copiar a `minegocio.db.nueva`, validar con `PRAGMA integrity_check`, cerrar
  la base y hacer `renameTo` (atómico). Al arrancar, si la base no abre y existe
  `copia-antes-de-restaurar.db`, ofrecer recuperarla.

### C3. Restaurar una copia sin fotos borra todas las fotos

- **Dónde:** `Respaldo.java:142-147`.
- **Escenario:** una exportación interrumpida deja un ZIP con la base y sin `adjuntos/`. Al
  restaurarlo se borra `files/adjuntos` y no se copia nada: se pierden todos los
  comprobantes y fotos actuales.
- **Corrección:** borrar el destino parcial si la exportación falla y exigir en el ZIP un
  manifiesto coherente antes de tocar nada.

## Alto

| # | Hallazgo | Dónde | Corrección |
|---|---|---|---|
| A1 | **«Eliminar todos los datos» no borra** `copia-antes-de-restaurar.db`, `adjuntos-antes-de-restaurar/` ni `errores.log`: quedan datos del negocio en un dispositivo que se cree limpio. | `AjustesActivity.java:252-272`, `Respaldo.java:122-124` | Borrarlos en `borrarTodo()` y hacer `VACUUM`. |
| A2 | **Base ilegible = app inutilizable**: si la base no abre, falla también Ajustes y no se llega a «Restaurar una copia». | `BaseActivity.java:58` | Envolver la apertura; si falla, pantalla con «Restaurar copia» o «Empezar de cero». |
| A3 | **Anular una compra no restaura el costo**: `guardarCompra` pisa el costo del producto y la anulación no lo revierte. | `BD.java:857` vs `:870-892` | Guardar el costo anterior o recalcularlo con la última compra no anulada. |
| A4 | **Salir a mitad de un formulario pierde lo escrito** en Cliente, Producto, Proveedor, Gasto y Pago: solo Venta y Compra confirman. | `onBackPressed` ausente; p. ej. `ClienteActivity`, `GastoActivity` | Confirmar al salir si hay cambios. |
| A5 | **El sobrepago no se aplica a la próxima venta fiada**: queda como «saldo a favor» pero la deuda nueva no lo consume. | `BD.java:947-977`, `PagoActivity.java:93-111` | Al crear una venta fiada, aplicar los abonos sin aplicar del cliente. |
| A6 | **Una copia de más de 1 GB o 5.000 archivos no se puede restaurar**; la exportación no avisa ni tiene límite. | `Respaldo.java:26-27, 83, 105` | Avisar al exportar y validar al importar. |

## Medio

| # | Hallazgo | Dónde |
|---|---|---|
| M1 | El abono se registra con un solo toque, sin confirmación. | `PagoActivity.java:93-111` |
| M2 | CSV y Excel se generan en el hilo principal: con años de historial puede salir «la app no responde». | `InformesActivity.java:186-228` |
| M3 | Borrados sin transacción (un pago son tres sentencias sueltas). | `BD.java:939-944` |
| M4 | «Aprobar y registrar» y «Guardar abono» no tienen bloqueo de doble toque. | `VentaActivity.java:513-527`, `PagoActivity.java:93-111` |

## Accesibilidad

| # | Hallazgo | Dónde | Corrección |
|---|---|---|---|
| AC1 | **Selectores sin etiqueta asociada**: TalkBack lee «Crédito, lista desplegable» sin decir que es la forma de pago. Igual el período, el estado del pago, la categoría del gasto y los campos de los diálogos de venta y compra. | `activity_venta.xml:150-163`, `activity_compra.xml:135-148`, `activity_pago.xml:62-75`, `activity_gasto.xml:82-95`, `activity_informes.xml:30-43`, `VentaActivity.java:256-276` | `android:labelFor` en el rótulo; en los diálogos, `l1.setLabelFor(campo.getId())`. |
| AC2 | **Objetivos táctiles por debajo de 48 dp**: botones de diálogo a 39 dp (`setMinHeight(52)` en píxeles crudos) y «Ver comprobante» a 44 dp. | `BaseActivity.java:136,141`, `ClienteDetalleActivity.java:339-340` | Convertir a dp: `Ui.dp(this, 52)` y `dp(48)`. |
| AC3 | **Borde de campos 1,61:1** (WCAG 1.4.11 pide 3:1) y estado presionado 1,33:1. | `res/values/colors.xml` (`borde`, `presionado`) | Oscurecer el borde a un tono de ~3:1. |

## Lo que falta probar

- TalkBack real en el dispositivo (recorrido completo de una venta y un fiado).
- Rotación y letra del sistema al máximo en las pantallas con más campos.
- Restauración real de una copia y borrado posterior (requiere datos de ejemplo).
