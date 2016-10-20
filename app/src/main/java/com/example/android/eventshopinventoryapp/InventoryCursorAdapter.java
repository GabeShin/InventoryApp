package com.example.android.eventshopinventoryapp;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.android.eventshopinventoryapp.data.InventoryContract.InventoryEntry;
import org.w3c.dom.Text;

/**
 * Created by Gabe on 2016-10-19.
 */
public class InventoryCursorAdapter extends CursorAdapter{

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.grid_item, parent, false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // find views to inflate
        TextView textViewName = (TextView) view.findViewById(R.id.item_name);
        TextView textViewCategory = (TextView) view.findViewById(R.id.item_category);
        TextView textViewPrice = (TextView) view.findViewById(R.id.item_price);
        TextView textViewOutOfStock = (TextView) view.findViewById(R.id.item_not_in_stock);
        ImageView imageView = (ImageView) view.findViewById(R.id.item_image);

        // extract the current cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_NAME));
        int categoryInt = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_CATEGORY));
        String category ="";
        switch (categoryInt){
            case InventoryEntry.CATEGORY_OTHERS:
                category = view.getResources().getString(R.string.item_category_others);
                break;
            case InventoryEntry.CATEGORY_TOY:
                category = view.getResources().getString(R.string.item_category_toy);
                break;
            case InventoryEntry.CATEGORY_LIVING:
                category = view.getResources().getString(R.string.item_category_living);
                break;
            case InventoryEntry.CATEGORY_FASHION:
                category = view.getResources().getString(R.string.item_category_fashion);
                break;
            case InventoryEntry.CATEGORY_SCHOOL:
                category = view.getResources().getString(R.string.item_category_school);
                break;
            case InventoryEntry.CATEGORY_TECH:
                category = view.getResources().getString(R.string.item_category_tech);
                break;
            case InventoryEntry.CATEGORY_OUTDOOR:
                category = view.getResources().getString(R.string.item_category_outdoor);
                break;
            case InventoryEntry.CATEGORY_COLLABORATION:
                category = view.getResources().getString(R.string.item_category_collaboration);
                break;
            case InventoryEntry.CATEGORY_JEWELRY:
                category = view.getResources().getString(R.string.item_category_jewelry);
                break;
            case InventoryEntry.CATEGORY_TODDLER:
                category = view.getResources().getString(R.string.item_category_toddler);
                break;
        }
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY));
        byte[] image = cursor.getBlob(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_IMAGE));

        // populate the field with extracted properties
        textViewName.setText(name);
        textViewCategory.setText(category);
        textViewPrice.setText("$ " + price);
        switch (quantity){
            case 0:
                textViewOutOfStock.setVisibility(View.VISIBLE);
                break;
            default:
                textViewOutOfStock.setVisibility(View.GONE);
        }
        if (image == null){
            Bitmap bitmapImage = BitmapFactory.decodeResource(context.getResources(),R.drawable.no_image_available);
            byte[] imageBytes = DbBitmapUtility.getBytes(bitmapImage);
            imageView.setImageResource(R.drawable.no_image_available);
        } else {
            Bitmap bitmapImage = DbBitmapUtility.getImage(image);
            imageView.setImageBitmap(bitmapImage);
        }
    }
}
