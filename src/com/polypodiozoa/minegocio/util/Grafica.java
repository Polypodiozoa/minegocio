package com.polypodiozoa.minegocio.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.polypodiozoa.minegocio.modelo.Modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Gráfica sencilla dibujada a mano (sin librerías): barras para la serie
 * principal y una línea para la segunda. Pensada para leerse de un vistazo en
 * una tablet de 800 px, sin gestos ni zoom.
 */
public class Grafica extends View {

    private List<Modelo.PuntoSerie> datos = new ArrayList<>();
    private boolean conLinea;
    private int colorBarra;
    private int colorLinea;
    private String descripcion = "";

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint texto = new Paint(Paint.ANTI_ALIAS_FLAG);
    /* Reutilizada en cada dibujo: crear un RectF por barra hacía basura en onDraw. */
    private final RectF barra = new RectF();

    public Grafica(Context ctx) {
        super(ctx);
        colorBarra = ctx.getColor(com.polypodiozoa.minegocio.R.color.primario);
        colorLinea = ctx.getColor(com.polypodiozoa.minegocio.R.color.advertencia);
        texto.setColor(ctx.getColor(com.polypodiozoa.minegocio.R.color.texto_secundario));
        texto.setTextSize(11 * ctx.getResources().getDisplayMetrics().scaledDensity);
    }

    public void mostrar(List<Modelo.PuntoSerie> datos, boolean conSegundaSerie, String descripcion) {
        this.datos = datos == null ? new ArrayList<>() : datos;
        this.conLinea = conSegundaSerie;
        this.descripcion = descripcion == null ? "" : descripcion;
        setContentDescription(this.descripcion);
        invalidate();
    }

    @Override
    protected void onMeasure(int ancho, int alto) {
        int w = MeasureSpec.getSize(ancho);
        int h = (int) (150 * getResources().getDisplayMetrics().density);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (datos.isEmpty()) return;
        float margenAbajo = 18 * getResources().getDisplayMetrics().density;
        float altoUtil = getHeight() - margenAbajo - 4;
        long maximo = 1;
        for (Modelo.PuntoSerie d : datos) {
            maximo = Math.max(maximo, Math.max(d.valor, d.valor2));
        }
        int n = datos.size();
        float anchoBarra = (float) getWidth() / n;
        float separacion = Math.max(2f, anchoBarra * 0.18f);

        /* Línea base: un gris suave, para no competir con los datos. */
        p.setColor(Color.argb(40, 0, 0, 0));
        p.setStrokeWidth(1);
        c.drawLine(0, getHeight() - margenAbajo, getWidth(), getHeight() - margenAbajo, p);

        for (int i = 0; i < n; i++) {
            Modelo.PuntoSerie d = datos.get(i);
            float alto = altoUtil * (float) d.valor / maximo;
            float izq = i * anchoBarra + separacion / 2;
            p.setColor(colorBarra);
            p.setAlpha(200);
            barra.set(izq, getHeight() - margenAbajo - alto,
                    izq + anchoBarra - separacion, getHeight() - margenAbajo);
            c.drawRoundRect(barra, 3, 3, p);
            p.setAlpha(255);

            /* Etiqueta del primer y último punto: suficiente para ubicarse. */
            if (i == 0 || i == n - 1) {
                float anchoTexto = texto.measureText(d.etiqueta);
                float x = i == 0 ? izq : izq + anchoBarra - separacion - anchoTexto;
                c.drawText(d.etiqueta, Math.max(0, x), getHeight() - 4, texto);
            }
        }

        if (conLinea) {
            p.setColor(colorLinea);
            p.setStrokeWidth(2 * getResources().getDisplayMetrics().density);
            p.setStyle(Paint.Style.STROKE);
            float anteriorX = -1, anteriorY = -1;
            for (int i = 0; i < n; i++) {
                Modelo.PuntoSerie d = datos.get(i);
                float x = i * anchoBarra + anchoBarra / 2;
                float y = getHeight() - margenAbajo - altoUtil * (float) d.valor2 / maximo;
                if (anteriorX >= 0) c.drawLine(anteriorX, anteriorY, x, y, p);
                anteriorX = x;
                anteriorY = y;
            }
            p.setStyle(Paint.Style.FILL);
        }
    }
}