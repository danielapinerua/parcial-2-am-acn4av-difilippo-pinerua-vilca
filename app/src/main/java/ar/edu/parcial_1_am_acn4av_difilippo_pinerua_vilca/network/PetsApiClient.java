package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.network;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
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

    public interface PetsCallback {
        void onSuccess(List<Pet> pets);

        void onError(Error error);
    }

    private static final String PETS_URL =
            "https://raw.githubusercontent.com/danielapinerua/"
                    + "parcial-2-am-acn4av-difilippo-pinerua-vilca/main/docs/pets.json";

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Call currentCall;

    public void fetchPets(PetsCallback callback) {
        Request request = new Request.Builder().url(PETS_URL).build();
        currentCall = CLIENT.newCall(request);

        currentCall.enqueue(new okhttp3.Callback() {
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
                    List<Pet> pets = PetJsonParser.parse(body.string());
                    mainHandler.post(() -> callback.onSuccess(pets));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError(Error.DATA));
                }
            }
        });
    }

    public void cancel() {
        if (currentCall != null) {
            currentCall.cancel();
        }
    }
}
