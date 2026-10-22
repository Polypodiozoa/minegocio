package com.polypodiozoa.minegocio.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Map;

/**
 * Convierte la foto de un QR de pago en un QR limpio, en blanco y negro y a alta
 * resolución. Primero lee el contenido con ZXing y luego lo vuelve a generar; así
 * se eliminan sombras, inclinación, fondo y ruido de la foto.
 */
public final class QrPago {

    private static final int LADO = 1200;

    private QrPago() {}

    /** Devuelve el QR limpio, o null si la foto no contiene un QR legible. */
    public static Bitmap limpiar(String ruta) {
        Bitmap foto = Imagen.cargar(ruta, 2200);
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
}
