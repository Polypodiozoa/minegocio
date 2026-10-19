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
import com.polypodiozoa.minegocio.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class ProveedoresActivity extends BaseActivity {

    private EditText etBuscar;
    private TextView txtResumen;
    private AdaptadorFilas adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_personas);
        configurarBarra("Proveedores");
        etBuscar = findViewById(R.id.etBuscar);
        etBuscar.setHint("Buscar proveedor");
        etBuscar.setContentDescription("Buscar proveedor por nombre o teléfono");
        txtResumen = findViewById(R.id.txtResumen);
        ListView lista = findViewById(R.id.lista);
        TextView vacio = findViewById(R.id.txtVacio);
        adaptador = new AdaptadorFilas(this);
        adaptador.setOnFilaClick(f -> {
            Modelo.Proveedor p = (Modelo.Proveedor) f.dato;
            Intent i = new Intent(this, ProveedorDetalleActivity.class);
            i.putExtra("id", p.id);
            startActivity(i);
        });
        lista.setAdapter(adaptador);
        lista.setEmptyView(vacio);
        ((Button) findViewById(R.id.btnNuevo)).setText("Nuevo proveedor");
        ((Button) findViewById(R.id.btnNuevo)).setContentDescription("Nuevo proveedor");
        findViewById(R.id.btnNuevo).setOnClickListener(v -> abrir(ProveedorActivity.class));
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
        List<Modelo.Proveedor> proveedores = bd.proveedores(texto(etBuscar));
        List<AdaptadorFilas.Fila> filas = new ArrayList<>();
        for (Modelo.Proveedor p : proveedores) {
            AdaptadorFilas.Fila f = new AdaptadorFilas.Fila();
            f.id = p.id;
            f.titulo = p.nombre;
            StringBuilder sub = new StringBuilder();
            if (!p.contacto.isEmpty()) sub.append(p.contacto);
            if (!p.telefono.isEmpty()) {
                if (sub.length() > 0) sub.append(" · ");
                sub.append(p.telefono);
            }
            if (sub.length() == 0) sub.append("Sin datos de contacto");
            f.subtitulo = sub.toString();
            f.foto = p.foto;
            f.iconoDefecto = R.drawable.ic_tienda;
            f.dato = p;
            f.descripcion = p.nombre + ", " + f.subtitulo + ". Toca para ver el detalle.";
            filas.add(f);
        }
        adaptador.setFilas(filas);
        txtResumen.setText(proveedores.size() + (proveedores.size() == 1
                ? " proveedor" : " proveedores"));
        TextView vacio = findViewById(R.id.txtVacio);
        if (filas.isEmpty()) {
            vacio.setText(texto(etBuscar).isEmpty()
                    ? "Todavía no tienes proveedores.\n\nAgrega el primero con el botón de abajo."
                    : "No encontramos proveedores que coincidan con esa búsqueda.");
        }
    }
}
