package com.example.android.eventshopinventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.eventshopinventoryapp.data.InventoryContract;
import com.example.android.eventshopinventoryapp.data.InventoryContract.InventoryEntry;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

/**
 * Created by Gabe on 2016-10-19.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mInventoryName;
    private EditText mInventoryPrice;
    private EditText mInventoryQuantity;
    private Spinner mCategorySpinner;
    private int mCategory = 0;
    private ImageView mItemImage;
    private Button mInsertImage;
    private Button mSalesButton;
    private EditText mSalesQuantity;
    private TextView mSalesText;
    private Button mPurchaseButton;
    private EditText mPurchaseQuantity;
    private TextView mPurchaseText;

    private Uri mContentUri;

    private static final int LOADER_ID = 100;
    private static final int PICK_IMAGE = 400;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // find all relevant views using findViewById
        mInventoryName = (EditText) findViewById(R.id.editor_edit_name);
        mInventoryPrice = (EditText) findViewById(R.id.editor_edit_price);
        mInventoryQuantity = (EditText) findViewById(R.id.editor_edit_quantity);
        mCategorySpinner = (Spinner) findViewById(R.id.editor_spinner_category);
        mInsertImage = (Button) findViewById(R.id.editor_insert_image);
        mSalesButton = (Button) findViewById(R.id.editor_sale_button);
        mSalesQuantity = (EditText) findViewById(R.id.editor_sale_quantity);
        mSalesText = (TextView) findViewById(R.id.editor_sale_text);
        mPurchaseButton = (Button) findViewById(R.id.editor_purchase_button);
        mPurchaseQuantity = (EditText) findViewById(R.id.editor_purchase_quantity);
        mPurchaseText = (TextView) findViewById(R.id.editor_purchase_text);
        mItemImage = (ImageView) findViewById(R.id.editor_item_image);

        // set up the spinner
        setUpSpinner();

        // set up sales & purchase buttons
        mSalesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = mSalesQuantity.getText().toString().trim();
                int quantity = Integer.parseInt(quantityString);
                updateQuantity(-quantity);
            }
        });
        mPurchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = mPurchaseQuantity.getText().toString().trim();
                int quantity = Integer.parseInt(quantityString);
                updateQuantity(quantity);
            }
        });

        // get passed Uri data from MainActivity
        // if there is Uri, then populate the layout with pre-existing item. It is updating inventory
        // if there ISN't Uri, then it's add new inventory
        mContentUri = getIntent().getData();

        if (mContentUri == null) {
            setTitle(R.string.editor_activity_title_add_inv);   // set Title

            // hide update quantity features
            mSalesButton.setVisibility(View.GONE);
            mSalesQuantity.setVisibility(View.GONE);
            mSalesText.setVisibility(View.GONE);
            mPurchaseButton.setVisibility(View.GONE);
            mPurchaseQuantity.setVisibility(View.GONE);
            mPurchaseText.setVisibility(View.GONE);

        } else if (mContentUri != null){
            setTitle(R.string.editor_activity_title_edit_inv);  // set Title
            disableEdit();
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }

        // set up insert image on click event
        // 1. send intent to get image from the device's gallery
        // 2. on onActivityResult -> save the image in form of byte[] AND populate imageview with the image
        mInsertImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get content from gallery
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, PICK_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && requestCode == RESULT_OK && data != null){
            // get data from Pick Image intent, convert to Bitmap to be used
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = DbBitmapUtility.getScaledBitmap(picturePath, 500, 500);

            if (mContentUri == null){
                /*
                coming from Add a Item
                populate the current image view with the image
                 */
                mItemImage.setImageBitmap(bitmap);
            } else {
                /*
                coming from Edit the Item
                pass bytesImage to updateImage() method to update image
                 */
                byte[] bytesImage = DbBitmapUtility.getBytes(BitmapFactory.decodeFile(picturePath));
                updateImage(bytesImage);
            }
        }
    }



    // Setting up Spinner
    private void setUpSpinner(){
        // create adapter for spinner. The list options are from the String array
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_category_options, android.R.layout.simple_selectable_list_item);
        // Specify dropdown layout style - simple list view with 1 item per line
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
        // Apply the adapter to the spinner
        mCategorySpinner.setAdapter(categorySpinnerAdapter);
        // Set the integer to the constant values
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)){
                    if (selection.equals(getString(R.string.item_category_others))){
                        mCategory = InventoryEntry.CATEGORY_OTHERS; // Others
                    } else if (selection.equals(getString(R.string.item_category_toy))){
                        mCategory = InventoryEntry.CATEGORY_TOY;
                    } else if (selection.equals(getString(R.string.item_category_living))){
                        mCategory = InventoryEntry.CATEGORY_LIVING;
                    } else if (selection.equals(getString(R.string.item_category_fashion))){
                        mCategory = InventoryEntry.CATEGORY_FASHION;
                    } else if (selection.equals(getString(R.string.item_category_school))){
                        mCategory = InventoryEntry.CATEGORY_SCHOOL;
                    } else if (selection.equals(getString(R.string.item_category_tech))){
                        mCategory = InventoryEntry.CATEGORY_TECH;
                    } else if (selection.equals(getString(R.string.item_category_outdoor))){
                        mCategory = InventoryEntry.CATEGORY_OUTDOOR;
                    } else if (selection.equals(getString(R.string.item_category_collaboration))){
                        mCategory = InventoryEntry.CATEGORY_COLLABORATION;
                    } else if (selection.equals(getString(R.string.item_category_jewelry))){
                        mCategory = InventoryEntry.CATEGORY_JEWELRY;
                    } else if (selection.equals(getString(R.string.item_category_toddler))){
                        mCategory = InventoryEntry.CATEGORY_TODDLER;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = 0;
            }
        });
    }

    // set up menu
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mContentUri == null){
            // If it's ADD INVENTORY, hide action_delete menu item.
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
            MenuItem menuItem1 = menu.findItem(R.id.action_edit);
            menuItem1.setVisible(false);
            this.invalidateOptionsMenu();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Set up user click event on each menu items
        switch (item.getItemId()) {
            case R.id.action_save:
                saveInventory();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_edit:
                showEnableConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




    // if ADD ITEM: get user input from editor and save new inventory to the database
    // if EDIT ITEM: update the item
    private void saveInventory(){
        String nameString = mInventoryName.getText().toString().trim();

        int category;
        String categoryString = mCategorySpinner.getSelectedItem().toString();
        if (categoryString.equals(getString(R.string.item_category_others))){
            category = InventoryEntry.CATEGORY_OTHERS;
        } else if (categoryString.equals(getString(R.string.item_category_toy))){
            category = InventoryEntry.CATEGORY_TOY;
        } else if (categoryString.equals(getString(R.string.item_category_living))){
            category = InventoryEntry.CATEGORY_LIVING;
        } else if (categoryString.equals(getString(R.string.item_category_fashion))){
            category = InventoryEntry.CATEGORY_FASHION;
        } else if (categoryString.equals(getString(R.string.item_category_school))){
            category = InventoryEntry.CATEGORY_SCHOOL;
        } else if (categoryString.equals(getString(R.string.item_category_tech))){
            category = InventoryEntry.CATEGORY_TECH;
        } else if (categoryString.equals(getString(R.string.item_category_outdoor))){
            category = InventoryEntry.CATEGORY_OUTDOOR;
        } else if (categoryString.equals(getString(R.string.item_category_collaboration))){
            category = InventoryEntry.CATEGORY_COLLABORATION;
        } else if (categoryString.equals(getString(R.string.item_category_jewelry))){
            category = InventoryEntry.CATEGORY_JEWELRY;
        } else if (categoryString.equals(getString(R.string.item_category_toddler))){
            category = InventoryEntry.CATEGORY_TODDLER;
        }

        String priceString = mInventoryPrice.getText().toString().trim();
        int price;
        price = Integer.parseInt(priceString);

        String quantityString = mInventoryQuantity.getText().toString().trim();
        int quantity;
        quantity = Integer.parseInt(quantityString);

        byte[] bytesImage = null;
        if (mItemImage == null) {
            Bitmap bitmapImage = BitmapFactory.decodeResource(this.getResources(),R.drawable.no_image_available);
            bytesImage = DbBitmapUtility.getBytes(bitmapImage);
        } else {
            // convert View into Bitmap
            mItemImage.setDrawingCacheEnabled(true);
            mInsertImage.buildDrawingCache();
            Bitmap bitmapImage = mItemImage.getDrawingCache();
            // convert Bitmap into Byte Array
            bytesImage = DbBitmapUtility.getBytes(bitmapImage);
        }
        ContentValues cv = new ContentValues();
        cv.put(InventoryEntry.COLUMN_NAME, nameString);
        cv.put(InventoryEntry.COLUMN_CATEGORY, InventoryEntry.CATEGORY_TOY);
        cv.put(InventoryEntry.COLUMN_PRICE, price);
        cv.put(InventoryEntry.COLUMN_QUANTITY, quantity);
        cv.put(InventoryEntry.COLUMN_IMAGE, bytesImage);

        if (mContentUri == null) { // add item
            Uri uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, cv);

            Long newRowId = Long.valueOf(ContentUris.parseId(uri));
            if (newRowId == -1) {
                Toast.makeText(this, getResources().getString(R.string.insert_fail), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.insert_successful) + "- ID: " + newRowId, Toast.LENGTH_LONG).show();
            }
        } else { // edit item
            int updatedRowId = getContentResolver().update(mContentUri, cv, null, null);

            if (updatedRowId == 0) {
                Toast.makeText(this, R.string.update_fail, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.update_successful, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // delta is change in quantity :
    // if negative -> sales
    // if positive -> order
    private void updateQuantity(int delta){

        String quantityString = mInventoryQuantity.getText().toString().trim();
        int quantity;
        quantity = Integer.parseInt(quantityString);

        ContentValues cv = new ContentValues();
        cv.put(InventoryEntry.COLUMN_QUANTITY, quantity + delta);

        int updatedRowId = getContentResolver().update(mContentUri, cv, null, null);

        if (updatedRowId == 0) {
                Toast.makeText(this, R.string.update_fail, Toast.LENGTH_SHORT).show();
            } else if (delta > 0) {
                Toast.makeText(this, R.string.purchase_successful, Toast.LENGTH_SHORT).show();
            } else if (delta < 0) {
                Toast.makeText(this, R.string.sales_successful, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImage(byte[] input){
        byte[] bytesImage = input;
//        if (mItemImage == null) {
//            Bitmap bitmapImage = BitmapFactory.decodeResource(this.getResources(),R.drawable.no_image_available);
//            bytesImage = DbBitmapUtility.getBytes(bitmapImage);
//        } else {
//            Bitmap bitmapImage = mItemImage.getDrawingCache();
//            bytesImage = DbBitmapUtility.getBytes(bitmapImage);
//        }
        ContentValues cv = new ContentValues();
        cv.put(InventoryEntry.COLUMN_IMAGE, bytesImage);

        int updatedRowId = getContentResolver().update(mContentUri, cv, null, null);

        if (updatedRowId == 0) {
            Toast.makeText(this, R.string.update_fail, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.update_successful, Toast.LENGTH_SHORT).show();
        }
    }

    // delete the item - show dialog to confirm then delete the inventory
    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteInventory();
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

    private void deleteInventory(){
        int deletedRows;
        deletedRows = getContentResolver().delete(mContentUri, null, null);
        if (deletedRows == 0) {
            Toast.makeText(this, R.string.delete_fail, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.delete_successful, Toast.LENGTH_SHORT).show();
        }
        finish();
    }


    /*
    Cursor Loader Methods:
        IF EDIT ITEM, get Cursor for the table, and populate the views with the information
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mContentUri != null){
            String[] projection = {
                    InventoryEntry._ID, InventoryEntry.COLUMN_NAME, InventoryEntry.COLUMN_CATEGORY,
                    InventoryEntry.COLUMN_PRICE, InventoryEntry.COLUMN_QUANTITY, InventoryEntry.COLUMN_IMAGE
            };

            CursorLoader cursorLoader = new CursorLoader(this, mContentUri, projection, null, null, null);
            return cursorLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
// populate the fields with extracted properties
        if (data.moveToFirst()) { // move to the first data - if default, the position is at -1
            String name = data.getString(data.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_NAME));
            int category = data.getInt(data.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_CATEGORY));
            int price = data.getInt(data.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_PRICE));
            int quantity = data.getInt(data.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_QUANTITY));
            byte[] image = data.getBlob(data.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_IMAGE));

            mInventoryName.setText(name);
            mCategorySpinner.setSelection(category);
            mInventoryPrice.setText(String.valueOf(price));
            mInventoryQuantity.setText(String.valueOf(quantity));

            if (image == null){
                Bitmap bitmapImage = BitmapFactory.decodeResource(this.getResources(),R.drawable.no_image_available);
                byte[] imageBytes = DbBitmapUtility.getBytes(bitmapImage);
                mItemImage.setImageResource(R.drawable.no_image_available);
            } else {
                Bitmap bitmapImage = DbBitmapUtility.getImage(image);
                mItemImage.setImageBitmap(bitmapImage);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /*
    Enable/ Disable EditTexts and ConfirmationDialog
     */
    public void disableEdit(){
        mInventoryName.setEnabled(false);
        mCategorySpinner.setEnabled(false);
        mInventoryQuantity.setEnabled(false);
        mInventoryPrice.setEnabled(false);
        mInsertImage.setVisibility(View.GONE);
    }

    public void enableEdit(){
        mInventoryName.setEnabled(true);
        mCategorySpinner.setEnabled(true);
        mInventoryQuantity.setEnabled(true);
        mInventoryPrice.setEnabled(true);
        mInsertImage.setVisibility(View.VISIBLE);
    }

    private void showEnableConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.enable_dialog_msg);
        builder.setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                enableEdit();
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
}
