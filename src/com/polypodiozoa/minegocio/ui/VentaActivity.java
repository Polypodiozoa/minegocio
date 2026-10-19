package com.polypodiozoa.minegocio.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.util.Calendar;
import java.util.List;

public class VentaActivity extends BaseActivity {

    private static final int PEDIR_CLIENTE = 601;
    private static final int PEDIR_PRODUCTO = 602;
    private static final int PEDIR_FOTO = 603;
    private static final int PEDIR_ARCHIVO = 604;

    private final List<Modelo.Item> items = new ArrayList<>();
    private long clienteId;
    private String clienteNombre = "";
    private boolean guardada;

    private Button btnCliente;
    private LinearLayout containerItems;
    private TextView txtVacioItems, txtSubtotal, txtDescuento, txtImpuesto, txtTotal, lblImpuesto;
    private TextView txtCosto, txtGanancia, txtMargen, txtVencimiento;
    private View filaVencimiento;
    private Switch swImpuesto;
    private EditText etImpuesto, etDescuento, etNotas;
    private Spinner spForma;
    private ListaAdjuntos adjuntos;
    private long fechaLimite = Fechas.vencimientoPorDefecto();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venta);
        configurarBarra("Nueva venta");
        btnCliente = findViewById(R.id.btnCliente);
        containerItems = findViewById(R.id.containerItems);
        txtVacioItems = findViewById(R.id.txtVacioItems);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtDescuento = findViewById(R.id.txtDescuento);
        txtImpuesto = findViewById(R.id.txtImpuesto);
        txtTotal = findViewById(R.id.txtTotal);
        lblImpuesto = findViewById(R.id.lblImpuesto);
        txtCosto = findViewById(R.id.txtCosto);
        txtGanancia = findViewById(R.id.txtGanancia);
        txtMargen = findViewById(R.id.txtMargen);
        txtVencimiento = findViewById(R.id.btnVencimiento);
        filaVencimiento = findViewById(R.id.filaVencimiento);
        swImpuesto = findViewById(R.id.swImpuesto);
        etImpuesto = findViewById(R.id.etImpuesto);
        etDescuento = findViewById(R.id.etDescuento);
        etNotas = findViewById(R.id.etNotas);
        spForma = findViewById(R.id.spForma);

        String[] formas = {"Efectivo", "Transferencia", "Nequi / Daviplata", "Tarjeta", "Crédito"};
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, R.layout.item_spinner, formas);
        adaptador.setDropDownViewResource(R.layout.item_spinner);
        spForma.setAdapter(adaptador);
        spForma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int posicion, long id) {
                actualizarVencimiento();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        boolean impuesto = bd.ajusteSi("impuesto_activado", true);
        swImpuesto.setChecked(impuesto);
        etImpuesto.setText(Formatos.decimal(bd.ajusteDecimal("impuesto_pct", 19)));
        etImpuesto.setEnabled(impuesto);
        etImpuesto.setAlpha(impuesto ? 1f : 0.55f);

        adjuntos = new ListaAdjuntos(this, findViewById(R.id.containerAdjuntos));
        btnCliente.setOnClickListener(v -> elegirCliente());
        findViewById(R.id.btnAgregarProducto).setOnClickListener(v -> elegirProducto());
        findViewById(R.id.btnConcepto).setOnClickListener(v -> dialogoConcepto());
        findViewById(R.id.btnFoto).setOnClickListener(v -> tomarFoto(PEDIR_FOTO));
        findViewById(R.id.btnArchivo).setOnClickListener(v -> elegirArchivo(PEDIR_ARCHIVO));
        findViewById(R.id.btnAprobar).setOnClickListener(v -> aprobar());
        txtVencimiento.setOnClickListener(v -> elegirVencimiento());

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
        etDescuento.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { actualizarTotales(false); }
            @Override public void afterTextChanged(Editable s) {}
        });

        long pre = idExtra("cliente_id");
        if (pre > 0) {
            seleccionarCliente(pre);
        } else {
            actualizarBotonCliente();
        }
        renderItems();
        actualizarTotales(false);
        actualizarVencimiento();
    }

    private void actualizarVencimiento() {
        boolean credito = "Crédito".equals(String.valueOf(spForma.getSelectedItem()));
        filaVencimiento.setVisibility(credito ? View.VISIBLE : View.GONE);
        if (!credito) return;
        TextView boton = findViewById(R.id.btnVencimiento);
        boton.setText("Vence el " + Fechas.fecha(fechaLimite)
                + " · " + Fechas.vencimientoTexto(fechaLimite));
        boton.setContentDescription("Fecha límite de pago: " + Fechas.fecha(fechaLimite)
                + ". Toca para cambiarla.");
    }

    private void elegirVencimiento() {
        Calendar c = Fechas.calendario(fechaLimite > 0 ? fechaLimite : Fechas.vencimientoPorDefecto());
        DatePickerDialog dialogo = new DatePickerDialog(this,
                (view, anio, mes, dia) -> {
                    fechaLimite = Fechas.mediodia(anio, mes, dia);
                    actualizarVencimiento();
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialogo.getDatePicker().setMinDate(Fechas.inicioDia(Fechas.ahora()));
        dialogo.show();
    }

    private void elegirCliente() {
        Intent i = new Intent(this, SelectorActivity.class);
        i.putExtra(SelectorActivity.EXTRA_MODO, SelectorActivity.MODO_CLIENTE);
        startActivityForResult(i, PEDIR_CLIENTE);
    }

    private void elegirProducto() {
        if (bd.totalProductos() == 0) {
            confirmar("No hay productos",
                    "Todavía no tienes productos en el inventario. Puedes crear uno ahora "
                            + "o usar un concepto libre.",
                    "Crear producto", () -> abrirResultado(ProductoActivity.class, PEDIR_PRODUCTO));
            return;
        }
        Intent i = new Intent(this, SelectorActivity.class);
        i.putExtra(SelectorActivity.EXTRA_MODO, SelectorActivity.MODO_PRODUCTO);
        startActivityForResult(i, PEDIR_PRODUCTO);
    }

    private void seleccionarCliente(long id) {
        clienteId = id;
        if (id <= 0) {
            clienteNombre = "";
            actualizarBotonCliente();
            return;
        }
        Modelo.Cliente c = bd.cliente(id);
        if (c != null) {
            clienteNombre = c.nombre;
            if (c.saldo > 0) {
                aviso("Este cliente tiene un saldo pendiente de " + Formatos.pesos(c.saldo) + ".");
            }
        }
        actualizarBotonCliente();
    }

    private void actualizarBotonCliente() {
        if (clienteId > 0) {
            btnCliente.setText(clienteNombre);
            btnCliente.setContentDescription("Cliente seleccionado: " + clienteNombre
                    + ". Toca para cambiar.");
        } else {
            btnCliente.setText("Consumidor final");
            btnCliente.setContentDescription("Consumidor final, sin cliente asociado. Toca para elegir un cliente.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == PEDIR_CLIENTE && data != null) {
            seleccionarCliente(data.getLongExtra("id", 0));
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
        Modelo.Item existente = null;
        for (Modelo.Item i : items) {
            if (i.productoId == id) {
                existente = i;
                break;
            }
        }
        if (existente != null) {
            existente.cantidad++;
            existente.subtotal = existente.precioUnitario * existente.cantidad;
        } else {
            Modelo.Item i = new Modelo.Item();
            i.productoId = p.id;
            i.descripcion = p.nombre;
            i.cantidad = 1;
            i.precioUnitario = p.precio;
            i.costoUnitario = p.costo;
            i.subtotal = p.precio;
            items.add(i);
        }
        renderItems();
        actualizarTotales(true);
        Ui.anunciar(containerItems, p.nombre + " agregado. Total " + txtTotal.getText());
    }

    private void dialogoConcepto() {
        LinearLayout caja = new LinearLayout(this);
        caja.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(this, 20);
        caja.setPadding(p, p, p, p);
        TextView l1 = Ui.texto(this, "Descripción", 16, getColor(R.color.texto_secundario), true);
        caja.addView(l1);
        EditText descripcion = new EditText(this);
        descripcion.setHint("Ej.: domicilio, mano de obra, servicio");
        descripcion.setTextSize(18);
        descripcion.setBackgroundResource(R.drawable.fondo_campo);
        descripcion.setPadding(p, p / 2, p, p / 2);
        caja.addView(descripcion);
        TextView l2 = Ui.texto(this, "Valor en pesos", 16, getColor(R.color.texto_secundario), true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(this, 14);
        l2.setLayoutParams(lp);
        caja.addView(l2);
        EditText monto = new EditText(this);
        monto.setHint("Ej.: 10000");
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
                    monto.setError("Escribe un valor mayor que cero.");
                    monto.requestFocus();
                    return;
                }
                Modelo.Item i = new Modelo.Item();
                i.descripcion = nombre;
                i.cantidad = 1;
                i.precioUnitario = valor;
                i.costoUnitario = 0;
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
            View fila = inflador.inflate(R.layout.fila_item, containerItems, false);
            TextView descripcion = fila.findViewById(R.id.txtDescripcionItem);
            TextView precio = fila.findViewById(R.id.txtPrecioItem);
            TextView ganancia = fila.findViewById(R.id.txtGananciaItem);
            TextView subtotal = fila.findViewById(R.id.txtSubtotalItem);
            TextView cantidad = fila.findViewById(R.id.txtCantidadItem);
            ImageButton menos = fila.findViewById(R.id.btnMenosItem);
            ImageButton mas = fila.findViewById(R.id.btnMasItem);
            ImageButton quitar = fila.findViewById(R.id.btnQuitarItem);

            descripcion.setText(item.descripcion);
            subtotal.setText(Formatos.pesos(item.precioUnitario * item.cantidad));
            cantidad.setText(String.valueOf(item.cantidad));
            if (item.productoId > 0) {
                Modelo.Producto prod = bd.producto(item.productoId);
                String stock = prod == null ? "Producto archivado"
                        : "Existencia: " + Ui.unidades(prod.stock, prod.unidad);
                precio.setText(Formatos.pesos(item.precioUnitario) + " c/u · " + stock);
            } else {
                precio.setText(Formatos.pesos(item.precioUnitario) + " · concepto libre");
            }
            long utilidadLinea = (item.precioUnitario - item.costoUnitario) * item.cantidad;
            long margenLinea = item.precioUnitario > 0
                    ? Math.round((item.precioUnitario - item.costoUnitario) * 100.0 / item.precioUnitario) : 0;
            ganancia.setText("Ganancia: " + Formatos.pesos(utilidadLinea)
                    + " (" + margenLinea + "%) · costo " + Formatos.pesos(item.costoUnitario) + " c/u");
            menos.setEnabled(item.cantidad > 1);
            menos.setAlpha(item.cantidad > 1 ? 1f : 0.35f);
            menos.setContentDescription("Quitar una unidad de " + item.descripcion);
            mas.setContentDescription("Agregar una unidad de " + item.descripcion);
            quitar.setContentDescription("Quitar " + item.descripcion + " de la venta");
            menos.setOnClickListener(v -> {
                if (item.cantidad > 1) {
                    item.cantidad--;
                    item.subtotal = item.precioUnitario * item.cantidad;
                    renderItems();
                    actualizarTotales(true);
                    Ui.anunciar(containerItems, item.descripcion + ", cantidad " + item.cantidad);
                }
            });
            mas.setOnClickListener(v -> {
                item.cantidad++;
                item.subtotal = item.precioUnitario * item.cantidad;
                renderItems();
                actualizarTotales(true);
                Ui.anunciar(containerItems, item.descripcion + ", cantidad " + item.cantidad);
            });
            quitar.setOnClickListener(v -> {
                items.remove(item);
                renderItems();
                actualizarTotales(true);
                Ui.anunciar(containerItems, item.descripcion + " quitado de la venta.");
            });
            containerItems.addView(fila);
            if (idx < items.size() - 1) containerItems.addView(Ui.separador(this));
        }
        if (desplazable != null) {
            desplazable.post(() -> desplazable.scrollTo(0, posicion));
        }
    }

    private void actualizarTotales(boolean anunciar) {
        Modelo.Venta borrador = new Modelo.Venta();
        borrador.descuento = numero(etDescuento);
        borrador.impuestoPct = swImpuesto.isChecked() ? decimal(etImpuesto) : 0;
        if (borrador.impuestoPct < 0) borrador.impuestoPct = 0;
        if (borrador.impuestoPct > 100) borrador.impuestoPct = 100;
        BD.calcularVenta(borrador, items);

        txtSubtotal.setText(Formatos.pesos(borrador.subtotal));
        txtDescuento.setText(Formatos.pesos(borrador.descuento));
        txtImpuesto.setText(Formatos.pesos(borrador.impuesto));
        long base = Math.max(0, borrador.subtotal - borrador.descuento);
        long ganancia = base - borrador.costo;
        long margen = base > 0 ? Math.round(ganancia * 100.0 / base) : 0;
        txtCosto.setText(Formatos.pesos(borrador.costo));
        txtGanancia.setText(Formatos.pesos(ganancia));
        txtMargen.setText(margen + "%");
        txtGanancia.setContentDescription("Ganancia estimada: " + Formatos.pesosHablado(ganancia));
        txtTotal.setText(Formatos.pesos(borrador.total));
        if (swImpuesto.isChecked()) {
            lblImpuesto.setText("Impuesto (" + Formatos.porcentaje(borrador.impuestoPct) + ")");
        } else {
            lblImpuesto.setText("Impuesto (no cobrado)");
        }
        txtTotal.setContentDescription("Total a cobrar: " + Formatos.pesosHablado(borrador.total));
        TextView boton = findViewById(R.id.btnAprobar);
        boton.setContentDescription("Aprobar y registrar venta por "
                + Formatos.pesosHablado(borrador.total));
        if (anunciar) {
            Ui.anunciar(txtTotal, "Total " + Formatos.pesosHablado(borrador.total));
        }
    }

    private void aprobar() {
        if (items.isEmpty()) {
            aviso("Agrega al menos un producto o concepto antes de registrar la venta.");
            return;
        }
        String forma = String.valueOf(spForma.getSelectedItem());
        if ("Crédito".equals(forma) && clienteId <= 0) {
            informar("Falta el cliente",
                    "Para una venta a crédito debes elegir un cliente. Toca el botón de cliente "
                            + "y selecciona o crea uno.");
            return;
        }
        if (swImpuesto.isChecked()) {
            double tasa = decimal(etImpuesto);
            if (tasa < 0 || tasa > 100) {
                error(etImpuesto, "El porcentaje debe estar entre 0 y 100.");
                return;
            }
        }
        if ("Crédito".equals(forma) && fechaLimite < Fechas.inicioDia(Fechas.ahora())) {
            informar("Fecha límite inválida",
                    "La fecha límite de pago no puede ser anterior a hoy. Toca la fecha y elige otra.");
            return;
        }
        long subtotal = 0;
        for (Modelo.Item i : items) subtotal += i.precioUnitario * i.cantidad;
        long descuento = numero(etDescuento);
        if (descuento > subtotal) {
            error(etDescuento, "El descuento no puede ser mayor que el subtotal ("
                    + Formatos.pesos(subtotal) + ").");
            return;
        }
        if (!bd.ajusteSi("permitir_stock_negativo", false)) {
            if (faltantesDeStock()) return;
        }
        continuarAprobacion();
    }

    private boolean faltantesDeStock() {
        StringBuilder faltan = new StringBuilder();
        for (Modelo.Item i : items) {
            if (i.productoId <= 0) continue;
            Modelo.Producto p = bd.producto(i.productoId);
            if (p != null && p.stock < i.cantidad) {
                if (faltan.length() > 0) faltan.append("\n");
                faltan.append("• ").append(p.nombre).append(": pides ").append(i.cantidad)
                        .append(" y hay ").append(p.stock).append(".");
            }
        }
        if (faltan.length() == 0) return false;
        confirmar("Existencias insuficientes",
                "Estos productos no tienen suficiente existencia:\n\n" + faltan
                        + "\n\nSi continúas, la existencia quedará en negativo.",
                "Vender de todas formas", this::continuarAprobacion);
        return true;
    }

    private void continuarAprobacion() {
        Modelo.Venta v = new Modelo.Venta();
        v.fecha = Fechas.ahora();
        v.clienteId = clienteId;
        v.formaPago = String.valueOf(spForma.getSelectedItem());
        v.estado = "Crédito".equals(v.formaPago) ? "credito" : "pagada";
        v.notas = texto(etNotas);
        v.impuestoPct = swImpuesto.isChecked() ? decimal(etImpuesto) : 0;
        v.descuento = numero(etDescuento);
        v.fechaLimite = "credito".equals(v.estado) ? fechaLimite : 0;
        BD.calcularVenta(v, items);

        if ("credito".equals(v.estado)) {
            Modelo.Cliente cliente = bd.cliente(clienteId);
            if (cliente != null && cliente.cupo > 0 && cliente.saldo + v.total > cliente.cupo) {
                long disponible = Math.max(0, cliente.cupo - cliente.saldo);
                confirmar("Cupo de fiado excedido",
                        "El cupo de " + cliente.nombre + " es " + Formatos.pesos(cliente.cupo)
                                + " y tiene disponible " + Formatos.pesos(disponible) + ". "
                                + "Esta venta es por " + Formatos.pesos(v.total) + ".\n\n"
                                + "¿Continuar de todas formas?",
                        "Continuar", () -> confirmarVenta(v));
                return;
            }
        }
        confirmarVenta(v);
    }

    private void confirmarVenta(Modelo.Venta v) {
        String detalle = "Cliente: " + (clienteId > 0 ? clienteNombre : "Consumidor final")
                + "\nForma de pago: " + v.formaPago
                + "\nTotal: " + Formatos.pesos(v.total)
                + (v.impuesto > 0 ? "\nImpuesto: " + Formatos.pesos(v.impuesto) : "")
                + (v.fechaLimite > 0 ? "\nVence: " + Fechas.fecha(v.fechaLimite) : "")
                + "\nGanancia estimada: " + Formatos.pesos(v.total - v.impuesto - v.costo)
                + "\n\n¿Registrar esta venta?";
        confirmar("Aprobar venta", detalle, "Aprobar y registrar", () -> guardar(v));
    }

    private void guardar(Modelo.Venta v) {
        List<Modelo.Adjunto> archivos = adjuntos.finalizar();
        try {
            long id = bd.guardarVenta(v, items, archivos);
            guardada = true;
            aviso("Venta registrada por " + Formatos.pesos(v.total) + ".");
            Intent i = new Intent(this, VentaDetalleActivity.class);
            i.putExtra("id", id);
            startActivity(i);
            finish();
        } catch (Exception e) {
            informar("No se pudo registrar la venta",
                    "Ocurrió un error al guardar: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (guardada || (items.isEmpty() && texto(etNotas).isEmpty()
                && adjuntos.estaVacia() && clienteId <= 0)) {
            super.onBackPressed();
            return;
        }
        confirmar("Descartar venta",
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
