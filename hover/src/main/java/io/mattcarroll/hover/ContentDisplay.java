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
package io.mattcarroll.hover;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Rectangular area that displays {@link Content}.  A {@code ContentDisplay} also renders a caret
 * that points at a tab.
 */
class ContentDisplay extends RelativeLayout {

    private static final String TAG = "ContentDisplay";

    private View mContainer;
    private FrameLayout mContentView;
    private Drawable mContentBackground;
    private TabSelectorView mTabSelectorView;
    private FloatingTab mSelectedTab;
    private Content mContent;
    private boolean mIsVisible = false;

    private final ViewTreeObserver.OnGlobalLayoutListener mMyVisibilityWatcher = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (mIsVisible && VISIBLE != getVisibility()) {
                mIsVisible = false;
                // Went from visible to not-visible. Hide tab selector to avoid visual artifacts
                // when we appear again.
                mTabSelectorView.setVisibility(INVISIBLE);
            } else {
                mIsVisible = true;
            }
        }
    };

    private final FloatingTab.OnPositionChangeListener mOnTabPositionChangeListener = new FloatingTab.OnPositionChangeListener() {
        @Override
        public void onPositionChange(@NonNull Point position) {
            Log.d(TAG, mSelectedTab + " tab moved to " + position);
            updateTabSelectorPosition();

            setPadding(0, position.y + (mSelectedTab.getTabSize() / 2), 0, 0);

            // We have received an affirmative position for the selected tab. Show tab selector.
            mTabSelectorView.setVisibility(VISIBLE);
        }

        @Override
        public void onDockChange(@NonNull Point dock) {
            // No-op.
        }
    };

    public ContentDisplay(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_hover_menu_content, this, true);

        mContainer = findViewById(R.id.container);
        expandToScreenBounds();

        int backgroundCornerRadiusPx = (int) getResources().getDimension(R.dimen.hover_navigator_corner_radius);
        mTabSelectorView = (TabSelectorView) findViewById(R.id.tabselector);
        mTabSelectorView.setPadding(backgroundCornerRadiusPx, 0, backgroundCornerRadiusPx, 0);

        mContentView = (FrameLayout) findViewById(R.id.view_content_container);
        mContentBackground = ContextCompat.getDrawable(getContext(), R.drawable.round_rect_white);
        mContentView.setBackgroundDrawable(mContentBackground);

        getViewTreeObserver().addOnGlobalLayoutListener(mMyVisibilityWatcher);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        setLayoutParams(layoutParams);
    }

    public void enableDebugMode(boolean debugMode) {
        if (debugMode) {
            setBackgroundColor(0x88FFFF00);
        } else {
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void selectedTabIs(@Nullable FloatingTab tab) {
        // Disconnect from old selected tab.
        if (null != mSelectedTab) {
            mSelectedTab.removeOnPositionChangeListener(mOnTabPositionChangeListener);
        }

        mSelectedTab = tab;

        // Connect to new selected tab.
        if (null != mSelectedTab) {
            updateTabSelectorPosition();
            mSelectedTab.addOnPositionChangeListener(mOnTabPositionChangeListener);
        } else {
            mTabSelectorView.setVisibility(INVISIBLE);
        }
    }

    private void updateTabSelectorPosition() {
        Point tabPosition = mSelectedTab.getPosition();
        Log.d(TAG, "Updating tab position to " + tabPosition.x);
        mTabSelectorView.setSelectorPosition(tabPosition.x);
    }

    public void displayContent(@Nullable Content content) {
        if (content == mContent) {
            // If content hasn't changed then we don't need to do anything.
            return;
        }

        if (null != mContent) {
            mContentView.removeView(mContent.getView());
            mContent.onHidden();
        }

        mContent = content;
        if (null != mContent) {
            mContentView.addView(mContent.getView());
            mContent.onShown();

            if (content.isFullscreen()) {
                expandToScreenBounds();
            } else {
                wrapContent();
            }
        }
    }

    public void expandToScreenBounds() {
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) mContainer.getLayoutParams();
        layoutParams.height = 0;
        layoutParams.addRule(ALIGN_PARENT_TOP);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM);
        mContainer.setLayoutParams(layoutParams);
    }

    public void wrapContent() {
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) mContainer.getLayoutParams();
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.addRule(ALIGN_PARENT_TOP);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM, 0); // This means "remove rule". Can't use removeRule() until API 17.
        mContainer.setLayoutParams(layoutParams);
    }
}
