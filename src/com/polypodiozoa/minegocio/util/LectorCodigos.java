package com.polypodiozoa.minegocio.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Lectura local de códigos de barras y QR con ZXing. No guarda ni envía imágenes.
 * Se prueban cuatro giros porque la cámara entrega el cuadro en la orientación del sensor.
 */
public final class LectorCodigos {

    public static final String MODO_BARRAS = "barras";
    public static final String MODO_QR = "qr";

    private LectorCodigos() {}

    public static boolean esModoQr(String modo) {
        return MODO_QR.equalsIgnoreCase(modo);
    }

    /** Decodifica un cuadro NV21 de la cámara (plano Y) en cualquier orientación. */
    public static String leerNv21(byte[] datos, int ancho, int alto, String modo) {
        if (datos == null || ancho <= 0 || alto <= 0 || datos.length < ancho * alto) return null;
        byte[] y = new byte[ancho * alto];
        System.arraycopy(datos, 0, y, 0, y.length);
        int[][] giros = {{0, 0}, {90, 0}, {180, 0}, {270, 0}};
        for (int[] giro : giros) {
            byte[] rotado = rotarY(y, ancho, alto, giro[0]);
            int w = giro[0] == 0 || giro[0] == 180 ? ancho : alto;
            int h = giro[0] == 0 || giro[0] == 180 ? alto : ancho;
            byte[] nv21 = new byte[rotado.length + rotado.length / 2];
            System.arraycopy(rotado, 0, nv21, 0, rotado.length);
            PlanarYUVLuminanceSource fuente = new PlanarYUVLuminanceSource(
                    nv21, w, h, 0, 0, w, h, false);
            String codigo = decodificar(fuente, modo);
            if (codigo != null) return codigo;
        }
        return null;
    }

    /** Decodifica píxeles ARGB (por ejemplo, una foto elegida por el usuario). */
    public static String leerPixeles(int[] pixeles, int ancho, int alto, String modo) {
        if (pixeles == null || ancho <= 0 || alto <= 0 || pixeles.length < ancho * alto) return null;
        int[][] giros = {{0, 0}, {90, 0}, {180, 0}, {270, 0}};
        for (int[] giro : giros) {
            int[] rotado = rotarArgb(pixeles, ancho, alto, giro[0]);
            int w = giro[0] == 0 || giro[0] == 180 ? ancho : alto;
            int h = giro[0] == 0 || giro[0] == 180 ? alto : ancho;
            RGBLuminanceSource fuente = new RGBLuminanceSource(w, h, rotado);
            String codigo = decodificar(fuente, modo);
            if (codigo != null) return codigo;
        }
        return null;
    }

    private static String decodificar(LuminanceSource fuente, String modo) {
        try {
            Map<DecodeHintType, Object> pistas = new EnumMap<>(DecodeHintType.class);
            pistas.put(DecodeHintType.POSSIBLE_FORMATS, formatos(modo));
            pistas.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            pistas.put(DecodeHintType.ALSO_INVERTED, Boolean.TRUE);
            pistas.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(fuente));
            String texto = new MultiFormatReader().decode(bitmap, pistas).getText();
            if (texto == null) return null;
            texto = texto.trim();
            return texto.isEmpty() ? null : texto;
        } catch (Exception e) {
            return null;
        }
    }

    private static Set<BarcodeFormat> formatos(String modo) {
        if (esModoQr(modo)) return EnumSet.of(BarcodeFormat.QR_CODE);
        return EnumSet.of(
                BarcodeFormat.CODE_128,
                BarcodeFormat.CODE_39,
                BarcodeFormat.CODE_93,
                BarcodeFormat.CODABAR,
                BarcodeFormat.EAN_13,
                BarcodeFormat.EAN_8,
                BarcodeFormat.UPC_A,
                BarcodeFormat.UPC_E,
                BarcodeFormat.ITF);
    }

    private static byte[] rotarY(byte[] origen, int ancho, int alto, int grados) {
        if (grados == 0) return origen.clone();
        int nuevoAncho = grados == 90 || grados == 270 ? alto : ancho;
        int nuevoAlto = grados == 90 || grados == 270 ? ancho : alto;
        byte[] destino = new byte[origen.length];
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int nx;
                int ny;
                if (grados == 90) {
                    nx = alto - 1 - y;
                    ny = x;
                } else if (grados == 180) {
                    nx = ancho - 1 - x;
                    ny = alto - 1 - y;
                } else {
                    nx = y;
                    ny = ancho - 1 - x;
                }
                destino[ny * nuevoAncho + nx] = origen[y * ancho + x];
            }
        }
        return destino;
    }

    private static int[] rotarArgb(int[] origen, int ancho, int alto, int grados) {
        if (grados == 0) return origen.clone();
        int nuevoAncho = grados == 90 || grados == 270 ? alto : ancho;
        int nuevoAlto = grados == 90 || grados == 270 ? ancho : alto;
        int[] destino = new int[origen.length];
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int nx;
                int ny;
                if (grados == 90) {
                    nx = alto - 1 - y;
                    ny = x;
                } else if (grados == 180) {
                    nx = ancho - 1 - x;
                    ny = alto - 1 - y;
                } else {
                    nx = y;
                    ny = ancho - 1 - x;
                }
                destino[ny * nuevoAncho + nx] = origen[y * ancho + x];
            }
        }
        return destino;
    }
}
