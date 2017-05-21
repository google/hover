package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import io.mattcarroll.hover.defaulthovermenu.Dragger;

/**
 * TODO
 */
class CollapsedMenu implements FloatingTabOwner {

    private static final String TAG = "CollapsedMenuFloatingTabOwner";

    private final Screen mScreen;
    private final Dragger mDragger;
    private FloatingTab mFloatingTab;
    private Point mDock;
    private boolean mHasControl = false;
    private Dragger.DragListener mDragListener;
    private Listener mListener;

    public CollapsedMenu(@NonNull Screen screen, @NonNull Dragger dragger) {
        this(screen, dragger, null);
    }

    public CollapsedMenu(@NonNull Screen screen, @NonNull Dragger dragger, @Nullable Point dock) {
        mScreen = screen;
        mDragger = dragger;
        mDock = null != dock ? dock : createInitialDock();
    }

    private Point createInitialDock() {
        Log.d(TAG, "Initial dock.  Screen height: " + mScreen.getHeight() + ", Position: " + (mScreen.getHeight() / 2));
        return new Point(0, mScreen.getHeight() / 2);
    }

    @Override
    public void takeControl(@NonNull FloatingTab floatingTab) {
        if (mHasControl) {
            throw new RuntimeException("Cannot take control of a FloatingTab when we already control one.");
        }

        Log.d(TAG, "Instructing tab to dock itself.");
        mHasControl = true;
        mFloatingTab = floatingTab;
        mDragListener = new FloatingTabDragListener(this);
        sendToDock();
    }

    @Override
    public void giveControlTo(@NonNull FloatingTabOwner otherController) {
        if (!mHasControl) {
            throw new RuntimeException("Cannot give control to another HoverMenuController when we don't have the HoverTab.");
        }

        mHasControl = false;
        mDragger.deactivate();
        mDragListener = null;
        otherController.takeControl(mFloatingTab);
        mFloatingTab = null;
    }

    @NonNull
    public Point getDock() {
        return mDock;
    }

    public void setListener(@Nullable Listener listener) {
        mListener = listener;
    }

    private void onPickedUpByUser() {
        if (null != mListener) {
            mListener.onDragStart();
        }
    }

    private void onDroppedByUser() {
        mDock = calculateDockPosition();

        if (null != mListener) {
            mListener.onDragEnd();
        }

        boolean droppedOnExit = mScreen.getExitView().isInExitZone(mFloatingTab.getPosition());
        if (droppedOnExit && null != mListener) {
            mListener.onDroppedOnExit();
        } else {
            sendToDock();
        }
    }

    private void onTap() {
        if (null != mListener) {
            mListener.onTap();
        }
    }

    private void sendToDock() {
        deactivateDragger();
        mFloatingTab.dockTo(mDock, new Runnable() {
            @Override
            public void run() {
                onDocked();
            }
        });
    }

    private Point calculateDockPosition() {
        Point currentTabPosition = mFloatingTab.getPosition();
        Point newAnchorPosition;
        if (currentTabPosition.x < (mScreen.getWidth() / 2)) {
            newAnchorPosition = new Point(0, currentTabPosition.y);
        } else {
            newAnchorPosition = new Point(mScreen.getWidth(), currentTabPosition.y);
        }
        return newAnchorPosition;
    }

    private void onDocked() {
        activateDragger();

        if (null != mListener) {
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
        void onDragStart();

        void onDragEnd();

        void onDocked();

        void onTap();

        void onDroppedOnExit();
    }

    private static final class FloatingTabDragListener implements Dragger.DragListener {

        private final CollapsedMenu mOwner;

        private FloatingTabDragListener(@NonNull CollapsedMenu owner) {
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
