package com.polypodiozoa.minegocio.util;

import com.polypodiozoa.minegocio.modelo.Modelo;

import java.util.List;

/**
 * Cálculo del IVA de una venta o cotización. Es lógica pura (sin Android) para
 * poder probarla con números: una sola tarifa se redondea una vez sobre la base
 * con descuento; varias tarifas se suman línea por línea.
 */
public final class Impuestos {

    private Impuestos() {}

    public static final class Totales {
        public long subtotal;
        public long base;
        public long impuesto;
        public long total;
    }

    /**
     * Reparte el impuesto en las líneas y devuelve los totales del documento.
     * La tarifa de cada línea es la suya si es >= 0; si no, la general.
     */
    public static Totales calcular(List<Modelo.Item> items, long descuento, double tarifaGeneral) {
        Totales t = new Totales();
        for (Modelo.Item i : items) {
            i.subtotal = i.precioUnitario * i.cantidad;
            t.subtotal += i.subtotal;
            if (i.costoTotal <= 0) i.costoTotal = i.costoUnitario * i.cantidad;
        }
        t.base = Math.max(0, t.subtotal - descuento);
        double factor = t.subtotal > 0 ? (double) t.base / t.subtotal : 0;

        double primera = -1;
        boolean uniforme = true;
        for (Modelo.Item i : items) {
            double pct = i.impuestoPct >= 0 ? i.impuestoPct : tarifaGeneral;
            if (primera < 0) primera = pct;
            else if (Math.abs(pct - primera) > 0.001) uniforme = false;
        }

        if (uniforme) {
            /* Con una sola tarifa el total se calcula una vez sobre la base, como
               siempre; el reparto por línea solo se ajusta para que cuadre al peso. */
            double pct = primera < 0 ? tarifaGeneral : primera;
            long impuesto = Math.round(t.base * pct / 100.0);
            t.impuesto = impuesto;
            long repartido = 0;
            Modelo.Item ultimo = null;
            for (Modelo.Item i : items) {
                double p = i.impuestoPct >= 0 ? i.impuestoPct : tarifaGeneral;
                i.impuesto = Math.round(i.subtotal * factor * p / 100.0);
                repartido += i.impuesto;
                ultimo = i;
            }
            if (ultimo != null && repartido != impuesto) {
                ultimo.impuesto += impuesto - repartido;
            }
        } else {
            long impuesto = 0;
            for (Modelo.Item i : items) {
                double p = i.impuestoPct >= 0 ? i.impuestoPct : tarifaGeneral;
                i.impuesto = Math.round(i.subtotal * factor * p / 100.0);
                impuesto += i.impuesto;
            }
            t.impuesto = impuesto;
        }
        t.total = t.base + t.impuesto;
        return t;
    }

    /** Tarifas distintas presentes en el documento, de mayor a menor. */
    public static java.util.List<Double> tarifas(List<Modelo.Item> items, double tarifaGeneral) {
        java.util.List<Double> lista = new java.util.ArrayList<>();
        for (Modelo.Item i : items) {
            double pct = i.impuestoPct >= 0 ? i.impuestoPct : tarifaGeneral;
            boolean existe = false;
            for (Double d : lista) {
                if (Math.abs(d - pct) < 0.001) existe = true;
            }
            if (!existe) lista.add(pct);
        }
        java.util.Collections.sort(lista, (a, b) -> Double.compare(b, a));
        return lista;
    }

    /** Impuesto cobrado de una tarifa concreta. */
    public static long impuestoDe(List<Modelo.Item> items, double tarifa, double tarifaGeneral) {
        long total = 0;
        for (Modelo.Item i : items) {
            double pct = i.impuestoPct >= 0 ? i.impuestoPct : tarifaGeneral;
            if (Math.abs(pct - tarifa) < 0.001) total += i.impuesto;
        }
        return total;
    }
}