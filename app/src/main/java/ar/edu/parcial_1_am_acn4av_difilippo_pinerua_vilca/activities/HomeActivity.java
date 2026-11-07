package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.R;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.network.PetsApiClient;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks.FetchCatsTask;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks.FetchDogsTask;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.tasks.ImageDownloadTask;
import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util.CuratedImages;

/**
 * Public home. Only reachable when there is no active session: it shows the
 * random pet gallery and the entry points to login and signup.
 */
public class HomeActivity extends AppCompatActivity {

    private static final int IMAGES_PER_SPECIES = 6;
    private static final int GRID_SIZE = 6;

    private final PetsApiClient apiClient = new PetsApiClient();

    private FetchDogsTask dogsTask;
    private FetchCatsTask catsTask;

    private ImageView heroImage;
    private ImageView editorialImage;
    private ImageView dogsImage;
    private ImageView catsImage;
    private ImageView adoptedImage;
    private GridLayout grid;
    private ProgressBar progressBar;
    private NestedScrollView scrollView;

    private List<String> dogs;
    private List<String> cats;
    private int pendingRequests;
    private boolean hasRendered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        heroImage = findViewById(R.id.home_hero_image);
        editorialImage = findViewById(R.id.home_editorial_image);
        dogsImage = findViewById(R.id.home_dogs_image);
        catsImage = findViewById(R.id.home_cats_image);
        adoptedImage = findViewById(R.id.home_adopted_image);
        grid = findViewById(R.id.home_grid);
        progressBar = findViewById(R.id.home_progress);
        scrollView = findViewById(R.id.home_scroll);

        setupActions();

        loadContent();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        if (dogsTask != null) {
            dogsTask.cancel(true);
        }
        if (catsTask != null) {
            catsTask.cancel(true);
        }
        super.onDestroy();
    }

    private void setupActions() {
        findViewById(R.id.btn_home_login).setOnClickListener(v -> openLogin());
        findViewById(R.id.btn_home_login_top).setOnClickListener(v -> openLogin());
        findViewById(R.id.btn_home_signup).setOnClickListener(v -> openSignup());
        findViewById(R.id.btn_home_explore).setOnClickListener(v -> openLogin());

        findViewById(R.id.card_home_dogs).setOnClickListener(v -> openLogin());
        findViewById(R.id.card_home_cats).setOnClickListener(v -> openLogin());
        findViewById(R.id.card_home_adopted).setOnClickListener(v -> openLogin());
    }

    private void openLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void openSignup() {
        startActivity(new Intent(this, SignupActivity.class));
    }

    private void loadContent() {
        setLoading(true);
        pendingRequests = 2;
        dogs = null;
        cats = null;

        dogsTask = new FetchDogsTask(this, apiClient);
        dogsTask.execute(IMAGES_PER_SPECIES);

        catsTask = new FetchCatsTask(this, apiClient);
        catsTask.execute(IMAGES_PER_SPECIES);
    }

    public void onDogsLoaded(List<String> urls) {
        dogs = urls.isEmpty() ? CuratedImages.dogs() : urls;
        onPartLoaded();
    }

    public void onCatsLoaded(List<String> urls) {
        cats = urls.isEmpty() ? CuratedImages.cats() : urls;
        onPartLoaded();
    }

    private void onPartLoaded() {
        pendingRequests--;
        if (pendingRequests > 0) {
            return;
        }
        if (isFinishing() || isDestroyed()) {
            return;
        }
        renderContent();
        setLoading(false);
    }

    private void renderContent() {
        List<Photo> pool = buildPool();
        Collections.shuffle(pool);

        Photo hero = pool.remove(0);
        Photo editorial = pool.remove(0);
        Photo stripDog = takeFirst(pool, true);
        Photo stripCat = takeFirst(pool, false);
        Photo stripThird = pool.remove(0);

        if (stripDog == null) {
            stripDog = new Photo(CuratedImages.dogs().get(0), true);
        }
        if (stripCat == null) {
            stripCat = new Photo(CuratedImages.cats().get(0), false);
        }

        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        loadImage(heroImage, hero.url, screenWidth);
        loadImage(editorialImage, editorial.url, screenWidth - dpToPx(40));
        loadImage(dogsImage, stripDog.url, dpToPx(60));
        loadImage(catsImage, stripCat.url, dpToPx(60));
        loadImage(adoptedImage, stripThird.url, dpToPx(60));

        renderGrid(pool);

        if (!hasRendered) {
            hasRendered = true;
            scrollView.post(() -> scrollView.scrollTo(0, 0));
        }
    }

    private List<Photo> buildPool() {
        List<Photo> pool = new ArrayList<>();

        List<String> dogUrls = dogs != null ? dogs : CuratedImages.dogs();
        List<String> catUrls = cats != null ? cats : CuratedImages.cats();

        for (String url : dogUrls) {
            pool.add(new Photo(url, true));
        }
        for (String url : catUrls) {
            pool.add(new Photo(url, false));
        }

        List<String> fallbackDogs = CuratedImages.dogs();
        List<String> fallbackCats = CuratedImages.cats();
        int index = 0;
        while (pool.size() < IMAGES_PER_SPECIES * 2) {
            boolean dog = index % 2 == 0;
            String url = dog
                    ? fallbackDogs.get(index % fallbackDogs.size())
                    : fallbackCats.get(index % fallbackCats.size());
            pool.add(new Photo(url, dog));
            index++;
        }
        return pool;
    }

    private void renderGrid(List<Photo> photos) {
        grid.removeAllViews();

        int available = getResources().getDisplayMetrics().widthPixels - dpToPx(40);
        int gap = dpToPx(8);
        int cell = (available - 3 * gap) / 3;

        int count = Math.min(GRID_SIZE, photos.size());
        for (int i = 0; i < count; i++) {
            Photo photo = photos.get(i);

            MaterialCardView card = new MaterialCardView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cell;
            params.height = cell;
            params.setMargins(gap / 2, gap / 2, gap / 2, gap / 2);
            card.setLayoutParams(params);
            card.setRadius(dpToPx(14));
            card.setCardElevation(0);
            card.setCardBackgroundColor(Color.parseColor("#F0EEFF"));
            card.setClickable(true);
            card.setFocusable(true);

            ImageView image = new ImageView(this);
            image.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setImageResource(R.drawable.pet_placeholder);
            card.addView(image);

            card.setOnClickListener(v -> openLogin());
            grid.addView(card);

            loadImage(image, photo.url, cell);
        }
    }

    private void loadImage(ImageView imageView, String url, int targetWidthPx) {
        new ImageDownloadTask(imageView, targetWidthPx).execute(url);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private static Photo takeFirst(List<Photo> pool, boolean dog) {
        for (int i = 0; i < pool.size(); i++) {
            if (pool.get(i).isDog == dog) {
                return pool.remove(i);
            }
        }
        return null;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private static final class Photo {
        final String url;
        final boolean isDog;

        Photo(String url, boolean isDog) {
            this.url = url;
            this.isDog = isDog;
        }
    }
}
