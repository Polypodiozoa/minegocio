package com.polypodiozoa.minegocio.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;

/** Pequeños constructores de vistas para mantener un diseño consistente y accesible. */
public final class Ui {

    private Ui() {}

    public static int dp(Context ctx, float valor) {
        return Math.round(valor * ctx.getResources().getDisplayMetrics().density);
    }

    public static TextView texto(Context ctx, String contenido, float tamanoSp,
                                 int color, boolean negrita) {
        TextView t = new TextView(ctx);
        t.setText(contenido);
        t.setTextSize(tamanoSp);
        t.setTextColor(color);
        if (negrita) t.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        t.setLineSpacing(0f, 1.12f);
        return t;
    }

    public static View tarjetaAccion(Context ctx, int icono, String etiqueta, View.OnClickListener click) {
        return tarjetaAccion(ctx, icono, etiqueta, false, click);
    }

    public static View tarjetaAccion(Context ctx, int icono, String etiqueta,
                                     boolean principal, View.OnClickListener click) {
        LinearLayout fila = new LinearLayout(ctx);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setBackgroundResource(principal
                ? R.drawable.fondo_tarjeta_principal : R.drawable.fondo_tarjeta);
        int p = dp(ctx, 12);
        fila.setPadding(p, p, p, p);
        fila.setMinimumHeight(dp(ctx, 78));
        fila.setClickable(true);
        fila.setFocusable(true);
        fila.setContentDescription(etiqueta);
        fila.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            fila.setForeground(ctx.getDrawable(R.drawable.selector_foreground));
        }

        ImageView imagen = new ImageView(ctx);
        int tam = dp(ctx, 34);
        LinearLayout.LayoutParams lpImagen = new LinearLayout.LayoutParams(tam, tam);
        lpImagen.setMarginEnd(dp(ctx, 12));
        imagen.setLayoutParams(lpImagen);
        imagen.setImageResource(icono);
        imagen.setColorFilter(ctx.getColor(R.color.primario));
        imagen.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        fila.addView(imagen);

        TextView t = texto(ctx, etiqueta, 17, ctx.getColor(R.color.texto), true);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        t.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        fila.addView(t);

        fila.setOnClickListener(click);
        return fila;
    }

    public static void agregarAccion(GridLayout grid, View vista) {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(dp(grid.getContext(), 4), dp(grid.getContext(), 4),
                dp(grid.getContext(), 4), dp(grid.getContext(), 4));
        grid.addView(vista, lp);
    }

    public static View filaResumen(Context ctx, String etiqueta, String valor) {
        LinearLayout fila = new LinearLayout(ctx);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
        fila.setContentDescription(etiqueta + ": " + valor);
        TextView l = texto(ctx, etiqueta, 16, ctx.getColor(R.color.texto_secundario), false);
        l.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        l.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        TextView v = texto(ctx, valor, 19, ctx.getColor(R.color.texto), true);
        v.setGravity(Gravity.END);
        v.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        fila.addView(l);
        fila.addView(v);
        return fila;
    }

    public static View separador(Context ctx) {
        View v = new View(ctx);
        v.setBackgroundColor(ctx.getColor(R.color.borde));
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(ctx, 1))));
        return v;
    }

    public static void ponerBadge(TextView vista, String estado) {
        if (estado == null || estado.isEmpty()) {
            vista.setVisibility(View.GONE);
            return;
        }
        vista.setVisibility(View.VISIBLE);
        vista.setText(estado);
        String e = estado.toLowerCase(Formatos.COLOMBIA);
        Context ctx = vista.getContext();
        int fondo;
        int colorTexto;
        if (e.contains("vencida")) {
            fondo = R.drawable.fondo_badge_rojo;
            colorTexto = R.color.error;
        } else if (e.contains("por vencer")) {
            fondo = R.drawable.fondo_badge_ambar;
            colorTexto = R.color.advertencia;
        } else if (e.contains("vigente")) {
            fondo = R.drawable.fondo_badge_verde;
            colorTexto = R.color.exito;
        } else if (e.contains("sin fecha")) {
            fondo = R.drawable.fondo_badge_gris;
            colorTexto = R.color.gris_texto;
        } else if (e.contains("al día") || e.contains("al dia") || e.contains("a favor")) {
            fondo = R.drawable.fondo_badge_verde;
            colorTexto = R.color.exito;
        } else if (e.contains("debe")) {
            fondo = R.drawable.fondo_badge_ambar;
            colorTexto = R.color.advertencia;
        } else if (e.contains("pagada") || e.contains("disponible") || e.contains("activo")
                || e.contains("completa")) {
            fondo = R.drawable.fondo_badge_verde;
            colorTexto = R.color.exito;
        } else if (e.contains("crédito") || e.contains("credito") || e.contains("bajo")
                || e.contains("pendiente")) {
            fondo = R.drawable.fondo_badge_ambar;
            colorTexto = R.color.advertencia;
        } else if (e.contains("anulada") || e.contains("agotado") || e.contains("eliminado")) {
            fondo = R.drawable.fondo_badge_rojo;
            colorTexto = R.color.error;
        } else if (e.contains("archivado")) {
            fondo = R.drawable.fondo_badge_gris;
            colorTexto = R.color.gris_texto;
        } else {
            fondo = R.drawable.fondo_badge_azul;
            colorTexto = R.color.primario;
        }
        vista.setBackgroundResource(fondo);
        vista.setTextColor(ctx.getColor(colorTexto));
    }

    public static String estadoVenta(Modelo.Venta v) {
        if (v == null) return "";
        if (v.anulada) return "Anulada";
        if ("credito".equals(v.estado)) {
            return v.saldo <= 0 ? "Crédito pagado" : "Crédito";
        }
        return "Pagada";
    }

    public static String estadoCompra(Modelo.Compra c) {
        if (c == null) return "";
        return c.anulada ? "Anulada" : "Recibida";
    }

    public static String estadoStock(Modelo.Producto p) {
        if (p == null) return "";
        if (p.stock <= 0) return "Agotado";
        if (p.stock <= p.stockMinimo) return "Bajo";
        return "";
    }

    public static void anunciar(View vista, String mensaje) {
        if (vista != null && mensaje != null && !mensaje.isEmpty()) {
            vista.announceForAccessibility(mensaje);
        }
    }

    public static void estiloFiltro(android.widget.TextView boton, boolean activo) {
        Context ctx = boton.getContext();
        boton.setBackgroundResource(activo ? R.drawable.boton_primario : R.drawable.boton_secundario);
        boton.setTextColor(ctx.getColor(activo ? R.color.blanco : R.color.primario));
        boton.setContentDescription(boton.getText() + (activo ? ", filtro activo" : ""));
    }

    public static String unidades(int cantidad, String unidad) {
        String u = unidad == null || unidad.trim().isEmpty() ? "unidad" : unidad.trim();
        if (cantidad == 1) return cantidad + " " + u;
        if (u.equalsIgnoreCase("unidad")) return cantidad + " unidades";
        if (u.endsWith("s")) return cantidad + " " + u;
        return cantidad + " " + u + "s";
    }

    public static int colorEstado(Context ctx, String estado) {
        if (estado == null) return ctx.getColor(R.color.texto);
        String e = estado.toLowerCase(Formatos.COLOMBIA);
        if (e.contains("anulada") || e.contains("agotado")) return ctx.getColor(R.color.error);
        if (e.contains("crédito") || e.contains("credito") || e.contains("bajo"))
            return ctx.getColor(R.color.advertencia);
        return ctx.getColor(R.color.exito);
    }

    public static LinearLayout.LayoutParams margen(Context ctx, int izq, int arriba, int der, int abajo) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(ctx, izq), dp(ctx, arriba), dp(ctx, der), dp(ctx, abajo));
        return lp;
    }
}
