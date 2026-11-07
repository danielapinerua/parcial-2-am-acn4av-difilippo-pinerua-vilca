package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.R;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks.SplashTask;

public class MainActivity extends AppCompatActivity {

    private boolean keepPulsing = true;
    private SplashTask splashTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ImageView logo = findViewById(R.id.logo);
        View appName = findViewById(R.id.app_name);
        View appDescription = findViewById(R.id.app_description);

        logo.setAlpha(0f);
        logo.setTranslationY(-40f);
        logo.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(100)
                .withEndAction(() -> pulseLogo(logo)).start();

        appName.setAlpha(0f);
        appName.setTranslationY(30f);
        appName.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(300).start();

        appDescription.setAlpha(0f);
        appDescription.setTranslationY(30f);
        appDescription.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(400).start();

        splashTask = new SplashTask(this, 2000);
        splashTask.execute();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onSplashFinished() {
        keepPulsing = false;

        Intent intent;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            intent = new Intent(MainActivity.this, PetListActivity.class);
        } else {
            intent = new Intent(MainActivity.this, HomeActivity.class);
        }

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (splashTask != null) {
            splashTask.cancel(true);
        }
        super.onDestroy();
    }

    private void pulseLogo(ImageView logo) {
        if (!keepPulsing) return;
        logo.animate()
                .scaleX(1.08f).scaleY(1.08f)
                .setDuration(600)
                .withEndAction(() -> {
                    if (!keepPulsing) return;
                    logo.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(600)
                            .withEndAction(() -> pulseLogo(logo))
                            .start();
                }).start();
    }
}