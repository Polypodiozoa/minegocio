package com.polypodiozoa.minegocio.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;

/** Alta y edición de una persona. Página de administración verde. */
public class UsuarioActivity extends BaseActivity {

    private EditText etNombre;
    private Spinner spRol;
    private Button btnDesactivar;
    private long id;
    private Modelo.Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TemaAdmin);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);
        id = idExtra("id");
        configurarBarra(id > 0 ? "Editar persona" : "Nueva persona");
        etNombre = findViewById(R.id.etNombreUsuario);
        spRol = findViewById(R.id.spRol);
        btnDesactivar = findViewById(R.id.btnDesactivar);

        ArrayAdapter<String> roles = new ArrayAdapter<>(this, R.layout.item_spinner,
                new String[]{"Empleado", "Administrador"});
        roles.setDropDownViewResource(R.layout.item_spinner);
        spRol.setAdapter(roles);

        if (id > 0) {
            usuario = bd.usuario(id);
            if (usuario == null) {
                aviso("La persona ya no existe.");
                finish();
                return;
            }
            etNombre.setText(usuario.nombre);
            spRol.setSelection(usuario.esAdministrador() ? 1 : 0);
            btnDesactivar.setVisibility(View.VISIBLE);
        }
        marcarInicial(etNombre);
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
        btnDesactivar.setOnClickListener(v -> desactivar());
    }

    @Override
    public void onBackPressed() {
        salirSiCambios();
    }

    private void guardar() {
        String nombre = texto(etNombre);
        if (nombre.isEmpty()) {
            error(etNombre, "Escribe el nombre de la persona.");
            return;
        }
        Modelo.Usuario u = usuario != null ? usuario : new Modelo.Usuario();
        u.nombre = nombre;
        u.rol = spRol.getSelectedItemPosition() == 1 ? "admin" : "empleado";
        long guardado = bd.guardarUsuario(u);
        if (guardado <= 0) {
            aviso("No se pudo guardar la persona.");
            return;
        }
        aviso("Persona guardada.");
        finish();
    }

    private void desactivar() {
        if (usuario == null) return;
        if (bd.usuarios().size() <= 1) {
            informar("No se puede desactivar", "Debe quedar al menos una persona activa.");
            return;
        }
        confirmar("Desactivar persona",
                "¿Desactivar a “" + usuario.nombre + "”? Sus ventas y compras se conservan, "
                        + "pero no aparecerá en el selector.",
                "Desactivar", () -> {
                    if (bd.desactivarUsuario(usuario.id)) {
                        aviso("Persona desactivada. Su historial se conserva.");
                        finish();
                    } else {
                        aviso("No se pudo desactivar. Debe quedar una persona activa.");
                    }
                });
    }
}
