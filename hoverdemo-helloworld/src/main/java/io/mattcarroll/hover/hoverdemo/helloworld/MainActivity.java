package io.mattcarroll.hover.hoverdemo.helloworld;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.codecanon.hover.hoverdemo.helloworld.R;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_HOVER_PERMISSION = 1000;

    private boolean mPermissionsRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_launch_hover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHoverIntent = new Intent(MainActivity.this, HelloWorldHoverMenuService.class);
                startService(startHoverIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // On Android M and above we need to ask the user for permission to display the Hover
        // menu within the "alert window" layer.
        if (!mPermissionsRequested && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean needsPermissionToShowHover = !Settings.canDrawOverlays(getApplicationContext());
            if (needsPermissionToShowHover) {
                Intent myIntent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                );
                startActivityForResult(myIntent, REQUEST_CODE_HOVER_PERMISSION);
            }
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
