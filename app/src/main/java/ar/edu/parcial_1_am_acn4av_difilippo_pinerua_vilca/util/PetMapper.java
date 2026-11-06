package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.DTO.PetDTO;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.constants.PetType;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;

public final class PetMapper {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_BREED = "breed";
    public static final String FIELD_AGE = "age";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_IMAGE_URL = "imageUrl";
    public static final String FIELD_IMAGE_VERSION = "imageVersion";
    public static final String FIELD_ADOPTED = "adopted";
    public static final String FIELD_ADOPTED_BY = "adoptedBy";
    public static final String FIELD_ADOPTED_AT = "adoptedAt";
    public static final String FIELD_CREATED_BY = "createdBy";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_UPDATED_BY = "updatedBy";
    public static final String FIELD_UPDATED_AT = "updatedAt";

    public static final String FIELD_IMAGE_DATA = "data";
    public static final String FIELD_IMAGE_MIME_TYPE = "mimeType";
    public static final String FIELD_IMAGE_OWNER = "owner";
    public static final String FIELD_IMAGE_UPDATED_AT = "updatedAt";

    private PetMapper() {
    }

    public static Pet toPet(DocumentSnapshot document) {
        return new Pet(
                document.getId(),
                safe(document.getString(FIELD_NAME)),
                safe(document.getString(FIELD_BREED)),
                safe(document.getString(FIELD_AGE)),
                document.getString(FIELD_TYPE) != null
                        ? document.getString(FIELD_TYPE)
                        : PetType.DOG.type,
                safe(document.getString(FIELD_DESCRIPTION)),
                Boolean.TRUE.equals(document.getBoolean(FIELD_ADOPTED)),
                null,
                document.getString(FIELD_IMAGE_URL),
                document.getString(FIELD_CREATED_BY),
                longOrZero(document.getLong(FIELD_IMAGE_VERSION))
        );
    }

    public static Map<String, Object> createMap(PetDTO dto, long imageVersion, String uid) {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_NAME, dto.getName());
        map.put(FIELD_BREED, dto.getBreed());
        map.put(FIELD_AGE, dto.getAge());
        map.put(FIELD_TYPE, dto.getType());
        map.put(FIELD_DESCRIPTION, dto.getDescription());
        map.put(FIELD_IMAGE_URL, null);
        map.put(FIELD_IMAGE_VERSION, imageVersion);
        map.put(FIELD_ADOPTED, false);
        map.put(FIELD_ADOPTED_BY, null);
        map.put(FIELD_ADOPTED_AT, null);
        map.put(FIELD_CREATED_BY, uid);
        map.put(FIELD_CREATED_AT, FieldValue.serverTimestamp());
        return map;
    }

    public static Map<String, Object> updateMap(PetDTO dto, String uid, Long newImageVersion) {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_NAME, dto.getName());
        map.put(FIELD_BREED, dto.getBreed());
        map.put(FIELD_AGE, dto.getAge());
        map.put(FIELD_TYPE, dto.getType());
        map.put(FIELD_DESCRIPTION, dto.getDescription());
        map.put(FIELD_UPDATED_BY, uid);
        map.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        if (newImageVersion != null) {
            map.put(FIELD_IMAGE_VERSION, newImageVersion);
        }
        return map;
    }

    public static Map<String, Object> imageMap(String base64, String owner) {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_IMAGE_DATA, base64);
        map.put(FIELD_IMAGE_MIME_TYPE, "image/jpeg");
        map.put(FIELD_IMAGE_OWNER, owner);
        map.put(FIELD_IMAGE_UPDATED_AT, FieldValue.serverTimestamp());
        return map;
    }

    public static long createdAtMillis(DocumentSnapshot document) {
        Timestamp timestamp = document.getTimestamp(FIELD_CREATED_AT);
        return timestamp != null ? timestamp.toDate().getTime() : 0L;
    }

    public static long adoptedAtMillis(DocumentSnapshot document) {
        Timestamp timestamp = document.getTimestamp(FIELD_ADOPTED_AT);
        return timestamp != null ? timestamp.toDate().getTime() : 0L;
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private static long longOrZero(Long value) {
        return value != null ? value : 0L;
    }
}
