package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.DTO.PetDTO;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetImageCodec;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.repository.PetRepository;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;

/**
 * Business layer. Validates the input, resolves the session and keeps the
 * in-memory catalog used by the UI. Delegates every database operation to
 * PetRepository.
 */
public class PetService {

    public interface Callback<T> {
        void onSuccess(T value);

        void onError(Exception e);
    }

    private static final String ERROR_EMPTY_FIELDS = "empty_fields";
    private static final String ERROR_NOT_AUTHENTICATED = "not_authenticated";
    private static final String ERROR_NOT_OWNER = "not_owner";
    private static final String ERROR_PET_ADOPTED = "pet_adopted";
    private static final String ERROR_IMAGE_TOO_LARGE = "image_too_large";
    private static final String ERROR_NOT_FOUND = "pet_not_found";

    private static PetService instance;

    private final PetRepository repository = PetRepository.getInstance();

    private List<Pet> petList = new ArrayList<>();
    private boolean loaded;

    private PetService() {
    }

    public static synchronized PetService getInstance() {
        if (instance == null) {
            instance = new PetService();
        }
        return instance;
    }

    // ── READ ──────────────────────────────────────────────────────────────

    public void fetchAll(Callback<List<Pet>> callback) {
        repository.fetchAll(new PetRepository.Callback<List<Pet>>() {
            @Override
            public void onSuccess(List<Pet> pets) {
                petList = new ArrayList<>(pets);
                loaded = true;
                callback.onSuccess(pets);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void fetchById(String petId, Callback<Pet> callback) {
        if (petId == null) {
            callback.onError(new IllegalStateException(ERROR_NOT_FOUND));
            return;
        }

        repository.fetchById(petId, new PetRepository.Callback<Pet>() {
            @Override
            public void onSuccess(Pet pet) {
                replaceCachedPet(pet);
                callback.onSuccess(pet);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void fetchAdoptions(Callback<List<Pet>> callback) {
        String uid;
        try {
            uid = requireUid();
        } catch (Exception e) {
            callback.onError(e);
            return;
        }
        repository.fetchByAdoptedBy(uid, adapt(callback));
    }

    public void fetchImage(String petId, Callback<String> callback) {
        repository.fetchImage(petId, adapt(callback));
    }

    // ── WRITE ─────────────────────────────────────────────────────────────

    public void create(PetDTO dto, byte[] jpeg, Callback<String> callback) {
        String uid;
        try {
            validate(dto, jpeg);
            uid = requireUid();
        } catch (Exception e) {
            callback.onError(e);
            return;
        }
        repository.create(dto, jpeg, uid, adapt(callback));
    }

    public void update(String petId, PetDTO dto, byte[] jpeg, Callback<Void> callback) {
        String uid;
        try {
            if (petId == null) {
                throw new IllegalStateException(ERROR_NOT_FOUND);
            }
            validate(dto, jpeg);
            uid = requireUid();
            validateOwnership(petId, uid);
        } catch (Exception e) {
            callback.onError(e);
            return;
        }
        repository.update(petId, dto, jpeg, uid, adapt(callback));
    }

    public void adopt(String petId, Callback<Void> callback) {
        String uid;
        try {
            if (petId == null) {
                throw new IllegalStateException(ERROR_NOT_FOUND);
            }
            uid = requireUid();
        } catch (Exception e) {
            callback.onError(e);
            return;
        }

        repository.adopt(petId, uid, new PetRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                Pet cached = getPetById(petId);
                if (cached != null) {
                    cached.setAdopted(true);
                }
                callback.onSuccess(null);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    // ── CACHE ─────────────────────────────────────────────────────────────

    public boolean isLoaded() {
        return loaded;
    }

    public List<Pet> getPetsByType(String type) {
        if (type == null || type.equalsIgnoreCase("All")) {
            return petList;
        }
        List<Pet> filtered = new ArrayList<>();
        for (Pet pet : petList) {
            if (type.equalsIgnoreCase(pet.getType())) {
                filtered.add(pet);
            }
        }
        return filtered;
    }

    public Pet getPetById(String id) {
        if (id == null) {
            return null;
        }
        for (Pet pet : petList) {
            if (id.equals(pet.getId())) {
                return pet;
            }
        }
        return null;
    }

    public void clear() {
        petList = new ArrayList<>();
        loaded = false;
    }

    // ── VALIDATION ────────────────────────────────────────────────────────

    private void validate(PetDTO dto, byte[] jpeg) {
        if (dto == null
                || isBlank(dto.getName())
                || isBlank(dto.getBreed())
                || isBlank(dto.getAge())
                || isBlank(dto.getType())
                || isBlank(dto.getDescription())) {
            throw new IllegalStateException(ERROR_EMPTY_FIELDS);
        }
        if (jpeg != null && jpeg.length > PetImageCodec.MAX_IMAGE_BYTES) {
            throw new IllegalStateException(ERROR_IMAGE_TOO_LARGE);
        }
    }

    private void validateOwnership(String petId, String uid) {
        Pet cached = getPetById(petId);
        if (cached == null) {
            // The security rules enforce ownership for pets not loaded yet.
            return;
        }
        if (!uid.equals(cached.getCreatedBy())) {
            throw new IllegalStateException(ERROR_NOT_OWNER);
        }
        if (cached.isAdopted()) {
            throw new IllegalStateException(ERROR_PET_ADOPTED);
        }
    }

    private String requireUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException(ERROR_NOT_AUTHENTICATED);
        }
        return user.getUid();
    }

    private void replaceCachedPet(Pet pet) {
        for (int i = 0; i < petList.size(); i++) {
            if (pet.getId().equals(petList.get(i).getId())) {
                petList.set(i, pet);
                return;
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static <T> PetRepository.Callback<T> adapt(Callback<T> callback) {
        return new PetRepository.Callback<T>() {
            @Override
            public void onSuccess(T value) {
                callback.onSuccess(value);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        };
    }
}
