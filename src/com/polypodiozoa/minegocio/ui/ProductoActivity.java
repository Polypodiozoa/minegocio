package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Archivos;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.Imagen;

public class ProductoActivity extends BaseActivity {

    private static final int PEDIR_FOTO = 401;
    private static final int PEDIR_ARCHIVO = 402;

    private EditText etNombre, etCategoria, etCodigo, etUnidad, etPrecio, etCosto,
            etStock, etStockMinimo;
    private ImageView imgProducto;
    private Button btnQuitarFoto;
    private TextView txtGananciaUnidad;

    private long id;
    private Modelo.Producto producto;
    private String rutaFoto = "";
    private String fotoOriginal = "";
    private String rutaTemporal = "";
    private boolean quitarOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto);
        id = idExtra("id");
        configurarBarra(id > 0 ? "Editar producto" : "Nuevo producto");
        etNombre = findViewById(R.id.etNombre);
        etCategoria = findViewById(R.id.etCategoria);
        etCodigo = findViewById(R.id.etCodigo);
        etUnidad = findViewById(R.id.etUnidad);
        etPrecio = findViewById(R.id.etPrecio);
        etCosto = findViewById(R.id.etCosto);
        etStock = findViewById(R.id.etStock);
        etStockMinimo = findViewById(R.id.etStockMinimo);
        imgProducto = findViewById(R.id.imgProducto);
        btnQuitarFoto = findViewById(R.id.btnQuitarFoto);
        txtGananciaUnidad = findViewById(R.id.txtGananciaUnidad);
        android.text.TextWatcher rentabilidad = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { actualizarRentabilidad(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        etPrecio.addTextChangedListener(rentabilidad);
        etCosto.addTextChangedListener(rentabilidad);
        imgProducto.setOnClickListener(v -> abrirFotoActual());

        findViewById(R.id.btnTomarFoto).setOnClickListener(v -> tomarFoto(PEDIR_FOTO));
        findViewById(R.id.btnElegirFoto).setOnClickListener(v -> elegirArchivo(PEDIR_ARCHIVO));
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
        btnQuitarFoto.setOnClickListener(v -> quitarFoto());
        Button eliminar = findViewById(R.id.btnEliminar);
        eliminar.setVisibility(id > 0 ? View.VISIBLE : View.GONE);
        eliminar.setOnClickListener(v -> eliminar());

        if (id > 0) {
            producto = bd.producto(id);
            if (producto == null) {
                aviso("El producto ya no existe.");
                finish();
                return;
            }
            etNombre.setText(producto.nombre);
            etCategoria.setText(producto.categoria);
            etCodigo.setText(producto.codigo);
            etUnidad.setText(producto.unidad);
            etPrecio.setText(String.valueOf(producto.precio));
            etCosto.setText(String.valueOf(producto.costo));
            etStock.setText(String.valueOf(producto.stock));
            etStockMinimo.setText(String.valueOf(producto.stockMinimo));
            rutaFoto = producto.foto;
            fotoOriginal = producto.foto;
        }
        actualizarFoto();
        actualizarRentabilidad();
        marcarInicial(etNombre, etCategoria, etCodigo, etUnidad, etPrecio, etCosto,
                etStock, etStockMinimo);
    }

    @Override
    public void onBackPressed() {
        salirSiCambios();
    }

    private void actualizarRentabilidad() {
        if (txtGananciaUnidad == null) return;
        long precio = Formatos.parsePesos(etPrecio.getText().toString());
        long costo = Formatos.parsePesos(etCosto.getText().toString());
        long ganancia = precio - costo;
        long margen = precio > 0 ? Math.round(ganancia * 100.0 / precio) : 0;
        if (precio <= 0) {
            txtGananciaUnidad.setText("Escribe el precio de venta para ver la ganancia.");
        } else {
            txtGananciaUnidad.setText("Ganancia por unidad: " + Formatos.pesos(ganancia)
                    + " (" + margen + "%)");
        }
        txtGananciaUnidad.setContentDescription(txtGananciaUnidad.getText());
    }

    private void actualizarFoto() {
        if (rutaFoto == null || rutaFoto.isEmpty()) {
            imgProducto.setPadding(UiDp(12), UiDp(12), UiDp(12), UiDp(12));
            imgProducto.setColorFilter(getColor(R.color.primario));
            imgProducto.setImageResource(R.drawable.ic_caja);
            imgProducto.setContentDescription("Sin foto del producto");
            btnQuitarFoto.setVisibility(View.GONE);
        } else {
            imgProducto.setPadding(0, 0, 0, 0);
            imgProducto.setColorFilter(null);
            Imagen.mostrar(imgProducto, rutaFoto, R.drawable.ic_caja, 400);
            imgProducto.setContentDescription("Foto del producto");
            btnQuitarFoto.setVisibility(View.VISIBLE);
        }
    }

    private void abrirFotoActual() {
        if (rutaFoto == null || rutaFoto.isEmpty()) return;
        Intent i = new Intent(this, FotoActivity.class);
        i.putExtra("ruta", rutaFoto);
        startActivity(i);
    }

    private int UiDp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    private void quitarFoto() {
        if (rutaTemporal != null && !rutaTemporal.isEmpty()) {
            Archivos.borrar(rutaTemporal);
            rutaTemporal = "";
        }
        if (!fotoOriginal.isEmpty() && fotoOriginal.equals(rutaFoto)) {
            quitarOriginal = true;
        }
        rutaFoto = "";
        actualizarFoto();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PEDIR_FOTO && requestCode != PEDIR_ARCHIVO) return;
        String nueva = requestCode == PEDIR_FOTO
                ? resultadoFoto(resultCode, data)
                : resultadoArchivo(resultCode, data);
        if (nueva == null || nueva.isEmpty()) return;
        if (!Archivos.esImagen(nueva, mimeArchivoPendiente)) {
            aviso("Para la foto del producto elige una imagen. Para documentos usa las ventas o compras.");
            Archivos.borrar(nueva);
            return;
        }
        if (rutaTemporal != null && !rutaTemporal.isEmpty()) Archivos.borrar(rutaTemporal);
        rutaTemporal = nueva;
        rutaFoto = nueva;
        quitarOriginal = false;
        actualizarFoto();
    }

    private void guardar() {
        String nombre = texto(etNombre);
        if (nombre.isEmpty()) {
            error(etNombre, "Escribe el nombre del producto.");
            return;
        }
        int stock = (int) Formatos.parsePesos(etStock.getText().toString());
        int minimo = entero(etStockMinimo);
        if (stock < -1000000 || stock > 1000000) {
            error(etStock, "Escribe una existencia válida.");
            return;
        }
        long precio = numero(etPrecio);
        long costo = numero(etCosto);
        if (precio <= 0) {
            error(etPrecio, "Escribe un precio de venta mayor que cero.");
            return;
        }

        Modelo.Producto p = producto != null ? producto : new Modelo.Producto();
        p.nombre = nombre;
        p.categoria = texto(etCategoria);
        p.codigo = texto(etCodigo);
        p.unidad = texto(etUnidad).isEmpty() ? "unidad" : texto(etUnidad);
        p.precio = precio;
        p.costo = costo;
        p.stock = stock;
        p.stockMinimo = minimo;
        if (!rutaTemporal.isEmpty()) {
            p.foto = Archivos.moverAPermanente(this, rutaTemporal);
            rutaTemporal = "";
        } else {
            p.foto = rutaFoto == null ? "" : rutaFoto;
        }
        long guardado = bd.guardarProducto(p);
        if (quitarOriginal && !fotoOriginal.isEmpty()) {
            Archivos.borrar(fotoOriginal);
            quitarOriginal = false;
        }
        aviso("Producto guardado.");
        volverConResultado(RESULT_OK, guardado);
    }

    private void eliminar() {
        if (producto == null) return;
        if (bd.referenciasProducto(producto.id) > 0) {
            informar("No se puede eliminar",
                    "Este producto tiene ventas o compras registradas. Para conservar el historial "
                            + "no se puede borrar, pero puedes editarlo y dejar su existencia en cero.");
            return;
        }
        confirmar("Eliminar producto", "¿Eliminar “" + producto.nombre + "”?",
                "Eliminar", () -> {
                    if (bd.eliminarProducto(producto.id)) {
                        if (!fotoOriginal.isEmpty()) Archivos.borrar(fotoOriginal);
                        aviso("Producto eliminado.");
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        aviso("No se pudo eliminar el producto.");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && rutaTemporal != null && !rutaTemporal.isEmpty()) {
            Archivos.borrar(rutaTemporal);
        }
    }
}
