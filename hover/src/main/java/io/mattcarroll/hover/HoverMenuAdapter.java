package io.mattcarroll.hover;

import android.support.annotation.NonNull;
import android.view.View;

import io.mattcarroll.hover.defaulthovermenu.menus.MenuAction;

/**
 * Adapter that provides {@code View}s for the tabs and the content within a Hover menu.
 */
public interface HoverMenuAdapter {

    /**
     * Returns the number of tabs that a {@code HoverMenu} should display.
     *
     * @return number of tabs
     */
    int getTabCount();

    /**
     * Returns the visual representation of the {@code index}'th tab.
     *
     * @param index index of tab
     * @return visual representation of the {@code index}'th tab
     */
    View getTabView(int index);

    /**
     * Returns the {@link MenuAction} to activate for the tab at the given {@code index}.
     *
     * @param index index of tab to activate
     */
    MenuAction getTabMenuAction(int index);

}
