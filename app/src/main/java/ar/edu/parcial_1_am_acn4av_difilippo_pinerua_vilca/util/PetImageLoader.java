package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.R;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.service.PetService;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks.DecodeImageTask;

public class PetImageLoader {

    private static final String TAG = "PetImageLoader";

    private static PetImageLoader instance;

    private final Map<String, Bitmap> cache = new HashMap<>();
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
                new DecodeImageTask(cache, key, targetWidthPx, imageView).execute(base64);
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Could not load image for pet " + petId, e);
            }
        });
    }

    public void clear() {
        cache.clear();
    }

    private static String cacheKey(String petId, long version) {
        return petId + "@" + version;
    }
}
