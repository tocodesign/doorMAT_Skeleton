package com.example.doormat_skeleton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.os.*;
import android.widget.Button;

import com.example.doormat_skeleton.Helpers.CameraPermissionHelper;
import com.example.doormat_skeleton.Helpers.SnackbarHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.core.Config.CloudAnchorMode;


import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ViewMode extends AppCompatActivity {

    private ArFragment arFragment;
    private ModelRenderable sphereRenderable;
    private Anchor cloudAnchor;
    private Button clear;
    private FloatingActionButton back_btn;
    private Session session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_mode);


        Button clear = findViewById(R.id.clear);
        FloatingActionButton back_btn = findViewById(R.id.back_btn);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCloudAnchor(null);
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewMode.this, MapActivity.class);
                startActivity(intent);
            }
        });

        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.BLUE))
                .thenAccept(material -> {
                    sphereRenderable = ShapeFactory.makeSphere(0.1f, new Vector3(0.0f,0.15f, 0.0f ),material);
                });

        arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);


        assert arFragment != null;
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                        return;
                    }

                    Anchor anchor = hitResult.createAnchor();

                    setCloudAnchor(anchor);

                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    TransformableNode sphere = new TransformableNode(arFragment.getTransformationSystem());
                    sphere.setParent(anchorNode);
                    sphere.setRenderable(sphereRenderable);
                    sphere.select();
                }
        );
    }


    private void setCloudAnchor(Anchor newAnchor){
        if(cloudAnchor != null){
            cloudAnchor.detach();
        }

        cloudAnchor = newAnchor;

    }


    private void placeObject(ArFragment fragment, Anchor anchor, Uri model){
        ModelRenderable.builder()
                .setSource(fragment.getContext(), model)
                .build()
                .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                .exceptionally((throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .setTitle("Error!");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return null;
                }));

    }
    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable){
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

}



