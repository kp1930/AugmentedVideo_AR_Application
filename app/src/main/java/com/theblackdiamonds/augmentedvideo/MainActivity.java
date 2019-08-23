package com.theblackdiamonds.augmentedvideo;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ModelRenderable videoRenderable;
    private float HEIGHT = 1.25f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExternalTexture texture = new ExternalTexture();

        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.video);
        mediaPlayer.setSurface(texture.getSurface());
        mediaPlayer.setLooping(true);

        ModelRenderable.builder().setSource(MainActivity.this, R.raw.video_screen).build()
                .thenAccept(modelRenderable -> {
                    videoRenderable = modelRenderable;
                    videoRenderable.getMaterial().setExternalTexture("videoTexture", texture);
                    videoRenderable.getMaterial().setFloat4("keyColor", new Color(0.01843f, 1.0f, 0.098f));
                });

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        assert arFragment != null;
        arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());

            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();

                texture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
                    anchorNode.setRenderable(videoRenderable);
                    texture.getSurfaceTexture().setOnFrameAvailableListener(null);
                });
            } else {
                anchorNode.setRenderable(videoRenderable);
            }
            float width = mediaPlayer.getVideoWidth();
            float height = mediaPlayer.getVideoHeight();

            anchorNode.setLocalScale(new Vector3(HEIGHT * (width / height), HEIGHT, 1.0f));
            arFragment.getArSceneView().getScene().addChild(anchorNode);
        }));
    }
}