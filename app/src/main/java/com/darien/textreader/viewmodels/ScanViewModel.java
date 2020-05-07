package com.darien.textreader.viewmodels;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.TextureView;

import androidx.camera.core.ImageProxy;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.darien.textreader.R;
import com.darien.textreader.repositories.MyCameraHandler;
import com.darien.textreader.repositories.MyTextHandler;

public class ScanViewModel extends ViewModel {
    private MutableLiveData<String> textScanned;
    private MyCameraHandler mCameraHandler;
    private MyTextHandler mTextHandler;

    public LiveData<String> getTextScanned (){
        return textScanned;
    }

    public ScanViewModel(Context context) {
        this.mCameraHandler = new MyCameraHandler();
        this.mTextHandler = new MyTextHandler();
        this.textScanned = new MutableLiveData<>();

        mCameraHandler.setImageHandlerListener(imageSection -> {
            mTextHandler.handleImageTaken(imageSection, context);
        });

        mTextHandler.setTextScannerListener(new MyTextHandler.TextScannerListener() {
            @Override
            public void onTexReceived(MyTextHandler textHandler, String texReceived) {
                textScanned.postValue(texReceived);
            }

            @Override
            public void onErrorReceivingText(MyTextHandler myTextHandler) {
                textScanned.postValue(""); //void text to indicate that none text was found
            }
        });
    }

    public void processImage(ImageProxy image, int rotationDegrees, ConstraintLayout scanArea, TextureView cameraView, Activity activity){
        mCameraHandler.takeImage(image, rotationDegrees, scanArea, cameraView, activity);
    }

    public void showInformativeAlert(String body, Context context){
        AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(body)
                .setTitle("Alert")
                .setCancelable(true)
                .setPositiveButton(context.getResources().getString(R.string.text_accept),
                        (dialog, id) -> {
                            dialog.dismiss();
                });
        try {
            alert = builder.create();
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
