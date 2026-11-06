package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util;

import com.google.firebase.firestore.FirebaseFirestoreException;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.R;

/**
 * Translates business and Firestore errors into user facing string resources.
 */
public final class PetErrorMessages {

    private PetErrorMessages() {
    }

    public static int messageRes(Exception error) {
        String message = error != null ? error.getMessage() : null;

        if ("empty_fields".equals(message)) {
            return R.string.form_incomplete;
        }
        if ("not_authenticated".equals(message)) {
            return R.string.error_permission;
        }
        if ("not_owner".equals(message)) {
            return R.string.error_not_owner;
        }
        if ("pet_adopted".equals(message)) {
            return R.string.error_pet_adopted;
        }
        if ("already_adopted".equals(message)) {
            return R.string.pet_already_adopted;
        }
        if ("pet_not_found".equals(message) || "not_found".equals(message)) {
            return R.string.pet_not_found;
        }
        if ("image_too_large".equals(message)) {
            return R.string.image_too_large;
        }
        if ("image_not_found".equals(message)) {
            return R.string.image_error;
        }

        if (error instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException.Code code = ((FirebaseFirestoreException) error).getCode();
            switch (code) {
                case UNAVAILABLE:
                case DEADLINE_EXCEEDED:
                case CANCELLED:
                    return R.string.error_network;
                case PERMISSION_DENIED:
                case UNAUTHENTICATED:
                    return R.string.error_permission;
                case NOT_FOUND:
                    return R.string.pet_not_found;
                default:
                    return R.string.error_server;
            }
        }

        return R.string.error_server;
    }
}
