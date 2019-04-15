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
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * {@link HoverViewState} that operates the {@link HoverView} when it is closed. Closed means that
 * nothing is visible - no tabs, no content.  From the user's perspective, there is no
 * {@code HoverView}.
 */
class HoverViewStatePreviewed extends HoverViewStateCollapsed {

    private static final String TAG = "HoverViewStatePreviewed";
    private TabMessageView mMessageView;
    private boolean mCollapseOnDocked = false;
    private boolean mTurnOffNextCollapseAnimation = false;

    @Override
    public void takeControl(@NonNull HoverView hoverView, final Runnable onStateChanged) {
        super.takeControl(hoverView, null);
        Log.d(TAG, "Taking control.");
        mMessageView = mHoverView.mScreen.getTabMessageView(mHoverView.mSelectedSectionId);
        mMessageView.setMessageView(mSelectedSection.getTabMessageView());
        mMessageView.appear(mHoverView.mCollapsedDock, new Runnable() {
            @Override
            public void run() {
                if (!hasControl()) {
                    return;
                }
                onStateChanged.run();
            }
        });
    }

    @Override
    public void giveUpControl(@NonNull final HoverViewState nextState) {
        Log.d(TAG, "Giving up control.");
        if (nextState instanceof HoverViewStateCollapsed) {
            mMessageView.disappear(!mTurnOffNextCollapseAnimation);
            mTurnOffNextCollapseAnimation = false;
        } else {
            mMessageView.disappear(false);
        }
        super.giveUpControl(nextState);
    }

    @Override
    protected void moveTabTo(View touchView, @NonNull Point position) {
        if (mHoverView == null) {
            return;
        }

        final int floatingTabOffset = mMessageView.getWidth() / 2;
        if (touchView.getTag() != null && touchView.getTag().equals(mFloatingTab.getTag())) {
            mFloatingTab.moveTo(position);
        } else if (mHoverView.mCollapsedDock.sidePosition().getSide() == SideDock.SidePosition.RIGHT) {
            mFloatingTab.moveTo(new Point(position.x + floatingTabOffset, position.y));
        } else {
            mFloatingTab.moveTo(new Point(position.x - floatingTabOffset, position.y));
        }
    }

    @Override
    protected void onPickedUpByUser() {
        mMessageView.disappear(true);
        mCollapseOnDocked = true;
        super.onPickedUpByUser();
    }

    @Override
    protected void activateDragger() {
        final ArrayList<View> list = new ArrayList<>();
        list.add(mFloatingTab);
        list.add(mMessageView);
        mHoverView.mDragger.activate(mDragListener, list);
    }

    @Override
    protected void onDocked() {
        super.onDocked();
        if (mCollapseOnDocked) {
            mTurnOffNextCollapseAnimation = true;
            mHoverView.collapse();
            mCollapseOnDocked = false;
        }
    }

    @Override
    public HoverViewStateType getStateType() {
        return HoverViewStateType.PREVIEWED;
    }
}
