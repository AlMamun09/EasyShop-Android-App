package com.devmats.easyshop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";

    // Database Info
    private static final String DATABASE_NAME = "EasyShopDB";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_PRODUCTS = "products";

    // Common Column
    private static final String KEY_ID = "id";

    // User Table Columns
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    // Product Table Columns
    private static final String KEY_PRODUCT_NAME = "name";
    private static final String KEY_PRODUCT_UNITS = "units";
    private static final String KEY_PRODUCT_PRICE = "price";
    private static final String KEY_PRODUCT_IMAGE_URI = "imageUri";  // Changed back to URI

    private static DBHelper instance;
    private SQLiteDatabase database;

    private final Context context;
    private boolean isDatabaseOpen = false;

    // Private constructor to prevent direct instantiation
    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    // Singleton pattern to get DBHelper instance
    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            if (context == null) {
                Log.e(TAG, "Context is null, cannot create DBHelper instance.");
                return null;
            }
            instance = new DBHelper(context);
        }
        return instance;
    }

    // Open the database connection
    public synchronized SQLiteDatabase openDatabase() {
        if (database == null || !database.isOpen()) {
            database = getWritableDatabase();
            isDatabaseOpen = true;
        }
        return database;
    }

    @Override
    public synchronized void close() {
        super.close();
        if (database != null && database.isOpen()) {
            database.close();
            database = null;
            isDatabaseOpen = false;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Create Users Table
            String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_USERNAME + " TEXT NOT NULL,"
                    + KEY_EMAIL + " TEXT UNIQUE NOT NULL,"
                    + KEY_PASSWORD + " TEXT NOT NULL)";

            // In table creation
            String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_PRODUCT_NAME + " TEXT UNIQUE NOT NULL,"
                    + KEY_PRODUCT_UNITS + " TEXT NOT NULL,"
                    + KEY_PRODUCT_PRICE + " TEXT NOT NULL,"
                    + KEY_PRODUCT_IMAGE_URI + " TEXT)";  // Changed from IMAGE_URI to IMAGE

            db.execSQL(CREATE_USERS_TABLE);
            db.execSQL(CREATE_PRODUCTS_TABLE);

            // Add indexes for better performance on frequently queried columns
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_email ON " + TABLE_USERS + " (" + KEY_EMAIL + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_product_name ON " + TABLE_PRODUCTS + " (" + KEY_PRODUCT_NAME + ")");
        } catch (SQLException e) {
            Log.e(TAG, "Error creating tables: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
            onCreate(db);
        } catch (SQLException e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage());
        }
    }

    // ==================== USER MANAGEMENT METHODS ====================

    public boolean insertUser(String username, String email, String password) {
        if (email == null || username == null || password == null) {
            showToast("Invalid input data.");
            return false;
        }

        try {
            SQLiteDatabase db = openDatabase();

            if (checkUserExists(email)) {
                showToast("Email already registered");
                return false;
            }

            ContentValues values = new ContentValues();
            values.put(KEY_USERNAME, username);
            values.put(KEY_EMAIL, email);
            values.put(KEY_PASSWORD, password);

            long result = db.insert(TABLE_USERS, null, values);

            if (result == -1) {
                showToast("Registration failed");
                return false;
            } else {
                showToast("Registration successful");
                return true;
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting user: " + e.getMessage());
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                showToast("Email already registered");
            }
            return false;
        }
    }

    public boolean checkUserExists(String email) {
        if (email == null) {
            return false;
        }

        Cursor cursor = null;
        try {
            SQLiteDatabase db = openDatabase();
            cursor = db.query(TABLE_USERS,
                    new String[]{KEY_ID},
                    KEY_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null);

            return cursor.getCount() > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Error checking user existence: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean checkUser(String email, String password) {
        if (email == null || password == null) {
            return false;
        }

        Cursor cursor = null;
        try {
            SQLiteDatabase db = openDatabase();
            cursor = db.query(TABLE_USERS,
                    new String[]{KEY_ID},
                    KEY_EMAIL + " = ? AND " + KEY_PASSWORD + " = ?",
                    new String[]{email, password},
                    null, null, null);

            boolean isValid = cursor.getCount() > 0;
            if (!isValid) {
                showToast("Invalid credentials");
            }
            return isValid;
        } catch (SQLException e) {
            Log.e(TAG, "Error checking user credentials: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getUsername(String email) {
        if (email == null) {
            return null;
        }

        Cursor cursor = null;
        try {
            SQLiteDatabase db = openDatabase();
            cursor = db.query(TABLE_USERS,
                    new String[]{KEY_USERNAME},
                    KEY_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null);

            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
            return null;
        } catch (SQLException e) {
            Log.e(TAG, "Error getting username: " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ==================== PRODUCT MANAGEMENT METHODS ====================

    public boolean checkIfProductExists(String productName) {
        if (productName == null) {
            return false;
        }

        Cursor cursor = null;
        try {
            SQLiteDatabase db = openDatabase();
            String lowerProductName = productName.toLowerCase();
            cursor = db.query(TABLE_PRODUCTS,
                    new String[]{KEY_ID},
                    "LOWER(" + KEY_PRODUCT_NAME + ") = ?",
                    new String[]{lowerProductName},
                    null, null, null);

            return cursor.getCount() > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Error checking product existence: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean insertProduct(String name, String units, String price, String imageUri) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCT_NAME, name);
        values.put(KEY_PRODUCT_UNITS, units);
        values.put(KEY_PRODUCT_PRICE, price);
        values.put(KEY_PRODUCT_IMAGE_URI, imageUri);  // Using URI constant

        long result = db.insert(TABLE_PRODUCTS, null, values);
        close();
        return result != -1;
    }

    public boolean updateProduct(int productId, String name, String units, String price, String imageUri) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCT_NAME, name);
        values.put(KEY_PRODUCT_UNITS, units);
        values.put(KEY_PRODUCT_PRICE, price);
        if (imageUri != null) {
            values.put(KEY_PRODUCT_IMAGE_URI, imageUri);  // Using URI constant
        }

        int rows = db.update(TABLE_PRODUCTS, values, KEY_ID + "=?",
                new String[]{String.valueOf(productId)});
        close();
        return rows > 0;
    }

    public Cursor getAllProducts() {
        SQLiteDatabase db = openDatabase();
        return db.query(TABLE_PRODUCTS,
                new String[]{KEY_ID, KEY_PRODUCT_NAME,
                        KEY_PRODUCT_UNITS, KEY_PRODUCT_PRICE,
                        KEY_PRODUCT_IMAGE_URI},  // Using URI constant
                null, null, null, null, null);
    }

    public void deleteProductById(int id) {
        try {
            SQLiteDatabase db = openDatabase();
            int deletedRows = db.delete(TABLE_PRODUCTS,
                    KEY_ID + " = ?",
                    new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            Log.e(TAG, "Error deleting product: " + e.getMessage());
        }
    }



    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
