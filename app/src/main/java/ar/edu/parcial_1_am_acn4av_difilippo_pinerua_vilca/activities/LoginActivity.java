package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.R;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvGoSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);

        tvGoSignup = findViewById(R.id.tv_go_signup);

        tvGoSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_back_home).setOnClickListener(v -> volverAlInicio());

        btnLogin.setOnClickListener(v -> iniciarSesion());
    }

    private void volverAlInicio() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void iniciarSesion() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, PetListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
                    }
                });
    }
}