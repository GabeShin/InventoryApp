package com.example.android.eventshopinventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import com.example.android.eventshopinventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Gabe on 2016-10-19.
 */

public class InventoryProvider extends ContentProvider {

    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    private InventoryDbHelper mDbHelper;

    private static final int INV = 100;
    private static final int INV_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        // "content://com.example.android.inventoryapp/inventory"
        // This URI is used to provide access to MULTIPLE rows of the table
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INV);
        // "content://com.example.android.inventoryapp/inventory/#"
        // This URI is used to provide access to ONE single row of the table.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INV_ID);
    }

    // onCreate() - Uses InventoryDbHelper - create table, if it doesn't exists, otherwise calls it
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match){
            case INV:
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INV_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri " + uri);
        }
        // Setting notification URI on the cursor!
        // we would know if ANY data at this URI changes, so we should update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        // return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case INV:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            case INV_ID:
                return InventoryEntry.CONTENT_LIST_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        int updatedRowId = 0;

        switch (match){
            case INV:
                updatedRowId =updateInv(uri, values, selection, selectionArgs);
            case INV_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                updatedRowId =updateInv(uri, values, selection, selectionArgs);
        }
        if (updatedRowId != 0) {
            // notify change in Uri
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedRowId;
    }

    private int updateInv(Uri uri, ContentValues cv, String selection, String[] selectionArgs){
        // do a sanity check //
        if (cv.containsKey(InventoryEntry.COLUMN_NAME)){
            String name = cv.getAsString(InventoryEntry.COLUMN_NAME);
            if (name == null || name.isEmpty()){
                throw new IllegalArgumentException("Inventory requires name");
            }
        }
        if (cv.containsKey(InventoryEntry.COLUMN_PRICE)){
            Integer price = cv.getAsInteger(InventoryEntry.COLUMN_PRICE);
            if (price == null){
                throw new IllegalArgumentException("Inventory requires price");
            }
        }
        if (cv.containsKey(InventoryEntry.COLUMN_QUANTITY)){
            Integer quantity = cv.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
            if (quantity == null || quantity < 0){
                throw new IllegalArgumentException("Inventory requires quantity");
            }
        }

        if (cv.size() == 0){
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.update(InventoryEntry.TABLE_NAME, cv, selection, selectionArgs);
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case INV:
                return insertInv(uri, values);
            // There is no case for INV_ID, for you don't insert to a SPECIFIC item
            default:
                throw new IllegalArgumentException("Insertion is not supported for the URI: " + uri);
        }
    }

    private Uri insertInv(Uri uri, ContentValues cv){
        // do a sanity check //
        if (cv.containsKey(InventoryEntry.COLUMN_NAME)){
            String name = cv.getAsString(InventoryEntry.COLUMN_NAME);
            if (name == null || name.isEmpty()){
                throw new IllegalArgumentException("Inventory requires name");
            }
        }
        if (cv.containsKey(InventoryEntry.COLUMN_PRICE)){
            Integer price = cv.getAsInteger(InventoryEntry.COLUMN_PRICE);
            if (price == null){
                throw new IllegalArgumentException("Inventory requires price");
            }
        }
        if (cv.containsKey(InventoryEntry.COLUMN_QUANTITY)){
            Integer quantity = cv.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
            if (quantity == null || quantity < 0){
                throw new IllegalArgumentException("Inventory requires quantity");
            }
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, cv);
        // notify change in Uri
        getContext().getContentResolver().notifyChange(uri, null);
        // return Uri of the updated item
        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deletedRows = 0;
        int match = sUriMatcher.match(uri);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (match){
            case INV:
                deletedRows = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INV_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                deletedRows = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete is not supported for the URI: " + uri);
        }

        if (deletedRows != 0){
            // notify change in Uri
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedRows;
    }
}
