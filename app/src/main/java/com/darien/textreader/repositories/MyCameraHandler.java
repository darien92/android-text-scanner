package com.darien.textreader.repositories;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.view.TextureView;
import android.view.Window;

import androidx.camera.core.ImageProxy;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.nio.ByteBuffer;

public class MyCameraHandler {
    private ImageHandlerListener imageHandlerListener;

    public void setImageHandlerListener(ImageHandlerListener imageHandlerListener) {
        this.imageHandlerListener = imageHandlerListener;
    }

    public void takeImage(ImageProxy image, int rotationDegrees, ConstraintLayout scanArea, TextureView cameraView,
                          Activity activity){
        //Matrix para rotar la imagen en caso de que sea necesario
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        //obteniendo posicion de la zona a escanear
        int [] location = new int[2];
        scanArea.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        try {
            //obteniendo la imagen
            Bitmap imageTaken = imageToBitmap(image.getImage(), matrix, cameraView);
            if (imageTaken != null) {
                //calculando la altura de la barra de estado (esto es necesario para
                // descontarlo de la altura de la posicion de area de scan, en caso usar ActionBar,
                // tambien hay que descontar la altura del ActionBar)
                Rect rectangle = new Rect();
                Window window = activity.getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
                int statusBarHeight = rectangle.top;
                //calculando las proporciones de la imagen tomada respecto a la original
                float widthProportion = (float) imageTaken.getWidth() / (float) cameraView.getWidth();
                float heightProportion = (float) imageTaken.getHeight() / (float) cameraView.getHeight();
                int sectionX = (int) (x * widthProportion);
                int sectionY = (int) (y * heightProportion) - statusBarHeight;
                int sectionHeight = (int) (scanArea.getHeight() * heightProportion);
                int sectionWidth = (int) (scanArea.getWidth() * widthProportion);
                //obtniendo la seccion escaneada
                final Bitmap imageSection = Bitmap.createBitmap(imageTaken, sectionX, sectionY, sectionWidth, sectionHeight);
                //delegado en el Hilo principal para poder manejar las vistas
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    if (imageHandlerListener != null){
                        imageHandlerListener.onImageTook(imageSection);
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Bitmap imageToBitmap(Image image, Matrix matrix, TextureView cameraView){
        try {
            //obteniendo la imagen en un buffer
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            //guardando la imagen en un bimtmap
            Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
            //rotando la imagen en caso de que sea diferente su rotacion a la del movil
            Bitmap rotatedImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(),
                    bitmapImage.getHeight(), matrix, true);
            //reescalando la imagen para que tenga la misma escala que la vista de Camara en el Activity
            rotatedImage = Bitmap.createScaledBitmap(rotatedImage, cameraView.getMeasuredWidth(),
                    cameraView.getMeasuredHeight(), true);
            return rotatedImage;
        }catch (Exception e){
            return null;
        }
    }

    public interface ImageHandlerListener{
        void onImageTook(Bitmap imageSection);
    }
}
