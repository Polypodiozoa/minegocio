package com.polypodiozoa.minegocio.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.datos.BD;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Archivos;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.Fechas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** Base de todas las pantallas: barra superior, escala de texto y avisos accesibles. */
public abstract class BaseActivity extends android.app.Activity {

    public static final String PREFS_UI = "ui";
    public static final String CLAVE_ESCALA = "escala_texto";
    private static final int PERMISO_CAMARA_FOTO = 900;

    protected BD bd;
    protected File fotoPendiente;
    protected String nombreArchivoPendiente = "";
    protected String mimeArchivoPendiente = "";
    private int fotoPendientePermiso = -1;

    /** Escala con la que se creó esta pantalla, para detectar cambios de tamaño. */
    private float escalaAplicada = 1f;

    @Override
    protected void attachBaseContext(Context base) {
        SharedPreferences p = base.getSharedPreferences(PREFS_UI, Context.MODE_PRIVATE);
        float factor = p.getFloat(CLAVE_ESCALA, 1f);
        escalaAplicada = factor;
        if (factor > 0.01f && Math.abs(factor - 1f) > 0.001f) {
            Configuration config = new Configuration(base.getResources().getConfiguration());
            config.fontScale = config.fontScale * factor;
            base = base.createConfigurationContext(config);
        }
        super.attachBaseContext(base);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* Si el tamaño de texto cambió en Ajustes, esta pantalla se vuelve a
           crear para que lo tome. Antes solo cambiaba dentro de Ajustes. */
        SharedPreferences p = getSharedPreferences(PREFS_UI, MODE_PRIVATE);
        float actual = p.getFloat(CLAVE_ESCALA, 1f);
        if (Math.abs(actual - escalaAplicada) > 0.001f) {
            recreate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bd = BD.get(this);
        Window w = getWindow();
        w.setStatusBarColor(getColor(R.color.primario_oscuro));
        w.setNavigationBarColor(getColor(R.color.superficie));
        if (Build.VERSION.SDK_INT >= 26) {
            View decor = w.getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        final View desplazable = findViewById(R.id.scrollForm);
        if (desplazable != null) {
            desplazable.post(() -> desplazable.scrollTo(0, 0));
        }
    }

    protected void configurarBarra(String titulo) {
        TextView t = findViewById(R.id.tituloBarra);
        if (t != null) {
            t.setText(titulo);
            t.setContentDescription(titulo);
        }
        View atras = findViewById(R.id.btnAtras);
        if (atras != null) {
            atras.setContentDescription("Volver");
            atras.setOnClickListener(v -> onBackPressed());
        }
    }

    protected void abrir(Class<?> destino) {
        startActivity(new Intent(this, destino));
    }

    protected void abrirResultado(Class<?> destino, int codigo) {
        startActivityForResult(new Intent(this, destino), codigo);
    }

    protected void aviso(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        View raiz = getWindow().getDecorView();
        raiz.announceForAccessibility(mensaje);
    }

    protected void informar(String titulo, String mensaje) {
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Entendido", null)
                .create();
        d.setOnShowListener(dialogo -> agrandarDialogo(d));
        d.show();
    }

    /** Texto original de los campos, para detectar cambios sin guardar. */
    private final java.util.Map<EditText, String> valoresIniciales = new java.util.HashMap<>();

    /** Recuerda cómo estaban los campos al abrir la pantalla. */
    protected void marcarInicial(EditText... campos) {
        valoresIniciales.clear();
        for (EditText c : campos) {
            if (c != null) {
                valoresIniciales.put(c, c.getText() == null ? "" : c.getText().toString());
            }
        }
    }

    protected boolean hayCambios() {
        for (java.util.Map.Entry<EditText, String> e : valoresIniciales.entrySet()) {
            String actual = e.getKey().getText() == null ? "" : e.getKey().getText().toString();
            if (!actual.equals(e.getValue())) return true;
        }
        return false;
    }

    /** Sale pidiendo confirmación si quedaron cambios sin guardar. */
    protected void salirSiCambios() {
        if (!hayCambios()) {
            finish();
            return;
        }
        confirmar("Descartar cambios",
                "Si sales ahora se perderá lo que escribiste.",
                "Descartar", this::finish);
    }

    protected void confirmar(String titulo, String mensaje, String textoAccion, Runnable accion) {
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton(textoAccion, (dialogo, w) -> accion.run())
                .setNegativeButton("Cancelar", null)
                .create();
        d.setOnShowListener(dialogo -> agrandarDialogo(d));
        d.show();
    }

    protected void agrandarDialogo(AlertDialog d) {
        TextView mensaje = d.findViewById(android.R.id.message);
        if (mensaje != null) {
            mensaje.setTextSize(17);
            mensaje.setLineSpacing(0f, 1.15f);
        }
        Button positivo = d.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativo = d.getButton(AlertDialog.BUTTON_NEGATIVE);
        /* MinHeight trabaja en píxeles: hay que convertir los 52 dp o en una
           pantalla de 213 dpi el botón queda en 39 dp. */
        int altoBoton = Math.round(52 * getResources().getDisplayMetrics().density);
        if (positivo != null) {
            positivo.setTextSize(17);
            positivo.setAllCaps(false);
            positivo.setMinHeight(altoBoton);
        }
        if (negativo != null) {
            negativo.setTextSize(17);
            negativo.setAllCaps(false);
            negativo.setMinHeight(altoBoton);
        }
    }

    protected void error(EditText campo, String mensaje) {
        campo.setError(mensaje);
        campo.requestFocus();
        campo.announceForAccessibility(mensaje);
    }

    protected long numero(EditText campo) {
        return Formatos.parsePesos(campo.getText().toString());
    }

    protected int entero(EditText campo) {
        return Formatos.parseInt(campo.getText().toString());
    }

    protected double decimal(EditText campo) {
        return Formatos.parseDecimal(campo.getText().toString());
    }

    protected String texto(EditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    protected void enfocar(EditText campo) {
        campo.requestFocus();
    }

    protected void volverConResultado(int codigo, long id) {
        Intent data = new Intent();
        data.putExtra("id", id);
        setResult(codigo, data);
        finish();
    }

    protected long idExtra(String nombre) {
        return getIntent().getLongExtra(nombre, 0);
    }


    // ------------------------------------------------------- fotos y archivos

    protected void tomarFoto(int codigo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            fotoPendientePermiso = codigo;
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA_FOTO);
            return;
        }
        abrirCamara(codigo);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permisos, int[] resultados) {
        super.onRequestPermissionsResult(requestCode, permisos, resultados);
        if (requestCode != PERMISO_CAMARA_FOTO) return;
        int codigo = fotoPendientePermiso;
        fotoPendientePermiso = -1;
        if (codigo > 0 && resultados.length > 0
                && resultados[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara(codigo);
        } else if (codigo > 0) {
            aviso("Para tomar la foto necesitas permitir la cámara. También puedes elegir una imagen.");
        }
    }

    private void abrirCamara(int codigo) {
        fotoPendiente = Archivos.nuevoArchivoTemporal(this, "foto", "jpg");
        nombreArchivoPendiente = "Foto " + Fechas.fechaHora(Fechas.ahora()) + ".jpg";
        mimeArchivoPendiente = "image/jpeg";
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri salida = Archivos.uriTemporal(this, fotoPendiente);
        i.putExtra(MediaStore.EXTRA_OUTPUT, salida);
        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(i, codigo);
        } catch (ActivityNotFoundException e) {
            fotoPendiente.delete();
            fotoPendiente = null;
            aviso("Este dispositivo no tiene aplicación de cámara. Usa el botón Elegir archivo.");
        }
    }

    protected String resultadoFoto(int resultCode, Intent data) {
        if (fotoPendiente == null) return null;
        if (resultCode == RESULT_OK) {
            if (fotoPendiente.length() == 0 && data != null && data.getExtras() != null) {
                Object mini = data.getExtras().get("data");
                if (mini instanceof Bitmap) {
                    try (FileOutputStream out = new FileOutputStream(fotoPendiente)) {
                        ((Bitmap) mini).compress(Bitmap.CompressFormat.JPEG, 90, out);
                    } catch (IOException ignored) {
                    }
                }
            }
            if (fotoPendiente.length() > 0) {
                String ruta = fotoPendiente.getAbsolutePath();
                fotoPendiente = null;
                return ruta;
            }
        }
        fotoPendiente.delete();
        fotoPendiente = null;
        return null;
    }

    protected void elegirArchivo(int codigo) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/pdf"});
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(i, codigo);
        } catch (ActivityNotFoundException e) {
            aviso("Este dispositivo no tiene una aplicación para elegir archivos.");
        }
    }

    protected String resultadoArchivo(int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return null;
        Uri origen = data.getData();
        nombreArchivoPendiente = Archivos.nombreDe(origen, this);
        mimeArchivoPendiente = Archivos.mimeDe(origen, this);
        try {
            File copia = Archivos.copiarATemporal(this, origen);
            return copia.getAbsolutePath();
        } catch (IOException e) {
            aviso("No se pudo leer el archivo: " + e.getMessage());
            return null;
        }
    }

    protected Modelo.Adjunto crearAdjunto(String ruta) {
        Modelo.Adjunto a = new Modelo.Adjunto();
        a.ruta = ruta;
        a.nombre = nombreArchivoPendiente == null || nombreArchivoPendiente.isEmpty()
                ? Archivos.nombreVisible(ruta) : nombreArchivoPendiente;
        a.mime = mimeArchivoPendiente == null || mimeArchivoPendiente.isEmpty()
                ? "application/octet-stream" : mimeArchivoPendiente;
        return a;
    }

    protected void abrirAdjunto(Modelo.Adjunto a) {
        if (a == null || a.ruta == null || a.ruta.isEmpty()) return;
        if (Archivos.esImagen(a.ruta, a.mime)) {
            Intent i = new Intent(this, FotoActivity.class);
            i.putExtra("ruta", a.ruta);
            startActivity(i);
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri uri = a.ruta.startsWith(Archivos.dirTemporal(this).getAbsolutePath())
                ? Archivos.uriTemporal(this, new File(a.ruta))
                : Archivos.uriPermanente(this, a.ruta);
        i.setDataAndType(uri, a.mime == null || a.mime.isEmpty() ? "*/*" : a.mime);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            informar("No hay aplicación para abrirlo",
                    "No se encontró una aplicación que pueda abrir este tipo de archivo ("
                            + a.mime + ").");
        }
    }

    protected void verAdjuntos(String entidad, long id) {
        java.util.List<Modelo.Adjunto> lista = bd.adjuntos(entidad, id);
        if (lista.isEmpty()) {
            aviso("No hay comprobantes guardados.");
            return;
        }
        if (lista.size() == 1) {
            abrirAdjunto(lista.get(0));
            return;
        }
        String[] nombres = new String[lista.size()];
        for (int i = 0; i < lista.size(); i++) {
            Modelo.Adjunto a = lista.get(i);
            nombres[i] = a.nombre == null || a.nombre.isEmpty() ? "Comprobante " + (i + 1) : a.nombre;
        }
        new AlertDialog.Builder(this)
                .setTitle("Comprobantes")
                .setItems(nombres, (d, cual) -> abrirAdjunto(lista.get(cual)))
                .setNegativeButton("Cerrar", null)
                .show();
    }

    protected void borrarTemporal(String ruta) {
        if (ruta != null && ruta.startsWith(Archivos.dirTemporal(this).getAbsolutePath())) {
            Archivos.borrar(ruta);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
