package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = "ImageDownloadTask";
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15_000;

    private final ImageView imageView;
    private final int targetWidthPx;

    public ImageDownloadTask(ImageView imageView, int targetWidthPx) {
        this.imageView = imageView;
        this.targetWidthPx = targetWidthPx;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(urls[0]).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            try (InputStream input = connection.getInputStream()) {
                return decodeSampled(readAll(input), targetWidthPx);
            }
        } catch (IOException e) {
            Log.e(TAG, "Download failed: " + urls[0], e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = input.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    private Bitmap decodeSampled(byte[] bytes, int targetWidth) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds.outWidth, targetWidth);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    private int calculateInSampleSize(int width, int targetWidth) {
        int sample = 1;
        while (width / (sample * 2) >= targetWidth) {
            sample *= 2;
        }
        return sample;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
    }
}
