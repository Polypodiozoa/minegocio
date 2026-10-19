package com.polypodiozoa.minegocio.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.util.Imagen;
import com.polypodiozoa.minegocio.util.Ui;

import java.util.ArrayList;
import java.util.List;

/** Adaptador común para las listas: misma fila, distinta información. */
public class AdaptadorFilas extends BaseAdapter {

    public static class Fila {
        public long id;
        public String titulo = "";
        public String subtitulo = "";
        public String valor = "";
        public String estado = "";
        public String foto = "";
        public int iconoDefecto = R.drawable.ic_caja;
        public String descripcion = "";
        public Object dato;
    }

    public interface OnFilaClick {
        void alHacerClick(Fila fila);
    }

    private final Context contexto;
    private final List<Fila> filas = new ArrayList<>();
    private final LayoutInflater inflador;
    private OnFilaClick onFilaClick;

    public void setOnFilaClick(OnFilaClick escucha) {
        this.onFilaClick = escucha;
    }

    public AdaptadorFilas(Context contexto) {
        this.contexto = contexto;
        this.inflador = LayoutInflater.from(contexto);
    }

    public void setFilas(List<Fila> nuevas) {
        filas.clear();
        if (nuevas != null) filas.addAll(nuevas);
        notifyDataSetChanged();
    }

    public Fila getItem(int posicion) {
        return filas.get(posicion);
    }

    @Override
    public int getCount() {
        return filas.size();
    }

    @Override
    public long getItemId(int posicion) {
        return filas.get(posicion).id;
    }

    @Override
    public View getView(int posicion, View convertida, ViewGroup padre) {
        View vista = convertida;
        if (vista == null) {
            vista = inflador.inflate(R.layout.fila_lista, padre, false);
        }
        Fila fila = filas.get(posicion);
        TextView titulo = vista.findViewById(R.id.tituloFila);
        TextView subtitulo = vista.findViewById(R.id.subtituloFila);
        TextView valor = vista.findViewById(R.id.valorFila);
        TextView badge = vista.findViewById(R.id.badgeFila);
        ImageView foto = vista.findViewById(R.id.fotoFila);

        titulo.setText(fila.titulo);
        if (fila.subtitulo == null || fila.subtitulo.isEmpty()) {
            subtitulo.setVisibility(View.GONE);
        } else {
            subtitulo.setVisibility(View.VISIBLE);
            subtitulo.setText(fila.subtitulo);
        }
        if (fila.valor == null || fila.valor.isEmpty()) {
            valor.setVisibility(View.GONE);
        } else {
            valor.setVisibility(View.VISIBLE);
            valor.setText(fila.valor);
        }
        Ui.ponerBadge(badge, fila.estado);

        if (fila.foto != null && !fila.foto.isEmpty()) {
            foto.setVisibility(View.VISIBLE);
            foto.setPadding(0, 0, 0, 0);
            foto.setColorFilter(null);
            Imagen.mostrar(foto, fila.foto, fila.iconoDefecto, 240);
        } else if (fila.iconoDefecto > 0) {
            foto.setVisibility(View.VISIBLE);
            int p = Ui.dp(contexto, 12);
            foto.setPadding(p, p, p, p);
            foto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            foto.setImageResource(fila.iconoDefecto);
            foto.setColorFilter(contexto.getColor(R.color.primario));
        } else {
            foto.setVisibility(View.GONE);
        }

        String descripcion = fila.descripcion;
        if (descripcion == null || descripcion.isEmpty()) {
            StringBuilder sb = new StringBuilder(fila.titulo);
            if (fila.subtitulo != null && !fila.subtitulo.isEmpty()) sb.append(", ").append(fila.subtitulo);
            if (fila.valor != null && !fila.valor.isEmpty()) sb.append(", ").append(fila.valor);
            if (fila.estado != null && !fila.estado.isEmpty()) sb.append(", ").append(fila.estado);
            descripcion = sb.toString();
        }
        vista.setContentDescription(descripcion);
        if (onFilaClick != null) {
            vista.setClickable(true);
            vista.setFocusable(true);
            vista.setOnClickListener(v -> onFilaClick.alHacerClick(fila));
        }
        titulo.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        subtitulo.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        valor.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        badge.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return vista;
    }
}
