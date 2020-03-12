package io.mattcarroll.hover;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class CloseImageView extends View {
    Paint mPaint;
    Paint mFillPaint;
    float mStrokeWidth = 3f;

    public CloseImageView(Context context) {
        super(context);
        init();
    }

    public CloseImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CloseImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        float radius = Math.min(width, height) / 2f;

        canvas.drawCircle(width / 2f, height / 2f, radius - mStrokeWidth, mPaint);
        canvas.drawCircle(width / 2f, height / 2f, radius - mStrokeWidth, mFillPaint);

        float centerX = width / 2f;
        float centerY = height / 2f;

        float[] cornerX = {0, width, width, 0};
        float[] cornerY = {0, 0, height, height};

        float length = radius / 4;
        for (int i = 0; i < cornerX.length; i++) {
            float deltaX = centerX - cornerX[i];
            float deltaY = centerY - cornerY[i];
            float squareDistance = deltaX * deltaX + deltaY * deltaY;
            if (squareDistance == 0) continue;
            deltaX /= Math.sqrt(squareDistance);
            deltaY /= Math.sqrt(squareDistance);

            canvas.drawLine(centerX, centerY, centerX + deltaX * length, centerY + deltaY * length, mPaint);
        }

        super.onDraw(canvas);
    }

    void highlight() {
        mFillPaint.setColor(getContext().getResources().getColor(R.color.exit_view_in_exit_fill_color));
        mPaint.setColor(getContext().getResources().getColor(R.color.exit_view_in_exit_stroke_color));
    }

    void unhighlight() {
        mFillPaint.setColor(getContext().getResources().getColor(R.color.exit_view_out_exit_fill_color));
        mPaint.setColor(Color.WHITE);
    }

    void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);

        mFillPaint = new Paint();
        mFillPaint.setColor(getContext().getResources().getColor(R.color.exit_view_out_exit_fill_color));
        mFillPaint.setStyle(Paint.Style.FILL);
    }
}
