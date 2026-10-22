package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;

import java.util.ArrayList;
import java.util.List;

public class GastosActivity extends BaseActivity {

    private EditText etBuscar;
    private TextView txtResumen;
    private AdaptadorFilas adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_personas);
        configurarBarra("Gastos");
        etBuscar = findViewById(R.id.etBuscar);
        etBuscar.setHint("Buscar gasto");
        etBuscar.setContentDescription("Buscar gasto por descripción o categoría");
        txtResumen = findViewById(R.id.txtResumen);
        ListView lista = findViewById(R.id.lista);
        TextView vacio = findViewById(R.id.txtVacio);
        adaptador = new AdaptadorFilas(this);
        adaptador.setOnFilaClick(f -> {
            Modelo.Gasto g = (Modelo.Gasto) f.dato;
            Intent i = new Intent(this, GastoActivity.class);
            i.putExtra("id", g.id);
            startActivity(i);
        });
        lista.setAdapter(adaptador);
        lista.setEmptyView(vacio);
        ((Button) findViewById(R.id.btnNuevo)).setText("Nuevo gasto");
        ((Button) findViewById(R.id.btnNuevo)).setContentDescription("Nuevo gasto");
        findViewById(R.id.btnNuevo).setOnClickListener(v -> abrir(GastoActivity.class));
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { cargar(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        List<Modelo.Gasto> gastos = bd.gastos(texto(etBuscar));
        List<AdaptadorFilas.Fila> filas = new ArrayList<>();
        long total = 0;
        for (Modelo.Gasto g : gastos) {
            total += g.monto;
            AdaptadorFilas.Fila f = new AdaptadorFilas.Fila();
            f.id = g.id;
            f.titulo = g.descripcion;
            String sub = (g.categoria.isEmpty() ? "Sin categoría" : g.categoria)
                    + " · " + Fechas.fecha(g.fecha) + " · " + g.formaPago
                    + (g.usuarioNombre.isEmpty() ? "" : " · " + g.usuarioNombre);
            f.subtitulo = sub;
            f.valor = Formatos.pesos(g.monto);
            f.iconoDefecto = R.drawable.ic_dinero;
            f.dato = g;
            f.descripcion = g.descripcion + ", " + sub + ", " + Formatos.pesosHablado(g.monto)
                    + (g.usuarioNombre.isEmpty() ? "" : ", registró " + g.usuarioNombre)
                    + ". Toca para editar.";
            filas.add(f);
        }
        adaptador.setFilas(filas);
        txtResumen.setText(gastos.size() + (gastos.size() == 1 ? " gasto" : " gastos")
                + (gastos.isEmpty() ? "" : " · " + Formatos.pesos(total) + " en gastos"));
        TextView vacio = findViewById(R.id.txtVacio);
        if (filas.isEmpty()) {
            vacio.setText(texto(etBuscar).isEmpty()
                    ? "Todavía no has registrado gastos.\n\nAgrega el primero con el botón de abajo."
                    : "No encontramos gastos con esa búsqueda.");
        }
    }
}
