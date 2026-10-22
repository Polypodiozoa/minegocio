#!/usr/bin/env bash
# Empaqueta Mi Negocio como AAB (Android App Bundle) para Google Play.
# Usa aapt2 en formato proto + bundletool, sin Gradle.
# Uso:  VERSION_CODE=18 VERSION_NAME=1.17 ./empaquetar-aab.sh
set -euo pipefail

SDK="${ANDROID_SDK:-$HOME/android-sdk}"
BT="$SDK/build-tools/35.0.0"
JAR="$SDK/platforms/android-35/android.jar"
PROY="$(cd "$(dirname "$0")" && pwd)"
ZXING="$PROY/herramientas/zxing-core-3.5.3.jar"
SALIDA="$PROY/build-aab"
BUNDLETOOL="$PROY/herramientas/bundletool.jar"
VERSION_CODE="${VERSION_CODE:-18}"
VERSION_NAME="${VERSION_NAME:-1.17}"
AAB="$PROY/minegocio.aab"

for f in "$BT/aapt2" "$BT/d8" "$JAR" "$ZXING" "$BUNDLETOOL"; do
    [ -e "$f" ] || { echo "FALTA: $f"; exit 1; }
done

rm -rf "$SALIDA"
mkdir -p "$SALIDA/clases" "$SALIDA/dex" "$SALIDA/gen" "$SALIDA/modulo"

echo "[1/6] aapt2 compile"
"$BT/aapt2" compile --dir "$PROY/res" -o "$SALIDA/res.zip"

echo "[2/6] aapt2 link (proto)"
"$BT/aapt2" link --proto-format -o "$SALIDA/base.apk" -I "$JAR" \
    --manifest "$PROY/AndroidManifest.xml" --java "$SALIDA/gen" \
    --min-sdk-version 24 --target-sdk-version 35 \
    --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" \
    "$SALIDA/res.zip"

echo "[3/6] javac"
javac -encoding UTF-8 -source 8 -target 8 -parameters -nowarn \
    -classpath "$JAR:$ZXING" -d "$SALIDA/clases" \
    $(find "$PROY/src" "$SALIDA/gen" -name '*.java')

echo "[4/6] d8"
"$BT/d8" --lib "$JAR" --min-api 24 --output "$SALIDA/dex" \
    "$ZXING" $(find "$SALIDA/clases" -name '*.class')

echo "[5/6] módulo base"
unzip -q "$SALIDA/base.apk" -d "$SALIDA/modulo"
mkdir -p "$SALIDA/modulo/manifest" "$SALIDA/modulo/dex"
mv "$SALIDA/modulo/AndroidManifest.xml" "$SALIDA/modulo/manifest/"
cp "$SALIDA/dex/classes.dex" "$SALIDA/modulo/dex/"
( cd "$SALIDA/modulo" && zip -q -r -X "$SALIDA/base.zip" . )

echo "[6/6] bundletool"
rm -f "$AAB"
java -jar "$BUNDLETOOL" build-bundle --modules="$SALIDA/base.zip" --output="$AAB"
java -jar "$BUNDLETOOL" validate --bundle="$AAB" > "$SALIDA/validacion.txt"
echo "AAB listo: $AAB"
ls -lh "$AAB"
