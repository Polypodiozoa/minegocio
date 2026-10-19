package com.polypodiozoa.minegocio.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formatos de dinero y números para Colombia (pesos sin centavos). */
public final class Formatos {

    public static final Locale COLOMBIA = new Locale("es", "CO");
    private static final DecimalFormatSymbols SIMBOLOS = DecimalFormatSymbols.getInstance(COLOMBIA);

    private Formatos() {}

    public static String entero(long valor) {
        DecimalFormat f = new DecimalFormat("#,##0", SIMBOLOS);
        return f.format(valor);
    }

    /** Ejemplo: $ 12.500. Los negativos se muestran como -$ 12.500. */
    public static String pesos(long valor) {
        if (valor < 0) return "-$ " + entero(-valor);
        return "$ " + entero(valor);
    }

    /** Descripción para lectores de pantalla: 12.500 pesos. */
    public static String pesosHablado(long valor) {
        if (valor < 0) return "menos " + entero(-valor) + " pesos";
        return entero(valor) + " pesos";
    }

    public static String porcentaje(double valor) {
        DecimalFormat f = new DecimalFormat("0.#", SIMBOLOS);
        return f.format(valor) + "%";
    }

    public static String decimal(double valor) {
        DecimalFormat f = new DecimalFormat("0.###", SIMBOLOS);
        return f.format(valor);
    }

    /** Acepta "$ 12.500", "12500", "12.500,50" (en pesos se descartan los centavos). */
    public static long parsePesos(String texto) {
        if (texto == null) return 0;
        String limpio = texto.replace("$", "").replace(" ", "").replace("\u00A0", "").trim();
        if (limpio.isEmpty()) return 0;
        boolean negativo = limpio.startsWith("-");
        limpio = limpio.replace("-", "").replace("+", "");
        limpio = limpio.replace(".", "");
        int coma = limpio.indexOf(',');
        if (coma >= 0) limpio = limpio.substring(0, coma);
        limpio = limpio.replaceAll("[^0-9]", "");
        if (limpio.isEmpty()) return 0;
        try {
            long v = Long.parseLong(limpio);
            return negativo ? -v : v;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static double parseDecimal(String texto) {
        if (texto == null) return 0;
        String limpio = texto.replace("$", "").replace(" ", "").replace("%", "").replace("\u00A0", "").trim();
        if (limpio.isEmpty()) return 0;
        if (limpio.contains(",")) {
            limpio = limpio.replace(".", "").replace(',', '.');
        }
        try {
            return new BigDecimal(limpio).doubleValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int parseInt(String texto) {
        return (int) Math.max(0, Math.min(1000000, parsePesos(texto)));
    }

    public static String recortar(String texto, int max) {
        if (texto == null) return "";
        texto = texto.trim();
        if (texto.length() <= max) return texto;
        return texto.substring(0, Math.max(0, max - 1)).trim() + "…";
    }

    public static String inicial(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return "?";
        return nombre.trim().substring(0, 1).toUpperCase(COLOMBIA);
    }
}
