package io.mattcarroll.hover;

import android.view.View;

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
     * Returns the content that should be displayed for the {@code index}'th tab.
     *
     * @param index index of tab
     * @return content for the {@code index}'th tab
     */
    NavigatorContent getContentView(int index);

}
