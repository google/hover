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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

/**
 * Fullscreen {@code View} that appears behind the other visual elements in a {@link HoverView} and
 * darkens the background.
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
