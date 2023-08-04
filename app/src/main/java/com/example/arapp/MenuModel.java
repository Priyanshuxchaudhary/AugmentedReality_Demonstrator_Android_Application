package com.example.arapp;

public class MenuModel {
    private String title;
    private int image;

    public MenuModel(String title, int image) {
        this.title = title;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public int getImage() {
        return image;
    }
}
