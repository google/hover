package io.mattcarroll.hover.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.View;

public class ViewUtils {

    private static final long DEFAULT_FADE_OUT_DURATION = 300L;
    private static final long DEFAULT_FADE_IN_DURATION = 400L;
    private static final long DEFAULT_SCALE_DURATION = 150L;

    private ViewUtils() {

    }

    public static void fadeOut(View view) {
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(0f)
                .setDuration(DEFAULT_FADE_OUT_DURATION)
                .setListener(null);
    }

    public static void fadeIn(View view) {
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(DEFAULT_FADE_IN_DURATION)
                .setListener(null);
    }

    public static void scale(View view, float scaleValue) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", scaleValue);
        scaleX.setDuration(DEFAULT_SCALE_DURATION);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", scaleValue);
        scaleY.setDuration(DEFAULT_SCALE_DURATION);
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }

    public static void scaleAfter(View view, float scaleValue) {
        final AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", scaleValue);
        scaleX.setDuration(DEFAULT_SCALE_DURATION);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", scaleValue);
        scaleY.setDuration(DEFAULT_SCALE_DURATION);
        animatorSet.playTogether(scaleX, scaleY);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                animatorSet.start();
            }
        }, DEFAULT_SCALE_DURATION);
    }

    public static Point getLocationOnScreen(View view, PointF anchor) {
        int[] result = new int[2];
        view.getLocationOnScreen(result);
        return new Point(result[0] + (int) (anchor.x * view.getMeasuredWidth()),
                result[1] + (int) (anchor.y + view.getMeasuredHeight()));
    }
}
