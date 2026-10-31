package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.data.PetRepository;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.models.Pet;

public class PetDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pet_detail);

        Intent intent = getIntent();
        int petId = intent.getIntExtra("pet_id", -1);

        PetRepository petRepository = PetRepository.getInstance();
        Pet pet = petRepository.getPetById(petId);

        ImageView petImage = findViewById(R.id.pet_image);
        TextView petName = findViewById(R.id.pet_name);
        TextView petBreed = findViewById(R.id.pet_breed);
        TextView petAge = findViewById(R.id.pet_age);
        TextView petDescription = findViewById(R.id.pet_description);
        TextView petStatus = findViewById(R.id.pet_status);
        Button btnAdopt = findViewById(R.id.btn_adopt);
        ImageButton btnBack = findViewById(R.id.btn_back);
        NestedScrollView contentScroll = findViewById(R.id.content_scroll);

        // Círculo blanco de fondo para el botón Volver
        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setShape(GradientDrawable.OVAL);
        circleBg.setColor(Color.WHITE);
        btnBack.setBackground(circleBg);

        // Tarjeta con esquinas redondeadas arriba, superpuesta a la imagen
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#FDFCFB"));
        cardBg.setCornerRadii(new float[]{
                dpToPx(28), dpToPx(28),
                dpToPx(28), dpToPx(28),
                0, 0,
                0, 0
        });
        contentScroll.setBackground(cardBg);

        if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
            new ImageDownloadTask(petImage).execute(pet.getImageUrl());

        } else if (pet.getImageUri() != null) {
            petImage.setImageURI(Uri.parse(pet.getImageUri()));

        } else {
            petImage.setImageResource(pet.getImageResId());
        }
        petName.setText(pet.getName());
        petBreed.setText(pet.getBreed());
        petAge.setText(pet.getAge());
        petDescription.setText(pet.getDescription());

        GradientDrawable statusBg = new GradientDrawable();
        statusBg.setCornerRadius(dpToPx(20));

        if (pet.isAdopted()) {
            petStatus.setText(R.string.status_adopted);
            petStatus.setTextColor(Color.parseColor("#B0B0B0"));
            statusBg.setColor(Color.parseColor("#F2F2F2"));
            btnAdopt.setEnabled(false);
            btnAdopt.setAlpha(0.5f);
        } else {
            petStatus.setText(R.string.status_available);
            petStatus.setTextColor(Color.parseColor("#388E3C"));
            statusBg.setColor(Color.parseColor("#E7F5EA"));
        }
        petStatus.setBackground(statusBg);

        btnAdopt.setOnClickListener(v -> {
            petRepository.adoptPet(petId);
            Intent confirmIntent = new Intent(PetDetailActivity.this, ConfirmationActivity.class);
            confirmIntent.putExtra("pet_id", petId);
            startActivity(confirmIntent);
        });

        btnBack.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}