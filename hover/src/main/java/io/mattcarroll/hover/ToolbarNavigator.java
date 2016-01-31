package io.mattcarroll.hover;

import android.support.v7.widget.Toolbar;

/**
 * A {@link Navigator} that offers a {@code Toolbar}.
 */
public interface ToolbarNavigator extends Navigator {

    Toolbar getToolbar();

}
