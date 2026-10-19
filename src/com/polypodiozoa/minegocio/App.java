package com.polypodiozoa.minegocio;

import android.app.Application;
import android.util.Log;

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
        final Thread.UncaughtExceptionHandler anterior = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) -> {
            try (PrintWriter salida = new PrintWriter(new FileWriter(
                    new File(getFilesDir(), "errores.log"), true))) {
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
