#!/usr/bin/env bash
# Pruebas de Mi Negocio v1.31.
#
#   ./pruebas/pruebas.sh                    sólo lógica pura (dinero, fechas, Excel)
#   ./pruebas/pruebas.sh copia.zip          además: invariantes financieras sobre esa copia
#   ./pruebas/pruebas.sh base.db            además: invariantes sobre una base suelta
#
# No necesita Gradle, ni emulador, ni la tablet.
set -euo pipefail

PROY="$(cd "$(dirname "$0")/.." && pwd)"
JDK="${JAVA_HOME:-$HOME/.local/jdk17}"
SQLITE="${SQLITE:-$HOME/.local/platform-tools/sqlite3}"
SALIDA="$PROY/build-pruebas"
mkdir -p "$SALIDA"

"$JDK/bin/javac" -encoding UTF-8 -nowarn -d "$SALIDA" \
    "$PROY/src/com/polypodiozoa/minegocio/util/Formatos.java" \
    "$PROY/src/com/polypodiozoa/minegocio/util/Fechas.java" \
    "$PROY/src/com/polypodiozoa/minegocio/util/Excel.java" \
    "$PROY/pruebas/Pruebas.java"
"$JDK/bin/java" -cp "$SALIDA" Pruebas

if [ $# -eq 0 ]; then
    echo
    echo "Sin base de datos: no se corrieron las invariantes financieras."
    echo "Pasa una copia de seguridad para correrlas:  ./pruebas/pruebas.sh copia.zip"
    exit 0
fi

ENTRADA="$1"
if [ "${ENTRADA##*.}" = "zip" ]; then
    rm -rf "$SALIDA/base"
    mkdir -p "$SALIDA/base"
    unzip -q "$ENTRADA" -d "$SALIDA/base"
    ENTRADA="$(find "$SALIDA/base" -name '*.db' | head -1)"
fi
if [ ! -f "$ENTRADA" ]; then
    echo "No encontré la base en: $1"
    exit 1
fi

echo
echo "Invariantes financieras sobre $(basename "$ENTRADA")"
# Copia a un archivo aparte: si la base viene en modo WAL, sqlite3 crearía -wal junto a ella.
cp "$ENTRADA" "$SALIDA/base-prueba.db"
"$SQLITE" -separator '|' "$SALIDA/base-prueba.db" < "$PROY/pruebas/invariantes.sql" > "$SALIDA/invariantes.txt"

FALLOS=0
while IFS='|' read -r nombre valor; do
    [ -z "$nombre" ] && continue
    if [ "$valor" = "0" ]; then
        echo "  ok    $nombre"
    else
        echo "  FALLA $nombre ($valor registros en contra)"
        FALLOS=$((FALLOS + 1))
    fi
done < "$SALIDA/invariantes.txt"

if [ "$FALLOS" -gt 0 ]; then
    echo
    echo "FALLARON $FALLOS invariantes financieras."
    exit 1
fi
echo
echo "Invariantes financieras: todas se cumplen."
