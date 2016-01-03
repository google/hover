package io.mattcarroll.hover.defaulthovermenu.window;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import io.mattcarroll.hover.BuildConfig;
import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.defaulthovermenu.HoverMenuView;
import io.mattcarroll.hover.defaulthovermenu.utils.window.InWindowDragger;
import io.mattcarroll.hover.defaulthovermenu.utils.window.WindowViewController;

/**
 * {@link HoverMenu} implementation that can shown directly within a {@code Window}.
 */
public class WindowHoverMenu implements HoverMenu {

    private static final String TAG = "WindowHoverMenu";

    private InWindowDragger mInWindowDragger;
    private WindowViewController mWindowViewController; // Shows/hides/positions Views in a Window.
    private HoverMenuView mHoverMenuView; // The visual presentation of the Hover menu.
    private boolean mIsShowingHoverMenu; // Are we currently display mHoverMenuView?
    private boolean mIsInDragMode; // If we're not in drag mode then we're in menu mode.
    private MenuExitListener mMenuExitListener = new NoOpMenuExitListener();

    private HoverMenuView.HoverMenuTransitionListener mHoverMenuTransitionListener = new HoverMenuView.HoverMenuTransitionListener() {
        @Override
        public void onCollapsing() { }

        @Override
        public void onCollapsed() {
            mIsInDragMode = true;
            // When collapsed, we make mHoverMenuView untouchable so that the WindowDragWatcher can
            // take over. We do this so that touch events outside the drag area can propagate to
            // applications on screen.
            mWindowViewController.makeUntouchable(mHoverMenuView);
        }

        @Override
        public void onExpanding() {
            mIsInDragMode = false;
        }

        @Override
        public void onExpanded() {
            mWindowViewController.makeTouchable(mHoverMenuView);
        }
    };

    private HoverMenuView.HoverMenuExitRequestListener mMenuExitRequestListener = new HoverMenuView.HoverMenuExitRequestListener() {
        @Override
        public void onExitRequested() {
            exit();
        }
    };

    public WindowHoverMenu(@NonNull Context context, @NonNull WindowManager windowManager, @NonNull PointF savedAnchorState) {
        mWindowViewController = new WindowViewController(windowManager);

        mInWindowDragger = new InWindowDragger(
                context,
                mWindowViewController,
                ViewConfiguration.get(context).getScaledTouchSlop()
        );
        mInWindowDragger.setDebugMode(BuildConfig.DEBUG);
        Log.d(TAG, "Is debug mode? " + BuildConfig.DEBUG);

        Log.d(TAG, "Initial normalized anchor position: " + savedAnchorState);
        mHoverMenuView = new HoverMenuView(context, mInWindowDragger, savedAnchorState);
        mHoverMenuView.setHoverMenuExitRequestListener(mMenuExitRequestListener);
    }

    @Override
    public PointF getAnchorState() {
        PointF anchor = mHoverMenuView.getAnchorState();
        Log.d(TAG, "Returning normalized anchor position - (" + anchor.x + ", " + anchor.y + ")");
        return mHoverMenuView.getAnchorState();
    }

    @Override
    public void setAdapter(@Nullable HoverMenuAdapter adapter) {
        mHoverMenuView.setAdapter(adapter);
    }

    /**
     * Expands the Hover menu to show all of its tabs and a content area for the selected tab. To
     * collapse the menu down a single active tab, use {@link #collapseMenu()}.
     */
    @Override
    public void expandMenu() {
//        Log.d(TAG, "expandMenu()");
        if (mIsInDragMode) {
            mHoverMenuView.expand();
        }
    }

    /**
     * Collapses the Hover menu down to its single active tab and allows the tab to be dragged
     * around the display. This method is the inverse of {@link #expandMenu()}.
     */
    @Override
    public void collapseMenu() {
        if (!mIsInDragMode) {
            Log.d(TAG, "collapseMenu()");
            mHoverMenuView.setHoverMenuTransitionListener(mHoverMenuTransitionListener);
            mHoverMenuView.collapse();
        }
    }

    /**
     * Initializes and displays the Hover menu. To destroy and remove the Hover menu, use {@link #exit()}.
     */
    public void show() {
        if (!mIsShowingHoverMenu) {
            mWindowViewController.addView(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, false, mHoverMenuView);

            // Sync our control state with the HoverMenuView state.
            if (mHoverMenuView.isExpanded()) {
//                expandMenu();
                mWindowViewController.makeTouchable(mHoverMenuView);
            } else {
                collapseMenu();
            }

            mIsShowingHoverMenu = true;
        }
    }

    /**
     * Exits the Hover menu system. This method is the inverse of {@link #show()}.
     */
    public void exit() {
        if (mIsShowingHoverMenu) {
            mIsShowingHoverMenu = false;

            // Notify our exit listener that we're exiting.
            mMenuExitListener.onHoverMenuAboutToExit();

            // Cleanup the control structures and Views.
            mInWindowDragger.release();
            mWindowViewController.removeView(mHoverMenuView);
        }
    }

    public void setMenuExitListener(@Nullable MenuExitListener menuExitListener) {
        if (null != menuExitListener) {
            mMenuExitListener = menuExitListener;
        } else {
            mMenuExitListener = new NoOpMenuExitListener();
        }
    }

    public interface MenuExitListener {
        void onHoverMenuAboutToExit();
    }

    public static class NoOpMenuExitListener implements MenuExitListener {
        @Override
        public void onHoverMenuAboutToExit() {
            // no-op
        }
    }
}
