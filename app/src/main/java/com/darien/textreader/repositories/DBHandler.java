package com.darien.textreader.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.darien.textreader.models.TextSaved;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "TextsScanned.sql";
    private static final int DATABASE_VERSION = 1;

    static abstract class PrevQuerys implements BaseColumns {
        static final String TABLE_NAME = "texts";
        static final String TEXT = "text"; //string
        static final String DATE = "date";
    }

    public DBHandler(Context ctx) {
        super(ctx, DB_NAME, null, DATABASE_VERSION);
    }

    //crear la tabla
    private static final String SQL_CREATE_NUMBERS_TABLE =
            "CREATE TABLE " + PrevQuerys.TABLE_NAME + " (" +
                    PrevQuerys._ID + " INTEGER PRIMARY KEY," +
                    PrevQuerys.TEXT + " TEXT" + "," +
                    PrevQuerys.DATE + " TEXT" +
                    " )";

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NUMBERS_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void insertText(String textToInsert) {
        // no repetir numeros
        List<TextSaved> texts = getTexts();
        for (TextSaved text: texts
             ) {
            if (text.getText().equals(textToInsert)){
                return;
            }
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PrevQuerys.TEXT, textToInsert);
        values.put(PrevQuerys.DATE, getDateAsString());
        db.insert(PrevQuerys.TABLE_NAME, null, values);
    }

    public void deleteText(String text){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(PrevQuerys.TABLE_NAME, PrevQuerys.TEXT + "=?", new String[]{text});
    }

    public ArrayList<TextSaved> getTexts() {
        ArrayList<TextSaved> texts = new ArrayList<>();

        String[] projection = {
                PrevQuerys.TEXT,
                PrevQuerys.DATE
        };
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(
                PrevQuerys.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            texts.add(new TextSaved(c.getString(0), c.getString(1)));
            c.moveToNext();
        }
        c.close();
        return texts;
    }

    public ArrayList<TextSaved> findText(String queries) {
        ArrayList<TextSaved> results = new ArrayList<>();
        try {
            String[] projection = {
                    PrevQuerys.TEXT,
                    PrevQuerys.DATE
            };
            SQLiteDatabase db = getReadableDatabase();

            Cursor c = db.query(true, PrevQuerys.TABLE_NAME, projection, PrevQuerys.TEXT + " LIKE ?",
                    new String[] {"%"+ queries+ "%" }, null, null, null,
                    null);
            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {
                results.add(new TextSaved(c.getString(0), c.getString(1)));
                c.moveToNext();
            }
            c.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return results;
    }

    private String getDateAsString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }
}
