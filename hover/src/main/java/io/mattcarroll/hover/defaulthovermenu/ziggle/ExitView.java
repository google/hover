package io.mattcarroll.hover.defaulthovermenu.ziggle;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.LayoutInflaterCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import io.mattcarroll.hover.R;

/**
 * Fullscreen View that provides an exit "drop zone" for users to exit the Hover Menu.
 */
public class ExitView extends RelativeLayout {

    private int mExitRadiusInPx;

    public ExitView(@NonNull Context context) {
        this(context, null);
    }

    public ExitView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_hover_menu_exit, this, true);

        mExitRadiusInPx = getResources().getDimensionPixelSize(R.dimen.exit_radius);
    }

}
