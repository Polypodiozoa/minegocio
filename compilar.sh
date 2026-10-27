#!/usr/bin/env bash
# Compila Mi Negocio (APK) sin Gradle, con el SDK de Android y sus build-tools.
# Uso:  ./compilar.sh
set -euo pipefail

SDK="${ANDROID_SDK:-$HOME/android-sdk}"
BT="$SDK/build-tools/36.0.0"
JAR="$SDK/platforms/android-36/android.jar"
PROY="$(cd "$(dirname "$0")" && pwd)"
ZXING="$PROY/herramientas/zxing-core-3.5.3.jar"
SALIDA="$PROY/build"
# La llave y su contraseña viven fuera del repo, en ~/.minegocio (permisos 600).
if [ -f "$HOME/.minegocio/signing.env" ]; then . "$HOME/.minegocio/signing.env"; fi
CLAVE="${MINEGOCIO_KEYSTORE:-$HOME/.minegocio/minegocio-release.jks}"
CLAVE_PASS="${MINEGOCIO_KEY_PASS:-}"
ALIAS="${MINEGOCIO_KEY_ALIAS:-minegocio}"
VERSION_CODE="${VERSION_CODE:-33}"
VERSION_NAME="${VERSION_NAME:-1.32}"

for f in "$BT/d8" "$BT/aapt2" "$BT/zipalign" "$BT/apksigner" "$JAR" "$ZXING"; do
    [ -e "$f" ] || { echo "FALTA: $f (SDK android-36, build-tools 36.0.0 y ZXing)"; exit 1; }
done

rm -rf "$SALIDA"
mkdir -p "$SALIDA/clases" "$SALIDA/dex" "$SALIDA/gen"

echo "[1/7] aapt2 compile (recursos)"
"$BT/aapt2" compile --dir "$PROY/res" -o "$SALIDA/res.zip"

echo "[2/7] aapt2 link (apk base + R.java)"
"$BT/aapt2" link -o "$SALIDA/base.apk" -I "$JAR" \
    --manifest "$PROY/AndroidManifest.xml" \
    --java "$SALIDA/gen" \
    --min-sdk-version 24 --target-sdk-version 36 \
    --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" \
    "$SALIDA/res.zip"

echo "[3/7] javac"
javac -encoding UTF-8 -source 8 -target 8 -parameters -nowarn \
    -classpath "$JAR:$ZXING" \
    -d "$SALIDA/clases" \
    $(find "$PROY/src" "$SALIDA/gen" -name '*.java')

echo "[4/7] d8 (dex)"
"$BT/d8" --lib "$JAR" --min-api 24 --output "$SALIDA/dex" \
    "$ZXING" $(find "$SALIDA/clases" -name '*.class')

echo "[5/7] empaquetando classes.dex"
( cd "$SALIDA/dex" && zip -q -X "$SALIDA/base.apk" classes.dex )
"$BT/zipalign" -f 4 "$SALIDA/base.apk" "$SALIDA/alineado.apk"

echo "[6/7] firmando"
if [ ! -f "$CLAVE" ]; then
    echo "FALTA la llave de firma: $CLAVE"
    echo "Restaura el respaldo cifrado del keystore o crea una llave nueva (docs/BUILD.md)."
    exit 1
fi
if [ -z "$CLAVE_PASS" ]; then
    echo "FALTA MINEGOCIO_KEY_PASS: revisa $HOME/.minegocio/signing.env"
    exit 1
fi
"$BT/apksigner" sign --ks "$CLAVE" --ks-key-alias "$ALIAS" --ks-pass "pass:$CLAVE_PASS" \
    --key-pass "pass:$CLAVE_PASS" --out "$PROY/minegocio.apk" "$SALIDA/alineado.apk"

echo "[7/7] verificando"
"$BT/apksigner" verify --print-certs "$PROY/minegocio.apk" | head -2
"$BT/aapt2" dump badging "$PROY/minegocio.apk" | grep -E "^package|launchable-activity" || true
echo "APK listo: $PROY/minegocio.apk"
