package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.network;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;

/**
 * Synchronous HTTP helper. It must be called from doInBackground().
 */
public class PetsApiClient {

    private static final String PETS_URL =
            "https://raw.githubusercontent.com/danielapinerua/"
                    + "parcial-2-am-acn4av-difilippo-pinerua-vilca/main/docs/pets.json";

    private static final String DOGS_URL = "https://dog.ceo/api/breeds/image/random/";
    private static final String CATS_URL = "https://api.thecatapi.com/v1/images/search?limit=";

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15_000;

    //SIN USO
    public List<Pet> fetchPets() {
        String body = read(PETS_URL);
        if (body == null) {
            return new ArrayList<>();
        }
        try {
            return PetJsonParser.parse(body);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<String> fetchRandomDogs(int count) {
        String body = read(DOGS_URL + count);
        List<String> urls = new ArrayList<>();
        if (body == null) {
            return urls;
        }
        try {
            JSONArray array = new JSONObject(body).getJSONArray("message");
            for (int i = 0; i < array.length(); i++) {
                addUrl(urls, array.optString(i, null));
            }
        } catch (Exception ignored) {
            // Invalid response: the caller falls back to the curated images.
        }
        return urls;
    }

    public List<String> fetchRandomCats(int count) {
        String body = read(CATS_URL + count);
        List<String> urls = new ArrayList<>();
        if (body == null) {
            return urls;
        }
        try {
            JSONArray array = new JSONArray(body);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object != null) {
                    addUrl(urls, object.optString("url", null));
                }
            }
        } catch (Exception ignored) {
            // Invalid response: the caller falls back to the curated images.
        }
        return urls;
    }

    private String read(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            try (InputStream input = connection.getInputStream()) {
                return readAll(input);
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readAll(InputStream input) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private static void addUrl(List<String> urls, String url) {
        if (url != null && url.startsWith("https://")) {
            urls.add(url);
        }
    }
}
