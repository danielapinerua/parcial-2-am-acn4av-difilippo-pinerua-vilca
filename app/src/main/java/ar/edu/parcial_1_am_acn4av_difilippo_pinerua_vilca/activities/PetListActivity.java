package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks.ImageDownloadTask;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.R;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.constants.PetType;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.service.PetService;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetErrorMessages;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.PetImageLoader;

public class PetListActivity extends AppCompatActivity {

    private static final String FILTER_ALL = "All";

    private FirebaseAuth mAuth;
    private PetService petService;

    private LinearLayout layoutPetContainer;
    private ProgressBar progressBar;
    private View errorContainer;
    private TextView errorMessage;
    private TextView emptyView;
    private View listScroll;
    private MaterialButton retryButton;

    private MaterialButton btnAll, btnDogs, btnCats;
    private String selectedType = FILTER_ALL;

    private int colorAccent;
    private int colorInactiveBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pet_list);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser usuario = mAuth.getCurrentUser();
        TextView tvUserEmail = findViewById(R.id.tv_user_email);
        if (usuario != null) {
            tvUserEmail.setText(usuario.getEmail());
        }

        MaterialButton btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> cerrarSesion());

        petService = PetService.getInstance();

        layoutPetContainer = findViewById(R.id.layout_pet_container);
        progressBar = findViewById(R.id.progress_bar);
        errorContainer = findViewById(R.id.error_container);
        errorMessage = findViewById(R.id.error_message);
        emptyView = findViewById(R.id.empty_view);
        listScroll = findViewById(R.id.list_scroll);
        retryButton = findViewById(R.id.retry_button);

        colorAccent = ContextCompat.getColor(this, R.color.accent);
        colorInactiveBg = ContextCompat.getColor(this, R.color.surface_variant);

        setupFilters();
        selectFilter(btnAll);
        setupBottomNav();

        retryButton.setOnClickListener(v -> loadPets(true));

        FloatingActionButton fabAddPet = findViewById(R.id.fab_add_pet);
        fabAddPet.setOnClickListener(v ->
                startActivity(new Intent(PetListActivity.this, AddPetActivity.class)));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPets(!petService.isLoaded());
    }

    private void setupBottomNav() {
        MaterialButton navPets = findViewById(R.id.nav_btn_pets);
        MaterialButton navAdoptions = findViewById(R.id.nav_btn_adoptions);

        navPets.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorAccent));
        navPets.setTextColor(ContextCompat.getColor(this, R.color.white));

        navAdoptions.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        navAdoptions.setTextColor(ContextCompat.getColor(this, R.color.nav_inactive));

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
            selectedType = FILTER_ALL;
            selectFilter(btnAll);
            renderCurrentFilter();
        });
        btnDogs.setOnClickListener(v -> {
            selectedType = PetType.DOG.type;
            selectFilter(btnDogs);
            renderCurrentFilter();
        });
        btnCats.setOnClickListener(v -> {
            selectedType = PetType.CAT.type;
            selectFilter(btnCats);
            renderCurrentFilter();
        });
    }

    private void selectFilter(MaterialButton selected) {
        for (MaterialButton btn : new MaterialButton[]{btnAll, btnDogs, btnCats}) {
            boolean isSelected = btn == selected;
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    isSelected ? colorAccent : colorInactiveBg));
            btn.setTextColor(isSelected
                    ? ContextCompat.getColor(this, R.color.white)
                    : colorAccent);
        }
    }

    private void loadPets(boolean withSpinner) {
        if (withSpinner) {
            showLoading();
        }

        petService.fetchAll(new PetService.Callback<List<Pet>>() {
            @Override
            public void onSuccess(List<Pet> pets) {
                renderCurrentFilter();
            }

            @Override
            public void onError(Exception e) {
                int messageRes = PetErrorMessages.messageRes(e);
                if (petService.isLoaded()) {
                    Toast.makeText(PetListActivity.this, messageRes, Toast.LENGTH_SHORT).show();
                } else {
                    showError(messageRes);
                }
            }
        });
    }

    private void renderCurrentFilter() {
        renderPets(petService.getPetsByType(selectedType));
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        errorContainer.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        listScroll.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        listScroll.setVisibility(View.VISIBLE);
    }

    private void showError(int messageRes) {
        progressBar.setVisibility(View.GONE);
        listScroll.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        errorContainer.setVisibility(View.VISIBLE);
        errorMessage.setText(messageRes);
    }

    private void showEmpty() {
        progressBar.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        listScroll.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    private void renderPets(List<Pet> pets) {
        layoutPetContainer.removeAllViews();
        int rendered = 0;

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
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);

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
            statusText.setText(pet.isAdopted() ? getString(R.string.status_adopted) : getString(R.string.status_available));
            statusText.setTextSize(11f);
            statusText.setTypeface(null, Typeface.BOLD);
            statusText.setTextColor(ContextCompat.getColor(this,
                    pet.isAdopted() ? R.color.text_disabled : R.color.success));
            statusText.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));

            GradientDrawable statusBg = new GradientDrawable();
            statusBg.setCornerRadius(dpToPx(20));
            statusBg.setColor(ContextCompat.getColor(this,
                    pet.isAdopted() ? R.color.surface_disabled : R.color.success_container));
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
            rendered++;
        }

        if (rendered == 0) {
            showEmpty();
        } else {
            showContent();
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void cerrarSesion() {
        mAuth.signOut();
        petService.clear();
        PetImageLoader.getInstance().clear();

        Intent intent = new Intent(PetListActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
