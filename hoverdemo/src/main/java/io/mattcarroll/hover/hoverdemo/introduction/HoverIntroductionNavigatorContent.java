package io.mattcarroll.hover.hoverdemo.introduction;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
 * {@link NavigatorContent} that displays an introduction to Hover.
 */
public class HoverIntroductionNavigatorContent extends FrameLayout implements NavigatorContent {

    private final EventBus mBus;
    private View mLogo;
    private HoverMotion mHoverMotion;
    private TextView mHoverTitleTextView;
    private TextView mGoalsTitleTextView;

    public HoverIntroductionNavigatorContent(@NonNull Context context, @NonNull EventBus bus) {
        super(context);
        mBus = bus;
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_content_introduction, this, true);

        mLogo = findViewById(R.id.imageview_logo);
        mHoverMotion = new HoverMotion();
        mHoverTitleTextView = (TextView) findViewById(R.id.textview_hover_title);
        mGoalsTitleTextView = (TextView) findViewById(R.id.textview_goals_title);
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

    @Nullable
    @Override
    public CharSequence getTitle() {
        return null;
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onShown(@NonNull Navigator navigator) {
        mHoverMotion.start(mLogo);
    }

    @Override
    public void onHidden() {
        mHoverMotion.stop();
    }

    public void onEventMainThread(@NonNull HoverTheme newTheme) {
        mHoverTitleTextView.setTextColor(newTheme.getAccentColor());
        mGoalsTitleTextView.setTextColor(newTheme.getAccentColor());
    }
}
