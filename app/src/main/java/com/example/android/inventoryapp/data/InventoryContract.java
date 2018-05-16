package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app.
 */
public class InventoryContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    @SuppressWarnings("unused")
    public InventoryContract() {
    }

    public final static String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    private final static Uri BASE_CONTENT_URI = Uri.parse("content://" +CONTENT_AUTHORITY);
    public final static String PATH_PRODUCT = "product";

    public final static class InventoryEntry implements BaseColumns {

        /** The content URI to access the product data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);

        //The MIME type of the {@link #CONTENT_URI} for a list of products.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

         // The MIME type of the {@link #CONTENT_URI} for a single product.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        //Name of table DB
        public final static String TABLE_NAME = "product";

        //Column of table
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME = "product_name";
        public final static String COLUMN_PRODUCT_PRICE = "price";
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
        public final static String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier_name";
        public final static String COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";

        // SUPPLIER_NAME LIST VALUES
        public final static int SUPPLIER_UNKNOWN = 0;
        public final static int SUPPLIER_AMAZON = 1;
        public final static int SUPPLIER_POWELL = 2;
        public final static int SUPPLIER_BARNESANDNOBLE = 3;

        public static boolean isValidSupplierName(int supplier) {
            return supplier != SUPPLIER_UNKNOWN && supplier != SUPPLIER_AMAZON &&
                    supplier != SUPPLIER_BARNESANDNOBLE && supplier != SUPPLIER_POWELL;
        }
    }
}
