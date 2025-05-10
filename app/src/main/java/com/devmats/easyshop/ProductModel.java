package com.devmats.easyshop;

public class ProductModel {
    private int id;
    private String name;
    private String units;
    private String price;
    private String imageUri;  // Changed back to imageUri

    public ProductModel(int id, String name, String units, String price, String imageUri) {
        this.id = id;
        this.name = name;
        this.units = units;
        this.price = price;
        this.imageUri = imageUri;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnits() {
        return units;
    }

    public String getPrice() {
        return price;
    }

    public String getImageUri() {  // Changed from getImageBase64()
        return imageUri;
    }

    // Setters (if needed)
    public void setImageUri(String imageUri) {  // Changed from setImageBase64()
        this.imageUri = imageUri;
    }
}