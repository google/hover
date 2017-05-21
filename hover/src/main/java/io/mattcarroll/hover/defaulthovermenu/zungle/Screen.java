package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import static android.view.View.GONE;

/**
 * TODO:
 */
public class Screen {

    private ViewGroup mContainer;
    private FloatingTab mFloatingTab;
    private ContentDisplay mContentDisplay;
    private ExitView mExitView;
    private ShadeView mShadeView;

    Screen(@NonNull ViewGroup hoverMenuContainer) {
        mContainer = hoverMenuContainer;

        mShadeView = new ShadeView(mContainer.getContext());
        mContainer.addView(mShadeView, new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        mShadeView.hideImmediate();

        mFloatingTab = new FloatingTab(mContainer.getContext());
        mContainer.addView(mFloatingTab);

        mContentDisplay = new ContentDisplay(mContainer.getContext());
        mContainer.addView(mContentDisplay);
        mContentDisplay.anchorTo(mFloatingTab);
        mContentDisplay.setVisibility(GONE);

        mExitView = new ExitView(mContainer.getContext());
        mContainer.addView(mExitView, new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        mExitView.setVisibility(GONE);
    }

    public int getWidth() {
        return mContainer.getWidth();
    }

    public int getHeight() {
        return mContainer.getHeight();
    }

    public FloatingTab getFloatingTab() {
        return mFloatingTab;
    }

    public ChainedTab createChainedTab() {
        ChainedTab chainedTab = new ChainedTab(mContainer.getContext());
        mContainer.addView(chainedTab);
        return chainedTab;
    }

    public void destroyChainedTab(@NonNull ChainedTab chainedTab) {
        mContainer.removeView(chainedTab);
        chainedTab.setTabView(null);
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
