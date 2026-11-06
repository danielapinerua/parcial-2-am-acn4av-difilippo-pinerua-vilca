package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.DTO;

public class PetDTO {
    private final String name;
    private final String breed;
    private final String age;
    private final String type;
    private final String description;

    public PetDTO(String name, String breed, String age, String type, String description) {
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.type = type;
        this.description = description;
    }

    public String getName() { return name; }
    public String getBreed() { return breed; }
    public String getAge() { return age; }
    public String getType() { return type; }
    public String getDescription() { return description; }
}
