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

import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Adapter that displays a {@link Menu} using {@link MenuItemView}s.
 */
public class MenuListAdapter extends BaseAdapter {

    private Menu mMenu;

    public void setMenu(@Nullable Menu menu) {
        mMenu = menu;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return null == mMenu ? 0 : mMenu.getMenuItemList().size();
    }

    @Override
    public MenuItem getItem(int index) {
        return mMenu.getMenuItemList().get(index);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (null == view) {
            view = new MenuItemView(parent.getContext());
        }

        ((MenuItemView) view).setTitle(getItem(i).getTitle());

        return view;
    }
}
