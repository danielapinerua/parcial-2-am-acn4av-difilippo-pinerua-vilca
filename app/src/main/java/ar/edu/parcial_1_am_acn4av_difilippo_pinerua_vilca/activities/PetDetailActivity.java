package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.activities;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks.ImageDownloadTask;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.R;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.service.PetService;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetErrorMessages;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetImageLoader;

public class PetDetailActivity extends AppCompatActivity {

    private final PetService petService = PetService.getInstance();

    private String petId;
    private Pet pet;

    private ImageView petImage;
    private TextView petName;
    private TextView petBreed;
    private TextView petAge;
    private TextView petDescription;
    private TextView petStatus;
    private Button btnAdopt;
    private Button btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pet_detail);

        petId = getIntent().getStringExtra("pet_id");
        if (petId == null) {
            Toast.makeText(this, R.string.pet_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        petImage = findViewById(R.id.pet_image);
        petName = findViewById(R.id.pet_name);
        petBreed = findViewById(R.id.pet_breed);
        petAge = findViewById(R.id.pet_age);
        petDescription = findViewById(R.id.pet_description);
        petStatus = findViewById(R.id.pet_status);
        btnAdopt = findViewById(R.id.btn_adopt);
        btnEdit = findViewById(R.id.btn_edit_pet);
        ImageButton btnBack = findViewById(R.id.btn_back);
        NestedScrollView contentScroll = findViewById(R.id.content_scroll);

        btnAdopt.setEnabled(false);

        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setShape(GradientDrawable.OVAL);
        circleBg.setColor(ContextCompat.getColor(this, R.color.white));
        btnBack.setBackground(circleBg);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(ContextCompat.getColor(this, R.color.background));
        cardBg.setCornerRadii(new float[]{
                dpToPx(28), dpToPx(28),
                dpToPx(28), dpToPx(28),
                0, 0,
                0, 0
        });
        contentScroll.setBackground(cardBg);

        btnBack.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPet();
    }

    private void loadPet() {
        petService.fetchById(petId, new PetService.Callback<Pet>() {
            @Override
            public void onSuccess(Pet loaded) {
                bindPet(loaded);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(PetDetailActivity.this,
                        PetErrorMessages.messageRes(e), Toast.LENGTH_LONG).show();
                if (pet == null) {
                    finish();
                }
            }
        });
    }

    private void bindPet(Pet loaded) {
        pet = loaded;

        petName.setText(pet.getName());
        petBreed.setText(pet.getBreed());
        petAge.setText(pet.getAge());
        petDescription.setText(pet.getDescription());

        if (pet.hasStoredImage()) {
            PetImageLoader.getInstance().load(
                    pet.getId(), pet.getImageVersion(), dpToPx(320), petImage);
        } else if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
            petImage.setImageResource(R.drawable.pet_placeholder);
            new ImageDownloadTask(petImage, dpToPx(320)).execute(pet.getImageUrl());
        } else if (pet.getImageUri() != null) {
            petImage.setImageURI(Uri.parse(pet.getImageUri()));
        } else if (pet.getImageResId() != 0) {
            petImage.setImageResource(pet.getImageResId());
        } else {
            petImage.setImageResource(R.drawable.pet_placeholder);
        }

        GradientDrawable statusBg = new GradientDrawable();
        statusBg.setCornerRadius(dpToPx(20));

        if (pet.isAdopted()) {
            petStatus.setText(R.string.status_adopted);
            petStatus.setTextColor(ContextCompat.getColor(this, R.color.text_disabled));
            statusBg.setColor(ContextCompat.getColor(this, R.color.surface_disabled));
            btnAdopt.setEnabled(false);
            btnAdopt.setAlpha(0.5f);
            btnAdopt.setText(R.string.btn_adopt);
        } else {
            petStatus.setText(R.string.status_available);
            petStatus.setTextColor(ContextCompat.getColor(this, R.color.success));
            statusBg.setColor(ContextCompat.getColor(this, R.color.success_container));
            btnAdopt.setEnabled(true);
            btnAdopt.setAlpha(1f);
            btnAdopt.setText(R.string.btn_adopt);
        }
        petStatus.setBackground(statusBg);

        btnAdopt.setOnClickListener(v -> adoptPet());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isOwner = user != null && user.getUid().equals(pet.getCreatedBy());
        if (isOwner && !pet.isAdopted()) {
            btnEdit.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(PetDetailActivity.this, EditPetActivity.class);
                intent.putExtra("pet_id", pet.getId());
                startActivity(intent);
            });
        } else {
            btnEdit.setVisibility(View.GONE);
        }
    }

    private void adoptPet() {
        if (pet == null || pet.isAdopted()) {
            return;
        }

        btnAdopt.setEnabled(false);
        btnAdopt.setText(R.string.adopt_in_progress);

        petService.adopt(pet.getId(), new PetService.Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pet.setAdopted(true);
                Intent intent = new Intent(PetDetailActivity.this, ConfirmationActivity.class);
                intent.putExtra("pet_id", pet.getId());
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Exception e) {
                btnAdopt.setEnabled(true);
                btnAdopt.setText(R.string.btn_adopt);
                Toast.makeText(PetDetailActivity.this,
                        PetErrorMessages.messageRes(e), Toast.LENGTH_LONG).show();
            }
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
