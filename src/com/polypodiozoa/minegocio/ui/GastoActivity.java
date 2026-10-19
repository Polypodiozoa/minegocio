package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Archivos;
import com.polypodiozoa.minegocio.util.Fechas;

import java.util.List;

public class GastoActivity extends BaseActivity {

    private static final int PEDIR_FOTO = 901;
    private static final int PEDIR_ARCHIVO = 902;

    private long id;
    private Modelo.Gasto gasto;
    private EditText etDescripcion, etCategoria, etMonto, etNotas;
    private Spinner spForma;
    private ListaAdjuntos adjuntos;
    private boolean guardado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gasto);
        id = idExtra("id");
        configurarBarra(id > 0 ? "Editar gasto" : "Nuevo gasto");
        etDescripcion = findViewById(R.id.etDescripcion);
        etCategoria = findViewById(R.id.etCategoria);
        etMonto = findViewById(R.id.etMonto);
        etNotas = findViewById(R.id.etNotas);
        spForma = findViewById(R.id.spForma);

        String[] formas = {"Efectivo", "Transferencia", "Tarjeta"};
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, R.layout.item_spinner, formas);
        adaptador.setDropDownViewResource(R.layout.item_spinner);
        spForma.setAdapter(adaptador);

        adjuntos = new ListaAdjuntos(this, findViewById(R.id.containerAdjuntos));
        findViewById(R.id.btnFoto).setOnClickListener(v -> tomarFoto(PEDIR_FOTO));
        findViewById(R.id.btnArchivo).setOnClickListener(v -> elegirArchivo(PEDIR_ARCHIVO));
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
        Button eliminar = findViewById(R.id.btnEliminar);
        eliminar.setVisibility(id > 0 ? View.VISIBLE : View.GONE);
        eliminar.setOnClickListener(v -> eliminar());

        if (id > 0) {
            gasto = bd.gasto(id);
            if (gasto == null) {
                aviso("El gasto ya no existe.");
                finish();
                return;
            }
            etDescripcion.setText(gasto.descripcion);
            etCategoria.setText(gasto.categoria);
            etMonto.setText(String.valueOf(gasto.monto));
            etNotas.setText(gasto.notas);
            for (int i = 0; i < formas.length; i++) {
                if (formas[i].equals(gasto.formaPago)) {
                    spForma.setSelection(i);
                    break;
                }
            }
            if (!bd.adjuntos("gasto", id).isEmpty()) {
                Button ver = findViewById(R.id.btnVerGuardados);
                ver.setVisibility(View.VISIBLE);
                ver.setOnClickListener(v -> verAdjuntos("gasto", id));
            }
        }
        marcarInicial(etDescripcion, etCategoria, etMonto, etNotas);
    }

    @Override
    public void onBackPressed() {
        salirSiCambios();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PEDIR_FOTO && requestCode != PEDIR_ARCHIVO) return;
        String ruta = requestCode == PEDIR_FOTO
                ? resultadoFoto(resultCode, data) : resultadoArchivo(resultCode, data);
        if (ruta != null && !ruta.isEmpty()) adjuntos.agregar(crearAdjunto(ruta));
    }

    private void guardar() {
        String descripcion = texto(etDescripcion);
        if (descripcion.isEmpty()) {
            error(etDescripcion, "Escribe la descripción del gasto.");
            return;
        }
        long monto = numero(etMonto);
        if (monto <= 0) {
            error(etMonto, "Escribe un monto mayor que cero.");
            return;
        }
        Modelo.Gasto g = gasto != null ? gasto : new Modelo.Gasto();
        g.descripcion = descripcion;
        g.categoria = texto(etCategoria);
        g.monto = monto;
        g.formaPago = String.valueOf(spForma.getSelectedItem());
        g.notas = texto(etNotas);
        if (g.fecha == 0) g.fecha = Fechas.ahora();
        long guardadoId = bd.guardarGasto(g);
        List<Modelo.Adjunto> archivos = adjuntos.finalizar();
        if (!archivos.isEmpty()) bd.agregarAdjuntos("gasto", guardadoId, archivos);
        guardado = true;
        aviso("Gasto guardado.");
        setResult(RESULT_OK);
        finish();
    }

    private void eliminar() {
        if (gasto == null) return;
        confirmar("Eliminar gasto", "¿Eliminar “" + gasto.descripcion + "”?",
                "Eliminar", () -> {
                    for (Modelo.Adjunto a : bd.adjuntos("gasto", gasto.id)) {
                        Archivos.borrar(a.ruta);
                    }
                    bd.eliminarGasto(gasto.id);
                    guardado = true;
                    aviso("Gasto eliminado.");
                    setResult(RESULT_OK);
                    finish();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && !guardado && adjuntos != null) adjuntos.limpiarTemporales();
    }
}
