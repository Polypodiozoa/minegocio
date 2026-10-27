# Publicar Mi Negocio en Google Play

**Revisado:** 26/10/2026 · **Base:** `minegocio.aab` versionCode 33 / 1.32, `targetSdk` 36.
**Estado:** los materiales están listos y el **candado técnico del target 36 ya está resuelto**
(ver §2.1); queda lo que solo depende de la cuenta de Play (§3).

---

## 1. Lo que ya está resuelto (verificado en esta máquina)

| Pieza | Estado | Evidencia |
|---|---|---|
| AAB firmado | ✅ | `jarsigner -verify` pasa; `META-INF/*.RSA` presente |
| `bundletool validate` | ✅ | `build-aab/validacion.txt` |
| versionCode / versionName | ✅ 33 / 1.32 | `bundletool dump manifest` |
| minSdk 24 (Android 7.0) | ✅ | coincide con la ficha |
| **targetSdk 36 (Android 16)** | ✅ | `bundletool dump manifest`; obligatorio desde el 31/08/2026 |
| Permisos | ✅ solo `CAMERA`, sin `INTERNET` | AndroidManifest; sostiene la declaración «no recoge datos» |
| `exported` en todos los componentes | ✅ | obligatorio desde Android 12 |
| Icono 512×512 | ✅ | `herramientas/icono-512.png` (512×512 RGBA) |
| Gráfico destacado 1024×500 | ✅ | `tienda/grafico-destacado-1024x500.png` |
| Capturas | ✅ 29 archivos 800×1280 (~0,625; dentro del rango 16:9–9:16 de Play) | `capturas/` |
| Textos de ficha | ✅ | `tienda/ficha.md` |
| Política de privacidad + seguridad de datos | ✅ redactadas | `tienda/privacidad.md`, `tienda/seguridad-datos.md` |
| Llave de firma propia, RSA 4096, 30 años + respaldo cifrado | ✅ | `~/.minegocio/`, `docs/BUILD.md` |
| Firma del APK instalado == llave del repo | ✅ `bb3bb566…88cbdc` | `apksigner --print-certs` |

---

## 2. Los dos candados que hay que abrir primero

### 2.1 · Target API 36 — **resuelto el 26/10/2026**

Según Google (developer.android.com/google/play/requirements/target-sdk), desde el **31/08/2026**
«new apps and app updates must target Android 16 (API level 36) or higher». Una app nueva con
`targetSdkVersion 35` se rechaza en la subida. La prórroga llega al 01/11/2026 y no sirve aquí.

**Lo que se hizo** (todo verificado; ver `docs/BUILD.md` para repetirlo):

1. `sdkmanager "platforms;android-36" "build-tools;36.0.0"` en `~/android-sdk`.
2. `compilar.sh` y `empaquetar-aab.sh` apuntan a build-tools 36.0.0, `android-36` y
   `--target-sdk-version 36`.
3. **Borde a borde:** se quitó `android:windowOptOutEdgeToEdgeEnforcement` de `styles.xml`
   (Google lo deshabilita con target 36). `BaseActivity.aplicarInsets()` estira la barra
   superior bajo la barra de estado, rellena abajo con el fondo de cada pantalla y fija iconos
   claros u oscuros según la luminosidad del fondo.
4. **Atrás predictivo:** `onBackPressed()` ya no lo llama el sistema con target 36 en Android 16.
   `BaseActivity` registra un `OnBackInvokedCallback` (API 33+) que reenvía a
   `onBackPressed()`, y el manifiesto declara `enableOnBackInvokedCallback="true"`. Los 9
   diálogos de «cambios sin guardar» siguen saliendo.
5. **Probado en emulador Android 16 (API 36)** y regresión en el teléfono real (API 28):
   - pantalla principal, inventario y formulario: contenido sin tapar por las barras;
   - teclado abierto: el campo activo se ve entero (adjustResize sigue funcionando);
   - atrás con cambios escritos → diálogo «Descartar cambios» con Cancelar/Descartar;
   - sin excepciones en logcat en ninguno de los dos dispositivos.

### 2.2 · Cuenta nueva personal: 12 testers × 14 días

Si la cuenta de Play es **personal** y se creó después del 13/11/2023, Producción queda
deshabilitada hasta cumplir: prueba cerrada con **mínimo 12 testadores** con *opt-in continuo*
durante **14 días**, y luego solicitud de acceso a producción (con preguntas de diseño y pruebas).
Es un reloj de pared: no se puede acelerar.

Si es cuenta de **organización** (requiere número D-U-N-S) ese requisito no aplica.

Consecuencia práctica: el alta + la prueba cerrada conviene abrirlas **en paralelo** al trabajo
del API 36, no después. La pista interna (hasta 100 correos, sin revisión de ficha) sí está
disponible de inmediato y sirve para validar la subida del AAB.

---

## 3. Lo que solo tú puedes hacer (yo no puedo)

| # | Cosa | Nota |
|---|---|---|
| 1 | Cuenta de Google Play Console | 25 USD **una vez**, tarjeta |
| 2 | Verificación de identidad | documento + dirección; tarda de horas a días |
| 3 | Tipo de cuenta: personal u organización | decide el candado 2.2 — **decisión tuya** |
| 4 | URL pública de la política de privacidad | Play la exige; `tienda/privacidad.md` tiene que estar publicado (GitHub Pages, Google Sites, Notion público…) |
| 5 | Correo de soporte | uno que revises de verdad (`tienda/ficha.md` lo deja como «(tú)») |
| 6 | 12 correos de testadores | cuentas Google reales para la prueba cerrada |

---

## 4. Orden de ejecución

### Fase A — Cuenta (tú, hoy)
Alta en play.google.com/console → pagar → verificar identidad → elegir tipo de cuenta.

### Fase B — API 36 (trabajo técnico, ~1 sesión)
Pasos 1–6 de §2.1. Verificación antes de seguir: `bundletool dump manifest` debe decir
`targetSdkVersion="36"`, la app abre sin contenido bajo las barras en Android 16, y los diálogos
de «cambios sin guardar» siguen saliendo con el gesto atrás del sistema.

### Fase C — Cierre de versión

- [x] Commit + tag `v1.32` con el trabajo de API 36 y las pruebas.
- [x] SHA-256 del AAB en README y LEEME (regla escrita en el plan v1.31).

### Fase D — Alta de la app en Play Console
1. Create app → nombre `Mi Negocio: ventas y fiados`, idioma predeterminado **Español
   (Latinoamérica)**, tipo **App**, **gratis**.
2. **Play App Signing:** elegir **«Usar la clave de firma de la app existente»** y subir
   `~/.minegocio/minegocio-release.jks` (alias `minegocio`). Es la misma llave con la que está
   instalada la 1.31 en el teléfono: así los usuarios actuales **no** tienen que desinstalar.
   Si Play genera una llave nueva, todos los que ya la tienen instalada quedan huérfanos de
   actualizaciones por Play. La llave de subida puede ser esta misma.
3. Subir el AAB a **Pista interna** primero (valida firma, versión y políticas sin ficha).

### Fase E — Ficha de tienda
Copiar de `tienda/ficha.md`; subir `herramientas/icono-512.png`, el gráfico 1024×500 y elegir
2–8 capturas de `capturas/` (empezar por `01-inicio`, `02-inventario`, `03-nueva-venta`,
`04-detalle-venta`, `07-informes`, `15-deudas`).

### Fase F — Formularios obligatorios
| Formulario | Respuesta correcta en este caso |
|---|---|
| Seguridad de los datos | **No se recogen ni comparten datos** (la app no tiene permiso de INTERNET). Marcar «los datos permanecen en el dispositivo» y que se pueden borrar desde Ajustes. |
| Clasificación de contenido (IARC) | Categoría Negocios, sin violencia/sexo/juego/apuestas; público **18+** (herramienta de trabajo, no dirigida a niños) |
| Público objetivo | Adultos; **no** incluir en Families |
| Anuncios | **No contiene anuncios** |
| Acceso a la app | **Toda la funcionalidad está disponible sin credenciales** (no hay login ni servidor) |
| Funciones financieras | Marcar «otras funciones financieras» y explicar: **registro interno de fiados del propio negocio**; la app no presta dinero, no cobra intereses, no procesa pagos. El QR Bre-B solo se *muestra* en pantalla para que el cliente pague por su banco. |
| Noticias / COVID / gobierno | No |

> Cuidado con el marketing: hablar de «fiados» y «cartera» está bien; **no** prometer préstamos ni
> crédito, porque eso dispara la política de servicios financieros y puede pedir licencias.

### Fase G — Prueba cerrada → Producción
1. Pista cerrada con los 12 testadores (opt-in), 14 días continuos.
2. Dashboard → solicitar acceso a producción (preguntas: cómo probaste, quién probó, qué se
   corrigió). Responder con datos reales del negocio piloto.
3. Producción: revisión de Play (de horas a 7 días) → publicación.

---

## 5. Errores de rechazo típicos que este proyecto ya evita

- AAB sin firmar → ✅ ya firmado.
- `targetSdkVersion` viejo → ❌ bloqueo actual, ver §2.1.
- Componentes sin `android:exported` → ✅ todos declarados.
- Política de privacidad inaccesible o sin URL → ❌ falta publicarla.
- Permisos que no se corresponden con la declaración de datos → ✅ solo CAMERA, coherente.
- Ficha con capturas de otra app / con datos personales visibles → ✅ usar solo `capturas/`.

---

## 6. Cómo se revierte

- Cambios de API 36: `git revert` del commit; el AAB anterior (32/1.31, target 35) sigue en disco
  y se puede volver a instalar por `adb install -r` (misma llave, versionCode mayor → conserva datos).
- Cuenta de Play: no se revierte; por eso la pista interna va antes que producción.
- Datos del negocio: copia de seguridad desde la app antes de cualquier instalación (regla del
  plan v1.31, §0.4).
