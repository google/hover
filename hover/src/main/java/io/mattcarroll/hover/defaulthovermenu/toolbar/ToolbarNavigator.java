package io.mattcarroll.hover.defaulthovermenu.toolbar;

import android.support.v7.widget.Toolbar;

import io.mattcarroll.hover.Navigator;

/**
 * A {@link Navigator} that offers a {@code Toolbar}.
 */
public interface ToolbarNavigator extends Navigator {

    Toolbar getToolbar();

}
