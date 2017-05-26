package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.LayoutInflaterCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import io.mattcarroll.hover.R;

/**
 * TODO
 */
class ShadeView extends FrameLayout {

    private static final int FADE_DURATION = 250;

    public ShadeView(@NonNull Context context) {
        this(context, null);
    }

    public ShadeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_shade, this, true);
    }

    public void show() {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(this, "alpha", 1.0f);
        fadeOut.setDuration(FADE_DURATION);
        fadeOut.start();

        setVisibility(VISIBLE);
    }

    public void showImmediate() {
        setVisibility(VISIBLE);
    }

    public void hide() {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(this, "alpha", 0.0f);
        fadeOut.setDuration(FADE_DURATION);
        fadeOut.start();

        fadeOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
    }

    public void hideImmediate() {
        setVisibility(GONE);
    }
}
