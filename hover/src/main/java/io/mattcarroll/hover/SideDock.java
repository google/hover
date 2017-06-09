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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 */
public class SideDock {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ LEFT, RIGHT })
    public @interface Side { }
    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    private float mVerticalDockPositionPercentage;
    @Side
    private int mSide;

    public SideDock(@NonNull Point dropPosition, @NonNull Point screenSize) {
        mVerticalDockPositionPercentage = (float) dropPosition.y / screenSize.y;
        mSide = dropPosition.x <= (screenSize.x / 2) ? LEFT : RIGHT;
    }

    public SideDock(float verticalDockPositionPercentage, @Side int side) {
        mVerticalDockPositionPercentage = verticalDockPositionPercentage;
        mSide = side;
    }

    public Point calculateDockPosition(@NonNull Point screenSize, int tabSize) {
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
