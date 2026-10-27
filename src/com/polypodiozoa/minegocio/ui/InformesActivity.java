package com.polypodiozoa.minegocio.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.ReporteExcel;
import com.polypodiozoa.minegocio.util.Ui;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class InformesActivity extends BaseActivity {

    private static final int PEDIR_CSV = 1001;
    private static final int PEDIR_XLSX = 1002;
    private static final int[] PERIODOS = {Fechas.HOY, Fechas.SEMANA, Fechas.MES,
            Fechas.MES_ANTERIOR, Fechas.ANIO, Fechas.TODO};

    private Spinner spPeriodo;
    private LinearLayout containerMetricas, containerTop, containerFormas;
    private TextView txtVacioTop, txtVacioFormas;
    private Modelo.Resumen resumenActual;
    private long[] rangoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informes);
        configurarBarra("Informes");
        spPeriodo = findViewById(R.id.spPeriodo);
        containerMetricas = findViewById(R.id.containerMetricas);
        containerTop = findViewById(R.id.containerTop);
        containerFormas = findViewById(R.id.containerFormas);
        txtVacioTop = findViewById(R.id.txtVacioTop);
        txtVacioFormas = findViewById(R.id.txtVacioFormas);

        String[] opciones = {"Hoy", "Esta semana", "Este mes", "Mes anterior", "Este año",
                "Todo el tiempo"};
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, R.layout.item_spinner, opciones);
        adaptador.setDropDownViewResource(R.layout.item_spinner);
        spPeriodo.setAdapter(adaptador);
        spPeriodo.setSelection(2);
        spPeriodo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int posicion, long id) {
                cargar();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        findViewById(R.id.btnExcel).setOnClickListener(v -> pedirExcel());
        findViewById(R.id.btnCsv).setOnClickListener(v -> pedirCsv());
        findViewById(R.id.btnCompartir).setOnClickListener(v -> compartir());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private int periodo() {
        int p = spPeriodo.getSelectedItemPosition();
        return PERIODOS[Math.max(0, Math.min(PERIODOS.length - 1, p))];
    }

    private void cargar() {
        rangoActual = Fechas.rango(periodo());
        resumenActual = bd.resumen(rangoActual[0], rangoActual[1]);
        containerMetricas.removeAllViews();
        agregarMetrica("Ventas realizadas", String.valueOf(resumenActual.ventas));
        agregarMetrica("Ingreso", Formatos.pesos(resumenActual.ventasTotal));
        agregarMetrica("Costo de lo vendido", Formatos.pesos(resumenActual.costoVentas));
        agregarMetrica("Utilidad bruta", Formatos.pesos(resumenActual.utilidadBruta));
        agregarMetrica("Impuestos cobrados en ventas", Formatos.pesos(resumenActual.impuestoVentas));
        agregarMetrica("Gastos", Formatos.pesos(resumenActual.gastosTotal));
        agregarMetrica("Utilidad neta", Formatos.pesos(resumenActual.utilidadNeta));
        agregarMetrica("Compras", Formatos.pesos(resumenActual.comprasTotal));
        agregarMetrica("Impuestos pagados en compras", Formatos.pesos(resumenActual.impuestoCompras));
        agregarMetrica("Por cobrar (total histórico)", Formatos.pesos(resumenActual.porCobrar));
        agregarMetrica("Deuda vencida", Formatos.pesos(resumenActual.deudaVencida));
        agregarMetrica("Por vencer en 7 días", Formatos.pesos(resumenActual.deudaPorVencer));
        agregarMetrica("Deudas abiertas", String.valueOf(resumenActual.deudasAbiertas));

        cargarTop();
        cargarFormas();
    }

    private void agregarMetrica(String etiqueta, String valor) {
        if (containerMetricas.getChildCount() > 0) containerMetricas.addView(Ui.separador(this));
        containerMetricas.addView(Ui.filaResumen(this, etiqueta, valor));
    }

    private void cargarTop() {
        containerTop.removeAllViews();
        List<Modelo.TopProducto> top = bd.topProductos(rangoActual[0], rangoActual[1], 5);
        txtVacioTop.setVisibility(top.isEmpty() ? View.VISIBLE : View.GONE);
        for (int i = 0; i < top.size(); i++) {
            if (i > 0) containerTop.addView(Ui.separador(this));
            Modelo.TopProducto t = top.get(i);
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.VERTICAL);
            fila.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
            TextView titulo = Ui.texto(this, (i + 1) + ". " + t.descripcion, 17,
                    getColor(R.color.texto), true);
            long margen = t.total > 0 ? Math.round(t.utilidad * 100.0 / t.total) : 0;
            TextView detalle = Ui.texto(this, t.cantidad + " vendidos · "
                            + Formatos.pesos(t.total) + " · gana " + Formatos.pesos(t.utilidad)
                            + " (" + margen + "%)",
                    15, getColor(R.color.texto_secundario), false);
            fila.addView(titulo);
            fila.addView(detalle);
            fila.setContentDescription(t.descripcion + ", " + t.cantidad
                    + " vendidos, total " + Formatos.pesosHablado(t.total));
            containerTop.addView(fila);
        }
    }

    private void cargarFormas() {
        containerFormas.removeAllViews();
        List<Modelo.TotalFormaPago> formas = bd.formasPago(rangoActual[0], rangoActual[1]);
        txtVacioFormas.setVisibility(formas.isEmpty() ? View.VISIBLE : View.GONE);
        for (int i = 0; i < formas.size(); i++) {
            if (i > 0) containerFormas.addView(Ui.separador(this));
            Modelo.TotalFormaPago f = formas.get(i);
            containerFormas.addView(Ui.filaResumen(this,
                    f.formaPago + " (" + f.cantidad + ")", Formatos.pesos(f.total)));
        }
    }

    private void pedirExcel() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String nombre = "reporte-" + Fechas.nombrePeriodo(periodo()).toLowerCase()
                .replace(" ", "-").replace("ó", "o") + "-"
                + Fechas.formato("yyyyMMdd", Fechas.ahora()) + ".xlsx";
        i.putExtra(Intent.EXTRA_TITLE, nombre);
        try {
            startActivityForResult(i, PEDIR_XLSX);
        } catch (ActivityNotFoundException e) {
            aviso("No hay una aplicación para guardar el archivo en este dispositivo.");
        }
    }

    private void pedirCsv() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("text/csv");
        String nombre = "ventas-" + Fechas.nombrePeriodo(periodo()).toLowerCase()
                .replace(" ", "-").replace("ó", "o") + "-"
                + Fechas.formato("yyyyMMdd", Fechas.ahora()) + ".csv";
        i.putExtra(Intent.EXTRA_TITLE, nombre);
        try {
            startActivityForResult(i, PEDIR_CSV);
        } catch (ActivityNotFoundException e) {
            aviso("No hay una aplicación para guardar el archivo en este dispositivo.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PEDIR_CSV && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            exportarCsv(data.getData());
        } else if (requestCode == PEDIR_XLSX && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            exportarExcel(data.getData());
        }
    }

    /** Tarea que puede fallar: se ejecuta fuera del hilo de la interfaz. */
    private interface Tarea {
        void ejecutar() throws Exception;
    }

    /** Genera el archivo en segundo plano, con aviso de progreso y resultado en la pantalla. */
    private void enSegundoPlano(String mensaje, Tarea tarea, String exito, String tituloError) {
        android.app.ProgressDialog progreso = new android.app.ProgressDialog(this);
        progreso.setMessage(mensaje);
        progreso.setCancelable(false);
        progreso.show();
        new Thread(() -> {
            String fallo = null;
            try {
                tarea.ejecutar();
            } catch (Exception e) {
                fallo = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            }
            final String error = fallo;
            runOnUiThread(() -> {
                if (isFinishing()) return;
                try { progreso.dismiss(); } catch (Exception ignored) { }
                if (error == null) aviso(exito);
                else informar(tituloError, "Ocurrió un problema: " + error);
            });
        }).start();
    }

    private void exportarCsv(Uri destino) {
        enSegundoPlano("Generando el archivo CSV…", () -> escribirCsv(destino),
                "Archivo CSV guardado con las ventas del período.", "No se pudo exportar");
    }

    private void escribirCsv(Uri destino) throws Exception {
        OutputStream salida = getContentResolver().openOutputStream(destino, "w");
        if (salida == null) throw new Exception("No se pudo abrir el destino.");
        try (Writer w = new OutputStreamWriter(salida, StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            w.write("Venta;Fecha;Cliente;Forma de pago;Estado;Subtotal;Descuento;Impuesto;Total;Costo;Utilidad;Productos\n");
            List<Modelo.Venta> ventas = bd.ventas("");
            for (Modelo.Venta v : ventas) {
                if (v.fecha < rangoActual[0] || v.fecha >= rangoActual[1]) continue;
                StringBuilder productos = new StringBuilder();
                for (Modelo.Item i : bd.itemsVenta(v.id)) {
                    if (productos.length() > 0) productos.append(" | ");
                    productos.append(i.cantidad).append(" x ").append(i.descripcion);
                }
                long utilidad = v.total - v.impuesto - v.costo;
                w.write(v.id + ";");
                w.write(csv(Fechas.formato("dd/MM/yyyy HH:mm", v.fecha)) + ";");
                w.write(csv(v.clienteNombre.isEmpty() ? "Consumidor final" : v.clienteNombre) + ";");
                w.write(csv(v.formaPago) + ";");
                w.write(csv(Ui.estadoVenta(v)) + ";");
                w.write(v.subtotal + ";" + v.descuento + ";" + v.impuesto + ";");
                w.write(v.total + ";" + v.costo + ";" + utilidad + ";");
                w.write(csv(productos.toString()) + "\n");
            }
        }
    }

    private void exportarExcel(Uri destino) {
        enSegundoPlano("Generando el reporte Excel…", () -> ReporteExcel.generar(
                        this, destino, rangoActual[0], rangoActual[1], Fechas.nombrePeriodo(periodo())),
                "Reporte Excel guardado con ventas, productos, deudas y gastos.",
                "No se pudo generar el Excel");
    }

    private String csv(String texto) {
        if (texto == null) return "";
        /* CWE-1236: Excel ejecuta como fórmula toda celda que empiece por = + - @
           (o tabulación / retorno de carro). El apóstrofo la convierte en texto;
           al importar el CSV el apóstrofo se ve en la celda, y es el precio de
           neutralizar la fórmula (convención OWASP). */
        if (!texto.isEmpty() && "=+-@\t\r".indexOf(texto.charAt(0)) >= 0) {
            texto = "'" + texto;
        }
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    private void compartir() {
        if (resumenActual == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append(bd.nombreNegocio()).append("\n");
        sb.append("Informe: ").append(Fechas.nombrePeriodo(periodo())).append("\n\n");
        sb.append("Ventas: ").append(resumenActual.ventas).append("\n");
        sb.append("Ingresos: ").append(Formatos.pesos(resumenActual.ventasTotal)).append("\n");
        sb.append("Costo de lo vendido: ").append(Formatos.pesos(resumenActual.costoVentas)).append("\n");
        sb.append("Utilidad bruta: ").append(Formatos.pesos(resumenActual.utilidadBruta)).append("\n");
        sb.append("Impuestos cobrados: ").append(Formatos.pesos(resumenActual.impuestoVentas)).append("\n");
        sb.append("Gastos: ").append(Formatos.pesos(resumenActual.gastosTotal)).append("\n");
        sb.append("Utilidad neta: ").append(Formatos.pesos(resumenActual.utilidadNeta)).append("\n");
        sb.append("Por cobrar: ").append(Formatos.pesos(resumenActual.porCobrar)).append("\n");
        Intent enviar = new Intent(Intent.ACTION_SEND);
        enviar.setType("text/plain");
        enviar.putExtra(Intent.EXTRA_SUBJECT, "Informe de " + bd.nombreNegocio());
        enviar.putExtra(Intent.EXTRA_TEXT, sb.toString());
        try {
            startActivity(Intent.createChooser(enviar, "Compartir informe"));
        } catch (ActivityNotFoundException e) {
            aviso("No hay una aplicación para compartir en este dispositivo.");
        }
    }
}
