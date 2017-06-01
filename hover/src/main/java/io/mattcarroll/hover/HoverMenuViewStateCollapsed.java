package io.mattcarroll.hover;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

/**
 * TODO
 */
class HoverMenuViewStateCollapsed implements HoverMenuViewState {

    private static final String TAG = "HoverMenuViewStateCollapsed";

    private final Dragger mDragger;
    private Screen mScreen;
    private HoverMenu mMenu;
    private FloatingTab mFloatingTab;
    private Point mDropPoint; // Where the floating tab is dropped before seeking its initial dock.
    private SideDock mSideDock;
    private boolean mHasControl = false;
    private boolean mIsCollapsed = false;
    private boolean mIsDocked = false;
    private String mPrimaryTabId = null;
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

    public HoverMenuViewStateCollapsed(@NonNull Dragger dragger) {
        mDragger = dragger;
    }

    public HoverMenuViewStateCollapsed(@NonNull Dragger dragger, @Nullable Point dropPoint) {
        mDragger = dragger;
        mDropPoint = dropPoint;
    }

    public HoverMenuViewStateCollapsed(@NonNull Dragger dragger, @Nullable SideDock sideDock) {
        mDragger = dragger;
        mSideDock = sideDock;
    }

    @Override
    public void takeControl(@NonNull Screen screen, @NonNull String primaryTabId) {
        Log.d(TAG, "Taking control.");
        if (mHasControl) {
            throw new RuntimeException("Cannot take control of a FloatingTab when we already control one.");
        }

        Log.d(TAG, "Instructing tab to dock itself.");
        mHasControl = true;
        mScreen = screen;
        mPrimaryTabId = primaryTabId;
        Log.d(TAG, "Taking control with primary tab: " + mPrimaryTabId);
        HoverMenu.Section section = mMenu.getSection(new HoverMenu.SectionId(mPrimaryTabId));
        section = null != section ? section : mMenu.getSection(0);
        mFloatingTab = screen.createChainedTab(mPrimaryTabId, section.getTabView());
        mDragListener = new FloatingTabDragListener(this);
        mIsCollapsed = false; // We're collapsing, not yet collapsed.
        if (null != mListener) {
            mListener.onCollapsing();
        }
        createDock();
        sendToDock();

        mFloatingTab.addOnLayoutChangeListener(mOnLayoutChangeListener);
    }

    @Override
    public void giveControlTo(@NonNull HoverMenuViewState otherController) {
        Log.d(TAG, "Giving up control.");
        if (!mHasControl) {
            throw new RuntimeException("Cannot give control to another HoverMenuController when we don't have the HoverTab.");
        }

        mFloatingTab.removeOnLayoutChangeListener(mOnLayoutChangeListener);

        mHasControl = false;
        mIsDocked = false;
        deactivateDragger();
        mDragListener = null;
        otherController.takeControl(mScreen, mPrimaryTabId);
        mScreen = null;
        mFloatingTab = null;
    }

    public void setMenu(@NonNull HoverMenu menu) {
        mMenu = menu;
    }

    @NonNull
    public SideDock getDock() {
        return mSideDock;
    }

    public void setListener(@Nullable Listener listener) {
        mListener = listener;
    }

    private void onPickedUpByUser() {
        if (null != mListener) {
            mIsDocked = false;
            mListener.onDragStart();
        }
    }

    private void onDroppedByUser() {
        if (null != mListener) {
            mListener.onDragEnd();
        }

        boolean droppedOnExit = mScreen.getExitView().isInExitZone(mFloatingTab.getPosition());
        if (droppedOnExit && null != mListener) {
            Log.d(TAG, "User dropped floating tab on exit.");
            mListener.onDroppedOnExit();
        } else {
            mSideDock = new SideDock(
                    mFloatingTab.getPosition(),
                    new Point(mScreen.getWidth(), mScreen.getHeight())
            );
            Log.d(TAG, "User dropped tab. Sending to new dock: " + mSideDock);

            sendToDock();
        }
    }

    private void onTap() {
        Log.d(TAG, "Floating tab was tapped.");
        if (null != mListener) {
            mListener.onTap();
        }
    }

    private void sendToDock() {
        Log.d(TAG, "Sending floating tab to dock.");
        deactivateDragger();
        Point dockPosition = mSideDock.calculateDockPosition(
                new Point(mScreen.getWidth(), mScreen.getHeight()),
                mFloatingTab.getTabSize()
        );
        mFloatingTab.dockTo(dockPosition, new Runnable() {
            @Override
            public void run() {
                onDocked();
            }
        });
    }

    private void moveToDock() {
        Log.d(TAG, "Moving floating tag to dock.");
        Point dockPosition = mSideDock.calculateDockPosition(
                new Point(mScreen.getWidth(), mScreen.getHeight()),
                mFloatingTab.getTabSize()
        );
        mFloatingTab.moveTo(dockPosition);
    }

    private void createDock() {
        if (null == mSideDock) {
            mSideDock = null != mDropPoint ?
                    new SideDock(mDropPoint, new Point(mScreen.getWidth(), mScreen.getHeight())):
                    createInitialDock();
        }
    }

    private SideDock createInitialDock() {
        return new SideDock(0.5f, SideDock.LEFT);
    }

    private void onDocked() {
        Log.d(TAG, "Docked. Activating dragger.");
        mIsDocked = true;
        activateDragger();

        // We consider ourselves having gone from "collapsing" to "collapsed" upon the very first dock.
        boolean didJustCollapse = !mIsCollapsed;
        mIsCollapsed = true;
        if (null != mListener) {
            if (didJustCollapse) {
                mListener.onCollapsed();
            }
            mListener.onDocked();
        }
    }

    private void moveTabTo(@NonNull Point position) {
        mFloatingTab.moveTo(position);
    }

    private void activateDragger() {
        mDragger.activate(mDragListener, mFloatingTab.getPosition());
    }

    private void deactivateDragger() {
        mDragger.deactivate();
    }

    public interface Listener {
        void onCollapsing();

        void onCollapsed();

        void onDragStart();

        void onDragEnd();

        void onDocked();

        void onTap();

        void onDroppedOnExit();
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
