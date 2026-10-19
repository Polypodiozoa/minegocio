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
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class ClientesActivity extends BaseActivity {

    private EditText etBuscar;
    private TextView txtResumen;
    private AdaptadorFilas adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_personas);
        configurarBarra("Clientes");
        etBuscar = findViewById(R.id.etBuscar);
        etBuscar.setHint("Buscar cliente");
        etBuscar.setContentDescription("Buscar cliente por nombre o teléfono");
        txtResumen = findViewById(R.id.txtResumen);
        ListView lista = findViewById(R.id.lista);
        TextView vacio = findViewById(R.id.txtVacio);
        adaptador = new AdaptadorFilas(this);
        adaptador.setOnFilaClick(f -> {
            Modelo.Cliente c = (Modelo.Cliente) f.dato;
            Intent i = new Intent(this, ClienteDetalleActivity.class);
            i.putExtra("id", c.id);
            startActivity(i);
        });
        lista.setAdapter(adaptador);
        lista.setEmptyView(vacio);
        ((Button) findViewById(R.id.btnNuevo)).setText("Nuevo cliente");
        ((Button) findViewById(R.id.btnNuevo)).setContentDescription("Nuevo cliente");
        findViewById(R.id.btnNuevo).setOnClickListener(v -> abrir(ClienteActivity.class));
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
        List<Modelo.Cliente> clientes = bd.clientes(texto(etBuscar));
        List<AdaptadorFilas.Fila> filas = new ArrayList<>();
        int conSaldo = 0;
        for (Modelo.Cliente c : clientes) {
            if (c.saldo != 0) conSaldo++;
            AdaptadorFilas.Fila f = new AdaptadorFilas.Fila();
            f.id = c.id;
            f.titulo = c.nombre;
            StringBuilder sub = new StringBuilder();
            if (!c.telefono.isEmpty()) sub.append(c.telefono);
            if (!c.correo.isEmpty()) {
                if (sub.length() > 0) sub.append(" · ");
                sub.append(c.correo);
            }
            if (sub.length() == 0) sub.append("Sin teléfono registrado");
            f.subtitulo = sub.toString();
            f.valor = c.saldo == 0 ? "" : Formatos.pesos(Math.abs(c.saldo));
            f.estado = c.saldo > 0 ? "Debe" : c.saldo < 0 ? "A favor" : "Al día";
            f.foto = c.foto;
            f.iconoDefecto = R.drawable.ic_persona;
            f.dato = c;
            f.descripcion = c.nombre + ", " + f.subtitulo + ", "
                    + (c.saldo > 0 ? "debe " + Formatos.pesosHablado(c.saldo)
                    : c.saldo < 0 ? "saldo a favor de " + Formatos.pesosHablado(-c.saldo)
                    : "al día")
                    + ". Toca para ver el detalle.";
            filas.add(f);
        }
        adaptador.setFilas(filas);
        txtResumen.setText(clientes.size() + (clientes.size() == 1 ? " cliente" : " clientes")
                + (conSaldo > 0 ? " · " + conSaldo + " con saldo pendiente" : ""));
        TextView vacio = findViewById(R.id.txtVacio);
        if (filas.isEmpty()) {
            vacio.setText(texto(etBuscar).isEmpty()
                    ? "Todavía no tienes clientes.\n\nAgrega el primero con el botón de abajo."
                    : "No encontramos clientes que coincidan con esa búsqueda.");
        }
    }
}
