package com.polypodiozoa.minegocio.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Convierte una foto o un PDF de un QR de pago en un QR limpio, en blanco y negro
 * y a alta resolución. Primero lee el contenido con ZXing y luego lo vuelve a
 * generar; así se eliminan sombras, inclinación, fondo y ruido de la imagen.
 */
public final class QrPago {

    private static final int LADO = 1200;

    private QrPago() {}

    /** Devuelve el QR limpio de una imagen o de la primera página de un PDF. */
    public static Bitmap limpiar(String ruta) {
        if (ruta != null && ruta.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            return limpiarPdf(new File(ruta));
        }
        Bitmap foto = Imagen.cargar(ruta, 2200);
        return limpiar(foto);
    }

    /** Limpia un bitmap ya cargado. */
    public static Bitmap limpiar(Bitmap foto) {
        if (foto == null) return null;
        int ancho = foto.getWidth();
        int alto = foto.getHeight();
        int[] pixeles = new int[ancho * alto];
        foto.getPixels(pixeles, 0, ancho, 0, 0, ancho, alto);
        String contenido = LectorCodigos.leerPixeles(pixeles, ancho, alto,
                LectorCodigos.MODO_QR);
        if (contenido == null || contenido.trim().isEmpty()) return null;
        try {
            Map<EncodeHintType, Object> pistas = new EnumMap<>(EncodeHintType.class);
            pistas.put(EncodeHintType.MARGIN, 4);
            pistas.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            pistas.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matriz = new MultiFormatWriter().encode(
                    contenido, BarcodeFormat.QR_CODE, LADO, LADO, pistas);
            int[] limpios = new int[LADO * LADO];
            for (int y = 0; y < LADO; y++) {
                for (int x = 0; x < LADO; x++) {
                    limpios[y * LADO + x] = matriz.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            return Bitmap.createBitmap(limpios, LADO, LADO, Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            return null;
        }
    }

    /** Renderiza hasta cuatro páginas y devuelve el primer QR legible del PDF. */
    private static Bitmap limpiarPdf(File archivo) {
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(
                archivo, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(pfd)) {
            int max = 2600;
            int paginas = Math.min(renderer.getPageCount(), 4);
            for (int i = 0; i < paginas; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                try {
                    float escala = Math.min(max / (float) page.getWidth(),
                            max / (float) page.getHeight());
                    int ancho = Math.max(1, Math.round(page.getWidth() * escala));
                    int alto = Math.max(1, Math.round(page.getHeight() * escala));
                    Bitmap pagina = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(pagina);
                    canvas.drawColor(Color.WHITE);
                    page.render(pagina, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    Bitmap limpio = limpiar(pagina);
                    pagina.recycle();
                    if (limpio != null) return limpio;
                } finally {
                    page.close();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
