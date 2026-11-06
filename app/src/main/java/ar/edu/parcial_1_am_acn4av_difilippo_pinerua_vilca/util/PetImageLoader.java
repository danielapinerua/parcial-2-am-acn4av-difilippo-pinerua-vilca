package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.R;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.service.PetService;

public class PetImageLoader {

    private static final String TAG = "PetImageLoader";
    private static final int CACHE_BYTES = 8 * 1024 * 1024;

    private static PetImageLoader instance;

    private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(CACHE_BYTES) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final PetService petService = PetService.getInstance();

    private PetImageLoader() {
    }

    public static synchronized PetImageLoader getInstance() {
        if (instance == null) {
            instance = new PetImageLoader();
        }
        return instance;
    }

    public void load(String petId, long version, int targetWidthPx, ImageView imageView) {
        String key = cacheKey(petId, version);
        Bitmap cached = cache.get(key);
        imageView.setTag(key);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        imageView.setImageResource(R.drawable.pet_placeholder);
        petService.fetchImage(petId, new PetService.Callback<String>() {
            @Override
            public void onSuccess(String base64) {
                executor.execute(() -> {
                    Bitmap bitmap = PetImageCodec.decode(base64, targetWidthPx);
                    if (bitmap == null) {
                        return;
                    }
                    cache.put(key, bitmap);
                    mainHandler.post(() -> {
                        if (key.equals(imageView.getTag())) {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                });
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Could not load image for pet " + petId, e);
            }
        });
    }

    public void clear() {
        cache.evictAll();
    }

    private static String cacheKey(String petId, long version) {
        return petId + "@" + version;
    }
}
