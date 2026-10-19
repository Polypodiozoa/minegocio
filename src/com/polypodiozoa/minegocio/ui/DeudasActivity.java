package com.polypodiozoa.minegocio.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.ReporteExcel;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.ArrayList;
import java.util.List;

/** Cartera de ventas fiadas: saldos, cliente, vencimiento y estado. */
public class DeudasActivity extends BaseActivity {

    private static final int PEDIR_XLSX = 1101;
    private static final int TODAS = 0;
    private static final int VENCIDAS = 1;
    private static final int POR_VENCER = 2;

    private final List<Modelo.Deuda> deudas = new ArrayList<>();
    private AdaptadorFilas adaptador;
    private TextView txtVacio;
    private int filtro = TODAS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deudas);
        configurarBarra("Deudas");
        txtVacio = findViewById(R.id.txtVacio);
        ListView lista = findViewById(R.id.lista);
        adaptador = new AdaptadorFilas(this);
        adaptador.setOnFilaClick(f -> {
            Modelo.Deuda d = (Modelo.Deuda) f.dato;
            Intent i = new Intent(this, VentaDetalleActivity.class);
            i.putExtra("id", d.ventaId);
            startActivity(i);
        });
        lista.setAdapter(adaptador);
        lista.setEmptyView(txtVacio);

        findViewById(R.id.btnTodas).setOnClickListener(v -> cambiarFiltro(TODAS));
        findViewById(R.id.btnVencidas).setOnClickListener(v -> cambiarFiltro(VENCIDAS));
        findViewById(R.id.btnPorVencer).setOnClickListener(v -> cambiarFiltro(POR_VENCER));
        findViewById(R.id.btnExcel).setOnClickListener(v -> pedirExcel());
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
        deudas.clear();
        deudas.addAll(bd.deudas());
        long total = 0;
        long vencido = 0;
        long porVencer = 0;
        int nVencidas = 0;
        int nPorVencer = 0;
        for (Modelo.Deuda d : deudas) {
            total += d.saldo;
            int dias = Fechas.diasHasta(d.fechaLimite);
            if (d.fechaLimite > 0 && dias < 0) {
                vencido += d.saldo;
                nVencidas++;
            } else if (d.fechaLimite > 0 && dias <= 7) {
                porVencer += d.saldo;
                nPorVencer++;
            }
        }

        LinearLayout resumen = findViewById(R.id.containerResumen);
        resumen.removeAllViews();
        resumen.addView(Ui.filaResumen(this, "Total por cobrar", Formatos.pesos(total)));
        resumen.addView(Ui.separador(this));
        resumen.addView(Ui.filaResumen(this, "Vencido (" + nVencidas + ")", Formatos.pesos(vencido)));
        resumen.addView(Ui.separador(this));
        resumen.addView(Ui.filaResumen(this, "Por vencer en 7 días (" + nPorVencer + ")",
                Formatos.pesos(porVencer)));

        Button bTodas = findViewById(R.id.btnTodas);
        Button bVencidas = findViewById(R.id.btnVencidas);
        Button bPorVencer = findViewById(R.id.btnPorVencer);
        bTodas.setText("Todas (" + deudas.size() + ")");
        bVencidas.setText("Vencidas (" + nVencidas + ")");
        bPorVencer.setText("Por vencer (" + nPorVencer + ")");
        Ui.estiloFiltro(bTodas, filtro == TODAS);
        Ui.estiloFiltro(bVencidas, filtro == VENCIDAS);
        Ui.estiloFiltro(bPorVencer, filtro == POR_VENCER);

        List<AdaptadorFilas.Fila> filas = new ArrayList<>();
        for (Modelo.Deuda d : deudas) {
            int dias = Fechas.diasHasta(d.fechaLimite);
            boolean vencida = d.fechaLimite > 0 && dias < 0;
            boolean proxima = d.fechaLimite > 0 && dias >= 0 && dias <= 7;
            if (filtro == VENCIDAS && !vencida) continue;
            if (filtro == POR_VENCER && !proxima) continue;
            AdaptadorFilas.Fila f = new AdaptadorFilas.Fila();
            f.id = d.ventaId;
            f.titulo = (d.clienteNombre.isEmpty() ? "Sin cliente" : d.clienteNombre)
                    + " · Venta #" + d.ventaId;
            String vence = d.fechaLimite > 0
                    ? "Vence " + Fechas.fecha(d.fechaLimite) + " · " + Fechas.vencimientoTexto(d.fechaLimite)
                    : "Sin fecha límite";
            f.subtitulo = vence + " · abonado " + Formatos.pesos(d.abonado)
                    + " de " + Formatos.pesos(d.total);
            f.valor = Formatos.pesos(d.saldo);
            f.estado = d.fechaLimite <= 0 ? "Sin fecha" : vencida ? "Vencida"
                    : proxima ? "Por vencer" : "Vigente";
            f.iconoDefecto = R.drawable.ic_dinero;
            f.dato = d;
            f.descripcion = f.titulo + ", " + f.subtitulo + ", saldo "
                    + Formatos.pesosHablado(d.saldo) + ", " + f.estado
                    + ". Toca para ver la venta y abonar.";
            filas.add(f);
        }
        adaptador.setFilas(filas);
        if (filas.isEmpty()) {
            txtVacio.setText(deudas.isEmpty()
                    ? "No hay ventas fiadas pendientes.\n\nCuando registres una venta a crédito, aparecerá aquí."
                    : filtro == VENCIDAS ? "No hay deudas vencidas. ¡Bien!"
                    : filtro == POR_VENCER ? "No hay deudas que venzan en los próximos 7 días."
                    : "No hay deudas en este filtro.");
        }
    }

    private void pedirExcel() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        i.putExtra(Intent.EXTRA_TITLE,
                "deudas-" + Fechas.formato("yyyyMMdd-HHmm", Fechas.ahora()) + ".xlsx");
        try {
            startActivityForResult(i, PEDIR_XLSX);
        } catch (ActivityNotFoundException e) {
            aviso("No hay una aplicación para guardar el archivo en este dispositivo.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PEDIR_XLSX && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            Uri destino = data.getData();
            try {
                ReporteExcel.generarDeudas(this, destino);
                aviso("Reporte de deudas en Excel guardado.");
            } catch (Exception e) {
                informar("No se pudo generar el Excel",
                        "Ocurrió un problema: " + e.getMessage());
            }
        }
    }
}
