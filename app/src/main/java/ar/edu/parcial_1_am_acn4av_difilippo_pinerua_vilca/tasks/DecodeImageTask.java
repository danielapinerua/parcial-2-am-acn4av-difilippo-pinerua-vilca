package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.util.Map;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetImageCodec;

/**
 * Decodes a Base64 image on a background thread and stores it in the cache and
 * the ImageView from onPostExecute.
 */
public class DecodeImageTask extends AsyncTask<String, Void, Bitmap> {

    private final Map<String, Bitmap> cache;
    private final String key;
    private final int targetWidthPx;
    private final ImageView imageView;

    public DecodeImageTask(Map<String, Bitmap> cache, String key, int targetWidthPx, ImageView imageView) {
        this.cache = cache;
        this.key = key;
        this.targetWidthPx = targetWidthPx;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... values) {
        return PetImageCodec.decode(values[0], targetWidthPx);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        cache.put(key, bitmap);
        if (key.equals(imageView.getTag())) {
            imageView.setImageBitmap(bitmap);
        }
    }
}
