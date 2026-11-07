package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.activities;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks.CompressImageTask;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks.ImageDownloadTask;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.R;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.constants.PetType;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.DTO.PetDTO;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.service.PetService;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetErrorMessages;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetImageLoader;

public class EditPetActivity extends PetFormActivity {

    private final PetService petService = PetService.getInstance();

    private TextInputEditText etName;
    private TextInputEditText etBreed;
    private TextInputEditText etAge;
    private TextInputEditText etDescription;
    private Spinner spinnerType;
    private Button btnSave;
    private String petId;
    private CompressImageTask compressTask;
    private PetDTO pendingDto;
    private boolean saving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_pet);

        petId = getIntent().getStringExtra("pet_id");
        if (petId == null) {
            Toast.makeText(this, R.string.pet_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView title = findViewById(R.id.tv_title_add);
        title.setText(R.string.title_edit_pet);

        Button btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save_pet);
        btnSave.setText(R.string.btn_save_changes);

        FrameLayout photoFrame = findViewById(R.id.photo_frame);
        Button btnSelectPhoto = findViewById(R.id.btn_select_photo);
        etName = findViewById(R.id.et_name);
        etBreed = findViewById(R.id.et_breed);
        etAge = findViewById(R.id.et_age);
        etDescription = findViewById(R.id.et_description);
        spinnerType = findViewById(R.id.spinner_type);

        stylePhotoFrame();
        styleSpinner(spinnerType);

        List<String> types = new ArrayList<>();
        for (PetType petType : PetType.values()) {
            types.add(petType.type);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSelectPhoto.setOnClickListener(v -> openImagePicker());
        photoFrame.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> savePet());

        loadPet();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadPet() {
        Pet cached = petService.getPetById(petId);
        if (cached != null) {
            bindPet(cached);
            return;
        }

        petService.fetchById(petId, new PetService.Callback<Pet>() {
            @Override
            public void onSuccess(Pet pet) {
                bindPet(pet);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditPetActivity.this,
                        PetErrorMessages.messageRes(e), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void bindPet(Pet pet) {
        etName.setText(pet.getName());
        etBreed.setText(pet.getBreed());
        etAge.setText(pet.getAge());
        etDescription.setText(pet.getDescription());

        PetType[] petTypes = PetType.values();
        for (int i = 0; i < petTypes.length; i++) {
            if (petTypes[i].type.equalsIgnoreCase(pet.getType())) {
                spinnerType.setSelection(i);
                break;
            }
        }

        ImageView imageView = findViewById(R.id.iv_pet_photo);
        if (pet.hasStoredImage()) {
            preparePhotoView();
            PetImageLoader.getInstance().load(
                    pet.getId(), pet.getImageVersion(), dpToPx(140), imageView);
        } else if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
            preparePhotoView();
            imageView.setImageResource(R.drawable.pet_placeholder);
            new ImageDownloadTask(imageView, dpToPx(140)).execute(pet.getImageUrl());
        }
    }

    private void savePet() {
        if (saving) {
            return;
        }

        String name = getText(etName);
        String breed = getText(etBreed);
        String age = getText(etAge);
        String description = getText(etDescription);
        String type = spinnerType.getSelectedItem().toString();

        pendingDto = new PetDTO(name, breed, age, type, description);

        compressTask = new CompressImageTask(this);
        compressTask.execute(selectedImageUri);
    }

    @Override
    public void onCompressionStarted() {
        setSaving(true);
    }

    @Override
    public void onImageReady(byte[] jpeg) {
        updatePet(pendingDto, jpeg);
    }

    @Override
    public void onImageError() {
        setSaving(false);
        Toast.makeText(this, R.string.image_error, Toast.LENGTH_LONG).show();
    }

    private void updatePet(PetDTO dto, byte[] jpeg) {
        petService.update(petId, dto, jpeg, new PetService.Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(EditPetActivity.this, R.string.pet_updated, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                setSaving(false);
                Toast.makeText(EditPetActivity.this,
                        PetErrorMessages.messageRes(e), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setSaving(boolean value) {
        saving = value;
        btnSave.setEnabled(!value);
        btnSave.setText(value ? R.string.saving_pet : R.string.btn_save_changes);
    }

    private String getText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    @Override
    protected void onDestroy() {
        if (compressTask != null) {
            compressTask.cancel(true);
        }
        super.onDestroy();
    }
}
