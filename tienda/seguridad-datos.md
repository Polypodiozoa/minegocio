# Seguridad de los datos — respuestas para Play Console

En Play Console, sección **Contenido de la aplicación → Seguridad de los datos**, las
respuestas son:

| Pregunta | Respuesta |
|---|---|
| ¿Tu app recopila o comparte datos del usuario? | **No** |
| ¿Los datos se cifran en tránsito? | No aplica (no se transmite nada) |
| ¿El usuario puede pedir que se eliminen sus datos? | Sí — **Ajustes → Borrar todos los datos** (y desinstalar) |
| ¿Se sigue la política de familias? | No (app para adultos) |
| ¿Tiene anuncios? | No |
| ¿Tiene compras dentro de la app? | No |
| ¿Usa identificadores de publicidad? | No |

## Justificación corta (por si la piden)

La app no declara el permiso `android.permission.INTERNET` en el manifiesto; toda la
información se guarda en una base SQLite privada del dispositivo y solo se exporta cuando el
usuario comparte un informe o guarda una copia de seguridad en el destino que él elige. Las
personas que comparten el dispositivo y la autoría de cada operación también son datos
locales; no se envían a ningún servidor.

## Permisos declarados en el manifiesto (21/10/2026)

- `android.permission.CAMERA`: solo para el **lector de códigos de barras o QR**. Se pide en
  tiempo de ejecución al abrir el lector y se puede negar; en ese caso el código se escribe a
  mano o se lee desde una imagen elegida por el usuario.
- **No** declara internet, ubicación, contactos, teléfono ni almacenamiento general.
- Para fotos, archivos y comprobantes se usan componentes del sistema
  (`ACTION_IMAGE_CAPTURE`, `ACTION_OPEN_DOCUMENT`, `ACTION_CREATE_DOCUMENT`) y un
  `FileProvider` no exportado.
- La lectura de códigos es local (ZXing 3.5.3); no se guardan fotogramas ni se envían a
  servidores.
- Las cuentas de las personas y el orden de su menú son locales. La app no tiene login
  remoto ni autenticación por PIN; el selector de persona es para un dispositivo compartido
  de confianza.

## Clasificación de contenido

- Categoría: **Negocios**
- Público objetivo: **18 años o más**
- Sin contenido generado por usuarios, sin compras, sin anuncios.
