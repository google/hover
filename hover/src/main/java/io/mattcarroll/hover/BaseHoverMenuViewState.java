package io.mattcarroll.hover;

import android.support.annotation.NonNull;
import android.view.WindowManager;

/**
 * TODO
 */
public abstract class BaseHoverMenuViewState implements HoverMenuViewState {

    private HoverMenuView mHoverMenuView;

    @Override
    public void takeControl(@NonNull HoverMenuView hoverMenuView) {
        mHoverMenuView = hoverMenuView;
    }

    // Only call this if using HoverMenuView directly in a window.
    @Override
    public void addToWindow() {
        if (!mHoverMenuView.mIsAddedToWindow) {
            mHoverMenuView.mWindowViewController.addView(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    false,
                    mHoverMenuView
            );

            mHoverMenuView.mIsAddedToWindow = true;

            if (mHoverMenuView.mIsTouchableInWindow) {
                mHoverMenuView.makeTouchableInWindow();
            } else {
                mHoverMenuView.makeUntouchableInWindow();
            }
        }
    }

    // Only call this if using HoverMenuView directly in a window.
    @Override
    public void removeFromWindow() {
        if (mHoverMenuView.mIsAddedToWindow) {
            mHoverMenuView.mWindowViewController.removeView(mHoverMenuView);
            mHoverMenuView.mIsAddedToWindow = false;
        }
    }

    @Override
    public void makeTouchableInWindow() {
        mHoverMenuView.mIsTouchableInWindow = true;
        if (mHoverMenuView.mIsAddedToWindow) {
            mHoverMenuView.mWindowViewController.makeTouchable(mHoverMenuView);
        }
    }

    @Override
    public void makeUntouchableInWindow() {
        mHoverMenuView.mIsTouchableInWindow = false;
        if (mHoverMenuView.mIsAddedToWindow) {
            mHoverMenuView.mWindowViewController.makeUntouchable(mHoverMenuView);
        }
    }
}
