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
package io.mattcarroll.hover.hoverdemo.introduction;

import android.graphics.PointF;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

/**
 * Moves and scales a View as if its hovering.
 */
public class HoverMotion {

    private static final String TAG = "HoverMotion";

    private static final int RENDER_CYCLE_IN_MILLIS = 16; // 60 FPS.

    private View mView;
    private BrownianMotionGenerator mBrownianMotionGenerator = new BrownianMotionGenerator();
    private GrowAndShrinkGenerator mGrowAndShrinkGenerator = new GrowAndShrinkGenerator(0.05f);
    private boolean mIsRunning;
    private long mTimeOfLastUpdate;
    private int mDtInMillis;
    private Runnable mStateUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIsRunning) {
                // Calculate the time that's passed since the last update.
                mDtInMillis = (int) (SystemClock.elapsedRealtime() - mTimeOfLastUpdate);

                // Update visual state.
                updatePosition();
                updateGrowth();

                // Update time tracking.
                mTimeOfLastUpdate = SystemClock.elapsedRealtime();

                // Schedule next loop.
                mView.postDelayed(this, RENDER_CYCLE_IN_MILLIS);
            }
        }
    };

    public void start(@NonNull View view) {
        Log.d(TAG, "start()");
        mView = view;
        mIsRunning = true;
        mTimeOfLastUpdate = SystemClock.elapsedRealtime();
        mView.post(mStateUpdateRunnable);
    }

    public void stop() {
        Log.d(TAG, "stop()");
        mIsRunning = false;
    }

    private void updatePosition() {
        // Calculate a new position and move the View.
        PointF mPositionOffset = mBrownianMotionGenerator.applyMotion(mDtInMillis);
        mView.setTranslationX(mPositionOffset.x);
        mView.setTranslationY(mPositionOffset.y);
    }

    private void updateGrowth() {
        // Calculate and apply scaling.
        float scale = mGrowAndShrinkGenerator.applyGrowth(mDtInMillis);
        mView.setScaleX(scale);
        mView.setScaleY(scale);

        // Set elevation based on scale (the bigger, the higher).
        int baseElevation = 50;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mView.setElevation(baseElevation * scale);
        }
    }

    public static class BrownianMotionGenerator {

        private static final float FRICTION = 0.8f;

        private int mMaxDisplacementInPixels = 200;
        private PointF mPosition = new PointF(0, 0);
        private PointF mVelocity = new PointF(0, 0);

        public PointF applyMotion(int dtInMillis) {
            float xConstraintAdditive = mPosition.x / mMaxDisplacementInPixels;
            float yConstraintAdditive = mPosition.y / mMaxDisplacementInPixels;

            // Randomly adjust velocity.
            float velocityXAdjustment = (float) (Math.random() * 1.0 - 0.5) - xConstraintAdditive;
            float velocityYAdjustment = (float) (Math.random() * 1.0 - 0.5) - yConstraintAdditive;
            mVelocity.offset(velocityXAdjustment, velocityYAdjustment);

            // Apply velocity to position.
            mPosition.offset(mVelocity.x, mVelocity.y);

            // Apply friction to velocity.
            mVelocity.set(mVelocity.x * FRICTION, mVelocity.y * FRICTION);

            return mPosition;
        }

    }

    public static class GrowAndShrinkGenerator {

        private static final int GROWTH_CYCLE_IN_MILLIS = 5000;

        private int mLastTimeInMillis;
        private float mGrowthFactor;

        public GrowAndShrinkGenerator(float growthFactor) {
            mGrowthFactor = growthFactor;
        }

        public float applyGrowth(int dtInMillis) {
            mLastTimeInMillis += dtInMillis;
            return 1.0f + (float) (Math.sin(Math.PI * ((float) mLastTimeInMillis / GROWTH_CYCLE_IN_MILLIS)) * mGrowthFactor);
        }

    }
}
