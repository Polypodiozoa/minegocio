package com.polypodiozoa.minegocio.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import com.polypodiozoa.minegocio.datos.ProveedorArchivos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Manejo de fotos y documentos: siempre dentro del almacenamiento privado de la app. */
public final class Archivos {

    private static final long LIMITE_BYTES = 60L * 1024L * 1024L;

    private Archivos() {}

    public static File dirPermanente(Context ctx) {
        File d = new File(ctx.getFilesDir(), "adjuntos");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static File dirTemporal(Context ctx) {
        File d = new File(ctx.getCacheDir(), "adjuntos");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static void limpiarTemporal(Context ctx) {
        File[] archivos = dirTemporal(ctx).listFiles();
        if (archivos == null) return;
        for (File f : archivos) f.delete();
    }

    public static String mimeDe(Uri uri, Context ctx) {
        String mime = null;
        try {
            mime = ctx.getContentResolver().getType(uri);
        } catch (Exception ignored) {
        }
        if (mime == null) {
            String nombre = nombreDe(uri, ctx);
            int punto = nombre.lastIndexOf('.');
            if (punto >= 0) {
                mime = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(nombre.substring(punto + 1).toLowerCase());
            }
        }
        return mime == null ? "application/octet-stream" : mime;
    }

    public static String nombreDe(Uri uri, Context ctx) {
        String nombre = null;
        try (Cursor c = ctx.getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) nombre = c.getString(idx);
            }
        } catch (Exception ignored) {
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = uri.getLastPathSegment();
        }
        if (nombre == null || nombre.trim().isEmpty()) nombre = "documento";
        nombre = nombre.replace("/", "-").replace("\\", "-").trim();
        return nombre;
    }

    public static File nuevoArchivoTemporal(Context ctx, String prefijo, String extension) {
        String ext = extension == null || extension.isEmpty() ? "jpg" : extension;
        return new File(dirTemporal(ctx), prefijo + "-" + System.currentTimeMillis() + "." + ext);
    }

    /** Copia el contenido elegido por el usuario a la carpeta temporal de la app. */
    public static File copiarATemporal(Context ctx, Uri origen) throws IOException {
        String nombre = nombreDe(origen, ctx);
        String ext = extensionDe(nombre, mimeDe(origen, ctx));
        File destino = nuevoArchivoTemporal(ctx, "adj", ext);
        long total = 0;
        try (InputStream in = ctx.getContentResolver().openInputStream(origen);
             OutputStream out = new FileOutputStream(destino)) {
            if (in == null) throw new IOException("No se pudo leer el archivo");
            byte[] buffer = new byte[16384];
            int leidos;
            while ((leidos = in.read(buffer)) > 0) {
                total += leidos;
                if (total > LIMITE_BYTES) {
                    throw new IOException("El archivo supera los 60 MB");
                }
                out.write(buffer, 0, leidos);
            }
        } catch (IOException e) {
            destino.delete();
            throw e;
        }
        return destino;
    }

    public static String moverAPermanente(Context ctx, String rutaTemporal) {
        if (rutaTemporal == null || rutaTemporal.isEmpty()) return "";
        File origen = new File(rutaTemporal);
        if (!origen.exists()) return origen.getAbsolutePath();
        File destino = new File(dirPermanente(ctx), origen.getName());
        if (destino.exists()) {
            String base = origen.getName();
            int punto = base.lastIndexOf('.');
            String ext = punto >= 0 ? base.substring(punto) : "";
            String sinExt = punto >= 0 ? base.substring(0, punto) : base;
            int n = 2;
            while (destino.exists()) {
                destino = new File(dirPermanente(ctx), sinExt + "-" + n + ext);
                n++;
            }
        }
        if (origen.renameTo(destino)) return destino.getAbsolutePath();
        try {
            copiarArchivo(origen, destino);
            origen.delete();
            return destino.getAbsolutePath();
        } catch (IOException e) {
            return origen.getAbsolutePath();
        }
    }

    private static void copiarArchivo(File origen, File destino) throws IOException {
        try (InputStream in = new FileInputStream(origen);
             OutputStream out = new FileOutputStream(destino)) {
            byte[] buffer = new byte[16384];
            int leidos;
            while ((leidos = in.read(buffer)) > 0) out.write(buffer, 0, leidos);
        }
    }

    public static Uri uriPermanente(Context ctx, String ruta) {
        return new Uri.Builder().scheme("content")
                .authority(ProveedorArchivos.AUTORIDAD)
                .appendPath("adjuntos")
                .appendPath(new File(ruta).getName())
                .build();
    }

    public static Uri uriTemporal(Context ctx, File archivo) {
        return new Uri.Builder().scheme("content")
                .authority(ProveedorArchivos.AUTORIDAD)
                .appendPath("temporal")
                .appendPath(archivo.getName())
                .build();
    }

    public static String extensionDe(String nombre, String mime) {
        if (nombre != null) {
            int punto = nombre.lastIndexOf('.');
            if (punto >= 0 && punto < nombre.length() - 1) {
                String ext = nombre.substring(punto + 1).toLowerCase();
                if (ext.matches("[a-z0-9]{2,5}")) return ext;
            }
        }
        if (mime != null) {
            String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
            if (ext != null) return ext;
            if (mime.contains("jpeg")) return "jpg";
            if (mime.contains("png")) return "png";
            if (mime.contains("pdf")) return "pdf";
            if (mime.contains("heic")) return "heic";
        }
        return "bin";
    }

    public static boolean esImagen(String ruta, String mime) {
        if (mime != null && mime.startsWith("image/")) return true;
        String ext = extensionDe(ruta, null);
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png")
                || ext.equals("webp") || ext.equals("heic") || ext.equals("heif")
                || ext.equals("gif") || ext.equals("bmp");
    }

    public static String nombreVisible(String ruta) {
        if (ruta == null) return "";
        String nombre = new File(ruta).getName();
        int guion = nombre.indexOf('-');
        if (guion > 0 && (nombre.startsWith("adj-") || nombre.startsWith("foto-"))) {
            nombre = nombre.substring(guion + 1);
        }
        return nombre;
    }

    public static void borrar(String ruta) {
        if (ruta == null || ruta.isEmpty()) return;
        File f = new File(ruta);
        if (f.exists()) f.delete();
    }

    public static String tamanoLegible(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return String.format(Formatos.COLOMBIA, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    public static void copiar(File origen, OutputStream salida) throws IOException {
        try (InputStream in = new FileInputStream(origen)) {
            byte[] buffer = new byte[16384];
            int leidos;
            while ((leidos = in.read(buffer)) > 0) salida.write(buffer, 0, leidos);
        }
    }
}
