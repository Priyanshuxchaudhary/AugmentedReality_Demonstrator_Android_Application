package com.example.arapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    //Adapter View:Recycler View
    RecyclerView recyclerView;

    //Data Source
    MenuModel[] myListData;

    //adapter
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView=findViewById(R.id.recyclerView);
        myListData = new MenuModel[]{
                new MenuModel("Eiffel Tower",R.drawable.eiffeltoweimg),
                new MenuModel("Silicon Valley",R.drawable.siliconvalleyimg),
                new MenuModel("Taj Mahal",R.drawable.tajmahalimg),
                new MenuModel("Statue Of Liberty",R.drawable.statuelibertyimg),
                new MenuModel("Eiffel Tower",R.drawable.delhiimg)
        };

        //Adapter
        adapter = new MyAdapter(myListData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }
}