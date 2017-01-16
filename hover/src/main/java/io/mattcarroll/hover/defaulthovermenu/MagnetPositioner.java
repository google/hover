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

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.Interpolator;

/**
 * Pulls a {@link Positionable} to the side of given {@code displayBounds} with animated motion.
 */
public class MagnetPositioner {

    private static final String TAG = "HoverMenuWindowSidePuller";

    private DisplayMetrics mDisplayMetrics;
    private Positionable mPositionable;
    private ValueAnimator mValueAnimator;
    private Rect mStartingBounds;
    private Rect mAnchoredBounds;
    private OnCompletionListener mCompletionListener;
    private ValueAnimator.AnimatorUpdateListener mPullUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int newX = mStartingBounds.left + (int) ((mAnchoredBounds.left - mStartingBounds.left) * valueAnimator.getAnimatedFraction());
            int newY = mStartingBounds.top + (int) ((mAnchoredBounds.top - mStartingBounds.top) * valueAnimator.getAnimatedFraction());
            mPositionable.setPosition(new Point(newX, newY));
        }
    };
    private Animator.AnimatorListener mPullAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) { }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (null != mCompletionListener) {
                mCompletionListener.onPullToSideCompleted();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) { }

        @Override
        public void onAnimationRepeat(Animator animation) { }
    };

    public MagnetPositioner(@NonNull DisplayMetrics displayMetrics, @NonNull Positionable positionable, @Nullable OnCompletionListener completionListener) {
        mDisplayMetrics = displayMetrics;
        mPositionable = positionable;
        mCompletionListener = completionListener;
    }

    public Point pullToAnchor(@NonNull CollapsedMenuAnchor anchor, @NonNull Rect viewToPullBounds, @NonNull Interpolator pullInterpolator) {
        Log.d(TAG, "Pulling to side. Anchor - side: " + anchor.getAnchorSide() + ", normalized Y: " + anchor.getAnchorNormalizedY());
        mStartingBounds = new Rect(viewToPullBounds);
        mAnchoredBounds = anchor.anchor(viewToPullBounds);
        Log.d(TAG, "Anchored bounds to pull to: (" + mAnchoredBounds.left + ", " + mAnchoredBounds.top + ")");
        animateToAnchorPosition(viewToPullBounds, mAnchoredBounds, pullInterpolator);
        return new Point(mAnchoredBounds.left, mAnchoredBounds.top);
    }

    private void animateToAnchorPosition(@NonNull Rect viewToPullBounds, @NonNull Rect anchoredBounds, @NonNull Interpolator interpolator) {
        Log.d(TAG, "animateToAnchorPosition() - from X: " + viewToPullBounds.left + ", to X: " + anchoredBounds.left);

        int timeForAnimation = getTimeForAnimationDistance(getDistanceBetweenTwoPoints(
                viewToPullBounds.left, viewToPullBounds.top,
                anchoredBounds.left, anchoredBounds.top
        ));

        if (null != mValueAnimator && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
        mValueAnimator = new ValueAnimator();
        mValueAnimator.setFloatValues(0.0f, 1.0f);
        mValueAnimator.setInterpolator(interpolator);
        mValueAnimator.setDuration(timeForAnimation);
        mValueAnimator.addUpdateListener(mPullUpdateListener);
        mValueAnimator.addListener(mPullAnimatorListener);
        mValueAnimator.start();
    }

    private double getDistanceBetweenTwoPoints(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private int getTimeForAnimationDistance(double distanceInPx) {
        double speedInDpPerSecond = 1000;
        double distanceInDp = distanceInPx / mDisplayMetrics.density;
        final int timingOffset = 200; // To give some time at front and back of animation regardless of how close we are to destination.

        return (int) ((distanceInDp / speedInDpPerSecond) * 1000) + timingOffset;
    }

    public interface OnCompletionListener {
        void onPullToSideCompleted();
    }
}
