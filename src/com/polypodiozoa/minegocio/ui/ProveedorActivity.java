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
import com.polypodiozoa.minegocio.util.Imagen;

public class ProveedorActivity extends BaseActivity {

    private static final int PEDIR_FOTO = 421;
    private static final int PEDIR_ARCHIVO = 422;

    private EditText etNombre, etContacto, etTelefono, etCorreo, etDireccion, etNotas;
    private ImageView imgPersona;
    private Button btnQuitarFoto;

    private long id;
    private Modelo.Proveedor proveedor;
    private String rutaFoto = "";
    private String fotoOriginal = "";
    private String rutaTemporal = "";
    private boolean quitarOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persona);
        id = idExtra("id");
        configurarBarra(id > 0 ? "Editar proveedor" : "Nuevo proveedor");
        etNombre = findViewById(R.id.etNombre);
        etContacto = findViewById(R.id.etContacto);
        etTelefono = findViewById(R.id.etTelefono);
        etCorreo = findViewById(R.id.etCorreo);
        etDireccion = findViewById(R.id.etDireccion);
        etNotas = findViewById(R.id.etNotas);
        imgPersona = findViewById(R.id.imgPersona);
        btnQuitarFoto = findViewById(R.id.btnQuitarFoto);
        imgPersona.setOnClickListener(v -> {
            if (!rutaFoto.isEmpty()) {
                Intent i = new Intent(this, FotoActivity.class);
                i.putExtra("ruta", rutaFoto);
                startActivity(i);
            }
        });
        findViewById(R.id.lblCupo).setVisibility(View.GONE);
        findViewById(R.id.etCupo).setVisibility(View.GONE);
        findViewById(R.id.ayudaCupo).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.lblNombre)).setText("Nombre del proveedor *");
        ((TextView) findViewById(R.id.lblContacto)).setVisibility(View.VISIBLE);
        etContacto.setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.btnGuardar)).setText("Guardar proveedor");
        ((Button) findViewById(R.id.btnGuardar)).setContentDescription("Guardar proveedor");
        imgPersona.setImageResource(R.drawable.ic_tienda);

        findViewById(R.id.btnTomarFoto).setOnClickListener(v -> tomarFoto(PEDIR_FOTO));
        findViewById(R.id.btnElegirFoto).setOnClickListener(v -> elegirArchivo(PEDIR_ARCHIVO));
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
        btnQuitarFoto.setOnClickListener(v -> quitarFoto());
        Button eliminar = findViewById(R.id.btnEliminar);
        eliminar.setText("Eliminar proveedor");
        eliminar.setVisibility(id > 0 ? View.VISIBLE : View.GONE);
        eliminar.setOnClickListener(v -> eliminar());

        if (id > 0) {
            proveedor = bd.proveedor(id);
            if (proveedor == null) {
                aviso("El proveedor ya no existe.");
                finish();
                return;
            }
            etNombre.setText(proveedor.nombre);
            etContacto.setText(proveedor.contacto);
            etTelefono.setText(proveedor.telefono);
            etCorreo.setText(proveedor.correo);
            etDireccion.setText(proveedor.direccion);
            etNotas.setText(proveedor.notas);
            rutaFoto = proveedor.foto;
            fotoOriginal = proveedor.foto;
        }
        actualizarFoto();
    }

    private void actualizarFoto() {
        if (rutaFoto == null || rutaFoto.isEmpty()) {
            imgPersona.setPadding(dp(12), dp(12), dp(12), dp(12));
            imgPersona.setColorFilter(getColor(R.color.primario));
            imgPersona.setImageResource(R.drawable.ic_tienda);
            imgPersona.setContentDescription("Sin foto del proveedor");
            btnQuitarFoto.setVisibility(View.GONE);
        } else {
            imgPersona.setPadding(0, 0, 0, 0);
            imgPersona.setColorFilter(null);
            Imagen.mostrar(imgPersona, rutaFoto, R.drawable.ic_tienda, 400);
            imgPersona.setContentDescription("Foto del proveedor");
            btnQuitarFoto.setVisibility(View.VISIBLE);
        }
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    private void quitarFoto() {
        if (!rutaTemporal.isEmpty()) {
            Archivos.borrar(rutaTemporal);
            rutaTemporal = "";
        }
        if (!fotoOriginal.isEmpty() && fotoOriginal.equals(rutaFoto)) quitarOriginal = true;
        rutaFoto = "";
        actualizarFoto();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PEDIR_FOTO && requestCode != PEDIR_ARCHIVO) return;
        String nueva = requestCode == PEDIR_FOTO
                ? resultadoFoto(resultCode, data) : resultadoArchivo(resultCode, data);
        if (nueva == null || nueva.isEmpty()) return;
        if (!Archivos.esImagen(nueva, mimeArchivoPendiente)) {
            aviso("Para la foto del proveedor elige una imagen.");
            Archivos.borrar(nueva);
            return;
        }
        if (!rutaTemporal.isEmpty()) Archivos.borrar(rutaTemporal);
        rutaTemporal = nueva;
        rutaFoto = nueva;
        quitarOriginal = false;
        actualizarFoto();
    }

    private void guardar() {
        String nombre = texto(etNombre);
        if (nombre.isEmpty()) {
            error(etNombre, "Escribe el nombre del proveedor.");
            return;
        }
        Modelo.Proveedor p = proveedor != null ? proveedor : new Modelo.Proveedor();
        p.nombre = nombre;
        p.contacto = texto(etContacto);
        p.telefono = texto(etTelefono);
        p.correo = texto(etCorreo);
        p.direccion = texto(etDireccion);
        p.notas = texto(etNotas);
        if (!rutaTemporal.isEmpty()) {
            p.foto = Archivos.moverAPermanente(this, rutaTemporal);
            rutaTemporal = "";
        } else {
            p.foto = rutaFoto == null ? "" : rutaFoto;
        }
        long guardado = bd.guardarProveedor(p);
        if (quitarOriginal && !fotoOriginal.isEmpty()) {
            Archivos.borrar(fotoOriginal);
            quitarOriginal = false;
        }
        aviso("Proveedor guardado.");
        volverConResultado(RESULT_OK, guardado);
    }

    private void eliminar() {
        if (proveedor == null) return;
        if (bd.referenciasProveedor(proveedor.id) > 0) {
            informar("No se puede eliminar",
                    "Este proveedor tiene compras registradas. Para conservar el historial "
                            + "no se puede borrar, pero puedes editarlo.");
            return;
        }
        confirmar("Eliminar proveedor", "¿Eliminar a “" + proveedor.nombre + "”?",
                "Eliminar", () -> {
                    if (bd.eliminarProveedor(proveedor.id)) {
                        if (!fotoOriginal.isEmpty()) Archivos.borrar(fotoOriginal);
                        aviso("Proveedor eliminado.");
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        aviso("No se pudo eliminar el proveedor.");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && !rutaTemporal.isEmpty()) Archivos.borrar(rutaTemporal);
    }
}
