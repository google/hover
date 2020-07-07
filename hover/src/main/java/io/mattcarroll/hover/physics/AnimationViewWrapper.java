package io.mattcarroll.hover.physics;

import android.graphics.Point;
import android.graphics.PointF;
import android.view.View;

import io.reactivex.subjects.PublishSubject;

public abstract class AnimationViewWrapper {
    private PublishSubject<Point> mPublishSubject;
    private View mView;
    private boolean mIsPause = false;
    OnActionListener mOnActionListener;

    protected AnimationViewWrapper(View view) {
        mView = view;
    }

    public void onStart() {
        mPublishSubject = PublishSubject.create();
    }

    public void onDestroy() {
        //mView = null;
    }

    public void onPause() {
        mIsPause = true;
    }

    public void onResume() {
        mIsPause = false;
    }

    public void setOnActionListener(OnActionListener onActionListener) {
        mOnActionListener = onActionListener;
    }

    public boolean isPause() {
        return mIsPause;
    }

    protected View getView() {
        return mView;
    }

    protected PublishSubject<Point> getPublishSubject() {
        return mPublishSubject;
    }

    protected OnActionListener getOnActionListener() {
        return mOnActionListener;
    }

    // Point with center anchor
    public void updatePosition(Point point) {
        mPublishSubject.onNext(point);
    }

    // Point with any anchor
    public void updatePosition(Point point, PointF anchor) {
        point.x -= mView.getWidth() * anchor.x;
        point.y -= mView.getHeight() * anchor.y;
        mPublishSubject.onNext(point);
    }

    public interface OnActionListener {
        void onMoveToX(int x);

        void onMoveToY(int y);

        void onEndX();

        void onEndY();
    }
}
