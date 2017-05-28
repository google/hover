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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.content.Navigator;
import io.mattcarroll.hover.defaulthovermenu.ExitListener;
import io.mattcarroll.hover.defaulthovermenu.HoverMenuView;

/**
 * {@link HoverMenu} implementation that displays within a {@code Window}.
 */
public class WindowHoverMenu implements HoverMenu {

    private static final String TAG = "WindowHoverMenu";

    private WindowManager mWindowManager;
    private WindowViewController mWindowViewController; // Shows/hides/positions Views in a Window.
    private HoverMenuView mHoverMenuView;
    private boolean mIsShowingHoverMenu; // Are we currently display mHoverMenuView?
    private boolean mIsInDragMode; // If we're not in drag mode then we're in menu mode.
    private Set<OnExitListener> mOnExitListeners = new HashSet<>();

    public WindowHoverMenu(@NonNull Context context,
                           @NonNull WindowManager windowManager,
                           @Nullable Navigator navigator,
                           @Nullable String savedVisualState,
                           @Nullable SharedPreferences savedInstanceState) {
        mWindowManager = windowManager;
        mWindowViewController = new WindowViewController(windowManager);

        PointF anchorState = new PointF(2, 0.5f); // Default to right side, half way down. See CollapsedMenuAnchor.
        if (null != savedVisualState) {
            try {
                VisualStateMemento visualStateMemento = VisualStateMemento.fromJsonString(savedVisualState);
                anchorState.set(visualStateMemento.getAnchorSide(), visualStateMemento.getNormalizedPositionY());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mHoverMenuView = HoverMenuView.createForWindow(context, savedInstanceState, mWindowViewController);
        mHoverMenuView.enableDebugMode(true);
        mHoverMenuView.setExitListener(new ExitListener() {
            @Override
            public void onExit() {
                Log.d(TAG, "Hover menu has exited. Hiding from window.");
                hide();
            }
        });

        mHoverMenuView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                String rotation = "";
                switch (mWindowManager.getDefaultDisplay().getRotation()) {
                    case Surface.ROTATION_0:
                        rotation = "portrait";
                        break;
                    case Surface.ROTATION_90:
                        rotation = "landscape";
                        break;
                    case Surface.ROTATION_180:
                        rotation = "upside down portrait";
                        break;
                    case Surface.ROTATION_270:
                        rotation = "upside down landscape";
                        break;
                }

                Log.d(TAG, "Rotation: " + rotation);
            }
        });
    }

    @Override
    public String getVisualState() {
//        PointF anchor = mHoverMenuView.getAnchorState();
//        return new VisualStateMemento((int) anchor.x, anchor.y).toJsonString();
        return null;
    }

    @Override
    public void restoreVisualState(@NonNull String savedVisualState) {
//        try {
//            VisualStateMemento memento = VisualStateMemento.fromJsonString(savedVisualState);
//            mHoverMenuView.setAnchorState(new PointF(memento.getAnchorSide(), memento.getNormalizedPositionY()));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void setAdapter(@Nullable HoverMenuAdapter adapter) {
        mHoverMenuView.setAdapter(adapter);
    }

    /**
     * Initializes and displays the Hover menu. To destroy and remove the Hover menu, use {@link #hide()}.
     */
    @Override
    public void show() {
        if (!mIsShowingHoverMenu) {
            mWindowViewController.addView(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    false,
                    mHoverMenuView
            );

            // Sync our control state with the HoverMenuView state.
//            if (mHoverMenuView.isExpanded()) {
                mWindowViewController.makeTouchable(mHoverMenuView);
//            } else {
//                collapseMenu();
//            }

            mIsShowingHoverMenu = true;
        }
    }

    /**
     * Exits the Hover menu system. This method is the inverse of {@link #show()}.
     */
    @Override
    public void hide() {
        if (mIsShowingHoverMenu) {
            mIsShowingHoverMenu = false;

            // Notify our exit listeners that we're exiting.
            notifyOnExitListeners();

            // Cleanup the control structures and Views.
            mWindowViewController.removeView(mHoverMenuView);
            mHoverMenuView.release();
        }
    }

    /**
     * Expands the Hover menu to show all of its tabs and a content area for the selected tab. To
     * collapse the menu down a single active tab, use {@link #collapseMenu()}.
     */
    @Override
    public void expandMenu() {
        if (mIsInDragMode) {
//            mHoverMenuView.expand();
        }
    }

    /**
     * Collapses the Hover menu down to its single active tab and allows the tab to be dragged
     * around the display. This method is the inverse of {@link #expandMenu()}.
     */
    @Override
    public void collapseMenu() {
        if (!mIsInDragMode) {
//            mHoverMenuView.setHoverMenuTransitionListener(mHoverMenuTransitionListener);
//            mHoverMenuView.collapse();
        }
    }

    @Override
    public HoverMenuView getHoverMenuView() {
        return mHoverMenuView;
    }

    @Override
    public void addOnExitListener(@NonNull OnExitListener onExitListener) {
        mOnExitListeners.add(onExitListener);
    }

    @Override
    public void removeOnExitListener(@NonNull OnExitListener onExitListener) {
        mOnExitListeners.remove(onExitListener);
    }

    private void notifyOnExitListeners() {
        for (OnExitListener listener : mOnExitListeners) {
            listener.onExitByUserRequest();
        }
    }

    private static class VisualStateMemento {

        private static final String JSON_KEY_ANCHOR_SIDE = "anchor_side";
        private static final String JSON_KEY_NORMALIZED_POSITION_Y = "normalized_position_y";

        public static VisualStateMemento fromJsonString(@NonNull String jsonString) throws JSONException {
            JSONObject jsonObject = new JSONObject(jsonString);
            int anchorSide = jsonObject.getInt(JSON_KEY_ANCHOR_SIDE);
            float normalizedPositionY = (float) jsonObject.getDouble(JSON_KEY_NORMALIZED_POSITION_Y);
            return new VisualStateMemento(anchorSide, normalizedPositionY);
        }

        private int mAnchorSide;
        private float mNormalizedPositionY;

        public VisualStateMemento(int anchorSide, float normalizedPositionY) {
            mAnchorSide = anchorSide;
            mNormalizedPositionY = normalizedPositionY;
        }

        public int getAnchorSide() {
            return mAnchorSide;
        }

        public float getNormalizedPositionY() {
            return mNormalizedPositionY;
        }

        public String toJsonString() {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(JSON_KEY_ANCHOR_SIDE, mAnchorSide);
                jsonObject.put(JSON_KEY_NORMALIZED_POSITION_Y, mNormalizedPositionY);
                return jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
