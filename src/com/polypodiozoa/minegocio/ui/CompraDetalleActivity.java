package com.polypodiozoa.minegocio.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.List;

public class CompraDetalleActivity extends BaseActivity {

    private long id;
    private Modelo.Compra compra;
    private List<Modelo.Item> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compra_detalle);
        configurarBarra("Detalle de compra");
        id = idExtra("id");
        if (id <= 0) {
            aviso("No se indicó la compra.");
            finish();
            return;
        }
        findViewById(R.id.btnVerComprobantes).setOnClickListener(v -> verAdjuntos("compra", id));
        findViewById(R.id.btnCompartir).setOnClickListener(v -> compartir());
        findViewById(R.id.btnAnular).setOnClickListener(v -> anular());
        findViewById(R.id.btnVerProveedor).setOnClickListener(v -> {
            if (compra != null && compra.proveedorId > 0) {
                Intent i = new Intent(this, ProveedorDetalleActivity.class);
                i.putExtra("id", compra.proveedorId);
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
        compra = bd.compra(id);
        if (compra == null) {
            aviso("La compra no existe.");
            finish();
            return;
        }
        items = bd.itemsCompra(id);
        ((TextView) findViewById(R.id.txtTituloCompra)).setText("Compra #" + compra.id);
        ((TextView) findViewById(R.id.txtFechaCompra)).setText(Fechas.fechaHora(compra.fecha));
        Ui.ponerBadge(findViewById(R.id.badgeCompra), Ui.estadoCompra(compra));
        findViewById(R.id.txtAvisoAnulada).setVisibility(compra.anulada ? View.VISIBLE : View.GONE);

        Button proveedor = findViewById(R.id.btnVerProveedor);
        if (compra.proveedorId > 0) {
            proveedor.setText("Proveedor: " + compra.proveedorNombre + "  ›");
            proveedor.setEnabled(true);
            proveedor.setContentDescription("Proveedor " + compra.proveedorNombre
                    + ". Toca para ver su ficha.");
        } else {
            proveedor.setText("Sin proveedor asociado");
            proveedor.setEnabled(false);
            proveedor.setContentDescription("Sin proveedor asociado.");
        }
        ((TextView) findViewById(R.id.txtFormaPago)).setText("Forma de pago: " + compra.formaPago);

        LinearLayout contenedor = findViewById(R.id.containerItems);
        contenedor.removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) contenedor.addView(Ui.separador(this));
            contenedor.addView(filaItem(items.get(i)));
        }

        ((TextView) findViewById(R.id.txtSubtotal)).setText(Formatos.pesos(compra.subtotal));
        ((TextView) findViewById(R.id.txtImpuesto)).setText(Formatos.pesos(compra.impuesto));
        TextView lbl = findViewById(R.id.lblImpuesto);
        lbl.setText(compra.impuesto > 0
                ? "Impuesto (" + Formatos.porcentaje(compra.impuestoPct) + ")"
                : "Impuesto (no incluido)");
        TextView total = findViewById(R.id.txtTotal);
        total.setText(Formatos.pesos(compra.total));
        total.setContentDescription("Total: " + Formatos.pesosHablado(compra.total));

        View cardNotas = findViewById(R.id.cardNotas);
        if (compra.notas == null || compra.notas.isEmpty()) {
            cardNotas.setVisibility(View.GONE);
        } else {
            cardNotas.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.txtNotas)).setText(compra.notas);
        }
        List<Modelo.Adjunto> comprobantes = bd.adjuntos("compra", id);
        View cardComprobantes = findViewById(R.id.cardComprobantes);
        if (comprobantes.isEmpty()) {
            cardComprobantes.setVisibility(View.GONE);
        } else {
            cardComprobantes.setVisibility(View.VISIBLE);
            Button ver = findViewById(R.id.btnVerComprobantes);
            ver.setText(comprobantes.size() == 1 ? "Ver comprobante"
                    : "Ver comprobantes (" + comprobantes.size() + ")");
            ver.setContentDescription("Ver " + comprobantes.size() + " comprobante(s) de la compra");
        }
        findViewById(R.id.btnAnular).setVisibility(compra.anulada ? View.GONE : View.VISIBLE);
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
        textos.addView(Ui.texto(this, i.descripcion, 17, getColor(R.color.texto), true));
        textos.addView(Ui.texto(this, i.cantidad + " × " + Formatos.pesos(i.costoUnitario), 15,
                getColor(R.color.texto_secundario), false));
        fila.addView(textos);
        fila.addView(Ui.texto(this, Formatos.pesos(i.costoUnitario * i.cantidad), 17,
                getColor(R.color.texto), true));
        fila.setContentDescription(i.descripcion + ", " + i.cantidad + " por "
                + Formatos.pesosHablado(i.costoUnitario) + ", subtotal "
                + Formatos.pesosHablado(i.costoUnitario * i.cantidad));
        return fila;
    }

    private void compartir() {
        StringBuilder sb = new StringBuilder();
        sb.append(bd.nombreNegocio()).append("\n\nCompra #").append(compra.id).append(" · ")
                .append(Fechas.fechaHora(compra.fecha)).append("\n");
        sb.append("Proveedor: ").append(compra.proveedorNombre.isEmpty()
                ? "Sin proveedor" : compra.proveedorNombre).append("\n\n");
        for (Modelo.Item i : items) {
            sb.append(i.cantidad).append(" × ").append(i.descripcion).append(" — ")
                    .append(Formatos.pesos(i.costoUnitario * i.cantidad)).append("\n");
        }
        sb.append("\nSubtotal: ").append(Formatos.pesos(compra.subtotal)).append("\n");
        if (compra.impuesto > 0) {
            sb.append("Impuesto (").append(Formatos.porcentaje(compra.impuestoPct)).append("): ")
                    .append(Formatos.pesos(compra.impuesto)).append("\n");
        }
        sb.append("TOTAL: ").append(Formatos.pesos(compra.total)).append("\n");
        sb.append("Pago: ").append(compra.formaPago).append("\n");
        if (compra.anulada) sb.append("\n(COMPRA ANULADA)\n");
        Intent enviar = new Intent(Intent.ACTION_SEND);
        enviar.setType("text/plain");
        enviar.putExtra(Intent.EXTRA_SUBJECT, "Resumen de compra #" + compra.id);
        enviar.putExtra(Intent.EXTRA_TEXT, sb.toString());
        try {
            startActivity(Intent.createChooser(enviar, "Compartir resumen"));
        } catch (ActivityNotFoundException e) {
            aviso("No hay una aplicación para compartir en esta tablet.");
        }
    }

    private void anular() {
        confirmar("Anular compra",
                "Se descontarán del inventario las existencias que sumó esta compra. "
                        + "La compra quedará marcada como anulada.",
                "Anular compra", () -> {
                    bd.anularCompra(id);
                    aviso("Compra anulada y existencias ajustadas.");
                    cargar();
                });
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
