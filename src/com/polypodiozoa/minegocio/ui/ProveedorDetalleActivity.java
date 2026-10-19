package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.Imagen;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.List;

public class ProveedorDetalleActivity extends BaseActivity {

    private long id;
    private Modelo.Proveedor proveedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proveedor_detalle);
        configurarBarra("Proveedor");
        id = idExtra("id");
        if (id <= 0) {
            aviso("No se indicó el proveedor.");
            finish();
            return;
        }
        findViewById(R.id.btnCompra).setOnClickListener(v -> {
            Intent i = new Intent(this, CompraActivity.class);
            i.putExtra("proveedor_id", id);
            startActivity(i);
        });
        findViewById(R.id.btnEditar).setOnClickListener(v -> {
            Intent i = new Intent(this, ProveedorActivity.class);
            i.putExtra("id", id);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        proveedor = bd.proveedor(id);
        if (proveedor == null) {
            aviso("El proveedor ya no existe.");
            finish();
            return;
        }
        TextView nombre = findViewById(R.id.txtNombre);
        nombre.setText(proveedor.nombre);
        nombre.setContentDescription("Proveedor: " + proveedor.nombre);
        ponerOpcional(R.id.txtContacto, proveedor.contacto);
        ponerOpcional(R.id.txtTelefono, proveedor.telefono);
        ponerOpcional(R.id.txtCorreo, proveedor.correo);
        ponerOpcional(R.id.txtDireccion, proveedor.direccion);

        ImageView foto = findViewById(R.id.imgPersona);
        foto.setOnClickListener(v -> {
            if (proveedor != null && proveedor.foto != null && !proveedor.foto.isEmpty()) {
                Intent i = new Intent(this, FotoActivity.class);
                i.putExtra("ruta", proveedor.foto);
                startActivity(i);
            }
        });
        if (proveedor.foto == null || proveedor.foto.isEmpty()) {
            foto.setPadding(dp(10), dp(10), dp(10), dp(10));
            foto.setColorFilter(getColor(R.color.primario));
            foto.setImageResource(R.drawable.ic_tienda);
            foto.setContentDescription("Sin foto del proveedor");
        } else {
            foto.setPadding(0, 0, 0, 0);
            foto.setColorFilter(null);
            Imagen.mostrar(foto, proveedor.foto, R.drawable.ic_tienda, 400);
            foto.setContentDescription("Foto de " + proveedor.nombre);
        }

        View cardNotas = findViewById(R.id.cardNotas);
        if (proveedor.notas == null || proveedor.notas.isEmpty()) {
            cardNotas.setVisibility(View.GONE);
        } else {
            cardNotas.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.txtNotas)).setText(proveedor.notas);
        }
        cargarCompras();
    }

    private void ponerOpcional(int idVista, String texto) {
        TextView t = findViewById(idVista);
        if (texto == null || texto.trim().isEmpty()) {
            t.setVisibility(View.GONE);
        } else {
            t.setVisibility(View.VISIBLE);
            t.setText(texto);
        }
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    private void cargarCompras() {
        LinearLayout contenedor = findViewById(R.id.containerCompras);
        TextView vacio = findViewById(R.id.txtVacioCompras);
        contenedor.removeAllViews();
        List<Modelo.Compra> compras = bd.comprasProveedor(id, 40);
        vacio.setVisibility(compras.isEmpty() ? View.VISIBLE : View.GONE);
        for (int i = 0; i < compras.size(); i++) {
            if (i > 0) contenedor.addView(Ui.separador(this));
            contenedor.addView(filaCompra(compras.get(i)));
        }
    }

    private View filaCompra(Modelo.Compra c) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setMinimumHeight(dp(64));
        fila.setPadding(0, dp(8), 0, dp(8));
        fila.setClickable(true);
        fila.setFocusable(true);
        fila.setForeground(getDrawable(R.drawable.selector_foreground));

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        textos.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView t = Ui.texto(this, "Compra #" + c.id, 17, getColor(R.color.texto), true);
        TextView s = Ui.texto(this, Fechas.fecha(c.fecha) + " · " + c.formaPago
                + (c.anulada ? " · Anulada" : ""), 14, getColor(R.color.texto_secundario), false);
        textos.addView(t);
        textos.addView(s);
        fila.addView(textos);

        LinearLayout derecha = new LinearLayout(this);
        derecha.setOrientation(LinearLayout.VERTICAL);
        derecha.setGravity(Gravity.END);
        derecha.setLayoutParams(new LinearLayout.LayoutParams(dp(120), ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView valor = Ui.texto(this, Formatos.pesos(c.total), 18,
                c.anulada ? getColor(R.color.error) : getColor(R.color.texto), true);
        if (c.anulada) valor.setPaintFlags(valor.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        TextView badge = new TextView(this);
        badge.setTextSize(13);
        badge.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        Ui.ponerBadge(badge, Ui.estadoCompra(c));
        LinearLayout.LayoutParams lb = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lb.topMargin = dp(4);
        badge.setLayoutParams(lb);
        derecha.addView(valor);
        derecha.addView(badge);
        fila.addView(derecha);

        fila.setContentDescription("Compra " + c.id + ", " + Fechas.fecha(c.fecha)
                + ", total " + Formatos.pesosHablado(c.total) + ", " + Ui.estadoCompra(c)
                + ". Toca para ver el detalle.");
        fila.setOnClickListener(x -> {
            Intent i = new Intent(this, CompraDetalleActivity.class);
            i.putExtra("id", c.id);
            startActivity(i);
        });
        return fila;
    }
}
