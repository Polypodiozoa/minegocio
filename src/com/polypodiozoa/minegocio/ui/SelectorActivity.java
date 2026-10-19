package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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

/** Selector de clientes, proveedores o productos. Devuelve el id elegido. */
public class SelectorActivity extends BaseActivity {

    public static final int MODO_CLIENTE = 1;
    public static final int MODO_PROVEEDOR = 2;
    public static final int MODO_PRODUCTO = 3;
    public static final String EXTRA_MODO = "modo";
    private static final int CREAR = 501;

    private int modo;
    private EditText etBuscar;
    private AdaptadorFilas adaptador;
    private TextView txtResumen, txtVacio;
    private Button btnNuevo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_personas);
        modo = getIntent().getIntExtra(EXTRA_MODO, MODO_CLIENTE);
        etBuscar = findViewById(R.id.etBuscar);
        txtResumen = findViewById(R.id.txtResumen);
        txtVacio = findViewById(R.id.txtVacio);
        btnNuevo = findViewById(R.id.btnNuevo);
        ListView lista = findViewById(R.id.lista);
        adaptador = new AdaptadorFilas(this);
        adaptador.setOnFilaClick(f -> devolver(f.id));
        lista.setAdapter(adaptador);
        lista.setEmptyView(txtVacio);

        if (modo == MODO_PRODUCTO) {
            configurarBarra("Agregar producto");
            etBuscar.setHint("Buscar producto");
            etBuscar.setContentDescription("Buscar producto");
            btnNuevo.setText("Nuevo producto");
            btnNuevo.setContentDescription("Nuevo producto");
            txtResumen.setText("Toca un producto para agregarlo a la venta.");
        } else if (modo == MODO_PROVEEDOR) {
            configurarBarra("Elegir proveedor");
            etBuscar.setHint("Buscar proveedor");
            etBuscar.setContentDescription("Buscar proveedor");
            btnNuevo.setText("Nuevo proveedor");
            btnNuevo.setContentDescription("Nuevo proveedor");
            txtResumen.setText("Toca un proveedor para asociarlo a la compra.");
        } else {
            configurarBarra("Elegir cliente");
            etBuscar.setHint("Buscar cliente");
            etBuscar.setContentDescription("Buscar cliente");
            btnNuevo.setText("Nuevo cliente");
            btnNuevo.setContentDescription("Nuevo cliente");
            txtResumen.setText("Toca un cliente para asociarlo a la venta.");
        }

        btnNuevo.setOnClickListener(v -> {
            Class<?> destino = modo == MODO_PRODUCTO ? ProductoActivity.class
                    : modo == MODO_PROVEEDOR ? ProveedorActivity.class : ClienteActivity.class;
            abrirResultado(destino, CREAR);
        });
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

    private void devolver(long id) {
        Intent data = new Intent();
        data.putExtra("id", id);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREAR && resultCode == RESULT_OK) {
            long id = data == null ? 0 : data.getLongExtra("id", 0);
            if (id > 0) devolver(id);
            else cargar();
        }
    }

    private void cargar() {
        String filtro = texto(etBuscar);
        List<AdaptadorFilas.Fila> filas = new ArrayList<>();
        if (modo == MODO_PRODUCTO) {
            for (Modelo.Producto p : bd.productos(filtro, false)) {
                AdaptadorFilas.Fila f = new AdaptadorFilas.Fila();
                f.id = p.id;
                f.titulo = p.nombre;
                f.subtitulo = (p.categoria.isEmpty() ? "Sin categoría" : p.categoria)
                        + " · " + Ui.unidades(p.stock, p.unidad);
                f.valor = Formatos.pesos(p.precio);
                f.estado = Ui.estadoStock(p);
                f.foto = p.foto;
                f.iconoDefecto = R.drawable.ic_caja;
                f.dato = p;
                f.descripcion = p.nombre + ", " + f.subtitulo + ", precio "
                        + Formatos.pesosHablado(p.precio)
                        + (f.estado.isEmpty() ? "" : ", " + f.estado)
                        + ". Toca para agregar.";
                filas.add(f);
            }
            if (filas.isEmpty()) {
                txtVacio.setText(filtro.isEmpty()
                        ? "No tienes productos activos.\n\nCrea uno con el botón de abajo."
                        : "No encontramos productos con esa búsqueda.");
            }
        } else if (modo == MODO_PROVEEDOR) {
            if (filtro.isEmpty()) {
                AdaptadorFilas.Fila f = new AdaptadorFilas.Fila();
                f.id = 0;
                f.titulo = "Sin proveedor";
                f.subtitulo = "Compra no asociada a un proveedor";
                f.iconoDefecto = R.drawable.ic_tienda;
                f.descripcion = "Sin proveedor. Compra no asociada a un proveedor.";
                filas.add(f);
            }
            for (Modelo.Proveedor p : bd.proveedores(filtro)) {
                AdaptadorFilas.Fila f = new AdaptadorFilas.Fila();
                f.id = p.id;
                f.titulo = p.nombre;
                f.subtitulo = p.contacto.isEmpty() ? p.telefono : p.contacto + " · " + p.telefono;
                f.foto = p.foto;
                f.iconoDefecto = R.drawable.ic_tienda;
                f.dato = p;
                f.descripcion = p.nombre + ", " + f.subtitulo + ". Toca para elegir.";
                filas.add(f);
            }
            if (filas.isEmpty()) {
                txtVacio.setText(filtro.isEmpty()
                        ? "No tienes proveedores.\n\nCrea uno con el botón de abajo."
                        : "No encontramos proveedores con esa búsqueda.");
            }
        } else {
            if (filtro.isEmpty()) {
                AdaptadorFilas.Fila f = new AdaptadorFilas.Fila();
                f.id = 0;
                f.titulo = "Consumidor final";
                f.subtitulo = "Venta sin cliente asociado";
                f.iconoDefecto = R.drawable.ic_persona;
                f.descripcion = "Consumidor final. Venta sin cliente asociado.";
                filas.add(f);
            }
            for (Modelo.Cliente c : bd.clientes(filtro)) {
                AdaptadorFilas.Fila f = new AdaptadorFilas.Fila();
                f.id = c.id;
                f.titulo = c.nombre;
                f.subtitulo = c.telefono.isEmpty() ? c.correo : c.telefono;
                f.valor = c.saldo == 0 ? "" : Formatos.pesos(Math.abs(c.saldo));
                f.estado = c.saldo > 0 ? "Debe" : "";
                f.foto = c.foto;
                f.iconoDefecto = R.drawable.ic_persona;
                f.dato = c;
                f.descripcion = c.nombre + ", " + f.subtitulo
                        + (c.saldo > 0 ? ", debe " + Formatos.pesosHablado(c.saldo) : "")
                        + ". Toca para elegir.";
                filas.add(f);
            }
            if (filas.isEmpty()) {
                txtVacio.setText(filtro.isEmpty()
                        ? "No tienes clientes.\n\nCrea uno con el botón de abajo."
                        : "No encontramos clientes con esa búsqueda.");
            }
        }
        adaptador.setFilas(filas);
    }
}
