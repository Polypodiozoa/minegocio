package com.polypodiozoa.minegocio.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Fechas del negocio: siempre se agrupan y muestran en hora de Colombia. */
public final class Fechas {

    public static final TimeZone BOGOTA = TimeZone.getTimeZone("America/Bogota");
    public static final int HOY = 0;
    public static final int SEMANA = 1;
    public static final int MES = 2;
    public static final int MES_ANTERIOR = 3;
    public static final int ANIO = 4;
    public static final int TODO = 5;

    private Fechas() {}

    public static long ahora() {
        return System.currentTimeMillis();
    }

    public static Calendar calendario(long tiempo) {
        Calendar c = Calendar.getInstance(BOGOTA, Formatos.COLOMBIA);
        c.setTimeInMillis(tiempo);
        return c;
    }

    public static long inicioDia(long tiempo) {
        Calendar c = calendario(tiempo);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long inicioDiaSiguiente(long tiempo) {
        Calendar c = calendario(inicioDia(tiempo));
        c.add(Calendar.DAY_OF_MONTH, 1);
        return c.getTimeInMillis();
    }

    public static long inicioMes(long tiempo) {
        Calendar c = calendario(tiempo);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long inicioMesSiguiente(long tiempo) {
        Calendar c = calendario(inicioMes(tiempo));
        c.add(Calendar.MONTH, 1);
        return c.getTimeInMillis();
    }

    public static long inicioMesAnterior(long tiempo) {
        Calendar c = calendario(inicioMes(tiempo));
        c.add(Calendar.MONTH, -1);
        return c.getTimeInMillis();
    }

    public static long inicioAnio(long tiempo) {
        Calendar c = calendario(tiempo);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long inicioAnioSiguiente(long tiempo) {
        Calendar c = calendario(inicioAnio(tiempo));
        c.add(Calendar.YEAR, 1);
        return c.getTimeInMillis();
    }

    /** Rango [desde, hasta) del período elegido. */
    public static long[] rango(int periodo) {
        long ahora = ahora();
        switch (periodo) {
            case SEMANA: {
                Calendar c = calendario(inicioDia(ahora));
                int dow = c.get(Calendar.DAY_OF_WEEK); // domingo=1
                int atras = (dow + 5) % 7; // lunes=0
                c.add(Calendar.DAY_OF_MONTH, -atras);
                return new long[]{c.getTimeInMillis(), inicioDiaSiguiente(ahora)};
            }
            case MES:
                return new long[]{inicioMes(ahora), inicioMesSiguiente(ahora)};
            case MES_ANTERIOR:
                return new long[]{inicioMesAnterior(ahora), inicioMes(ahora)};
            case ANIO:
                return new long[]{inicioAnio(ahora), inicioAnioSiguiente(ahora)};
            case TODO:
                return new long[]{0L, Long.MAX_VALUE};
            default:
                return new long[]{inicioDia(ahora), inicioDiaSiguiente(ahora)};
        }
    }

    public static long mediodia(int anio, int mes, int dia) {
        Calendar c = Calendar.getInstance(BOGOTA, Formatos.COLOMBIA);
        c.set(Calendar.YEAR, anio);
        c.set(Calendar.MONTH, mes);
        c.set(Calendar.DAY_OF_MONTH, dia);
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long vencimientoPorDefecto() {
        Calendar c = calendario(ahora());
        c.add(Calendar.DAY_OF_MONTH, 30);
        return mediodia(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }

    /** Días desde hoy hasta la fecha. Negativo si ya pasó. */
    public static int diasHasta(long fecha) {
        if (fecha <= 0) return 0;
        long diferencia = inicioDia(fecha) - inicioDia(ahora());
        return (int) Math.round(diferencia / (24.0 * 60 * 60 * 1000));
    }

    public static String vencimientoTexto(long fecha) {
        if (fecha <= 0) return "Sin fecha límite";
        int dias = diasHasta(fecha);
        if (dias == 0) return "Vence hoy";
        if (dias == 1) return "Vence mañana";
        if (dias > 1) return "Vence en " + dias + " días";
        if (dias == -1) return "Vencida hace 1 día";
        return "Vencida hace " + Math.abs(dias) + " días";
    }

    public static String nombrePeriodo(int periodo) {
        switch (periodo) {
            case SEMANA: return "Esta semana";
            case MES: return "Este mes";
            case MES_ANTERIOR: return "Mes anterior";
            case ANIO: return "Este año";
            case TODO: return "Todo el tiempo";
            default: return "Hoy";
        }
    }

    public static String fecha(long tiempo) {
        return formato("d MMM yyyy", tiempo);
    }

    public static String fechaHora(long tiempo) {
        return formato("d MMM yyyy, h:mm a", tiempo);
    }

    public static String fechaLarga(long tiempo) {
        return formato("EEEE, d 'de' MMMM 'de' yyyy", tiempo);
    }

    public static String fechaCorta(long tiempo) {
        return formato("dd/MM/yyyy", tiempo);
    }

    public static String hora(long tiempo) {
        return formato("h:mm a", tiempo);
    }

    public static String nombreArchivo(long tiempo) {
        return formato("yyyyMMdd-HHmm", tiempo);
    }

    public static String formato(String patron, long tiempo) {
        SimpleDateFormat f = new SimpleDateFormat(patron, Formatos.COLOMBIA);
        f.setTimeZone(BOGOTA);
        return f.format(new Date(tiempo));
    }

    public static String hoyTexto() {
        String s = fechaLarga(ahora());
        if (s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Formatos.COLOMBIA) + s.substring(1);
    }

    public static String mesCorto(long tiempo) {
        String s = formato("MMM", tiempo);
        return s.replace(".", "");
    }

    public static String conMayuscula(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        return texto.substring(0, 1).toUpperCase(Locale.getDefault()) + texto.substring(1);
    }
}
