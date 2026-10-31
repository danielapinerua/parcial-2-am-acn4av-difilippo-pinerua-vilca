package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.data;

import java.util.ArrayList;
import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.R;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.constants.PetType;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;

public class PetRepository {
    private static PetRepository instance;
    private List<Pet> petList;

    private PetRepository() {
        petList = new ArrayList<>();
        initDummyData();
    }

    public static PetRepository getInstance() {
        if (instance == null) {
            instance = new PetRepository();
        }
        return instance;
    }

    private void initDummyData() {
        int defaultImg = R.mipmap.ic_launcher;

        petList.add(new Pet(
                1,
                "Max",
                "Golden Retriever",
                "5 meses",
                PetType.DOG.type,
                "Muy amigable y juguetón. Le encanta correr y jugar.",
                false,
                null,
                "https://images.unsplash.com/photo-1552053831-71594a27632d?w=800"
        ));

        petList.add(new Pet(
                2,
                "Daphne",
                "Sin raza",
                "5 años",
                PetType.CAT.type,
                "Tranquila y cariñosa. Le encanta dormir al sol.",
                false,
                R.drawable.daphne
        ));

        petList.add(new Pet(
                3,
                "Rocky",
                "Bulldog",
                "3 años",
                PetType.DOG.type,
                "Es fuerte pero muy cariñoso y se lleva muy bien con los niños.",
                true,
                R.drawable.rocky
        ));

        petList.add(new Pet(
                4,
                "Paris",
                "Angora turco",
                "4 meses",
                PetType.CAT.type,
                "Muy cariñosa y juguetona. Necesita cuidados frecuentes de su pelaje.",
                false,
                R.drawable.paris
        ));

        petList.add(new Pet(
                5,
                "Charlie",
                "Sin raza",
                "2 meses",
                PetType.DOG.type,
                "Muy activo y juguetón.",
                false,
                R.drawable.charlie
        ));

        petList.add(new Pet(
                6,
                "Mora",
                "Sin raza",
                "2 años",
                PetType.CAT.type,
                "Muy tranquila y cariñosa. Le encanta recibir mimos y dormir en lugares calentitos.",
                false,
                R.drawable.mora
        ));

        petList.add(new Pet(
                7,
                "Toby",
                "Sin raza",
                "1 año",
                PetType.DOG.type,
                "Muy juguetón y cariñoso. Le encanta salir a pasear y jugar con otros perros.",
                false,
                R.drawable.toby
        ));
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