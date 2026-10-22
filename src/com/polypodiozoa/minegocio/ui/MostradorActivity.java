package com.polypodiozoa.minegocio.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.util.Archivos;
import com.polypodiozoa.minegocio.util.Imagen;
import com.polypodiozoa.minegocio.util.MostradorModulos;
import com.polypodiozoa.minegocio.util.QrPago;
import com.polypodiozoa.minegocio.util.Ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Datos para pagar en el mostrador, con módulos y orden configurables. */
public class MostradorActivity extends BaseActivity {

    private static final String LLAVE_EJEMPLO = "@EJEMPLO2567";
    private static final String CUENTA_EJEMPLO = "#4004EJEMPLO2145";
    private static final String BANCO_EJEMPLO = "Banco de ejemplo";
    private static final String TITULAR_EJEMPLO = "Titular de ejemplo";
    private static final int PEDIR_QR_CAMARA = 401;
    private static final int PEDIR_QR_IMAGEN = 402;

    private ImageView imgQr;
    private TextView txtQrTitulo, txtQrAyuda, txtLlave, txtCuenta, txtBanco, txtTitular;
    private TextView txtAviso, txtSinModulos;
    private Button btnCompletarDatos;
    private LinearLayout contenedorModulos, cardQr, cardLlave, cardCuenta, cardAyuda;
    private String rutaQr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrador);
        configurarBarra("Mostrador");
        imgQr = findViewById(R.id.imgQr);
        txtQrTitulo = findViewById(R.id.txtQrTitulo);
        txtQrAyuda = findViewById(R.id.txtQrAyuda);
        txtAviso = findViewById(R.id.txtAvisoEjemplo);
        txtSinModulos = findViewById(R.id.txtSinModulos);
        btnCompletarDatos = findViewById(R.id.btnCompletarDatos);
        contenedorModulos = findViewById(R.id.contenedorModulos);
        cardQr = findViewById(R.id.cardQr);
        cardLlave = findViewById(R.id.cardLlave);
        cardCuenta = findViewById(R.id.cardCuenta);
        cardAyuda = findViewById(R.id.cardAyuda);
        txtLlave = findViewById(R.id.txtLlave);
        txtCuenta = findViewById(R.id.txtCuenta);
        txtBanco = findViewById(R.id.txtBanco);
        txtTitular = findViewById(R.id.txtTitular);
        btnCompletarDatos.setOnClickListener(v -> completarDatos());
        cargar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private String modulos() {
        return MostradorModulos.limpiar(bd.ajuste(
                "pago_mostrador_modulos", MostradorModulos.ORDEN_DEFECTO));
    }

    private void cargar() {
        String modulos = modulos();
        boolean qrVisible = modulos.contains(MostradorModulos.QR);
        boolean llaveVisible = modulos.contains(MostradorModulos.LLAVE);
        boolean cuentaVisible = modulos.contains(MostradorModulos.CUENTA);
        boolean ayudaVisible = modulos.contains(MostradorModulos.AYUDA);

        String llave = bd.ajuste("pago_bre_b", "").trim();
        String cuenta = bd.ajuste("pago_cuenta", "").trim();
        String banco = bd.ajuste("pago_banco", "").trim();
        String titular = bd.ajuste("pago_titular", "").trim();
        rutaQr = bd.ajuste("pago_qr_foto", "").trim();

        boolean llaveVacia = llave.isEmpty();
        boolean cuentaVacia = cuenta.isEmpty();
        boolean bancoVacio = banco.isEmpty();
        boolean titularVacio = titular.isEmpty();
        boolean ejemplo = llaveVacia || cuentaVacia || bancoVacio;
        if (llaveVacia) llave = LLAVE_EJEMPLO;
        if (cuentaVacia) cuenta = CUENTA_EJEMPLO;
        if (bancoVacio) banco = BANCO_EJEMPLO;
        if (titularVacio) titular = ejemplo ? TITULAR_EJEMPLO : "";

        txtLlave.setText(llave);
        txtCuenta.setText(cuenta);
        txtBanco.setText("Banco: " + banco);
        txtTitular.setText(titular.isEmpty() ? "" : "Titular: " + titular);
        txtTitular.setVisibility(titular.isEmpty() ? View.GONE : View.VISIBLE);

        Bitmap foto = Imagen.cargar(rutaQr, 1200);
        boolean faltaQr = qrVisible && foto == null;
        boolean faltaLlave = llaveVisible && llaveVacia;
        boolean faltaCuenta = cuentaVisible && (cuentaVacia || bancoVacio);
        boolean faltan = faltaQr || faltaLlave || faltaCuenta;
        txtAviso.setVisibility(faltan ? View.VISIBLE : View.GONE);
        btnCompletarDatos.setVisibility(faltan ? View.VISIBLE : View.GONE);
        btnCompletarDatos.setEnabled(true);

        if (foto != null) {
            imgQr.setImageBitmap(foto);
            imgQr.setScaleType(ImageView.ScaleType.FIT_CENTER);
            txtQrTitulo.setText("QR");
            txtQrAyuda.setText("Toca el QR para verlo en grande");
            txtQrAyuda.setVisibility(View.VISIBLE);
            imgQr.setClickable(true);
            imgQr.setFocusable(true);
            imgQr.setContentDescription("Código QR. Toca para verlo en grande.");
            imgQr.setOnClickListener(v -> {
                Intent i = new Intent(this, FotoActivity.class);
                i.putExtra("ruta", rutaQr);
                i.putExtra("titulo", "QR");
                i.putExtra("descripcion", "Código QR");
                startActivity(i);
            });
        } else {
            String contenido = llaveVacia ? LLAVE_EJEMPLO : llave;
            Bitmap generado = qr(contenido);
            if (generado != null) imgQr.setImageBitmap(generado);
            else imgQr.setImageResource(R.drawable.ic_qr);
            imgQr.setScaleType(ImageView.ScaleType.FIT_CENTER);
            txtQrTitulo.setText(llaveVacia ? "QR de ejemplo" : "QR");
            txtQrAyuda.setText("Configúralo una vez aquí; después se cambia en Ajustes.");
            txtQrAyuda.setVisibility(View.VISIBLE);
            imgQr.setClickable(false);
            imgQr.setFocusable(false);
            imgQr.setContentDescription(llaveVacia
                    ? "Código QR de ejemplo" : "Código QR");
            imgQr.setOnClickListener(null);
        }

        cardQr.setVisibility(qrVisible ? View.VISIBLE : View.GONE);
        cardLlave.setVisibility(llaveVisible ? View.VISIBLE : View.GONE);
        cardCuenta.setVisibility(cuentaVisible ? View.VISIBLE : View.GONE);
        cardAyuda.setVisibility(ayudaVisible ? View.VISIBLE : View.GONE);
        ordenar(modulos);
    }

    private void ordenar(String modulos) {
        contenedorModulos.removeAllViews();
        for (String id : MostradorModulos.lista(modulos)) {
            View card = null;
            if (MostradorModulos.QR.equals(id)) card = cardQr;
            else if (MostradorModulos.LLAVE.equals(id)) card = cardLlave;
            else if (MostradorModulos.CUENTA.equals(id)) card = cardCuenta;
            else if (MostradorModulos.AYUDA.equals(id)) card = cardAyuda;
            if (card != null) contenedorModulos.addView(card);
        }
        txtSinModulos.setVisibility(modulos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void completarDatos() {
        String modulos = modulos();
        List<String> nombres = new ArrayList<>();
        List<Runnable> acciones = new ArrayList<>();
        if (modulos.contains(MostradorModulos.QR) && Imagen.cargar(rutaQr, 200) == null) {
            nombres.add("Configurar QR");
            acciones.add(this::ofrecerConfigurarQr);
        }
        if (modulos.contains(MostradorModulos.LLAVE)
                && bd.ajuste("pago_bre_b", "").trim().isEmpty()) {
            nombres.add("Llave Bre-B");
            acciones.add(() -> editarDato("pago_bre_b", "Llave Bre-B", "@EJEMPLO2567"));
        }
        if (modulos.contains(MostradorModulos.CUENTA)) {
            if (bd.ajuste("pago_cuenta", "").trim().isEmpty()) {
                nombres.add("Número de cuenta");
                acciones.add(() -> editarDato("pago_cuenta", "Número de cuenta",
                        "#4004EJEMPLO2145"));
            }
            if (bd.ajuste("pago_banco", "").trim().isEmpty()) {
                nombres.add("Banco o identificador");
                acciones.add(() -> editarDato("pago_banco", "Banco o identificador",
                        "Bancolombia · 0001"));
            }
            if (bd.ajuste("pago_titular", "").trim().isEmpty()) {
                nombres.add("Titular (opcional)");
                acciones.add(() -> editarDato("pago_titular", "Titular", "Papelería Somor"));
            }
        }
        if (nombres.isEmpty()) {
            cargar();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Completar datos")
                .setItems(nombres.toArray(new String[0]), (d, w) -> acciones.get(w).run())
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void ofrecerConfigurarQr() {
        new AlertDialog.Builder(this)
                .setTitle("Configurar QR")
                .setMessage("La app limpia la foto y la guarda. Se configura una sola vez desde "
                        + "El mostrador; para cambiarlo después, entra a Ajustes → Datos para "
                        + "recibir pagos.")
                .setPositiveButton("Tomar foto", (d, w) -> tomarFoto(PEDIR_QR_CAMARA))
                .setNeutralButton("Elegir imagen", (d, w) -> elegirQr())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void elegirQr() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        try {
            startActivityForResult(i, PEDIR_QR_IMAGEN);
        } catch (Exception e) {
            aviso("No hay una aplicación para elegir imágenes.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PEDIR_QR_CAMARA) {
            String ruta = resultadoFoto(resultCode, data);
            if (ruta != null) procesarQrOnboarding(ruta);
            return;
        }
        if (requestCode == PEDIR_QR_IMAGEN) {
            if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
            String ruta = resultadoArchivo(resultCode, data);
            if (ruta == null) return;
            if (!Archivos.esImagen(ruta, mimeArchivoPendiente)) {
                Archivos.borrar(ruta);
                aviso("Elige una imagen del código QR.");
                return;
            }
            procesarQrOnboarding(ruta);
        }
    }

    private void procesarQrOnboarding(String origen) {
        if (origen == null || origen.isEmpty()) return;
        ProgressDialog progreso = new ProgressDialog(this);
        progreso.setMessage("Limpiando el QR…");
        progreso.setCancelable(false);
        progreso.show();
        btnCompletarDatos.setEnabled(false);
        new Thread(() -> {
            Bitmap limpio = QrPago.limpiar(origen);
            String rutaFinal = null;
            if (limpio != null) {
                File temp = Archivos.nuevoArchivoTemporal(this, "qr", "png");
                try (FileOutputStream out = new FileOutputStream(temp)) {
                    limpio.compress(Bitmap.CompressFormat.PNG, 100, out);
                    String permanente = Archivos.moverAPermanente(this, temp.getAbsolutePath());
                    if (!permanente.isEmpty()) {
                        bd.guardarAjuste("pago_qr_foto", permanente);
                        rutaFinal = permanente;
                    }
                } catch (IOException e) {
                    Archivos.borrar(temp.getAbsolutePath());
                }
            }
            Archivos.borrar(origen);
            final String guardada = rutaFinal;
            runOnUiThread(() -> {
                progreso.dismiss();
                if (isFinishing()) return;
                if (guardada == null) {
                    btnCompletarDatos.setEnabled(true);
                    aviso("No se pudo leer el QR. Acércalo, evita reflejos y muestra los cuatro bordes.");
                    return;
                }
                cargar();
                aviso("QR configurado. Ya puedes cambiarlo en Ajustes.");
            });
        }).start();
    }

    private void editarDato(String clave, String titulo, String ejemplo) {
        EditText campo = new EditText(this);
        campo.setHint(ejemplo);
        campo.setTextSize(18);
        campo.setSingleLine(true);
        campo.setBackgroundResource(R.drawable.fondo_campo);
        int p = Ui.dp(this, 14);
        campo.setPadding(p, p, p, p);

        LinearLayout caja = new LinearLayout(this);
        caja.setOrientation(LinearLayout.VERTICAL);
        caja.setPadding(p, p, p, 0);
        TextView aviso = Ui.texto(this,
                "El botón Completar datos desaparecerá cuando ya no queden datos de ejemplo. "
                        + "Si necesitas cambiarlo después, ve a Ajustes → Datos para recibir pagos.",
                15, getColor(R.color.texto_secundario), false);
        aviso.setPadding(0, 0, 0, p);
        aviso.setLineSpacing(0f, 1.1f);
        caja.addView(aviso);
        caja.addView(campo);

        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Editar " + titulo)
                .setView(caja)
                .setPositiveButton("Confirmar", null)
                .setNegativeButton("Cerrar", null)
                .create();
        d.setOnShowListener(x -> {
            agrandarDialogo(d);
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String valor = campo.getText() == null ? "" : campo.getText().toString().trim();
                if (valor.isEmpty()) {
                    campo.setError("Escribe el dato o cierra sin cambios.");
                    campo.requestFocus();
                    return;
                }
                AlertDialog confirmar = new AlertDialog.Builder(this)
                        .setTitle("Confirmar dato")
                        .setMessage("¿Confirmas que " + titulo + " es “" + valor + "”?\n\n"
                                + "Después de confirmar, el botón Completar datos desaparecerá "
                                + "cuando ya no queden datos de ejemplo. Podrás cambiar el dato "
                                + "en Ajustes → Datos para recibir pagos.")
                        .setPositiveButton("Confirmar", (cd, w) -> {
                            bd.guardarAjuste(clave, valor);
                            d.dismiss();
                            cargar();
                            aviso("Dato guardado.");
                        })
                        .setNegativeButton("Volver", null)
                        .create();
                confirmar.setOnShowListener(cd -> agrandarDialogo(confirmar));
                confirmar.show();
            });
        });
        d.show();
    }

    private Bitmap qr(String contenido) {
        try {
            Map<EncodeHintType, Object> pistas = new EnumMap<>(EncodeHintType.class);
            pistas.put(EncodeHintType.MARGIN, 2);
            pistas.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matriz = new MultiFormatWriter().encode(
                    contenido, BarcodeFormat.QR_CODE, 720, 720, pistas);
            int ancho = matriz.getWidth();
            int alto = matriz.getHeight();
            int[] pixeles = new int[ancho * alto];
            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {
                    pixeles[y * ancho + x] = matriz.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            return Bitmap.createBitmap(pixeles, ancho, alto, Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            return null;
        }
    }
}
