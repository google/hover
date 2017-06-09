package io.mattcarroll.hover;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Fullscreen View that provides an exit "drop zone" for users to exit the Hover Menu.
 */
class ExitView extends RelativeLayout {

    private static final String TAG = "ExitView";

    private int mExitRadiusInPx;
    private View mExitIcon;

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

        mExitRadiusInPx = getResources().getDimensionPixelSize(R.dimen.hover_exit_radius);
    }

    public boolean isInExitZone(@NonNull Point position) {
        Point exitCenter = getExitZoneCenter();
        double distanceToExit = calculateDistance(position, exitCenter);
        Log.d(TAG, "Drop point: " + position + ", Exit center: " + exitCenter + ", Distance: " + distanceToExit);
        return distanceToExit <= mExitRadiusInPx;
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
}
