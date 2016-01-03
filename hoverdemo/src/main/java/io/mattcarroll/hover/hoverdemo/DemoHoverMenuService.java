package io.mattcarroll.hover.hoverdemo;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;

import io.mattcarroll.hover.defaulthovermenu.window.HoverMenuService;
import io.mattcarroll.hover.HoverMenuAdapter;

/**
 * Demo {@link HoverMenuService}.
 */
public class DemoHoverMenuService extends HoverMenuService {

    private static final String TAG = "DemoHoverMenuService";

    public static void showFloatingMenu(Context context) {
        context.startService(new Intent(context, DemoHoverMenuService.class));
    }

    @Override
    protected int getMenuTheme() {
        return R.style.AppTheme;
    }

    @Override
    protected HoverMenuAdapter createHoverMenuAdapter() {
        final ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, R.style.AppTheme);
        return new DemoHoverMenuAdapter(contextThemeWrapper);
    }

}
