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

import static android.view.View.GONE;

/**
 * {@link HoverViewState} that operates the {@link HoverView} when it is closed. Closed means that
 * nothing is visible - no tabs, no content.  From the user's perspective, there is no
 * {@code HoverView}.
 */
class HoverViewStateClosed extends BaseHoverViewState {

    private static final String TAG = "HoverMenuViewStateClosed";

    private HoverView mHoverView;

    @Override
    public void takeControl(@NonNull HoverView hoverView) {
        Log.d(TAG, "Taking control.");
        super.takeControl(hoverView);
        mHoverView = hoverView;
        mHoverView.notifyListenersClosing();
        mHoverView.mState = this;
        mHoverView.clearFocus();
        mHoverView.mScreen.getContentDisplay().setVisibility(GONE);

        final FloatingTab selectedTab = mHoverView.mScreen.getChainedTab(mHoverView.mSelectedSectionId);
        if (null != selectedTab) {
            selectedTab.disappear(new Runnable() {
                @Override
                public void run() {
                    mHoverView.mScreen.destroyChainedTab(selectedTab);
                    mHoverView.notifyListenersClosed();
                }
            });
        } else {
            mHoverView.notifyListenersClosed();
        }

        mHoverView.makeUntouchableInWindow();
    }

    private void changeState(@NonNull HoverViewState nextState) {
        mHoverView.setState(nextState);
        mHoverView = null;
    }

    @Override
    public void expand() {
        if (null != mHoverView.mMenu) {
            Log.d(TAG, "Expanding.");
            changeState(mHoverView.mExpanded);
        } else {
            Log.d(TAG, "Asked to expand, but there is no menu set. Can't expand until a menu is available.");
        }
    }

    @Override
    public void collapse() {
        if (null != mHoverView.mMenu) {
            Log.d(TAG, "Collapsing.");
            changeState(mHoverView.mCollapsed);
        } else {
            Log.d(TAG, "Asked to collapse, but there is no menu set. Can't collapse until a menu is available.");
        }
    }

    @Override
    public void close() {
        Log.d(TAG, "Instructed to close, but Hover is already closed.");
    }

    @Override
    public void setMenu(@Nullable final HoverMenu menu) {
        mHoverView.mMenu = menu;

        // If the menu is null then there is nothing to restore.
        if (null == menu) {
            return;
        }

        mHoverView.restoreVisualState();

        if (null == mHoverView.mSelectedSectionId || null == mHoverView.mMenu.getSection(mHoverView.mSelectedSectionId)) {
            mHoverView.mSelectedSectionId = mHoverView.mMenu.getSection(0).getId();
        }
    }

    @Override
    public boolean respondsToBackButton() {
        return false;
    }

    @Override
    public void onBackPressed() {
        // No-op
    }
}
