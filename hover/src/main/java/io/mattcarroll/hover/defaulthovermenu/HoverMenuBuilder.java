package io.mattcarroll.hover.defaulthovermenu;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.defaulthovermenu.utils.Dragger;
import io.mattcarroll.hover.defaulthovermenu.utils.view.InViewGroupDragger;
import io.mattcarroll.hover.defaulthovermenu.utils.window.InWindowDragger;
import io.mattcarroll.hover.defaulthovermenu.utils.window.WindowViewController;

/**
 * Builds a {@link HoverMenuView}.
 */
public class HoverMenuBuilder {

    public static final int DISPLAY_MODE_WINDOW = 1; // Display directly in a window.
    public static final int DISPLAY_MODE_VIEW = 2; // Display within View hierarchy.

    private Context mContext;
    private Dragger mViewDragger;
    private Navigator mNavigator;
    private PointF mSavedAnchor = new PointF(0.0f, 0.5f);

    public HoverMenuBuilder(@NonNull Context context) {
        mContext = context;
    }

    public HoverMenuBuilder displayWithinWindow(@NonNull WindowViewController windowViewController) {
        mViewDragger = new InWindowDragger(mContext, windowViewController, ViewConfiguration.get(mContext).getScaledTouchSlop());
        return this;
    }

    public HoverMenuBuilder displayWithinView(@NonNull ViewGroup container) {
        mViewDragger = new InViewGroupDragger(container, ViewConfiguration.get(mContext).getScaledTouchSlop());
        return this;
    }

    public HoverMenuBuilder useNavigator(@Nullable Navigator navigator) {
        mNavigator = navigator;
        return this;
    }

    public HoverMenuView build() {
        return new HoverMenuView(mContext, mNavigator, mViewDragger, mSavedAnchor);
    }

}
