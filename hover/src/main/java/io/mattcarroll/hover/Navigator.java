/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mattcarroll.hover;

import android.support.annotation.NonNull;
import android.view.View;

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
     * Removes the current content {@code View} if content is visible. Then displays the provided
     * {@code content}.
     *
     * The given {@code content} is retained in a navigation stack so that this {@code Navigator}
     * can navigate to other content and then later return to this content.
     *
     * To remove the given {@code content}, make a corresponding call to {@link #popContent()}.
     *
     * @param content Content to display
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

    /**
     * Returns the View that presents the Navigator.
     * @return navigator View
     */
    @NonNull
    View getView();

}
