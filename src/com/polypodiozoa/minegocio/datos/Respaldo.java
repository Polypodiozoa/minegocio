package com.polypodiozoa.minegocio.datos;

import android.content.Context;
import android.net.Uri;

import com.polypodiozoa.minegocio.util.Archivos;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Imagen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** Copia de seguridad completa (base de datos + fotos) en un solo archivo ZIP. */
public final class Respaldo {

    private static final long LIMITE_TOTAL = 1024L * 1024L * 1024L;
    private static final int LIMITE_ARCHIVOS = 5000;

    private Respaldo() {}

    public static void exportar(Context ctx, Uri destino) throws IOException {
        BD bd = BD.get(ctx);
        try (android.database.Cursor c = bd.getWritableDatabase()
                .rawQuery("PRAGMA wal_checkpoint(FULL)", null)) {
            if (c != null) c.moveToFirst();
        } catch (Exception ignored) {
        }
        File base = BD.archivo(ctx);
        if (!base.exists()) throw new IOException("No se encontró la base de datos.");
        File adjuntos = Archivos.dirPermanente(ctx);
        OutputStream salida = ctx.getContentResolver().openOutputStream(destino, "w");
        if (salida == null) throw new IOException("No se pudo abrir el destino.");
        try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(salida))) {
            zip.setLevel(6);
            zip.putNextEntry(new ZipEntry("minegocio.db"));
            copiar(new FileInputStream(base), zip);
            zip.closeEntry();
            File[] fotos = adjuntos.listFiles();
            if (fotos != null) {
                for (File foto : fotos) {
                    if (!foto.isFile()) continue;
                    zip.putNextEntry(new ZipEntry("adjuntos/" + foto.getName()));
                    copiar(new FileInputStream(foto), zip);
                    zip.closeEntry();
                }
            }
            String manifiesto = "Mi Negocio - copia de seguridad\n"
                    + "Fecha: " + Fechas.fechaHora(Fechas.ahora()) + "\n"
                    + "Negocio: " + bd.nombreNegocio() + "\n"
                    + "Contenido: base de datos y fotos/documentos adjuntos.\n"
                    + "Para restaurar: Ajustes > Copia de seguridad > Restaurar una copia.\n";
            zip.putNextEntry(new ZipEntry("LEEME.txt"));
            zip.write(manifiesto.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
    }

    public static void restaurar(Context ctx, Uri origen) throws IOException {
        File trabajo = new File(ctx.getCacheDir(), "restauracion");
        borrarDirectorio(trabajo);
        if (!trabajo.mkdirs()) throw new IOException("No se pudo preparar la restauración.");

        long total = 0;
        int archivos = 0;
        InputStream entradaCopia = ctx.getContentResolver().openInputStream(origen);
        if (entradaCopia == null) throw new IOException("No se pudo abrir la copia.");
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(entradaCopia))) {
            ZipEntry entrada;
            byte[] buffer = new byte[16384];
            while ((entrada = zip.getNextEntry()) != null) {
                if (entrada.isDirectory()) continue;
                archivos++;
                if (archivos > LIMITE_ARCHIVOS) throw new IOException("La copia tiene demasiados archivos.");
                String nombre = entrada.getName().replace('\\', '/');
                while (nombre.startsWith("./")) nombre = nombre.substring(2);
                if (nombre.contains("..") || nombre.startsWith("/")) {
                    throw new IOException("La copia contiene una ruta no válida.");
                }
                if (!nombre.equals("minegocio.db") && !nombre.startsWith("adjuntos/")
                        && !nombre.equals("LEEME.txt")) {
                    continue;
                }
                if (nombre.startsWith("adjuntos/") && nombre.indexOf('/', 9) >= 0) {
                    throw new IOException("La copia contiene carpetas no válidas.");
                }
                File destino = new File(trabajo, nombre);
                File padre = destino.getParentFile();
                if (padre != null && !padre.exists() && !padre.mkdirs()) {
                    throw new IOException("No se pudo crear la carpeta de restauración.");
                }
                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(destino))) {
                    int leidos;
                    while ((leidos = zip.read(buffer)) > 0) {
                        total += leidos;
                        if (total > LIMITE_TOTAL) throw new IOException("La copia es demasiado grande.");
                        out.write(buffer, 0, leidos);
                    }
                }
            }
        } catch (IOException e) {
            borrarDirectorio(trabajo);
            throw e;
        }

        File nuevaBase = new File(trabajo, "minegocio.db");
        if (!nuevaBase.exists() || !esBaseValida(nuevaBase)) {
            borrarDirectorio(trabajo);
            throw new IOException("La copia no contiene una base de datos válida de Mi Negocio.");
        }

        File baseActual = BD.archivo(ctx);
        File previa = new File(ctx.getFilesDir(), "copia-antes-de-restaurar.db");
        File adjuntosActuales = Archivos.dirPermanente(ctx);
        File adjuntosPrevios = new File(ctx.getFilesDir(), "adjuntos-antes-de-restaurar");
        borrarDirectorio(adjuntosPrevios);
        if (previa.exists()) previa.delete();

        BD.reiniciar();
        boolean movido = false;
        try {
            if (baseActual.exists()) {
                copiarArchivo(baseActual, previa);
            }
            borrarSiExiste(new File(baseActual.getPath() + "-wal"));
            borrarSiExiste(new File(baseActual.getPath() + "-shm"));
            borrarSiExiste(new File(baseActual.getPath() + "-journal"));
            File padre = baseActual.getParentFile();
            if (padre != null && !padre.exists()) padre.mkdirs();
            if (adjuntosActuales.exists() && !adjuntosActuales.renameTo(adjuntosPrevios)) {
                copiarDirectorio(adjuntosActuales, adjuntosPrevios);
            }
            if (adjuntosActuales.exists()) borrarDirectorio(adjuntosActuales);
            adjuntosActuales.mkdirs();
            File nuevosAdjuntos = new File(trabajo, "adjuntos");
            if (nuevosAdjuntos.exists()) {
                File[] lista = nuevosAdjuntos.listFiles();
                if (lista != null) {
                    for (File f : lista) {
                        if (f.isFile()) copiarArchivo(f, new File(adjuntosActuales, f.getName()));
                    }
                }
            }
            copiarArchivo(nuevaBase, baseActual);
            movido = true;
        } catch (IOException e) {
            try {
                if (previa.exists()) copiarArchivo(previa, baseActual);
                if (adjuntosPrevios.exists()) {
                    borrarDirectorio(adjuntosActuales);
                    adjuntosPrevios.renameTo(adjuntosActuales);
                }
            } catch (IOException ignored) {
            }
            BD.reiniciar();
            throw new IOException("No se pudo restaurar la copia: " + e.getMessage());
        } finally {
            BD.reiniciar();
            borrarDirectorio(trabajo);
        }
        if (movido) {
            Imagen.limpiar();
        }
    }

    private static boolean esBaseValida(File archivo) {
        byte[] cabecera = new byte[16];
        try (InputStream in = new FileInputStream(archivo)) {
            int leidos = in.read(cabecera);
            if (leidos < 16) return false;
            String texto = new String(cabecera, StandardCharsets.US_ASCII);
            return texto.startsWith("SQLite format 3");
        } catch (IOException e) {
            return false;
        }
    }

    private static void copiar(InputStream in, OutputStream out) throws IOException {
        try (InputStream entrada = in) {
            byte[] buffer = new byte[16384];
            int leidos;
            while ((leidos = entrada.read(buffer)) > 0) out.write(buffer, 0, leidos);
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

    private static void copiarDirectorio(File origen, File destino) throws IOException {
        if (!destino.exists() && !destino.mkdirs()) throw new IOException("No se pudo crear " + destino);
        File[] lista = origen.listFiles();
        if (lista == null) return;
        for (File f : lista) {
            if (f.isFile()) copiarArchivo(f, new File(destino, f.getName()));
        }
    }

    private static void borrarSiExiste(File f) {
        if (f.exists()) f.delete();
    }

    private static void borrarDirectorio(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] lista = dir.listFiles();
        if (lista != null) {
            for (File f : lista) {
                if (f.isDirectory()) borrarDirectorio(f);
                else f.delete();
            }
        }
        dir.delete();
    }
}
