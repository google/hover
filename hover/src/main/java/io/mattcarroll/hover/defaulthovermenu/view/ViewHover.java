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
package io.mattcarroll.hover.defaulthovermenu.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.HashSet;
import java.util.Set;

import io.mattcarroll.hover.BuildConfig;
import io.mattcarroll.hover.Hover;
import io.mattcarroll.hover.R;
import io.mattcarroll.hover.defaulthovermenu.HoverMenu;
import io.mattcarroll.hover.defaulthovermenu.HoverMenuView;

/**
 * {@link Hover} implementation that can be embedded in traditional view hierarchies.
 */
public class ViewHover extends FrameLayout implements Hover {

    private static final String TAG = "ViewHover";

    private static final String PREFS_FILE = "viewhovermenu";

    private HoverMenuView mHoverMenuView;
    private InViewGroupDragger mDragger;
    private HoverMenu mMenu;
    private SharedPreferences mPrefs;
    private Set<OnExitListener> mOnExitListeners = new HashSet<>();

    public ViewHover(Context context, @Nullable SharedPreferences savedInstanceState) {
        super(context);
        init(savedInstanceState);
    }

    public ViewHover(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(null);
    }

    private void init(@Nullable SharedPreferences savedInstanceState) {
        mPrefs = getContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

        int touchDiameter = getResources().getDimensionPixelSize(R.dimen.exit_radius);
        mDragger = new InViewGroupDragger(this, touchDiameter, ViewConfiguration.get(getContext()).getScaledTouchSlop());
        mDragger.enableDebugMode(BuildConfig.DEBUG);
        mHoverMenuView = HoverMenuView.createForView(getContext(), savedInstanceState, this);
        mHoverMenuView.setId(R.id.hovermenu);
        addView(mHoverMenuView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (null != mMenu) {
            mHoverMenuView.setMenu(mMenu);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        removeView(mHoverMenuView);
        mHoverMenuView = null;
        mDragger.deactivate(); // TODO: should be called by HoverMenuView in some kind of release() method.
        super.onDetachedFromWindow();
    }

    @Override
    public void setMenu(@Nullable io.mattcarroll.hover.defaulthovermenu.HoverMenu menu) {
        mMenu = menu;
        if (null != mMenu && null != mHoverMenuView) {
            mHoverMenuView.setMenu(menu);
        }
    }

    @Override
    public void show() {
        // TODO:
    }

    @Override
    public void hide() {
        // TODO:
    }

    @Override
    public void expandMenu() {
        // TODO: figure out programmatic expansion/collapse for hover view
//        mHoverView.expand();
    }

    @Override
    public void collapseMenu() {
        // TODO: figure out programmatic expansion/collapse for hover view
//        mHoverView.collapse();
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

}
