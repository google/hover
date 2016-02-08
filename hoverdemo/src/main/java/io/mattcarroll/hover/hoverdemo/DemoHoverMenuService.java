package io.mattcarroll.hover.hoverdemo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.ContextThemeWrapper;

import java.io.IOException;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.defaulthovermenu.window.HoverMenuService;
import io.mattcarroll.hover.hoverdemo.theming.HoverTheme;

/**
 * Demo {@link HoverMenuService}.
 */
public class DemoHoverMenuService extends HoverMenuService {

    private static final String TAG = "DemoHoverMenuService";

    public static void showFloatingMenu(Context context) {
        context.startService(new Intent(context, DemoHoverMenuService.class));
    }

    private DemoHoverMenuAdapter mDemoHoverMenuAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        Bus.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        Bus.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    protected Context getContextForHoverMenu() {
        return new ContextThemeWrapper(this, R.style.AppTheme);
    }

    @Override
    protected HoverMenuAdapter createHoverMenuAdapter() {
        try {
            mDemoHoverMenuAdapter = new DemoHoverMenuFactory().createDemoMenuFromCode(getContextForHoverMenu(), Bus.getInstance());
//            mDemoHoverMenuAdapter = new DemoHoverMenuFactory().createDemoMenuFromFile(getContextForHoverMenu());
            return mDemoHoverMenuAdapter;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onEventMainThread(@NonNull HoverTheme newTheme) {
        mDemoHoverMenuAdapter.setTheme(newTheme);
    }

}
