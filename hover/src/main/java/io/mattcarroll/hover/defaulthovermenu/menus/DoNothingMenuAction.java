package io.mattcarroll.hover.defaulthovermenu.menus;

import android.content.Context;
import android.support.annotation.NonNull;

import io.mattcarroll.hover.Navigator;

/**
 * {@link MenuAction} that does nothing. Use this for temporary stubbing of menu item behavior.
 */
public class DoNothingMenuAction implements MenuAction {

    @Override
    public void execute(@NonNull Context context, @NonNull Navigator navigator) {
        // Do nothing.
    }

}
