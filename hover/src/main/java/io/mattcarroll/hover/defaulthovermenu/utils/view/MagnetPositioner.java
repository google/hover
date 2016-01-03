package io.mattcarroll.hover.defaulthovermenu.utils.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.BounceInterpolator;

import io.mattcarroll.hover.defaulthovermenu.CollapsedMenuAnchor;

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
//            Log.d(TAG, "onAnimationUpdate() - Setting position: (" + ((int) valueAnimator.getAnimatedValue()) + ", " + mAnchoredBounds.top + ")");
//            mPositionable.setPosition(new Point((int) valueAnimator.getAnimatedValue(), mAnchoredBounds.top));

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

    public Point pullToAnchor(@NonNull CollapsedMenuAnchor anchor, @NonNull Rect viewToPullBounds) {
        Log.d(TAG, "Pulling to side. Anchor - side: " + anchor.getAnchorSide() + ", normalized Y: " + anchor.getAnchorNormalizedY());
        mStartingBounds = new Rect(viewToPullBounds);
        mAnchoredBounds = anchor.anchor(viewToPullBounds);
        Log.d(TAG, "Anchored bounds to pull to: (" + mAnchoredBounds.left + ", " + mAnchoredBounds.top + ")");
        animateToAnchorPosition(viewToPullBounds, mAnchoredBounds);
        return new Point(mAnchoredBounds.left, mAnchoredBounds.top);
    }

    private void animateToAnchorPosition(@NonNull Rect viewToPullBounds, @NonNull Rect anchoredBounds) {
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
        mValueAnimator.setInterpolator(new BounceInterpolator());
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
