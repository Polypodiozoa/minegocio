package com.polypodiozoa.minegocio.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.List;

/** Selector de la persona que está usando la terminal. Página de administración verde. */
public class AccesosActivity extends BaseActivity {

    private GridLayout grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TemaAdmin);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accesos);
        configurarBarra("Administrador de Accesos");
        grid = findViewById(R.id.gridUsuarios);
        findViewById(R.id.btnAgregar).setOnClickListener(v ->
                startActivity(new Intent(this, UsuarioActivity.class)));
        findViewById(R.id.btnEditar).setOnClickListener(v ->
                startActivity(new Intent(this, UsuariosActivity.class)));
        if (getResources().getConfiguration().screenWidthDp >= 600) {
            grid.setColumnCount(3);
        } else {
            grid.setColumnCount(2);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        grid.removeAllViews();
        List<Modelo.Usuario> usuarios = bd.usuarios();
        Modelo.Usuario actual = bd.usuarioActual();
        for (Modelo.Usuario u : usuarios) {
            grid.addView(tarjeta(u, actual != null && actual.id == u.id), parametros());
        }
    }

    private GridLayout.LayoutParams parametros() {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        int m = Ui.dp(this, 6);
        lp.setMargins(m, m, m, m);
        return lp;
    }

    private View tarjeta(Modelo.Usuario u, boolean actual) {
        LinearLayout caja = new LinearLayout(this);
        caja.setOrientation(LinearLayout.VERTICAL);
        caja.setGravity(Gravity.CENTER_HORIZONTAL);
        caja.setPadding(Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14));
        caja.setMinimumHeight(Ui.dp(this, 148));
        caja.setBackgroundResource(actual
                ? R.drawable.fondo_tarjeta_admin_seleccionada : R.drawable.fondo_tarjeta_admin);
        caja.setClickable(true);
        caja.setFocusable(true);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            caja.setForeground(getDrawable(R.drawable.selector_foreground));
        }

        TextView inicial = new TextView(this);
        inicial.setText(inicial(u.nombre));
        inicial.setTextColor(getColor(R.color.admin_verde));
        inicial.setTextSize(26);
        inicial.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        inicial.setGravity(Gravity.CENTER);
        inicial.setBackgroundResource(R.drawable.fondo_avatar_admin);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                Ui.dp(this, 58), Ui.dp(this, 58));
        inicial.setLayoutParams(lp);
        caja.addView(inicial);

        TextView nombre = Ui.texto(this, u.nombre, 17, getColor(R.color.admin_verde_texto), true);
        nombre.setGravity(Gravity.CENTER);
        nombre.setMaxLines(2);
        nombre.setEllipsize(android.text.TextUtils.TruncateAt.END);
        nombre.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        nombre.setPadding(0, Ui.dp(this, 8), 0, 0);
        caja.addView(nombre);

        TextView rol = Ui.texto(this, u.rolVisible(), 14, getColor(R.color.texto_secundario), false);
        rol.setGravity(Gravity.CENTER);
        caja.addView(rol);

        if (actual) {
            TextView enUso = Ui.texto(this, "En uso", 14, getColor(R.color.admin_verde), true);
            enUso.setGravity(Gravity.CENTER);
            enUso.setPadding(0, Ui.dp(this, 4), 0, 0);
            caja.addView(enUso);
        }

        String descripcion = u.nombre + ", " + u.rolVisible()
                + (actual ? ", cuenta en uso" : "") + ". Toca para usar esta cuenta.";
        caja.setContentDescription(descripcion);
        caja.setOnClickListener(v -> {
            if (actual) {
                finish();
                return;
            }
            bd.cambiarUsuario(u.id);
            aviso("Ahora usa Mi Negocio: " + u.nombre + ".");
            setResult(RESULT_OK);
            finish();
        });
        return caja;
    }

    private String inicial(String nombre) {
        String n = nombre == null ? "" : nombre.trim();
        if (n.isEmpty()) return "?";
        return n.substring(0, 1).toUpperCase(java.util.Locale.getDefault());
    }
}
