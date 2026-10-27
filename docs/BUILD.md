# Compilar y firmar Mi Negocio

Todo vive en el espacio de usuario: no hace falta `rpm-ostree install`, ni Gradle, ni Android
Studio. La app se compila con `aapt2` + `javac` + `d8` y se firma con `apksigner`.

## Herramientas (rutas de esta máquina)

| Pieza | Ruta / versión |
|---|---|
| JDK | `~/.local/jdk17` (Temurin 17.0.21) — `javac`, `keytool`, `jarsigner` |
| SDK de Android | `~/android-sdk` con `platforms;android-36` y `build-tools;36.0.0` |
| adb | `~/.local/platform-tools/adb` |
| ZXing (lector de códigos) | `herramientas/zxing-core-3.5.3.jar` (dentro del repo) |
| bundletool (AAB) | `herramientas/bundletool.jar` (dentro del repo) |

```bash
export JAVA_HOME="$HOME/.local/jdk17"
export PATH="$JAVA_HOME/bin:$HOME/.local/platform-tools:$PATH"
```

## Compilar

```bash
./compilar.sh                                   # APK (1.32 / 33 por defecto)
VERSION_CODE=34 VERSION_NAME=1.33 ./compilar.sh  # APK de una versión nueva
VERSION_CODE=34 VERSION_NAME=1.33 ./empaquetar-aab.sh  # AAB firmado para Play
```

**Google Play exige `targetSdkVersion` 36** para apps nuevas desde el 31/08/2026, así que los
scripts apuntan a `platforms;android-36` y `--target-sdk-version 36`. Con ese target cambian
dos comportamientos que ya están implementados en `BaseActivity`:

- **Edge-to-edge:** Android 15/16 dibuja la app por debajo de las barras y el opt-out
  `windowOptOutEdgeToEdgeEnforcement` ya no sirve. `BaseActivity.aplicarInsets()` estira la
  barra superior bajo la barra de estado, rellena abajo con el fondo de la pantalla y elige
  color de iconos según el fondo (sin esto, el contenido queda tapado).
- **Atrás predictivo:** con target 36 en Android 16 el sistema ya no llama a
  `onBackPressed()`. `BaseActivity` registra un `OnBackInvokedCallback` (API 33+) que
  reenvía el gesto a `onBackPressed()`, de modo que siguen funcionando los diálogos de
  «cambios sin guardar». El manifiesto declara `enableOnBackInvokedCallback="true"`.

Ambos scripts leen la llave de `~/.minegocio/signing.env` (ver abajo), así que **no** hay
contraseñas en el repositorio. Si falta la llave, el script se detiene en vez de firmar con
otra cosa.

## La llave de firma

- **Keystore:** `~/.minegocio/minegocio-release.jks` · **alias:** `minegocio` · RSA 4096 ·
  vigencia 30 años.
- **Contraseña:** en `~/.minegocio/signing.env` (permisos 600, fuera del repo) y en la nota
  del vault `Mi Negocio — llave de firma`.
- **Huella SHA-256 del certificado:**
  `BB:3B:B5:66:F1:39:BC:F2:6E:12:D4:10:99:28:74:91:8B:63:0D:96:E7:54:B1:42:80:55:F6:F1:36:88:CB:DC`
- **Respaldo cifrado:** `backup/minegocio-auditoria-2026-10/firma-minegocio-v1.31.tar.gz.enc`
  (AES-256, PBKDF2). Se descifra con la misma contraseña:

```bash
openssl enc -d -aes-256-cbc -pbkdf2 -iter 200000 \
  -in firma-minegocio-v1.31.tar.gz.enc -out firma.tar.gz -pass pass:'LA_CONTRASEÑA'
tar xzf firma.tar.gz     # deja .minegocio/ con el keystore y signing.env
```

**Si se pierde esta llave** no se pueden publicar actualizaciones: Android exige la misma
firma para actualizar sin desinstalar. Habría que repetir el trasplante (copia → desinstalar
→ instalar → restaurar) con una llave nueva. En Play, la llave de subida se puede resetear
desde Play Console (`Play App Signing`); la llave de la app la guarda Google, no este equipo.

### Historia de la llave

La app 1.30 se firmó con una llave de desarrollo (`~/.android/debug.keystore`) que se perdió
al reinstalar el sistema operativo de esta máquina. La 1.31 se firma con la llave nueva de
arriba: por eso la primera instalación de la 1.31 exige desinstalar la 1.30 (ver abajo).

## Pruebas

```bash
./pruebas/pruebas.sh                        # lógica pura: dinero, fechas, Excel
./pruebas/pruebas.sh copia.zip              # + invariantes financieras sobre datos reales
```

`pruebas/invariantes.sql` comprueba sobre una base de verdad: total = subtotal − descuento +
impuesto, subtotal y costo = suma de sus ítems, impuesto = redondeo de la base × tarifa,
abonos nunca mayores que la venta, dinero recibido = repartido + saldo a favor, ventas
anuladas sin abonos y arqueo de cajas cerradas. Cada línea debe dar **0**.

## Instalar en la tablet

```bash
adb install -r minegocio.apk    # actualiza conservando datos (misma firma, versionCode mayor)
```

Si cambia la firma (1.30 → 1.31), Android rechaza la instalación con
`INSTALL_FAILED_UPDATE_INCOMPATIBLE`. El trasplante, en este orden:

1. `Ajustes → Crear copia de seguridad` y guardar el ZIP fuera de la tablet (`adb pull`).
2. `adb uninstall com.polypodiozoa.minegocio` (borra los datos locales).
3. `adb install minegocio.apk`.
4. Abrir la app → `Ajustes → Restaurar una copia` → elegir el ZIP del paso 1.

`Respaldo.java` no se toca en ninguna versión: restaura bases de 1.30 sin cambios de esquema.
