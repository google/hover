package io.mattcarroll.hover.hoverdemo.appstate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.hoverdemo.Bus;
import io.mattcarroll.hover.hoverdemo.R;
import io.mattcarroll.hover.hoverdemo.theming.HoverTheme;
import io.mattcarroll.hover.hoverdemo.theming.HoverThemeManager;

/**
 * {@link NavigatorContent} that displays the Activity and Service state of the app.
 */
public class AppStateNavigatorContent extends FrameLayout implements NavigatorContent {

    private AppStateAdapter mAppStateAdapter;

    public AppStateNavigatorContent(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_app_state_content, this, true);

        mAppStateAdapter = new AppStateAdapter(HoverThemeManager.getInstance().getTheme());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAppStateAdapter);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Bus.getInstance().registerSticky(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        Bus.getInstance().unregister(this);
        super.onDetachedFromWindow();
    }

    public void onEventMainThread(@NonNull HoverTheme newTheme) {
        mAppStateAdapter.setTheme(newTheme);
    }

    public void onEventMainThread(@NonNull AppStateTracker.ActivityStackChangeEvent event) {
        mAppStateAdapter.setActivityStates(event.getActivityStack());
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onShown(@NonNull Navigator navigator) {
        mAppStateAdapter.setServiceStates(AppStateTracker.getInstance().getServiceStates());
    }

    @Override
    public void onHidden() {

    }
}
