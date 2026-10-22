package com.polypodiozoa.minegocio.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.List;

/** Apertura, cierre y reporte general de caja. */
public class CajaActivity extends BaseActivity {

    private TextView txtEstado, txtInfo;
    private EditText etMontoInicial;
    private Button btnAbrir, btnCerrar;
    private LinearLayout listaResumen, listaHistorial;
    private Modelo.Caja cajaAbierta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caja);
        configurarBarra("Caja");
        txtEstado = findViewById(R.id.txtEstadoCaja);
        txtInfo = findViewById(R.id.txtInfoCaja);
        etMontoInicial = findViewById(R.id.etMontoInicial);
        btnAbrir = findViewById(R.id.btnAbrirCaja);
        btnCerrar = findViewById(R.id.btnCerrarCaja);
        listaResumen = findViewById(R.id.listaResumenCaja);
        listaHistorial = findViewById(R.id.listaHistorialCaja);
        btnAbrir.setOnClickListener(v -> abrir());
        btnCerrar.setOnClickListener(v -> pedirCierre());
        cargar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        cajaAbierta = bd.cajaAbierta();
        listaResumen.removeAllViews();
        listaHistorial.removeAllViews();
        if (cajaAbierta != null) {
            txtEstado.setText("Estado actual: Abierta");
            txtInfo.setText("Abierta el " + Fechas.fechaHora(cajaAbierta.apertura)
                    + " por " + cajaAbierta.usuarioNombre + ".\nEfectivo inicial: "
                    + Formatos.pesos(cajaAbierta.montoInicial) + ".");
            etMontoInicial.setVisibility(View.GONE);
            btnAbrir.setVisibility(View.GONE);
            btnCerrar.setVisibility(View.VISIBLE);
            Modelo.ResumenCaja r = bd.resumenCaja(cajaAbierta.apertura, Fechas.ahora(),
                    cajaAbierta.montoInicial);
            mostrarResumen(r, cajaAbierta, false);
        } else {
            txtEstado.setText("Estado actual: Cerrada");
            List<Modelo.Caja> cerradas = bd.cajasCerradas(5);
            Modelo.Caja ultima = cerradas.isEmpty() ? null : cerradas.get(0);
            if (ultima != null) {
                txtInfo.setText("Último cierre: " + Fechas.fechaHora(ultima.cierre)
                        + ". Abre una caja nueva para empezar otro turno.");
            } else {
                txtInfo.setText("No hay una caja abierta. Escribe el efectivo inicial y abre la caja.");
            }
            etMontoInicial.setVisibility(View.VISIBLE);
            etMontoInicial.setText("0");
            btnAbrir.setVisibility(View.VISIBLE);
            btnCerrar.setVisibility(View.GONE);
            if (ultima != null) {
                Modelo.ResumenCaja r = bd.resumenCaja(ultima.apertura, ultima.cierre,
                        ultima.montoInicial);
                mostrarResumen(r, ultima, true);
            } else {
                agregarAviso("Aún no hay cierres. Abre la caja para comenzar.");
            }
            mostrarHistorial(cerradas);
        }
    }

    private void abrir() {
        long inicial = numero(etMontoInicial);
        if (inicial < 0) {
            error(etMontoInicial, "Escribe un monto inicial válido.");
            return;
        }
        if (bd.abrirCaja(inicial, bd.usuarioActualId())) {
            aviso("Caja abierta.");
            cargar();
        } else {
            aviso("Ya hay una caja abierta.");
        }
    }

    private void pedirCierre() {
        if (cajaAbierta == null) return;
        Modelo.ResumenCaja r = bd.resumenCaja(cajaAbierta.apertura, Fechas.ahora(),
                cajaAbierta.montoInicial);
        int p = Ui.dp(this, 14);
        EditText contado = new EditText(this);
        contado.setText(String.valueOf(r.efectivoEsperado));
        contado.setTextSize(18);
        contado.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        contado.setPadding(p, p, p, p);
        contado.setBackgroundResource(R.drawable.fondo_campo);

        EditText notas = new EditText(this);
        notas.setHint("Notas del cierre (opcional)");
        notas.setTextSize(16);
        notas.setPadding(p, p, p, p);
        notas.setBackgroundResource(R.drawable.fondo_campo);

        LinearLayout caja = new LinearLayout(this);
        caja.setOrientation(LinearLayout.VERTICAL);
        caja.setPadding(p, p, p, 0);
        TextView esperado = Ui.texto(this, "Efectivo esperado: "
                + Formatos.pesos(r.efectivoEsperado), 17,
                getColor(R.color.texto), true);
        esperado.setPadding(0, 0, 0, p / 2);
        caja.addView(esperado);
        caja.addView(Ui.texto(this, "Efectivo contado", 15,
                getColor(R.color.texto_secundario), false));
        caja.addView(contado);
        caja.addView(Ui.texto(this, "Notas", 15,
                getColor(R.color.texto_secundario), false));
        caja.addView(notas);

        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Cerrar caja")
                .setView(caja)
                .setPositiveButton("Cerrar caja", null)
                .setNegativeButton("Cancelar", null)
                .create();
        d.setOnShowListener(x -> {
            agrandarDialogo(d);
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (contado.getText() == null || contado.getText().toString().trim().isEmpty()) {
                    error(contado, "Escribe el efectivo contado.");
                    return;
                }
                long valor = numero(contado);
                if (valor < 0) {
                    error(contado, "Escribe el efectivo contado.");
                    return;
                }
                long diferencia = valor - r.efectivoEsperado;
                String texto = "¿Cerrar la caja con " + Formatos.pesos(valor) + "?\n\n"
                        + "Diferencia: " + Formatos.pesos(diferencia)
                        + (diferencia == 0 ? " (cuadra)" : diferencia > 0
                        ? " (sobrante)" : " (faltante)");
                new AlertDialog.Builder(this)
                        .setTitle("Confirmar cierre")
                        .setMessage(texto)
                        .setPositiveButton("Confirmar", (cd, w) -> {
                            if (bd.cerrarCaja(cajaAbierta.id, valor, texto(notas))) {
                                d.dismiss();
                                aviso("Caja cerrada.");
                                cargar();
                            } else {
                                aviso("No se pudo cerrar la caja.");
                            }
                        })
                        .setNegativeButton("Volver", null)
                        .show();
            });
        });
        d.show();
    }

    private void mostrarResumen(Modelo.ResumenCaja r, Modelo.Caja caja, boolean cerrada) {
        long ventasOtros = Math.max(0, r.ventasTotal - r.ventasEfectivo);
        long pagosOtros = Math.max(0, r.pagosTotal - r.pagosEfectivo);
        long comprasOtros = Math.max(0, r.comprasTotal - r.comprasEfectivo);
        long gastosOtros = Math.max(0, r.gastosTotal - r.gastosEfectivo);

        agregarFila("Ventas totales", r.ventasCantidad + " · " + Formatos.pesos(r.ventasTotal));
        agregarFila("Ventas en efectivo", Formatos.pesos(r.ventasEfectivo));
        agregarFila("Ventas con otros métodos", Formatos.pesos(ventasOtros));
        agregarFila("Pagos en efectivo (abonos)", Formatos.pesos(r.pagosEfectivo));
        agregarFila("Pagos con otros métodos", Formatos.pesos(pagosOtros));
        agregarFila("Compras totales", Formatos.pesos(r.comprasTotal));
        agregarFila("Compras en efectivo", Formatos.pesos(r.comprasEfectivo));
        agregarFila("Compras con otros métodos", Formatos.pesos(comprasOtros));
        agregarFila("Gastos totales", Formatos.pesos(r.gastosTotal));
        agregarFila("Gastos en efectivo", Formatos.pesos(r.gastosEfectivo));
        agregarFila("Gastos con otros métodos", Formatos.pesos(gastosOtros));
        agregarFila("Efectivo esperado", Formatos.pesos(r.efectivoEsperado));
        if (cerrada && caja != null) {
            agregarFila("Efectivo contado", Formatos.pesos(caja.montoContado));
            String etiqueta = caja.diferencia == 0 ? "Cuadra"
                    : caja.diferencia > 0 ? "Sobrante" : "Faltante";
            agregarFila("Diferencia", Formatos.pesos(caja.diferencia) + " · " + etiqueta);
        }
        TextView nota = Ui.texto(this,
                "Otros métodos incluye transferencia, tarjeta y crédito.", 14,
                getColor(R.color.texto_secundario), false);
        nota.setPadding(0, Ui.dp(this, 8), 0, 0);
        listaResumen.addView(nota);
    }

    private void mostrarHistorial(List<Modelo.Caja> cajas) {
        if (cajas.isEmpty()) {
            listaHistorial.addView(Ui.texto(this, "Todavía no hay cierres.", 16,
                    getColor(R.color.texto_secundario), false));
            return;
        }
        for (Modelo.Caja c : cajas) {
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.VERTICAL);
            fila.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
            TextView titulo = Ui.texto(this,
                    "Cierre " + Fechas.fechaHora(c.cierre), 17,
                    getColor(R.color.texto), true);
            TextView detalle = Ui.texto(this,
                    "Inicial " + Formatos.pesos(c.montoInicial)
                            + " · contado " + Formatos.pesos(c.montoContado)
                            + " · diferencia " + Formatos.pesos(c.diferencia), 15,
                    getColor(R.color.texto_secundario), false);
            detalle.setPadding(0, Ui.dp(this, 3), 0, 0);
            fila.addView(titulo);
            fila.addView(detalle);
            fila.setOnClickListener(v -> verReporte(c));
            fila.setContentDescription("Ver reporte del cierre " + Fechas.fechaHora(c.cierre));
            listaHistorial.addView(fila);
        }
    }

    private void verReporte(Modelo.Caja c) {
        Modelo.ResumenCaja r = bd.resumenCaja(c.apertura, c.cierre, c.montoInicial);
        StringBuilder s = new StringBuilder();
        s.append("Ventas totales: ").append(r.ventasCantidad).append(" · ")
                .append(Formatos.pesos(r.ventasTotal)).append("\n");
        s.append("Ventas en efectivo: ").append(Formatos.pesos(r.ventasEfectivo)).append("\n");
        s.append("Ventas con otros métodos: ")
                .append(Formatos.pesos(Math.max(0, r.ventasTotal - r.ventasEfectivo))).append("\n");
        s.append("Pagos en efectivo (abonos): ")
                .append(Formatos.pesos(r.pagosEfectivo)).append("\n");
        s.append("Pagos con otros métodos: ")
                .append(Formatos.pesos(Math.max(0, r.pagosTotal - r.pagosEfectivo))).append("\n");
        s.append("Compras totales: ").append(Formatos.pesos(r.comprasTotal)).append("\n");
        s.append("Compras en efectivo: ").append(Formatos.pesos(r.comprasEfectivo)).append("\n");
        s.append("Gastos totales: ").append(Formatos.pesos(r.gastosTotal)).append("\n");
        s.append("Gastos en efectivo: ").append(Formatos.pesos(r.gastosEfectivo)).append("\n");
        s.append("Efectivo esperado: ").append(Formatos.pesos(r.efectivoEsperado)).append("\n");
        s.append("Efectivo contado: ").append(Formatos.pesos(c.montoContado)).append("\n");
        s.append("Diferencia: ").append(Formatos.pesos(c.diferencia));
        new AlertDialog.Builder(this)
                .setTitle("Reporte del cierre")
                .setMessage(s.toString())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void agregarFila(String etiqueta, String valor) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setMinimumHeight(Ui.dp(this, 36));
        TextView l = Ui.texto(this, etiqueta, 16, getColor(R.color.texto_secundario), false);
        l.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView v = Ui.texto(this, valor, 16, getColor(R.color.texto), true);
        v.setGravity(Gravity.END);
        v.setContentDescription(etiqueta + ": " + valor);
        fila.addView(l);
        fila.addView(v);
        listaResumen.addView(fila);
    }

    private void agregarAviso(String texto) {
        listaResumen.addView(Ui.texto(this, texto, 16,
                getColor(R.color.texto_secundario), false));
    }
}
