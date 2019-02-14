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

import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * {@link HoverViewState} that operates the {@link HoverView} when it is closed. Closed means that
 * nothing is visible - no tabs, no content.  From the user's perspective, there is no
 * {@code HoverView}.
 */
class HoverViewStatePreviewed extends HoverViewStateCollapsed {

    private static final String TAG = "HoverViewStatePreviewed";
    private TabMessageView mMessageView;

    @Override
    public void takeControl(@NonNull HoverView hoverView) {
        super.takeControl(hoverView);
        mHoverView.mState = this;
        mHoverView.notifyListenersPreviewing();
        mMessageView = mHoverView.mScreen.getTabMessageView(mHoverView.mSelectedSectionId);
        mMessageView.appear(mHoverView.mCollapsedDock, new Runnable() {
            @Override
            public void run() {
                mHoverView.notifyListenersPreviewed();
            }
        });
    }

    @Override
    protected void changeState(@NonNull final HoverViewState nextState) {
        if (nextState instanceof HoverViewStateCollapsed) {
            mMessageView.disappear(true);
        } else {
            mMessageView.disappear(false);
        }
        super.changeState(nextState);
    }

    @Override
    public void preview() {
        Log.d(TAG, "Instructed to preview, but already previewed.");
    }

    @Override
    public void collapse() {
        changeState(mHoverView.mCollapsed);
    }

    @Override
    protected void moveTabTo(@NonNull Point position) {
        final int floatingTabOffset = mMessageView.getWidth() / 2;
        if (mHoverView.mCollapsedDock.sidePosition().getSide() == SideDock.SidePosition.RIGHT) {
            mFloatingTab.moveTo(new Point(position.x + floatingTabOffset, position.y));
        } else {
            mFloatingTab.moveTo(new Point(position.x - floatingTabOffset, position.y));
        }
    }

    @Override
    protected void activateDragger() {
        final Rect tabRect = new Rect();
        final Rect messageRect = new Rect();
        mFloatingTab.getGlobalVisibleRect(tabRect);
        mMessageView.getGlobalVisibleRect(messageRect);
        tabRect.union(messageRect);

        mHoverView.mDragger.activate(mDragListener, tabRect);
    }
}
