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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.util.ListUpdateCallback;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import static android.view.View.INVISIBLE;

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

    private static final String TAG = "HoverViewStateCollapsed";
    private static final float MIN_TAB_VERTICAL_POSITION = 0.0f;
    private static final float MAX_TAB_VERTICAL_POSITION = 1.0f;
    private static final long DEFAULT_IDLE_MILLIS = 5000;

    protected FloatingTab mFloatingTab;
    protected final FloatingTabDragListener mFloatingTabDragListener = new FloatingTabDragListener(this);
    protected HoverMenu.Section mSelectedSection;
    private int mSelectedSectionIndex = -1;
    private boolean mIsCollapsed = false;
    private Handler mHandler = new Handler();
    private Runnable mIdleActionRunnable;
    private Runnable mOnStateChanged;
    private Point mStartPoint = null;
    private long mDragStartMillis = -1L;

    @Override
    public void takeControl(@NonNull HoverView floatingTab, final Runnable onStateChanged) {
        super.takeControl(floatingTab, onStateChanged);
        Log.d(TAG, "Taking control.");
        mOnStateChanged = onStateChanged;
        mHoverView.makeUntouchableInWindow();
        mHoverView.clearFocus(); // For handling hardware back button presses.

        Log.d(TAG, "Taking control with selected section: " + mHoverView.mSelectedSectionId);
        mSelectedSection = mHoverView.mMenu.getSection(mHoverView.mSelectedSectionId);
        mSelectedSection = null != mSelectedSection ? mSelectedSection : mHoverView.mMenu.getSection(0);
        mSelectedSectionIndex = mHoverView.mMenu.getSectionIndex(mSelectedSection);
        mFloatingTab = mHoverView.mScreen.getChainedTab(mHoverView.mSelectedSectionId);
        final boolean wasFloatingTabVisible;
        if (null == mFloatingTab) {
            wasFloatingTabVisible = false;
            mFloatingTab = mHoverView.mScreen.createChainedTab(mSelectedSection);
        } else {
            wasFloatingTabVisible = true;
        }
        mIsCollapsed = false; // We're collapsing, not yet collapsed.
        initDockPosition();

        // post() animation to dock in case the container hasn't measured itself yet.
        if (!wasFloatingTabVisible) {
            mFloatingTab.setVisibility(INVISIBLE);
        }
        mHoverView.post(new Runnable() {
            @Override
            public void run() {
                if (!hasControl()) {
                    return;
                }
                if (wasFloatingTabVisible) {
                    sendToDock();
                } else {
                    moveToDock();
                    mFloatingTab.appear(new Runnable() {
                        @Override
                        public void run() {
                            if (!hasControl()) {
                                return;
                            }
                            onDocked();
                        }
                    });
                }
            }
        });

        if (null != mHoverView.mMenu) {
            listenForMenuChanges();
        }

        initIdleActionRunnable();
    }

    @Override
    public void giveUpControl(@NonNull HoverViewState nextState) {
        Log.d(TAG, "Giving up control.");
        restoreHoverViewIdleAction();

        if (null != mHoverView.mMenu) {
            mHoverView.mMenu.setUpdatedCallback(null);
        }

        mHoverView.mScreen.getExitView().hide();

        deactivateDragger();
        mFloatingTab = null;
        super.giveUpControl(nextState);
    }

    @Override
    public void setMenu(@Nullable final HoverMenu menu) {
        listenForMenuChanges();
    }

    private void listenForMenuChanges() {
        mHoverView.mMenu.setUpdatedCallback(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                // no-op
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d(TAG, "onRemoved. Position: " + position + ", Count: " + count);
                if (mSelectedSectionIndex == position) {
                    Log.d(TAG, "Selected tab removed. Displaying a new tab.");
                    // TODO: externalize a selection strategy for when the selected section disappears
                    mHoverView.mScreen.destroyChainedTab(mFloatingTab);

                    mSelectedSectionIndex = mSelectedSectionIndex > 0 ? mSelectedSectionIndex - 1 : 0;
                    mSelectedSection = mHoverView.mMenu.getSection(mSelectedSectionIndex);
                    mHoverView.mSelectedSectionId = mSelectedSection.getId();
                    mFloatingTab = mHoverView.mScreen.createChainedTab(mSelectedSection);
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
                    if (i == mSelectedSectionIndex) {
                        Log.d(TAG, "Selected tab changed. Updating its display.");
                        mFloatingTab.setTabView(mHoverView.mMenu.getSection(position).getTabView());
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

    protected void onPickedUpByUser() {
        if (!hasControl()) {
            return;
        }

        mStartPoint = mFloatingTab.getPosition();
        mDragStartMillis = System.currentTimeMillis();

        mHoverView.mScreen.getExitView().show();
        restoreHoverViewIdleAction();
        mHoverView.notifyOnDragStart(this);
    }

    private double calculateDistance(@NonNull Point p1, @NonNull Point p2) {
        return Math.sqrt(
                Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2)
        );
    }

    // Function to find the line given two points
    Point lineFromPoints(Point p, Point q) {
        int a = q.y - p.y;
        int b = p.x - q.x;
        int c = a * (p.x) + b * (p.y);

        if (b < 0) {
            Log.d(TAG, "The line passing through points P and Q is: "
                    + a + "x " + b + "y = " + c);
        } else {
            Log.d(TAG, "The line passing through points P and Q is: "
                    + a + "x + " + b + "y = " + c);
        }

        int xDirection = 0;
        int yDirection = 0;

        if (p.x - q.x >= 0) {
            xDirection = 0;
        } else {
            xDirection = 1;
        }

        if (p.y - q.y >= 0) {
            yDirection = 0;
        } else {
            yDirection = 1;
        }

        // TODO handle DIVIDE BY ZERO
        Log.d(TAG, "xDirection = " + xDirection);
        Log.d(TAG, "yDirection = " + yDirection);

        Log.d(TAG, "x = 0 ->  y = " + (c / b));
//        Log.d(TAG, "x = " + (c / a) + ", y = 0");
        Log.d(TAG, "x = mHoverView.getScreenSize().x ->  y = " + ((c - a * mHoverView.getScreenSize().x) / b));
//        Log.d(TAG, "x = " + ((c - b * mHoverView.getScreenSize().y) / a) + ", y = mHoverView.getScreenSize().y");

        if (xDirection == 0) {
            return new Point(0, (c / b));
        } else {
            return new Point(0, (c - a * mHoverView.getScreenSize().x) / b);
        }

    }

    static Point lineLineIntersection(Point pointA, Point pointB, Point pointC, Point pointD) {
        // Line AB represented as a1x + b1y = c1
        double a1 = pointB.y - pointA.y;
        double b1 = pointA.x - pointB.x;
        double c1 = a1 * (pointA.x) + b1 * (pointA.y);

        // Line CD represented as a2x + b2y = c2
        double a2 = pointD.y - pointC.y;
        double b2 = pointC.x - pointD.x;
        double c2 = a2 * (pointC.x) + b2 * (pointC.y);

        double determinant = a1 * b2 - a2 * b1;

        if (determinant == 0) {
            // The lines are parallel. This is simplified
            // by returning a pair of FLT_MAX
            return new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            double x = (b2 * c1 - b1 * c2) / determinant;
            double y = (a1 * c2 - a2 * c1) / determinant;
            return new Point((int) x, (int) y);
        }
    }

    private void onDroppedByUser() {
        if (!hasControl()) {
            return;
        }

        mHoverView.mScreen.getExitView().hide();

        int diffPositionX = mStartPoint.x - mFloatingTab.getPosition().x;
        int diffPositionY = mStartPoint.y - mFloatingTab.getPosition().y;

        boolean droppedOnExit = mHoverView.mScreen.getExitView().isInExitZone(mFloatingTab.getPosition(), mHoverView.getScreenSize());
        if (droppedOnExit) {
            onClose(true);
        } else {

            Log.d(TAG, "TRACK_DEBUG onDroppedByUser diffPositionX = " + diffPositionX + ", diffPositionY = " + diffPositionY);
            Log.d(TAG, "TRACK_DEBUG onDroppedByUser distance = " + calculateDistance(mStartPoint, mFloatingTab.getPosition()));
            Log.d(TAG, "TRACK_DEBUG onDroppedByUser diffTimeMillis = " + (System.currentTimeMillis() - mDragStartMillis));
//            lineFromPoints(mStartPoint, mFloatingTab.getPosition());

            int tabSize = mHoverView.getResources().getDimensionPixelSize(R.dimen.hover_tab_size);
            Point screenSize = mHoverView.getScreenSize();
            final float tabHorizontalPositionPercent = (float) mFloatingTab.getPosition().x / screenSize.x;
            final float viewHeightPercent = mFloatingTab.getHeight() / 2f / screenSize.y;
            float tabVerticalPosition = (float) lineFromPoints(mStartPoint, mFloatingTab.getPosition()).y / screenSize.y;
            if (tabVerticalPosition < MIN_TAB_VERTICAL_POSITION) {
                tabVerticalPosition = MIN_TAB_VERTICAL_POSITION;
            } else if (tabVerticalPosition > MAX_TAB_VERTICAL_POSITION - viewHeightPercent) {
                tabVerticalPosition = MAX_TAB_VERTICAL_POSITION - viewHeightPercent;
            }
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

    protected void onClose(final boolean userDropped) {
        if (!hasControl()) {
            return;
        }

        if (userDropped) {
            Log.d(TAG, "User dropped floating tab on exit.");
            if (null != mHoverView.mOnExitListener) {
                mHoverView.mOnExitListener.onExit();
            }
        } else {
            Log.d(TAG, "Auto dropped.");
        }
        mHoverView.close();
    }

    protected void onTap() {
        Log.d(TAG, "Floating tab was tapped.");
        if (mHoverView != null) {
            mHoverView.notifyOnTap(this);
        }
    }

    private void sendToDock() {
        Log.d(TAG, "Sending floating tab to dock.");
        deactivateDragger();
        mFloatingTab.setDock(mHoverView.mCollapsedDock);
        mFloatingTab.dock(new Runnable() {
            @Override
            public void run() {
                if (!hasControl()) {
                    return;
                }
                onDocked();
            }
        });
    }

    private void moveToDock() {
        Log.d(TAG, "Moving floating tag to dock.");
        Point dockPosition = mHoverView.mCollapsedDock.sidePosition().calculateDockPosition(
                mHoverView.getScreenSize(),
                mFloatingTab.getTabSize()
        );
        mFloatingTab.moveCenterTo(dockPosition);
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

    protected void onDocked() {
        Log.d(TAG, "Docked. Activating dragger.");
        if (!hasControl() || !mHoverView.mIsAddedToWindow) {
            return;
        }
        activateDragger();
        scheduleHoverViewIdleAction();

        // We consider ourselves having gone from "collapsing" to "collapsed" upon the very first dock.
        boolean didJustCollapse = !mIsCollapsed;
        mIsCollapsed = true;
        mHoverView.saveVisualState();
        if (didJustCollapse) {
            if (mOnStateChanged != null) {
                mOnStateChanged.run();
            }
        }
        mHoverView.notifyOnDocked(this);
    }

    void moveFloatingTabTo(View floatingTab, @NonNull Point position) {
        Log.d(TAG, "TRACK_DEBUG moveFloatingTabTo position = " + position);
        if (mHoverView.mScreen.getExitView().isInExitZone(position, mHoverView.getScreenSize())) {
            mHoverView.mScreen.getExitView().startEnterExitAnim();
        } else {
            mHoverView.mScreen.getExitView().startExitExitAnim();
        }

//        mHoverView.mScreen.getExitView().enterExitRange(position);
        mFloatingTab.moveCenterTo(position);
    }

    protected void activateDragger() {
        ArrayList<Pair<? extends HoverFrameLayout, ? extends BaseTouchController.TouchListener>> list = new ArrayList<>();
        list.add(new Pair<>(mFloatingTab, mFloatingTabDragListener));
        mHoverView.mDragger.activate(list);
    }

    protected void deactivateDragger() {
        mHoverView.mDragger.deactivate();
    }

    private void initIdleActionRunnable() {
        this.mIdleActionRunnable = new Runnable() {
            @Override
            public void run() {
                if (mHoverView == null) {
                    return;
                }

                final HoverViewState state = mHoverView.getState();
                if (!(state instanceof HoverViewStatePreviewed) && state instanceof HoverViewStateCollapsed) {
                    final HoverView.HoverViewIdleAction idleAction = mHoverView.getIdleAction();
                    if (idleAction != null) {
                        idleAction.changeState(mFloatingTab);
                    }

                }
            }
        };
    }

    private void scheduleHoverViewIdleAction() {
        mHandler.postDelayed(mIdleActionRunnable, DEFAULT_IDLE_MILLIS);
    }

    protected void restoreHoverViewIdleAction() {
        mHandler.removeCallbacks(mIdleActionRunnable);
        final HoverView.HoverViewIdleAction idleAction = mHoverView.getIdleAction();
        if (idleAction != null) {
            idleAction.restoreState(mFloatingTab);
        }
    }

    @Override
    public HoverViewStateType getStateType() {
        return HoverViewStateType.COLLAPSED;
    }

    protected static final class FloatingTabDragListener implements Dragger.DragListener<FloatingTab> {

        private final HoverViewStateCollapsed mOwner;

        protected FloatingTabDragListener(@NonNull HoverViewStateCollapsed owner) {
            mOwner = owner;
        }

        @Override
        public void onDragStart(FloatingTab floatingTab, float x, float y) {
            mOwner.onPickedUpByUser();
        }

        @Override
        public void onDragTo(FloatingTab floatingTab, float x, float y) {
            mOwner.moveFloatingTabTo(floatingTab, new Point((int) x, (int) y));
        }

        @Override
        public void onReleasedAt(FloatingTab floatingTab, float x, float y) {
            mOwner.onDroppedByUser();
        }

        @Override
        public void onDragCancel(FloatingTab floatingTab) {
            mOwner.onDroppedByUser();
        }

        @Override
        public void onTap(FloatingTab floatingTab) {
            mOwner.onTap();
        }

        @Override
        public void onTouchDown(FloatingTab floatingTab) {
        }

        @Override
        public void onTouchUp(FloatingTab floatingTab) {
        }
    }
}
