package com.example.pc_control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "computers.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_COMPUTERS = "computers";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_IP_ADDRESS = "ip_address";
    private static final String COLUMN_LAST_CONNECTED = "last_connected";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_COMPUTERS_TABLE = "CREATE TABLE " + TABLE_COMPUTERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_IP_ADDRESS + " TEXT UNIQUE,"
                + COLUMN_LAST_CONNECTED + " TEXT" + ")";
        db.execSQL(CREATE_COMPUTERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPUTERS);
        onCreate(db);
    }

    public void addComputer(String ipAddress, String lastConnected) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_IP_ADDRESS, ipAddress);
            values.put(COLUMN_LAST_CONNECTED, lastConnected);
            db.insert(TABLE_COMPUTERS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public List<String> getAllComputers() {
        List<String> computers = new ArrayList<>();
        String selectQuery = "SELECT " + COLUMN_IP_ADDRESS + " FROM " + TABLE_COMPUTERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    computers.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IP_ADDRESS)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return computers;
    }

    public void clearAllComputers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_COMPUTERS);
        db.close();
    }
}
