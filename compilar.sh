#!/usr/bin/env bash
# Compila Mi Negocio (APK) sin Gradle, con el SDK de Android y sus build-tools.
# Uso:  ./compilar.sh
set -euo pipefail

SDK="${ANDROID_SDK:-$HOME/android-sdk}"
BT="$SDK/build-tools/34.0.0"
JAR="$SDK/platforms/android-28/android.jar"
PROY="$(cd "$(dirname "$0")" && pwd)"
SALIDA="$PROY/build"
CLAVE="${MINEGOCIO_KEYSTORE:-$HOME/.android/debug.keystore}"
CLAVE_PASS="${MINEGOCIO_KEY_PASS:-android}"
ALIAS="${MINEGOCIO_KEY_ALIAS:-androiddebugkey}"
VERSION_CODE="${VERSION_CODE:-2}"
VERSION_NAME="${VERSION_NAME:-1.1}"

for f in "$BT/d8" "$BT/aapt2" "$BT/zipalign" "$BT/apksigner" "$JAR"; do
    [ -e "$f" ] || { echo "FALTA: $f (instala platforms;android-28 y build-tools;34.0.0)"; exit 1; }
done

rm -rf "$SALIDA"
mkdir -p "$SALIDA/clases" "$SALIDA/dex" "$SALIDA/gen"

echo "[1/7] aapt2 compile (recursos)"
"$BT/aapt2" compile --dir "$PROY/res" -o "$SALIDA/res.zip"

echo "[2/7] aapt2 link (apk base + R.java)"
"$BT/aapt2" link -o "$SALIDA/base.apk" -I "$JAR" \
    --manifest "$PROY/AndroidManifest.xml" \
    --java "$SALIDA/gen" \
    --min-sdk-version 24 --target-sdk-version 28 \
    --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" \
    "$SALIDA/res.zip"

echo "[3/7] javac"
javac -encoding UTF-8 -source 8 -target 8 -parameters -nowarn \
    -classpath "$JAR" \
    -d "$SALIDA/clases" \
    $(find "$PROY/src" "$SALIDA/gen" -name '*.java')

echo "[4/7] d8 (dex)"
"$BT/d8" --lib "$JAR" --min-api 24 --output "$SALIDA/dex" \
    $(find "$SALIDA/clases" -name '*.class')

echo "[5/7] empaquetando classes.dex"
( cd "$SALIDA/dex" && zip -q -X "$SALIDA/base.apk" classes.dex )
"$BT/zipalign" -f 4 "$SALIDA/base.apk" "$SALIDA/alineado.apk"

echo "[6/7] firmando"
if [ ! -f "$CLAVE" ]; then
    echo "FALTA la llave de firma: $CLAVE"
    echo "Genera una llave de desarrollo con:"
    echo "  keytool -genkeypair -keystore ~/.android/debug.keystore -storepass android -keypass android -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=Android Debug,O=Android,C=US'"
    exit 1
fi
"$BT/apksigner" sign --ks "$CLAVE" --ks-key-alias "$ALIAS" --ks-pass "pass:$CLAVE_PASS" \
    --key-pass "pass:$CLAVE_PASS" --out "$PROY/minegocio.apk" "$SALIDA/alineado.apk"

echo "[7/7] verificando"
"$BT/apksigner" verify --print-certs "$PROY/minegocio.apk" | head -2
"$BT/aapt2" dump badging "$PROY/minegocio.apk" | grep -E "^package|launchable-activity" || true
echo "APK listo: $PROY/minegocio.apk"
