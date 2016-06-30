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
     * Returns the unique ID for the tab that is currently at the given {@code position}. The ID
     * should be unique per tab, not per position.  If the same tab moves around in the tab list,
     * it should still return the same ID.
     *
     * @return ID of tab at given position
     */
    long getTabId(int position);

    /**
     * Returns the visual representation of the {@code index}'th tab.
     *
     * @param position index of tab
     * @return visual representation of the {@code index}'th tab
     */
    View getTabView(int position);

    /**
     * Returns the {@link NavigatorContent} to display for the tab at the given {@code position}.
     *
     * @param position position of tab to activate
     */
    NavigatorContent getNavigatorContent(int position);

    void addContentChangeListener(@NonNull ContentChangeListener listener);

    void removeContentChangeListener(@NonNull ContentChangeListener listener);

    interface ContentChangeListener {
        void onContentChange(@NonNull HoverMenuAdapter adapter);
    }
}
