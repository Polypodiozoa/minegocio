package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.MenuPrincipal;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.List;

public class PrincipalActivity extends BaseActivity {

    private GridLayout gridAcciones;
    private LinearLayout listaRecientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        gridAcciones = findViewById(R.id.gridAcciones);
        listaRecientes = findViewById(R.id.listaRecientes);
        findViewById(R.id.btnUsuario).setOnClickListener(v -> abrir(AccesosActivity.class));
        prepararAcciones();
        findViewById(R.id.btnVerVentas).setOnClickListener(v -> abrir(VentasActivity.class));
        findViewById(R.id.btnCopia).setOnClickListener(v -> abrir(AjustesActivity.class));
        findViewById(R.id.btnPrimerProducto).setOnClickListener(v -> abrir(ProductoActivity.class));
        findViewById(R.id.cardAlerta).setOnClickListener(v -> {
            Intent i = new Intent(this, InventarioActivity.class);
            i.putExtra("bajos", true);
            startActivity(i);
        });
        View porCobrar = findViewById(R.id.celdaPorCobrar);
        porCobrar.setClickable(true);
        porCobrar.setFocusable(true);
        porCobrar.setOnClickListener(v -> abrir(DeudasActivity.class));
        findViewById(R.id.cardDeudas).setOnClickListener(v -> abrir(DeudasActivity.class));
        avisarBaseApartada();
    }

    /** Si la app apartó una base dañada al arrancar, se explica y se ofrece Ajustes. */
    private void avisarBaseApartada() {
        android.content.SharedPreferences p = getSharedPreferences(PREFS_UI, MODE_PRIVATE);
        if (!p.getBoolean("base_apartada", false)) return;
        p.edit().putBoolean("base_apartada", false).apply();
        confirmar("La base de datos estaba dañada",
                "La app no pudo abrir la base de datos y la guardó aparte para no perderla. "
                        + "Arrancó vacía. Si tienes una copia de seguridad, restáurala en "
                        + "Ajustes → Restaurar una copia.",
                "Ir a Ajustes", () -> abrir(AjustesActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        View desplazable = findViewById(R.id.scrollForm);
        if (desplazable != null) desplazable.post(() -> desplazable.scrollTo(0, 0));
        prepararAcciones();
        cargar();
    }

    private void prepararAcciones() {
        gridAcciones.removeAllViews();
        Modelo.Usuario usuario = bd.usuarioActual();
        String orden = usuario == null ? "" : usuario.menu;
        for (MenuPrincipal.Opcion o : MenuPrincipal.opciones(orden)) {
            agregar(o.icono, o.etiqueta, o.principal, () -> abrirPorClave(o.clave));
        }
    }

    private void agregar(int icono, String texto, boolean principal, Runnable accion) {
        View tarjeta = Ui.tarjetaAccion(this, icono, texto, principal, v -> accion.run());
        Ui.agregarAccion(gridAcciones, tarjeta);
    }

    private void abrirPorClave(String clave) {
        switch (clave) {
            case "nueva_venta": abrir(VentaActivity.class); break;
            case "nueva_compra": abrir(CompraActivity.class); break;
            case "ventas": abrir(VentasActivity.class); break;
            case "compras": abrir(ComprasActivity.class); break;
            case "inventario": abrir(InventarioActivity.class); break;
            case "clientes": abrir(ClientesActivity.class); break;
            case "deudas": abrir(DeudasActivity.class); break;
            case "proveedores": abrir(ProveedoresActivity.class); break;
            case "gastos": abrir(GastosActivity.class); break;
            case "informes": abrir(InformesActivity.class); break;
            case "perfil": abrir(PerfilActivity.class); break;
            case "ajustes": abrir(AjustesActivity.class); break;
            default: break;
        }
    }

    private void cargar() {
        String nombreNegocio = bd.ajuste("negocio_nombre", "").trim();
        TextView negocio = findViewById(R.id.txtNegocio);
        if (nombreNegocio.isEmpty()) {
            negocio.setVisibility(View.GONE);
        } else {
            negocio.setText(nombreNegocio);
            negocio.setContentDescription("Negocio: " + nombreNegocio);
            negocio.setVisibility(View.VISIBLE);
        }
        TextView fecha = findViewById(R.id.txtFecha);
        fecha.setText(Fechas.hoyTexto());

        Modelo.Usuario usuario = bd.usuarioActual();
        TextView botonUsuario = findViewById(R.id.btnUsuario);
        if (usuario != null) {
            botonUsuario.setText(usuario.nombre);
            botonUsuario.setContentDescription("Administrador de accesos. Usuario actual: "
                    + usuario.nombre + ". Toca para cambiar de persona.");
        }

        long[] hoy = Fechas.rango(Fechas.HOY);
        long[] mes = Fechas.rango(Fechas.MES);
        Modelo.Resumen rh = bd.resumen(hoy[0], hoy[1]);
        Modelo.Resumen rm = bd.resumen(mes[0], mes[1]);

        ponerMetrica(R.id.celdaVentasHoy, R.id.txtVentasHoy, "Ventas de hoy", rh.ventasTotal);
        ponerMetrica(R.id.celdaVentasMes, R.id.txtVentasMes, "Ventas del mes", rm.ventasTotal);
        ponerMetrica(R.id.celdaUtilidad, R.id.txtUtilidad, "Utilidad del mes", rm.utilidadBruta);

        TextView porCobrar = findViewById(R.id.txtPorCobrar);
        porCobrar.setText(Formatos.pesos(rm.porCobrar));
        View celda = findViewById(R.id.celdaPorCobrar);
        celda.setContentDescription("Por cobrar: " + Formatos.pesosHablado(rm.porCobrar)
                + ". Toca para ver las deudas.");

        cargarAlerta();
        cargarAlertaDeudas();
        cargarRecientes();
        cargarRespaldo();
        findViewById(R.id.cardBienvenida).setVisibility(
                bd.totalProductos() == 0 ? View.VISIBLE : View.GONE);
    }

    private void ponerMetrica(int idCelda, int idValor, String etiqueta, long valor) {
        TextView v = findViewById(idValor);
        v.setText(Formatos.pesos(valor));
        View celda = findViewById(idCelda);
        celda.setContentDescription(etiqueta + ": " + Formatos.pesosHablado(valor));
    }

    private void cargarAlerta() {
        View card = findViewById(R.id.cardAlerta);
        TextView texto = findViewById(R.id.txtAlerta);
        List<Modelo.Producto> bajos = bd.productosStockBajo(50);
        if (bajos.isEmpty()) {
            card.setVisibility(View.GONE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(bajos.size() == 1 ? "1 producto necesita reposición"
                : bajos.size() + " productos necesitan reposición");
        sb.append(". Toca para revisar el inventario.");
        texto.setText(sb.toString());
        card.setContentDescription(sb.toString());
        card.setVisibility(View.VISIBLE);
    }

    private void cargarAlertaDeudas() {
        View card = findViewById(R.id.cardDeudas);
        TextView texto = findViewById(R.id.txtDeudas);
        Modelo.Resumen rm = bd.resumen(Fechas.rango(Fechas.MES)[0], Fechas.rango(Fechas.MES)[1]);
        if (rm.deudasVencidas == 0) {
            card.setVisibility(View.GONE);
            return;
        }
        String mensaje = rm.deudasVencidas == 1
                ? "1 deuda vencida por " + Formatos.pesos(rm.deudaVencida)
                : rm.deudasVencidas + " deudas vencidas por " + Formatos.pesos(rm.deudaVencida);
        texto.setText(mensaje + ". Toca para revisar la cartera.");
        card.setContentDescription(mensaje + ". Toca para revisar la cartera de fiados.");
        card.setVisibility(View.VISIBLE);
    }

    private void cargarRecientes() {
        listaRecientes.removeAllViews();
        List<Modelo.Venta> ventas = bd.ultimasVentas(5);
        if (ventas.isEmpty()) {
            TextView vacio = Ui.texto(this,
                    "Aún no hay ventas registradas. Cuando registres la primera, aparecerá aquí.",
                    16, getColor(R.color.texto_secundario), false);
            vacio.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 6));
            listaRecientes.addView(vacio);
            return;
        }
        for (int i = 0; i < ventas.size(); i++) {
            if (i > 0) listaRecientes.addView(Ui.separador(this));
            listaRecientes.addView(filaReciente(ventas.get(i)));
        }
    }

    private View filaReciente(Modelo.Venta v) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setMinimumHeight(Ui.dp(this, 66));
        fila.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
        fila.setClickable(true);
        fila.setFocusable(true);
        fila.setForeground(getDrawable(R.drawable.selector_foreground));

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        textos.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        String titulo = "Venta #" + v.id + (v.clienteNombre.isEmpty() ? "" : " · " + v.clienteNombre);
        TextView t = Ui.texto(this, titulo, 17, getColor(R.color.texto), true);
        t.setMaxLines(1);
        t.setEllipsize(android.text.TextUtils.TruncateAt.END);
        String sub = Fechas.fechaHora(v.fecha) + " · " + v.formaPago
                + (v.usuarioNombre.isEmpty() ? "" : " · " + v.usuarioNombre);
        if (v.anulada) sub += " · Anulada";
        TextView s = Ui.texto(this, sub, 14, getColor(R.color.texto_secundario), false);
        textos.addView(t);
        textos.addView(s);
        fila.addView(textos);

        TextView total = Ui.texto(this, Formatos.pesos(v.total), 18,
                v.anulada ? getColor(R.color.error) : getColor(R.color.texto), true);
        if (v.anulada) total.setPaintFlags(total.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        fila.addView(total);

        String descripcion = titulo + ", " + Fechas.fechaHora(v.fecha) + ", total "
                + Formatos.pesosHablado(v.total) + (v.anulada ? ", anulada" : "")
                + ". Toca para ver el detalle.";
        fila.setContentDescription(descripcion);
        fila.setOnClickListener(x -> {
            Intent i = new Intent(this, VentaDetalleActivity.class);
            i.putExtra("id", v.id);
            startActivity(i);
        });
        return fila;
    }

    private void cargarRespaldo() {
        TextView t = findViewById(R.id.txtRespaldo);
        long ultima = bd.ajusteNumero("ultima_copia", 0);
        if (ultima <= 0) {
            t.setText("Última copia: nunca. Te recomendamos crear una copia hoy.");
        } else {
            t.setText("Última copia: " + Fechas.fechaHora(ultima)
                    + ". Incluye productos, ventas, clientes y fotos.");
        }
    }
}
