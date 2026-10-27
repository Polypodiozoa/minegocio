# Experiencia con Play Console y Android — borrador de respuesta

**Uso:** pegar en el formulario de alta de Play Console («Tell us about your previous experience
with Play Console and Android»). El formulario está en inglés, así que la versión principal es en
inglés; abajo va la traducción.

**Antes de pegar:** rellena lo que está entre «» y borra los corchetes. Todo lo demás está
respaldado por el proyecto real (`com.polypodiozoa.minegocio`, 52 archivos Java, ~12.300 líneas,
AAB firmado versionCode 32).

---

## Versión en inglés (para el formulario)

This is my first app submitted to Google Play, so I have no prior Play Console publishing
history. I am registering the account myself because I want to distribute and maintain the app
under my own developer identity.

I built Mi Negocio from scratch as a native Android application in Java, without Android Studio
or Gradle: resources are compiled with aapt2, sources with javac, dexed with d8, and packaged and
validated as an Android App Bundle with bundletool. The app is offline-only by design — it holds
no internet permission at all — and stores everything in a local SQLite database with versioned
schema migrations. It covers inventory with photos, cash and credit sales with discounts and
optional tax, a customer credit ledger with due dates and partial payments, supplier purchases,
expenses, cash-session reconciliation, reports with XLSX/CSV export, and PDF receipts shared
through other apps. Camera use is limited to barcode and QR scanning via ZXing, with the declared
permission reflecting exactly that. I also implemented encrypted-key release signing outside
Studio, an internal ZIP backup/restore path, and multi-user profiles on a shared device.

Before touching Google Play I released signed builds with my own keystore, validated the bundle
with `bundletool validate`, and tested on a physical device over adb. The app is in daily
production use by «un negocio real / nombre del negocio», which is how I found and fixed the
usability and data-integrity bugs. I also ran a code and security audit of my own app and wrote
automated SQL invariant tests that check financial consistency (sale totals against their line
items, payments never exceeding a sale, cash reconciliation) — every one of them must return zero
before a release.

Relevant supporting material: GitHub profile https://github.com/Polypodiozoa (the repositories are
private; I can grant read access on request), package name `com.polypodiozoa.minegocio`, and
«si tienes ficha, web o correo público, ponlo aquí».

I am «formación / ocupación: p. ej. “a self-taught developer”, “estudiante de ingeniería de
sistemas”» and I have «N» years of «Java / Android» experience. «Si tienes otros proyectos
Android, menciónalos aquí en una línea cada uno.»

## Traducción (referencia, no pegar en el formulario)

Esta es mi primera app enviada a Google Play, así que no tengo historial previo de publicación en
Play Console. Abro la cuenta yo mismo porque quiero distribuir y mantener la app con mi propia
identidad de desarrollador.

Construí Mi Negocio desde cero como aplicación Android nativa en Java, sin Android Studio ni
Gradle: los recursos se compilan con aapt2, el código con javac, se convierte a dex con d8 y se
empaqueta y valida como App Bundle con bundletool. La app es offline por diseño —no tiene permiso
de internet— y guarda todo en una base SQLite local con migraciones de esquema versionadas. Cubre
inventario con fotos, ventas de contado y fiadas con descuento e impuesto opcional, cartera de
fiados con vencimientos y abonos, compras a proveedores, gastos, arqueo de caja, informes con
exportación a XLSX/CSV y comprobantes PDF compartidos con otras apps. La cámara se usa solo para
leer códigos de barras y QR con ZXing, y el permiso declarado refleja exactamente eso. También
implementé la firma de versiones de release con llave propia fuera de Studio, respaldo/restauración
en ZIP y varios perfiles de usuario en un mismo dispositivo.

Antes de tocar Google Play publiqué builds firmadas con mi propio keystore, validé el bundle con
`bundletool validate` y probé en un dispositivo físico por adb. La app está en uso diario en
«negocio real», y de ahí salieron los errores de usabilidad e integridad de datos que corregí.
También hice una auditoría de código y seguridad de mi propia app y escribí pruebas SQL de
invariantes que verifican consistencia financiera (totales contra sus ítems, abonos que nunca
superan la venta, arqueo de caja); todas deben dar cero antes de cada versión.

---

## Reglas para no meterte en problemas

1. **No infles la experiencia.** Google contrasta datos y una declaración falsa en el alta puede
   costar la cuenta (y con ella el nombre del paquete `com.polypodiozoa.minegocio`, que no se
   libera nunca). «Primera app» es una respuesta perfectamente válida y frecuente.
2. **No pegues la lista entera de funciones** si el campo tiene límite de caracteres; quédate con
   el párrafo de construcción + el de pruebas y recorta el resto.
3. **Enlaces:** tus tres repos son privados (`minegocio`, `mercadomundial`, `ProjectSifn`), así que
   un revisor no verá nada. Si quieres que el enlace aporte algo, deja público un README del
   proyecto o pon «puedo dar acceso bajo solicitud», que es lo que ya dice el borrador.
4. **Coherencia con la ficha:** aquí dices «offline, sin internet, solo cámara»; la declaración de
   seguridad de datos y el manifiesto dicen lo mismo. Mantenlo así.
