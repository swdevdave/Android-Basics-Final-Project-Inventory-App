package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;


@SuppressWarnings("ConstantConditions")
public class InventoryProvider extends ContentProvider {

    /** URI matcher code for the content URI for the products table */
    private static final int PRODUCTS = 100;

    /** URI matcher code for the content URI for a single product in the products table */
    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The content URI of the form will map to the integer code {@link #PRODUCTS}.
        // This URI is used to provide access to MULTIPLE rowws of the products table.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCT, PRODUCTS);

        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCT + "/#", PRODUCT_ID);
    }

    /** Database helper object */
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper((getContext()));
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI" + uri + " with match " + match);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private Uri insertProduct(Uri uri, ContentValues values) {
        String nameProduct = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (nameProduct == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        Integer priceProduct = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (priceProduct != null && priceProduct < 0) {
            throw new IllegalArgumentException("Product requires a price ");
        }

        Integer quantityProduct = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantityProduct != null && quantityProduct < 0) {
            throw new IllegalArgumentException("Product requires a quantity");
        }

        Integer supplierName = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        if (supplierName == null || InventoryEntry.isValidSupplierName(supplierName)) {
            throw new IllegalArgumentException("Supplier Name requires Supplier Name");
        }

        Integer supplierPhone = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);
        if (supplierPhone != null && supplierPhone < 0) {
            throw new IllegalArgumentException("Supplier Phone requires Phone Number");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.v("message:", "Failed to insert new row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[]
            selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String nameProduct = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (nameProduct == null) {
                throw new IllegalArgumentException("Product name is required");
            }
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            Integer priceProduct = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (priceProduct != null && priceProduct < 0) {
                throw new
                        IllegalArgumentException("Product requires a price");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantityProduct = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantityProduct != null && quantityProduct < 0) {
                throw new
                        IllegalArgumentException("Product requires a quantity");
            }
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME)) {
            Integer supplierName = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            if (supplierName == null || InventoryEntry.isValidSupplierName(supplierName)) {
                throw new IllegalArgumentException("Supplier Name required");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER)) {
            Integer supplierPhone = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);
            if (supplierPhone != null && supplierPhone < 0) {
                throw new
                        IllegalArgumentException("Supplier Phone is required");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}