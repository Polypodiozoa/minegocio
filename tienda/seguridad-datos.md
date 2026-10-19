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
usuario comparte un informe o guarda una copia de seguridad en el destino que él elige.

## Permisos declarados en el manifiesto (18/10/2026)

Ninguno sensible. Solo se usan componentes del sistema para cámara y selección de archivos
(`ACTION_IMAGE_CAPTURE`, `ACTION_OPEN_DOCUMENT`, `ACTION_CREATE_DOCUMENT`) y un
`FileProvider` no exportado para entregar temporalmente el archivo al visor o a la app de
correo.

## Clasificación de contenido

- Categoría: **Negocios**
- Público objetivo: **18 años o más**
- Sin contenido generado por usuarios, sin compras, sin anuncios.
