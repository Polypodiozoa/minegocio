package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.datos.BD;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class CompraActivity extends BaseActivity {

    private static final int PEDIR_PROVEEDOR = 801;
    private static final int PEDIR_PRODUCTO = 802;
    private static final int PEDIR_FOTO = 803;
    private static final int PEDIR_ARCHIVO = 804;

    private final List<Modelo.Item> items = new ArrayList<>();
    private long proveedorId;
    private String proveedorNombre = "";
    private boolean guardada;
    private boolean actualizando;

    private Button btnProveedor;
    private LinearLayout containerItems;
    private TextView txtVacioItems, txtSubtotal, txtImpuesto, txtTotal, lblImpuesto;
    private Switch swImpuesto;
    private EditText etImpuesto, etNotas;
    private Spinner spForma;
    private ListaAdjuntos adjuntos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compra);
        configurarBarra("Nueva compra");
        btnProveedor = findViewById(R.id.btnProveedor);
        containerItems = findViewById(R.id.containerItems);
        txtVacioItems = findViewById(R.id.txtVacioItems);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtImpuesto = findViewById(R.id.txtImpuesto);
        txtTotal = findViewById(R.id.txtTotal);
        lblImpuesto = findViewById(R.id.lblImpuesto);
        swImpuesto = findViewById(R.id.swImpuesto);
        etImpuesto = findViewById(R.id.etImpuesto);
        etNotas = findViewById(R.id.etNotas);
        spForma = findViewById(R.id.spForma);

        String[] formas = {"Efectivo", "Transferencia", "Nequi / Daviplata", "Tarjeta"};
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, R.layout.item_spinner, formas);
        adaptador.setDropDownViewResource(R.layout.item_spinner);
        spForma.setAdapter(adaptador);

        boolean impuesto = bd.ajusteSi("impuesto_activado", true);
        swImpuesto.setChecked(impuesto);
        etImpuesto.setText(Formatos.decimal(bd.ajusteDecimal("impuesto_pct", 19)));
        etImpuesto.setEnabled(impuesto);
        etImpuesto.setAlpha(impuesto ? 1f : 0.55f);

        adjuntos = new ListaAdjuntos(this, findViewById(R.id.containerAdjuntos));
        btnProveedor.setOnClickListener(v -> elegirProveedor());
        findViewById(R.id.btnAgregarProducto).setOnClickListener(v -> elegirProducto());
        findViewById(R.id.btnConcepto).setOnClickListener(v -> dialogoConcepto());
        findViewById(R.id.btnFoto).setOnClickListener(v -> tomarFoto(PEDIR_FOTO));
        findViewById(R.id.btnArchivo).setOnClickListener(v -> elegirArchivo(PEDIR_ARCHIVO));
        findViewById(R.id.btnAprobar).setOnClickListener(v -> aprobar());
        swImpuesto.setOnCheckedChangeListener((v, marcado) -> {
            etImpuesto.setEnabled(marcado);
            etImpuesto.setAlpha(marcado ? 1f : 0.55f);
            actualizarTotales(false);
        });
        etImpuesto.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { actualizarTotales(false); }
            @Override public void afterTextChanged(Editable s) {}
        });

        long pre = idExtra("proveedor_id");
        if (pre > 0) {
            seleccionarProveedor(pre);
        } else {
            actualizarBotonProveedor();
        }
        renderItems();
        actualizarTotales(false);
    }

    private void elegirProveedor() {
        Intent i = new Intent(this, SelectorActivity.class);
        i.putExtra(SelectorActivity.EXTRA_MODO, SelectorActivity.MODO_PROVEEDOR);
        startActivityForResult(i, PEDIR_PROVEEDOR);
    }

    private void elegirProducto() {
        if (bd.totalProductos() == 0) {
            confirmar("No hay productos",
                    "Todavía no tienes productos en el inventario. Crea uno ahora "
                            + "o registra un concepto libre.",
                    "Crear producto", () -> abrirResultado(ProductoActivity.class, PEDIR_PRODUCTO));
            return;
        }
        Intent i = new Intent(this, SelectorActivity.class);
        i.putExtra(SelectorActivity.EXTRA_MODO, SelectorActivity.MODO_PRODUCTO);
        startActivityForResult(i, PEDIR_PRODUCTO);
    }

    private void seleccionarProveedor(long id) {
        proveedorId = id;
        if (id <= 0) {
            proveedorNombre = "";
            actualizarBotonProveedor();
            return;
        }
        Modelo.Proveedor p = bd.proveedor(id);
        if (p != null) proveedorNombre = p.nombre;
        actualizarBotonProveedor();
    }

    private void actualizarBotonProveedor() {
        if (proveedorId > 0) {
            btnProveedor.setText(proveedorNombre);
            btnProveedor.setContentDescription("Proveedor seleccionado: " + proveedorNombre
                    + ". Toca para cambiar.");
        } else {
            btnProveedor.setText("Sin proveedor");
            btnProveedor.setContentDescription("Sin proveedor asociado. Toca para elegir uno.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == PEDIR_PROVEEDOR && data != null) {
            seleccionarProveedor(data.getLongExtra("id", 0));
        } else if (requestCode == PEDIR_PRODUCTO && data != null) {
            long id = data.getLongExtra("id", 0);
            if (id > 0) agregarProducto(id);
        } else if (requestCode == PEDIR_FOTO) {
            String ruta = resultadoFoto(resultCode, data);
            if (ruta != null) adjuntos.agregar(crearAdjunto(ruta));
        } else if (requestCode == PEDIR_ARCHIVO) {
            String ruta = resultadoArchivo(resultCode, data);
            if (ruta != null) adjuntos.agregar(crearAdjunto(ruta));
        }
    }

    private void agregarProducto(long id) {
        Modelo.Producto p = bd.producto(id);
        if (p == null) {
            aviso("El producto ya no existe.");
            return;
        }
        for (Modelo.Item i : items) {
            if (i.productoId == id) {
                i.cantidad++;
                i.subtotal = i.costoUnitario * i.cantidad;
                renderItems();
                actualizarTotales(true);
                return;
            }
        }
        Modelo.Item i = new Modelo.Item();
        i.productoId = p.id;
        i.descripcion = p.nombre;
        i.cantidad = 1;
        i.costoUnitario = p.costo;
        i.precioUnitario = p.costo;
        i.subtotal = p.costo;
        items.add(i);
        renderItems();
        actualizarTotales(true);
        Ui.anunciar(containerItems, p.nombre + " agregado a la compra.");
    }

    private void dialogoConcepto() {
        LinearLayout caja = new LinearLayout(this);
        caja.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(this, 20);
        caja.setPadding(p, p, p, p);
        caja.addView(Ui.texto(this, "Descripción", 16, getColor(R.color.texto_secundario), true));
        EditText descripcion = new EditText(this);
        descripcion.setHint("Ej.: transporte, empaques, servicio");
        descripcion.setTextSize(18);
        descripcion.setBackgroundResource(R.drawable.fondo_campo);
        descripcion.setPadding(p, p / 2, p, p / 2);
        caja.addView(descripcion);
        TextView l2 = Ui.texto(this, "Costo en pesos", 16, getColor(R.color.texto_secundario), true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(this, 14);
        l2.setLayoutParams(lp);
        caja.addView(l2);
        EditText monto = new EditText(this);
        monto.setHint("Ej.: 20000");
        monto.setTextSize(18);
        monto.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        monto.setBackgroundResource(R.drawable.fondo_campo);
        monto.setPadding(p, p / 2, p, p / 2);
        caja.addView(monto);

        android.app.AlertDialog d = new android.app.AlertDialog.Builder(this)
                .setTitle("Agregar concepto libre")
                .setView(caja)
                .setPositiveButton("Agregar", null)
                .setNegativeButton("Cancelar", null)
                .create();
        d.setOnShowListener(x -> {
            agrandarDialogo(d);
            d.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String nombre = descripcion.getText().toString().trim();
                long valor = Formatos.parsePesos(monto.getText().toString());
                if (nombre.isEmpty()) {
                    descripcion.setError("Escribe una descripción.");
                    descripcion.requestFocus();
                    return;
                }
                if (valor <= 0) {
                    monto.setError("Escribe un costo mayor que cero.");
                    monto.requestFocus();
                    return;
                }
                Modelo.Item i = new Modelo.Item();
                i.descripcion = nombre;
                i.cantidad = 1;
                i.costoUnitario = valor;
                i.precioUnitario = valor;
                i.subtotal = valor;
                items.add(i);
                d.dismiss();
                renderItems();
                actualizarTotales(true);
                Ui.anunciar(containerItems, nombre + " agregado. Total " + txtTotal.getText());
            });
        });
        d.show();
    }

    private void renderItems() {
        final View desplazable = findViewById(R.id.scrollForm);
        final int posicion = desplazable == null ? 0 : desplazable.getScrollY();
        containerItems.removeAllViews();
        txtVacioItems.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflador = LayoutInflater.from(this);
        for (int idx = 0; idx < items.size(); idx++) {
            final Modelo.Item item = items.get(idx);
            View fila = inflador.inflate(R.layout.fila_item_compra, containerItems, false);
            TextView descripcion = fila.findViewById(R.id.txtDescripcionItem);
            TextView subtotal = fila.findViewById(R.id.txtSubtotalItem);
            TextView cantidad = fila.findViewById(R.id.txtCantidadItem);
            EditText costo = fila.findViewById(R.id.etCostoUnitario);
            ImageButton menos = fila.findViewById(R.id.btnMenosItem);
            ImageButton mas = fila.findViewById(R.id.btnMasItem);
            ImageButton quitar = fila.findViewById(R.id.btnQuitarItem);

            descripcion.setText(item.descripcion);
            cantidad.setText(String.valueOf(item.cantidad));
            subtotal.setText(Formatos.pesos(item.costoUnitario * item.cantidad));
            actualizando = true;
            costo.setText(item.costoUnitario == 0 ? "" : String.valueOf(item.costoUnitario));
            actualizando = false;
            costo.setContentDescription("Costo unitario de " + item.descripcion);
            costo.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                    if (actualizando) return;
                    item.costoUnitario = Formatos.parsePesos(s.toString());
                    item.precioUnitario = item.costoUnitario;
                    item.subtotal = item.costoUnitario * item.cantidad;
                    subtotal.setText(Formatos.pesos(item.subtotal));
                    actualizarTotales(false);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
            menos.setEnabled(item.cantidad > 1);
            menos.setAlpha(item.cantidad > 1 ? 1f : 0.35f);
            menos.setContentDescription("Quitar una unidad de " + item.descripcion);
            mas.setContentDescription("Agregar una unidad de " + item.descripcion);
            quitar.setContentDescription("Quitar " + item.descripcion + " de la compra");
            menos.setOnClickListener(v -> {
                if (item.cantidad > 1) {
                    item.cantidad--;
                    item.subtotal = item.costoUnitario * item.cantidad;
                    renderItems();
                    actualizarTotales(true);
                    Ui.anunciar(containerItems, item.descripcion + ", cantidad " + item.cantidad);
                }
            });
            mas.setOnClickListener(v -> {
                item.cantidad++;
                item.subtotal = item.costoUnitario * item.cantidad;
                renderItems();
                actualizarTotales(true);
                Ui.anunciar(containerItems, item.descripcion + ", cantidad " + item.cantidad);
            });
            quitar.setOnClickListener(v -> {
                items.remove(item);
                renderItems();
                actualizarTotales(true);
                Ui.anunciar(containerItems, item.descripcion + " quitado de la compra.");
            });
            containerItems.addView(fila);
            if (idx < items.size() - 1) containerItems.addView(Ui.separador(this));
        }
        if (desplazable != null) {
            desplazable.post(() -> desplazable.scrollTo(0, posicion));
        }
    }

    private void actualizarTotales(boolean anunciar) {
        Modelo.Compra borrador = new Modelo.Compra();
        borrador.impuestoPct = swImpuesto.isChecked() ? decimal(etImpuesto) : 0;
        if (borrador.impuestoPct < 0) borrador.impuestoPct = 0;
        if (borrador.impuestoPct > 100) borrador.impuestoPct = 100;
        BD.calcularCompra(borrador, items);
        txtSubtotal.setText(Formatos.pesos(borrador.subtotal));
        txtImpuesto.setText(Formatos.pesos(borrador.impuesto));
        txtTotal.setText(Formatos.pesos(borrador.total));
        lblImpuesto.setText(swImpuesto.isChecked()
                ? "Impuesto (" + Formatos.porcentaje(borrador.impuestoPct) + ")"
                : "Impuesto (no incluido)");
        txtTotal.setContentDescription("Total de la compra: " + Formatos.pesosHablado(borrador.total));
        TextView boton = findViewById(R.id.btnAprobar);
        boton.setContentDescription("Aprobar y registrar compra por "
                + Formatos.pesosHablado(borrador.total));
        if (anunciar) Ui.anunciar(txtTotal, "Total " + Formatos.pesosHablado(borrador.total));
    }

    private void aprobar() {
        if (items.isEmpty()) {
            aviso("Agrega al menos un producto o concepto antes de registrar la compra.");
            return;
        }
        if (swImpuesto.isChecked()) {
            double tasa = decimal(etImpuesto);
            if (tasa < 0 || tasa > 100) {
                error(etImpuesto, "El porcentaje debe estar entre 0 y 100.");
                return;
            }
        }
        Modelo.Compra v = new Modelo.Compra();
        v.fecha = Fechas.ahora();
        v.proveedorId = proveedorId;
        v.formaPago = String.valueOf(spForma.getSelectedItem());
        v.notas = texto(etNotas);
        v.impuestoPct = swImpuesto.isChecked() ? decimal(etImpuesto) : 0;
        BD.calcularCompra(v, items);
        if (v.total == 0) {
            confirmar("Compra con total en cero",
                    "Todos los costos están en cero. ¿Registrar la compra de todas formas?",
                    "Registrar", () -> confirmarCompra(v));
            return;
        }
        confirmarCompra(v);
    }

    private void confirmarCompra(Modelo.Compra v) {
        String detalle = "Proveedor: " + (proveedorId > 0 ? proveedorNombre : "Sin proveedor")
                + "\nForma de pago: " + v.formaPago
                + "\nTotal: " + Formatos.pesos(v.total)
                + (v.impuesto > 0 ? "\nImpuesto: " + Formatos.pesos(v.impuesto) : "")
                + "\n\n¿Registrar esta compra? Las existencias se sumarán al inventario.";
        confirmar("Aprobar compra", detalle, "Aprobar y registrar", () -> guardar(v));
    }

    private void guardar(Modelo.Compra v) {
        List<Modelo.Adjunto> archivos = adjuntos.finalizar();
        try {
            long id = bd.guardarCompra(v, items, archivos);
            guardada = true;
            aviso("Compra registrada por " + Formatos.pesos(v.total) + ".");
            Intent i = new Intent(this, CompraDetalleActivity.class);
            i.putExtra("id", id);
            startActivity(i);
            finish();
        } catch (Exception e) {
            informar("No se pudo registrar la compra",
                    "Ocurrió un error al guardar: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (guardada || (items.isEmpty() && texto(etNotas).isEmpty()
                && adjuntos.estaVacia() && proveedorId <= 0)) {
            super.onBackPressed();
            return;
        }
        confirmar("Descartar compra",
                "Si sales ahora se perderán los productos y datos que agregaste.",
                "Descartar", () -> {
                    adjuntos.limpiarTemporales();
                    finish();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && !guardada && adjuntos != null) {
            adjuntos.limpiarTemporales();
        }
    }
}
