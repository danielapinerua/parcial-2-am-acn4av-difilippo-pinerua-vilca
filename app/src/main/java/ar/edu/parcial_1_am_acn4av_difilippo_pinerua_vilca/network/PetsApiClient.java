package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.network;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PetsApiClient {

    public enum Error { NETWORK, SERVER, DATA }

    public interface ImageUrlsCallback {
        void onSuccess(List<String> urls);

        void onError(Error error);
    }

    private interface BodyParser {
        List<String> parse(String body) throws Exception;
    }

    private static final String PETS_URL =
            "https://raw.githubusercontent.com/danielapinerua/"
                    + "parcial-2-am-acn4av-difilippo-pinerua-vilca/main/docs/pets.json";

    private static final String DOGS_URL = "https://dog.ceo/api/breeds/image/random/";
    private static final String CATS_URL = "https://api.thecatapi.com/v1/images/search?limit=";

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<Call> activeCalls = new ArrayList<>();

    public void fetchRandomDogs(int count, ImageUrlsCallback callback) {
        Request request = new Request.Builder().url(DOGS_URL + count).build();
        enqueue(request, callback, body -> {
            JSONArray array = new JSONObject(body).getJSONArray("message");
            List<String> urls = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                addUrl(urls, array.optString(i, null));
            }
            return urls;
        });
    }

    public void fetchRandomCats(int count, ImageUrlsCallback callback) {
        Request request = new Request.Builder().url(CATS_URL + count).build();
        enqueue(request, callback, body -> {
            JSONArray array = new JSONArray(body);
            List<String> urls = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object != null) {
                    addUrl(urls, object.optString("url", null));
                }
            }
            return urls;
        });
    }

    public void cancel() {
        for (Call call : activeCalls) {
            call.cancel();
        }
        activeCalls.clear();
    }

    private void enqueue(Request request, ImageUrlsCallback callback, BodyParser parser) {
        Call call = CLIENT.newCall(request);
        activeCalls.add(call);

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (call.isCanceled()) {
                    return;
                }
                mainHandler.post(() -> callback.onError(Error.NETWORK));
            }

            @Override
            public void onResponse(Call call, Response response) {
                ResponseBody body = response.body();
                if (!response.isSuccessful() || body == null) {
                    mainHandler.post(() -> callback.onError(Error.SERVER));
                    return;
                }

                try (ResponseBody ignored = body) {
                    List<String> urls = parser.parse(body.string());
                    mainHandler.post(() -> callback.onSuccess(urls));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError(Error.DATA));
                }
            }
        });
    }

    private static void addUrl(List<String> urls, String url) {
        if (url != null && url.startsWith("https://")) {
            urls.add(url);
        }
    }
}
