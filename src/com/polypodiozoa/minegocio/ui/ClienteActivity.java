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

public class ClienteActivity extends BaseActivity {

    private static final int PEDIR_FOTO = 411;
    private static final int PEDIR_ARCHIVO = 412;

    private EditText etNombre, etTelefono, etCorreo, etDireccion, etNotas, etCupo;
    private ImageView imgPersona;
    private Button btnQuitarFoto;

    private long id;
    private Modelo.Cliente cliente;
    private String rutaFoto = "";
    private String fotoOriginal = "";
    private String rutaTemporal = "";
    private boolean quitarOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persona);
        id = idExtra("id");
        configurarBarra(id > 0 ? "Editar cliente" : "Nuevo cliente");
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etCorreo = findViewById(R.id.etCorreo);
        etDireccion = findViewById(R.id.etDireccion);
        etNotas = findViewById(R.id.etNotas);
        etCupo = findViewById(R.id.etCupo);
        imgPersona = findViewById(R.id.imgPersona);
        btnQuitarFoto = findViewById(R.id.btnQuitarFoto);
        imgPersona.setOnClickListener(v -> {
            if (!rutaFoto.isEmpty()) {
                Intent i = new Intent(this, FotoActivity.class);
                i.putExtra("ruta", rutaFoto);
                startActivity(i);
            }
        });
        findViewById(R.id.lblContacto).setVisibility(View.GONE);
        findViewById(R.id.etContacto).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.lblNombre)).setText("Nombre completo *");
        ((Button) findViewById(R.id.btnGuardar)).setText("Guardar cliente");
        ((Button) findViewById(R.id.btnGuardar)).setContentDescription("Guardar cliente");

        findViewById(R.id.btnTomarFoto).setOnClickListener(v -> tomarFoto(PEDIR_FOTO));
        findViewById(R.id.btnElegirFoto).setOnClickListener(v -> elegirArchivo(PEDIR_ARCHIVO));
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
        btnQuitarFoto.setOnClickListener(v -> quitarFoto());
        Button eliminar = findViewById(R.id.btnEliminar);
        eliminar.setText("Eliminar cliente");
        eliminar.setVisibility(id > 0 ? View.VISIBLE : View.GONE);
        eliminar.setOnClickListener(v -> eliminar());

        if (id > 0) {
            cliente = bd.cliente(id);
            if (cliente == null) {
                aviso("El cliente ya no existe.");
                finish();
                return;
            }
            etNombre.setText(cliente.nombre);
            etTelefono.setText(cliente.telefono);
            etCorreo.setText(cliente.correo);
            etDireccion.setText(cliente.direccion);
            etNotas.setText(cliente.notas);
            etCupo.setText(cliente.cupo == 0 ? "" : String.valueOf(cliente.cupo));
            rutaFoto = cliente.foto;
            fotoOriginal = cliente.foto;
        }
        actualizarFoto();
        marcarInicial(etNombre, etTelefono, etCorreo, etDireccion, etNotas, etCupo);
    }

    @Override
    public void onBackPressed() {
        salirSiCambios();
    }

    private void actualizarFoto() {
        if (rutaFoto == null || rutaFoto.isEmpty()) {
            imgPersona.setPadding(dp(12), dp(12), dp(12), dp(12));
            imgPersona.setColorFilter(getColor(R.color.primario));
            imgPersona.setImageResource(R.drawable.ic_persona);
            imgPersona.setContentDescription("Sin foto del cliente");
            btnQuitarFoto.setVisibility(View.GONE);
        } else {
            imgPersona.setPadding(0, 0, 0, 0);
            imgPersona.setColorFilter(null);
            Imagen.mostrar(imgPersona, rutaFoto, R.drawable.ic_persona, 400);
            imgPersona.setContentDescription("Foto del cliente");
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
            aviso("Para la foto del cliente elige una imagen.");
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
            error(etNombre, "Escribe el nombre del cliente.");
            return;
        }
        if (!montoValido(etCupo)) return;
        Modelo.Cliente c = cliente != null ? cliente : new Modelo.Cliente();
        c.nombre = nombre;
        c.telefono = texto(etTelefono);
        c.correo = texto(etCorreo);
        c.direccion = texto(etDireccion);
        c.notas = texto(etNotas);
        c.cupo = numero(etCupo);
        if (!rutaTemporal.isEmpty()) {
            c.foto = Archivos.moverAPermanente(this, rutaTemporal);
            rutaTemporal = "";
        } else {
            c.foto = rutaFoto == null ? "" : rutaFoto;
        }
        long guardado = bd.guardarCliente(c);
        if (quitarOriginal && !fotoOriginal.isEmpty()) {
            Archivos.borrar(fotoOriginal);
            quitarOriginal = false;
        }
        aviso("Cliente guardado.");
        volverConResultado(RESULT_OK, guardado);
    }

    private void eliminar() {
        if (cliente == null) return;
        if (bd.referenciasCliente(cliente.id) > 0) {
            informar("No se puede eliminar",
                    "Este cliente tiene ventas o abonos registrados. Para conservar el historial "
                            + "no se puede borrar, pero puedes editarlo.");
            return;
        }
        confirmar("Eliminar cliente", "¿Eliminar a “" + cliente.nombre + "”?",
                "Eliminar", () -> {
                    if (bd.eliminarCliente(cliente.id)) {
                        if (!fotoOriginal.isEmpty()) Archivos.borrar(fotoOriginal);
                        aviso("Cliente eliminado.");
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        aviso("No se pudo eliminar el cliente.");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && !rutaTemporal.isEmpty()) Archivos.borrar(rutaTemporal);
    }
}
