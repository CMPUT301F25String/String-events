package com.example.string_events;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

/**
 * Utility class for generating QR code bitmaps.
 * <p>
 * This class uses the ZXing library to encode a text content string
 * into a {@link Bitmap} representing a QR code. It is used wherever
 * the app needs to create a scannable QR image (for example, for events).
 */
public class QRUtils {

    /**
     * Generates a QR code bitmap from the given content string.
     *
     * @param content the text to encode into the QR code (e.g., a deep link or event ID)
     * @param width   the desired width of the output bitmap in pixels
     * @param height  the desired height of the output bitmap in pixels
     * @return a {@link Bitmap} containing the generated QR code
     * @throws WriterException if the encoding process fails
     */
    public static Bitmap generateQrCode(String content, int width, int height) throws WriterException {
        // Configure encoding hints: UTF-8 charset and a small margin around the QR code.
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        // Encode the content string into a BitMatrix representing the QR code.
        BitMatrix matrix = new MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
        );

        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[] pixels = new int[w * h];

        // ARGB colors for the QR code (black modules on a white background).
        int black = 0xFF000000;
        int white = 0xFFFFFFFF;

        // Convert the BitMatrix into a 1D pixel array for the Bitmap.
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                // If the matrix cell is "on", use black; otherwise, use white.
                pixels[offset + x] = matrix.get(x, y) ? black : white;
            }
        }

        // Create the bitmap and apply the generated pixel array.
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}
