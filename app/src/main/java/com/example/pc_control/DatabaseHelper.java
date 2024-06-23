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
    private static final int DATABASE_VERSION = 2; // Updated to 2 to trigger onUpgrade
    private static final String TABLE_COMPUTERS = "computers";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DEVICE_NAME = "device_name"; // New column for device name
    private static final String COLUMN_IP_ADDRESS = "ip_address";
    private static final String COLUMN_LAST_CONNECTED = "last_connected";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_COMPUTERS_TABLE = "CREATE TABLE " + TABLE_COMPUTERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DEVICE_NAME + " TEXT," // Added new column
                + COLUMN_IP_ADDRESS + " TEXT UNIQUE,"
                + COLUMN_LAST_CONNECTED + " TEXT" + ")";
        db.execSQL(CREATE_COMPUTERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_COMPUTERS + " ADD COLUMN " + COLUMN_DEVICE_NAME + " TEXT");
        }
    }

    public void addComputer(String deviceName, String ipAddress, String lastConnected) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DEVICE_NAME, deviceName); // Added device name
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
        String selectQuery = "SELECT " + COLUMN_DEVICE_NAME + ", " + COLUMN_IP_ADDRESS + " FROM " + TABLE_COMPUTERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    String deviceName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEVICE_NAME));
                    String ipAddress = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IP_ADDRESS));
                    computers.add(deviceName + " - " + ipAddress); // Format the output to include both device name and IP address
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

    public void removeComputer(String ipAddress) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COMPUTERS, COLUMN_IP_ADDRESS + "=?", new String[]{ipAddress});
        db.close();
    }

    public void updateComputerName(String ipAddress, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICE_NAME, newName); // Ensure COLUMN_DEVICE_NAME exists in your schema
        db.update(TABLE_COMPUTERS, values, COLUMN_IP_ADDRESS + "=?", new String[]{ipAddress});
        db.close();
    }

}
