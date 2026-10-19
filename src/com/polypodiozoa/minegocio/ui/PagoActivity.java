package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Formatos;

import java.util.List;

public class PagoActivity extends BaseActivity {

    private static final int PEDIR_FOTO = 431;
    private static final int PEDIR_ARCHIVO = 432;

    private Modelo.Cliente cliente;
    private long ventaId;
    private Modelo.Deuda deuda;
    private EditText etMonto, etNotas;
    private Spinner spForma;
    private ListaAdjuntos adjuntos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);
        configurarBarra("Registrar abono");
        long clienteId = idExtra("cliente_id");
        ventaId = idExtra("venta_id");
        cliente = bd.cliente(clienteId);
        if (cliente == null) {
            aviso("El cliente no existe.");
            finish();
            return;
        }
        etMonto = findViewById(R.id.etMonto);
        etNotas = findViewById(R.id.etNotas);
        spForma = findViewById(R.id.spForma);
        ((TextView) findViewById(R.id.txtCliente)).setText(cliente.nombre);
        ((TextView) findViewById(R.id.txtCliente)).setContentDescription("Cliente: " + cliente.nombre);
        long saldo = cliente.saldo;
        TextView saldoActual = findViewById(R.id.txtSaldoActual);
        if (ventaId > 0) {
            deuda = bd.deudaVenta(ventaId);
            if (deuda == null || deuda.saldo <= 0) {
                aviso("Esta venta ya no tiene saldo pendiente.");
                finish();
                return;
            }
            saldo = deuda.saldo;
            saldoActual.setText("Venta #" + ventaId + " · saldo " + Formatos.pesos(saldo)
                    + (deuda.fechaLimite > 0
                    ? " · vence " + com.polypodiozoa.minegocio.util.Fechas.fecha(deuda.fechaLimite) : ""));
            ((TextView) findViewById(R.id.txtCliente)).setText(cliente.nombre
                    + " · venta fiada");
        }
        if (saldo > 0) {
            if (ventaId <= 0) saldoActual.setText("Saldo pendiente: " + Formatos.pesos(saldo));
            etMonto.setText(String.valueOf(saldo));
        } else if (saldo < 0) {
            saldoActual.setText("Saldo a favor: " + Formatos.pesos(-saldo));
        } else {
            saldoActual.setText("Este cliente está al día.");
        }

        String[] formas = {"Efectivo", "Transferencia", "Nequi / Daviplata", "Tarjeta"};
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, R.layout.item_spinner, formas);
        adaptador.setDropDownViewResource(R.layout.item_spinner);
        spForma.setAdapter(adaptador);

        adjuntos = new ListaAdjuntos(this, findViewById(R.id.containerAdjuntos));
        findViewById(R.id.btnFoto).setOnClickListener(v -> tomarFoto(PEDIR_FOTO));
        findViewById(R.id.btnArchivo).setOnClickListener(v -> elegirArchivo(PEDIR_ARCHIVO));
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
        marcarInicial(etMonto, etNotas);
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
        if (ruta != null && !ruta.isEmpty()) {
            adjuntos.agregar(crearAdjunto(ruta));
        }
    }

    private void guardar() {
        long monto = numero(etMonto);
        if (monto <= 0) {
            error(etMonto, "Escribe el monto del abono.");
            return;
        }
        Modelo.Pago p = new Modelo.Pago();
        p.clienteId = cliente.id;
        p.fecha = com.polypodiozoa.minegocio.util.Fechas.ahora();
        p.monto = monto;
        p.formaPago = String.valueOf(spForma.getSelectedItem());
        p.notas = texto(etNotas);
        p.ventaId = ventaId;
        List<Modelo.Adjunto> lista = adjuntos.finalizar();
        bd.registrarPago(p, lista);
        aviso("Abono registrado por " + Formatos.pesos(monto) + ".");
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && adjuntos != null) adjuntos.limpiarTemporales();
    }
}
