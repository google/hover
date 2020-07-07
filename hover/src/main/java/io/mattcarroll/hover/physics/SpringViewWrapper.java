package io.mattcarroll.hover.physics;

import android.graphics.Point;
import android.support.animation.DynamicAnimation;
import android.support.animation.FloatPropertyCompat;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.view.View;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SpringViewWrapper extends AnimationViewWrapper {
    private SpringAnimation mSpringAnimationX;
    private SpringAnimation mSpringAnimationY;
    private Disposable mDisposable;

    public SpringViewWrapper(View view) {
        super(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSpringAnimationX = createSpringAnimation(getView(), DynamicAnimation.X);
        mSpringAnimationY = createSpringAnimation(getView(), DynamicAnimation.Y);

        mDisposable = getPublishSubject()
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
        if (mSpringAnimationX != null) {
            mSpringAnimationX.cancel();
            mSpringAnimationX = null;
        }
        if (mSpringAnimationY != null) {
            mSpringAnimationY.cancel();
            mSpringAnimationY = null;
        }
    }

    public void addUpdateListener(DynamicAnimation.OnAnimationUpdateListener onAnimationUpdateListener) {
        mSpringAnimationX.addUpdateListener(onAnimationUpdateListener);
        mSpringAnimationY.addUpdateListener(onAnimationUpdateListener);
    }

    private void onStartAnimation(Point point) {
        mSpringAnimationX.animateToFinalPosition(point.x - getView().getWidth() / 2);
        mSpringAnimationY.animateToFinalPosition(point.y - getView().getHeight() / 2);
    }

    private SpringAnimation createSpringAnimation(View view, FloatPropertyCompat<View> propertyCompat) {
        SpringForce springForce = new SpringForce();
        springForce.setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);
        springForce.setStiffness(SpringForce.STIFFNESS_HIGH);
        return new SpringAnimation(view, propertyCompat).setSpring(springForce);
    }
}
