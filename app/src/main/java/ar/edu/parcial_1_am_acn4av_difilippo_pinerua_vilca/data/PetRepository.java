package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.data;

import java.util.ArrayList;
import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;

public class PetRepository {
    private static PetRepository instance;
    private List<Pet> petList;
    private boolean loaded;

    private PetRepository() {
        petList = new ArrayList<>();
    }

    public static PetRepository getInstance() {
        if (instance == null) {
            instance = new PetRepository();
        }
        return instance;
    }

    public void setPets(List<Pet> pets) {
        this.petList = new ArrayList<>(pets);
        this.loaded = true;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public List<Pet> getAllPets() {
        return petList;
    }

    public List<Pet> getPetsByType(String type) {
        if (type.equalsIgnoreCase("All")) {
            return petList;
        }
        List<Pet> filteredList = new ArrayList<>();
        for (Pet pet : petList) {
            if (pet.getType().equalsIgnoreCase(type)) {
                filteredList.add(pet);
            }
        }
        return filteredList;
    }

    public Pet getPetById(int id) {
        for (Pet pet : petList) {
            if (pet.getId() == id) {
                return pet;
            }
        }
        return null;
    }

    public void adoptPet(int id) {
        Pet pet = getPetById(id);
        if (pet != null) {
            pet.setAdopted(true);
        }
    }

    public void addPet(String name, String breed, String age, String type, String description, String imageUri) {
        int maxId = 0;
        for (Pet pet : petList) {
            if (pet.getId() > maxId) {
                maxId = pet.getId();
            }
        }
        Pet newPet = new Pet(
                maxId + 1,
                name,
                breed,
                age,
                type,
                description,
                false,
                imageUri
        );
        petList.add(newPet);
    }
}
