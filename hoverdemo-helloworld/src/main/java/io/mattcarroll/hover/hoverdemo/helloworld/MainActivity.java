/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mattcarroll.hover.hoverdemo.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
                Intent startHoverIntent = new Intent(MainActivity.this, SingleSectionHoverMenuService.class);
                startService(startHoverIntent);
            }
        });

        findViewById(R.id.button_launch_hover_multi_sections).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHoverIntent = new Intent(MainActivity.this, MultipleSectionsHoverMenuService.class);
                startService(startHoverIntent);
            }
        });

        findViewById(R.id.button_launch_hover_foreground).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHoverIntent = new Intent(MainActivity.this, SingleSectionNotificationHoverMenuService.class);
                startService(startHoverIntent);
            }
        });

        findViewById(R.id.button_launch_hover_changing_sections).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHoverIntent = new Intent(MainActivity.this, MutatingSectionsHoverMenuService.class);
                startService(startHoverIntent);
            }
        });

        findViewById(R.id.button_launch_hover_reordering_sections).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHoverIntent = new Intent(MainActivity.this, ReorderingHoverMenuService.class);
                startService(startHoverIntent);
            }
        });

        findViewById(R.id.button_launch_hover_all_states).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHoverIntent = new Intent(MainActivity.this, AllStatesHoverMenuService.class);
                startService(startHoverIntent);
            }
        });

        findViewById(R.id.button_launch_hover_changing_menus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHoverIntent = new Intent(MainActivity.this, ChangingMenusHoverMenuService.class);
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
