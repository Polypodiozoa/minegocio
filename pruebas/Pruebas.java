import com.polypodiozoa.minegocio.util.Excel;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Pruebas de la lógica pura de Mi Negocio (dinero, fechas y Excel).
 * Se ejecuta con pruebas/pruebas.sh; no necesita Android ni la tablet.
 */
public final class Pruebas {

    private static int fallos = 0;
    private static int total = 0;

    public static void main(String[] args) throws Exception {
        dinero();
        fechas();
        excel();
        System.out.println();
        if (fallos > 0) {
            System.out.println("FALLARON " + fallos + " de " + total + " comprobaciones.");
            System.exit(1);
        }
        System.out.println("Todo bien: " + total + " comprobaciones.");
    }

    // --------------------------------------------------------------- dinero

    private static void dinero() {
        System.out.println("Dinero (Formatos)");
        /* Montos reales: cualquier cambio aquí rompe precios, saldos o utilidades. */
        igual("lee \"$ 12.500\"", 12500L, Formatos.parsePesos("$ 12.500"));
        igual("lee \"12500\"", 12500L, Formatos.parsePesos("12500"));
        igual("lee \"12.500\"", 12500L, Formatos.parsePesos("12.500"));
        igual("lee \"12.500,50\" sin centavos", 12500L, Formatos.parsePesos("12.500,50"));
        igual("lee negativo \"-5.000\"", -5000L, Formatos.parsePesos("-5.000"));
        igual("vacío es 0", 0L, Formatos.parsePesos(""));
        igual("solo signo es 0", 0L, Formatos.parsePesos("-"));
        igual("25 dígitos no desbordan", 0L, Formatos.parsePesos("9999999999999999999999999"));

        igual("válido \"12500\"", true, Formatos.pesosValidos("12500"));
        igual("válido \"$ 12.500\"", true, Formatos.pesosValidos("$ 12.500"));
        igual("válido \"12.500,50\"", true, Formatos.pesosValidos("12.500,50"));
        igual("válido vacío (es 0)", true, Formatos.pesosValidos(""));
        igual("válido 15 dígitos", true, Formatos.pesosValidos("123456789012345"));
        igual("inválido 16 dígitos", false, Formatos.pesosValidos("1234567890123456"));
        igual("inválido con letras", false, Formatos.pesosValidos("12a34"));
        igual("válido con espacio de miles", true, Formatos.pesosValidos("12 500"));

        igual("escribe 12500", "$ 12.500", Formatos.pesos(12500));
        igual("escribe negativo", "-$ 5.000", Formatos.pesos(-5000));
        igual("escribe cero", "$ 0", Formatos.pesos(0));
        igual("cantidades topan en 1.000.000", 1000000, Formatos.parseInt("123456789"));
        igual("no inventa negativos en cantidades", 0, Formatos.parseInt("-5"));
        igual("decimal \"19\"", 19.0, Formatos.parseDecimal("19"));
        igual("decimal \"12,5\"", 12.5, Formatos.parseDecimal("12,5"));
        igual("porcentaje \"19\"", "19%", Formatos.porcentaje(19));
    }

    // --------------------------------------------------------------- fechas

    private static void fechas() {
        System.out.println("Fechas");
        long ahora = Fechas.ahora();
        long[] semana = Fechas.rango(Fechas.SEMANA);
        Calendar lunes = Fechas.calendario(semana[0]);
        igual("la semana empieza en lunes", Calendar.MONDAY, lunes.get(Calendar.DAY_OF_WEEK));
        igual("la semana empieza a medianoche", 0, lunes.get(Calendar.HOUR_OF_DAY));
        igual("la semana empieza hoy o antes", true, semana[0] <= ahora);
        igual("la semana termina mañana", Fechas.inicioDiaSiguiente(ahora), semana[1]);

        long[] mes = Fechas.rango(Fechas.MES);
        igual("el mes empieza el día 1", 1, Fechas.calendario(mes[0]).get(Calendar.DAY_OF_MONTH));
        igual("el mes anterior termina donde empieza este",
                mes[0], Fechas.rango(Fechas.MES_ANTERIOR)[1]);
        igual("el año empieza en enero", Calendar.JANUARY,
                Fechas.calendario(Fechas.rango(Fechas.ANIO)[0]).get(Calendar.MONTH));
        igual("«todo el tiempo» cubre desde siempre", 0L, Fechas.rango(Fechas.TODO)[0]);
        igual("«hoy» empieza a medianoche", 0,
                Fechas.calendario(Fechas.rango(Fechas.HOY)[0]).get(Calendar.HOUR_OF_DAY));

        igual("el vencimiento por defecto es a 30 días", 30,
                Fechas.diasHasta(Fechas.vencimientoPorDefecto()));
        igual("una venta de ayer vence hace 1 día", -1,
                Fechas.diasHasta(Fechas.inicioDia(ahora) - 86400000L));
        igual("sin fecha límite", 0, Fechas.diasHasta(0));
        igual("texto de vencida", "Vencida hace 1 día",
                Fechas.vencimientoTexto(Fechas.inicioDia(ahora) - 86400000L));
    }

    // ---------------------------------------------------------------- excel

    private static void excel() throws Exception {
        System.out.println("Excel (XLSX)");
        /* Un nombre de producto puede venir de un código escaneado: puede traer
           caracteres de control que invalidan el XML entero. */
        Excel.Hoja hoja = new Excel.Hoja("Ven/tas\u0007")
                .fila("Producto", "Total")
                .fila("Café \u0007 500 g", Excel.Celda.moneda(12500))
                .fila("=HYPERLINK(\"http://x\")", Excel.Celda.numero(-3))
                .fila("A&B <C> \"D\" 'E'", Excel.Celda.porcentaje(0.19))
                .fila("margen imposible", Excel.Celda.porcentaje(0.0 / 0.0));
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        new Excel().agregar(hoja).escribir(salida);

        Map<String, String> partes = descomprimir(salida.toByteArray());
        String libro = partes.get("xl/workbook.xml");
        String hoja1 = partes.get("xl/worksheets/sheet1.xml");
        igual("el archivo trae la hoja de cálculo", true, hoja1 != null);
        igual("la hoja no lleva barra en el nombre", false, libro.contains("Ven/tas"));
        igual("el nombre de la hoja pierde el control", false, libro.contains("\u0007"));
        igual("la celda pierde el control", false, hoja1.contains("\u0007"));
        igual("el texto con «=» no se vuelve fórmula", false, hoja1.contains("<f>"));
        igual("el texto con «=» queda como texto",
                true, hoja1.contains("=HYPERLINK(&quot;http://x&quot;)"));
        igual("escapa & < >", true, hoja1.contains("A&amp;B &lt;C&gt;"));
        igual("moneda entera sin decimales", true, hoja1.contains("<v>12500</v>"));
        igual("número negativo exacto", true, hoja1.contains("<v>-3</v>"));
        igual("porcentaje 19%", true, hoja1.contains("<v>0.19</v>"));
        igual("NaN no llega al archivo", false, hoja1.contains("NaN"));
        igual("NaN se guarda como 0", true, hoja1.contains("<v>0</v>"));

        /* Que sea XML válido de verdad: es lo que comprueba Excel al abrirlo. */
        DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(hoja1.getBytes(StandardCharsets.UTF_8)));
        igual("el XML de la hoja está bien formado", true, true);
    }

    private static Map<String, String> descomprimir(byte[] zip) throws Exception {
        Map<String, String> partes = new LinkedHashMap<>();
        try (ZipInputStream entrada = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry e;
            while ((e = entrada.getNextEntry()) != null) {
                ByteArrayOutputStream contenido = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int leidos;
                while ((leidos = entrada.read(buffer)) > 0) contenido.write(buffer, 0, leidos);
                partes.put(e.getName(), new String(contenido.toByteArray(), StandardCharsets.UTF_8));
            }
        }
        return partes;
    }

    // -------------------------------------------------------------- soporte

    private static void igual(String nombre, Object esperado, Object obtenido) {
        total++;
        boolean bien = esperado == null ? obtenido == null : esperado.equals(obtenido);
        if (bien) {
            System.out.println("  ok    " + nombre);
            return;
        }
        fallos++;
        System.out.println("  FALLA " + nombre + ": esperaba " + esperado + ", obtuvo " + obtenido);
    }
}
