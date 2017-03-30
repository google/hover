package io.mattcarroll.hoverdemo.nonfullscreen;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;

/**
 * A Hover menu screen that does not take up the entire screen.
 */
public class NonFullscreenContent implements NavigatorContent {

    private final Context mContext;
    private View mContent;

    public NonFullscreenContent(@NonNull Context context) {
        mContext= context.getApplicationContext();
    }

    @NonNull
    @Override
    public View getView() {
        if (null == mContent) {
            mContent = LayoutInflater.from(mContext).inflate(R.layout.content_non_fullscreen, null);

            // We present our desire to be non-fullscreen by using WRAP_CONTENT for height.  This
            // preference will be honored by the Hover Menu to make our content only as tall as we
            // want to be.
            mContent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        return mContent;
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
