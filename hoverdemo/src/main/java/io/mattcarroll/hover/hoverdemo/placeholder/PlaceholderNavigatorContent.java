package io.mattcarroll.hover.hoverdemo.placeholder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.hoverdemo.R;
import io.mattcarroll.hover.hoverdemo.theming.HoverTheme;

/**
 * Use this class to try adding your own content to the Hover menu.
 */
public class PlaceholderNavigatorContent extends FrameLayout implements NavigatorContent {

    private final EventBus mBus;
    private TextView mTitleTextView;

    public PlaceholderNavigatorContent(@NonNull Context context, @NonNull EventBus bus) {
        super(context);
        mBus = bus;
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_placeholder_content, this, true);
        mTitleTextView = (TextView) findViewById(R.id.textview_title);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBus.registerSticky(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        mBus.unregister(this);
        super.onDetachedFromWindow();
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onShown(@NonNull Navigator navigator) {

    }

    @Override
    public void onHidden() {

    }

    public void onEventMainThread(@NonNull HoverTheme newTheme) {
        mTitleTextView.setTextColor(newTheme.getAccentColor());
    }
}
