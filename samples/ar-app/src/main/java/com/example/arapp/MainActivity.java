package com.example.arapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    // RecyclerView for displaying a list of items
    RecyclerView recyclerView;

    // Data Source: An array of MenuModel objects
    MenuModel[] myListData;

    // Adapter for binding data to the RecyclerView
    CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the RecyclerView
        recyclerView=findViewById(R.id.recyclerView);

        // Create an array of MenuModel objects with some sample data
        myListData = new MenuModel[]{
                new MenuModel("Tomb of Tự Đức",R.drawable.tomb),
                new MenuModel("Silicon Valley",R.drawable.siliconvalleyimg),
                new MenuModel("Delhi tour",R.drawable.delhiimg)
        };

        // Create an instance of the custom adapter (MyAdapter) and pass the data source (myListData)
        adapter = new CustomAdapter(myListData);

        // - Set the fixed size to true as the RecyclerView won't change in size based on its content.
        // - Use a LinearLayoutManager to display items in a vertical list.
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Attach the adapter to the RecyclerView to display the data
        recyclerView.setAdapter(adapter);
    }

}