package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class PetImageCodec {

    public static final int MAX_IMAGE_DIMENSION_PX = 800;
    public static final int JPEG_QUALITY = 70;
    public static final int MAX_IMAGE_BYTES = 200 * 1024;

    private PetImageCodec() {
    }

    public static byte[] compressFromUri(Context context, Uri uri) throws IOException {
        Bitmap bitmap = decodeFromUri(context, uri, MAX_IMAGE_DIMENSION_PX);
        if (bitmap == null) {
            throw new IOException("Image could not be decoded");
        }

        int quality = JPEG_QUALITY;
        byte[] bytes = compress(bitmap, quality);
        while (bytes.length > MAX_IMAGE_BYTES && quality > 40) {
            quality -= 10;
            bytes = compress(bitmap, quality);
        }
        bitmap.recycle();
        return bytes;
    }

    public static String encode(byte[] jpeg) {
        return Base64.encodeToString(jpeg, Base64.NO_WRAP);
    }

    public static Bitmap decode(String base64, int targetPx) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return decodeSampled(bytes, targetPx);
    }

    private static byte[] compress(Bitmap bitmap, int quality) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output);
        return output.toByteArray();
    }

    private static Bitmap decodeFromUri(Context context, Uri uri, int maxDimension) throws IOException {
        ContentResolver resolver = context.getContentResolver();

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream input = resolver.openInputStream(uri)) {
            if (input == null) {
                return null;
            }
            BitmapFactory.decodeStream(input, null, bounds);
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(
                Math.max(bounds.outWidth, bounds.outHeight), maxDimension);

        Bitmap decoded;
        try (InputStream input = resolver.openInputStream(uri)) {
            if (input == null) {
                return null;
            }
            decoded = BitmapFactory.decodeStream(input, null, options);
        }

        if (decoded == null) {
            return null;
        }

        int currentMax = Math.max(decoded.getWidth(), decoded.getHeight());
        if (currentMax <= maxDimension) {
            return decoded;
        }

        float scale = (float) maxDimension / (float) currentMax;
        int width = Math.max(1, Math.round(decoded.getWidth() * scale));
        int height = Math.max(1, Math.round(decoded.getHeight() * scale));
        Bitmap scaled = Bitmap.createScaledBitmap(decoded, width, height, true);
        if (scaled != decoded) {
            decoded.recycle();
        }
        return scaled;
    }

    private static Bitmap decodeSampled(byte[] bytes, int targetPx) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds.outWidth, targetPx);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    private static int calculateInSampleSize(int width, int targetWidth) {
        if (width <= 0 || targetWidth <= 0) {
            return 1;
        }
        int sample = 1;
        while (width / (sample * 2) >= targetWidth) {
            sample *= 2;
        }
        return sample;
    }
}
