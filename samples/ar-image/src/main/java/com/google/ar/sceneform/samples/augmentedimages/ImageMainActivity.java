package com.google.ar.sceneform.samples.augmentedimages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.android.filament.Engine;
import com.google.android.filament.filamat.MaterialBuilder;
import com.google.android.filament.filamat.MaterialPackage;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.EngineInstance;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.RenderableInstance;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.InstructionsController;
import com.google.ar.sceneform.ux.TransformableNode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ImageMainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnSessionConfigurationListener {

    // List to store futures for asynchronous loading of models and materials
    private final List<CompletableFuture<Void>> futures = new ArrayList<>();

    // ARFragment for handling AR interactions
    private ArFragment arFragment;

    // Flag to track if the "Delhi" image is detected
    private boolean delhi_Detected = false;

    // AugmentedImageDatabase to store images for AR detection
    private AugmentedImageDatabase database;

    // Renderable for the video model
    private Renderable plainVideoModel;

    // Material for the video model
    private Material plainVideoMaterial;

    // MediaPlayer to play the video
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for the activity
        setContentView(R.layout.activity_main);

        // Find the Toolbar view and adjust its top margin to accommodate the system status bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            ((ViewGroup.MarginLayoutParams) toolbar.getLayoutParams()).topMargin = insets
                    .getInsets(WindowInsetsCompat.Type.systemBars())
                    .top;
            return WindowInsetsCompat.CONSUMED;
        });

        // Attach the FragmentOnAttachListener to the fragment manager
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        // Check if Sceneform is supported on the device and initialize the ARFragment if supported
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        // Load the 3D video model and its material
        if (Sceneform.isSupported(this)) {
            loadModel();
            loadMaterial();
        }
    }

    // Method called when a Fragment is attached to the FragmentManager
    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
        }
    }

    // Method called when the AR session configuration is being set
    @Override
    public void onSessionConfiguration(Session session, Config config) {
        // Disable plane detection to avoid detecting surfaces
        config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);

        // Create a new AugmentedImageDatabase to store the image(s) for AR detection
        database = new AugmentedImageDatabase(session);

        // Load the "Delhi" image from resources and add it to the database with a unique identifier
        Bitmap delhiImage = BitmapFactory.decodeResource(getResources(), R.drawable.delhi);
        database.addImage("delhi", delhiImage);

        // Set the AugmentedImageDatabase in the session configuration
        config.setAugmentedImageDatabase(database);

        // Set the ARFragment's augmented image update listener to this class's onAugmentedImageTrackingUpdate method
        arFragment.setOnAugmentedImageUpdateListener(this::onAugmentedImageTrackingUpdate);
    }

    // Method called when the activity is being destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel any incomplete futures
        futures.forEach(future -> {
            if (!future.isDone())
                future.cancel(true);
        });

        // Release and stop the MediaPlayer if it is playing
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    // Method to load the 3D video model from a file (GLB format)
    private void loadModel() {
        futures.add(ModelRenderable.builder()
                .setSource(this, Uri.parse("models/delhi.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(model -> {
                    // Removing shadows for this Renderable to optimize performance
                    model.setShadowCaster(false);
                    model.setShadowReceiver(true);
                    plainVideoModel = model;
                })
                .exceptionally(
                        throwable -> {
                            Toast.makeText(this, "Unable to load renderable", Toast.LENGTH_LONG).show();
                            return null;
                        }));
    }

    // Method to load the material for the 3D video model
    private void loadMaterial() {
        // Get the Filament engine instance
        Engine filamentEngine = EngineInstance.getEngine().getFilamentEngine();

        // Initialize the MaterialBuilder
        MaterialBuilder.init();

        // Create a MaterialBuilder for the external video material
        MaterialBuilder materialBuilder = new MaterialBuilder()
                .platform(MaterialBuilder.Platform.MOBILE)
                .name("External Video Material")
                .require(MaterialBuilder.VertexAttribute.UV0)
                .shading(MaterialBuilder.Shading.UNLIT)
                .doubleSided(true)
                .samplerParameter(MaterialBuilder.SamplerType.SAMPLER_EXTERNAL, MaterialBuilder.SamplerFormat.FLOAT, MaterialBuilder.ParameterPrecision.DEFAULT, "videoTexture")
                .optimization(MaterialBuilder.Optimization.NONE);

        // Build the material package for the external video material
        MaterialPackage plainVideoMaterialPackage = materialBuilder
                .blending(MaterialBuilder.BlendingMode.OPAQUE)
                .material("void material(inout MaterialInputs material) {\n" +
                        "    prepareMaterial(material);\n" +
                        "    material.baseColor = texture(materialParams_videoTexture, getUV0()).rgba;\n" +
                        "}\n")
                .build(filamentEngine);

        // Check if the material package is valid and create the Material
        if (plainVideoMaterialPackage.isValid()) {
            ByteBuffer buffer = plainVideoMaterialPackage.getBuffer();
            futures.add(Material.builder()
                    .setSource(buffer)
                    .build()
                    .thenAccept(material -> {
                        plainVideoMaterial = material;
                    })
                    .exceptionally(
                            throwable -> {
                                Toast.makeText(this, "Unable to load material", Toast.LENGTH_LONG).show();
                                return null;
                            }));
        }

        // Shutdown the MaterialBuilder
        MaterialBuilder.shutdown();
    }

    // Method called when an augmented image is being tracked and updated
    public void onAugmentedImageTrackingUpdate(AugmentedImage augmentedImage) {
        // If the "Delhi" image is already detected, return to optimize CPU usage
        if (delhi_Detected) {
            return;
        }

        // Check if the augmented image is being tracked with full tracking
        if (augmentedImage.getTrackingState() == TrackingState.TRACKING
                && augmentedImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING) {

            // Create an AnchorNode at the center of the detected augmented image
            AnchorNode anchorNode = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));

            // Check if the "Delhi" image is detected and process accordingly
            if (!delhi_Detected && augmentedImage.getName().equals("delhi")) {
                delhi_Detected = true;
                Toast.makeText(this, "Delhi tag detected", Toast.LENGTH_LONG).show();

                // Set the world scale of the AnchorNode to the size of the detected image
                anchorNode.setWorldScale(new Vector3(augmentedImage.getExtentX(), 1f, augmentedImage.getExtentZ()));

                // Add the AnchorNode to the AR scene
                arFragment.getArSceneView().getScene().addChild(anchorNode);

                // Create a TransformableNode to hold the video model and set its rotation to display correctly
                TransformableNode videoNode = new TransformableNode(arFragment.getTransformationSystem());
                videoNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 180f));
                anchorNode.addChild(videoNode);

                // Create an ExternalTexture to set the video texture
                ExternalTexture externalTexture = new ExternalTexture();
                RenderableInstance renderableInstance = videoNode.setRenderable(plainVideoModel);
                renderableInstance.setMaterial(plainVideoMaterial);

                // Set the video texture to the material's external texture
                renderableInstance.getMaterial().setExternalTexture("videoTexture", externalTexture);

                // Create and set up the MediaPlayer to play the video
                mediaPlayer = MediaPlayer.create(this, R.raw.delhi);
                mediaPlayer.setLooping(true);
                mediaPlayer.setSurface(externalTexture.getSurface());
                mediaPlayer.start();
            }
        }

        // If the "Delhi" image is detected, disable the instructions for scanning more images
        if (delhi_Detected) {
            arFragment.getInstructionsController().setEnabled(
                    InstructionsController.TYPE_AUGMENTED_IMAGE_SCAN, false);
        }
    }
}