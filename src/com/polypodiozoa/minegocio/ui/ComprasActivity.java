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

public class ComprasActivity extends BaseActivity {

    private EditText etBuscar;
    private TextView txtResumen;
    private AdaptadorFilas adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_personas);
        configurarBarra("Compras");
        etBuscar = findViewById(R.id.etBuscar);
        etBuscar.setHint("Buscar compra");
        etBuscar.setContentDescription("Buscar compra por proveedor, nota o número");
        txtResumen = findViewById(R.id.txtResumen);
        ListView lista = findViewById(R.id.lista);
        TextView vacio = findViewById(R.id.txtVacio);
        adaptador = new AdaptadorFilas(this);
        adaptador.setOnFilaClick(f -> {
            Modelo.Compra c = (Modelo.Compra) f.dato;
            Intent i = new Intent(this, CompraDetalleActivity.class);
            i.putExtra("id", c.id);
            startActivity(i);
        });
        lista.setAdapter(adaptador);
        lista.setEmptyView(vacio);
        ((Button) findViewById(R.id.btnNuevo)).setText("Nueva compra");
        ((Button) findViewById(R.id.btnNuevo)).setContentDescription("Nueva compra");
        findViewById(R.id.btnNuevo).setOnClickListener(v -> abrir(CompraActivity.class));
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
        List<Modelo.Compra> compras = bd.compras(texto(etBuscar));
        List<AdaptadorFilas.Fila> filas = new ArrayList<>();
        long total = 0;
        for (Modelo.Compra c : compras) {
            if (!c.anulada) total += c.total;
            AdaptadorFilas.Fila f = new AdaptadorFilas.Fila();
            f.id = c.id;
            f.titulo = "Compra #" + c.id + (c.proveedorNombre.isEmpty() ? "" : " · " + c.proveedorNombre);
            String sub = Fechas.fechaHora(c.fecha) + " · " + c.formaPago
                    + (c.usuarioNombre.isEmpty() ? "" : " · " + c.usuarioNombre);
            if (c.numItems > 0) sub += " · " + c.numItems + (c.numItems == 1 ? " ítem" : " ítems");
            f.subtitulo = sub;
            f.valor = Formatos.pesos(c.total);
            f.estado = Ui.estadoCompra(c);
            f.iconoDefecto = R.drawable.ic_carrito;
            f.dato = c;
            f.descripcion = "Compra " + c.id
                    + (c.proveedorNombre.isEmpty() ? ", sin proveedor" : ", " + c.proveedorNombre)
                    + ", " + Fechas.fechaHora(c.fecha) + ", total "
                    + Formatos.pesosHablado(c.total) + ", " + Ui.estadoCompra(c)
                    + (c.usuarioNombre.isEmpty() ? "" : ", registró " + c.usuarioNombre)
                    + ". Toca para ver el detalle.";
            filas.add(f);
        }
        adaptador.setFilas(filas);
        txtResumen.setText(compras.size() + (compras.size() == 1 ? " compra" : " compras")
                + (compras.isEmpty() ? "" : " · " + Formatos.pesos(total) + " en compras"));
        TextView vacio = findViewById(R.id.txtVacio);
        if (filas.isEmpty()) {
            vacio.setText(texto(etBuscar).isEmpty()
                    ? "Todavía no has registrado compras.\n\nRegistra la primera con el botón de abajo."
                    : "No encontramos compras con esa búsqueda.");
        }
    }
}
