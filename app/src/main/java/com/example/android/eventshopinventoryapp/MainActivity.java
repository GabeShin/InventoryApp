package com.example.android.eventshopinventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.eventshopinventoryapp.data.InventoryDbHelper;
import com.example.android.eventshopinventoryapp.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private InventoryDbHelper mDbHelper;

    private InventoryCursorAdapter mAdapter;

    private static final int LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up FAB to open editor activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find GridView to populate with the list
        GridView gridView = (GridView) findViewById(R.id.gridview);

        // set up CursorAdapter & Kick off the loader!
        mAdapter = new InventoryCursorAdapter(this, null);
        gridView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        // set up on Click Items Listeners for GridView items
        // When clicked on the Item, go to Editor Activity, and PASS Uri Data along -
        //      in order to populate the Editor Activity with corresponding item.
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                Uri passedUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                intent.setData(passedUri);

                Log.v(LOG_TAG, "Passed Uri is " + Uri.withAppendedPath(InventoryEntry.CONTENT_URI, String.valueOf(i)).toString());

                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Set up user click event on each menu items
        switch (item.getItemId()){
            case R.id.action_view_transaction:
                return true;
            case R.id.action_insert_dummy_item:
                insertDummyInventory();
                return true;
            case R.id.action_delete_all_item:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyInventory(){
        ContentValues cv = new ContentValues();
        cv.put(InventoryEntry.COLUMN_NAME, "Ryan Doll");
        cv.put(InventoryEntry.COLUMN_CATEGORY, InventoryEntry.CATEGORY_TOY);
        cv.put(InventoryEntry.COLUMN_PRICE, 34);
        cv.put(InventoryEntry.COLUMN_QUANTITY, 0);

        Bitmap image = BitmapFactory.decodeResource(this.getResources(),R.drawable.ryan_doll_test);
        byte[] imageBytes = DbBitmapUtility.getBytes(image);
        cv.put(InventoryEntry.COLUMN_IMAGE, imageBytes);

        Uri uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, cv);
    }

    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAllInventory();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllInventory(){
        int deletedRows = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        if (deletedRows == 0){
            Toast.makeText(this, R.string.delete_fail, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.delete_successful, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID, InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_CATEGORY, InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY, InventoryEntry.COLUMN_IMAGE
        };
        CursorLoader cursorLoader = new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, null, null, null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}