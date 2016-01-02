package io.mattcarroll.hover;

import android.support.annotation.NonNull;

/**
 * A visual display that can push and pop {@code View}s in a content area. The size and location of
 * the content area is chosen by implementing classes.
 *
 * A {@code Navigator} also displays a title that can be set by a client.
 *
 * The content to display in a {@code Navigator} must be provided as a {@link NavigatorContent}. Each
 * pushed {@code NavigatorContent} is retained in a navigation stack until a corresponding
 * {@link #popContent()} is called.  Therefore, {@code NavigatorContent}s must retain their {@code View}
 * and state until garbage collected.
 */
public interface Navigator {

    /**
     * Sets the title that is displayed by this {@code Navigator}. The details of the title presentation
     * are implementation specific.
     */
    void setTitle(@NonNull String title);

    /**
     * Removes the current content {@code View} if content is visible. Then displays the provided
     * {@code content}.
     *
     * The given {@code content} is retained in a navigation stack so that this {@code Navigator}
     * can navigate to other content and then later return to this content.
     *
     * To remove the given {@code content}, make a corresponding call to {@link #popContent()}.
     *
     * @param content Content to display.
     */
    void pushContent(@NonNull NavigatorContent content);

    /**
     * Removes the current content {@code View} and restores the previous content {@code View}. If
     * there is no previous content then this {@code Navigator} returns to its base visual state
     * without any content.
     *
     * @return true if there was content to remove, false if there was no content to remove
     */
    boolean popContent();

    /**
     * Pops all content {@code View}s and returns this {@code Navigator} to its base visual state
     * without any content.
     */
    void clearContent();

}
