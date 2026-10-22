package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.List;

/** Lista editable de personas. Página de administración verde. */
public class UsuariosActivity extends BaseActivity {

    private LinearLayout lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TemaAdmin);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);
        configurarBarra("Editar personas");
        lista = findViewById(R.id.listaUsuarios);
        findViewById(R.id.btnAgregar).setOnClickListener(v ->
                startActivity(new Intent(this, UsuarioActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        lista.removeAllViews();
        List<Modelo.Usuario> usuarios = bd.usuarios();
        Modelo.Usuario actual = bd.usuarioActual();
        for (Modelo.Usuario u : usuarios) {
            lista.addView(fila(u, actual != null && actual.id == u.id));
        }
    }

    private View fila(Modelo.Usuario u, boolean actual) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setBackgroundResource(R.drawable.fondo_tarjeta_admin);
        fila.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), Ui.dp(this, 8), Ui.dp(this, 10));
        fila.setMinimumHeight(Ui.dp(this, 72));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Ui.dp(this, 8);
        fila.setLayoutParams(lp);

        TextView inicial = new TextView(this);
        inicial.setText(inicial(u.nombre));
        inicial.setTextColor(getColor(R.color.admin_verde));
        inicial.setTextSize(20);
        inicial.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        inicial.setGravity(Gravity.CENTER);
        inicial.setBackgroundResource(R.drawable.fondo_avatar_admin);
        LinearLayout.LayoutParams lpAvatar = new LinearLayout.LayoutParams(
                Ui.dp(this, 44), Ui.dp(this, 44));
        lpAvatar.setMarginEnd(Ui.dp(this, 10));
        inicial.setLayoutParams(lpAvatar);
        fila.addView(inicial);

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        textos.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView nombre = Ui.texto(this, u.nombre, 17, getColor(R.color.admin_verde_texto), true);
        nombre.setMaxLines(1);
        nombre.setEllipsize(android.text.TextUtils.TruncateAt.END);
        textos.addView(nombre);
        textos.addView(Ui.texto(this, u.rolVisible() + (actual ? " · En uso" : ""),
                14, getColor(R.color.texto_secundario), false));
        fila.addView(textos);

        Button editar = new Button(this);
        editar.setText("Editar");
        editar.setTextSize(15);
        editar.setTextColor(getColor(R.color.admin_verde));
        editar.setAllCaps(false);
        editar.setBackgroundResource(R.drawable.boton_admin_texto);
        editar.setMinWidth(Ui.dp(this, 72));
        editar.setMinHeight(Ui.dp(this, 48));
        editar.setContentDescription("Editar a " + u.nombre);
        editar.setOnClickListener(v -> {
            Intent i = new Intent(this, UsuarioActivity.class);
            i.putExtra("id", u.id);
            startActivity(i);
        });
        fila.addView(editar);
        fila.setContentDescription(u.nombre + ", " + u.rolVisible()
                + (actual ? ", cuenta en uso" : "") + ". Toca Editar para cambiar sus datos.");
        return fila;
    }

    private String inicial(String nombre) {
        String n = nombre == null ? "" : nombre.trim();
        if (n.isEmpty()) return "?";
        return n.substring(0, 1).toUpperCase(java.util.Locale.getDefault());
    }
}
