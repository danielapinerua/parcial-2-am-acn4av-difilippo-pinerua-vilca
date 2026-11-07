package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks;

import android.os.AsyncTask;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.activities.MainActivity;

/**
 * Keeps the splash screen visible without blocking the UI thread.
 */
public class SplashTask extends AsyncTask<Void, Void, Void> {

    private final MainActivity activity;
    private final long delayMillis;

    public SplashTask(MainActivity activity, long delayMillis) {
        this.activity = activity;
        this.delayMillis = delayMillis;
    }

    @Override
    protected Void doInBackground(Void... ignored) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void ignored) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        activity.onSplashFinished();
    }
}
