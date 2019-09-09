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
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Fullscreen View that provides an exit "drop zone" for users to exit the Hover Menu.
 */
class ExitView extends RelativeLayout {

    private static final String TAG = "ExitView";

    private static final int FADE_DURATION = 300;
    private int mExitRadiusInPx;
    private View mExitIcon;
    private View mExitGradient;
    private ViewGroup mVgExit;
    //    private boolean mAnimated = false;
//    public ViewPropertyAnimator exitEnterAnim;
//    public ViewPropertyAnimator exitExitAnim;
    public ObjectAnimator anim1 = null;
    public ObjectAnimator anim2 = null;
    public boolean isExitAnimated = false;
    private float mDefaultScaleX = 1.0f;
    private float mDefaultScaleY = 1.0f;
    private float mDefaultRotation = 0f;

    public ExitView(@NonNull Context context) {
        this(context, null);
    }

    public ExitView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_hover_menu_exit, this, true);

        mExitIcon = findViewById(R.id.view_exit);
        mVgExit = findViewById(R.id.vg_exit);
        mExitGradient = findViewById(R.id.view_exit_gradient);
        mExitRadiusInPx = getResources().getDimensionPixelSize(R.dimen.hover_exit_radius);

        setAnimations();
    }

    private void setAnimations() {
        PropertyValuesHolder pv1 = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.5f);
        PropertyValuesHolder pv2 = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.5f);
        PropertyValuesHolder pv3 = PropertyValuesHolder.ofFloat("rotation", 0f, 90f);
        anim1 = ObjectAnimator.ofPropertyValuesHolder(mExitIcon, pv1, pv2, pv3);
        anim1.setDuration(300L);
        anim1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                Log.d(TAG, "anim1 onAnimationStart");
                mExitIcon.setScaleY(mDefaultScaleY);
                mExitIcon.setScaleX(mDefaultScaleX);
                mExitIcon.setRotation(mDefaultRotation);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Log.d(TAG, "anim1 onAnimationEnd");
            }

            @Override
            public void onAnimationCancel(Animator animator) {
//                initExitButtonProperties();
                Log.d(TAG, "anim1 onAnimationCancel");
                mExitIcon.setScaleY(mDefaultScaleY);
                mExitIcon.setScaleX(mDefaultScaleX);
                mExitIcon.setRotation(mDefaultRotation);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                Log.d(TAG, "anim1 onAnimationRepeat");
            }
        });

        PropertyValuesHolder pva1 = PropertyValuesHolder.ofFloat("scaleX", 1.5f, 1.0f);
        PropertyValuesHolder pva2 = PropertyValuesHolder.ofFloat("scaleY", 1.5f, 1.0f);
        PropertyValuesHolder pva3 = PropertyValuesHolder.ofFloat("rotation", 90f, 0f);
        anim2 = ObjectAnimator.ofPropertyValuesHolder(mExitIcon, pva1, pva2, pva3);
        anim2.setDuration(300L);
        anim2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                Log.d(TAG, "anim2 onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Log.d(TAG, "anim2 onAnimationEnd");
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                Log.d(TAG, "anim2 onAnimationCancel");

            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                Log.d(TAG, "anim2 onAnimationRepeat");
            }
        });
    }

    public boolean isInExitZone(@NonNull Point position, @NonNull Point screenSize) {
        int exitXExcludeThresholdLeft = screenSize.x / 18;
        int exitXExcludeThresholdRight = screenSize.x * 17 / 18;

        Rect exitArea = new Rect(
                0 - mExitIcon.getWidth(),
                screenSize.y * 4 / 6,
                screenSize.x + mExitIcon.getWidth(),
                screenSize.y + mExitIcon.getHeight()
        );

        Rect excludedXExitArea = new Rect(
                0 - mExitIcon.getWidth(),
                screenSize.y * 4 / 6,
                exitXExcludeThresholdLeft,
                screenSize.y * 5 / 6
        );

        Rect excludedXExitArea2 = new Rect(
                exitXExcludeThresholdRight,
                screenSize.y * 4 / 6,
                screenSize.x + mExitIcon.getWidth(),
                screenSize.y * 5 / 6
        );

        return exitArea.contains(position.x, position.y)
                && !excludedXExitArea.contains(position.x, position.y)
                && !excludedXExitArea2.contains(position.x, position.y);
    }

    private Point getExitZoneCenter() {
        return new Point(
                (int) (mExitIcon.getX() + (mExitIcon.getWidth() / 2)),
                (int) (mExitIcon.getY() + (mExitIcon.getHeight() / 2))
        );
    }

    private double calculateDistance(@NonNull Point p1, @NonNull Point p2) {
        return Math.sqrt(
                Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2)
        );
    }

    public void startEnterExitAnim() {
        if ((anim1 == null || !anim1.isRunning()) && !isExitAnimated) {

//                if (anim2 != null && anim2.isRunning()) {
//                    anim2.cancel();
//                }
            anim1.start();
            isExitAnimated = true;
        }
    }

    public void startExitExitAnim() {
        if ((anim2 == null || !anim2.isRunning()) && isExitAnimated) {
//                if (anim1 != null && anim1.isRunning()) {
//                    anim1.cancel();
//                }
            anim2.start();
            isExitAnimated = false;
        }
    }

    public void testEnterExitRange(Point position) {
//        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mExitIcon, "scaleX", 1.0f, 1.5f);
//        fadeOut.setDuration(FADE_DURATION);
//        fadeOut.start();
//
//
//        ObjectAnimator aa = ObjectAnimator.ofFloat(mExitIcon, "scaleY", 1.0f, 1.5f);
//        aa.setDuration(FADE_DURATION);
//        aa.start();

//        if (anim1 != null) {
//            Log.d(TAG, "enterExitRange anim1.isRunning = " + anim1.isRunning());
//        }

        // case 1 done but seems slow.
//        if (isInExitZone(position)) {
//            if ((anim1 == null || !anim1.isRunning()) && !isExitAnimated) {
//
////                if (anim2 != null && anim2.isRunning()) {
////                    anim2.cancel();
////                }
//                PropertyValuesHolder pv1 = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.5f);
//                PropertyValuesHolder pv2 = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.5f);
//                PropertyValuesHolder pv3 = PropertyValuesHolder.ofFloat("rotation", 0f, 90f);
//                anim1 = ObjectAnimator.ofPropertyValuesHolder(mExitIcon, pv1, pv2, pv3);
//                anim1.setDuration(300L);
//                anim1.start();
//                isExitAnimated = true;
//            }
//        } else {
//            if ((anim2 == null || !anim2.isRunning()) && isExitAnimated) {
////                if (anim1 != null && anim1.isRunning()) {
////                    anim1.cancel();
////                }
//                PropertyValuesHolder pv1 = PropertyValuesHolder.ofFloat("scaleX", 1.5f, 1.0f);
//                PropertyValuesHolder pv2 = PropertyValuesHolder.ofFloat("scaleY", 1.5f, 1.0f);
//                PropertyValuesHolder pv3 = PropertyValuesHolder.ofFloat("rotation", 90f, 0f);
//                anim2 = ObjectAnimator.ofPropertyValuesHolder(mExitIcon, pv1, pv2, pv3);
//                anim2.setDuration(300L);
//                anim2.start();
//                isExitAnimated = false;
//            }
//        }

//        mExitIcon.rotation


//        if (isInExitZone(position) && !mAnimated) {
//            Log.d(TAG, "enterExitRange 1 isInExitZone(position) = " + isInExitZone(position) + ", Animated = " + mAnimated);
//
////            if (exitEnterAnim != null)
//            exitEnterAnim = mExitIcon.animate()
//                    .scaleXBy(0.5f)
//                    .scaleYBy(0.5f)
//                    .rotationBy(90)
//                    .setDuration(300);
//            exitEnterAnim.start();
//            mAnimated = true;
//        } else if (mAnimated) {
//            Log.d(TAG, "enterExitRange 2 isInExitZone(position) = " + isInExitZone(position) + ", Animated = " + mAnimated);
//            exitExitAnim = mExitIcon.animate()
//                    .scaleXBy(0f)
//                    .scaleYBy(0f)
//                    .rotationBy(-90)
//                    .setDuration(300);
//            exitExitAnim.start();
//            mAnimated = false;
//
//        }

//        Log.d(TAG, "enterExitRange mExitIcon.getAnimation() == null = " + (mExitIcon.getAnimation() == null));
//        if (isInExitZone(position) && !mAnimated) {
//            Log.d(TAG, "enterExitRange 1 isInExitZone(position) = " + isInExitZone(position) + ", Animated = " + mAnimated);
//
////            if (exitEnterAnim != null)
//            if (mExitIcon.getAnimation() == null || mExitIcon.getAnimation().hasEnded()) {
//                exitEnterAnim = mExitIcon.animate()
//                        .scaleXBy(0.5f)
//                        .scaleYBy(0.5f)
//                        .rotationBy(90)
//                        .setDuration(300);
//                exitEnterAnim.start();
//                mAnimated = true;
//            }
//        } else if (mAnimated) {
//            Log.d(TAG, "enterExitRange 2 isInExitZone(position) = " + isInExitZone(position) + ", Animated = " + mAnimated);
//            if (mExitIcon.getAnimation() == null || mExitIcon.getAnimation().hasEnded()) {
//                exitExitAnim = mExitIcon.animate()
//                        .scaleXBy(0f)
//                        .scaleYBy(0f)
//                        .rotationBy(-90)
//                        .setDuration(300);
//                exitExitAnim.start();
//                mAnimated = false;
//            }
//        }

        setVisibility(VISIBLE);
    }

    public void show() {
        resetExitButtonAnimation();

        ObjectAnimator exitGradientAnimator = ObjectAnimator.ofFloat(mExitGradient, "alpha", EXIT_VIEW_TARGET_ALPHA);
        exitGradientAnimator.setDuration(300L);
        exitGradientAnimator.start();

        ObjectAnimator vgExitAnimator = ObjectAnimator.ofFloat(mVgExit, "y", EXIT_VIEW_BASE_Y, EXIT_VIEW_TARGET_Y);
        vgExitAnimator.setDuration(300L);
        vgExitAnimator.start();

        setVisibility(VISIBLE);
    }

    public void resetExitButtonAnimation() {
        Log.d(TAG, "resetExitButtonAnimation");
        isExitAnimated = false;
        mExitIcon.setScaleY(mDefaultScaleY);
        mExitIcon.setScaleX(mDefaultScaleX);
        mExitIcon.setRotation(mDefaultRotation);
    }

    public static final float EXIT_VIEW_TARGET_ALPHA = 1.0f;
    public static final float EXIT_VIEW_TARGET_Y = 0f;

    public static final float EXIT_VIEW_BASE_ALPHA = 0f;
    public static final float EXIT_VIEW_BASE_Y = 800f;

    public void hide() {
        ObjectAnimator vgExitAnimator = ObjectAnimator.ofFloat(mVgExit, "y", EXIT_VIEW_TARGET_Y, EXIT_VIEW_BASE_Y);
        vgExitAnimator.setDuration(FADE_DURATION);
        vgExitAnimator.start();

        ObjectAnimator exitGradientAnimator = ObjectAnimator.ofFloat(mExitGradient, "alpha", EXIT_VIEW_BASE_ALPHA);
        exitGradientAnimator.setDuration(FADE_DURATION);
        exitGradientAnimator.start();

        exitGradientAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }
}
