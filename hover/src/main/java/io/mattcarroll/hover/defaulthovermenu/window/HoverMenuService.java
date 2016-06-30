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
package io.mattcarroll.hover.defaulthovermenu.window;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.defaulthovermenu.HoverMenuBuilder;

/**
 * {@code Service} that presents a {@code HoverMenu} within a {@code Window}.
 *
 * The Hover menu is displayed whenever any Intent is received by this {@code Service}. The Hover
 * menu is removed and destroyed whenever this {@code Service} is destroyed.
 *
 * A {@link Service} is required for displaying a {@code HoverMenu} in a {@code Window} because there
 * is no {@code Activity} to associate with the {@code HoverMenu}'s UI. This {@code Service} is the
 * application's link to the device's {@code Window} to display the {@code HoverMenu}.
 */
public abstract class HoverMenuService extends Service {

    private static final String TAG = "HoverMenuService";

    private static final String PREF_FILE = "hover_menu";
    private static final String PREF_HOVER_MENU_VISUAL_STATE = "hover_menu_visual_state";

    private HoverMenu mHoverMenu;
    private boolean mIsRunning;
    private SharedPreferences mPrefs;
    private HoverMenu.OnExitListener mWindowHoverMenuMenuExitListener = new HoverMenu.OnExitListener() {
        @Override
        public void onExitByUserRequest() {
            Log.d(TAG, "Menu exit requested.");
            savePreferredLocation();
            onHoverMenuExitingByUserRequest();
            stopSelf();
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        mPrefs = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

        mHoverMenu = new HoverMenuBuilder(getContextForHoverMenu())
                .displayWithinWindow()
                .useNavigator(createNavigator())
                .useAdapter(createHoverMenuAdapter())
                .restoreVisualState(loadPreferredLocation())
                .build();
        mHoverMenu.addOnExitListener(mWindowHoverMenuMenuExitListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mIsRunning) {
            Log.d(TAG, "onStartCommand() - showing Hover menu.");
            mIsRunning = true;
            mHoverMenu.show();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        mHoverMenu.hide();
        mIsRunning = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Hook for subclasses to return a custom Context to be used in the creation of the {@code HoverMenu}.
     * For example, subclasses might choose to provide a ContextThemeWrapper.
     *
     * @return context for HoverMenu initialization
     */
    protected Context getContextForHoverMenu() {
        return this;
    }

    /**
     * Subclasses can use this hook method to return a customized {@link Navigator} to be used
     * throughout the entire Hover menu. This {@link Navigator} will be used for every tab in the
     * Hover menu, so only supply a {@code Navigator} if you truly want every screen to display it.
     *
     * If you want only a portion of the screens in the Hover menu to look different, then consider
     * using composition of {@link NavigatorContent} to achieve the desired effect.  For example,
     * if you want a {@code Toolbar} to appear on one or more screens, consider placing your content
     * within a {@link ToolbarNavigatorContent}, and then add the {@code ToolbarNavigatorContent}
     * as the content of the default {@code Navigator}.
     *
     * @return Custom Navigator to use on every screen in the Hover menu
     */
    protected Navigator createNavigator() {
        return null; // Subclasses can override this to provide a custom Navigator.
    }

    abstract protected HoverMenuAdapter createHoverMenuAdapter();

    /**
     * Hook method for subclasses to take action when the user exits the HoverMenu. This method runs
     * just before this {@code HoverMenuService} calls {@code stopSelf()}.
     */
    protected void onHoverMenuExitingByUserRequest() {
        // Hook for subclasses.
    }

    private void savePreferredLocation() {
        String memento = mHoverMenu.getVisualState();
        mPrefs.edit().putString(PREF_HOVER_MENU_VISUAL_STATE, memento).apply();
    }

    private String loadPreferredLocation() {
        return mPrefs.getString(PREF_HOVER_MENU_VISUAL_STATE, null);
    }
}
