package com.darien.textreader.views;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.FlashMode;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.darien.textreader.R;
import com.darien.textreader.viewmodels.ScanViewModel;

public class ScanActivity extends AppCompatActivity {
    private static final int CAMERA_PRERMISSION_CODE = 0;
    private static final String FLASH_MODE_STATUS = "flashModeStatus";

    private TextureView cameraView;
    private LinearLayout photoBtnBorder;
    private ConstraintLayout scanArea;
    private Button btnScan, btnSavedTexts, btnFlash;
    private ProgressBar btnScanPB;
    private ImageCapture imageCapture;
    private ImageView imageFlash;

    private ScanViewModel mScanViewModel;

    private int flashModeStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initVariables();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Handler handler = new Handler();
        handler.postDelayed(()->{
            requestCameraPermission();
        }, 100);
    }

    private void initVariables() {
        btnScan = findViewById(R.id.btn_scan);
        cameraView = findViewById(R.id.camera_surface_view);
        btnSavedTexts = findViewById(R.id.btn_saved_texts);
        btnScanPB = findViewById(R.id.scan_progress_bar);
        scanArea = findViewById(R.id.scan_area);
        imageFlash = findViewById(R.id.flash_status_image);
        btnFlash = findViewById(R.id.flash_status_btn);
        photoBtnBorder = findViewById(R.id.rounded_button_border);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mScanViewModel = new ScanViewModel(getApplicationContext());
        flashModeStatus = preferences.getInt(ScanActivity.FLASH_MODE_STATUS, 0);
        btnFlash.setOnClickListener(view -> {
            flashModeStatus ++;
            setFlashModeStatus();
        });
        btnSavedTexts.setOnClickListener(view -> {
            Intent intent = new Intent(ScanActivity.this, SavedTextsActivity.class);
            startActivity(intent);
        });

        final Observer<String> textScannerObserver = (result) ->{
            if (result.length() == 0){
                //no text
                hideProgressBar();
                mScanViewModel.showInformativeAlert("Sorry, we could not scan any text", this);
            }else {
                //manage received text
                hideProgressBar();
                Intent intent = new Intent(this, ReadCurrentTextActivity.class);
                intent.putExtra("text", result);
                intent.putExtra("canSave", true);
                startActivity(intent);
            }
        };

        mScanViewModel.getTextScanned().observe(this, textScannerObserver);
    }

    private void setFlashModeStatus(){
        if (flashModeStatus > 2){
            flashModeStatus = 0;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(ScanActivity.FLASH_MODE_STATUS, flashModeStatus);
        editor.apply();
        if (flashModeStatus == 0) {//no flash
            imageFlash.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off_black_24dp));
            imageCapture.setFlashMode(FlashMode.OFF);
        }else if (flashModeStatus == 1) {// flash
            imageFlash.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on_black_24dp));
            imageCapture.setFlashMode(FlashMode.ON);
        }else if (flashModeStatus == 2){//auto
            imageFlash.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_auto_black_24dp));
            imageCapture.setFlashMode(FlashMode.AUTO);
        }
    }

    private void requestCameraPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        ScanActivity.CAMERA_PRERMISSION_CODE);
            } else {
                initCamera();
            }
        }else {
            initCamera();
        }
    }

    private void initCamera(){
        CameraX.unbindAll();
        //setting up Camera preview
        DisplayMetrics metrics = new DisplayMetrics();
        Rational aspectRatio = new Rational(metrics.widthPixels, metrics.heightPixels);
        Size screen = new Size(metrics.widthPixels, metrics.heightPixels);
        PreviewConfig pConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetResolution(screen)
                .build();
        Preview preview = new Preview(pConfig);
        preview.setOnPreviewOutputUpdateListener( output -> {
            ViewGroup parent = (ViewGroup) cameraView.getParent();
            parent.removeView(cameraView);
            parent.addView(cameraView);
            cameraView.setSurfaceTexture(output.getSurfaceTexture());
            updateTransform();
        });
        //Setting up image capture
        ImageCaptureConfig imgCaptureConfig = new ImageCaptureConfig.Builder()
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .setTargetResolution(new Size(cameraView.getMeasuredWidth(), cameraView.getMeasuredHeight()))
                .setTargetAspectRatio(new Rational(cameraView.getMeasuredWidth(), cameraView.getMeasuredHeight()))
                .build();
        imageCapture = new ImageCapture(imgCaptureConfig);
        setFlashModeStatus();
        btnScan.setOnClickListener(view -> {
            showProgressBar();
            imageCapture.takePicture(new ImageCapture.OnImageCapturedListener() {
                @Override
                public void onCaptureSuccess(ImageProxy image, int rotationDegrees) {
                    //Image taken
                    mScanViewModel.processImage(image, rotationDegrees, scanArea, cameraView, ScanActivity.this);
                    super.onCaptureSuccess(image, rotationDegrees);
                }

                @Override
                public void onError(ImageCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
                    //error taking image
                    super.onError(useCaseError, message, cause);
                }
            });
        });

        //starting camera
        CameraX.bindToLifecycle(this, preview, imageCapture);
        hideProgressBar();
    }

    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = cameraView.getMeasuredWidth();
        float h = cameraView.getMeasuredHeight();
        float cx = w / 2f;
        float cy = h / 2f;

        //getting screen rotation
        int rotationdegree;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        switch (rotation){
            case Surface.ROTATION_0:
                rotationdegree = 0;
                break;
            case Surface.ROTATION_90:
                rotationdegree = 270;
                break;
            case Surface.ROTATION_180:
                rotationdegree = 180;
                break;
            case Surface.ROTATION_270:
                rotationdegree = 90;
                break;
            default:
                return;
        }
        //rescaling image on landscape mode
        if (cameraView.getHeight() < cameraView.getWidth()){
            float bufferRatio = (float) cameraView.getBitmap().getHeight() / (float)cameraView.getBitmap().getWidth();
            int scaledHeight, scaledWidth;
            scaledHeight = cameraView.getWidth();
            scaledWidth = Math.round(cameraView.getWidth() * bufferRatio);
            float xScale = (float) scaledWidth / (float) cameraView.getWidth();
            float yScale = (float) scaledHeight / (float) cameraView.getHeight();
            mx.preScale(xScale, yScale, cx, cy);
        }
        //rotating image Matrix (bitmap)
        mx.postRotate((float) rotationdegree, cx, cy);
        cameraView.setTransform(mx);
    }

    private void showProgressBar(){
        btnScan.setVisibility(View.INVISIBLE);
        photoBtnBorder.setVisibility(View.INVISIBLE);
        btnScan.setClickable(false);
        btnScanPB.setVisibility(View.VISIBLE);
        btnScanPB.animate();
    }

    private void hideProgressBar(){
        btnScan.setVisibility(View.VISIBLE);
        photoBtnBorder.setVisibility(View.VISIBLE);
        btnScan.setClickable(true);
        btnScanPB.setVisibility(View.INVISIBLE);
        btnScanPB.clearAnimation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == ScanActivity.CAMERA_PRERMISSION_CODE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initCamera();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
