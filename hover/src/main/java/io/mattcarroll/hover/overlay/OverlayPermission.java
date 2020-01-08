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
package io.mattcarroll.hover.overlay;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * Provides queries and actions that are required for dealing with the user permission to display
 * window overlays. Attempting to display a window overlay without permission results in a crash.
 */
public class OverlayPermission {

    /**
     * Does this app have permission to display Views as an overlay above all other apps?
     *
     * @param context context
     * @return true if overlay drawing is permitted, false otherwise
     */
    public static boolean hasRuntimePermissionToDrawOverlay(@NonNull Context context) {
        //noinspection SimplifiableIfStatement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Runtime permissions are required. Check for the draw overlay permission.
            return Settings.canDrawOverlays(context);
        } else {
            // No runtime permissions required. We're all good.
            return true;
        }
    }

    /**
     * Starting with Android M, a runtime permission is required to be able to display UI elements
     * as an overlay above all other apps.  This method creates and returns an Intent that prompts
     * the user for this permission.
     *
     * @param context context
     * @return Intent to launch permission prompt
     */
    @RequiresApi(Build.VERSION_CODES.M)
    @NonNull
    public static Intent createIntentToRequestOverlayPermission(@NonNull Context context) {
        return new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName())
        );
    }

    private OverlayPermission() {
        // Utility class.
    }
}
