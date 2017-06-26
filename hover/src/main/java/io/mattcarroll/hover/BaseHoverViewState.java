package io.mattcarroll.hover;

import android.support.annotation.NonNull;
import android.view.WindowManager;

/**
 * TODO
 */
public abstract class BaseHoverViewState implements HoverViewState {

    private HoverView mHoverView;

    @Override
    public void takeControl(@NonNull HoverView hoverView) {
        mHoverView = hoverView;
    }

    // Only call this if using HoverMenuView directly in a window.
    @Override
    public void addToWindow() {
        if (!mHoverView.mIsAddedToWindow) {
            mHoverView.mWindowViewController.addView(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    false,
                    mHoverView
            );

            mHoverView.mIsAddedToWindow = true;

            if (mHoverView.mIsTouchableInWindow) {
                mHoverView.makeTouchableInWindow();
            } else {
                mHoverView.makeUntouchableInWindow();
            }
        }
    }

    // Only call this if using HoverMenuView directly in a window.
    @Override
    public void removeFromWindow() {
        if (mHoverView.mIsAddedToWindow) {
            mHoverView.mWindowViewController.removeView(mHoverView);
            mHoverView.mIsAddedToWindow = false;
        }
    }

    @Override
    public void makeTouchableInWindow() {
        mHoverView.mIsTouchableInWindow = true;
        if (mHoverView.mIsAddedToWindow) {
            mHoverView.mWindowViewController.makeTouchable(mHoverView);
        }
    }

    @Override
    public void makeUntouchableInWindow() {
        mHoverView.mIsTouchableInWindow = false;
        if (mHoverView.mIsAddedToWindow) {
            mHoverView.mWindowViewController.makeUntouchable(mHoverView);
        }
    }
}
