package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class InventarioActivity extends BaseActivity {

    private EditText etBuscar;
    private ListView lista;
    private TextView txtVacio, txtResumen;
    private AdaptadorFilas adaptador;
    private int filtro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);
        configurarBarra("Inventario");
        etBuscar = findViewById(R.id.etBuscar);
        lista = findViewById(R.id.lista);
        txtVacio = findViewById(R.id.txtVacio);
        txtResumen = findViewById(R.id.txtResumen);
        adaptador = new AdaptadorFilas(this);
        adaptador.setOnFilaClick(f -> {
            Modelo.Producto p = (Modelo.Producto) f.dato;
            Intent i = new Intent(this, ProductoActivity.class);
            i.putExtra("id", p.id);
            startActivity(i);
        });
        lista.setAdapter(adaptador);
        lista.setEmptyView(txtVacio);
        filtro = getIntent().getBooleanExtra("bajos", false) ? 2 : 0;
        findViewById(R.id.btnTodos).setOnClickListener(v -> cambiarFiltro(0));
        findViewById(R.id.btnAgotados).setOnClickListener(v -> cambiarFiltro(1));
        findViewById(R.id.btnBajos).setOnClickListener(v -> cambiarFiltro(2));
        findViewById(R.id.btnNuevo).setOnClickListener(v -> abrir(ProductoActivity.class));
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

    private void cambiarFiltro(int nuevo) {
        filtro = nuevo;
        cargar();
    }

    private void cargar() {
        String busqueda = texto(etBuscar);
        List<Modelo.Producto> todos = bd.productos(busqueda, false);
        List<Modelo.Producto> todosSinFiltro = bd.productos("", false);
        int agotados = 0;
        int bajos = 0;
        for (Modelo.Producto p : todosSinFiltro) {
            if (p.stock <= 0) agotados++;
            else if (p.stock <= p.stockMinimo) bajos++;
        }
        List<AdaptadorFilas.Fila> filas = new ArrayList<>();
        int mostrados = 0;
        for (Modelo.Producto p : todos) {
            if (filtro == 1 && p.stock > 0) continue;
            if (filtro == 2 && (p.stock <= 0 || p.stock > p.stockMinimo)) continue;
            mostrados++;
            AdaptadorFilas.Fila f = new AdaptadorFilas.Fila();
            f.id = p.id;
            f.titulo = p.nombre;
            f.subtitulo = (p.categoria.isEmpty() ? "Sin categoría" : p.categoria)
                    + " · " + Ui.unidades(p.stock, p.unidad)
                    + (p.stockMinimo > 0 ? " · aviso en " + p.stockMinimo : "");
            f.valor = Formatos.pesos(p.precio);
            f.estado = Ui.estadoStock(p);
            f.foto = p.foto;
            f.iconoDefecto = R.drawable.ic_caja;
            f.dato = p;
            f.descripcion = p.nombre + ", " + f.subtitulo + ", precio "
                    + Formatos.pesosHablado(p.precio)
                    + (f.estado.isEmpty() ? "" : ", " + f.estado)
                    + ". Toca para editar.";
            filas.add(f);
        }
        adaptador.setFilas(filas);

        TextView bTodos = findViewById(R.id.btnTodos);
        TextView bAgotados = findViewById(R.id.btnAgotados);
        TextView bBajos = findViewById(R.id.btnBajos);
        bTodos.setText("Todos (" + todosSinFiltro.size() + ")");
        bAgotados.setText("Agotados (" + agotados + ")");
        bBajos.setText("Por agotarse (" + bajos + ")");
        Ui.estiloFiltro(bTodos, filtro == 0);
        Ui.estiloFiltro(bAgotados, filtro == 1);
        Ui.estiloFiltro(bBajos, filtro == 2);

        txtResumen.setText(todosSinFiltro.size() + (todosSinFiltro.size() == 1
                ? " producto" : " productos") + " · " + agotados + " agotados · "
                + bajos + " por agotarse");

        if (filas.isEmpty()) {
            if (!busqueda.isEmpty()) {
                txtVacio.setText("No encontramos productos que coincidan con “" + busqueda + "”.");
            } else if (filtro == 1) {
                txtVacio.setText("No hay productos agotados. ¡Bien!");
            } else if (filtro == 2) {
                txtVacio.setText("No hay productos cerca de agotarse. ¡Todo al día!");
            } else {
                txtVacio.setText("Todavía no tienes productos.\n\nAgrega el primero con el botón de abajo.");
            }
        }
    }
}
