package com.polypodiozoa.minegocio.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import com.polypodiozoa.minegocio.util.Archivos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Proveedor propio de archivos: permite que la app de cámara escriba la foto y
 * que otras apps abran comprobantes sin pedir permisos de almacenamiento.
 */
public class ProveedorArchivos extends android.content.ContentProvider {

    public static final String AUTORIDAD = "com.polypodiozoa.minegocio.archivos";

    @Override
    public boolean onCreate() {
        return true;
    }

    private File resolver(Uri uri) throws FileNotFoundException {
        if (uri == null || uri.getPathSegments() == null || uri.getPathSegments().size() != 2) {
            throw new FileNotFoundException("Ruta no válida");
        }
        String tipo = uri.getPathSegments().get(0);
        String nombre = uri.getPathSegments().get(1);
        if (nombre.contains("/") || nombre.contains("..")) {
            throw new FileNotFoundException("Ruta no válida");
        }
        Context ctx = getContext();
        if (ctx == null) throw new FileNotFoundException("Sin contexto");
        File base = "temporal".equals(tipo) ? Archivos.dirTemporal(ctx) : Archivos.dirPermanente(ctx);
        try {
            File f = new File(base, nombre).getCanonicalFile();
            if (!f.getParentFile().getCanonicalPath().equals(base.getCanonicalPath())) {
                throw new FileNotFoundException("Ruta fuera del área permitida");
            }
            return f;
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File f = resolver(uri);
        int flags;
        if (mode != null && mode.contains("w")) {
            flags = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            flags = ParcelFileDescriptor.MODE_READ_ONLY;
        }
        return ParcelFileDescriptor.open(f, flags);
    }

    @Override
    public String getType(Uri uri) {
        try {
            File f = resolver(uri);
            String nombre = f.getName();
            int punto = nombre.lastIndexOf('.');
            if (punto >= 0) {
                String ext = nombre.substring(punto + 1).toLowerCase();
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                if (mime != null) return mime;
            }
        } catch (FileNotFoundException ignored) {
        }
        return "application/octet-stream";
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        try {
            File f = resolver(uri);
            String[] columnas = projection != null ? projection
                    : new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
            MatrixCursor cursor = new MatrixCursor(columnas, 1);
            Object[] fila = new Object[columnas.length];
            for (int i = 0; i < columnas.length; i++) {
                if (OpenableColumns.DISPLAY_NAME.equals(columnas[i])) fila[i] = f.getName();
                else if (OpenableColumns.SIZE.equals(columnas[i])) fila[i] = f.length();
                else fila[i] = null;
            }
            cursor.addRow(fila);
            return cursor;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
