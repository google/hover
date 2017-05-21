package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.R;
import io.mattcarroll.hover.defaulthovermenu.view.InViewGroupDragger;

/**
 * TODO:
 */
public class HoverView extends FrameLayout {

    private static final String TAG = "HoverView";

    private HoverMenuView3 mHoverMenuView;
    private ExitListener mExitListener;

    public HoverView(@NonNull Context context) {
        this(context, null);
    }

    public HoverView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        int touchDiameter = getResources().getDimensionPixelSize(R.dimen.exit_radius);
        int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        InViewGroupDragger dragger = new InViewGroupDragger(this, touchDiameter, slop);
        dragger.setDebugMode(true);
        mHoverMenuView = new HoverMenuView3(getContext(), dragger);
        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        addView(mHoverMenuView, layoutParams);

        mHoverMenuView.setExitListener(new ExitListener() {
            @Override
            public void onExit() {
                Log.d(TAG, "Hover menu has exited.");
                if (null != mExitListener) {
                    mExitListener.onExit();
                }
            }
        });
    }

    public void setAdapter(@Nullable HoverMenuAdapter adapter) {
        mHoverMenuView.setAdapter(adapter);
    }

    public void setExitListener(@Nullable ExitListener listener) {
        mExitListener = listener;
    }
}
