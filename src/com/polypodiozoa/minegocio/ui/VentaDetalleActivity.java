package com.polypodiozoa.minegocio.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.ComprobantePdf;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.List;

public class VentaDetalleActivity extends BaseActivity {

    private static final int PEDIR_PDF = 701;

    private long id;
    private Modelo.Venta venta;
    private List<Modelo.Item> items;
    private Modelo.Deuda deuda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venta_detalle);
        configurarBarra("Detalle de venta");
        id = idExtra("id");
        if (id <= 0) {
            aviso("No se indicó la venta.");
            finish();
            return;
        }
        findViewById(R.id.btnAbonar).setOnClickListener(v -> {
            if (venta != null && venta.clienteId > 0) {
                Intent i = new Intent(this, PagoActivity.class);
                i.putExtra("cliente_id", venta.clienteId);
                i.putExtra("venta_id", venta.id);
                startActivity(i);
            }
        });
        findViewById(R.id.btnPdf).setOnClickListener(v -> pedirPdf());
        findViewById(R.id.btnCompartir).setOnClickListener(v -> compartir());
        findViewById(R.id.btnAnular).setOnClickListener(v -> anular());
        findViewById(R.id.btnVerComprobantes).setOnClickListener(v -> verAdjuntos("venta", id));
        findViewById(R.id.btnVerCliente).setOnClickListener(v -> {
            if (venta != null && venta.clienteId > 0) {
                Intent i = new Intent(this, ClienteDetalleActivity.class);
                i.putExtra("id", venta.clienteId);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        venta = bd.venta(id);
        if (venta == null) {
            aviso("La venta no existe.");
            finish();
            return;
        }
        items = bd.itemsVenta(id);
        deuda = bd.deudaVenta(id);
        ((TextView) findViewById(R.id.txtTituloVenta)).setText("Venta #" + venta.id);
        ((TextView) findViewById(R.id.txtFechaVenta)).setText(Fechas.fechaHora(venta.fecha));
        TextView badge = findViewById(R.id.badgeVenta);
        Ui.ponerBadge(badge, Ui.estadoVenta(venta));
        findViewById(R.id.txtAvisoAnulada).setVisibility(venta.anulada ? View.VISIBLE : View.GONE);

        Button cliente = findViewById(R.id.btnVerCliente);
        if (venta.clienteId > 0) {
            cliente.setText("Cliente: " + venta.clienteNombre + "  ›");
            cliente.setEnabled(true);
            cliente.setContentDescription("Cliente " + venta.clienteNombre
                    + ". Toca para ver su ficha.");
        } else {
            cliente.setText("Consumidor final (sin cliente)");
            cliente.setEnabled(false);
            cliente.setContentDescription("Consumidor final, sin cliente asociado.");
        }
        ((TextView) findViewById(R.id.txtFormaPago)).setText("Forma de pago: " + venta.formaPago);
        TextView usuario = findViewById(R.id.txtUsuario);
        usuario.setText(venta.usuarioNombre.isEmpty() ? "" : "Atendió: " + venta.usuarioNombre);
        usuario.setVisibility(venta.usuarioNombre.isEmpty() ? View.GONE : View.VISIBLE);

        TextView vencimiento = findViewById(R.id.txtVencimiento);
        TextView saldoFiado = findViewById(R.id.txtSaldoFiado);
        Button abonar = findViewById(R.id.btnAbonar);
        if (deuda != null && deuda.saldo > 0) {
            vencimiento.setVisibility(View.VISIBLE);
            vencimiento.setText(deuda.fechaLimite > 0
                    ? "Vence: " + Fechas.fecha(deuda.fechaLimite) + " · "
                    + Fechas.vencimientoTexto(deuda.fechaLimite)
                    : "Sin fecha límite de pago");
            saldoFiado.setVisibility(View.VISIBLE);
            saldoFiado.setText("Total fiado " + Formatos.pesos(deuda.total)
                    + " · abonado " + Formatos.pesos(deuda.abonado)
                    + " · saldo " + Formatos.pesos(deuda.saldo));
            saldoFiado.setContentDescription("Deuda: total " + Formatos.pesosHablado(deuda.total)
                    + ", abonado " + Formatos.pesosHablado(deuda.abonado)
                    + ", saldo " + Formatos.pesosHablado(deuda.saldo));
            abonar.setVisibility(View.VISIBLE);
            abonar.setContentDescription("Registrar abono a la venta fiada. Saldo "
                    + Formatos.pesosHablado(deuda.saldo));
        } else {
            vencimiento.setVisibility(View.GONE);
            saldoFiado.setVisibility(View.GONE);
            abonar.setVisibility(View.GONE);
        }

        LinearLayout contenedor = findViewById(R.id.containerItems);
        contenedor.removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) contenedor.addView(Ui.separador(this));
            contenedor.addView(filaItem(items.get(i)));
        }

        ((TextView) findViewById(R.id.txtSubtotal)).setText(Formatos.pesos(venta.subtotal));
        ((TextView) findViewById(R.id.txtDescuento)).setText(Formatos.pesos(venta.descuento));
        ((TextView) findViewById(R.id.txtImpuesto)).setText(Formatos.pesos(venta.impuesto));
        TextView lbl = findViewById(R.id.lblImpuesto);
        lbl.setText(venta.impuesto > 0
                ? "Impuesto (" + Formatos.porcentaje(venta.impuestoPct) + ")"
                : "Impuesto (no cobrado)");
        long base = Math.max(0, venta.subtotal - venta.descuento);
        long ganancia = base - venta.costo;
        long margen = base > 0 ? Math.round(ganancia * 100.0 / base) : 0;
        ((TextView) findViewById(R.id.txtCosto)).setText(Formatos.pesos(venta.costo));
        ((TextView) findViewById(R.id.txtGanancia)).setText(Formatos.pesos(ganancia));
        ((TextView) findViewById(R.id.txtMargen)).setText(margen + "%");
        TextView total = findViewById(R.id.txtTotal);
        total.setText(Formatos.pesos(venta.total));
        total.setContentDescription("Total: " + Formatos.pesosHablado(venta.total));

        View cardNotas = findViewById(R.id.cardNotas);
        if (venta.notas == null || venta.notas.isEmpty()) {
            cardNotas.setVisibility(View.GONE);
        } else {
            cardNotas.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.txtNotas)).setText(venta.notas);
        }

        List<Modelo.Adjunto> comprobantes = bd.adjuntos("venta", id);
        View cardComprobantes = findViewById(R.id.cardComprobantes);
        if (comprobantes.isEmpty()) {
            cardComprobantes.setVisibility(View.GONE);
        } else {
            cardComprobantes.setVisibility(View.VISIBLE);
            Button ver = findViewById(R.id.btnVerComprobantes);
            ver.setText(comprobantes.size() == 1 ? "Ver comprobante"
                    : "Ver comprobantes (" + comprobantes.size() + ")");
            ver.setContentDescription("Ver " + comprobantes.size() + " comprobante(s) de la venta");
        }
        findViewById(R.id.btnAnular).setVisibility(venta.anulada ? View.GONE : View.VISIBLE);
    }

    private View filaItem(Modelo.Item i) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setPadding(0, dp(8), 0, dp(8));
        fila.setMinimumHeight(dp(56));

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        textos.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView t = Ui.texto(this, i.descripcion, 17, getColor(R.color.texto), true);
        TextView s = Ui.texto(this, i.cantidad + " × " + Formatos.pesos(i.precioUnitario), 15,
                getColor(R.color.texto_secundario), false);
        textos.addView(t);
        textos.addView(s);
        fila.addView(textos);

        TextView total = Ui.texto(this, Formatos.pesos(i.precioUnitario * i.cantidad), 17,
                getColor(R.color.texto), true);
        fila.addView(total);
        fila.setContentDescription(i.descripcion + ", " + i.cantidad + " por "
                + Formatos.pesosHablado(i.precioUnitario) + ", subtotal "
                + Formatos.pesosHablado(i.precioUnitario * i.cantidad));
        return fila;
    }

    private void pedirPdf() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/pdf");
        i.putExtra(Intent.EXTRA_TITLE, "comprobante-venta-" + id + ".pdf");
        try {
            startActivityForResult(i, PEDIR_PDF);
        } catch (ActivityNotFoundException e) {
            aviso("No hay una aplicación para guardar el PDF en este dispositivo.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PEDIR_PDF && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            Uri destino = data.getData();
            try {
                ComprobantePdf.generar(this, venta, items, destino);
                aviso("Comprobante PDF guardado.");
            } catch (Exception e) {
                informar("No se pudo guardar el PDF",
                        "Ocurrió un problema: " + e.getMessage());
            }
        }
    }

    private void compartir() {
        StringBuilder sb = new StringBuilder();
        sb.append(bd.nombreNegocio()).append("\n");
        String nit = bd.ajuste("negocio_nit", "");
        if (!nit.isEmpty()) sb.append("NIT: ").append(nit).append("\n");
        sb.append("\nVenta #").append(venta.id).append(" · ")
                .append(Fechas.fechaHora(venta.fecha)).append("\n");
        sb.append("Cliente: ").append(venta.clienteNombre.isEmpty()
                ? "Consumidor final" : venta.clienteNombre).append("\n\n");
        for (Modelo.Item i : items) {
            sb.append(i.cantidad).append(" × ").append(i.descripcion).append(" — ")
                    .append(Formatos.pesos(i.precioUnitario * i.cantidad)).append("\n");
        }
        sb.append("\nSubtotal: ").append(Formatos.pesos(venta.subtotal)).append("\n");
        if (venta.descuento > 0) {
            sb.append("Descuento: -").append(Formatos.pesos(venta.descuento)).append("\n");
        }
        if (venta.impuesto > 0) {
            sb.append("Impuesto (").append(Formatos.porcentaje(venta.impuestoPct)).append("): ")
                    .append(Formatos.pesos(venta.impuesto)).append("\n");
        }
        sb.append("TOTAL: ").append(Formatos.pesos(venta.total)).append("\n");
        sb.append("Pago: ").append(venta.formaPago).append("\n");
        if (venta.anulada) sb.append("\n(VENTA ANULADA)\n");
        sb.append("\nGracias por tu compra.");
        Intent enviar = new Intent(Intent.ACTION_SEND);
        enviar.setType("text/plain");
        enviar.putExtra(Intent.EXTRA_SUBJECT, "Comprobante de venta #" + venta.id);
        enviar.putExtra(Intent.EXTRA_TEXT, sb.toString());
        try {
            startActivity(Intent.createChooser(enviar, "Compartir comprobante"));
        } catch (ActivityNotFoundException e) {
            aviso("No hay una aplicación para compartir en este dispositivo.");
        }
    }

    private void anular() {
        confirmar("Anular venta",
                "Se devolverán las existencias al inventario y la venta quedará marcada como anulada. "
                        + "No se borra el historial.",
                "Anular venta", () -> {
                    bd.anularVenta(id);
                    aviso("Venta anulada y existencias devueltas.");
                    cargar();
                });
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
