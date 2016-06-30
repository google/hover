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
     *
     * @return anchor state of this HoverMenu
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
