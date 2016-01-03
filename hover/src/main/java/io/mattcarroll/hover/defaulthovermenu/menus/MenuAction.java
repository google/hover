package io.mattcarroll.hover.defaulthovermenu.menus;

import android.content.Context;
import android.support.annotation.NonNull;

import io.mattcarroll.hover.Navigator;

/**
 * Represents an action that executes when the user selects an item in a Hover menu.
 */
public interface MenuAction {

    /**
     * Executes a desired action, possibly navigating to new content by using the given {@code navigator}.
     * @param context context
     * @param navigator the {@link Navigator} that holds the menu that this action belongs to
     */
    void execute(@NonNull Context context, @NonNull Navigator navigator);

}
