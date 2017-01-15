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
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.R;

/**
 * HoverMenu content area that shows a tab selector above the content area, and a {@code Toolbar} at the
 * top of the content area.  The content area itself can display anything provided by a given
 * {@link NavigatorContent}.
 */
public class HoverMenuContentView extends FrameLayout {

    private static final String TAG = "HoverMenuContentView";

    private HoverMenuTabSelectorView mTabSelectorView;
    private View mSelectedTabView;
    private FrameLayout mContentView;
    private Drawable mContentBackground;
    private Navigator mNavigator;
    // We need to update the tab selector position every draw frame so that animations don't result in a bad selector position.
    private ViewTreeObserver.OnDrawListener mOnDrawListener;
    // This version of the listener is for compatibility with API level 15.
    private ViewTreeObserver.OnPreDrawListener mOnPreDrawListener;

    public HoverMenuContentView(Context context) {
        super(context);
        init();
    }

    public HoverMenuContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_hover_menu_content, this, true);

        int backgroundCornerRadiusPx = (int) getResources().getDimension(R.dimen.popup_corner_radius);
        mTabSelectorView = (HoverMenuTabSelectorView) findViewById(R.id.tabselector);
        mTabSelectorView.setPadding(backgroundCornerRadiusPx, 0, backgroundCornerRadiusPx, 0);

        mContentView = (FrameLayout) findViewById(R.id.view_content_container);
        mContentBackground = ContextCompat.getDrawable(getContext(), R.drawable.round_rect_white);
        mContentView.setBackgroundDrawable(mContentBackground);

        createSelectedTabDrawListener();
    }

    private void createSelectedTabDrawListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mOnDrawListener = new ViewTreeObserver.OnDrawListener() {
                @Override
                public void onDraw() {
                    updateTabSelectorPosition();
                }
            };
        } else {
            mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    updateTabSelectorPosition();
                    return true;
                }
            };
        }
    }

    /**
     * Positions the selector triangle below the center of the given {@code tabView}.
     *
     * @param tabView the tab with which this content view will align its selector
     */
    public void setActiveTab(@NonNull View tabView) {
        detachSelectedTabOnDrawListener();
        mSelectedTabView = tabView;
        attachSelectedTabOnDrawListener();
        updateTabSelectorPosition();
    }

    public void setNavigator(@Nullable Navigator navigator) {
        if (null != mNavigator) {
            mContentView.removeView(mNavigator.getView());
        }

        if (null != navigator) {
            mNavigator = navigator;
            mContentView.addView(navigator.getView());
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        // Forward the call on to our constituent pieces.
        mTabSelectorView.setSelectorColor(color);
        mContentBackground.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        // Don't allow setting a background.
    }

    @Override
    public void setBackgroundResource(int resid) {
        // Don't allow setting a background.
    }

    @Override
    public void setBackground(Drawable background) {
        // Don't allow setting a background.
    }

    /**
     * Tries to handle a back-press.
     * @return true if the back-press was handled, false otherwise
     */
    public boolean onBackPressed() {
        if (null != mNavigator) {
            return mNavigator.popContent();
        } else {
            return false;
        }
    }

    private void updateTabSelectorPosition() {
        if (null != mSelectedTabView) {
            Rect tabBounds = new Rect();
            mSelectedTabView.getGlobalVisibleRect(tabBounds);
            int globalTabCenter = tabBounds.centerX();
            mTabSelectorView.setSelectorPosition(globalTabCenter);
        }
    }

    private void attachSelectedTabOnDrawListener() {
        if (null != mSelectedTabView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mSelectedTabView.getViewTreeObserver().addOnDrawListener(mOnDrawListener);
            } else {
                mSelectedTabView.getViewTreeObserver().addOnPreDrawListener(mOnPreDrawListener);
            }
        }
    }

    private void detachSelectedTabOnDrawListener() {
        if (null != mSelectedTabView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mSelectedTabView.getViewTreeObserver().removeOnDrawListener(mOnDrawListener);
            } else {
                mSelectedTabView.getViewTreeObserver().removeOnPreDrawListener(mOnPreDrawListener);
            }
        }
    }

}
