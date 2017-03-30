package io.mattcarroll.hoverdemo.nonfullscreen;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_launch_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DemoHoverMenuService.showFloatingMenu(getApplicationContext());
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent drawOverlaysSettingsIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                drawOverlaysSettingsIntent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(drawOverlaysSettingsIntent);
            }
        }
    }
}
