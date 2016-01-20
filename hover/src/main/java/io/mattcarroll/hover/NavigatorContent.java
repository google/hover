package io.mattcarroll.hover;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Content to be displayed by a {@link Navigator}.
 */
public interface NavigatorContent {

    /**
     * Returns the title to be displayed with this content.
     *
     * @return title to display with this content, or null to keep whatever title is already being displayed
     */
    @Nullable
    CharSequence getTitle();

    /**
     * Returns the visual display of this content.
     *
     * @return the visual representation of this content
     */
    @NonNull
    View getView();

    /**
     * Called when this content is displayed to the user.
     *
     * @param navigator the {@link Navigator} that is displaying this content.
     */
    void onShown(@NonNull Navigator navigator);

    /**
     * Called when this content is no longer displayed to the user.
     *
     * Implementation Note: a {@code NavigatorContent} can be brought back due to user navigation so
     * this call must not release resources that are required to show this content again.
     */
    void onHidden();

}
