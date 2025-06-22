package com.s23010269.skill4u;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DATABASE_NAME = "skill4u.db";
    private static final int DATABASE_VERSION = 3;

    // User table
    public static final String USER_TABLE = "user";
    public static final String USER_COL_ID = "id";
    public static final String USER_COL_USERNAME = "username";
    public static final String USER_COL_NAME = "name";
    public static final String USER_COL_EMAIL = "email";
    public static final String USER_COL_PASSWORD = "password";

    // Time records table
    public static final String TIME_RECORDS_TABLE = "time_records";
    public static final String TIME_RECORDS_COL_ID = "id";
    public static final String TIME_RECORDS_COL_TYPE = "type"; // Type will be username
    public static final String TIME_RECORDS_COL_DURATION = "duration"; // Duration of the activity

    // Location table
    public static final String LOCATION_TABLE = "location";
    public static final String LOCATION_COL_ID = "id";
    public static final String LOCATION_COL_USERNAME = "username";
    public static final String LOCATION_COL_LAT = "latitude";
    public static final String LOCATION_COL_LNG = "longitude";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // User table
        db.execSQL("CREATE TABLE " + USER_TABLE + " (" +
                USER_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_COL_USERNAME + " TEXT UNIQUE, " +
                USER_COL_NAME + " TEXT, " +
                USER_COL_EMAIL + " TEXT, " +
                USER_COL_PASSWORD + " TEXT)");

        // Time records table
        db.execSQL("CREATE TABLE " + TIME_RECORDS_TABLE + " (" +
                TIME_RECORDS_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TIME_RECORDS_COL_TYPE + " TEXT, " +
                TIME_RECORDS_COL_DURATION + " TEXT)");

        // Location table
        db.execSQL("CREATE TABLE " + LOCATION_TABLE + " (" +
                LOCATION_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LOCATION_COL_USERNAME + " TEXT UNIQUE, " +
                LOCATION_COL_LAT + " REAL, " +
                LOCATION_COL_LNG + " REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple upgrade strategy: drop all tables and recreate
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TIME_RECORDS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);
        onCreate(db);
    }

    // Insert new user
    public boolean insertUser(String username, String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_COL_USERNAME, username);
        values.put(USER_COL_NAME, name);
        values.put(USER_COL_EMAIL, email);
        values.put(USER_COL_PASSWORD, password);
        long result = db.insert(USER_TABLE, null, values);
        return result != -1;
    }

    // Check if username exists
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + USER_TABLE + " WHERE " + USER_COL_USERNAME + " = ?",
                new String[]{username}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // Insert time record
    public boolean insertTimeRecord(String type, String duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TIME_RECORDS_COL_TYPE, type);
        values.put(TIME_RECORDS_COL_DURATION, duration);
        long result = db.insert(TIME_RECORDS_TABLE, null, values);
        return result != -1;
    }

    // Retrieve full name from username
    public String getNameFromDatabase(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String name = null;
        Cursor cursor = db.rawQuery(
                "SELECT " + USER_COL_NAME + " FROM " + USER_TABLE + " WHERE " + USER_COL_USERNAME + " = ?",
                new String[]{username}
        );
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    // Get total time by user
    public ArrayList<String> getActivityTimesByUser() {
        ArrayList<String> userTimes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Corrected query to sum durations for each user
        String query = "SELECT u." + USER_COL_USERNAME + ", SUM(CAST(t." + TIME_RECORDS_COL_DURATION + " AS INTEGER)) AS total_time " +
                "FROM " + USER_TABLE + " u " +
                "LEFT JOIN " + TIME_RECORDS_TABLE + " t ON u." + USER_COL_USERNAME + " = t." + TIME_RECORDS_COL_TYPE +
                " GROUP BY u." + USER_COL_USERNAME;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndexOrThrow(USER_COL_USERNAME));
                String totalTime = cursor.getString(cursor.getColumnIndexOrThrow("total_time"));
                userTimes.add(username + ": " + totalTime + " hours");
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userTimes;
    }

    // Save or update user location
    public boolean saveUserLocation(String username, double lat, double lng) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LOCATION_COL_USERNAME, username);
        values.put(LOCATION_COL_LAT, lat);
        values.put(LOCATION_COL_LNG, lng);

        // Try to update first
        int rows = db.update(LOCATION_TABLE, values, LOCATION_COL_USERNAME + " = ?", new String[]{username});
        if (rows == 0) {
            // Insert if update didn't happen
            long id = db.insert(LOCATION_TABLE, null, values);
            db.close();
            return id != -1;
        }
        db.close();
        return true;
    }

    // Remove a user's location entry (disables location)
    public boolean disableLocation(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(LOCATION_TABLE, LOCATION_COL_USERNAME + " = ?", new String[]{username});
        db.close();
        return rows > 0;
    }

    // Check if user has a saved location
    public boolean hasLocation(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + LOCATION_TABLE + " WHERE " + LOCATION_COL_USERNAME + " = ?", new String[]{username});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // Get all users with saved locations
    public ArrayList<UserLocation> getAllUsersWithLocation() {
        ArrayList<UserLocation> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + LOCATION_TABLE, null);

        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndexOrThrow(LOCATION_COL_USERNAME));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(LOCATION_COL_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(LOCATION_COL_LNG));
                locations.add(new UserLocation(username, lat, lng));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return locations;
    }

    // Alias method for clarity
    public boolean updateUserLocation(String username, double lat, double lng) {
        return saveUserLocation(username, lat, lng);
    }
}
