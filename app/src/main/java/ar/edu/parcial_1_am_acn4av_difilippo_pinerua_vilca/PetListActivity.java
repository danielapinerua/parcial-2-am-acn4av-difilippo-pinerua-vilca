package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca;

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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.constants.PetType;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.data.PetRepository;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;

public class PetListActivity extends AppCompatActivity {

    private LinearLayout layoutPetContainer;
    private PetRepository petRepository;

    private MaterialButton btnAll, btnDogs, btnCats;
    private static final int COLOR_ACCENT = Color.parseColor("#7C5CFC");
    private static final int COLOR_INACTIVE_BG = Color.parseColor("#F0EEFF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pet_list);

        petRepository = PetRepository.getInstance();
        layoutPetContainer = findViewById(R.id.layout_pet_container);

        setupFilters();
        selectFilter(btnAll);
        setupBottomNav();
        renderPets(petRepository.getAllPets());

        FloatingActionButton fabAddPet = findViewById(R.id.fab_add_pet);

        fabAddPet.setOnClickListener(v -> {
            startActivity(new Intent(PetListActivity.this, AddPetActivity.class));
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (btnAll != null) {
            btnAll.performClick();
        }
    }

    private void setupBottomNav() {
        MaterialButton navPets = findViewById(R.id.nav_btn_pets);
        MaterialButton navAdoptions = findViewById(R.id.nav_btn_adoptions);

        navPets.setBackgroundTintList(android.content.res.ColorStateList.valueOf(COLOR_ACCENT));
        navPets.setTextColor(Color.WHITE);

        navAdoptions.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        navAdoptions.setTextColor(Color.DKGRAY);

        navAdoptions.setOnClickListener(v -> {
            Intent intent = new Intent(PetListActivity.this, AdoptionsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }

    private void setupFilters() {
        btnAll = findViewById(R.id.btn_filter_all);
        btnDogs = findViewById(R.id.btn_filter_dogs);
        btnCats = findViewById(R.id.btn_filter_cats);

        btnAll.setOnClickListener(v -> {
            selectFilter(btnAll);
            renderPets(petRepository.getAllPets());
        });
        btnDogs.setOnClickListener(v -> {
            selectFilter(btnDogs);
            renderPets(petRepository.getPetsByType(PetType.DOG.type));
        });
        btnCats.setOnClickListener(v -> {
            selectFilter(btnCats);
            renderPets(petRepository.getPetsByType(PetType.CAT.type));
        });
    }

    private void selectFilter(MaterialButton selected) {
        for (MaterialButton btn : new MaterialButton[]{btnAll, btnDogs, btnCats}) {
            boolean isSelected = btn == selected;
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    isSelected ? COLOR_ACCENT : COLOR_INACTIVE_BG));
            btn.setTextColor(isSelected ? Color.WHITE : COLOR_ACCENT);
        }
    }

    private void renderPets(List<Pet> pets) {
        layoutPetContainer.removeAllViews();

        for (Pet pet : pets) {

            if (pet.isAdopted()) {
                continue;
            }

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
            statusText.setText(pet.isAdopted() ? getString(R.string.status_adopted) : getString(R.string.status_available));
            statusText.setTextSize(11f);
            statusText.setTypeface(null, Typeface.BOLD);
            statusText.setTextColor(pet.isAdopted() ? Color.parseColor("#B0B0B0") : Color.parseColor("#388E3C"));
            statusText.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));

            GradientDrawable statusBg = new GradientDrawable();
            statusBg.setCornerRadius(dpToPx(20));
            statusBg.setColor(pet.isAdopted() ? Color.parseColor("#F2F2F2") : Color.parseColor("#E7F5EA"));
            statusText.setBackground(statusBg);

            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            statusParams.topMargin = dpToPx(6);
            statusText.setLayoutParams(statusParams);
            textContainer.addView(statusText);

            content.addView(textContainer);
            card.addView(content);

            card.setOnClickListener(v -> {
                Intent intent = new Intent(PetListActivity.this, PetDetailActivity.class);
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