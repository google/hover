package io.mattcarroll.hover.defaulthovermenu;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

/**
 * TODO:
 */
class Screen {

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

    public FloatingTab createChainedTab(@NonNull String tabId, @NonNull View tabView) {
        if (mTabs.containsKey(tabId)) {
            return mTabs.get(tabId);
        } else {
            FloatingTab chainedTab = new FloatingTab(mContainer.getContext(), tabId);
            chainedTab.setTabView(tabView);
            chainedTab.enableDebugMode(mIsDebugMode);
            mContainer.addView(chainedTab);
            mTabs.put(tabId, chainedTab);
            return chainedTab;
        }
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
