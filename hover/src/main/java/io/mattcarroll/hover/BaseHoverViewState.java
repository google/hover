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
import android.view.WindowManager;

/**
 * {@link HoverViewState} that includes behavior common to all implementations.
 */
abstract class BaseHoverViewState implements HoverViewState {

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
