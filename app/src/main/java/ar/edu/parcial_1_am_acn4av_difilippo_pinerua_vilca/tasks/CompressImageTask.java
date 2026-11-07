package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks;

import android.net.Uri;
import android.os.AsyncTask;

import java.io.IOException;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.activities.PetFormActivity;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetImageCodec;

/**
 * Compresses the selected image on a background thread. The activity is
 * notified through its own AsyncTask callbacks (onCompressionStarted,
 * onImageReady, onImageError).
 */
public class CompressImageTask extends AsyncTask<Uri, Void, byte[]> {

    private final PetFormActivity activity;
    private boolean failed;

    public CompressImageTask(PetFormActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        if (!activity.isFinishing() && !activity.isDestroyed()) {
            activity.onCompressionStarted();
        }
    }

    @Override
    protected byte[] doInBackground(Uri... uris) {
        Uri imageUri = uris[0];
        if (imageUri == null) {
            return null;
        }
        try {
            return PetImageCodec.compressFromUri(activity.getApplicationContext(), imageUri);
        } catch (IOException e) {
            failed = true;
            return null;
        }
    }

    @Override
    protected void onPostExecute(byte[] jpeg) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        if (failed) {
            activity.onImageError();
        } else {
            activity.onImageReady(jpeg);
        }
    }
}
