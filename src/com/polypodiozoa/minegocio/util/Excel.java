package com.polypodiozoa.minegocio.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generador mínimo de archivos .xlsx (OOXML) sin librerías externas.
 * Soporta texto, números, moneda y porcentajes, varias hojas y anchos de columna.
 */
public final class Excel {

    public static final int NORMAL = 0;
    public static final int TITULO = 1;
    public static final int MONEDA = 2;
    public static final int PORCENTAJE = 3;

    public static final class Celda {
        final String texto;
        final Double numero;
        final int estilo;

        private Celda(String texto, Double numero, int estilo) {
            this.texto = texto;
            this.numero = numero;
            this.estilo = estilo;
        }

        public static Celda texto(String valor) {
            return new Celda(valor == null ? "" : valor, null, NORMAL);
        }

        public static Celda texto(String valor, int estilo) {
            return new Celda(valor == null ? "" : valor, null, estilo);
        }

        public static Celda numero(long valor) {
            return new Celda(null, (double) valor, NORMAL);
        }

        public static Celda moneda(long valor) {
            return new Celda(null, (double) valor, MONEDA);
        }

        public static Celda porcentaje(double valor) {
            return new Celda(null, valor, PORCENTAJE);
        }
    }

    public static final class Hoja {
        final String nombre;
        final List<List<Celda>> filas = new ArrayList<>();
        int[] anchos;

        public Hoja(String nombre) {
            this.nombre = nombre;
        }

        public Hoja ancho(int... anchos) {
            this.anchos = anchos;
            return this;
        }

        public Hoja filaVacia() {
            filas.add(new ArrayList<>());
            return this;
        }

        public Hoja fila(Celda... celdas) {
            List<Celda> fila = new ArrayList<>();
            for (Celda c : celdas) fila.add(c == null ? Celda.texto("") : c);
            filas.add(fila);
            return this;
        }

        public Hoja fila(Object... valores) {
            List<Celda> fila = new ArrayList<>();
            for (Object valor : valores) {
                if (valor instanceof Celda) fila.add((Celda) valor);
                else if (valor instanceof Number) fila.add(Celda.numero(((Number) valor).longValue()));
                else fila.add(Celda.texto(valor == null ? "" : valor.toString()));
            }
            filas.add(fila);
            return this;
        }

        public Hoja titulo(Object... valores) {
            List<Celda> fila = new ArrayList<>();
            for (Object valor : valores) {
                fila.add(Celda.texto(valor == null ? "" : valor.toString(), TITULO));
            }
            filas.add(fila);
            return this;
        }
    }

    private final List<Hoja> hojas = new ArrayList<>();

    public Excel agregar(Hoja hoja) {
        hojas.add(hoja);
        return this;
    }

    public void escribir(OutputStream destino) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(destino))) {
            entrar(zip, "[Content_Types].xml");
            escribirTexto(zip, contentTypes());
            entrar(zip, "_rels/.rels");
            escribirTexto(zip, relacionesRaiz());
            entrar(zip, "xl/workbook.xml");
            escribirTexto(zip, workbook());
            entrar(zip, "xl/_rels/workbook.xml.rels");
            escribirTexto(zip, relacionesWorkbook());
            entrar(zip, "xl/styles.xml");
            escribirTexto(zip, estilos());
            for (int i = 0; i < hojas.size(); i++) {
                entrar(zip, "xl/worksheets/sheet" + (i + 1) + ".xml");
                escribirTexto(zip, hoja(hojas.get(i)));
            }
        }
    }

    private static void entrar(ZipOutputStream zip, String nombre) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
    }

    private static void escribirTexto(ZipOutputStream zip, String texto) throws IOException {
        zip.write(texto.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String contentTypes() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">");
        sb.append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>");
        sb.append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>");
        sb.append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
        sb.append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
        for (int i = 0; i < hojas.size(); i++) {
            sb.append("<Override PartName=\"/xl/worksheets/sheet").append(i + 1)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        sb.append("</Types>");
        return sb.toString();
    }

    private String relacionesRaiz() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private String workbook() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ");
        sb.append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">");
        sb.append("<sheets>");
        for (int i = 0; i < hojas.size(); i++) {
            sb.append("<sheet name=\"").append(xml(nombreHoja(hojas.get(i).nombre, i)))
                    .append("\" sheetId=\"").append(i + 1).append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        sb.append("</sheets></workbook>");
        return sb.toString();
    }

    private String nombreHoja(String nombre, int indice) {
        String limpio = nombre == null ? "Hoja" + (indice + 1) : nombre.replaceAll("[\\\\/*?:\\[\\]]", " ");
        limpio = limpio.trim();
        if (limpio.isEmpty()) limpio = "Hoja" + (indice + 1);
        if (limpio.length() > 31) limpio = limpio.substring(0, 31);
        return limpio;
    }

    private String relacionesWorkbook() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 0; i < hojas.size(); i++) {
            sb.append("<Relationship Id=\"rId").append(i + 1)
                    .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
                    .append(i + 1).append(".xml\"/>");
        }
        int idEstilos = hojas.size() + 1;
        sb.append("<Relationship Id=\"rId").append(idEstilos)
                .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");
        sb.append("</Relationships>");
        return sb.toString();
    }

    private String estilos() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<numFmts count=\"2\">"
                + "<numFmt numFmtId=\"164\" formatCode=\"&quot;$&quot;#,##0\"/>"
                + "<numFmt numFmtId=\"165\" formatCode=\"0.0%\"/>"
                + "</numFmts>"
                + "<fonts count=\"2\">"
                + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
                + "</fonts>"
                + "<fills count=\"3\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF0B4F6C\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"4\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>"
                + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                + "<xf numFmtId=\"165\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";
    }

    private String hoja(Hoja hoja) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        sb.append("<sheetViews><sheetView workbookViewId=\"0\">");
        if (!hoja.filas.isEmpty()) {
            sb.append("<pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/>");
        }
        sb.append("</sheetView></sheetViews>");
        if (hoja.anchos != null && hoja.anchos.length > 0) {
            sb.append("<cols>");
            for (int i = 0; i < hoja.anchos.length; i++) {
                sb.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
                        .append("\" width=\"").append(hoja.anchos[i]).append("\" customWidth=\"1\"/>");
            }
            sb.append("</cols>");
        }
        sb.append("<sheetData>");
        for (int f = 0; f < hoja.filas.size(); f++) {
            sb.append("<row r=\"").append(f + 1).append("\">");
            List<Celda> fila = hoja.filas.get(f);
            for (int c = 0; c < fila.size(); c++) {
                Celda celda = fila.get(c);
                String ref = columna(c) + (f + 1);
                if (celda.numero != null) {
                    sb.append("<c r=\"").append(ref).append("\"");
                    if (celda.estilo != NORMAL) sb.append(" s=\"").append(celda.estilo).append("\"");
                    sb.append("><v>").append(numero(celda.numero)).append("</v></c>");
                } else if (celda.texto != null && !celda.texto.isEmpty()) {
                    sb.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"");
                    if (celda.estilo != NORMAL) sb.append(" s=\"").append(celda.estilo).append("\"");
                    sb.append("><is><t xml:space=\"preserve\">").append(xml(celda.texto))
                            .append("</t></is></c>");
                }
            }
            sb.append("</row>");
        }
        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    private static String numero(double valor) {
        /* NaN o infinito no son números válidos en el XLSX: con uno solo, Excel
           rechaza el archivo completo. Se escribe 0 en vez de romper el reporte. */
        if (Double.isNaN(valor) || Double.isInfinite(valor)) return "0";
        if (valor == Math.rint(valor) && Math.abs(valor) < 1e15) {
            return String.valueOf((long) valor);
        }
        return String.valueOf(valor);
    }

    private static String columna(int indice) {
        StringBuilder sb = new StringBuilder();
        int n = indice + 1;
        while (n > 0) {
            int resto = (n - 1) % 26;
            sb.insert(0, (char) ('A' + resto));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }

    private static String xml(String valor) {
        /* XML 1.0 no admite los caracteres de control 0x00-0x08, 0x0B, 0x0C y
           0x0E-0x1F: con uno solo, Excel rechaza el archivo entero. Tab, salto de
           línea y retorno (0x09, 0x0A, 0x0D) sí son válidos y se conservan. */
        valor = valor.replaceAll("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]", "");
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
