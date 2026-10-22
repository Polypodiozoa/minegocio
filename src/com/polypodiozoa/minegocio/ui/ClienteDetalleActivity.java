package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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

public class ClienteDetalleActivity extends BaseActivity {

    private long id;
    private Modelo.Cliente cliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_detalle);
        configurarBarra("Cliente");
        id = idExtra("id");
        if (id <= 0) {
            aviso("No se indicó el cliente.");
            finish();
            return;
        }
        findViewById(R.id.btnAbono).setOnClickListener(v -> {
            Intent i = new Intent(this, PagoActivity.class);
            i.putExtra("cliente_id", id);
            startActivity(i);
        });
        findViewById(R.id.btnVenta).setOnClickListener(v -> {
            Intent i = new Intent(this, VentaActivity.class);
            i.putExtra("cliente_id", id);
            startActivity(i);
        });
        findViewById(R.id.btnEditar).setOnClickListener(v -> {
            Intent i = new Intent(this, ClienteActivity.class);
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
        cliente = bd.cliente(id);
        if (cliente == null) {
            aviso("El cliente ya no existe.");
            finish();
            return;
        }
        TextView nombre = findViewById(R.id.txtNombre);
        nombre.setText(cliente.nombre);
        nombre.setContentDescription("Cliente: " + cliente.nombre);
        ponerOpcional(R.id.txtTelefono, cliente.telefono);
        ponerOpcional(R.id.txtCorreo, cliente.correo);
        ponerOpcional(R.id.txtDireccion, cliente.direccion);

        ImageView foto = findViewById(R.id.imgPersona);
        foto.setOnClickListener(v -> {
            if (cliente != null && cliente.foto != null && !cliente.foto.isEmpty()) {
                Intent i = new Intent(this, FotoActivity.class);
                i.putExtra("ruta", cliente.foto);
                startActivity(i);
            }
        });
        if (cliente.foto == null || cliente.foto.isEmpty()) {
            foto.setPadding(dp(10), dp(10), dp(10), dp(10));
            foto.setColorFilter(getColor(R.color.primario));
            foto.setImageResource(R.drawable.ic_persona);
            foto.setContentDescription("Sin foto del cliente");
        } else {
            foto.setPadding(0, 0, 0, 0);
            foto.setColorFilter(null);
            Imagen.mostrar(foto, cliente.foto, R.drawable.ic_persona, 400);
            foto.setContentDescription("Foto de " + cliente.nombre);
        }

        long saldo = cliente.saldo;
        TextView lbl = findViewById(R.id.lblSaldo);
        TextView valor = findViewById(R.id.txtSaldo);
        TextView estado = findViewById(R.id.txtEstadoSaldo);
        View card = findViewById(R.id.cardSaldo);
        if (saldo > 0) {
            lbl.setText("Saldo pendiente");
            valor.setText(Formatos.pesos(saldo));
            estado.setText("Registra un abono cuando el cliente pague.");
            estado.setTextColor(getColor(R.color.advertencia));
            card.setBackgroundResource(R.drawable.fondo_alerta);
        } else if (saldo < 0) {
            lbl.setText("Saldo a favor");
            valor.setText(Formatos.pesos(-saldo));
            estado.setText("El cliente tiene saldo a favor.");
            estado.setTextColor(getColor(R.color.exito));
            card.setBackgroundResource(R.drawable.fondo_badge_verde);
        } else {
            lbl.setText("Saldo del cliente");
            valor.setText("$ 0");
            estado.setText("Al día. No tiene saldo pendiente.");
            estado.setTextColor(getColor(R.color.exito));
            card.setBackgroundResource(R.drawable.fondo_badge_verde);
        }
        card.setContentDescription(lbl.getText() + ": " + Formatos.pesosHablado(Math.abs(saldo))
                + ". " + estado.getText());

        View cardNotas = findViewById(R.id.cardNotas);
        if (cliente.notas == null || cliente.notas.isEmpty()) {
            cardNotas.setVisibility(View.GONE);
        } else {
            cardNotas.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.txtNotas)).setText(cliente.notas);
        }

        cargarResumenCuenta();
        cargarDeudas();
        cargarVentas();
        cargarAbonos();
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

    private void cargarResumenCuenta() {
        LinearLayout contenedor = findViewById(R.id.containerResumen);
        contenedor.removeAllViews();
        long comprado = bd.totalCompradoCliente(id);
        long abonado = bd.totalAbonadoCliente(id);
        contenedor.addView(Ui.filaResumen(this, "Total comprado", Formatos.pesos(comprado)));
        contenedor.addView(Ui.separador(this));
        contenedor.addView(Ui.filaResumen(this, "Total abonado", Formatos.pesos(abonado)));
        contenedor.addView(Ui.separador(this));
        if (cliente.cupo > 0) {
            long disponible = Math.max(0, cliente.cupo - Math.max(0, cliente.saldo));
            contenedor.addView(Ui.filaResumen(this, "Cupo de fiado", Formatos.pesos(cliente.cupo)));
            contenedor.addView(Ui.separador(this));
            contenedor.addView(Ui.filaResumen(this, "Cupo disponible", Formatos.pesos(disponible)));
        } else {
            contenedor.addView(Ui.filaResumen(this, "Cupo de fiado", "Sin límite"));
        }
    }

    private void cargarDeudas() {
        LinearLayout contenedor = findViewById(R.id.containerDeudas);
        TextView vacio = findViewById(R.id.txtVacioDeudas);
        contenedor.removeAllViews();
        List<Modelo.Deuda> deudas = bd.deudasCliente(id);
        vacio.setVisibility(deudas.isEmpty() ? View.VISIBLE : View.GONE);
        for (int i = 0; i < deudas.size(); i++) {
            if (i > 0) contenedor.addView(Ui.separador(this));
            contenedor.addView(filaDeuda(deudas.get(i)));
        }
    }

    private View filaDeuda(Modelo.Deuda d) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setMinimumHeight(dp(70));
        fila.setPadding(0, dp(8), 0, dp(8));
        fila.setClickable(true);
        fila.setFocusable(true);
        fila.setForeground(getDrawable(R.drawable.selector_foreground));

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        textos.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView t = Ui.texto(this, "Venta #" + d.ventaId + " · " + Formatos.pesos(d.saldo),
                17, getColor(R.color.texto), true);
        String sub = (d.fechaLimite > 0
                ? "Vence " + Fechas.fecha(d.fechaLimite) + " · " + Fechas.vencimientoTexto(d.fechaLimite)
                : "Sin fecha límite") + " · Abonado " + Formatos.pesos(d.abonado);
        TextView s = Ui.texto(this, sub, 14, getColor(R.color.texto_secundario), false);
        textos.addView(t);
        textos.addView(s);
        fila.addView(textos);

        TextView badge = new TextView(this);
        badge.setTextSize(13);
        badge.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        int dias = Fechas.diasHasta(d.fechaLimite);
        String estado = d.fechaLimite <= 0 ? "Sin fecha"
                : dias < 0 ? "Vencida" : dias <= 7 ? "Por vencer" : "Vigente";
        Ui.ponerBadge(badge, estado);
        fila.addView(badge);
        fila.setContentDescription("Venta fiada " + d.ventaId + ", saldo "
                + Formatos.pesosHablado(d.saldo) + ", " + sub + ", " + estado
                + ". Toca para ver la venta.");
        fila.setOnClickListener(v -> {
            Intent i = new Intent(this, VentaDetalleActivity.class);
            i.putExtra("id", d.ventaId);
            startActivity(i);
        });
        return fila;
    }

    private void cargarVentas() {
        LinearLayout contenedor = findViewById(R.id.containerVentas);
        TextView vacio = findViewById(R.id.txtVacioVentas);
        contenedor.removeAllViews();
        List<Modelo.Venta> ventas = bd.ventasCliente(id, 40);
        vacio.setVisibility(ventas.isEmpty() ? View.VISIBLE : View.GONE);
        for (int i = 0; i < ventas.size(); i++) {
            if (i > 0) contenedor.addView(Ui.separador(this));
            contenedor.addView(filaVenta(ventas.get(i)));
        }
    }

    private View filaVenta(Modelo.Venta v) {
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
        TextView t = Ui.texto(this, "Venta #" + v.id, 17, getColor(R.color.texto), true);
        TextView s = Ui.texto(this, Fechas.fecha(v.fecha) + " · " + v.formaPago
                + (v.anulada ? " · Anulada" : ""), 14, getColor(R.color.texto_secundario), false);
        textos.addView(t);
        textos.addView(s);
        fila.addView(textos);

        LinearLayout derecha = new LinearLayout(this);
        derecha.setOrientation(LinearLayout.VERTICAL);
        derecha.setGravity(Gravity.END);
        derecha.setLayoutParams(new LinearLayout.LayoutParams(dp(120), ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView valor = Ui.texto(this, Formatos.pesos(v.total), 18,
                v.anulada ? getColor(R.color.error) : getColor(R.color.texto), true);
        if (v.anulada) valor.setPaintFlags(valor.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        TextView badge = new TextView(this);
        badge.setTextSize(13);
        badge.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        Ui.ponerBadge(badge, Ui.estadoVenta(v));
        LinearLayout.LayoutParams lb = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lb.topMargin = dp(4);
        badge.setLayoutParams(lb);
        derecha.addView(valor);
        derecha.addView(badge);
        fila.addView(derecha);

        fila.setContentDescription("Venta " + v.id + ", " + Fechas.fecha(v.fecha)
                + ", total " + Formatos.pesosHablado(v.total) + ", " + Ui.estadoVenta(v)
                + ". Toca para ver el detalle.");
        fila.setOnClickListener(x -> {
            Intent i = new Intent(this, VentaDetalleActivity.class);
            i.putExtra("id", v.id);
            startActivity(i);
        });
        return fila;
    }

    private void cargarAbonos() {
        LinearLayout contenedor = findViewById(R.id.containerAbonos);
        TextView vacio = findViewById(R.id.txtVacioAbonos);
        contenedor.removeAllViews();
        List<Modelo.Pago> pagos = bd.pagosCliente(id);
        vacio.setVisibility(pagos.isEmpty() ? View.VISIBLE : View.GONE);
        for (int i = 0; i < pagos.size(); i++) {
            if (i > 0) contenedor.addView(Ui.separador(this));
            contenedor.addView(filaPago(pagos.get(i)));
        }
    }

    private View filaPago(Modelo.Pago pago) {
        LinearLayout exterior = new LinearLayout(this);
        exterior.setOrientation(LinearLayout.VERTICAL);
        exterior.setPadding(0, dp(6), 0, dp(6));

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        textos.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView t = Ui.texto(this, "Abono · " + Formatos.pesos(pago.monto), 17,
                getColor(R.color.texto), true);
        String sub = Fechas.fechaHora(pago.fecha) + " · " + pago.formaPago
                + (pago.usuarioNombre.isEmpty() ? "" : " · " + pago.usuarioNombre);
        if (pago.notas != null && !pago.notas.isEmpty()) sub += "\n" + pago.notas;
        TextView s = Ui.texto(this, sub, 14, getColor(R.color.texto_secundario), false);
        textos.addView(t);
        textos.addView(s);
        fila.addView(textos);

        ImageButton borrar = new ImageButton(this);
        borrar.setImageResource(R.drawable.ic_basura);
        borrar.setColorFilter(getColor(R.color.error));
        borrar.setBackgroundResource(android.R.drawable.list_selector_background);
        borrar.setContentDescription("Eliminar abono de " + Formatos.pesosHablado(pago.monto));
        LinearLayout.LayoutParams lb = new LinearLayout.LayoutParams(dp(48), dp(48));
        borrar.setLayoutParams(lb);
        borrar.setOnClickListener(v -> confirmarEliminarPago(pago));
        fila.addView(borrar);
        exterior.addView(fila);

        List<Modelo.Adjunto> comprobantes = bd.adjuntos("pago", pago.id);
        if (!comprobantes.isEmpty()) {
            Button ver = new Button(this);
            ver.setText(comprobantes.size() == 1 ? "Ver comprobante" : "Ver comprobantes (" + comprobantes.size() + ")");
            ver.setTextSize(15);
            ver.setAllCaps(false);
            ver.setBackgroundResource(R.drawable.boton_texto);
            ver.setTextColor(getColor(R.color.primario));
            ver.setMinHeight(0);
            ver.setMinimumHeight(dp(48));
            ver.setPadding(0, 0, 0, 0);
            ver.setOnClickListener(v -> verAdjuntos("pago", pago.id));
            exterior.addView(ver);
        }
        exterior.setContentDescription("Abono de " + Formatos.pesosHablado(pago.monto) + ", "
                + Fechas.fechaHora(pago.fecha) + ", " + pago.formaPago
                + (pago.usuarioNombre.isEmpty() ? "" : ", registró " + pago.usuarioNombre));
        return exterior;
    }

    private void confirmarEliminarPago(Modelo.Pago pago) {
        confirmar("Eliminar abono",
                "¿Eliminar el abono de " + Formatos.pesos(pago.monto) + " del "
                        + Fechas.fechaHora(pago.fecha) + "? El saldo del cliente volverá a subir.",
                "Eliminar", () -> {
                    for (Modelo.Adjunto a : bd.adjuntos("pago", pago.id)) {
                        com.polypodiozoa.minegocio.util.Archivos.borrar(a.ruta);
                    }
                    bd.eliminarPago(pago.id);
                    aviso("Abono eliminado.");
                    cargar();
                });
    }
}
