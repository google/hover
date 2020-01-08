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

import java.util.List;

/**
 * A {@code Menu} contains {@link MenuItem}s.
 */
public class Menu {

    private final String mTitle;
    private final List<MenuItem> mMenuItemList;

    public Menu(@NonNull String title, @NonNull List<MenuItem> menuItemList) {
        mTitle = title;
        mMenuItemList = menuItemList;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    @NonNull
    public List<MenuItem> getMenuItemList() {
        return mMenuItemList;
    }

}
