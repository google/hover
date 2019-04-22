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
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;

/**
 * {@link HoverViewState} that operates the {@link HoverView} when it is closed. Closed means that
 * nothing is visible - no tabs, no content.  From the user's perspective, there is no
 * {@code HoverView}.
 */
class HoverViewStatePreviewed extends HoverViewStateCollapsed {

    private static final String TAG = "HoverViewStatePreviewed";
    private TabMessageView mMessageView;
    private Dragger.DragListener<TabMessageView> mDefaultMessageViewDragListener;
    private Dragger.DragListener<TabMessageView> mCustomMessageViewDragListener;

    HoverViewStatePreviewed() {
        mDefaultMessageViewDragListener = new DefaultMessageViewDragListener();
    }

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
                activateDragger();
            }
        });
    }

    @Override
    public void giveUpControl(@NonNull final HoverViewState nextState) {
        Log.d(TAG, "Giving up control.");
        if (nextState instanceof HoverViewStateCollapsed) {
            mMessageView.disappear(true);
        } else {
            mMessageView.disappear(false);
        }
        super.giveUpControl(nextState);
    }

    @Override
    protected void onPickedUpByUser() {
        super.onPickedUpByUser();
    }

    @Override
    protected void onClose(final boolean userDropped) {
        super.onClose(userDropped);
    }

    @Override
    protected void activateDragger() {
        ArrayList<Pair<? extends HoverFrameLayout, ? extends BaseTouchController.TouchListener>> list = new ArrayList<>();
        list.add(new Pair<>(mFloatingTab, mFloatingTabDragListener));
        list.add(new Pair<>(mMessageView, mDefaultMessageViewDragListener));
        mHoverView.mDragger.activate(list);
    }

    @Override
    protected void onDocked() {
        super.onDocked();
    }

    @Override
    public HoverViewStateType getStateType() {
        return HoverViewStateType.PREVIEWED;
    }

    public void setMessageViewDragListener(final Dragger.DragListener<TabMessageView> messageViewDragListener) {
        this.mCustomMessageViewDragListener = messageViewDragListener;
    }

    private class DefaultMessageViewDragListener implements Dragger.DragListener<TabMessageView> {

        @Override
        public void onDragStart(TabMessageView view, float x, float y) {
            if (mCustomMessageViewDragListener == null) {
                return;
            }
            mCustomMessageViewDragListener.onDragStart(view, x, y);

        }

        @Override
        public void onDragTo(TabMessageView view, float x, float y) {
            if (mCustomMessageViewDragListener == null) {
                return;
            }
            mCustomMessageViewDragListener.onDragTo(view, x, y);
        }

        @Override
        public void onReleasedAt(TabMessageView view, float x, float y) {
            if (mCustomMessageViewDragListener == null) {
                return;
            }
            mCustomMessageViewDragListener.onReleasedAt(view, x, y);
        }

        @Override
        public void onTap(TabMessageView view) {
            if (mCustomMessageViewDragListener == null) {
                return;
            }
            mCustomMessageViewDragListener.onTap(view);
        }

        @Override
        public void onTouchDown(TabMessageView view) {
            if (mCustomMessageViewDragListener == null) {
                return;
            }
            mCustomMessageViewDragListener.onTouchDown(view);
        }

        @Override
        public void onTouchUp(TabMessageView view) {
            if (mCustomMessageViewDragListener == null) {
                return;
            }
            mCustomMessageViewDragListener.onTouchUp(view);
        }
    }

}
