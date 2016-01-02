package io.mattcarroll.hover;

import android.graphics.PointF;
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

    // TODO: should we be publishing anchor stuff? if so, why is there not a setter?
    /**
     * Returns the current state of the collapsed menu anchor. When the {@code HoverMenu} is collapsed
     * and the user is not actively dragging it, the collapsed icon gets pulled to an anchor position.
     */
    PointF getAnchorState();

    /**
     * Sets the {@link HoverMenuAdapter} that is used to determine what tabs and content should be
     * displayed in this {@code HoverMenu}.
     *
     * @param adapter adapter to provide content for this {@code HoverMenu}
     */
    void setAdapter(@Nullable HoverMenuAdapter adapter);

    /**
     * Expands the {@code HoverMenu} to display content.
     */
    void expandMenu();

    /**
     * Collapses the {@code HoverMenu} to a single draggable icon.
     */
    void collapseMenu();

}
