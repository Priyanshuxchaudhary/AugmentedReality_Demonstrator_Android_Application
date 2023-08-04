package com.google.ar.sceneform.samples.videotexture;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.VideoNode;

import java.util.ArrayList;
import java.util.List;

public class VideoMainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener {

    // List to keep track of MediaPlayer instances for the videos
    private final List<MediaPlayer> mediaPlayers = new ArrayList<>();

    // ARFragment to display the AR scene
    private ArFragment arFragment;

    // Selected mode for video rendering, initialized with the ID of the "Plain Video" menu item
    private int mode = R.id.menuPlainVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for this activity
        setContentView(R.layout.video_activity_main);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            ((ViewGroup.MarginLayoutParams) toolbar.getLayoutParams()).topMargin = insets
                    .getInsets(WindowInsetsCompat.Type.systemBars())
                    .top;

            return WindowInsetsCompat.CONSUMED;
        });

        // Add the ARFragment to the activity
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        // Check if Sceneform is supported on this device
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                // Add ARFragment to the layout container
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        // Check if the attached fragment is the ARFragment
        if (fragment.getId() == R.id.arFragment) {

            // Initialize the ARFragment and set the tap listener
            arFragment = (ArFragment) fragment;
            arFragment.setOnTapArPlaneListener(this);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu resource for the activity
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Toggle the checked state of the selected menu item and update the selected mode
        item.setChecked(!item.isChecked());
        this.mode = item.getItemId();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start all the MediaPlayer instances when the activity starts
        for (MediaPlayer mediaPlayer : this.mediaPlayers) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Pause all the MediaPlayer instances when the activity stops
        for (MediaPlayer mediaPlayer : this.mediaPlayers) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop and reset all the MediaPlayer instances when the activity is destroyed
        for (MediaPlayer mediaPlayer : this.mediaPlayers) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode modelNode = new TransformableNode(arFragment.getTransformationSystem());
        modelNode.setParent(anchorNode);

        // Set default rawResId and chromaKeyColor (To be updated based on the selected mode)
        final int rawResId;
        final Color chromaKeyColor;
        rawResId = R.raw.silicon_valley;
        chromaKeyColor = null;

        // Create a MediaPlayer for the selected video and start playback
        MediaPlayer player = MediaPlayer.create(this, rawResId);
        player.setLooping(true);
        player.start();

        // Add the MediaPlayer to the list
        mediaPlayers.add(player);

        // Create a VideoNode to display the video on the AR scene
        VideoNode videoNode = new VideoNode(this, player, chromaKeyColor, new VideoNode.Listener() {
            @Override
            public void onCreated(VideoNode videoNode) {}

            @Override
            public void onError(Throwable throwable) {

                // Display a Toast if there is an error
                Toast.makeText(VideoMainActivity.this, "Unable to load material", Toast.LENGTH_LONG).show();
            }
        });
        videoNode.setParent(modelNode);

        // If you want that the VideoNode is always looking to the
        // Camera (You) comment the next line out.
        //videoNode.setRotateAlwaysToCamera(true);

        modelNode.select();
    }
}
