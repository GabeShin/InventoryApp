package com.example.android.eventshopinventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Gabe on 2016-10-19.
 */
public class InventoryContract {

    // create empty constructor - in order to prevent someone from accidentally instantiating the contract class
    private InventoryContract(){
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.eventshopinventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";

    public static abstract class InventoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        // CURSOR_DIR_BASE_TYPE/com.example.android.eventshopinventoryapp/inv
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        // CURSOR_ITEM_BASE_TYPE/com.example.android.eventshopinventoryapp/inv
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        // Name of the database table for inventories
        public static final String TABLE_NAME = "inventory";

        // name of columns
        public static final String _ID = BaseColumns._ID;                 // INTERGER. Unique ID
        public static final String COLUMN_NAME = "name";                  // TEXT
        public static final String COLUMN_CATEGORY = "category";          // INTEGER - possible values below
        public static final String COLUMN_PRICE = "price";                // INTEGER
        public static final String COLUMN_QUANTITY = "quantity";          // INTEGER
        public static final String COLUMN_IMAGE = "image";                // BLOB

        // possible values for category
        public static final int CATEGORY_OTHERS = 0;
        public static final int CATEGORY_TOY = 1;
        public static final int CATEGORY_LIVING = 2;
        public static final int CATEGORY_FASHION = 3;
        public static final int CATEGORY_SCHOOL = 4;
        public static final int CATEGORY_TECH = 5;
        public static final int CATEGORY_OUTDOOR = 6;
        public static final int CATEGORY_COLLABORATION = 7;
        public static final int CATEGORY_JEWELRY = 8;
        public static final int CATEGORY_TODDLER = 9;
    }
}