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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import io.mattcarroll.hover.content.Navigator;

/**
 * {@link MenuAction} that displays a submenu in a given {@link Navigator}.
 */
public class ShowSubmenuMenuAction implements MenuAction {

    private final Menu mMenu;
    private final View mEmptyView;
    private MenuListContent mNavigatorContent;

    public ShowSubmenuMenuAction(@NonNull Menu menu) {
        this(menu, null);
    }

    public ShowSubmenuMenuAction(@NonNull Menu menu, @Nullable View emptyView) {
        mMenu = menu;
        mEmptyView = emptyView;
    }

    @Override
    public void execute(@NonNull Context context, @NonNull Navigator navigator) {
        if (null == mNavigatorContent) {
            // This is our first time being activated. Create our menu display.
            mNavigatorContent = new MenuListContent(context, mMenu, mEmptyView);
        }

        navigator.pushContent(mNavigatorContent);
    }

}
