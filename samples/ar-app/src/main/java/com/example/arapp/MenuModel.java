package com.example.arapp;

// This is the class that holds the properties of the elements of the custom view
public class MenuModel {
    private String title;
    private int image;
    private int position;

    // Constructor to initialize the MenuModel object with a title and an image
    public MenuModel(String title, int image) {
        this.title = title;
        this.image = image;
    }

    // Setter method for setting the position of the element in the custom view
    public void setPosition(int position) {
        this.position = position;
    }

    // Getter method to retrieve the position of the element in the custom view
    public int getPosition() {
        return position;
    }

    // Getter method to retrieve the title of the element
    public String getTitle() {
        return title;
    }

    // Getter method to retrieve the image resource ID associated with the element
    public int getImage() {
        return image;
    }
}
