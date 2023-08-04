package com.example.arapp;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// MyAdapter class extends RecyclerView.Adapter and is used to display data in a RecyclerView.
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    //Constants representing package names and main activities of different AR-related apps.
    public static final String modelPackage = "com.google.ar.sceneform.samples.gltf";
    public static final String modelActivity = "com.google.ar.sceneform.samples.gltf.ModelMainActivity";

    public static final String imagePackage = "com.google.ar.sceneform.samples.augmentedimages";
    public static final String imageActivity = "com.google.ar.sceneform.samples.augmentedimages.ImageMainActivity";

    public static final String videoPackage = "com.google.ar.sceneform.samples.videotexture";
    public static final String videoActivity = "com.google.ar.sceneform.samples.videotexture.VideoMainActivity";

    // Data source for the adapter, an array of MenuModel objects.
    public static MenuModel[] listData;

    // Constructor to initialize the adapter with the data source.
    public CustomAdapter(MenuModel[] listData) {
        this.listData = listData;
    }

    // MyViewHolder class holds the views for each item in the RecyclerView.
    public static class MyViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;
        public TextView textView;

        // Constructor for MyViewHolder to bind the views for each item.
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views (ImageView and TextView) for each item in the RecyclerView.
            this.imageView=itemView.findViewById(R.id.imageView);
            this.textView=itemView.findViewById(R.id.textView);

            // Set a click listener to each item in the RecyclerView.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the position of the clicked item.
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {

                        // Get the corresponding MenuModel object for the clicked item.
                        MenuModel clickedItem = listData[position];

                        // Determine the package name and main activity for launching AR-related apps
                        // based on the title of the clicked item.
                        String packageName = null;
                        String mainActivity = null;

                        if (clickedItem.getTitle().equals("Tomb of Tự Đức")) {
                            packageName = modelPackage;
                            mainActivity = modelActivity;
                        }
                        else if (clickedItem.getTitle().equals("Silicon Valley")) {
                            packageName = videoPackage;
                            mainActivity = videoActivity;
                        }
                        else if (clickedItem.getTitle().equals("Delhi tour")) {
                            packageName = imagePackage;
                            mainActivity = imageActivity;
                        }
                        // Create an Intent to launch the AR-related app with the specified package and activity.
                        Intent intent = new Intent();
                        intent.setClassName(packageName, mainActivity);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        // Start the other application
                        v.getContext().startActivity(intent);
                    }
                }
            });
        }
    }
    //implementation of methods:


    // onCreateViewHolder is called when the RecyclerView needs a new ViewHolder (MyViewHolder).
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Inflate the layout for each item in the RecyclerView.
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View listItem= inflater.inflate(R.layout.recyclerview_item,parent,false);

        // Create and return a new instance of MyViewHolder, which holds the views for each item.
        MyViewHolder viewHolder = new MyViewHolder(listItem);
        return viewHolder;
    }

    // onBindViewHolder is called to bind the data to a ViewHolder at the specified position.
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        // Get the MenuModel object at the current position.
        final MenuModel myListData = listData[position];

        // Set the title and image of the item to the corresponding views in the ViewHolder.
        holder.textView.setText(listData[position].getTitle());
        holder.imageView.setImageResource(listData[position].getImage());

        // Set the position of the item in the MenuModel object.
        myListData.setPosition(position);
    }

    @Override
    public int getItemCount() {
        return listData.length;
    }


}
