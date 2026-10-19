package com.polypodiozoa.minegocio.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import com.polypodiozoa.minegocio.datos.BD;
import com.polypodiozoa.minegocio.modelo.Modelo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/** Genera un comprobante de venta en PDF, sin librerías externas. */
public final class ComprobantePdf {

    private static final int ANCHO = 595;
    private static final int ALTO = 842;
    private static final int MARGEN = 40;

    private ComprobantePdf() {}

    private static void derecha(Canvas c, String texto, float x, float y, Paint p) {
        c.drawText(texto, x - p.measureText(texto), y, p);
    }

    public static void generar(Context ctx, Modelo.Venta venta, List<Modelo.Item> items,
                               Uri destino) throws IOException {
        BD bd = BD.get(ctx);
        PdfDocument documento = new PdfDocument();
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.BLACK);

        int pagina = 1;
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(ANCHO, ALTO, pagina).create();
        PdfDocument.Page hoja = documento.startPage(info);
        Canvas c = hoja.getCanvas();
        int y = MARGEN;

        p.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        p.setTextSize(21);
        c.drawText(bd.nombreNegocio(), MARGEN, y, p);
        y += 20;

        p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        p.setTextSize(10);
        String nit = bd.ajuste("negocio_nit", "");
        String tel = bd.ajuste("negocio_telefono", "");
        String dir = bd.ajuste("negocio_direccion", "");
        if (!nit.isEmpty()) {
            c.drawText("NIT: " + nit, MARGEN, y, p);
            y += 14;
        }
        if (!dir.isEmpty()) {
            c.drawText(dir, MARGEN, y, p);
            y += 14;
        }
        if (!tel.isEmpty()) {
            c.drawText("Teléfono: " + tel, MARGEN, y, p);
            y += 14;
        }
        y += 6;
        p.setStrokeWidth(1);
        c.drawLine(MARGEN, y, ANCHO - MARGEN, y, p);
        y += 26;

        p.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        p.setTextSize(15);
        c.drawText("COMPROBANTE DE VENTA", MARGEN, y, p);
        p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        p.setTextSize(11);
        c.drawText("Venta #" + venta.id, ANCHO - MARGEN - 110, y, p);
        y += 18;
        c.drawText(Fechas.fechaHora(venta.fecha), MARGEN, y, p);
        y += 24;

        p.setTextSize(11);
        c.drawText("Cliente: " + (venta.clienteNombre.isEmpty() ? "Consumidor final" : venta.clienteNombre),
                MARGEN, y, p);
        y += 16;
        c.drawText("Forma de pago: " + venta.formaPago, MARGEN, y, p);
        y += 16;
        if (venta.anulada) {
            p.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            p.setTextSize(12);
            c.drawText("ESTA VENTA FUE ANULADA", MARGEN, y, p);
            p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            p.setTextSize(11);
            y += 16;
        }
        y += 6;

        p.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        p.setTextSize(10);
        c.drawText("Descripción", MARGEN, y, p);
        c.drawText("Cant.", 350, y, p);
        c.drawText("Precio", 400, y, p);
        c.drawText("Subtotal", 490, y, p);
        y += 6;
        c.drawLine(MARGEN, y, ANCHO - MARGEN, y, p);
        y += 15;
        p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        p.setTextSize(10);

        for (Modelo.Item i : items) {
            if (y > 730) {
                documento.finishPage(hoja);
                pagina++;
                info = new PdfDocument.PageInfo.Builder(ANCHO, ALTO, pagina).create();
                hoja = documento.startPage(info);
                c = hoja.getCanvas();
                y = MARGEN + 10;
            }
            String descripcion = i.descripcion;
            if (descripcion.length() > 42) descripcion = descripcion.substring(0, 41) + "…";
            c.drawText(descripcion, MARGEN, y, p);
            c.drawText(String.valueOf(i.cantidad), 355, y, p);
            c.drawText(Formatos.pesos(i.precioUnitario), 400, y, p);
            c.drawText(Formatos.pesos(i.precioUnitario * i.cantidad), 490, y, p);
            y += 15;
        }

        y += 4;
        c.drawLine(300, y, ANCHO - MARGEN, y, p);
        y += 16;
        p.setTextSize(10);
        p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        c.drawText("Subtotal", 300, y, p);
        derecha(c, Formatos.pesos(venta.subtotal), ANCHO - MARGEN, y, p);
        y += 14;
        if (venta.descuento > 0) {
            c.drawText("Descuento", 300, y, p);
            derecha(c, "- " + Formatos.pesos(venta.descuento), ANCHO - MARGEN, y, p);
            y += 14;
        }
        if (venta.impuesto > 0) {
            c.drawText("Impuesto (" + Formatos.porcentaje(venta.impuestoPct) + ")", 300, y, p);
            derecha(c, Formatos.pesos(venta.impuesto), ANCHO - MARGEN, y, p);
            y += 14;
        }
        p.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        p.setTextSize(13);
        c.drawText("TOTAL", 300, y, p);
        derecha(c, Formatos.pesos(venta.total), ANCHO - MARGEN, y, p);
        y += 26;

        if (venta.notas != null && !venta.notas.isEmpty()) {
            p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            p.setTextSize(10);
            c.drawText("Notas: " + venta.notas, MARGEN, y, p);
            y += 16;
        }
        p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        p.setTextSize(10);
        c.drawText("Gracias por tu compra.", MARGEN, ALTO - 58, p);
        c.drawText("Generado con Mi Negocio", MARGEN, ALTO - 44, p);

        documento.finishPage(hoja);
        OutputStream salida = ctx.getContentResolver().openOutputStream(destino, "w");
        if (salida == null) {
            documento.close();
            throw new IOException("No se pudo abrir el destino.");
        }
        try (OutputStream out = salida) {
            documento.writeTo(out);
        } finally {
            documento.close();
        }
    }
}
