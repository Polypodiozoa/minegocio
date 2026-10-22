package com.polypodiozoa.minegocio.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.MenuPrincipal;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.List;

/** Orden personal de los botones de «¿Qué quieres hacer?». */
public class MenuPerfilActivity extends BaseActivity {

    private LinearLayout lista;
    private Modelo.Usuario usuario;
    private List<MenuPrincipal.Opcion> opciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TemaAdmin);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_perfil);
        configurarBarra("Mi menú");
        lista = findViewById(R.id.listaMenu);
        usuario = bd.usuarioActual();
        if (usuario == null) {
            finish();
            return;
        }
        opciones = MenuPrincipal.opciones(usuario.menu);
        findViewById(R.id.btnRestaurar).setOnClickListener(v -> {
            bd.guardarMenuUsuario(usuario.id, MenuPrincipal.ordenDefecto());
            opciones = MenuPrincipal.opciones(MenuPrincipal.ordenDefecto());
            cargar();
            aviso("Menú restaurado al orden recomendado.");
        });
        cargar();
    }

    private void cargar() {
        lista.removeAllViews();
        for (int i = 0; i < opciones.size(); i++) {
            final int indice = i;
            MenuPrincipal.Opcion o = opciones.get(i);
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.CENTER_VERTICAL);
            fila.setBackgroundResource(R.drawable.fondo_tarjeta_admin);
            fila.setPadding(Ui.dp(this, 10), Ui.dp(this, 8),
                    Ui.dp(this, 6), Ui.dp(this, 8));
            fila.setMinimumHeight(Ui.dp(this, 64));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = Ui.dp(this, 8);
            fila.setLayoutParams(lp);

            ImageView icono = new ImageView(this);
            icono.setImageResource(o.icono);
            icono.setColorFilter(getColor(R.color.admin_verde));
            LinearLayout.LayoutParams lpIcono = new LinearLayout.LayoutParams(
                    Ui.dp(this, 28), Ui.dp(this, 28));
            lpIcono.setMarginEnd(Ui.dp(this, 10));
            icono.setLayoutParams(lpIcono);
            icono.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            fila.addView(icono);

            LinearLayout textos = new LinearLayout(this);
            textos.setOrientation(LinearLayout.VERTICAL);
            textos.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            TextView titulo = Ui.texto(this, o.etiqueta, 17,
                    getColor(R.color.admin_verde_texto), true);
            textos.addView(titulo);
            if (o.principal) {
                textos.addView(Ui.texto(this, "Acción principal", 13,
                        getColor(R.color.admin_verde), false));
            }
            fila.addView(textos);

            Button subir = boton("Subir", "Subir " + o.etiqueta);
            subir.setEnabled(i > 0);
            subir.setOnClickListener(v -> mover(indice, -1));
            fila.addView(subir);

            Button bajar = boton("Bajar", "Bajar " + o.etiqueta);
            bajar.setEnabled(i < opciones.size() - 1);
            bajar.setOnClickListener(v -> mover(indice, 1));
            fila.addView(bajar);

            fila.setContentDescription(o.etiqueta + ". Posición " + (i + 1)
                    + " de " + opciones.size() + ".");
            lista.addView(fila);
        }
    }

    private Button boton(String texto, String descripcion) {
        Button b = new Button(this);
        b.setText(texto);
        b.setTextSize(13);
        b.setTextColor(getColor(R.color.admin_verde));
        b.setAllCaps(false);
        b.setBackgroundResource(R.drawable.boton_admin_texto);
        b.setMinWidth(Ui.dp(this, 58));
        b.setMinHeight(Ui.dp(this, 48));
        b.setContentDescription(descripcion);
        return b;
    }

    private void mover(int indice, int delta) {
        int destino = indice + delta;
        if (destino < 0 || destino >= opciones.size()) return;
        MenuPrincipal.Opcion o = opciones.remove(indice);
        opciones.add(destino, o);
        guardar();
        cargar();
        Ui.anunciar(lista, o.etiqueta + " movido a la posición " + (destino + 1));
    }

    private void guardar() {
        StringBuilder s = new StringBuilder();
        for (MenuPrincipal.Opcion o : opciones) {
            if (s.length() > 0) s.append(',');
            s.append(o.clave);
        }
        bd.guardarMenuUsuario(usuario.id, s.toString());
    }
}
