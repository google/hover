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
    private Point mLockedPosition;
    private Tab mPredecessorTab;
    private final Set<Tab.OnPositionChangeListener> mOnPositionChangeListeners = new CopyOnWriteArraySet<Tab.OnPositionChangeListener>();

    private final Tab.OnPositionChangeListener mOnPredecessorPositionChange = new Tab.OnPositionChangeListener() {
        @Override
        public void onPositionChange(@NonNull Point position) {
            // No-op. We only care when our predecessor's dock changes.
        }

        @Override
        public void onDockChange(@NonNull Point dock) {
            Log.d(TAG, hashCode() + "'s predecessor dock moved to: " + dock);
            moveToChainedPosition(false);
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

        Log.d(TAG, mTab.getTabId() + " is now chained to " + tab.getTabId());
        mPredecessorTab = tab;
        mLockedPosition = null;
        Point myPosition = getMyChainPositionRelativeTo(mPredecessorTab);
        mTab.setDock(new PositionDock(myPosition));
    }

    public void chainTo(@NonNull Point lockedPosition) {
        chainTo(lockedPosition, null);
    }

    public void chainTo(@NonNull Point lockedPosition, @Nullable Runnable onChained) {
        if (null != mPredecessorTab) {
            mPredecessorTab.removeOnPositionChangeListener(mOnPredecessorPositionChange);
        }

        Log.d(TAG, mTab.getTabId() + " is now chained to position " + lockedPosition);
        mPredecessorTab = null;
        mLockedPosition = lockedPosition;
        mTab.setDock(new PositionDock(mLockedPosition));
    }

    public void tightenChain() {
        tightenChain(false);
    }

    public void tightenChain(boolean immediate) {
        moveToChainedPosition(immediate);

        if (null != mPredecessorTab) {
            // TODO: need to only add this once.
            mPredecessorTab.addOnPositionChangeListener(mOnPredecessorPositionChange);
        }
    }

    private void moveToChainedPosition(boolean immediate) {
        if (View.VISIBLE == mTab.getVisibility()) {
            if (immediate) {
                mTab.dockImmediately();
            } else {
                mTab.dock();
            }
        } else {
            mTab.moveTo(mTab.getDockPosition());
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
        if (null != mPredecessorTab) {
            mPredecessorTab.removeOnPositionChangeListener(mOnPredecessorPositionChange);
        }
        mTab.disappear(onUnchained);
    }
}
