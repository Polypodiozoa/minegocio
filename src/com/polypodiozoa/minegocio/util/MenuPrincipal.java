package com.polypodiozoa.minegocio.util;

import com.polypodiozoa.minegocio.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Orden y contenido de los accesos de «¿Qué quieres hacer?». */
public final class MenuPrincipal {

    public static final class Opcion {
        public final String clave;
        public final String etiqueta;
        public final int icono;
        public final boolean principal;

        Opcion(String clave, String etiqueta, int icono, boolean principal) {
            this.clave = clave;
            this.etiqueta = etiqueta;
            this.icono = icono;
            this.principal = principal;
        }
    }

    private static final String[] CLAVES = {
            "nueva_venta", "nueva_compra", "ventas", "compras", "inventario",
            "clientes", "deudas", "proveedores", "gastos", "informes", "perfil", "ajustes"
    };

    private static final Map<String, Opcion> OPCIONES = new LinkedHashMap<>();

    static {
        OPCIONES.put("nueva_venta", new Opcion("nueva_venta", "Nueva venta", R.drawable.ic_venta, true));
        OPCIONES.put("nueva_compra", new Opcion("nueva_compra", "Nueva compra", R.drawable.ic_carrito, true));
        OPCIONES.put("ventas", new Opcion("ventas", "Ventas", R.drawable.ic_recibo, false));
        OPCIONES.put("compras", new Opcion("compras", "Compras", R.drawable.ic_camion, false));
        OPCIONES.put("inventario", new Opcion("inventario", "Inventario", R.drawable.ic_caja, false));
        OPCIONES.put("clientes", new Opcion("clientes", "Clientes", R.drawable.ic_personas, false));
        OPCIONES.put("deudas", new Opcion("deudas", "Deudas", R.drawable.ic_calendario, false));
        OPCIONES.put("proveedores", new Opcion("proveedores", "Proveedores", R.drawable.ic_edificio, false));
        OPCIONES.put("gastos", new Opcion("gastos", "Gastos", R.drawable.ic_dinero, false));
        OPCIONES.put("informes", new Opcion("informes", "Informes", R.drawable.ic_grafico, false));
        OPCIONES.put("perfil", new Opcion("perfil", "Perfil", R.drawable.ic_persona, false));
        OPCIONES.put("ajustes", new Opcion("ajustes", "Ajustes", R.drawable.ic_ajustes, false));
    }

    private MenuPrincipal() {}

    public static String ordenDefecto() {
        StringBuilder s = new StringBuilder();
        for (String clave : CLAVES) {
            if (s.length() > 0) s.append(',');
            s.append(clave);
        }
        return s.toString();
    }

    /** Devuelve todas las opciones, respetando el orden guardado y completando las que falten. */
    public static List<Opcion> opciones(String guardado) {
        Map<String, Opcion> orden = new LinkedHashMap<>();
        if (guardado != null) {
            for (String clave : guardado.split(",")) {
                String c = clave.trim();
                Opcion o = OPCIONES.get(c);
                if (o != null) orden.put(c, o);
            }
        }
        for (String clave : CLAVES) orden.putIfAbsent(clave, OPCIONES.get(clave));
        return new ArrayList<>(orden.values());
    }

    public static String claveDe(String etiqueta) {
        for (Opcion o : OPCIONES.values()) {
            if (o.etiqueta.equals(etiqueta)) return o.clave;
        }
        return "";
    }
}
