package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks;

import android.os.AsyncTask;

import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.activities.HomeActivity;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.network.PetsApiClient;

/**
 * Fetches random cat images on a background thread and delivers them to the home.
 */
public class FetchCatsTask extends AsyncTask<Integer, Void, List<String>> {

    private final HomeActivity activity;
    private final PetsApiClient apiClient;

    public FetchCatsTask(HomeActivity activity, PetsApiClient apiClient) {
        this.activity = activity;
        this.apiClient = apiClient;
    }

    @Override
    protected List<String> doInBackground(Integer... counts) {
        return apiClient.fetchRandomCats(counts[0]);
    }

    @Override
    protected void onPostExecute(List<String> urls) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        activity.onCatsLoaded(urls);
    }
}
