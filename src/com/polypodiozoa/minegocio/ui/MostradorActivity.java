package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.util.Imagen;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Datos para pagar en el mostrador: QR, llave Bre-B, cuenta y métodos. */
public class MostradorActivity extends BaseActivity {

    private static final String LLAVE_EJEMPLO = "11111111-2222-3333-aaaa-a1b2c3d4e5f6";
    private static final String CUENTA_EJEMPLO = "1111 2222 3333 4444";
    private static final String BANCO_EJEMPLO = "Banco de ejemplo · aaaaaa";
    private static final String TITULAR_EJEMPLO = "Titular de ejemplo";
    private static final String METODOS_EJEMPLO = "Bre-B, Nequi, Daviplata, Transferencia bancaria";

    private ImageView imgQr;
    private TextView txtQrTitulo, txtQrAyuda, txtLlave, txtCuenta, txtBanco, txtTitular;
    private TextView txtEjemploLlave, txtEjemploCuenta, txtEjemploBanco, txtEjemploTitular,
            txtEjemploMetodos;
    private LinearLayout listaMetodos;
    private String rutaQr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrador);
        configurarBarra("El mostrador");
        imgQr = findViewById(R.id.imgQr);
        txtQrTitulo = findViewById(R.id.txtQrTitulo);
        txtQrAyuda = findViewById(R.id.txtQrAyuda);
        txtEjemploLlave = findViewById(R.id.txtEjemploLlave);
        txtEjemploCuenta = findViewById(R.id.txtEjemploCuenta);
        txtEjemploBanco = findViewById(R.id.txtEjemploBanco);
        txtEjemploTitular = findViewById(R.id.txtEjemploTitular);
        txtEjemploMetodos = findViewById(R.id.txtEjemploMetodos);
        txtLlave = findViewById(R.id.txtLlave);
        txtCuenta = findViewById(R.id.txtCuenta);
        txtBanco = findViewById(R.id.txtBanco);
        txtTitular = findViewById(R.id.txtTitular);
        listaMetodos = findViewById(R.id.listaMetodos);
        cargar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        String llave = bd.ajuste("pago_bre_b", "").trim();
        String cuenta = bd.ajuste("pago_cuenta", "").trim();
        String banco = bd.ajuste("pago_banco", "").trim();
        String titular = bd.ajuste("pago_titular", "").trim();
        String metodos = bd.ajuste("pago_metodos", "").trim();
        rutaQr = bd.ajuste("pago_qr_foto", "").trim();

        boolean llaveVacia = llave.isEmpty();
        boolean cuentaVacia = cuenta.isEmpty();
        boolean bancoVacio = banco.isEmpty();
        boolean metodosVacios = metodos.isEmpty();
        boolean titularVacio = titular.isEmpty();
        boolean ejemplo = llaveVacia || cuentaVacia || bancoVacio || metodosVacios;
        if (llaveVacia) llave = LLAVE_EJEMPLO;
        if (cuentaVacia) cuenta = CUENTA_EJEMPLO;
        if (bancoVacio) banco = BANCO_EJEMPLO;
        if (titularVacio) titular = ejemplo ? TITULAR_EJEMPLO : "";
        if (metodosVacios) metodos = METODOS_EJEMPLO;

        txtLlave.setText(llave);
        txtCuenta.setText(cuenta);
        txtBanco.setText("Banco: " + banco);
        txtTitular.setText(titular.isEmpty() ? "" : "Titular: " + titular);
        txtTitular.setVisibility(titular.isEmpty() ? View.GONE : View.VISIBLE);
        txtEjemploLlave.setVisibility(llaveVacia ? View.VISIBLE : View.GONE);
        txtEjemploCuenta.setVisibility(cuentaVacia ? View.VISIBLE : View.GONE);
        txtEjemploBanco.setVisibility(bancoVacio ? View.VISIBLE : View.GONE);
        txtEjemploTitular.setVisibility(titularVacio && ejemplo ? View.VISIBLE : View.GONE);
        txtEjemploMetodos.setVisibility(metodosVacios ? View.VISIBLE : View.GONE);

        Bitmap foto = Imagen.cargar(rutaQr, 1200);
        if (foto != null) {
            imgQr.setImageBitmap(foto);
            imgQr.setScaleType(ImageView.ScaleType.FIT_CENTER);
            txtQrTitulo.setText("Escanea el QR de Bre-B");
            txtQrAyuda.setVisibility(View.VISIBLE);
            imgQr.setClickable(true);
            imgQr.setFocusable(true);
            imgQr.setContentDescription("Código QR de Bre-B. Toca para verlo en grande.");
            imgQr.setOnClickListener(v -> {
                Intent i = new Intent(this, FotoActivity.class);
                i.putExtra("ruta", rutaQr);
                startActivity(i);
            });
        } else {
            String contenido = llaveVacia ? LLAVE_EJEMPLO : llave;
            Bitmap generado = qr(contenido);
            if (generado != null) imgQr.setImageBitmap(generado);
            else imgQr.setImageResource(R.drawable.ic_qr);
            imgQr.setScaleType(ImageView.ScaleType.FIT_CENTER);
            txtQrTitulo.setText(llaveVacia ? "QR de ejemplo" : "Escanea el QR de Bre-B");
            txtQrAyuda.setVisibility(View.GONE);
            imgQr.setClickable(false);
            imgQr.setFocusable(false);
            imgQr.setContentDescription(llaveVacia
                    ? "Código QR de ejemplo" : "Código QR de Bre-B");
            imgQr.setOnClickListener(null);
        }

        listaMetodos.removeAllViews();
        for (String m : metodos.split(",")) {
            String nombre = m.trim();
            if (nombre.isEmpty()) continue;
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.CENTER_VERTICAL);
            fila.setMinimumHeight(Ui.dp(this, 48));
            ImageView check = new ImageView(this);
            check.setImageResource(R.drawable.ic_check);
            check.setColorFilter(getColor(R.color.primario));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    Ui.dp(this, 26), Ui.dp(this, 26));
            lp.setMarginEnd(Ui.dp(this, 10));
            check.setLayoutParams(lp);
            check.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            fila.addView(check);
            TextView texto = Ui.texto(this, nombre, 17, getColor(R.color.texto), true);
            texto.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            fila.addView(texto);
            fila.setContentDescription("Método disponible: " + nombre);
            listaMetodos.addView(fila);
        }
        listaMetodos.setContentDescription(metodosVacios
                ? "Métodos de ejemplo" : "Métodos de pago disponibles");
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
