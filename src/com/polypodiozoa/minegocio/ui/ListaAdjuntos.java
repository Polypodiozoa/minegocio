package com.polypodiozoa.minegocio.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polypodiozoa.minegocio.R;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Archivos;
import com.polypodiozoa.minegocio.util.Imagen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Lista de comprobantes pendientes de guardar, con vista previa, abrir y quitar. */
public class ListaAdjuntos {

    private final BaseActivity actividad;
    private final LinearLayout contenedor;
    private final List<Modelo.Adjunto> adjuntos = new ArrayList<>();
    private final LayoutInflater inflador;

    public ListaAdjuntos(BaseActivity actividad, LinearLayout contenedor) {
        this.actividad = actividad;
        this.contenedor = contenedor;
        this.inflador = LayoutInflater.from(actividad);
    }

    public void agregar(Modelo.Adjunto adjunto) {
        if (adjunto == null) return;
        adjuntos.add(adjunto);
        render();
    }

    public List<Modelo.Adjunto> lista() {
        return adjuntos;
    }

    public void render() {
        contenedor.removeAllViews();
        for (int i = 0; i < adjuntos.size(); i++) {
            final int posicion = i;
            final Modelo.Adjunto a = adjuntos.get(i);
            View fila = inflador.inflate(R.layout.fila_adjunto, contenedor, false);
            ImageView imagen = fila.findViewById(R.id.imgAdjunto);
            TextView nombre = fila.findViewById(R.id.txtNombreAdjunto);
            TextView tipo = fila.findViewById(R.id.txtTipoAdjunto);
            ImageButton abrir = fila.findViewById(R.id.btnAbrirAdjunto);
            ImageButton quitar = fila.findViewById(R.id.btnQuitarAdjunto);

            nombre.setText(a.nombre == null || a.nombre.isEmpty()
                    ? "Comprobante" : a.nombre);
            boolean esImagen = Archivos.esImagen(a.ruta, a.mime);
            if (esImagen) {
                imagen.setPadding(0, 0, 0, 0);
                Imagen.mostrar(imagen, a.ruta, R.drawable.ic_imagen, 180);
                tipo.setText("Imagen");
            } else {
                imagen.setPadding(10, 10, 10, 10);
                imagen.setImageResource(R.drawable.ic_documento);
                imagen.setColorFilter(actividad.getColor(R.color.primario));
                tipo.setText(a.mime != null && a.mime.contains("pdf") ? "Documento PDF" : "Documento");
            }
            abrir.setContentDescription("Abrir " + nombre.getText());
            quitar.setContentDescription("Quitar " + nombre.getText());
            abrir.setOnClickListener(v -> actividad.abrirAdjunto(a));
            quitar.setOnClickListener(v -> {
                if (a.ruta != null && a.ruta.startsWith(Archivos.dirTemporal(actividad).getAbsolutePath())) {
                    Archivos.borrar(a.ruta);
                }
                adjuntos.remove(posicion);
                render();
            });
            contenedor.addView(fila);
            if (i < adjuntos.size() - 1) {
                View sep = new View(actividad);
                sep.setBackgroundColor(actividad.getColor(R.color.borde));
                sep.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                contenedor.addView(sep);
            }
        }
    }

    /** Mueve los archivos temporales a la carpeta definitiva antes de guardar en la base. */
    public List<Modelo.Adjunto> finalizar() {
        List<Modelo.Adjunto> resultado = new ArrayList<>();
        for (Modelo.Adjunto a : adjuntos) {
            if (a.ruta == null || a.ruta.isEmpty()) continue;
            if (a.ruta.startsWith(Archivos.dirTemporal(actividad).getAbsolutePath())) {
                a.ruta = Archivos.moverAPermanente(actividad, a.ruta);
            }
            resultado.add(a);
        }
        adjuntos.clear();
        render();
        return resultado;
    }

    public void limpiarTemporales() {
        for (Modelo.Adjunto a : adjuntos) {
            if (a.ruta != null && a.ruta.startsWith(Archivos.dirTemporal(actividad).getAbsolutePath())) {
                Archivos.borrar(a.ruta);
            }
        }
        adjuntos.clear();
        render();
    }

    public void eliminarArchivoDe(Modelo.Adjunto a) {
        if (a != null && a.ruta != null) Archivos.borrar(a.ruta);
    }

    public boolean estaVacia() {
        return adjuntos.isEmpty();
    }

    public static String nombreDeArchivo(String ruta) {
        return new File(ruta).getName();
    }
}
