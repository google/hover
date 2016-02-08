package io.mattcarroll.hover.defaulthovermenu;

import android.graphics.Rect;
import android.support.annotation.NonNull;

/**
 * Reports user drag behavior on the screen to a {@link DragListener}.
 */
public interface Dragger {

    /**
     * Starts reporting user drag behavior given a drag area represented by {@code controlBounds}.
     * @param dragListener listener that receives information about drag behavior
     * @param controlBounds the initial area that is draggable (this area will move as the user drags)
     */
    void activate(@NonNull DragListener dragListener, @NonNull Rect controlBounds);

    /**
     * Stops monitoring and reporting user drag behavior.
     */
    void deactivate();

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
