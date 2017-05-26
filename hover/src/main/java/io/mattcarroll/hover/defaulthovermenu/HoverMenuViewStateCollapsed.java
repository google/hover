package io.mattcarroll.hover.defaulthovermenu;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * TODO
 */
class HoverMenuViewStateCollapsed implements HoverMenuViewState {

    private static final String TAG = "HoverMenuViewStateCollapsed";

    private final Dragger mDragger;
    private Screen mScreen;
    private FloatingTab mFloatingTab;
    private Point mDropPoint; // Where the floating tab is dropped before seeking its initial dock.
    private Point mDock;
    private boolean mHasControl = false;
    private Dragger.DragListener mDragListener;
    private Listener mListener;

    public HoverMenuViewStateCollapsed(@NonNull Dragger dragger) {
        this(dragger, null);
    }

    public HoverMenuViewStateCollapsed(@NonNull Dragger dragger, @Nullable Point dropPoint) {
        mDragger = dragger;
        mDropPoint = dropPoint;
    }

    @Override
    public void takeControl(@NonNull Screen screen) {
        if (mHasControl) {
            throw new RuntimeException("Cannot take control of a FloatingTab when we already control one.");
        }

        Log.d(TAG, "Instructing tab to dock itself.");
        mHasControl = true;
        mScreen = screen;
        mFloatingTab = screen.createChainedTab("PRIMARY", null); // TODO:
        mDragListener = new FloatingTabDragListener(this);
        createDock();
        sendToDock();
    }

    @Override
    public void giveControlTo(@NonNull HoverMenuViewState otherController) {
        if (!mHasControl) {
            throw new RuntimeException("Cannot give control to another HoverMenuController when we don't have the HoverTab.");
        }

        mHasControl = false;
        mDragger.deactivate();
        mDragListener = null;
        otherController.takeControl(mScreen);
        mScreen = null;
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
        mDock = calculateDockPosition(mFloatingTab.getPosition());

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

    private void createDock() {
        if (null == mDock) {
            mDock = null != mDropPoint ?
                    calculateDockPosition(mDropPoint) :
                    createInitialDock();
        }
    }

    private Point createInitialDock() {
        Point artificialInitialDropPoint = new Point(0, mScreen.getHeight() / 2);
        Log.d(TAG, "Initial dock.  Screen height: " + mScreen.getHeight()
                + ", artificial drop point: " + artificialInitialDropPoint);
        return calculateDockPosition(artificialInitialDropPoint);
    }

    private Point calculateDockPosition(@NonNull Point dropPosition) {
        Point newAnchorPosition;
        int dockInset = mFloatingTab.getTabSize() / 4;
        if (dropPosition.x < (mScreen.getWidth() / 2)) {
            newAnchorPosition = new Point(dockInset, dropPosition.y);
        } else {
            newAnchorPosition = new Point(mScreen.getWidth() - dockInset, dropPosition.y);
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
