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
import android.support.annotation.Nullable;
import android.support.v7.util.ListUpdateCallback;
import android.util.Log;
import android.view.View;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * TODO
 */
class HoverMenuViewStateCollapsed extends BaseHoverMenuViewState {

    private static final String TAG = "HoverMenuViewStateCollapsed";

    private HoverMenuView mHoverMenuView;
    private FloatingTab mFloatingTab;
    private HoverMenu.Section mActiveSection;
    private int mActiveSectionIndex = -1;
    private boolean mHasControl = false;
    private boolean mIsCollapsed = false;
    private boolean mIsDocked = false;
    private Dragger.DragListener mDragListener;
    private Listener mListener;

    private final View.OnLayoutChangeListener mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (mHasControl && mIsDocked) {
                // We're docked. Adjust the tab position in case the screen was rotated. This should
                // only be a concern when displaying as a window overlay, but not when displaying
                // within a view hierarchy.
                moveToDock();
            }
        }
    };

    HoverMenuViewStateCollapsed() { }

    @Override
    public void takeControl(@NonNull HoverMenuView hoverMenuView) {
        Log.d(TAG, "Taking control.");
        super.takeControl(hoverMenuView);

        if (mHasControl) {
            Log.w(TAG, "Already has control.");
            return;
        }

        Log.d(TAG, "Instructing tab to dock itself.");
        mHasControl = true;
        mHoverMenuView = hoverMenuView;
        mHoverMenuView.mState = this;
        mHoverMenuView.clearFocus(); // For handling hardware back button presses.
        mHoverMenuView.mScreen.getContentDisplay().setVisibility(GONE);
        mHoverMenuView.makeUntouchableInWindow();

        Log.d(TAG, "Taking control with primary tab: " + mHoverMenuView.mSelectedSectionId.toString());
        mActiveSection = mHoverMenuView.mMenu.getSection(mHoverMenuView.mSelectedSectionId);
        mActiveSection = null != mActiveSection ? mActiveSection : mHoverMenuView.mMenu.getSection(0);
        mActiveSectionIndex = mHoverMenuView.mMenu.getSectionIndex(mActiveSection);
        mFloatingTab = mHoverMenuView.mScreen.createChainedTab(mHoverMenuView.mSelectedSectionId.toString(), mActiveSection.getTabView());
        mDragListener = new FloatingTabDragListener(this);
        mIsCollapsed = false; // We're collapsing, not yet collapsed.
        if (null != mListener) {
            mHoverMenuView.notifyListenersCollapsing();
            mListener.onCollapsing();
        }
        initDockPosition();

        // post() animation to dock in case the container hasn't measured itself yet.
        mHoverMenuView.post(new Runnable() {
            @Override
            public void run() {
                sendToDock();
            }
        });

        mFloatingTab.addOnLayoutChangeListener(mOnLayoutChangeListener);

        if (null != mHoverMenuView.mMenu) {
            listenForMenuChanges();
        }
    }

    @Override
    public void expand() {
        changeState(mHoverMenuView.mExpanded);
    }

    @Override
    public void collapse() {
        Log.d(TAG, "Instructed to collapse, but already collapsed.");
    }

    @Override
    public void close() {
        changeState(mHoverMenuView.mClosed);
    }

    private void changeState(@NonNull HoverMenuViewState nextState) {
        Log.d(TAG, "Giving up control.");
        if (!mHasControl) {
            throw new RuntimeException("Cannot give control to another HoverMenuController when we don't have the HoverTab.");
        }

        mFloatingTab.removeOnLayoutChangeListener(mOnLayoutChangeListener);

        if (null != mHoverMenuView.mMenu) {
            mHoverMenuView.mMenu.setUpdatedCallback(null);
        }

        mHasControl = false;
        mIsDocked = false;
        deactivateDragger();
        mDragListener = null;
        mFloatingTab = null;

        mHoverMenuView.setState(nextState);
        mHoverMenuView = null;
    }

    @Override
    public void setMenu(@Nullable final HoverMenu menu) {
        mHoverMenuView.mMenu = menu;

        // If the menu is null or empty then we can't be collapsed, close the menu.
        if (null == menu || menu.getSectionCount() == 0) {
            close();
            return;
        }

        mHoverMenuView.restoreVisualState();

        if (null == mHoverMenuView.mSelectedSectionId || null == mHoverMenuView.mMenu.getSection(mHoverMenuView.mSelectedSectionId)) {
            mHoverMenuView.mSelectedSectionId = mHoverMenuView.mMenu.getSection(0).getId();
        }

        listenForMenuChanges();
    }

    private void listenForMenuChanges() {
        mHoverMenuView.mMenu.setUpdatedCallback(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                // no-op
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d(TAG, "onRemoved. Position: " + position + ", Count: " + count);
                if (mActiveSectionIndex == position) {
                    Log.d(TAG, "Active tab removed. Displaying a new tab.");
                    // TODO: externalize a selection strategy for when the selected section disappears
                    mFloatingTab.removeOnLayoutChangeListener(mOnLayoutChangeListener);
                    mHoverMenuView.mScreen.destroyChainedTab(mFloatingTab);

                    mActiveSectionIndex = mActiveSectionIndex > 0 ? mActiveSectionIndex - 1 : 0;
                    mActiveSection = mHoverMenuView.mMenu.getSection(mActiveSectionIndex);
                    mHoverMenuView.mSelectedSectionId = mActiveSection.getId();
                    mFloatingTab = mHoverMenuView.mScreen.createChainedTab(
                            mActiveSection.getId().toString(),
                            mActiveSection.getTabView()
                    );

                    mFloatingTab.addOnLayoutChangeListener(mOnLayoutChangeListener);
                }
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                // no-op
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.d(TAG, "Tab(s) changed. From: " + position + ", To: " + count);
                for (int i = position; i < position + count; ++i) {
                    if (i == mActiveSectionIndex) {
                        Log.d(TAG, "Primary tab changed. Updating its display.");
                        mFloatingTab.setTabView(mHoverMenuView.mMenu.getSection(position).getTabView());
                    }
                }
            }
        });
    }

    @Override
    public boolean respondsToBackButton() {
        return false;
    }

    @Override
    public void onBackPressed() {
        // No-op
    }

    public void setListener(@Nullable Listener listener) {
        mListener = listener;
    }

    private void onPickedUpByUser() {
        mIsDocked = false;
        mHoverMenuView.mScreen.getExitView().setVisibility(VISIBLE);
        if (null != mListener) {
            mListener.onDragStart();
        }
    }

    private void onDroppedByUser() {
        mHoverMenuView.mScreen.getExitView().setVisibility(GONE);
        if (null != mListener) {
            mListener.onDragEnd();
        }

        boolean droppedOnExit = mHoverMenuView.mScreen.getExitView().isInExitZone(mFloatingTab.getPosition());
        if (droppedOnExit) {
            Log.d(TAG, "User dropped floating tab on exit.");
            closeMenu(new Runnable() {
                @Override
                public void run() {
                    if (null != mHoverMenuView.mOnExitListener) {
                        mHoverMenuView.mOnExitListener.onExit();
                    }
                }
            });
        } else {
            int tabSize = mHoverMenuView.getResources().getDimensionPixelSize(R.dimen.hover_tab_size);
            Point screenSize = new Point(mHoverMenuView.mScreen.getWidth(), mHoverMenuView.mScreen.getHeight());
            float tabHorizontalPositionPercent = (float) mFloatingTab.getPosition().x / screenSize.x;
            float tabVerticalPosition = (float) mFloatingTab.getPosition().y / screenSize.y;
            Log.d(TAG, "Dropped at horizontal " + tabHorizontalPositionPercent + ", vertical " + tabVerticalPosition);
            SideDock.SidePosition sidePosition = new SideDock.SidePosition(
                    tabHorizontalPositionPercent <= 0.5 ? SideDock.SidePosition.LEFT : SideDock.SidePosition.RIGHT,
                    tabVerticalPosition
            );
            mHoverMenuView.mCollapsedDock = new SideDock(
                    mHoverMenuView,
                    tabSize,
                    sidePosition
            );
            mHoverMenuView.saveVisualState();
            Log.d(TAG, "User dropped tab. Sending to new dock: " + mHoverMenuView.mCollapsedDock);

            sendToDock();
        }
    }

    private void onTap() {
        Log.d(TAG, "Floating tab was tapped.");
        expand();
        if (null != mListener) {
            mListener.onTap();
        }
    }

    private void sendToDock() {
        Log.d(TAG, "Sending floating tab to dock.");
        deactivateDragger();
        mFloatingTab.setDock(mHoverMenuView.mCollapsedDock);
        mFloatingTab.dock(new Runnable() {
            @Override
            public void run() {
                onDocked();
            }
        });
    }

    private void moveToDock() {
        Log.d(TAG, "Moving floating tag to dock.");
        Point dockPosition = mHoverMenuView.mCollapsedDock.sidePosition().calculateDockPosition(
                new Point(mHoverMenuView.mScreen.getWidth(), mHoverMenuView.mScreen.getHeight()),
                mFloatingTab.getTabSize()
        );
        mFloatingTab.moveTo(dockPosition);
    }

    private void initDockPosition() {
        if (null == mHoverMenuView.mCollapsedDock) {
            int tabSize = mHoverMenuView.getResources().getDimensionPixelSize(R.dimen.hover_tab_size);
            mHoverMenuView.mCollapsedDock = new SideDock(
                    mHoverMenuView,
                    tabSize,
                    new SideDock.SidePosition(SideDock.SidePosition.LEFT, 0.5f)
            );
        }
    }

    private void onDocked() {
        Log.d(TAG, "Docked. Activating dragger.");
        mIsDocked = true;
        activateDragger();

        // We consider ourselves having gone from "collapsing" to "collapsed" upon the very first dock.
        boolean didJustCollapse = !mIsCollapsed;
        mIsCollapsed = true;
        mHoverMenuView.saveVisualState();
        if (null != mListener) {
            if (didJustCollapse) {
                mHoverMenuView.notifyListenersCollapsed();
                mListener.onCollapsed();
            }
            mListener.onDocked();
        }
    }

    private void moveTabTo(@NonNull Point position) {
        mFloatingTab.moveTo(position);
    }

    private void closeMenu(final @Nullable Runnable onClosed) {
        mFloatingTab.disappear(new Runnable() {
            @Override
            public void run() {
                mHoverMenuView.mScreen.destroyChainedTab(mFloatingTab);

                if (null != onClosed) {
                    onClosed.run();
                }

                close();
            }
        });
    }

    private void activateDragger() {
        mHoverMenuView.mDragger.activate(mDragListener, mFloatingTab.getPosition());
    }

    private void deactivateDragger() {
        mHoverMenuView.mDragger.deactivate();
    }

    public interface Listener {
        void onCollapsing();

        void onCollapsed();

        void onDragStart();

        void onDragEnd();

        void onDocked();

        void onTap();

        // TODO: do we need this?
        void onExited();
    }

    private static final class FloatingTabDragListener implements Dragger.DragListener {

        private final HoverMenuViewStateCollapsed mOwner;

        private FloatingTabDragListener(@NonNull HoverMenuViewStateCollapsed owner) {
            mOwner = owner;
        }

        @Override
        public void onPress(float x, float y) { }

        @Override
        public void onDragStart(float x, float y) {
            mOwner.onPickedUpByUser();
        }

        @Override
        public void onDragTo(float x, float y) {
            mOwner.moveTabTo(new Point((int) x, (int) y));
        }

        @Override
        public void onReleasedAt(float x, float y) {
            mOwner.onDroppedByUser();
        }

        @Override
        public void onTap() {
            mOwner.onTap();
        }
    }
}
