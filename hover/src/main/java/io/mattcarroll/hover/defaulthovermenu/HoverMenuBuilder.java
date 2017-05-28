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
package io.mattcarroll.hover.defaulthovermenu;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.view.WindowManager;

import io.mattcarroll.hover.Hover;
import io.mattcarroll.hover.content.Navigator;
import io.mattcarroll.hover.defaulthovermenu.view.ViewHover;
import io.mattcarroll.hover.defaulthovermenu.window.WindowHover;

/**
 * Builds a {@link Hover}.
 */
public class HoverMenuBuilder {

    public static final int DISPLAY_MODE_WINDOW = 1; // Display directly in a window.
    public static final int DISPLAY_MODE_VIEW = 2; // Display within View hierarchy.

    private Context mContext;
    private int mDisplayMode = DISPLAY_MODE_WINDOW;
    private WindowManager mWindowManager;
    private Navigator mNavigator;
    private io.mattcarroll.hover.defaulthovermenu.HoverMenu mMenu;
    private String mSavedVisualState = null;
    private SharedPreferences mSavedInstanceState = null;

    public HoverMenuBuilder(@NonNull Context context) {
        mContext = context;
    }

    public HoverMenuBuilder displayWithinWindow() {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mDisplayMode = DISPLAY_MODE_WINDOW;
        return this;
    }

    public HoverMenuBuilder displayWithinView(@NonNull ViewGroup container) {
        mDisplayMode = DISPLAY_MODE_VIEW;
        return this;
    }

    public HoverMenuBuilder useNavigator(@Nullable Navigator navigator) {
        mNavigator = navigator;
        return this;
    }

    public HoverMenuBuilder useMenu(@Nullable io.mattcarroll.hover.defaulthovermenu.HoverMenu menu) {
        mMenu = menu;
        return this;
    }

    public HoverMenuBuilder restoreVisualState(@NonNull String visualState) {
        mSavedVisualState = visualState;
        return this;
    }

    public HoverMenuBuilder restoreState(@NonNull SharedPreferences savedInstanceState) {
        mSavedInstanceState = savedInstanceState;
        return this;
    }

    public Hover build() {
        if (DISPLAY_MODE_WINDOW == mDisplayMode) {
            WindowHover windowHoverMenu = new WindowHover(
                    mContext,
                    mWindowManager,
                    mNavigator,
                    mSavedInstanceState
            );
            windowHoverMenu.setMenu(mMenu);
            return windowHoverMenu;
        } else {
            ViewHover viewHoverMenu = new ViewHover(mContext, mSavedInstanceState);
            viewHoverMenu.setMenu(mMenu);
            return viewHoverMenu;
        }
    }

}
