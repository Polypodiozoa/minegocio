# Mi Negocio

App Android para llevar el día a día de un negocio pequeño en Colombia: inventario con
fotos, ventas (de contado y fiadas), compras, clientes, proveedores, gastos, cartera de
fiados, informes y reportes en Excel. **Funciona sin internet** y no pide permisos de
cámara, ubicación, contactos ni almacenamiento: todo queda en la tablet.

- **Versión:** 1.3 (versionCode 4)
- **Paquete:** `com.polypodiozoa.minegocio`
- **APK:** [`minegocio.apk`](minegocio.apk) — 255 KB
- **SHA-256:** `4424f166000eca1d6b2122d8b68c0c12bdbc02b3356c4c7f9b3777b2ad482424`
- **Android:** 7.0 (API 24) o superior; probada en una Amazon Fire HD 8 (Fire OS 9 / API 28).

## Documentos

| Archivo | Qué contiene |
|---|---|
| [`DISENO.md`](DISENO.md) | Registro de diseño: usuarios, principios, pantallas, sistema visual, flujos, modelo de datos y decisiones. |
| [`AUDITORIA.md`](AUDITORIA.md) | Revisión del 18/10/2026: cálculos, accesibilidad, robustez y seguridad de datos, con lo que falta corregir. |
| [`LEEME.md`](LEEME.md) | Manual completo: instalación, cada módulo, compilación y restauración. |

## Instalar

1. Copiar `minegocio.apk` a la tablet (cable, Bluetooth, Drive o Telegram).
2. Abrir el archivo desde **Archivos** y aceptar la instalación de origen desconocido.
3. La primera vez la app abre vacía; en **Ajustes → Cargar datos de ejemplo** se puede
   recorrer con datos de prueba, y luego borrarlos con **Eliminar todos los datos**.

> El APK está firmado con la llave de desarrollo. Si algún día se firma con otra llave,
> Android no deja actualizar encima: hay que desinstalar (y eso borra los datos), así que
> **hacer una copia antes**.

## Compilar

Requiere el SDK de Android (build-tools 34.0.0 y plataforma android-28) y JDK. No usa Gradle:

```bash
VERSION_CODE=4 VERSION_NAME=1.3 ./compilar.sh
```

El resultado queda en `minegocio.apk`.

## Datos

- Base SQLite en el almacenamiento privado de la app, fotos y adjuntos en `files/adjuntos/`.
- **Copia de seguridad:** Ajustes → *Crear copia de seguridad* genera un `.zip` con la base,
  las fotos y un `LEEME.txt`, y se guarda donde el usuario elija.
- **Restaurar:** Ajustes → *Restaurar una copia*.
- Desinstalar la app borra todo. La copia es la única forma de recuperarlo.
