package com.polypodiozoa.minegocio.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.util.MostradorModulos;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Elige los módulos visibles del mostrador y su orden. */
public class MostradorConfigActivity extends BaseActivity {

    private final List<String> orden = new ArrayList<>();
    private final Set<String> visibles = new HashSet<>();
    private LinearLayout contenedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrador_config);
        configurarBarra("Personalizar Mostrador");
        contenedor = findViewById(R.id.contenedorOpciones);

        String guardado = bd.ajuste("pago_mostrador_modulos", MostradorModulos.ORDEN_DEFECTO);
        for (String id : MostradorModulos.lista(guardado)) {
            if (!orden.contains(id)) {
                orden.add(id);
                visibles.add(id);
            }
        }
        for (String id : MostradorModulos.todos()) {
            if (!orden.contains(id)) orden.add(id);
        }

        construir();
        findViewById(R.id.btnGuardarMostrador).setOnClickListener(v -> guardar());
    }

    private void construir() {
        contenedor.removeAllViews();
        for (int i = 0; i < orden.size(); i++) {
            final int indice = i;
            final String id = orden.get(i);

            LinearLayout tarjeta = new LinearLayout(this);
            tarjeta.setOrientation(LinearLayout.VERTICAL);
            tarjeta.setBackgroundResource(R.drawable.fondo_tarjeta);
            tarjeta.setPadding(Ui.dp(this, 16), Ui.dp(this, 14),
                    Ui.dp(this, 16), Ui.dp(this, 14));
            LinearLayout.LayoutParams lpTarjeta = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lpTarjeta.bottomMargin = Ui.dp(this, 14);
            tarjeta.setLayoutParams(lpTarjeta);

            LinearLayout cabecera = new LinearLayout(this);
            cabecera.setOrientation(LinearLayout.HORIZONTAL);
            cabecera.setGravity(Gravity.CENTER_VERTICAL);

            TextView titulo = Ui.texto(this, MostradorModulos.etiqueta(id),
                    20, getColor(R.color.texto), true);
            titulo.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            cabecera.addView(titulo);

            Switch mostrar = new Switch(this);
            mostrar.setText("Mostrar");
            mostrar.setTextSize(14);
            mostrar.setTextColor(getColor(R.color.texto_secundario));
            mostrar.setChecked(visibles.contains(id));
            mostrar.setMinHeight(Ui.dp(this, 48));
            mostrar.setContentDescription("Mostrar " + MostradorModulos.etiqueta(id)
                    + " en Mostrador");
            mostrar.setOnCheckedChangeListener((b, checked) -> {
                if (checked) visibles.add(id);
                else visibles.remove(id);
            });
            cabecera.addView(mostrar);
            tarjeta.addView(cabecera);

            TextView descripcion = Ui.texto(this, descripcion(id),
                    15, getColor(R.color.texto_secundario), false);
            descripcion.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 10));
            descripcion.setLineSpacing(0f, 1.1f);
            tarjeta.addView(descripcion);

            LinearLayout acciones = new LinearLayout(this);
            acciones.setOrientation(LinearLayout.HORIZONTAL);
            Button subir = boton("Subir", "Subir " + MostradorModulos.etiqueta(id));
            subir.setEnabled(i > 0);
            subir.setOnClickListener(v -> mover(indice, -1));
            LinearLayout.LayoutParams lpSubir = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lpSubir.rightMargin = Ui.dp(this, 6);
            acciones.addView(subir, lpSubir);

            Button bajar = boton("Bajar", "Bajar " + MostradorModulos.etiqueta(id));
            bajar.setEnabled(i < orden.size() - 1);
            bajar.setOnClickListener(v -> mover(indice, 1));
            LinearLayout.LayoutParams lpBajar = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lpBajar.leftMargin = Ui.dp(this, 6);
            acciones.addView(bajar, lpBajar);
            tarjeta.addView(acciones);

            contenedor.addView(tarjeta);
        }
    }

    private String descripcion(String id) {
        if (MostradorModulos.QR.equals(id)) {
            return "Muestra el código QR de pago.";
        }
        if (MostradorModulos.LLAVE.equals(id)) {
            return "Muestra la llave Bre-B para dictarla.";
        }
        if (MostradorModulos.CUENTA.equals(id)) {
            return "Muestra el número de cuenta y, si los configuras, banco y titular.";
        }
        return "Muestra los pasos para pagar.";
    }

    private Button boton(String texto, String descripcion) {
        Button b = new Button(this);
        b.setText(texto);
        b.setTextSize(16);
        b.setMinimumHeight(Ui.dp(this, 50));
        b.setBackgroundResource(R.drawable.boton_secundario);
        b.setTextColor(getColor(R.color.primario));
        b.setAllCaps(false);
        b.setContentDescription(descripcion);
        return b;
    }

    private void mover(int indice, int direccion) {
        int destino = indice + direccion;
        if (destino < 0 || destino >= orden.size()) return;
        String movido = orden.remove(indice);
        orden.add(destino, movido);
        construir();
    }

    private void guardar() {
        StringBuilder s = new StringBuilder();
        for (String id : orden) {
            if (!visibles.contains(id)) continue;
            if (s.length() > 0) s.append(',');
            s.append(id);
        }
        bd.guardarAjuste("pago_mostrador_modulos", s.toString());
        aviso("El mostrador se actualizó.");
        finish();
    }
}
