package com.polypodiozoa.minegocio;

import android.app.Application;
import android.util.Log;

import com.polypodiozoa.minegocio.datos.BD;
import com.polypodiozoa.minegocio.util.Archivos;
import com.polypodiozoa.minegocio.util.Formatos;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Locale.setDefault(Formatos.COLOMBIA);
        Archivos.limpiarTemporal(this);
        /* Si la base no abre, no se borra: se aparta y se avisa en el inicio,
           para que el negocio restaure su copia en vez de perder todo. */
        try {
            if (BD.archivo(this).exists() && !BD.get(this).sana()) {
                BD.apartarDanada(this);
                getSharedPreferences("ui", MODE_PRIVATE).edit()
                        .putBoolean("base_apartada", true).commit();
            }
        } catch (Throwable ignored) {
        }
        final Thread.UncaughtExceptionHandler anterior = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) -> {
            File registro = new File(getFilesDir(), "errores.log");
            /* Sin rotación el archivo crece indefinidamente. Se guarda una sola
               generación anterior: alcanza para diagnosticar y no llena el disco. */
            if (registro.length() > 1024 * 1024) {
                File viejo = new File(getFilesDir(), "errores.log.old");
                viejo.delete();
                registro.renameTo(viejo);
            }
            try (PrintWriter salida = new PrintWriter(new FileWriter(registro, true))) {
                salida.println("=== " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        .format(new Date()));
                error.printStackTrace(salida);
            } catch (Exception ignored) {
            }
            Log.e("MiNegocio", "Error no controlado", error);
            if (anterior != null) anterior.uncaughtException(hilo, error);
        });
    }
}
