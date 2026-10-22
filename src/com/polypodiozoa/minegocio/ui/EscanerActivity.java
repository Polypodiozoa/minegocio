package com.polypodiozoa.minegocio.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.util.LectorCodigos;
import com.polypodiozoa.minegocio.util.Ui;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lector local de códigos de barras o QR con la cámara. También acepta el código
 * escrito a mano o leído de una imagen, para que funcione sin cámara o sin permiso.
 * No guarda fotos ni envía nada.
 */
public class EscanerActivity extends BaseActivity implements SurfaceHolder.Callback {

    public static final String EXTRA_CODIGO = "codigo";
    private static final int PERMISO_CAMARA = 701;
    private static final int PEDIR_IMAGEN = 702;

    private SurfaceView vistaCamara;
    private TextView txtEstado, txtAyuda;
    private EditText etManual;
    private Button btnBarras, btnQr, btnLinterna;
    private String modo = LectorCodigos.MODO_BARRAS;

    private Camera camara;
    private int anchoPreview, altoPreview;
    private boolean superficieLista;
    private boolean linterna;
    private boolean permisoPedido;
    private boolean permisoDenegado;
    private final AtomicBoolean procesando = new AtomicBoolean(false);
    private final AtomicBoolean terminado = new AtomicBoolean(false);
    private ExecutorService ejecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_escaner);
        configurarBarra("Escanear código");

        vistaCamara = findViewById(R.id.vistaCamara);
        txtEstado = findViewById(R.id.txtEstado);
        txtAyuda = findViewById(R.id.txtAyuda);
        etManual = findViewById(R.id.etManual);
        btnBarras = findViewById(R.id.btnBarras);
        btnQr = findViewById(R.id.btnQr);
        btnLinterna = findViewById(R.id.btnLinterna);
        ejecutor = Executors.newSingleThreadExecutor();

        modo = LectorCodigos.esModoQr(bd.ajuste("escaner_formato", LectorCodigos.MODO_BARRAS))
                ? LectorCodigos.MODO_QR : LectorCodigos.MODO_BARRAS;
        vistaCamara.getHolder().addCallback(this);
        btnBarras.setOnClickListener(v -> cambiarModo(LectorCodigos.MODO_BARRAS));
        btnQr.setOnClickListener(v -> cambiarModo(LectorCodigos.MODO_QR));
        btnLinterna.setOnClickListener(v -> alternarLinterna());
        findViewById(R.id.btnUsarManual).setOnClickListener(v -> usarManual());
        findViewById(R.id.btnImagen).setOnClickListener(v -> elegirImagen());
        etManual.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                usarManual();
                return true;
            }
            return false;
        });
        actualizarModo();

        if (!tieneCamara()) {
            txtEstado.setText("Esta tablet no tiene cámara disponible. Escribe el código o elige una imagen.");
            btnLinterna.setVisibility(View.GONE);
            txtAyuda.setText("Sin cámara puedes usar el código escrito o una foto del código.");
        } else if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            txtEstado.setText("Se necesita la cámara solo para leer el código.");
            solicitarPermiso();
        }
    }

    private boolean tieneCamara() {
        try {
            return Camera.getNumberOfCameras() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void solicitarPermiso() {
        if (permisoPedido) return;
        permisoPedido = true;
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permisos, int[] resultados) {
        super.onRequestPermissionsResult(requestCode, permisos, resultados);
        if (requestCode != PERMISO_CAMARA) return;
        boolean concedido = resultados.length > 0
                && resultados[0] == PackageManager.PERMISSION_GRANTED;
        if (concedido) {
            permisoDenegado = false;
            txtEstado.setText("Apunta al código.");
            abrirCamaraSiPuede();
        } else {
            permisoDenegado = true;
            txtEstado.setText("Sin permiso de cámara. Escribe el código o elige una imagen.");
            txtAyuda.setText("Puedes seguir usando el lector con el código escrito o una foto.");
            btnLinterna.setVisibility(View.GONE);
            Ui.anunciar(txtEstado, txtEstado.getText().toString());
        }
    }

    private void cambiarModo(String nuevo) {
        modo = LectorCodigos.esModoQr(nuevo) ? LectorCodigos.MODO_QR : LectorCodigos.MODO_BARRAS;
        bd.guardarAjuste("escaner_formato", modo);
        actualizarModo();
    }

    private void actualizarModo() {
        boolean qr = LectorCodigos.esModoQr(modo);
        Ui.estiloFiltro(btnBarras, !qr);
        Ui.estiloFiltro(btnQr, qr);
        btnBarras.setContentDescription("Leer código de barras" + (!qr ? ", activo" : ""));
        btnQr.setContentDescription("Leer código QR" + (qr ? ", activo" : ""));
        if (permisoDenegado) {
            txtEstado.setText("Sin permiso de cámara. Escribe el código o elige una imagen.");
        } else if (camara != null) {
            txtEstado.setText(qr ? "Apunta al código QR" : "Apunta al código de barras");
        } else {
            txtEstado.setText(qr ? "Apunta al código QR" : "Apunta al código de barras");
        }
        txtAyuda.setText(qr
                ? "Modo QR: apunta al código. También puedes escribir el contenido o elegir una imagen."
                : "Modo código de barras: EAN, UPC, Code 128, Code 39. También puedes escribir el código o elegir una imagen.");
    }

    private void elegirImagen() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        try {
            startActivityForResult(i, PEDIR_IMAGEN);
        } catch (Exception e) {
            aviso("No hay una aplicación para elegir imágenes.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PEDIR_IMAGEN || resultCode != RESULT_OK
                || data == null || data.getData() == null) return;
        final Uri uri = data.getData();
        txtEstado.setText("Leyendo la imagen…");
        ejecutor.execute(() -> {
            String codigo = leerImagen(uri);
            runOnUiThread(() -> {
                if (codigo != null) {
                    devolver(codigo);
                } else {
                    txtEstado.setText("No se encontró un código en la imagen. Prueba con otra foto.");
                    Ui.anunciar(txtEstado, "No se encontró un código en la imagen.");
                }
            });
        });
    }

    private String leerImagen(Uri uri) {
        try {
            BitmapFactory.Options opciones = new BitmapFactory.Options();
            opciones.inJustDecodeBounds = true;
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(in, null, opciones);
            }
            int muestra = 1;
            while (opciones.outWidth / (muestra * 2) > 2000
                    || opciones.outHeight / (muestra * 2) > 2000) {
                muestra *= 2;
            }
            BitmapFactory.Options reales = new BitmapFactory.Options();
            reales.inSampleSize = Math.max(1, muestra);
            Bitmap mapa;
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                mapa = BitmapFactory.decodeStream(in, null, reales);
            }
            if (mapa == null) return null;
            int ancho = mapa.getWidth();
            int alto = mapa.getHeight();
            int[] pixeles = new int[ancho * alto];
            mapa.getPixels(pixeles, 0, ancho, 0, 0, ancho, alto);
            String codigo = LectorCodigos.leerPixeles(pixeles, ancho, alto, modo);
            if (codigo == null) {
                codigo = LectorCodigos.leerPixeles(pixeles, ancho, alto,
                        LectorCodigos.esModoQr(modo)
                                ? LectorCodigos.MODO_BARRAS : LectorCodigos.MODO_QR);
            }
            mapa.recycle();
            return codigo;
        } catch (Exception e) {
            return null;
        }
    }

    private void usarManual() {
        String codigo = etManual.getText() == null ? "" : etManual.getText().toString().trim();
        if (codigo.isEmpty()) {
            error(etManual, "Escribe o pega el código.");
            return;
        }
        devolver(codigo);
    }

    private void devolver(String codigo) {
        if (!terminado.compareAndSet(false, true)) return;
        txtEstado.setText("Código leído: " + codigo);
        txtEstado.announceForAccessibility("Código leído: " + codigo);
        Intent data = new Intent();
        data.putExtra(EXTRA_CODIGO, codigo);
        setResult(RESULT_OK, data);
        finish();
    }

    // ------------------------------------------------------------ cámara

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        superficieLista = true;
        abrirCamaraSiPuede();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        abrirCamaraSiPuede();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        superficieLista = false;
        cerrarCamara();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (superficieLista && vistaCamara != null
                && vistaCamara.getHolder().getSurface().isValid()) {
            abrirCamaraSiPuede();
        }
    }

    @Override
    protected void onPause() {
        cerrarCamara();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        terminado.set(true);
        cerrarCamara();
        if (ejecutor != null) ejecutor.shutdownNow();
        super.onDestroy();
    }

    private void abrirCamaraSiPuede() {
        if (terminado.get() || camara != null || !superficieLista) return;
        if (!tieneCamara()) return;
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            solicitarPermiso();
            return;
        }
        try {
            int id = elegirCamara();
            camara = Camera.open(id);
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(id, info);
            camara.setDisplayOrientation(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ? 270 : 90);

            Camera.Parameters parametros = camara.getParameters();
            Camera.Size tamano = elegirTamano(parametros.getSupportedPreviewSizes());
            if (tamano != null) {
                parametros.setPreviewSize(tamano.width, tamano.height);
                anchoPreview = tamano.width;
                altoPreview = tamano.height;
            }
            parametros.setPreviewFormat(ImageFormat.NV21);
            List<String> enfoques = parametros.getSupportedFocusModes();
            if (enfoques != null) {
                if (enfoques.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parametros.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (enfoques.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parametros.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            }
            List<String> flashes = parametros.getSupportedFlashModes();
            boolean tieneLinterna = flashes != null && flashes.contains(Camera.Parameters.FLASH_MODE_TORCH);
            btnLinterna.setVisibility(tieneLinterna ? View.VISIBLE : View.GONE);
            camara.setParameters(parametros);
            camara.setPreviewDisplay(vistaCamara.getHolder());
            camara.setPreviewCallback(this::onPreviewFrame);
            camara.startPreview();
            linterna = false;
            btnLinterna.setText("Linterna");
            txtEstado.setText(LectorCodigos.esModoQr(modo)
                    ? "Apunta al código QR" : "Apunta al código de barras");
        } catch (Exception e) {
            cerrarCamara();
            txtEstado.setText("No se pudo abrir la cámara. Escribe el código o elige una imagen.");
            txtAyuda.setText("La cámara no está disponible ahora. Puedes usar el código manual o una foto.");
            btnLinterna.setVisibility(View.GONE);
        }
    }

    private int elegirCamara() {
        int total = Camera.getNumberOfCameras();
        for (int i = 0; i < total; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) return i;
        }
        return 0;
    }

    private Camera.Size elegirTamano(List<Camera.Size> tamanos) {
        if (tamanos == null || tamanos.isEmpty()) return null;
        Camera.Size mejor = tamanos.get(0);
        long mejorPuntaje = Long.MAX_VALUE;
        for (Camera.Size t : tamanos) {
            if (t.width < 480 || t.height < 320) continue;
            long puntaje = Math.abs(t.width - 1280L) + Math.abs(t.height - 720L);
            if (puntaje < mejorPuntaje) {
                mejorPuntaje = puntaje;
                mejor = t;
            }
        }
        return mejor;
    }

    private void onPreviewFrame(byte[] datos, Camera camaraOrigen) {
        if (datos == null || terminado.get() || camara == null) return;
        if (!procesando.compareAndSet(false, true)) return;
        final byte[] copia = datos.clone();
        final int ancho = anchoPreview;
        final int alto = altoPreview;
        final String modoActual = modo;
        ejecutor.execute(() -> {
            try {
                String codigo = LectorCodigos.leerNv21(copia, ancho, alto, modoActual);
                if (codigo != null && !codigo.isEmpty()) {
                    runOnUiThread(() -> devolver(codigo));
                }
            } finally {
                procesando.set(false);
            }
        });
    }

    private void alternarLinterna() {
        if (camara == null) {
            aviso("La cámara aún no está lista.");
            return;
        }
        try {
            Camera.Parameters parametros = camara.getParameters();
            List<String> flashes = parametros.getSupportedFlashModes();
            if (flashes == null || !flashes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                aviso("Esta cámara no tiene linterna.");
                return;
            }
            linterna = !linterna;
            parametros.setFlashMode(linterna
                    ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            camara.setParameters(parametros);
            btnLinterna.setText(linterna ? "Apagar" : "Linterna");
            btnLinterna.setContentDescription(linterna ? "Apagar linterna" : "Encender linterna");
        } catch (Exception e) {
            linterna = false;
            btnLinterna.setText("Linterna");
        }
    }

    private void cerrarCamara() {
        if (camara == null) return;
        try {
            camara.setPreviewCallback(null);
            camara.stopPreview();
        } catch (Exception ignored) {
        }
        try {
            camara.release();
        } catch (Exception ignored) {
        }
        camara = null;
        linterna = false;
        if (btnLinterna != null) btnLinterna.setText("Linterna");
    }
}
