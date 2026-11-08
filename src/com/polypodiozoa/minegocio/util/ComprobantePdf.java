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

/**
 * Comprobante de venta y cotización en PDF, sin librerías externas.
 * Muestra el descuento, la base con descuento y una línea de IVA por tarifa:
 * el cliente ve exactamente qué paga y el negocio cuadra con su informe.
 */
public final class ComprobantePdf {

    private static final int ANCHO = 595;
    private static final int ALTO = 842;
    private static final int MARGEN = 40;

    private ComprobantePdf() {}

    public static void generar(Context ctx, Modelo.Venta venta, List<Modelo.Item> items,
                               Uri destino) throws IOException {
        Documento d = new Documento(ctx, destino);
        d.cabecera(BD.get(ctx), "COMPROBANTE DE VENTA", "Venta #" + venta.id, venta.fecha);
        d.cliente(venta.clienteNombre == null || venta.clienteNombre.isEmpty()
                ? "Consumidor final" : venta.clienteNombre);
        d.linea("Forma de pago: " + venta.formaPago);
        if (!venta.entregada) d.linea("Entrega: pendiente");
        if (venta.retroactiva) d.linea("Registrada con fecha anterior a hoy");
        if (venta.anulada) d.aviso("ESTA VENTA FUE ANULADA");
        d.items(items);
        d.totales(venta.subtotal, venta.descuento, venta.impuestoPct, venta.impuesto,
                venta.total, items);
        if (venta.notas != null && !venta.notas.isEmpty()) d.notas(venta.notas);
        d.cerrar();
    }

    public static void generarCotizacion(Context ctx, Modelo.Cotizacion cot,
                                         List<Modelo.Item> items, Uri destino) throws IOException {
        Documento d = new Documento(ctx, destino);
        d.cabecera(BD.get(ctx), "COTIZACIÓN", cot.numero, cot.fecha);
        d.cliente(cot.clienteNombre == null || cot.clienteNombre.isEmpty()
                ? "Cliente" : cot.clienteNombre);
        if (cot.validaHasta > 0) d.linea("Válida hasta: " + Fechas.fecha(cot.validaHasta));
        d.linea("Los precios pueden cambiar después de esta fecha.");
        d.items(items);
        d.totales(cot.subtotal, cot.descuento, cot.impuestoPct, cot.impuesto, cot.total, items);
        if (cot.notas != null && !cot.notas.isEmpty()) d.notas(cot.notas);
        d.cerrar();
    }

    /** Una hoja con salto de página automático. */
    private static final class Documento {
        final Context ctx;
        final Uri destino;
        final PdfDocument documento = new PdfDocument();
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        PdfDocument.Page hoja;
        Canvas c;
        int pagina = 1;
        int y = MARGEN;

        Documento(Context ctx, Uri destino) {
            this.ctx = ctx;
            this.destino = destino;
            p.setColor(Color.BLACK);
            nuevaHoja();
        }

        void nuevaHoja() {
            if (hoja != null) documento.finishPage(hoja);
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(ANCHO, ALTO, pagina).create();
            hoja = documento.startPage(info);
            c = hoja.getCanvas();
            y = MARGEN;
        }

        void salto(int alto) {
            if (y + alto > ALTO - 70) {
                pagina++;
                nuevaHoja();
            }
        }

        void cabecera(BD bd, String titulo, String numero, long fecha) {
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
            c.drawText(titulo, MARGEN, y, p);
            p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            p.setTextSize(11);
            c.drawText(numero, ANCHO - MARGEN - 110, y, p);
            y += 18;
            c.drawText(Fechas.fechaHora(fecha), MARGEN, y, p);
            y += 24;
        }

        void cliente(String nombre) {
            p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            p.setTextSize(11);
            c.drawText("Cliente: " + nombre, MARGEN, y, p);
            y += 16;
        }

        void linea(String texto) {
            p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            p.setTextSize(11);
            c.drawText(texto, MARGEN, y, p);
            y += 16;
        }

        void aviso(String texto) {
            p.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            p.setTextSize(12);
            c.drawText(texto, MARGEN, y, p);
            p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            p.setTextSize(11);
            y += 16;
        }

        void items(List<Modelo.Item> items) {
            y += 6;
            p.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            p.setTextSize(10);
            c.drawText("Descripción", MARGEN, y, p);
            c.drawText("Cant.", 340, y, p);
            c.drawText("Precio", 385, y, p);
            c.drawText("Subtotal", 490, y, p);
            y += 6;
            c.drawLine(MARGEN, y, ANCHO - MARGEN, y, p);
            y += 15;
            p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            p.setTextSize(10);

            for (Modelo.Item i : items) {
                salto(16);
                String descripcion = i.descripcion;
                if (descripcion.length() > 40) descripcion = descripcion.substring(0, 39) + "…";
                c.drawText(descripcion, MARGEN, y, p);
                c.drawText(String.valueOf(i.cantidad), 343, y, p);
                c.drawText(Formatos.pesos(i.precioUnitario), 385, y, p);
                derecha(c, Formatos.pesos(i.precioUnitario * i.cantidad), ANCHO - MARGEN, y, p);
                y += 15;
            }
        }

        void totales(long subtotal, long descuento, double pct, long impuesto, long total,
                     List<Modelo.Item> items) {
            y += 4;
            c.drawLine(300, y, ANCHO - MARGEN, y, p);
            y += 16;
            p.setTextSize(10);
            p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            c.drawText("Subtotal", 300, y, p);
            derecha(c, Formatos.pesos(subtotal), ANCHO - MARGEN, y, p);
            y += 14;
            if (descuento > 0) {
                c.drawText("Descuento", 300, y, p);
                derecha(c, "- " + Formatos.pesos(descuento), ANCHO - MARGEN, y, p);
                y += 14;
                c.drawText("Base con descuento", 300, y, p);
                derecha(c, Formatos.pesos(Math.max(0, subtotal - descuento)), ANCHO - MARGEN, y, p);
                y += 14;
            }
            /* Una línea por tarifa: 19 %, 5 %, exento. Así la factura coincide
               con el informe de impuestos aunque la venta mezcle tarifas. */
            List<Double> tarifas = Impuestos.tarifas(items, pct);
            boolean alguna = false;
            for (Double tarifa : tarifas) {
                long valor = Impuestos.impuestoDe(items, tarifa, pct);
                if (valor <= 0 && tarifas.size() > 1) continue;
                alguna = true;
                String etiqueta = tarifa <= 0 ? "Exento" : "IVA " + Formatos.porcentaje(tarifa);
                c.drawText(etiqueta, 300, y, p);
                derecha(c, Formatos.pesos(valor), ANCHO - MARGEN, y, p);
                y += 14;
            }
            if (!alguna && impuesto > 0) {
                c.drawText("Impuesto (" + Formatos.porcentaje(pct) + ")", 300, y, p);
                derecha(c, Formatos.pesos(impuesto), ANCHO - MARGEN, y, p);
                y += 14;
            }
            p.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            p.setTextSize(13);
            c.drawText("TOTAL", 300, y, p);
            derecha(c, Formatos.pesos(total), ANCHO - MARGEN, y, p);
            y += 26;
        }

        void notas(String notas) {
            p.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            p.setTextSize(10);
            c.drawText("Notas: " + notas, MARGEN, y, p);
            y += 16;
        }

        void cerrar() throws IOException {
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

    private static void derecha(Canvas c, String texto, float x, float y, Paint p) {
        c.drawText(texto, x - p.measureText(texto), y, p);
    }
}