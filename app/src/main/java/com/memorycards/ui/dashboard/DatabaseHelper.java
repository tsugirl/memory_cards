package com.memorycards.ui.dashboard;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "memorycards.db";
    private static final int DATABASE_VERSION = 2;
    public static final String COLUMN_LAST_SELECTED_BUTTON = "last_selected_button";
    private static final String TABLE_FOLDERS = "folders";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String TABLE_CARDS = "cards";
    private static final String COLUMN_FOLDER_NAME = "folder_name";
    public static final String COLUMN_FRONT = "front";
    public static final String COLUMN_BACK = "back";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Cursor loadFrontCards(String folderName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT rowid as _id, " + COLUMN_FRONT + ", " + COLUMN_BACK + " FROM " + TABLE_CARDS + " WHERE " + COLUMN_FOLDER_NAME + "=?";
        return db.rawQuery(query, new String[]{folderName});
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FOLDERS_TABLE = "CREATE TABLE " + TABLE_FOLDERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_LAST_SELECTED_BUTTON + " TEXT)";
        db.execSQL(CREATE_FOLDERS_TABLE);

        String CREATE_CARDS_TABLE = "CREATE TABLE " + TABLE_CARDS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FOLDER_NAME + " TEXT,"
                + COLUMN_FRONT + " TEXT,"
                + COLUMN_BACK + " TEXT)";
        db.execSQL(CREATE_CARDS_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String CREATE_CARDS_TABLE = "CREATE TABLE " + TABLE_CARDS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_FOLDER_NAME + " TEXT,"
                    + COLUMN_FRONT + " TEXT,"
                    + COLUMN_BACK + " TEXT)";
            db.execSQL(CREATE_CARDS_TABLE);
        }
    }

    public String getLastSelectedFolder() {
        SQLiteDatabase db = this.getReadableDatabase();
        String folderName = null;
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_LAST_SELECTED_BUTTON + " FROM " + TABLE_FOLDERS, null);
        if (cursor.moveToLast()) {
            folderName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_SELECTED_BUTTON));
        }
        cursor.close();
        db.close();
        return folderName;
    }

    public void saveFolders(List<String> folders, String lastSelectedButton) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_FOLDERS, null, null);
            for (String folder : folders) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME, folder);
                values.put(COLUMN_LAST_SELECTED_BUTTON, lastSelectedButton);
                db.insert(TABLE_FOLDERS, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public List<String> loadFolders() {
        List<String> folders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FOLDERS, null);
        if (cursor.moveToFirst()) {
            do {
                folders.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return folders;
    }

    public void saveCard(String folderName, String front, String back) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FOLDER_NAME, folderName);
        values.put(COLUMN_FRONT, front);
        values.put(COLUMN_BACK, back);
        db.insert(TABLE_CARDS, null, values);
        db.close();
    }

    public List<String[]> loadCards(String folderName) {
        List<String[]> cards = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_FRONT + ", " + COLUMN_BACK + " FROM " + TABLE_CARDS + " WHERE " + COLUMN_FOLDER_NAME + "=?", new String[]{folderName});
        if (cursor.moveToFirst()) {
            do {
                String front = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FRONT));
                String back = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BACK));
                cards.add(new String[]{front, back});
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return cards;
    }

    public void deleteCards(String folderName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CARDS, COLUMN_FOLDER_NAME + "=?", new String[]{folderName});
        db.close();
    }

    public List<Map<String, String>> getAllCards() {
        List<Map<String, String>> cards = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("cards", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String front = cursor.getString(cursor.getColumnIndexOrThrow("front"));
                String back = cursor.getString(cursor.getColumnIndexOrThrow("back"));
                Map<String, String> card = new HashMap<>();
                card.put("front", front);
                card.put("back", back);
                cards.add(card);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cards;
    }
}