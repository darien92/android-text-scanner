package com.darien.textreader.repositories;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.widget.Toast;

import com.darien.textreader.R;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class MyTextHandler {
    private TextScannerListener textScannerListener;

    public void setTextScannerListener(TextScannerListener textScannerListener) {
        this.textScannerListener = textScannerListener;
    }

    public void handleImageTaken(Bitmap imageTaken, Context context){
        //initialize textRecognizer
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        if (!textRecognizer.isOperational()){
            Toast.makeText(context, context.getResources().getString(R.string.text_recognizer_error),
                    Toast.LENGTH_SHORT).show();
        }else if (textScannerListener != null){
            //getting image text
            Frame frame = new Frame.Builder().setBitmap(imageTaken).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            if (items.size() == 0){ //if there is no text, handle it
                textScannerListener.onErrorReceivingText(this);
            }
            else {
                //StringBuilder para eficiencia de memoria en el manejo de Strings
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    TextBlock myItem = items.valueAt(i);
                    sb.append(myItem.getValue());
                    sb.append("\n");
                }
                textScannerListener.onTexReceived(this, sb.toString());
            }
        }
    }

    public interface TextScannerListener{
        void onTexReceived(MyTextHandler textHandler, String texReceived);
        void onErrorReceivingText(MyTextHandler myTextHandler);
    }
}
