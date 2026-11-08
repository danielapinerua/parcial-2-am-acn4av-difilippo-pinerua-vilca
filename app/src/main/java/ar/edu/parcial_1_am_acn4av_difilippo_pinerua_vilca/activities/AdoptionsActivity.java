package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks.ImageDownloadTask;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.R;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.service.PetService;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetErrorMessages;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetImageLoader;

public class AdoptionsActivity extends AppCompatActivity {

    private final PetService petService = PetService.getInstance();

    private LinearLayout layoutPetContainer;
    private FirebaseAuth mAuth;
    private int colorAccent;
    private int colorInactiveBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adoptions);

        layoutPetContainer = findViewById(R.id.layout_pet_container);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser usuario = mAuth.getCurrentUser();
        TextView tvUserEmail = findViewById(R.id.tv_user_email);
        if (usuario != null) {
            tvUserEmail.setText(usuario.getEmail());
        }

        MaterialButton btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> cerrarSesion());

        colorAccent = ContextCompat.getColor(this, R.color.accent);
        colorInactiveBg = ContextCompat.getColor(this, R.color.surface_variant);

        setupBottomNav();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdoptions();
    }

    private void setupBottomNav() {
        MaterialButton navPets = findViewById(R.id.nav_btn_pets);
        MaterialButton navAdoptions = findViewById(R.id.nav_btn_adoptions);

        navAdoptions.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorAccent));
        navAdoptions.setTextColor(ContextCompat.getColor(this, R.color.white));

        navPets.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        navPets.setTextColor(ContextCompat.getColor(this, R.color.nav_inactive));

        navPets.setOnClickListener(v -> {
            Intent intent = new Intent(AdoptionsActivity.this, PetListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }

    private void loadAdoptions() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        petService.fetchAdoptions(new PetService.Callback<List<Pet>>() {
            @Override
            public void onSuccess(List<Pet> pets) {
                renderAdoptedPets(pets);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdoptionsActivity.this,
                        PetErrorMessages.messageRes(e), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderAdoptedPets(List<Pet> adoptedPets) {
        layoutPetContainer.removeAllViews();

        if (adoptedPets.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText(R.string.empty_adoptions);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, dpToPx(32), 0, 0);
            emptyText.setTextSize(16f);
            emptyText.setTextColor(ContextCompat.getColor(this, R.color.nav_inactive));
            layoutPetContainer.addView(emptyText);
            return;
        }

        for (Pet pet : adoptedPets) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, dpToPx(14));
            card.setLayoutParams(cardParams);
            card.setRadius(dpToPx(18));
            card.setCardElevation(dpToPx(1));
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(ContextCompat.getColor(this, R.color.outline));
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.surface));
            card.setRippleColor(android.content.res.ColorStateList.valueOf(colorInactiveBg));
            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.HORIZONTAL);
            content.setGravity(Gravity.CENTER_VERTICAL);
            content.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));

            ImageView image = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dpToPx(76), dpToPx(76));
            image.setLayoutParams(imgParams);
            if (pet.hasStoredImage()) {
                PetImageLoader.getInstance().load(
                        pet.getId(), pet.getImageVersion(), dpToPx(76), image);
            } else if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
                image.setImageResource(R.drawable.pet_placeholder);
                new ImageDownloadTask(image, dpToPx(76)).execute(pet.getImageUrl());
            } else if (pet.getImageUri() != null) {
                image.setImageURI(Uri.parse(pet.getImageUri()));
            } else if (pet.getImageResId() != 0) {
                image.setImageResource(pet.getImageResId());
            } else {
                image.setImageResource(R.drawable.pet_placeholder);
            }
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);

            GradientDrawable imgBg = new GradientDrawable();
            imgBg.setCornerRadius(dpToPx(38));
            image.setClipToOutline(true);
            image.setBackground(imgBg);
            content.addView(image);

            LinearLayout textContainer = new LinearLayout(this);
            textContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            );
            textContainerParams.setMargins(dpToPx(16), 0, 0, 0);
            textContainer.setLayoutParams(textContainerParams);
            textContainer.setGravity(Gravity.CENTER_VERTICAL);

            TextView nameText = new TextView(this);
            nameText.setText(pet.getName());
            nameText.setTextSize(17f);
            nameText.setTypeface(null, Typeface.BOLD);
            nameText.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            textContainer.addView(nameText);

            TextView breedText = new TextView(this);
            breedText.setText(pet.getBreed());
            breedText.setTextSize(13f);
            breedText.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
            LinearLayout.LayoutParams breedParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            breedParams.topMargin = dpToPx(2);
            breedText.setLayoutParams(breedParams);
            textContainer.addView(breedText);

            TextView statusText = new TextView(this);
            statusText.setText(getString(R.string.status_adopted));
            statusText.setTextSize(11f);
            statusText.setTypeface(null, Typeface.BOLD);
            statusText.setTextColor(ContextCompat.getColor(this, R.color.text_disabled));
            statusText.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));

            GradientDrawable statusBg = new GradientDrawable();
            statusBg.setCornerRadius(dpToPx(20));
            statusBg.setColor(ContextCompat.getColor(this, R.color.surface_disabled));
            statusText.setBackground(statusBg);

            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            statusParams.topMargin = dpToPx(6);
            statusText.setLayoutParams(statusParams);
            textContainer.addView(statusText);

            content.addView(textContainer);
            card.addView(content);

            card.setOnClickListener(v -> {
                Intent intent = new Intent(AdoptionsActivity.this, PetDetailActivity.class);
                intent.putExtra("pet_id", pet.getId());
                startActivity(intent);
            });

            layoutPetContainer.addView(card);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void cerrarSesion() {
        mAuth.signOut();
        PetService.getInstance().clear();
        PetImageLoader.getInstance().clear();

        Intent intent = new Intent(AdoptionsActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
