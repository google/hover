package io.mattcarroll.hover.physics;

import android.graphics.Point;
import android.graphics.PointF;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.animation.FloatPropertyCompat;
import android.view.View;

import java.util.concurrent.TimeUnit;

import io.mattcarroll.hover.utils.ViewUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FlingViewWrapper extends AnimationViewWrapper {
    FlingAnimation mFlingAnimationX;
    FlingAnimation mFlingAnimationY;
    Disposable mDisposable;

    float mMaxX = sDefaultMaxValue;
    float mMaxY = sDefaultMaxValue;
    float mMinX = sDefaultMinValue;
    float mMinY = sDefaultMinValue;
    float mFriction = sDefaultFriction;
    float mScaleVelocity = sDefaultScaleVelocity;

    public FlingViewWrapper(View view) {
        super(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        createFlingAnimationX();
        createFlingAnimationY();

        mDisposable = getPublishSubject()
                .throttleLatest(50, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Point>() {
                    @Override
                    public void accept(Point point) throws Exception {
                        if (!isPause()) {
                            onStartAnimation(point);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        // no-op
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mFlingAnimationX != null) {
            mFlingAnimationX.cancel();
            mFlingAnimationX = null;
        }
        if (mFlingAnimationY != null) {
            mFlingAnimationY.cancel();
            mFlingAnimationY = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFlingAnimationX != null) {
            mFlingAnimationX.cancel();
            mFlingAnimationX = null;
        }
        if (mFlingAnimationY != null) {
            mFlingAnimationY.cancel();
            mFlingAnimationY = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        createFlingAnimationX();
        createFlingAnimationY();
    }

    public float normalize(float value, float minValue, float maxValue) {
        return Math.min(Math.max(value, minValue), maxValue);
    }

    public FlingViewWrapper setBoundValue(float maxX, float maxY, float minX, float minY) {
        this.mMaxX = maxX;
        this.mMaxY = maxY;
        this.mMinX = minX;
        this.mMinY = minY;
        return this;
    }

    public FlingViewWrapper setFriction(float friction) {
        this.mFriction = friction;
        return this;
    }

    public FlingViewWrapper setScaleVelocity(float scaleVelocity) {
        this.mScaleVelocity = scaleVelocity;
        return this;
    }

    private void createFlingAnimationX() {
        mFlingAnimationX = createFlingAnimation(getView(), DynamicAnimation.X)
                .addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
                    @Override
                    public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                        if (getOnActionListener() == null) return;
                        getOnActionListener().onMoveToX((int) value);
                    }
                })
                .addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                        if (getOnActionListener() == null) return;
                        getOnActionListener().onEndX();
                    }
                });
    }

    private void createFlingAnimationY() {
        mFlingAnimationY = createFlingAnimation(getView(), DynamicAnimation.Y)
                .addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
                    @Override
                    public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                        if (getOnActionListener() == null) return;
                        getOnActionListener().onMoveToY((int) value);
                    }
                })
                .addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                        if (getOnActionListener() == null) return;
                        getOnActionListener().onEndY();
                    }
                });
    }

    private void onStartAnimation(Point point) {
        Point fromPosition = ViewUtils.getLocationOnScreen(getView(), new PointF(0f, 0f));

        mFlingAnimationX.setStartVelocity(mScaleVelocity * (point.x - fromPosition.x))
                .setMaxValue(mMaxX)
                .setMinValue(mMinX)
                .start();

        mFlingAnimationY.setStartVelocity(mScaleVelocity * (point.y - fromPosition.y))
                .setMaxValue(mMaxY)
                .setMinValue(mMinY)
                .start();
    }

    private FlingAnimation createFlingAnimation(View view, FloatPropertyCompat<View> propertyCompat) {
        normalizeView(propertyCompat);
        return new FlingAnimation(view, propertyCompat)
                .setFriction(mFriction);
    }

    private void normalizeView(FloatPropertyCompat<View> propertyCompat) {
        if (propertyCompat == DynamicAnimation.X) {
            getView().setX(normalize(getView().getX(), mMinX, mMaxX));
        }
        if (propertyCompat == DynamicAnimation.Y) {
            getView().setY(normalize(getView().getY(), mMinY, mMaxY));
        }
    }

    private static float sDefaultFriction = 0.5f;
    private static float sDefaultScaleVelocity = 5f;
    private static float sDefaultMinValue = -10000;
    private static float sDefaultMaxValue = 10000;
}
