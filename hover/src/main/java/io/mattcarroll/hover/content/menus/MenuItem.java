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
package io.mattcarroll.hover.content.menus;

import androidx.annotation.NonNull;

/**
 * Represents a menu item that can act as a composite with submenu items.
 */
public class MenuItem {

    private final String mId;
    private final String mTitle;
    private final MenuAction mMenuAction;

    public MenuItem(@NonNull String id, @NonNull String title, @NonNull MenuAction menuAction) {
        mId = id;
        mTitle = title;
        mMenuAction = menuAction;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    @NonNull
    public MenuAction getMenuAction() {
        return mMenuAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MenuItem menuItem = (MenuItem) o;

        return mId.equals(menuItem.mId);

    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

}
