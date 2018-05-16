/*
Udacity Final Project for Android Basics Nano-Degree.
Stage 2 of Inventory App.
By Dave King 05/15/18


*/
package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the product data loader */
    private static final int INVENTORY_LOADER = 0;

    /** Adapter for the ListView */
    private InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.plus);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the product data
        ListView inventoryListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        TextView emptyView = findViewById(R.id.empty_text_view);
        inventoryListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of product data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, final long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(InventoryActivity.this, ViewActivity.class);
                // Form the content URI that represents the specific product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link InventoryEntry#CONTENT_URI}.
                Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);
                // Launch the {@link EditorActivity} to display the data for the current product.
                startActivity(intent);
            }

        });
        // Kick off the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Toast.makeText(this, rowsDeleted + " " + getString(R.string.deleted_all_products_message), Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Shows Alert menu verifying the user wants to delete
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAllProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // method to check if quantity is at 0 so it cant go below. Will sent Toast to user that you cant go below zero.
    public void productSaleCount(int productID, int productQuantity) {
        productQuantity = productQuantity - 1;
        if (productQuantity >= 0) {
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
            Uri updateUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, productID);
            int rowsAffected = getContentResolver().update(updateUri, values, null, null);
            Toast.makeText(this, R.string.quantity_changed, Toast.LENGTH_SHORT).show();

            Log.d("Log msg", "rowsAffected " + rowsAffected + " - productID " + productID + " - quantity " + productQuantity + " , decreaseCount has been called.");
        } else {
            Toast.makeText(this, R.string.already_zero, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }



}


