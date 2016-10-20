package com.example.android.eventshopinventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.eventshopinventoryapp.data.InventoryContract.InventoryEntry;
/**
 * Created by Gabe on 2016-10-19.
 */
public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    // Name of the database file
    private static final String DATABASE_NAME = "inventory.db";
    // Database version. If you change database schema, you MUST change the database version.
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create the database, when the database is called for the FIRST time.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // String that contains the SQL statement to create the table
        String SQL_CREATE_INV_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
                InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryEntry.COLUMN_NAME + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_CATEGORY + " INTEGER DEFAULT 0, " +
                InventoryEntry.COLUMN_PRICE + " INTEGER NOT NULL, " +
                InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
                InventoryEntry.COLUMN_IMAGE + " BLOB);";
        // Execute SQL command
        db.execSQL(SQL_CREATE_INV_TABLE);
    }
    // This is called when the database needs to be upgraded - like when database schema changed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}