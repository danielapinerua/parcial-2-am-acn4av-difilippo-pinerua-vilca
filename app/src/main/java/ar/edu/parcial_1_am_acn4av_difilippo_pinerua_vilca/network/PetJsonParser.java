package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.constants.PetType;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;

public final class PetJsonParser {

    private static final String KEY_PETS = "pets";

    private PetJsonParser() {
    }

    public static List<Pet> parse(String json) throws JSONException {
        JSONArray array = new JSONObject(json).getJSONArray(KEY_PETS);
        List<Pet> pets = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            pets.add(parsePet(array.getJSONObject(i)));
        }
        return pets;
    }

    private static Pet parsePet(JSONObject json) throws JSONException {
        return new Pet(
                json.getInt("id"),
                json.getString("name"),
                json.optString("breed", "Sin raza"),
                json.optString("age", ""),
                json.optString("type", PetType.DOG.type),
                json.optString("description", ""),
                json.optBoolean("adopted", false),
                null,
                json.optString("imageUrl", null)
        );
    }
}
