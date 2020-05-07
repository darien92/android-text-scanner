package com.darien.textreader.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.darien.textreader.models.TextSaved;
import com.darien.textreader.repositories.DBHandler;

import java.util.ArrayList;

public class SavedTextsViewModel extends ViewModel {
    private DBHandler dbHandler;
    private MutableLiveData<ArrayList<TextSaved>> texts;

    public LiveData<ArrayList<TextSaved>> getTexts() {
        return texts;
    }

    public SavedTextsViewModel(Context context) {
        dbHandler = new DBHandler(context);
        texts = new MutableLiveData<>();
    }

    public void searchText(String query){
        if (texts.getValue() == null) {
            texts.setValue(dbHandler.findText(query));
        }else{
            texts.getValue().addAll(dbHandler.findText(query));
        }
    }

    public void getAllTexts(){
        if (texts.getValue() == null) {
            texts.setValue(dbHandler.getTexts());
        }else{
            texts.getValue().addAll(dbHandler.getTexts());
        }
    }

    public void deleteText(String text){
        dbHandler.deleteText(text);
        if (texts.getValue() == null) {
            texts.setValue(dbHandler.getTexts());
        }else{
            texts.getValue().addAll(dbHandler.getTexts());
        }
    }
}
