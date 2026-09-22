package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.net.Uri;
import android.content.Intent;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.constants.PetType;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.data.PetRepository;

public class AddPetActivity extends AppCompatActivity {
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_pet);

        Button btnBack = findViewById(R.id.btn_back);
        Button btnSave = findViewById(R.id.btn_save_pet);
        ImageView ivPetPhoto = findViewById(R.id.iv_pet_photo);
        FrameLayout photoFrame = findViewById(R.id.photo_frame);
        Button btnSelectPhoto = findViewById(R.id.btn_select_photo);
        TextInputEditText etName = findViewById(R.id.et_name);
        TextInputEditText etBreed = findViewById(R.id.et_breed);
        TextInputEditText etAge = findViewById(R.id.et_age);
        TextInputEditText etDescription = findViewById(R.id.et_description);
        Spinner spinnerType = findViewById(R.id.spinner_type);

        GradientDrawable photoBg = new GradientDrawable();
        photoBg.setShape(GradientDrawable.OVAL);
        photoBg.setColor(Color.parseColor("#F0EEFF"));
        photoFrame.setBackground(photoBg);

        GradientDrawable spinnerBg = new GradientDrawable();
        spinnerBg.setColor(Color.WHITE);
        spinnerBg.setCornerRadius(dpToPx(14));
        spinnerBg.setStroke(dpToPx(1), Color.parseColor("#DDDDDD"));
        spinnerType.setBackground(spinnerBg);

        List<String> types = new ArrayList<>();
        for (PetType pt : PetType.values()) {
            types.add(pt.type);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        // Tocar el botón de texto O la foto/círculo abre el selector
        btnSelectPhoto.setOnClickListener(v -> openImagePicker());
        photoFrame.setOnClickListener(v -> openImagePicker());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String breed = etBreed.getText() != null ? etBreed.getText().toString().trim() : "";
            String age = etAge.getText() != null ? etAge.getText().toString().trim() : "";
            String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String type = spinnerType.getSelectedItem().toString();

            if (name.isEmpty() || breed.isEmpty() || age.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            PetRepository.getInstance().addPet(
                    name,
                    breed,
                    age,
                    type,
                    description,
                    selectedImageUri != null ? selectedImageUri.toString() : null
            );
            Toast.makeText(this, "¡Mascota agregada con éxito!", Toast.LENGTH_SHORT).show();
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            if (selectedImageUri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(
                            selectedImageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException e) {
                    // El proveedor de imágenes no permite guardar el permiso.
                }
            }

            ImageView ivPetPhoto = findViewById(R.id.iv_pet_photo);
            FrameLayout photoFrame = findViewById(R.id.photo_frame);

            photoFrame.setBackground(null);
            ivPetPhoto.setBackground(null);
            ivPetPhoto.setImageTintList(null);

            int sizeInPx = dpToPx(140);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizeInPx, sizeInPx);
            ivPetPhoto.setLayoutParams(params);
            ivPetPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);

            ivPetPhoto.setImageURI(null);
            ivPetPhoto.setImageURI(selectedImageUri);
        }
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (ev.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            android.view.View v = getCurrentFocus();
            if (v instanceof android.widget.EditText) {
                android.graphics.Rect outRect = new android.graphics.Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}