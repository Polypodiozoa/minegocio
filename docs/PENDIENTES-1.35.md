# Deuda registrada para la 1.35

Registro de lo que se decidió **no** tocar en la 1.34 para no mover layouts que ya funcionan a
pocas horas de subir a Play, con el detalle suficiente para ejecutarlo sin volver a investigar.
Todo lo de aquí ya está medido sobre el código, no es una lista de ideas.

La 1.34 sale con: etiquetas accesibles de los campos (41 avisos `Missing accessibility label` →
0), cero texto en inglés visible, y la versión del AAB arreglada (antes salía con el código
anterior y Play lo habría rechazado por duplicado).

## La regla con la que se decide qué arreglar

Un campo que el tendero toca cien veces al día pesa más que un aviso de lint con severidad alta
sobre un campo que se configura una vez en la vida del negocio. Las pantallas de uso diario son
**venta, pago, gasto, producto, compra y caja**; lo demás (ajustes, usuario, búsquedas) no entra
en esta deuda porque su fricción no la nota nadie.

---

## A. El teclado no avanza al campo siguiente — 37 campos

**Qué pasa:** sin `imeOptions`, pulsar Intro o la tecla de avance en el teclado no lleva al campo
siguiente. El usuario tiene que **levantar el dedo y tocar cada campo**. Registrar un producto son
10 campos: diez toques de más, todos los días.

**Medido:** 45 campos en 13 layouts; 42 sin `imeOptions` (sólo lo tienen `etManual` en el escáner,
y los dos `etBuscar` de inventario y lista de personas, que ya usan `actionSearch`). De esos 42,
**5 son `etNotas` multilínea** (`textCapSentences|textMultiLine` en compra, gasto, pago, persona y
venta): ahí Intro **debe** seguir haciendo salto de línea, así que se quedan como están.
El trabajo real son **37 campos de una línea en 10 layouts**.

| Layout | Campos que faltan |
|---|---|
| `activity_ajustes.xml` | `etNegocio`, `etNit`, `etTelefono`, `etDireccion`, `etImpuesto`, `etRetroDias`, `etAvisoVence`, `etBreB`, `etCuenta`, `etBanco`, `etTitular` |
| `activity_producto.xml` | `etNombre`, `etCategoria`, `etCodigo`, `etUnidad`, `etPrecio`, `etCosto`, `etStock`, `etStockMinimo`, `etImpuestoProducto`, `etDiasAviso` |
| `activity_persona.xml` | `etNombre`, `etContacto`, `etTelefono`, `etCorreo`, `etDireccion`, `etCupo` |
| `activity_gasto.xml` | `etDescripcion`, `etCategoria`, `etMonto` |
| `activity_venta.xml` | `etImpuesto`, `etDescuento` |
| `activity_compra.xml` | `etImpuesto` |
| `activity_pago.xml` | `etMonto` |
| `activity_caja.xml` | `etMontoInicial` |
| `fila_item_compra.xml` | `etCostoUnitario` |
| `activity_usuario.xml` | `etNombreUsuario` |

**Cómo:** `android:imeOptions="actionNext"` en todos menos el último de cada formulario, que lleva
`actionDone` (cierra el teclado y dispara el mismo guardado que el botón). Y en los pocos campos
donde el orden visual no es el orden de foco, añadir `android:nextFocusForward="@+id/..."`.

**Ya verificado:** todas las pantallas declaran `android:windowSoftInputMode="adjustResize"`, así
que el campo enfocado no queda tapado por el teclado. No hay que tocar nada ahí.

**Cómo se revierte:** un `git checkout -- res/layout` devuelve los 25 layouts; respaldo adicional
en `~/.hermes/cache/scratch/res-layout-antes-label.tar.gz` (previo a los cambios de la 1.34).

## B. Los números no se seleccionan al tocarlos — 13 campos

**Qué pasa:** al corregir un precio de «3500» el cursor entra al lado del número y hay que borrar
dígito por dígito. **Ningún** campo de la app tiene `selectAllOnFocus`.

**Cómo:** `android:selectAllOnFocus="true"` en los 13 campos numéricos de las pantallas de uso
diario (medido): `etPrecio`, `etCosto`, `etStock`, `etStockMinimo`, `etImpuestoProducto`,
`etDiasAviso` (producto), `etDescuento` (venta), `etMonto` (gasto y pago), `etMontoInicial`
(caja), `etImpuesto` (compra) y `etCostoUnitario` (fila de compra). Los numéricos de ajustes
—`etImpuesto`, `etRetroDias`, `etAvisoVence`— se dejan: se escriben una vez.

**Trampa:** no cambiar `inputType="number"` a `numberDecimal` en los montos. Las cantidades de
dinero son `long` en pesos enteros (`Formatos.parsePesos`, `ProductoActivity:192`), así que la
tecla decimal sobraría y permitiría escribir valores que el parser recorta en silencio.

## C. Autofill — 45 campos en 13 layouts

**Qué pasa:** sin `autofillHints`, el gestor de contraseñas puede ofrecer rellenar «Precio» o
«Cantidad» con el correo o la contraseña del usuario. Dato equivocado en un campo de dinero es
peor que no autocompletar nada.

**Cómo:** `android:importantForAutofill="noExcludeDescendants"` en la raíz de cada layout de
formulario (una línea por archivo, sin tocar los 45 campos). Reparto actual: `activity_ajustes` 11,
`activity_producto` 10, `activity_persona` 7, `activity_gasto` 4, `activity_venta` 3,
`activity_compra` 2, `activity_pago` 2, y 1 en `caja`, `escaner`, `inventario`, `lista_personas`,
`usuario`, `fila_item_compra`.

**Matiz:** en `inventario` y `lista_personas` el campo es de **búsqueda**; ahí el valor correcto es
`importantForAutofill="no"` a secas, no el `noExcludeDescendants` del formulario.

**Sin verificar:** cuántos de los 13 son formularios reales frente a pantallas de sólo lectura.
Revisar antes de aplicar la línea; donde no haya campos que escribir, sobra.

## D. HardcodedText — 371 avisos. **No es UX, va aparte y va último**

Todo el texto está en español y visible al usuario, pero escrito en los layouts en vez de en
`strings.xml`. No afecta a lo que la persona ve ni a Play: es deuda de mantenimiento. Migrarlo
toca cada pantalla, así que no se mezcla con arreglos de comportamiento — o se hace solo, o no se
hace. Riesgo de hacerlo deprisa: cambiar un identificador de texto o dejar un `setText` apuntando
a un recurso que ya no existe.

Orden recomendado para la 1.35: **A y B primero** (se notan a diario), luego C (una línea por
layout), y D en una versión aparte, con las pruebas de `pruebas/pruebas.sh` en verde antes y
después.

## E. Encontrado en la revisión de la 1.34, para decidir después

**`errores.log` — resuelto: se queda y ahora se puede enviar.** `App.java` guarda el rastro de cada
caída en un archivo privado de la app (con `.old` de respaldo). Antes solo se podía borrar; ahora
Ajustes tiene el botón **«Enviar el registro de errores»** (junto a la versión), que abre el
selector de Android con los últimos 64 KB del archivo como texto —lo suficiente para ver la caída y
sin pasarse del límite de un intent— y avisa «No hay errores registrados.» cuando el archivo no
existe. Nada sale del teléfono si no se pulsa el botón y se elige a quién enviarlo. No lleva datos
del negocio: el SQL va entero con `?` (6 `LIKE` con parámetro, 0 concatenado) y los parseos de lo
que escribe la gente están todos dentro de `try/catch` y devuelven 0.

**El APK y el AAB firmados se quedan versionados en git** (decidido el 2026-11-08). El coste es
tamaño: cada recompilación añade ~1,5 MB al historial. Se acepta porque el release se arma a mano
y sin CI el binario publicado tiene que quedar en el mismo sitio que el código que lo produjo. Si
algún día molesta, se sacan con `git rm --cached minegocio.aab minegocio.apk` + `.gitignore`, sin
reescribir la historia.

**`ventas` no tiene índice por `cliente_id`.** Lo medido en esta revisión: 19 tablas y 16 índices,
47 sumas resueltas en SQL y no en Java, ninguna consulta sin filtro. O sea que el recorrido completo
de una tabla a escala de negocio es cosa de milisegundos. El único hueco son 14 consultas que
filtran por `cliente_id` (deudas, abonos, historial de cliente) sin índice que las cubra. No se
añade ahora: exige subir la versión de esquema, y una migración no es lo que hay que estrenar justo
antes de publicar. Cuando la app pase de ~10.000 ventas, un `CREATE INDEX` en la migración
siguiente.

## F. Estado antes de subir la 1.34

- `tienda/ficha.md` ya tiene la entrada de la 1.34 (308 caracteres, dentro del límite de 500).
- `docs/BUILD.md` actualizado: la versión vive sólo en `AndroidManifest.xml` y los scripts
  abortan si el entorno no coincide.
- `versionCode` 35 / `versionName` 1.34 en el manifest y en el AAB firmado.
- La 1.33 y la 1.34 quedaron confirmadas en git (commits «1.34: etiquetas accesibles en los
  campos y versión desde el manifest» y «Ajustes: botón para enviar el registro de errores»), así
  que se revierten con `git revert` en vez de a mano.
- `minegocio.aab` y `minegocio.apk` van **dentro** del commit, como se decidió en el punto E.
  `bin/` (salida de compilación) se sacó del índice con `git rm -r --cached bin` y quedó en
  `.gitignore`; los binarios firmados no, para que el release a mano caiga en el mismo sitio que
  el código que lo produjo.

## G. Estado de la 1.35

Play Console exige que la política de privacidad esté enlazada **en la ficha y dentro de la app**;
la 1.34 no la tenía dentro, así que la 1.35 añade un botón **«Política de privacidad»** en Ajustes
(debajo de «Enviar el registro de errores»), que abre
`https://polypodiozoa.github.io/minegocio/privacidad.html` con el navegador del teléfono. Si el
teléfono no tiene navegador, en vez de caerse muestra la dirección en un diálogo para copiarla.
No pide permiso de `INTERNET`: el enlace lo abre el navegador, no la app.

`versionCode` 36 / `versionName` 1.35. Verificado sobre el APK instalado en el teléfono: el botón
aparece en Ajustes y abre la página publicada.

## Anexo: lo que sí se revisó y salió limpio

Para no repetirlo: sin credenciales ni llaves en `src`, `res` ni el manifest; sin `Log.`,
`printStackTrace` suelto ni `android:debuggable`; sin marcas `TODO`/`FIXME` (los `todo` que
aparecen son la palabra castellana); sin texto en inglés en recursos ni código; el AAB no lleva
`.java`, `.kt`, `docs/`, keystore ni scripts; va firmado, con su `MINEGOCI.RSA`.