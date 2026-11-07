package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.R;

public abstract class PetFormActivity extends AppCompatActivity {

    protected static final int REQUEST_PICK_IMAGE = 100;

    protected Uri selectedImageUri;

    public abstract void onCompressionStarted();

    public abstract void onImageReady(byte[] jpeg);

    public abstract void onImageError();

    protected void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            if (selectedImageUri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(
                            selectedImageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException ignored) {
                    // Not every provider supports persistable permissions.
                }
            }

            showPickedImage();
        }
    }

    protected void showPickedImage() {
        ImageView imageView = preparePhotoView();
        imageView.setImageURI(null);
        imageView.setImageURI(selectedImageUri);
    }

    protected ImageView preparePhotoView() {
        ImageView imageView = findViewById(R.id.iv_pet_photo);

        FrameLayout photoFrame = findViewById(R.id.photo_frame);
        photoFrame.setBackground(null);

        imageView.setBackground(null);
        imageView.setImageTintList(null);

        int size = dpToPx(140);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageDrawable(null);

        return imageView;
    }

    protected void stylePhotoFrame() {
        FrameLayout photoFrame = findViewById(R.id.photo_frame);
        GradientDrawable photoBg = new GradientDrawable();
        photoBg.setShape(GradientDrawable.OVAL);
        photoBg.setColor(Color.parseColor("#F0EEFF"));
        photoFrame.setBackground(photoBg);
    }

    protected void styleSpinner(Spinner spinner) {
        GradientDrawable spinnerBg = new GradientDrawable();
        spinnerBg.setColor(Color.WHITE);
        spinnerBg.setCornerRadius(dpToPx(14));
        spinnerBg.setStroke(dpToPx(1), Color.parseColor("#DDDDDD"));
        spinner.setBackground(spinnerBg);
    }

    protected int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View focused = getCurrentFocus();
            if (focused instanceof EditText) {
                Rect outRect = new Rect();
                focused.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    focused.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
