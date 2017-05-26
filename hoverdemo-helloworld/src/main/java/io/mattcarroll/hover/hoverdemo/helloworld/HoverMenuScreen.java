package io.mattcarroll.hover.hoverdemo.helloworld;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;

/**
 * A screen that is displayed in our Hello World Hover Menu.
 */
public class HoverMenuScreen implements NavigatorContent {

    private final Context mContext;
    private final String mPageTitle;
    private final View mWholeScreen;

    public HoverMenuScreen(@NonNull Context context, @NonNull String pageTitle) {
        mContext = context.getApplicationContext();
        mPageTitle = pageTitle;
        mWholeScreen = createScreenView();
    }

    @NonNull
    private View createScreenView() {
        TextView wholeScreen = new TextView(mContext);
        wholeScreen.setText("Screen: " + mPageTitle);
        wholeScreen.setGravity(Gravity.CENTER);
        return wholeScreen;
    }

    // Make sure that this method returns the SAME View.  It should NOT create a new View each time
    // that it is invoked.
    @NonNull
    @Override
    public View getView() {
        return mWholeScreen;
    }

    @Override
    public boolean isFullscreen() {
        return true;
    }

    @Override
    public void onShown(@NonNull Navigator navigator) {
        // No-op.
    }

    @Override
    public void onHidden() {
        // No-op.
    }
}
