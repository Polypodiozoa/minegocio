package com.polypodiozoa.minegocio.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

import com.polypodiozoa.minegocio.R;

import java.io.File;

/** Miniaturas con caché para no llenar la memoria de el dispositivo. */
public final class Imagen {

    private static final LruCache<String, Bitmap> CACHE;

    static {
        int max = (int) (Runtime.getRuntime().maxMemory() / 8);
        CACHE = new LruCache<String, Bitmap>(max) {
            @Override
            protected int sizeOf(String clave, Bitmap valor) {
                return valor.getByteCount();
            }
        };
    }

    private Imagen() {}

    public static Bitmap cargar(String ruta, int maxPx) {
        if (ruta == null || ruta.isEmpty()) return null;
        String clave = ruta + "@" + maxPx;
        Bitmap enCache = CACHE.get(clave);
        if (enCache != null) return enCache;
        File f = new File(ruta);
        if (!f.exists()) return null;
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ruta, opciones);
        if (opciones.outWidth <= 0 || opciones.outHeight <= 0) return null;
        int muestra = 1;
        while (opciones.outWidth / (muestra * 2) >= maxPx
                || opciones.outHeight / (muestra * 2) >= maxPx) {
            muestra *= 2;
        }
        BitmapFactory.Options reales = new BitmapFactory.Options();
        reales.inSampleSize = muestra;
        reales.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap b = BitmapFactory.decodeFile(ruta, reales);
        if (b != null) CACHE.put(clave, b);
        return b;
    }

    public static void mostrar(ImageView vista, String ruta, int iconoDefecto, int maxPx) {
        Bitmap b = cargar(ruta, maxPx);
        if (b != null) {
            vista.setImageBitmap(b);
            vista.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            vista.setImageResource(iconoDefecto > 0 ? iconoDefecto : R.drawable.ic_imagen);
            vista.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }

    public static void limpiar() {
        CACHE.evictAll();
    }
}
