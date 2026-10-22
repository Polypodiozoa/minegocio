package com.polypodiozoa.minegocio.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

    private static final String LLAVE_EJEMPLO = "@EJEMPLO2567";
    private static final String CUENTA_EJEMPLO = "#4004EJEMPLO2145";
    private static final String BANCO_EJEMPLO = "Banco de ejemplo";
    private static final String TITULAR_EJEMPLO = "Titular de ejemplo";

    private ImageView imgQr;
    private TextView txtQrTitulo, txtQrAyuda, txtLlave, txtCuenta, txtBanco, txtTitular;
    private TextView txtAviso, btnEditarLlave, btnEditarCuenta, btnEditarBanco,
            btnEditarTitular;
    private String rutaQr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrador);
        configurarBarra("El mostrador");
        imgQr = findViewById(R.id.imgQr);
        txtQrTitulo = findViewById(R.id.txtQrTitulo);
        txtQrAyuda = findViewById(R.id.txtQrAyuda);
        txtAviso = findViewById(R.id.txtAvisoEjemplo);
        btnEditarLlave = findViewById(R.id.btnEditarLlave);
        btnEditarCuenta = findViewById(R.id.btnEditarCuenta);
        btnEditarBanco = findViewById(R.id.btnEditarBanco);
        btnEditarTitular = findViewById(R.id.btnEditarTitular);
        txtLlave = findViewById(R.id.txtLlave);
        txtCuenta = findViewById(R.id.txtCuenta);
        txtBanco = findViewById(R.id.txtBanco);
        txtTitular = findViewById(R.id.txtTitular);
        btnEditarLlave.setOnClickListener(v -> editarDato(
                "pago_bre_b", "Llave Bre-B", "@EJEMPLO2567"));
        btnEditarCuenta.setOnClickListener(v -> editarDato(
                "pago_cuenta", "Número de cuenta", "#4004EJEMPLO2145"));
        btnEditarBanco.setOnClickListener(v -> editarDato(
                "pago_banco", "Banco o identificador", "Bancolombia · 0001"));
        btnEditarTitular.setOnClickListener(v -> editarDato(
                "pago_titular", "Titular", "Papelería Somor"));
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
        btnEditarLlave.setVisibility(llaveVacia ? View.VISIBLE : View.GONE);
        btnEditarCuenta.setVisibility(cuentaVacia ? View.VISIBLE : View.GONE);
        btnEditarBanco.setVisibility(bancoVacio ? View.VISIBLE : View.GONE);
        btnEditarTitular.setVisibility(titularVacio && ejemplo ? View.VISIBLE : View.GONE);
        txtAviso.setVisibility(ejemplo ? View.VISIBLE : View.GONE);
        btnEditarLlave.setContentDescription("Editar llave Bre-B. El botón desaparece al confirmar.");
        btnEditarCuenta.setContentDescription("Editar número de cuenta. El botón desaparece al confirmar.");
        btnEditarBanco.setContentDescription("Editar banco. El botón desaparece al confirmar.");
        btnEditarTitular.setContentDescription("Editar titular. El botón desaparece al confirmar.");

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
                i.putExtra("titulo", "QR de Bre-B");
                i.putExtra("descripcion", "Código QR de Bre-B");
                startActivity(i);
            });
        } else {
            String contenido = llaveVacia ? LLAVE_EJEMPLO : llave;
            Bitmap generado = qr(contenido);
            if (generado != null) imgQr.setImageBitmap(generado);
            else imgQr.setImageResource(R.drawable.ic_qr);
            imgQr.setScaleType(ImageView.ScaleType.FIT_CENTER);
            txtQrTitulo.setText(llaveVacia ? "QR de Bre-B de ejemplo" : "Escanea el QR de Bre-B");
            txtQrAyuda.setVisibility(View.GONE);
            imgQr.setClickable(false);
            imgQr.setFocusable(false);
            imgQr.setContentDescription(llaveVacia
                    ? "Código QR de Bre-B de ejemplo" : "Código QR de Bre-B");
            imgQr.setOnClickListener(null);
        }
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
                "Este botón Editar desaparecerá cuando confirmes el dato. "
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
                                + "Después de confirmar, este botón Editar desaparecerá de El mostrador. "
                                + "Podrás cambiarlo en Ajustes → Datos para recibir pagos.")
                        .setPositiveButton("Confirmar", (cd, w) -> {
                            bd.guardarAjuste(clave, valor);
                            d.dismiss();
                            cargar();
                            aviso("Dato guardado. El botón Editar desapareció.");
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
