package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models;

public class Pet {
    private int id;
    private String name;
    private String breed;
    private String age;
    private String type;
    private String description;
    private boolean isAdopted;
    private int imageResId;
    private String imageUri;
    private String imageUrl;

    public Pet(int id, String name, String breed, String age, String type,
               String description, boolean isAdopted, int imageResId) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.type = type;
        this.description = description;
        this.isAdopted = isAdopted;
        this.imageResId = imageResId;
        this.imageUri = null;
        this.imageUrl = null;
    }

    public Pet(int id, String name, String breed, String age, String type,
               String description, boolean isAdopted, String imageUri) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.type = type;
        this.description = description;
        this.isAdopted = isAdopted;
        this.imageResId = 0;
        this.imageUri = imageUri;
        this.imageUrl = null;
    }

    public Pet(int id, String name, String breed, String age, String type,
               String description, boolean isAdopted, String imageUri, String imageUrl) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.type = type;
        this.description = description;
        this.isAdopted = isAdopted;
        this.imageResId = 0;
        this.imageUri = imageUri;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getBreed() { return breed; }
    public String getAge() { return age; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public boolean isAdopted() { return isAdopted; }
    public int getImageResId() { return imageResId; }
    public String getImageUri() { return imageUri; }
    public String getImageUrl() { return imageUrl; }
    public void setAdopted(boolean adopted) { isAdopted = adopted; }
}