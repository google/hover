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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@link Dock} that always positions itself either on the left or right side of its container. A
 * {@code SideDock} insets itself slightly from the edge of its container based on a proportion of
 * a tab's size.
 *
 * A {@link SideDock} also constrains its position slightly within the top and bottom of its
 * container so that the docking position can't be lost off-screen.
 */
public class SideDock extends Dock {

    private static final String TAG = "SideDock";

    private ViewGroup mContainerView;
    private int mTabSize;
    private SidePosition mSidePosition;

    SideDock(@NonNull ViewGroup containerView, int tabSize, @NonNull SidePosition sidePosition) {
        mContainerView = containerView;
        mTabSize = tabSize;
        mSidePosition = sidePosition;
    }

    @NonNull
    @Override
    public Point position() {
        Point screenSize = new Point(mContainerView.getWidth(), mContainerView.getHeight());
        return mSidePosition.calculateDockPosition(screenSize, mTabSize);
    }

    @NonNull
    public SidePosition sidePosition() {
        return mSidePosition;
    }

    @Override
    public String toString() {
        return mSidePosition.toString();
    }

    public static class SidePosition {

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({ LEFT, RIGHT })
        public @interface Side { }
        public static final int LEFT = 0;
        public static final int RIGHT = 1;

        @Side
        private int mSide;
        private float mVerticalDockPositionPercentage;

        public SidePosition(@Side int side, float verticalDockPositionPercentage) {
            mSide = side;
            mVerticalDockPositionPercentage = verticalDockPositionPercentage;
        }

        public Point calculateDockPosition(@NonNull Point screenSize, int tabSize) {
            Log.d(TAG, "Calculating dock position. Screen size: " + screenSize + ", tab size: " + tabSize);
            int x = LEFT == mSide
                    ? ((int) (tabSize * 0.25))
                    : screenSize.x - ((int) (tabSize * 0.25));

            int y = (int) (screenSize.y * mVerticalDockPositionPercentage);

            // Prevent the dock from sitting completely above or below the screen area.
            int minY = (int) (tabSize * 0.25);
            int maxY = (int) (screenSize.y - (tabSize * 0.25));
            y = Math.max(y, minY);
            y = Math.min(y, maxY);

            return new Point(x, y);
        }

        public float getVerticalDockPositionPercentage() {
            return mVerticalDockPositionPercentage;
        }

        @Side
        public int getSide() {
            return mSide;
        }

        @Override
        public String toString() {
            String side = LEFT == mSide ? "Left" : "Right";
            int percent = (int) Math.floor(mVerticalDockPositionPercentage * 100);
            return String.format("%s side at %d%%", side, percent);
        }
    }
}
