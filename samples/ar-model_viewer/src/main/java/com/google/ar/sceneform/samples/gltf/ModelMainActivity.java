package com.google.ar.sceneform.samples.gltf;

import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.lang.ref.WeakReference;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ModelMainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private ArFragment arFragment;
    private Renderable model;
    private ViewRenderable viewRenderable;

    private String[] modelNames = {"Tomb of Tự Đức", "Eiffel Tower"};
    private String[] modelResource = {"models/tomb.glb", "models/eiffel_tower.glb"};
    private int currentModelIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for this activity
        setContentView(R.layout.model_activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        // Check if Sceneform is supported on this device
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        // Load 3D models (Tomb and the title view)
        loadModels();

        // For setting the custom menu list view
        ListView menuList = findViewById(R.id.menuList);
        ArrayAdapter<String> menuAdapter = new ArrayAdapter<>(this, R.layout.menu_item, modelNames);
        menuList.setAdapter(menuAdapter);
        menuList.setOnItemClickListener((adapterView, view, position, id) -> {
            currentModelIndex = position;
            loadModels();
            closeOptionsMenu();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_icon) {
            ViewGroup menuContainer = findViewById(R.id.menuContainer);
            if (menuContainer.getVisibility() == View.VISIBLE) {
                menuContainer.setVisibility(View.GONE);
            } else {
                menuContainer.setVisibility(View.VISIBLE);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;

            // Set listeners for AR functionality
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        // Enable depth mode if supported by the device
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);

        // Set the frame rate to FULL for smooth rendering
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    public void loadModels() {
        // Use WeakReference to avoid memory leaks
        WeakReference<ModelMainActivity> weakActivity = new WeakReference<>(this);

        // Load the 3D model asynchronously using ModelRenderable.builder()
        ModelRenderable.builder()
                .setSource(this, Uri.parse( modelResource[currentModelIndex]))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    ModelMainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;

                        // Load the title view using ViewRenderable.builder()
                        ViewRenderable.builder()
                                .setView(activity, R.layout.title)
                                .build()
                                .thenAccept(viewRenderable -> {
                                    // Now both model and viewRenderable are loaded successfully
                                    activity.viewRenderable = viewRenderable;

                                    // Update the title view with the selected model name
                                    TextView titleView = findViewById(R.id.title);
                                    titleView.setText(activity.modelNames[activity.currentModelIndex]);
                                })
                                .exceptionally(throwable -> {
                                    Toast.makeText(activity, "Unable to load model", Toast.LENGTH_LONG).show();
                                    return null;
                                });
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

        // Check if the models have been loaded successfully
        if (model == null || viewRenderable==null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }


        // Create the Anchor at the tap location on the detected plane
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(this.model)
                .animate(true).start();
        model.select();

        // Create a Node for the title view and attach it to the 3D model
        Node titleNode = new Node();
        titleNode.setParent(model);
        titleNode.setEnabled(false);
        titleNode.setLocalPosition(new Vector3(0.0f, 1.0f, 0.0f));
        titleNode.setRenderable(viewRenderable);
        titleNode.setEnabled(true);
    }
}
