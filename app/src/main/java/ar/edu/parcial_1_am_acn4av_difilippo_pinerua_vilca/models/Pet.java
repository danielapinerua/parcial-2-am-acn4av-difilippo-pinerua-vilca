package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models;

public class Pet {
    private String id;
    private String name;
    private String breed;
    private String age;
    private String type;
    private String description;
    private boolean isAdopted;
    private int imageResId;
    private String imageUri;
    private String imageUrl;
    private String createdBy;
    private long imageVersion;

    public Pet(String id, String name, String breed, String age, String type,
               String description, boolean isAdopted, int imageResId) {
        this(id, name, breed, age, type, description, isAdopted, null, null, null, 0L);
        this.imageResId = imageResId;
    }

    public Pet(String id, String name, String breed, String age, String type,
               String description, boolean isAdopted, String imageUri) {
        this(id, name, breed, age, type, description, isAdopted, imageUri, null, null, 0L);
    }

    public Pet(String id, String name, String breed, String age, String type,
               String description, boolean isAdopted, String imageUri, String imageUrl) {
        this(id, name, breed, age, type, description, isAdopted, imageUri, imageUrl, null, 0L);
    }

    public Pet(String id, String name, String breed, String age, String type,
               String description, boolean isAdopted, String imageUri, String imageUrl,
               String createdBy, long imageVersion) {
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
        this.createdBy = createdBy;
        this.imageVersion = imageVersion;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getBreed() { return breed; }
    public String getAge() { return age; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public boolean isAdopted() { return isAdopted; }
    public int getImageResId() { return imageResId; }
    public String getImageUri() { return imageUri; }
    public String getImageUrl() { return imageUrl; }
    public String getCreatedBy() { return createdBy; }
    public long getImageVersion() { return imageVersion; }
    public boolean hasStoredImage() { return imageVersion > 0; }
    public void setAdopted(boolean adopted) { isAdopted = adopted; }
}
