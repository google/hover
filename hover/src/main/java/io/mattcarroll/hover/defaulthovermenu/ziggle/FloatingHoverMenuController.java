package io.mattcarroll.hover.defaulthovermenu.ziggle;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.FrameLayout;

import io.mattcarroll.hover.defaulthovermenu.Dragger;

/**
 * TODO
 */
class FloatingHoverMenuController extends FrameLayout implements HoverMenuController {

    private static final String TAG = "FloatingHoverMenuController";

    private final Dragger mDragger;
    private Point mDock;
    private HoverTab mFloatingIcon;
    private boolean mIsDocked = false;
    private Listener mListener;

    FloatingHoverMenuController(@NonNull Context context, @NonNull Dragger dragger) {
        this(context, dragger, new Point(0, 0));
    }

    FloatingHoverMenuController(@NonNull Context context, @NonNull Dragger dragger, @NonNull Point dock) {
        super(context);
        mDock = dock;
        mDragger = dragger;
    }

    @Override
    public void takeControl(@NonNull HoverTab hoverTab) {
        if (null != mFloatingIcon) {
            throw new RuntimeException("Cannot take control of a HoverTab when we already control one.");
        }

        Log.d(TAG, "Taking control. HoverTab width: " + hoverTab.getTabWidth() + ", height: " + hoverTab.getTabHeight());
        mFloatingIcon = hoverTab;
        slideToDock();
    }

    @Override
    public void giveControlTo(@NonNull HoverMenuController otherController) {
        if (null == mFloatingIcon) {
            throw new RuntimeException("Cannot give control to another HoverMenuController when we don't have the HoverTab.");
        }

        onUnDocked();
        mDragger.deactivate();
        otherController.takeControl(mFloatingIcon);
        mFloatingIcon = null;
    }

    public void setListener(@Nullable Listener listener) {
        mListener = listener;
    }

    private void onDocked() {
        if (mIsDocked) {
            return;
        }

        mIsDocked = true;
        Log.d(TAG, "onDocked() - Position: " + mFloatingIcon.getPosition());
        mDragger.activate(new Dragger.DragListener() {
            @Override
            public void onPress(float x, float y) {

            }

            @Override
            public void onDragStart(float x, float y) {
                onUnDocked();
            }

            @Override
            public void onDragTo(float x, float y) {
                mFloatingIcon.moveTo(new Point((int) x, (int) y));
            }

            @Override
            public void onReleasedAt(float x, float y) {
                mDragger.deactivate();
                updateDockPosition(new Point((int) x, (int) y));
                slideToDock();
            }

            @Override
            public void onTap() {
                if (null != mListener) {
                    mListener.onTap();
                }
            }
        }, mFloatingIcon.getPosition());
    }

    private void onUnDocked() {
        mIsDocked = false;
    }

    private void updateDockPosition(@NonNull Point dropPoint) {
        int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        boolean isOnLeft = dropPoint.x <= (screenWidth / 2);

        int dockX = isOnLeft ?
                (mFloatingIcon.getTabWidth() / 2) - 20 :
                screenWidth - ((mFloatingIcon.getTabWidth() / 2) - 20);

        mDock = new Point(dockX, dropPoint.y);
    }

    private void slideToDock() {
        mFloatingIcon.slideTo(mDock, new Runnable() {
            @Override
            public void run() {
                onDocked();
            }
        });
    }

    public interface Listener {
        void onTap();
    }
}
