package com.devmats.easyshop;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity {

    public static final int ADD_PRODUCT_REQUEST = 101;
    private static final int STORAGE_PERMISSION_CODE = 102;

    ImageView btnNavigation;
    EditText edtSearch;
    DrawerLayout drawerLayout;
    Button btnSignOut, btnDashboard, btnInventory, btnSaleHistory, btnProfile, btnAddProduct;
    RecyclerView recyclerView;
    DBHelper db;
    ProductAdapter adapter;
    ArrayList<ProductModel> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        SharedPreferences prefs = getSharedPreferences("EasyShopPrefs", MODE_PRIVATE);

        // Initialize views
        edtSearch = findViewById(R.id.edt_search);
        btnNavigation = findViewById(R.id.nav_icon);
        drawerLayout = findViewById(R.id.drawer_layout);
        btnSignOut = findViewById(R.id.btn_signout);
        btnDashboard = findViewById(R.id.btn_dashboard);
        btnInventory = findViewById(R.id.btn_inventory);
        btnSaleHistory = findViewById(R.id.btn_sales_histroy);
        btnProfile = findViewById(R.id.btn_profile);
        btnAddProduct = findViewById(R.id.btn_add_product);
        recyclerView = findViewById(R.id.recycler_view_products);

        // In your activity's onCreate or where you need database access
        DBHelper dbHelper = DBHelper.getInstance(this);
        db = dbHelper;
        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList, db);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);


        // Drawer actions
        btnNavigation.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        btnDashboard.setOnClickListener(v -> Toast.makeText(this, "Dashboard clicked", Toast.LENGTH_SHORT).show());
        btnInventory.setOnClickListener(v -> Toast.makeText(this, "Inventory clicked", Toast.LENGTH_SHORT).show());
        btnSaleHistory.setOnClickListener(v -> Toast.makeText(this, "Sales History clicked", Toast.LENGTH_SHORT).show());
        btnProfile.setOnClickListener(v -> Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show());

        // Sign out action
        btnSignOut.setOnClickListener(v -> {
            prefs.edit().remove("lastLoginTime").apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // Add product action
        btnAddProduct.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, AddProductActivity.class), ADD_PRODUCT_REQUEST);
        });

        // Permission check and loading products
        if (hasStoragePermission()) {
            loadProducts();
        } else {
            requestStoragePermission();
        }
    }

    // In HomePageActivity: Call loadProducts to refresh the product list from the database
    // Method to load products from the database
    public void loadProducts() {
        productList.clear(); // Clear the existing list
        Cursor cursor = db.getAllProducts(); // Get all products from the database
        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String units = cursor.getString(2);
                String price = cursor.getString(3);
                String imageUri = cursor.getString(4);

                productList.add(new ProductModel(id, name, units, price, imageUri)); // Add the updated product to the list
            }
        } finally {
            cursor.close(); // Make sure to close the cursor
        }
        adapter.notifyDataSetChanged(); // Notify the adapter that the list has been updated
    }



    // Check if the app has permission to read storage
    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Request permission to read storage
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadProducts();
            } else {
                Toast.makeText(this, "Permission required to load product images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper method to find the index of a product by its ID
    private int getProductIndexById(int productId) {
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).getId() == productId) {
                return i; // Return the index of the updated product
            }
        }
        return -1; // If not found, return -1
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_PRODUCT_REQUEST && resultCode == RESULT_OK) {
            adapter.refreshData(); // Refresh the adapter after update
        }

        if (requestCode == ADD_PRODUCT_REQUEST && resultCode == RESULT_OK && data != null) {
            String productName = data.getStringExtra("productName");
            String units = data.getStringExtra("units");
            String price = data.getStringExtra("price");
            String imageUri = data.getStringExtra("imageUri");
            int productId = data.getIntExtra("productId", -1);

            if (productId != -1) {
                // This is an update operation
                boolean isUpdated = db.updateProduct(productId, productName, units, price, imageUri);

                if (isUpdated) {
                    // Immediately update the product in the local productList
                    for (int i = 0; i < productList.size(); i++) {
                        if (productList.get(i).getId() == productId) {
                            productList.set(i, new ProductModel(
                                    productId,
                                    productName,
                                    units,
                                    price,
                                    imageUri
                            ));
                            loadProducts();
                            break;
                        }
                    }
                    // Notify the adapter to refresh the RecyclerView
                    adapter.notifyItemChanged(getProductIndexById(productId));
                    Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show();
                }
            } else {
                // This is a new product addition
                if (db.insertProduct(productName, units, price, imageUri)) {
                    loadProducts(); // Refresh the list after adding the new product
                }
            }
        }
    }
}
