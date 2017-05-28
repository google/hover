package io.mattcarroll.hoverdemo.nonfullscreen;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.content.NavigatorContent;

/**
 * A Hover Menu Adapter tells the Hover Menu how many tabs/pages should appear in the Hover menu.  It
 * also provides the content for those pages.
 */
public class DemoHoverMenuAdapter implements HoverMenuAdapter {

    private final Context mContext;
    private final NonFullscreenContent mTheOnlyScreen;

    public DemoHoverMenuAdapter(@NonNull Context context) {
        mContext = context.getApplicationContext();
        mTheOnlyScreen = new NonFullscreenContent(context);
    }

    @Override
    public int getTabCount() {
        return 1;
    }

    @Override
    public String getTabId(int position) {
        return Integer.toString(position);
    }

    @Override
    public View getTabView(int position) {
        return createTabView(R.drawable.ic_orange_circle, 0xFFFF9600, null);
    }

    @Override
    public NavigatorContent getNavigatorContent(int position) {
        return mTheOnlyScreen;
    }

    @Override
    public void addContentChangeListener(@NonNull ContentChangeListener listener) {
        // No-op.
    }

    @Override
    public void removeContentChangeListener(@NonNull ContentChangeListener listener) {
        // No-op.
    }

    private View createTabView(@DrawableRes int tabBitmapRes, @ColorInt int backgroundColor, @ColorInt Integer iconColor) {
        Resources resources = mContext.getResources();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, resources.getDisplayMetrics());

        DemoTabView view = new DemoTabView(mContext, resources.getDrawable(R.drawable.tab_background), resources.getDrawable(tabBitmapRes));
        view.setTabBackgroundColor(backgroundColor);
        view.setTabForegroundColor(iconColor);
        view.setPadding(padding, padding, padding, padding);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(padding);
        }
        return view;
    }
}
