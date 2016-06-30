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
package io.mattcarroll.hover.defaulthovermenu;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

/**
 * Represents an anchor position on either the {@link #LEFT} or {@link #RIGHT} side of given display
 * bounds.
 *
 * Before calculating any anchor positions, this {@code CollapsedMenuAnchor} must be given the display
 * bounds that it needs to anchor to. To do this, use {@link #setDisplayBounds(Rect)}.
 *
 * Multiple methods are provided for setting the anchor position of this {@code CollapsedMenuAnchor}.
 * - {@link #setAnchorAt(int, float)} directly sets the state of this anchor by providing a desired
 * anchored side (left or right), as well as a desired y-position (normalized).
 * - {@link #setAnchorByInterpolation(PointF)} takes an (x, y) position that is normalized, and then
 * generates an anchor position corresponding to the side closest to the given position.
 * - {@link #setAnchorByInterpolation(Rect)} takes the bounds of an item in the display region and
 * generates an anchor position corresponding to the side closest to the given bounds.
 *
 * Once the anchor state is set, an anchor position for a given item can be generated using
 * {@link #anchor(Rect)}. The reason that {@link #anchor(Rect)} takes an item's bounds is because
 * this anchor will align the vertical center of the given bounds with the desired anchor position.
 */
public class CollapsedMenuAnchor {

    private static final String TAG = "CollapsedMenuAnchor";

    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    private final int mAnchorSideOffset; // How far beyond the left/right bounds should the anchor sit.
    private Rect mDisplayBounds;
    private int mSide = LEFT;
    private float mNormalizedY = 0f;

    public CollapsedMenuAnchor(@NonNull DisplayMetrics displayMetrics, int anchorSideOffsetInDp) {
        mAnchorSideOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, anchorSideOffsetInDp, displayMetrics);
    }

    /**
     * Sets the bounds of the display that this anchor is intended to apply to. When this anchor
     * applies a {@link #LEFT} or {@link #RIGHT} side, it uses the {@code displayBounds} left and
     * right sides.  When this anchor calculates a y-position, it applies its normalized y-position
     * to the given {@code displayBounds}' vertical region.
     *
     * @param displayBounds bounds of the display area
     */
    public void setDisplayBounds(@NonNull Rect displayBounds) {
        Log.d(TAG, "setDisplayBounds() - width: " + displayBounds.width() + ", height: " + displayBounds.height());
        mDisplayBounds = displayBounds;
    }

    /**
     * Returns the side that this anchor is tied to.
     * @return left or right
     */
    public int getAnchorSide() {
        return mSide;
    }

    /**
     * Returns the normalized y-position that this anchor is tied to. The normalized y-position
     * represents the vertical position on the screen, as a percentage, of where a given items
     * center should be located.
     *
     * @return y-position of anchor that has been normalized
     */
    public float getAnchorNormalizedY() {
        return mNormalizedY;
    }

    /**
     * Sets the internal anchor state of this {@code CollapsedMenuAnchor}.
     *
     * @param side left or right side
     * @param normalizedY y-position normalized: [0.0, 1.0]
     */
    public void setAnchorAt(int side, float normalizedY) {
        mSide = side;
        mNormalizedY = normalizedY;
        Log.d(TAG, "setAnchorAt() - side: " + mSide + ", normalized Y: " + mNormalizedY);
    }

    /**
     * Sets the anchor position of this {@code CollapsedMenuAnchor} by interpolating an anchor
     * position from an existing item's bounds. If the item is closer to the left side of the display
     * than the right side, then the anchor is set on the left side and vice versa. The y-position
     * of the given item will remain the same (though the anchor is based on the vertical center
     * of the given {@code bounds}, not the top or bottom).
     *
     * @param bounds of an item in the display
     */
    public void setAnchorByInterpolation(@NonNull Rect bounds) {
        if (null == mDisplayBounds) {
            throw new IllegalStateException("Display bounds must be provided before interpolating an anchor position.");
        }

        mSide = (float) getCenter(bounds).x / mDisplayBounds.width() < 0.5 ? LEFT : RIGHT;
        mNormalizedY = (float) getCenter(bounds).y / mDisplayBounds.height();
        Log.d(TAG, "setAnchorByInterpolation() - Top Y: " + bounds.top + ", height: " + bounds.height() + ", Center Y: " + getCenter(bounds).y + ", height: " + mDisplayBounds.height() + ", normalized Y: " + mNormalizedY);
    }

    /**
     * Sets the anchor position of this {@code CollapsedMenuAnchor} by interpolating an anchor
     * position from a normalized (x,y) position. For example, if the normalized position (0.24, 0.7)
     * were given, then the resulting anchor position would be (left, 0.7) because this anchor
     * operates by pulling items to the sides of the display.
     *
     * @param normalizedPosition (x,y) coordinate with normalized values: [0.0,1.0]
     */
    public void setAnchorByInterpolation(@NonNull PointF normalizedPosition) {
        mSide = normalizedPosition.x < 0.5f ? LEFT : RIGHT;
        mNormalizedY = normalizedPosition.y;
        Log.d(TAG, "setAnchorByInterpolation() - Normalized Position: (" + normalizedPosition.x + ", " + normalizedPosition.y + "), Side: " + mSide + ", normalized Y: " + mNormalizedY);
    }

    /**
     * Given the {@code bounds} of an item on the screen, {@link #anchor(Rect)} will determine where that
     * item should be anchored and return those new bounds. The returned anchor position is based on
     * the display bounds (set in {@link #setDisplayBounds(Rect)}), the internal anchor state of this
     * {@code CollapsedMenuAnchor}, and the size of the given {@code bounds}.
     *
     * When anchoring the given {@code bounds}, the resulting bounds will be within the given
     * display bounds.
     *
     * @param bounds bounds of an item on the screen
     * @return new bounds representing where the item should be anchored
     */
    public Rect anchor(@NonNull Rect bounds) {
        Log.d(TAG, "Creating a new anchor position on the " + (LEFT == mSide ? "LEFT" : "RIGHT") + " side at a normalized vertical position: " + mNormalizedY);
        Log.d(TAG, "Anchoring bounds with height: " + bounds.height());

        Rect anchoredBounds = new Rect(bounds);

        // Anchor to either the left or right wall of the display.
        if (LEFT == mSide) {
            // Left side
            anchoredBounds.offsetTo(
                    0 - mAnchorSideOffset,
                    anchoredBounds.top
            );
        } else {
            // Right side
            anchoredBounds.offsetTo(
                    mDisplayBounds.right + mAnchorSideOffset - bounds.width(),
                    anchoredBounds.top
            );
        }

        // Anchor to the desired y-position.
        anchoredBounds.offsetTo(
                anchoredBounds.left,
                (int) (mNormalizedY * mDisplayBounds.height()) - (bounds.height() / 2)
        );

        // Ensure that the icon does not exceed the vertical bounds of the display.
        if (anchoredBounds.top < 0) {
            anchoredBounds.offsetTo(
                    anchoredBounds.left,
                    0
            );
        } else if (anchoredBounds.bottom > mDisplayBounds.bottom) {
            anchoredBounds.offsetTo(
                    anchoredBounds.left,
                    mDisplayBounds.bottom - bounds.height()
            );
        }

        return anchoredBounds;
    }

    private Point getCenter(@NonNull Rect bounds) {
        return new Point(
                bounds.left + (bounds.width() / 2),
                bounds.top + (bounds.height() / 2)
        );
    }
}
