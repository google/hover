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
import android.view.ViewGroup;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import io.mattcarroll.hover.utils.ViewUtils;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * {@link HoverViewState} that operates the {@link HoverView} when it is collapsed. Collapsed means
 * that the only thing visible is the selected {@link FloatingTab}.  This tab docks itself against
 * the left or right sides of the screen.  The user can drag the tab around and drop it.
 * <p>
 * If the tab is tapped, the {@code HoverView} is transitioned to its expanded state.
 * <p>
 * If the tab is dropped on the exit region, the {@code HoverView} is transitioned to its closed state.
 */
class HoverViewStateCollapsed extends BaseHoverViewState {

    private static final String TAG = "HoverMenuViewStateCollapsed";

    private HoverView mHoverView;
    private FloatingTab mFloatingTab;
    private HoverMenu.Section mSelectedSection;
    private int mSelectedSectionIndex = -1;
    private boolean mHasControl = false;
    private boolean mIsCollapsed = false;
    private boolean mIsDocked = false;
    private FloatingTabDragListener mDragListener;
    private Listener mListener;

    private List<FloatingTab> mChainedTabs = new ArrayList<>();
    private Queue<Point> mHistory = new ArrayDeque<>();
    private boolean mIsInExitView = false;
    private FloatingTab.OnPositionChangeListener mOnPositionChangeListener;

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

    HoverViewStateCollapsed() {
    }

    @Override
    public void takeControl(@NonNull HoverView hoverView) {
        Log.d(TAG, "Taking control.");
        super.takeControl(hoverView);

        if (mHasControl) {
            Log.w(TAG, "Already has control.");
            return;
        }

        Log.d(TAG, "Instructing tab to dock itself.");
        mHasControl = true;
        mHoverView = hoverView;
        mHoverView.mState = this;
        mHoverView.clearFocus(); // For handling hardware back button presses.
        mHoverView.mScreen.getContentDisplay().setVisibility(GONE);
        mHoverView.makeUntouchableInWindow();

        Log.d(TAG, "Taking control with selected section: " + mHoverView.mSelectedSectionId);
        mSelectedSection = mHoverView.mMenu.getSection(mHoverView.mSelectedSectionId);
        mSelectedSection = null != mSelectedSection ? mSelectedSection : mHoverView.mMenu.getSection(0);
        mSelectedSectionIndex = mHoverView.mMenu.getSectionIndex(mSelectedSection);
        mFloatingTab = mHoverView.mScreen.getChainedTab(mHoverView.mSelectedSectionId);
        final boolean wasFloatingTabVisible;
        if (null == mFloatingTab) {
            wasFloatingTabVisible = false;
            mFloatingTab = mHoverView.mScreen.createChainedTab(mHoverView.mSelectedSectionId, mSelectedSection.getTabView());
        } else {
            wasFloatingTabVisible = true;
        }
        mDragListener = new FloatingTabDragListener(this);
        mIsCollapsed = false; // We're collapsing, not yet collapsed.
        mHoverView.notifyListenersCollapsing();
        if (null != mListener) {
            mListener.onCollapsing();
        }
        initDockPosition();

        // post() animation to dock in case the container hasn't measured itself yet.
        if (!wasFloatingTabVisible) {
            mFloatingTab.setVisibility(INVISIBLE);
        }
        createChainedTabs();
        mHoverView.post(new Runnable() {
            @Override
            public void run() {
                if (wasFloatingTabVisible) {
                    sendToDock();
                } else {
                    moveToDock();
                    mFloatingTab.setVisibility(VISIBLE);
                    onDocked();
                    addOnPositionChangeListener();
                }
            }
        });

        mFloatingTab.addOnLayoutChangeListener(mOnLayoutChangeListener);

        if (null != mHoverView.mMenu) {
            listenForMenuChanges();
        }
    }

    private void createChainedTabs() {
        Log.d(TAG, "Creating chained tabs");
        if (null != mHoverView.mMenu) {
            final int sectionCount = mHoverView.mMenu.getSectionCount();
            for (int i = 0; i < sectionCount; i++) {
                HoverMenu.Section section = mHoverView.mMenu.getSection(i);

                if (section == null) continue;
                if (!section.isContainsDisplayType(HoverMenu.Section.DisplayType.COLLAPSED))
                    continue;

                FloatingTab chainedTab = mHoverView.mScreen.createChainedTab(section.getId(), section.getTabView());
                mChainedTabs.add(chainedTab);
            }
            reorderChainedTabs();
        }
    }

    private void reorderChainedTabs() {
        final int sectionCount = mHoverView.mMenu.getSectionCount();
        for (int i = sectionCount - 1; i >= 0; --i) {
            mChainedTabs.get(i).bringToFront();
        }
        mFloatingTab.bringToFront();
    }

    @Override
    public void expand() {
        changeState(mHoverView.mExpanded);
    }

    @Override
    public void collapse() {
        Log.d(TAG, "Instructed to collapse, but already collapsed.");
    }

    @Override
    public void close() {
        changeState(mHoverView.mClosed);
    }

    private void changeState(@NonNull HoverViewState nextState) {
        Log.d(TAG, "Giving up control.");
        if (!mHasControl) {
            throw new RuntimeException("Cannot give control to another HoverMenuController when we don't have the HoverTab.");
        }

        mFloatingTab.removeOnPositionChangeListener(mOnPositionChangeListener);
        mFloatingTab.removeOnLayoutChangeListener(mOnLayoutChangeListener);

        if (null != mHoverView.mMenu) {
            mHoverView.mMenu.setUpdatedCallback(null);
        }

        mHasControl = false;
        mIsDocked = false;
        deactivateDragger();
        if (mDragListener != null) {
            mDragListener.detach();
            mDragListener = null;
        }
        mFloatingTab = null;
        mSelectedSection = null;

        mChainedTabs.clear();
        mOnPositionChangeListener = null;

        mHoverView.setState(nextState);
        mHoverView = null;
    }

    @Override
    public void setMenu(@Nullable final HoverMenu menu) {
        mHoverView.mMenu = menu;

        // If the menu is null or empty then we can't be collapsed, close the menu.
        if (null == menu || menu.getSectionCount() == 0) {
            close();
            return;
        }

        mHoverView.restoreVisualState();

        if (null == mHoverView.mSelectedSectionId || null == mHoverView.mMenu.getSection(mHoverView.mSelectedSectionId)) {
            mHoverView.mSelectedSectionId = mHoverView.mMenu.getSection(0).getId();
        }

        listenForMenuChanges();
    }

    private void listenForMenuChanges() {
        mHoverView.mMenu.setUpdatedCallback(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                for (int i = position; i < position + count; i++) {
                    HoverMenu.Section section = mHoverView.mMenu.getSection(i);
                    FloatingTab floatingTab = mHoverView.mScreen.createChainedTab(section.getId(), section.getTabView());
                    mChainedTabs.add(floatingTab);
                    floatingTab.appear(null);
                }
                reorderChainedTabs();
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d(TAG, "onRemoved. Position: " + position + ", Count: " + count);
                for (int i = position; i < position + count; i++) {
                    FloatingTab floatingTab = mChainedTabs.get(i);
                    floatingTab.disappear(null);

                    if (mSelectedSectionIndex == position) {
                        mFloatingTab.removeOnLayoutChangeListener(mOnLayoutChangeListener);

                        mSelectedSectionIndex = 0;
                        mSelectedSection = mHoverView.mMenu.getSection(mSelectedSectionIndex);
                        mHoverView.mSelectedSectionId = mSelectedSection.getId();
                        mFloatingTab = mHoverView.mScreen.createChainedTab(
                                mSelectedSection.getId(),
                                mSelectedSection.getTabView()
                        );

                        mFloatingTab.addOnLayoutChangeListener(mOnLayoutChangeListener);
                    }
                }
                reorderChainedTabs();
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                // no-op
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.d(TAG, "Tab(s) changed. From: " + position + ", To: " + count);
                for (int i = position; i < position + count; ++i) {
                    HoverMenu.Section section = mHoverView.mMenu.getSection(i);
                    if (section == null) continue;
                    FloatingTab floatingTab = mHoverView.mScreen.getChainedTab(section.getId());
                    if (floatingTab == null) continue;
                    floatingTab.setTabView(section.getTabView());
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

    @Override
    public void onSelectedSectionIdChanged() {
        mFloatingTab.removeOnLayoutChangeListener(mOnLayoutChangeListener);
        mFloatingTab.removeOnPositionChangeListener(mOnPositionChangeListener);

        mSelectedSection = mHoverView.mMenu.getSection(mHoverView.mSelectedSectionId);
        mSelectedSection = null != mSelectedSection ? mSelectedSection : mHoverView.mMenu.getSection(0);
        mSelectedSectionIndex = mHoverView.mMenu.getSectionIndex(mSelectedSection);

        mChainedTabs.add(mFloatingTab);
        mFloatingTab = mHoverView.mScreen.createChainedTab(mSelectedSection.getId(), mSelectedSection.getTabView());
        mChainedTabs.remove(mFloatingTab);

        mFloatingTab.addOnLayoutChangeListener(mOnLayoutChangeListener);
        mFloatingTab.addOnPositionChangeListener(mOnPositionChangeListener);
        reorderChainedTabs();
    }

    public void setListener(@Nullable Listener listener) {
        mListener = listener;
    }

    private void onFloatingTabPressed() {
        ViewUtils.scale(mFloatingTab, 0.8f);
    }

    private void onPickedUpByUser() {
        mIsDocked = false;
        if (null != mListener) {
            mListener.onDragStart();
        }
    }

    private void onDroppedByUser() {
        if (null != mListener) {
            mListener.onDragEnd();
        }

        boolean droppedOnExit = mHoverView.mScreen.getExitView().isInExitZone(mFloatingTab.getPosition());
        if (droppedOnExit) {
            Log.d(TAG, "User dropped floating tab on exit.");
            closeMenu(new Runnable() {
                @Override
                public void run() {
                    if (null != mHoverView.mOnExitListener) {
                        mHoverView.mOnExitListener.onExit();
                    }
                }
            });
        } else {
            int tabSize = mHoverView.getResources().getDimensionPixelSize(R.dimen.hover_tab_size);
            Point screenSize = new Point(mHoverView.mScreen.getWidth(), mHoverView.mScreen.getHeight());
            float tabHorizontalPositionPercent = (float) mFloatingTab.getPosition().x / screenSize.x;
            float tabVerticalPosition = (float) mFloatingTab.getPosition().y / screenSize.y;
            Log.d(TAG, "Dropped at horizontal " + tabHorizontalPositionPercent + ", vertical " + tabVerticalPosition);
            SideDock.SidePosition sidePosition = new SideDock.SidePosition(
                    tabHorizontalPositionPercent <= 0.5 ? SideDock.SidePosition.LEFT : SideDock.SidePosition.RIGHT,
                    tabVerticalPosition
            );
            mHoverView.mCollapsedDock = new SideDock(
                    mHoverView,
                    tabSize,
                    sidePosition
            );
            mHoverView.saveVisualState();
            Log.d(TAG, "User dropped tab. Sending to new dock: " + mHoverView.mCollapsedDock);

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

    private void addOnPositionChangeListener() {
        if (mOnPositionChangeListener == null) {
            mOnPositionChangeListener = new FloatingTab.OnPositionChangeListener() {
                @Override
                public void onPositionChange(@NonNull Point tabPosition) {
                    chainedTabsFollow(tabPosition);
                }

                @Override
                public void onDockChange(@NonNull Point dockPosition) {
                    mHoverView.notifyCollapsedListenerChangeCollapsedDockPosition(dockPosition);
                }
            };
            mFloatingTab.addOnPositionChangeListener(mOnPositionChangeListener);
        }
    }

    private void sendToDock() {
        Log.d(TAG, "Sending floating tab to dock.");
        mIsDocked = false;
        deactivateDragger();
        mFloatingTab.setDock(mHoverView.mCollapsedDock);
        mFloatingTab.dock(new Runnable() {
            @Override
            public void run() {
                addOnPositionChangeListener();
                onDocked();
            }
        });
    }

    private void moveToDock() {
        Log.d(TAG, "Moving floating tag to dock.");
        final Point dockPosition = mHoverView.mCollapsedDock.sidePosition().calculateDockPosition(
                new Point(mHoverView.mScreen.getWidth(), mHoverView.mScreen.getHeight()),
                mFloatingTab.getTabSize()
        );
        mHoverView.notifyCollapsedListenerChangeCollapsedDockPosition(dockPosition);
        mFloatingTab.moveTo(dockPosition);
        for (int i = 0; i < mChainedTabs.size(); i++) {
            mChainedTabs.get(i).moveTo(dockPosition);
        }
        deactivateDragger();
        activateDragger();
    }

    private void initDockPosition() {
        if (null == mHoverView.mCollapsedDock) {
            int tabSize = mHoverView.getResources().getDimensionPixelSize(R.dimen.hover_tab_size);
            mHoverView.mCollapsedDock = new SideDock(
                    mHoverView,
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
        mHoverView.saveVisualState();
        mHoverView.notifyListenersCollapsed();
        if (null != mListener) {
            if (didJustCollapse) {
                mListener.onCollapsed();
            }
            mListener.onDocked();
        }
    }

    private void moveTabTo(@NonNull Point position) {
        ExitView exitView = mHoverView.mScreen.getExitView();
        boolean isCannotMove = exitView.receiveTabPosition(position);
        if (isCannotMove) {
            if (!mIsInExitView) {
                mIsInExitView = true;
                exitView.bringExitIconToFront((ViewGroup) mFloatingTab.getParent());
                mFloatingTab.moveTo(exitView.getExitViewPosition());
                for (int i = 0; i < mChainedTabs.size(); i++)
                    mChainedTabs.get(i).moveTo(exitView.getExitViewPosition());
            }
        } else {
            mIsInExitView = false;
            mFloatingTab.moveTo(position);
        }
        chainedTabsFollow(position);
    }

    private void closeMenu(final @Nullable Runnable onClosed) {
        mFloatingTab.disappear(new Runnable() {
            @Override
            public void run() {
                mHoverView.mScreen.destroyChainedTab(mFloatingTab);

                if (null != onClosed) {
                    onClosed.run();
                }

                close();
            }
        });
    }

    private void activateDragger() {
        mHoverView.mDragger.activate(mDragListener, mFloatingTab.getPosition());
    }

    private void deactivateDragger() {
        mHoverView.mDragger.deactivate();
    }

    private void chainedTabsFollow(Point position) {
        Object[] history = mHistory.toArray();
        if (!mIsInExitView) {
            int count = 0;
            for (int i = 0; i < mChainedTabs.size(); i++) {
                if (i == mSelectedSectionIndex) continue;
                count++;
                int mDistanceBetweenChainedTabs = 2;
                if (history.length >= mDistanceBetweenChainedTabs * count) {
                    mChainedTabs.get(i).moveTo((Point) history[history.length - mDistanceBetweenChainedTabs * count]);
                }
            }
        }
        updateHistory(position);
    }

    private void updateHistory(Point position) {
        mHistory.add(position);
        if (mHistory.size() > mChainedTabs.size() * 2)
            mHistory.poll();
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

        private HoverViewStateCollapsed mOwner;

        private FloatingTabDragListener(@NonNull HoverViewStateCollapsed owner) {
            mOwner = owner;
        }

        @Override
        public void onPress(float x, float y) {
            ViewUtils.scale(mOwner.mFloatingTab, 0.9f);
        }

        @Override
        public void onDragStart(float x, float y) {
            mOwner.onPickedUpByUser();
        }

        @Override
        public void onDragTo(float x, float y, boolean isTouchWithinSlopOfOriginalTouch) {
            Point point = new Point((int) x, (int) y);
            if (!isTouchWithinSlopOfOriginalTouch) {
                mOwner.mHoverView.mScreen.getExitView()
                        .prepareExit(point);
            }
            mOwner.moveTabTo(point);
        }

        @Override
        public void onReleasedAt(float x, float y) {
            mOwner.mHoverView.mScreen.getExitView().releaseExit();
            ViewUtils.scale(mOwner.mFloatingTab, 1f);
            mOwner.onDroppedByUser();
        }

        @Override
        public void onTap() {
            ViewUtils.scaleAfter(mOwner.mFloatingTab, 1f);
            mOwner.onTap();
        }

        public void detach() {
            mOwner = null;
        }
    }
}
