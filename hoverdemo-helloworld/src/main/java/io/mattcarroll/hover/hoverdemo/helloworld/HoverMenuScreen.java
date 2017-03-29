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

    public HoverMenuScreen(@NonNull Context context, @NonNull String pageTitle) {
        mContext = context.getApplicationContext();
        mPageTitle = pageTitle;
    }

    @NonNull
    @Override
    public View getView() {
        TextView wholeScreen = new TextView(mContext);
        wholeScreen.setText("Screen: " + mPageTitle);
        wholeScreen.setGravity(Gravity.CENTER);
        return wholeScreen;
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
