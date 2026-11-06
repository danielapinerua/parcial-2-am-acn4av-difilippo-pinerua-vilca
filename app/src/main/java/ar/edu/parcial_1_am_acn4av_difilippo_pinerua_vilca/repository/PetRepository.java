package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.repository;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.DTO.PetDTO;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetImageCodec;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetMapper;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;

/**
 * Persistence layer. Knows only about Firestore: collections, documents, batches
 * and the adoption transaction. No business rules, no cache, no Android UI.
 */
public class PetRepository {

    public interface Callback<T> {
        void onSuccess(T value);

        void onError(Exception e);
    }

    private static final String COLLECTION_PETS = "pets";
    private static final String COLLECTION_IMAGES = "pet_images";

    private static PetRepository instance;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private PetRepository() {
    }

    public static synchronized PetRepository getInstance() {
        if (instance == null) {
            instance = new PetRepository();
        }
        return instance;
    }

    // ── READ ──────────────────────────────────────────────────────────────

    public void fetchAll(Callback<List<Pet>> callback) {
        db.collection(COLLECTION_PETS)
                .get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(
                        toPets(snapshot, PetMapper.FIELD_CREATED_AT)))
                .addOnFailureListener(callback::onError);
    }

    public void fetchById(String petId, Callback<Pet> callback) {
        db.collection(COLLECTION_PETS)
                .document(petId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        callback.onError(new IllegalStateException("pet_not_found"));
                        return;
                    }
                    callback.onSuccess(PetMapper.toPet(document));
                })
                .addOnFailureListener(callback::onError);
    }

    public void fetchByAdoptedBy(String uid, Callback<List<Pet>> callback) {
        db.collection(COLLECTION_PETS)
                .whereEqualTo(PetMapper.FIELD_ADOPTED_BY, uid)
                .get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(
                        toPets(snapshot, PetMapper.FIELD_ADOPTED_AT)))
                .addOnFailureListener(callback::onError);
    }

    public void fetchImage(String petId, Callback<String> callback) {
        db.collection(COLLECTION_IMAGES)
                .document(petId)
                .get()
                .addOnSuccessListener(document -> {
                    String data = document.getString(PetMapper.FIELD_IMAGE_DATA);
                    if (!document.exists() || data == null || data.isEmpty()) {
                        callback.onError(new IllegalStateException("image_not_found"));
                        return;
                    }
                    callback.onSuccess(data);
                })
                .addOnFailureListener(callback::onError);
    }

    // ── CREATE ────────────────────────────────────────────────────────────

    public void create(PetDTO dto, byte[] jpeg, String uid, Callback<String> callback) {
        long imageVersion = jpeg != null ? System.currentTimeMillis() : 0L;
        DocumentReference petRef = db.collection(COLLECTION_PETS).document();

        WriteBatch batch = db.batch();
        batch.set(petRef, PetMapper.createMap(dto, imageVersion, uid));

        if (jpeg != null) {
            DocumentReference imageRef = db.collection(COLLECTION_IMAGES).document(petRef.getId());
            batch.set(imageRef, PetMapper.imageMap(PetImageCodec.encode(jpeg), uid));
        }

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(petRef.getId()))
                .addOnFailureListener(callback::onError);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────

    public void update(String petId, PetDTO dto, byte[] jpeg, String uid, Callback<Void> callback) {
        Long newImageVersion = jpeg != null ? System.currentTimeMillis() : null;

        WriteBatch batch = db.batch();
        batch.update(db.collection(COLLECTION_PETS).document(petId),
                PetMapper.updateMap(dto, uid, newImageVersion));

        if (jpeg != null) {
            DocumentReference imageRef = db.collection(COLLECTION_IMAGES).document(petId);
            batch.set(imageRef, PetMapper.imageMap(PetImageCodec.encode(jpeg), uid));
        }

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void adopt(String petId, String uid, Callback<Void> callback) {
        DocumentReference petRef = db.collection(COLLECTION_PETS).document(petId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(petRef);
            if (!snapshot.exists()) {
                return "not_found";
            }
            if (Boolean.TRUE.equals(snapshot.getBoolean(PetMapper.FIELD_ADOPTED))) {
                return "already_adopted";
            }
            transaction.update(petRef,
                    PetMapper.FIELD_ADOPTED, true,
                    PetMapper.FIELD_ADOPTED_BY, uid,
                    PetMapper.FIELD_ADOPTED_AT, FieldValue.serverTimestamp());
            return "ok";
        }).addOnSuccessListener(result -> {
            if ("ok".equals(result)) {
                callback.onSuccess(null);
            } else {
                callback.onError(new IllegalStateException(result));
            }
        }).addOnFailureListener(callback::onError);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static List<Pet> toPets(QuerySnapshot snapshot, String sortField) {
        List<QueryDocumentSnapshot> documents = new ArrayList<>();
        for (QueryDocumentSnapshot document : snapshot) {
            documents.add(document);
        }
        documents.sort((first, second) -> Long.compare(
                timestampMillis(second, sortField), timestampMillis(first, sortField)));

        List<Pet> pets = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            pets.add(PetMapper.toPet(document));
        }
        return pets;
    }

    private static long timestampMillis(DocumentSnapshot document, String field) {
        if (PetMapper.FIELD_CREATED_AT.equals(field)) {
            return PetMapper.createdAtMillis(document);
        }
        return PetMapper.adoptedAtMillis(document);
    }
}
