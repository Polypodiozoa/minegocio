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
import com.polypodiozoa.minegocio.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class VentasActivity extends BaseActivity {

    private EditText etBuscar;
    private TextView txtResumen;
    private AdaptadorFilas adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_personas);
        configurarBarra("Ventas");
        etBuscar = findViewById(R.id.etBuscar);
        etBuscar.setHint("Buscar venta");
        etBuscar.setContentDescription("Buscar venta por cliente, nota o número");
        txtResumen = findViewById(R.id.txtResumen);
        ListView lista = findViewById(R.id.lista);
        TextView vacio = findViewById(R.id.txtVacio);
        adaptador = new AdaptadorFilas(this);
        adaptador.setOnFilaClick(f -> {
            Modelo.Venta v = (Modelo.Venta) f.dato;
            Intent i = new Intent(this, VentaDetalleActivity.class);
            i.putExtra("id", v.id);
            startActivity(i);
        });
        lista.setAdapter(adaptador);
        lista.setEmptyView(vacio);
        ((Button) findViewById(R.id.btnNuevo)).setText("Nueva venta");
        ((Button) findViewById(R.id.btnNuevo)).setContentDescription("Nueva venta");
        findViewById(R.id.btnNuevo).setOnClickListener(v -> abrir(VentaActivity.class));
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
        List<Modelo.Venta> ventas = bd.ventas(texto(etBuscar));
        List<AdaptadorFilas.Fila> filas = new ArrayList<>();
        long total = 0;
        for (Modelo.Venta v : ventas) {
            if (!v.anulada) total += v.total;
            AdaptadorFilas.Fila f = new AdaptadorFilas.Fila();
            f.id = v.id;
            f.titulo = "Venta #" + v.id + (v.clienteNombre.isEmpty() ? "" : " · " + v.clienteNombre);
            String sub = Fechas.fechaHora(v.fecha) + " · " + v.formaPago
                    + (v.usuarioNombre.isEmpty() ? "" : " · " + v.usuarioNombre);
            if (v.numItems > 0) sub += " · " + v.numItems + (v.numItems == 1 ? " ítem" : " ítems");
            f.subtitulo = sub;
            f.valor = Formatos.pesos(v.total);
            f.estado = Ui.estadoVenta(v);
            f.iconoDefecto = R.drawable.ic_recibo;
            f.dato = v;
            f.descripcion = "Venta " + v.id
                    + (v.clienteNombre.isEmpty() ? ", consumidor final" : ", " + v.clienteNombre)
                    + ", " + Fechas.fechaHora(v.fecha) + ", total "
                    + Formatos.pesosHablado(v.total) + ", " + Ui.estadoVenta(v)
                    + (v.usuarioNombre.isEmpty() ? "" : ", atendió " + v.usuarioNombre)
                    + ". Toca para ver el detalle.";
            filas.add(f);
        }
        adaptador.setFilas(filas);
        txtResumen.setText(ventas.size() + (ventas.size() == 1 ? " venta" : " ventas")
                + (ventas.isEmpty() ? "" : " · " + Formatos.pesos(total) + " en ventas"));
        TextView vacio = findViewById(R.id.txtVacio);
        if (filas.isEmpty()) {
            vacio.setText(texto(etBuscar).isEmpty()
                    ? "Todavía no has registrado ventas.\n\nRegistra la primera con el botón de abajo."
                    : "No encontramos ventas con esa búsqueda.");
        }
    }
}
