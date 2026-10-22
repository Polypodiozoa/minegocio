package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.Ui;

/** Ficha de la cuenta actual y acceso a su menú. Página de administración verde. */
public class PerfilActivity extends BaseActivity {

    private static final int PEDIR_EDICION = 801;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TemaAdmin);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        configurarBarra("Perfil");
        findViewById(R.id.btnMenu).setOnClickListener(v ->
                startActivity(new Intent(this, MenuPerfilActivity.class)));
        findViewById(R.id.btnEditarPerfil).setOnClickListener(v -> {
            Modelo.Usuario u = bd.usuarioActual();
            if (u == null) return;
            Intent i = new Intent(this, UsuarioActivity.class);
            i.putExtra("id", u.id);
            startActivityForResult(i, PEDIR_EDICION);
        });
        findViewById(R.id.btnCambiar).setOnClickListener(v ->
                startActivity(new Intent(this, AccesosActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        Modelo.Usuario u = bd.usuarioActual();
        if (u == null) {
            finish();
            return;
        }
        TextView inicial = findViewById(R.id.txtInicial);
        inicial.setText(inicial(u.nombre));
        inicial.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        inicial.setGravity(Gravity.CENTER);
        ((TextView) findViewById(R.id.txtNombrePerfil)).setText(u.nombre);
        ((TextView) findViewById(R.id.txtRolPerfil)).setText(u.rolVisible());

        long[] hoy = Fechas.rango(Fechas.HOY);
        long[] mes = Fechas.rango(Fechas.MES);
        long ventasHoy = bd.totalVentasUsuario(u.id, hoy[0], hoy[1]);
        long ventasMes = bd.totalVentasUsuario(u.id, mes[0], mes[1]);
        int cantidadHoy = bd.cantidadVentasUsuario(u.id, hoy[0], hoy[1]);
        int cantidadMes = bd.cantidadVentasUsuario(u.id, mes[0], mes[1]);
        long gastosMes = bd.totalGastosUsuario(u.id, mes[0], mes[1]);
        ((TextView) findViewById(R.id.txtVentasHoy)).setText(Formatos.pesos(ventasHoy));
        ((TextView) findViewById(R.id.txtVentasMes)).setText(Formatos.pesos(ventasMes));
        ((TextView) findViewById(R.id.txtDetalleVentas)).setText(
                "Hoy: " + cantidadHoy + (cantidadHoy == 1 ? " venta" : " ventas")
                        + " · Este mes: " + cantidadMes
                        + (cantidadMes == 1 ? " venta" : " ventas"));
        ((TextView) findViewById(R.id.txtGastosMes)).setText(
                "Gastos registrados este mes: " + Formatos.pesos(gastosMes));
    }

    private String inicial(String nombre) {
        String n = nombre == null ? "" : nombre.trim();
        if (n.isEmpty()) return "?";
        return n.substring(0, 1).toUpperCase(java.util.Locale.getDefault());
    }
}
