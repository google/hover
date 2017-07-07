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

/**
 * Reports user drag behavior on the screen to a {@link DragListener}.
 */
public interface Dragger {

    /**
     * Starts reporting user drag behavior given a drag area represented by {@code controlBounds}.
     * @param dragListener listener that receives information about drag behavior
     * @param dragStartCenterPosition initial touch point to start dragging
     */
    void activate(@NonNull DragListener dragListener, @NonNull Point dragStartCenterPosition);

    /**
     * Changes the position of this Dragger's touch area to the given position.
     * @param position new position of touch area
     */
    void moveTo(@NonNull Point position);

    /**
     * Stops monitoring and reporting user drag behavior.
     */
    void deactivate();

    /**
     * Enable/Disable debug mode.  In debug mode this Dragger will paint its touch area with a
     * translucent color.
     * @param debugMode true for debug mode, false otherwise
     */
    void enableDebugMode(boolean debugMode);

    interface DragListener {

        /**
         * The user has pressed within the draggable area at the given position.
         * @param x x-coordinate of the user's press (in the parent View's coordinate space)
         * @param y y-coordiante of the user's press (in the parent View's coordinate space)
         */
        void onPress(float x, float y);

        /**
         * The user has begun dragging.
         * @param x x-coordinate of the user's drag start (in the parent View's coordinate space)
         * @param y y-coordiante of the user's drag start (in the parent View's coordinate space)
         */
        void onDragStart(float x, float y);

        /**
         * The user has dragged to the given coordinates.
         * @param x x-coordinate of the user's drag (in the parent View's coordinate space)
         * @param y y-coordiante of the user's drag (in the parent View's coordinate space)
         */
        void onDragTo(float x, float y);

        /**
         * The user has stopped touching the drag area.
         * @param x x-coordinate of the user's release (in the parent View's coordinate space)
         * @param y y-coordiante of the user's release (in the parent View's coordinate space)
         */
        void onReleasedAt(float x, float y);

        /**
         * The user tapped the drag area (instead of dragging it).
         */
        void onTap();

    }
}
