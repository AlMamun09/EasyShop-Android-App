package com.devmats.easyshop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.InputStream;

public class AddProductActivity extends AppCompatActivity {

    private static final String TAG = "AddProductActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 102;

    private ImageView imgUpload;
    private EditText edtProductName, edtUnits, edtPrice;
    private Button btnAddProduct;
    private Uri imageUri = null;
    private int productId = -1;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Initialize views
        imgUpload = findViewById(R.id.img_upload);
        edtProductName = findViewById(R.id.edt_product_name);
        edtUnits = findViewById(R.id.edt_units);
        edtPrice = findViewById(R.id.edt_price);
        btnAddProduct = findViewById(R.id.btn_add_product);
        dbHelper = DBHelper.getInstance(this);

        // Check if editing existing product
        setupIntentData();

        // Set click listeners
        imgUpload.setOnClickListener(v -> checkStoragePermission());
        btnAddProduct.setOnClickListener(v -> handleProductOperation());
    }

    private void setupIntentData() {
        Intent intent = getIntent();
        productId = intent.getIntExtra("productId", -1);

        if (productId != -1) {
            edtProductName.setText(intent.getStringExtra("productName"));
            edtUnits.setText(intent.getStringExtra("productUnits"));
            edtPrice.setText(intent.getStringExtra("productPrice"));

            String imageUriString = intent.getStringExtra("productImageUri");
            if (imageUriString != null && !imageUriString.isEmpty()) {
                imageUri = Uri.parse(imageUriString);
                imgUpload.setImageURI(imageUri);
            }

            btnAddProduct.setText("Update Product");
        }
    }

    private void handleProductOperation() {
        String name = edtProductName.getText().toString().trim();
        String units = edtUnits.getText().toString().trim();
        String price = edtPrice.getText().toString().trim();

        if (!validateInputs(name, units, price)) return;

        String imageUriString = imageUri != null ? imageUri.toString() : null;

        boolean success;
        if (productId != -1) {
            success = dbHelper.updateProduct(productId, name, units, price, imageUriString);
        } else {
            success = dbHelper.insertProduct(name, units, price, imageUriString);
        }

        if (success) {
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String name, String units, String price) {
        if (name.isEmpty() || units.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    // Take persistable permission
                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

                    // Load the image safely
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imgUpload.setImageBitmap(bitmap);
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    imageUri = uri;
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image", e);
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission required to select images", Toast.LENGTH_SHORT).show();
            }
        }
    }
}