package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.net.Uri;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.data.PetRepository;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;

public class AdoptionsActivity extends AppCompatActivity {

    private LinearLayout layoutPetContainer;
    private PetRepository petRepository;

    private static final int COLOR_ACCENT = Color.parseColor("#7C5CFC");
    private static final int COLOR_INACTIVE_BG = Color.parseColor("#F0EEFF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adoptions);

        petRepository = PetRepository.getInstance();
        layoutPetContainer = findViewById(R.id.layout_pet_container);

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
        renderAdoptedPets();
    }

    private void setupBottomNav() {
        MaterialButton navPets = findViewById(R.id.nav_btn_pets);
        MaterialButton navAdoptions = findViewById(R.id.nav_btn_adoptions);

        navAdoptions.setBackgroundTintList(android.content.res.ColorStateList.valueOf(COLOR_ACCENT));
        navAdoptions.setTextColor(Color.WHITE);

        navPets.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        navPets.setTextColor(Color.DKGRAY);

        navPets.setOnClickListener(v -> {
            Intent intent = new Intent(AdoptionsActivity.this, PetListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }

    private void renderAdoptedPets() {
        layoutPetContainer.removeAllViews();

        List<Pet> allPets = petRepository.getAllPets();
        List<Pet> adoptedPets = new ArrayList<>();
        for (Pet p : allPets) {
            if (p.isAdopted()) {
                adoptedPets.add(p);
            }
        }

        if (adoptedPets.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Aún no tienes mascotas adoptadas.");
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, dpToPx(32), 0, 0);
            emptyText.setTextSize(16f);
            emptyText.setTextColor(Color.DKGRAY);
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
            card.setStrokeColor(Color.parseColor("#EFEFEF"));
            card.setCardBackgroundColor(Color.WHITE);
            card.setRippleColor(android.content.res.ColorStateList.valueOf(COLOR_INACTIVE_BG));
            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.HORIZONTAL);
            content.setGravity(Gravity.CENTER_VERTICAL);
            content.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));

            ImageView image = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dpToPx(76), dpToPx(76));
            image.setLayoutParams(imgParams);
            if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
                new ImageDownloadTask(image).execute(pet.getImageUrl());

            } else if (pet.getImageUri() != null) {
                image.setImageURI(Uri.parse(pet.getImageUri()));

            } else {
                image.setImageResource(pet.getImageResId());
            }
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);

            GradientDrawable imgBg = new GradientDrawable();
            imgBg.setCornerRadius(dpToPx(38)); // circular
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
            nameText.setTextColor(Color.parseColor("#1A1A1A"));
            textContainer.addView(nameText);

            TextView breedText = new TextView(this);
            breedText.setText(pet.getBreed());
            breedText.setTextSize(13f);
            breedText.setTextColor(Color.parseColor("#9A9A9A"));
            LinearLayout.LayoutParams breedParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            breedParams.topMargin = dpToPx(2);
            breedText.setLayoutParams(breedParams);
            textContainer.addView(breedText);

            TextView statusText = new TextView(this);
            statusText.setText(getString(R.string.status_adopted));
            statusText.setTextSize(11f);
            statusText.setTypeface(null, Typeface.BOLD);
            statusText.setTextColor(Color.parseColor("#B0B0B0"));
            statusText.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));

            GradientDrawable statusBg = new GradientDrawable();
            statusBg.setCornerRadius(dpToPx(20));
            statusBg.setColor(Color.parseColor("#F2F2F2"));
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
}
