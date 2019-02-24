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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@link Dock} that always positions itself either on the left or right side of its container. A
 * {@code SideDock} insets itself slightly from the edge of its container based on a proportion of
 * a tab's size.
 */
public class SideDock extends Dock {

    private static final String TAG = "SideDock";

    private HoverView mHoverView;
    private int mTabSize;
    private SidePosition mSidePosition;

    SideDock(@NonNull HoverView hoverView, int tabSize, @NonNull SidePosition sidePosition) {
        mHoverView = hoverView;
        mTabSize = tabSize;
        mSidePosition = sidePosition;
    }

    @NonNull
    @Override
    public Point position() {
        return mSidePosition.calculateDockPosition(mHoverView.getScreenSize(), mTabSize);
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
