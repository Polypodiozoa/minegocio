package com.polypodiozoa.minegocio.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.util.MostradorModulos;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Elige los módulos visibles de El mostrador y su orden. */
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

            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.CENTER_VERTICAL);
            fila.setMinimumHeight(Ui.dp(this, 56));
            fila.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));

            CheckBox cb = new CheckBox(this);
            cb.setText(MostradorModulos.etiqueta(id));
            cb.setTextColor(getColor(R.color.texto));
            cb.setTextSize(17);
            cb.setChecked(visibles.contains(id));
            cb.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            cb.setOnCheckedChangeListener((b, checked) -> {
                if (checked) visibles.add(id);
                else visibles.remove(id);
            });
            fila.addView(cb);

            Button subir = boton("Subir");
            subir.setEnabled(i > 0);
            subir.setOnClickListener(v -> mover(indice, -1));
            fila.addView(subir);

            Button bajar = boton("Bajar");
            bajar.setEnabled(i < orden.size() - 1);
            bajar.setOnClickListener(v -> mover(indice, 1));
            fila.addView(bajar);

            contenedor.addView(fila);
        }
    }

    private Button boton(String texto) {
        Button b = new Button(this);
        b.setText(texto);
        b.setTextSize(14);
        b.setMinWidth(Ui.dp(this, 68));
        b.setMinimumHeight(Ui.dp(this, 48));
        b.setBackgroundResource(R.drawable.boton_secundario);
        b.setTextColor(getColor(R.color.primario));
        b.setAllCaps(false);
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
