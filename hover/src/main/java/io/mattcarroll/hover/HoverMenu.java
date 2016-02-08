package io.mattcarroll.hover;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A {@code HoverMenu} is a menu that can appear either in a collapsed, draggable state, or in an expanded
 * state that displays content.  A {@code HoverMenu} presents some number of tabs, each of which has its
 * own content that the {@code HoverMenu} also displays.
 *
 * Content in the {@code HoverMenu} is provided by a given {@link HoverMenuAdapter}. {@code HoverMenuAdapter}s
 * work in a similar fashion to a traditional Android {@code ListAdapter}.
 */
public interface HoverMenu {

    void show();

    void hide();

    /**
     * Expands the {@code HoverMenu} to display content.
     */
    void expandMenu();

    /**
     * Collapses the {@code HoverMenu} to a single draggable icon.
     */
    void collapseMenu();

    /**
     * Sets the {@link HoverMenuAdapter} that is used to determine what tabs and content should be
     * displayed in this {@code HoverMenu}.
     *
     * @param adapter adapter to provide content for this {@code HoverMenu}
     */
    void setAdapter(@Nullable HoverMenuAdapter adapter);

    String getVisualState();

    void restoreVisualState(@NonNull String savedVisualState);

    void addOnExitListener(@NonNull OnExitListener onExitListener);

    void removeOnExitListener(@NonNull OnExitListener onExitListener);

    interface OnExitListener {
        void onExitByUserRequest();
    }
}
