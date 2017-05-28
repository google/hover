package io.mattcarroll.hover.hoverdemo.helloworld;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.codecanon.hover.hoverdemo.helloworld.R;

import io.mattcarroll.hover.overlay.OverlayPermission;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_HOVER_PERMISSION = 1000;

    private boolean mPermissionsRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_launch_hover_single_section).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHoverIntent = HelloWorldHoverMenuService.intentForSingleSection(MainActivity.this);
                startService(startHoverIntent);
            }
        });

        findViewById(R.id.button_launch_hover_multi_sections).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHoverIntent = HelloWorldHoverMenuService.intentForMultiSection(MainActivity.this);
                startService(startHoverIntent);
            }
        });

        findViewById(R.id.button_launch_hover_changing_sections).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHoverIntent = HelloWorldHoverMenuService.intentForChangingSections(MainActivity.this);
                startService(startHoverIntent);
            }
        });

        findViewById(R.id.button_launch_hover_reordering_sections).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHoverIntent = HelloWorldHoverMenuService.intentForReorderingSections(MainActivity.this);
                startService(startHoverIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // On Android M and above we need to ask the user for permission to display the Hover
        // menu within the "alert window" layer.  Use OverlayPermission to check for the permission
        // and to request it.
        if (!mPermissionsRequested && !OverlayPermission.hasRuntimePermissionToDrawOverlay(this)) {
            @SuppressWarnings("NewApi")
            Intent myIntent = OverlayPermission.createIntentToRequestOverlayPermission(this);
            startActivityForResult(myIntent, REQUEST_CODE_HOVER_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE_HOVER_PERMISSION == requestCode) {
            mPermissionsRequested = true;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
