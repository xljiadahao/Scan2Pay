package com.paypal.scancode.zxing.encode;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

public final class EncodingHandler {
    static final int BLACK = 0xff000000;
    static final int WHITE = 0xFFFFFFFF;

    public static Bitmap create2Code(String str, int widthAndHeight) throws WriterException, UnsupportedEncodingException {
        BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, getEncodeHintMap());
        return BitMatrixToBitmap(matrix);
    }

    public static Bitmap createBarCode(String str, Integer width, Integer height) throws Exception {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(str, BarcodeFormat.CODE_128, width, height, getEncodeHintMap());
            return BitMatrixToBitmap(bitMatrix);
    }

    private static Hashtable<EncodeHintType, Object> getEncodeHintMap() {
        Hashtable<EncodeHintType, Object> hints= new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        return hints;
    }

    private static Bitmap BitMatrixToBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                if(matrix.get(x,y)){
                    pixels[offset + x] =BLACK;
                }else{
                    pixels[offset + x] =WHITE;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        Log.e("hongliang","width:"+bitmap.getWidth()+" height:"+bitmap.getHeight());
        return bitmap;
    }
}
