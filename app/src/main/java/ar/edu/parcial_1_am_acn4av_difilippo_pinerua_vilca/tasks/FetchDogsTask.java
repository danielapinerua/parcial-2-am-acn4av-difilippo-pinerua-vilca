package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks;

import android.os.AsyncTask;

import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.activities.HomeActivity;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.network.PetsApiClient;

/**
 * Fetches random dog images on a background thread and delivers them to the home.
 */
public class FetchDogsTask extends AsyncTask<Integer, Void, List<String>> {

    private final HomeActivity activity;
    private final PetsApiClient apiClient;

    public FetchDogsTask(HomeActivity activity, PetsApiClient apiClient) {
        this.activity = activity;
        this.apiClient = apiClient;
    }

    @Override
    protected List<String> doInBackground(Integer... counts) {
        return apiClient.fetchRandomDogs(counts[0]);
    }

    @Override
    protected void onPostExecute(List<String> urls) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        activity.onDogsLoaded(urls);
    }
}
