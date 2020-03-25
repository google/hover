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
package io.mattcarroll.hover.window;

import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * Controls {@code View}s' positions, visibility, etc within a {@code Window}.
 */
public class WindowViewController {

    private WindowManager mWindowManager;

    public WindowViewController(@NonNull WindowManager windowManager) {
        mWindowManager = windowManager;
    }

    public void addViewBackground(int width, int height, boolean isTouchable, @NonNull View view) {
        // If this view is untouchable then add the corresponding flag, otherwise set to zero which
        // won't have any effect on the OR'ing of flags.
        int touchableFlag = isTouchable ? 0 : WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        int windowType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width,
                height,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | touchableFlag,
                PixelFormat.TRANSLUCENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            params.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        mWindowManager.addView(view, params);
    }

    public void addView(int width, int height, boolean isTouchable, @NonNull View view) {
        // If this view is untouchable then add the corresponding flag, otherwise set to zero which
        // won't have any effect on the OR'ing of flags.
        int touchableFlag = isTouchable ? 0 : WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        int windowType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width,
                height,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | touchableFlag,
                PixelFormat.TRANSLUCENT
        );

        if (Build.VERSION.SDK_INT >= 19) {
            params.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
        view.setFitsSystemWindows(true);
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        mWindowManager.addView(view, params);
    }

    public void addViewDragger(int width, int height, boolean isTouchable, @NonNull View view) {
        // If this view is untouchable then add the corresponding flag, otherwise set to zero which
        // won't have any effect on the OR'ing of flags.
        int touchableFlag = isTouchable ? 0 : WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        int windowType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width,
                height,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | touchableFlag,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        mWindowManager.addView(view, params);
    }

    public Point getScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        return new Point(width, height);
    }

    public void removeView(@NonNull View view) {
        if (null != view.getParent()) {
            mWindowManager.removeView(view);
        }
    }

    public Point getViewPosition(@NonNull View view) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
        return new Point(params.x, params.y);
    }

    public void moveViewTo(View view, int x, int y) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
        params.x = x;
        params.y = y;
        mWindowManager.updateViewLayout(view, params);
    }

    public void showView(View view) {
        try {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
            mWindowManager.addView(view, params);
        } catch (IllegalStateException e) {
            // The view is already visible.
        }
    }

    public void hideView(View view) {
        try {
            mWindowManager.removeView(view);
        } catch (IllegalArgumentException e) {
            // The View wasn't visible to begin with.
        }
    }

    public void makeTouchable(View view) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
        params.flags = params.flags & ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE & ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowManager.updateViewLayout(view, params);
    }

    public void makeUntouchable(View view) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
        params.flags = params.flags | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowManager.updateViewLayout(view, params);
    }

}
