package com.polypodiozoa.minegocio.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.datos.Respaldo;
import com.polypodiozoa.minegocio.util.Archivos;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.Imagen;
import com.polypodiozoa.minegocio.util.QrPago;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AjustesActivity extends BaseActivity {

    private static final int PEDIR_DESTINO_COPIA = 301;
    private static final int PEDIR_COPIA_RESTAURAR = 302;
    private static final int PEDIR_QR = 303;
    private static final int PEDIR_QR_FOTO = 304;
    private static final int PERMISO_CAMARA_QR = 305;

    private EditText etNegocio, etNit, etTelefono, etDireccion, etImpuesto;
    private Switch swImpuesto, swStockNegativo;
    private RadioGroup grupoTexto;
    private RadioGroup grupoEscaner;
    private TextView txtUltimaCopia;
    private Button btnCrearCopia, btnRestaurar;
    private EditText etBreB, etCuenta, etBanco, etTitular;
    private ImageView imgQrPago;
    private Button btnQuitarQr, btnTomarQr, btnElegirQr;
    private String rutaQr = "", qrOriginal = "", qrTemporal = "";
    private boolean quitarQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);
        configurarBarra("Ajustes");
        etNegocio = findViewById(R.id.etNegocio);
        etNit = findViewById(R.id.etNit);
        etTelefono = findViewById(R.id.etTelefono);
        etDireccion = findViewById(R.id.etDireccion);
        etImpuesto = findViewById(R.id.etImpuesto);
        swImpuesto = findViewById(R.id.swImpuesto);
        swStockNegativo = findViewById(R.id.swStockNegativo);
        grupoTexto = findViewById(R.id.grupoTexto);
        grupoEscaner = findViewById(R.id.grupoEscaner);
        txtUltimaCopia = findViewById(R.id.txtUltimaCopia);
        btnCrearCopia = findViewById(R.id.btnCrearCopia);
        btnRestaurar = findViewById(R.id.btnRestaurar);
        etBreB = findViewById(R.id.etBreB);
        etCuenta = findViewById(R.id.etCuenta);
        etBanco = findViewById(R.id.etBanco);
        etTitular = findViewById(R.id.etTitular);
        imgQrPago = findViewById(R.id.imgQrPago);
        btnQuitarQr = findViewById(R.id.btnQuitarQr);
        btnTomarQr = findViewById(R.id.btnTomarQr);
        btnElegirQr = findViewById(R.id.btnElegirQr);
        btnTomarQr.setOnClickListener(v -> tomarQrConPermiso());
        btnElegirQr.setOnClickListener(v -> elegirQr());
        btnQuitarQr.setOnClickListener(v -> quitarQr());
        findViewById(R.id.btnPersonalizarMostrador).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, MostradorConfigActivity.class));
        });

        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
        btnCrearCopia.setOnClickListener(v -> pedirDestinoCopia());
        btnRestaurar.setOnClickListener(v -> pedirCopiaRestaurar());
        findViewById(R.id.btnEjemplo).setOnClickListener(v -> cargarEjemplo());
        findViewById(R.id.btnBorrarTodo).setOnClickListener(v -> borrarTodo());

        cargar();
    }

    private void cargar() {
        etNegocio.setText(bd.ajuste("negocio_nombre", ""));
        etNit.setText(bd.ajuste("negocio_nit", ""));
        etTelefono.setText(bd.ajuste("negocio_telefono", ""));
        etDireccion.setText(bd.ajuste("negocio_direccion", ""));
        etImpuesto.setText(Formatos.decimal(bd.ajusteDecimal("impuesto_pct", 19)));
        swImpuesto.setChecked(bd.ajusteSi("impuesto_activado", true));
        swStockNegativo.setChecked(bd.ajusteSi("permitir_stock_negativo", false));
        etBreB.setText(bd.ajuste("pago_bre_b", ""));
        etCuenta.setText(bd.ajuste("pago_cuenta", ""));
        etBanco.setText(bd.ajuste("pago_banco", ""));
        etTitular.setText(bd.ajuste("pago_titular", ""));
        rutaQr = bd.ajuste("pago_qr_foto", "");
        qrOriginal = rutaQr;
        quitarQr = false;
        mostrarQr();
        if ("qr".equals(bd.ajuste("escaner_formato", "barras"))) {
            grupoEscaner.check(R.id.rbEscanerQr);
        } else {
            grupoEscaner.check(R.id.rbEscanerBarras);
        }

        SharedPreferences p = getSharedPreferences(PREFS_UI, MODE_PRIVATE);
        float escala = p.getFloat(CLAVE_ESCALA, 1f);
        if (Math.abs(escala - 1.15f) < 0.01f) grupoTexto.check(R.id.rbGrande);
        else if (Math.abs(escala - 1.3f) < 0.01f) grupoTexto.check(R.id.rbMuyGrande);
        else grupoTexto.check(R.id.rbSistema);

        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            ((TextView) findViewById(R.id.txtVersion)).setText("Versión " + version
                    + " · Hecha para negocios Colombianos.");
        } catch (Exception ignored) {
        }
        cargarUltimaCopia();
    }

    private void cargarUltimaCopia() {
        long ultima = bd.ajusteNumero("ultima_copia", 0);
        if (ultima <= 0) {
            txtUltimaCopia.setText("Última copia: nunca. Crea la primera copia hoy.");
        } else {
            txtUltimaCopia.setText("Última copia: " + Fechas.fechaHora(ultima));
        }
    }

    private void guardar() {
        double tasa = decimal(etImpuesto);
        if (etImpuesto.getText().toString().trim().isEmpty()) tasa = 0;
        if (tasa < 0 || tasa > 100) {
            error(etImpuesto, "Escribe un porcentaje entre 0 y 100.");
            return;
        }
        bd.guardarAjuste("negocio_nombre", texto(etNegocio));
        bd.guardarAjuste("negocio_nit", texto(etNit));
        bd.guardarAjuste("negocio_telefono", texto(etTelefono));
        bd.guardarAjuste("negocio_direccion", texto(etDireccion));
        bd.guardarAjuste("impuesto_pct", Formatos.decimal(tasa));
        bd.guardarAjuste("impuesto_activado", swImpuesto.isChecked());
        bd.guardarAjuste("permitir_stock_negativo", swStockNegativo.isChecked());
        bd.guardarAjuste("escaner_formato",
                grupoEscaner.getCheckedRadioButtonId() == R.id.rbEscanerQr ? "qr" : "barras");
        bd.guardarAjuste("pago_bre_b", texto(etBreB));
        bd.guardarAjuste("pago_cuenta", texto(etCuenta));
        bd.guardarAjuste("pago_banco", texto(etBanco));
        bd.guardarAjuste("pago_titular", texto(etTitular));

        if (quitarQr) {
            if (!qrOriginal.isEmpty()) Archivos.borrar(qrOriginal);
            rutaQr = "";
            qrOriginal = "";
            quitarQr = false;
        } else if (!qrTemporal.isEmpty()) {
            String nueva = Archivos.moverAPermanente(this, qrTemporal);
            qrTemporal = "";
            if (!nueva.isEmpty()) {
                if (!qrOriginal.isEmpty() && !qrOriginal.equals(nueva)) Archivos.borrar(qrOriginal);
                rutaQr = nueva;
                qrOriginal = nueva;
            }
        }
        bd.guardarAjuste("pago_qr_foto", rutaQr);
        mostrarQr();

        float nueva = 1f;
        if (grupoTexto.getCheckedRadioButtonId() == R.id.rbGrande) nueva = 1.15f;
        else if (grupoTexto.getCheckedRadioButtonId() == R.id.rbMuyGrande) nueva = 1.3f;
        SharedPreferences p = getSharedPreferences(PREFS_UI, MODE_PRIVATE);
        float anterior = p.getFloat(CLAVE_ESCALA, 1f);
        p.edit().putFloat(CLAVE_ESCALA, nueva).apply();

        aviso("Ajustes guardados.");
        if (Math.abs(nueva - anterior) > 0.001f) {
            recreate();
        }
    }

    private void procesarQr(String origen) {
        if (origen == null || origen.isEmpty()) return;
        ProgressDialog progreso = new ProgressDialog(this);
        progreso.setMessage("Preparando el QR…");
        progreso.setCancelable(false);
        progreso.show();
        btnTomarQr.setEnabled(false);
        btnElegirQr.setEnabled(false);
        new Thread(() -> {
            Bitmap limpio = QrPago.limpiar(origen);
            String ruta = null;
            if (limpio != null) {
                File salida = Archivos.nuevoArchivoTemporal(this, "qr", "png");
                try (FileOutputStream out = new FileOutputStream(salida)) {
                    limpio.compress(Bitmap.CompressFormat.PNG, 100, out);
                    ruta = salida.getAbsolutePath();
                } catch (IOException e) {
                    Archivos.borrar(salida.getAbsolutePath());
                }
            }
            Archivos.borrar(origen);
            final String nueva = ruta;
            runOnUiThread(() -> {
                progreso.dismiss();
                if (isFinishing()) {
                    if (nueva != null) Archivos.borrar(nueva);
                    return;
                }
                btnTomarQr.setEnabled(true);
                btnElegirQr.setEnabled(true);
                if (nueva == null) {
                    aviso("No se pudo leer el QR. Acércalo, evita reflejos y muestra los cuatro bordes.");
                    return;
                }
                qrTemporal = nueva;
                rutaQr = nueva;
                quitarQr = false;
                mostrarQr();
                aviso("QR limpio listo. Guarda los ajustes para conservarlo.");
            });
        }).start();
    }

    private void tomarQrConPermiso() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA_QR);
            return;
        }
        tomarFoto(PEDIR_QR_FOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permisos, int[] resultados) {
        super.onRequestPermissionsResult(requestCode, permisos, resultados);
        if (requestCode != PERMISO_CAMARA_QR) return;
        if (resultados.length > 0 && resultados[0] == PackageManager.PERMISSION_GRANTED) {
            tomarFoto(PEDIR_QR_FOTO);
        } else {
            aviso("Para tomar la foto necesitas permitir la cámara. También puedes elegir una imagen.");
        }
    }

    private void elegirQr() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        try {
            startActivityForResult(i, PEDIR_QR);
        } catch (Exception e) {
            aviso("No hay una aplicación para elegir imágenes.");
        }
    }

    private void quitarQr() {
        if (!qrTemporal.isEmpty()) {
            Archivos.borrar(qrTemporal);
            qrTemporal = "";
        }
        quitarQr = true;
        rutaQr = "";
        mostrarQr();
    }

    private void mostrarQr() {
        Bitmap b = Imagen.cargar(rutaQr, 800);
        if (b != null) {
            imgQrPago.setImageBitmap(b);
            imgQrPago.setScaleType(ImageView.ScaleType.FIT_CENTER);
            btnQuitarQr.setVisibility(android.view.View.VISIBLE);
        } else {
            imgQrPago.setImageResource(R.drawable.ic_qr);
            imgQrPago.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            btnQuitarQr.setVisibility(android.view.View.GONE);
        }
    }

    private void pedirDestinoCopia() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/zip");
        i.putExtra(Intent.EXTRA_TITLE,
                "minegocio-" + Fechas.formato("yyyyMMdd-HHmm", Fechas.ahora()) + ".zip");
        try {
            startActivityForResult(i, PEDIR_DESTINO_COPIA);
        } catch (Exception e) {
            informar("Sin aplicación de archivos",
                    "No se encontró una aplicación para elegir dónde guardar la copia. "
                            + "Puedes instalarla desde la tienda de el dispositivo.");
        }
    }

    private void pedirCopiaRestaurar() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES,
                new String[]{"application/zip", "application/x-zip-compressed", "application/octet-stream"});
        try {
            startActivityForResult(i, PEDIR_COPIA_RESTAURAR);
        } catch (Exception e) {
            informar("Sin aplicación de archivos",
                    "No se encontró una aplicación para elegir la copia de seguridad.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PEDIR_QR) {
            if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
            String ruta = resultadoArchivo(resultCode, data);
            if (ruta == null) return;
            if (!Archivos.esImagen(ruta, mimeArchivoPendiente)) {
                Archivos.borrar(ruta);
                aviso("Elige una imagen del código QR.");
                return;
            }
            procesarQr(ruta);
            return;
        }
        if (requestCode == PEDIR_QR_FOTO) {
            String ruta = resultadoFoto(resultCode, data);
            if (ruta != null) procesarQr(ruta);
            return;
        }
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == PEDIR_DESTINO_COPIA) {
            crearCopia(uri);
        } else if (requestCode == PEDIR_COPIA_RESTAURAR) {
            confirmarRestauracion(uri);
        }
    }

    private void crearCopia(Uri destino) {
        ProgressDialog progreso = new ProgressDialog(this);
        progreso.setMessage("Creando copia de seguridad…");
        progreso.setCancelable(false);
        progreso.show();
        btnCrearCopia.setEnabled(false);
        new Thread(() -> {
            String error = null;
            try {
                Respaldo.exportar(getApplicationContext(), destino);
                bd.guardarAjuste("ultima_copia", Fechas.ahora());
            } catch (Exception e) {
                error = e.getMessage();
            }
            final String mensajeError = error;
            runOnUiThread(() -> {
                progreso.dismiss();
                btnCrearCopia.setEnabled(true);
                cargarUltimaCopia();
                if (mensajeError == null) {
                    aviso("Copia creada correctamente.");
                } else {
                    informar("No se pudo crear la copia",
                            "Ocurrió un problema: " + mensajeError
                                    + ". Intenta guardarla en otra carpeta.");
                }
            });
        }).start();
    }

    private void confirmarRestauracion(Uri origen) {
        confirmar("Restaurar copia",
                "Se reemplazarán todos los datos actuales por los de la copia. "
                        + "Si tienes información nueva, crea primero una copia de seguridad.",
                "Restaurar", () -> restaurar(origen));
    }

    private void restaurar(Uri origen) {
        ProgressDialog progreso = new ProgressDialog(this);
        progreso.setMessage("Restaurando copia…");
        progreso.setCancelable(false);
        progreso.show();
        btnRestaurar.setEnabled(false);
        new Thread(() -> {
            String error = null;
            try {
                Respaldo.restaurar(getApplicationContext(), origen);
            } catch (Exception e) {
                error = e.getMessage();
            }
            final String mensajeError = error;
            runOnUiThread(() -> {
                progreso.dismiss();
                btnRestaurar.setEnabled(true);
                if (mensajeError == null) {
                    android.app.AlertDialog d = new android.app.AlertDialog.Builder(this)
                            .setTitle("Copia restaurada")
                            .setMessage("Los datos se restauraron correctamente. La app se reiniciará "
                                    + "para cargar la información.")
                            .setCancelable(false)
                            .setPositiveButton("Reiniciar", (x, w) -> reiniciarApp())
                            .create();
                    d.setOnShowListener(x -> agrandarDialogo(d));
                    d.show();
                } else {
                    informar("No se pudo restaurar",
                            "La copia no se pudo aplicar: " + mensajeError
                                    + ". Tus datos actuales no se modificaron.");
                }
            });
        }).start();
    }

    private void reiniciarApp() {
        Intent i = new Intent(this, PrincipalActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finishAffinity();
    }

    private void cargarEjemplo() {
        confirmar("Datos de ejemplo",
                "Se agregarán 6 productos, 3 clientes con cupo de fiado, 2 proveedores, "
                        + "3 ventas (una fiada y vencida), 1 compra y 1 gasto de ejemplo. "
                        + "Puedes borrarlos después.",
                "Cargar ejemplo", () -> {
                    bd.cargarEjemplo();
                    aviso("Datos de ejemplo cargados. Ve al inicio para verlos.");
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && !qrTemporal.isEmpty()) {
            Archivos.borrar(qrTemporal);
            qrTemporal = "";
        }
    }

    private void borrarTodo() {
        confirmar("Borrar todos los datos",
                "Se eliminarán productos, ventas, compras, clientes, proveedores, gastos y fotos. "
                        + "Esta acción no se puede deshacer.",
                "Continuar", () -> confirmar("Confirmar borrado",
                        "¿Seguro que quieres borrar todo? Te recomendamos crear una copia primero.",
                        "Sí, borrar todo", this::ejecutarBorrado));
    }

    private void ejecutarBorrado() {
        bd.borrarTodo();
        File[] fotos = Archivos.dirPermanente(this).listFiles();
        if (fotos != null) {
            for (File f : fotos) f.delete();
        }
        /* Las copias que deja una restauración también son datos del negocio:
           si se borra todo, se borran. */
        new File(getFilesDir(), "copia-antes-de-restaurar.db").delete();
        borrarCarpeta(new File(getFilesDir(), "adjuntos-antes-de-restaurar"));
        new File(getFilesDir(), "errores.log").delete();
        File[] viejas = getFilesDir().listFiles();
        if (viejas != null) {
            for (File f : viejas) {
                if (f.getName().startsWith("base-danada-")) f.delete();
            }
        }
        Archivos.limpiarTemporal(this);
        bd.guardarAjuste("ultima_copia", 0);
        cargarUltimaCopia();
        aviso("Se borraron todos los datos.");
        findViewById(R.id.btnBorrarTodo).postDelayed(this::reiniciarApp, 900);
    }

    private static void borrarCarpeta(File carpeta) {
        if (carpeta == null || !carpeta.exists()) return;
        File[] hijos = carpeta.listFiles();
        if (hijos != null) {
            for (File h : hijos) {
                if (h.isDirectory()) borrarCarpeta(h);
                else h.delete();
            }
        }
        carpeta.delete();
    }
}
