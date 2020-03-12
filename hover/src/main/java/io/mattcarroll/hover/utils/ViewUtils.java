package io.mattcarroll.hover.utils;

import android.view.View;

public class ViewUtils {

    private static final long DEFAULT_FADE_OUT_DURATION = 300L;
    private static final long DEFAULT_FADE_IN_DURATION = 400L;

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
}
