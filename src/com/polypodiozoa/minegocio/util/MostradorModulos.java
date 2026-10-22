package com.polypodiozoa.minegocio.util;

import java.util.ArrayList;
import java.util.List;

/** Módulos del mostrador: orden y visibilidad configurables. */
public final class MostradorModulos {

    public static final String QR = "qr";
    public static final String LLAVE = "llave";
    public static final String CUENTA = "cuenta";
    public static final String AYUDA = "ayuda";
    public static final String ORDEN_DEFECTO = QR + "," + LLAVE + "," + CUENTA + "," + AYUDA;

    private static final String[] TODOS = {QR, LLAVE, CUENTA, AYUDA};

    private MostradorModulos() {}

    public static String etiqueta(String id) {
        if (QR.equals(id)) return "QR";
        if (LLAVE.equals(id)) return "Llave Bre-B";
        if (CUENTA.equals(id)) return "Cuenta, banco y titular";
        if (AYUDA.equals(id)) return "Cómo pagar";
        return id;
    }

    /** Deja solo módulos conocidos, sin repetir. Un texto vacío significa "ninguno". */
    public static String limpiar(String texto) {
        List<String> lista = new ArrayList<>();
        if (texto != null) {
            for (String parte : texto.split(",")) {
                String id = parte.trim();
                if (conocido(id) && !lista.contains(id)) lista.add(id);
            }
        }
        StringBuilder s = new StringBuilder();
        for (String id : lista) {
            if (s.length() > 0) s.append(',');
            s.append(id);
        }
        return s.toString();
    }

    public static List<String> lista(String texto) {
        List<String> r = new ArrayList<>();
        String limpio = limpiar(texto);
        if (limpio.isEmpty()) return r;
        for (String id : limpio.split(",")) r.add(id);
        return r;
    }

    public static String[] todos() {
        return TODOS.clone();
    }

    private static boolean conocido(String id) {
        for (String t : TODOS) if (t.equals(id)) return true;
        return false;
    }
}
