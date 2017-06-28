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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

/**
 * The visual area occupied by a {@link HoverView}. A {@code Screen} acts as a factory for the
 * visual elements used within a {@code HoverView}.
 */
class Screen {

    private static final String TAG = "Screen";

    private ViewGroup mContainer;
    private ContentDisplay mContentDisplay;
    private ExitView mExitView;
    private ShadeView mShadeView;
    private Map<String, FloatingTab> mTabs = new HashMap<>();
    private boolean mIsDebugMode = false;

    Screen(@NonNull ViewGroup hoverMenuContainer) {
        mContainer = hoverMenuContainer;

        mShadeView = new ShadeView(mContainer.getContext());
        mContainer.addView(mShadeView, new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        mShadeView.hideImmediate();

        mExitView = new ExitView(mContainer.getContext());
        mContainer.addView(mExitView, new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        mExitView.setVisibility(GONE);

        mContentDisplay = new ContentDisplay(mContainer.getContext());
        mContainer.addView(mContentDisplay);
        mContentDisplay.setVisibility(GONE);
    }

    public void enableDrugMode(boolean debugMode) {
        mIsDebugMode = debugMode;

        mContentDisplay.enableDebugMode(debugMode);
        for (FloatingTab tab : mTabs.values()) {
            tab.enableDebugMode(debugMode);
        }
    }

    public int getWidth() {
        return mContainer.getWidth();
    }

    public int getHeight() {
        return mContainer.getHeight();
    }

    @NonNull
    public FloatingTab createChainedTab(@NonNull HoverMenu.SectionId sectionId, @NonNull View tabView) {
        String tabId = sectionId.toString();
        return createChainedTab(tabId, tabView);
    }

    @NonNull
    public FloatingTab createChainedTab(@NonNull String tabId, @NonNull View tabView) {
        Log.d(TAG, "Existing tabs...");
        for (String existingTabId : mTabs.keySet()) {
            Log.d(TAG, existingTabId);
        }
        if (mTabs.containsKey(tabId)) {
            return mTabs.get(tabId);
        } else {
            Log.d(TAG, "Creating new tab with ID: " + tabId);
            FloatingTab chainedTab = new FloatingTab(mContainer.getContext(), tabId);
            chainedTab.setTabView(tabView);
            chainedTab.enableDebugMode(mIsDebugMode);
            mContainer.addView(chainedTab);
            mTabs.put(tabId, chainedTab);
            return chainedTab;
        }
    }

    @Nullable
    public FloatingTab getChainedTab(@Nullable HoverMenu.SectionId sectionId) {
        String tabId = null != sectionId ? sectionId.toString() : null;
        return getChainedTab(tabId);
    }

    @Nullable
    public FloatingTab getChainedTab(@Nullable String tabId) {
        return mTabs.get(tabId);
    }

    public void destroyChainedTab(@NonNull FloatingTab chainedTab) {
        mTabs.remove(chainedTab.getTabId());
        chainedTab.setTabView(null);
        mContainer.removeView(chainedTab);
    }

    public ContentDisplay getContentDisplay() {
        return mContentDisplay;
    }

    public ExitView getExitView() {
        return mExitView;
    }

    public ShadeView getShadeView() {
        return mShadeView;
    }
}
