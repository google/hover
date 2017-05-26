package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * TODO:
 */
class TabChain {

    private static final String TAG = "TabChain";

    private FloatingTab mTab;
    private Tab mPredecessorTab;
    private final Set<Tab.OnPositionChangeListener> mOnPositionChangeListeners = new CopyOnWriteArraySet<Tab.OnPositionChangeListener>();

    private final Tab.OnPositionChangeListener mOnPredecessorPositionChange = new Tab.OnPositionChangeListener() {
        @Override
        public void onPositionChange(@NonNull Point position) {
            Log.d(TAG, hashCode() + "'s predecessor moved to: " + position);
            moveToChainedPosition();
        }
    };

    public TabChain(@NonNull FloatingTab tab) {
        mTab = tab;
    }

    @NonNull
    public FloatingTab getTab() {
        return mTab;
    }

    public void chainTo(@NonNull Tab tab) {
        chainTo(tab, null);
    }

    public void chainTo(@NonNull Tab tab, @Nullable final Runnable onChained) {
        if (null != mPredecessorTab) {
            mPredecessorTab.removeOnPositionChangeListener(mOnPredecessorPositionChange);
        }

        Log.d(TAG, hashCode() + " is now chained to " + tab.hashCode());
        mPredecessorTab = tab;
        moveToChainedPosition();
        mPredecessorTab.addOnPositionChangeListener(mOnPredecessorPositionChange);
    }

    private void moveToChainedPosition() {
        Point newDock = getMyChainPositionRelativeTo(mPredecessorTab);
        if (View.VISIBLE == mTab.getVisibility()) {
            mTab.dockTo(newDock);
        } else {
            mTab.setDockPosition(newDock);
            mTab.moveTo(newDock);
            mTab.appear(null);
        }
    }

    private Point getMyChainPositionRelativeTo(@NonNull Tab tab) {
        Point predecessorTabPosition = tab.getDockPosition();
        Log.d(TAG, "Predecessor position: " + predecessorTabPosition);
        return new Point(
                predecessorTabPosition.x - 200, // TODO: configurable spacing
                predecessorTabPosition.y
        );
    }

    public void unchain() {
        unchain(null);
    }

    public void unchain(@Nullable final Runnable onUnchained) {
        mPredecessorTab.removeOnPositionChangeListener(mOnPredecessorPositionChange);
        mTab.disappear(onUnchained);
    }
}
