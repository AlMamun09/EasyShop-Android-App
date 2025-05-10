package com.devmats.easyshop;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private ArrayList<ProductModel> productList;
    private final DBHelper db;

    public ProductAdapter(Context context, ArrayList<ProductModel> productList, DBHelper db) {
        this.context = context;
        this.productList = productList;
        this.db = db;
    }

    public void refreshData() {
        this.productList = getAllProductsFromDB();
        notifyDataSetChanged();
    }

    private ArrayList<ProductModel> getAllProductsFromDB() {
        ArrayList<ProductModel> newList = new ArrayList<>();
        Cursor cursor = db.getAllProducts();
        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String units = cursor.getString(2);
                String price = cursor.getString(3);
                String imageUri = cursor.getString(4); // Changed back to URI
                newList.add(new ProductModel(id, name, units, price, imageUri));
            }
        } finally {
            cursor.close();
        }
        return newList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_row, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);

        holder.name.setText(product.getName());
        holder.price.setText("৳ " + product.getPrice());
        holder.units.setText(product.getUnits() + " units");

        // Load image using URI
        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            try {
                Uri uri = Uri.parse(product.getImageUri());
                // Take persistable permission
                context.getContentResolver().takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                Glide.with(context)
                        .load(uri)
                        .into(holder.image);
            } catch (Exception e) {
                holder.image.setImageResource(R.drawable.noimage);
                e.printStackTrace();
            }
        } else {
            holder.image.setImageResource(R.drawable.noimage);
        }

        // Add to cart button
        holder.addButton.setOnClickListener(v -> {
            String qtyStr = holder.quantity.getText().toString().trim();
            if (!qtyStr.isEmpty()) {
                Toast.makeText(context, qtyStr + " units of " + product.getName() + " added to cart.", Toast.LENGTH_SHORT).show();
                holder.quantity.setText("");
            } else {
                Toast.makeText(context, "Please enter a quantity", Toast.LENGTH_SHORT).show();
            }
        });

        // Long press to delete/update
        holder.itemView.setOnLongClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Product Options")
                    .setMessage("Do you want to delete or update " + product.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.deleteProductById(product.getId());
                        refreshData();
                        Toast.makeText(context, product.getName() + " deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .setNeutralButton("Update", (dialog, which) -> {
                        Intent intent = new Intent(context, AddProductActivity.class);
                        intent.putExtra("productId", product.getId());
                        intent.putExtra("productName", product.getName());
                        intent.putExtra("productUnits", product.getUnits());
                        intent.putExtra("productPrice", product.getPrice());
                        intent.putExtra("productImageUri", product.getImageUri()); // Changed back to URI
                        ((HomePageActivity) context).startActivityForResult(intent, HomePageActivity.ADD_PRODUCT_REQUEST);
                    })
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, units;
        ImageView image;
        EditText quantity;
        Button addButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            units = itemView.findViewById(R.id.txt_units);
            image = itemView.findViewById(R.id.product_image);
            quantity = itemView.findViewById(R.id.edt_quantity);
            addButton = itemView.findViewById(R.id.btn_add_cart);
        }
    }
}