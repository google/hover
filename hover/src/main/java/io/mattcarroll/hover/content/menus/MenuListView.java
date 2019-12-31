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
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

/**
 * View that displays all items in a given {@link Menu}.
 */
public class MenuListView extends FrameLayout {

    private ListView mListView;
    private View mEmptyView;
    private MenuListAdapter mMenuListAdapter;
    private MenuItemSelectionListener mMenuItemSelectionListener;

    public MenuListView(Context context) {
        this(context, null);
    }

    public MenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mMenuListAdapter = new MenuListAdapter();

        mListView = new ListView(getContext());
        mListView.setAdapter(mMenuListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mMenuItemSelectionListener) {
                    mMenuItemSelectionListener.onMenuItemSelected(mMenuListAdapter.getItem(position));
                }
            }
        });
        addView(mListView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setEmptyView(@Nullable View emptyView) {
        // Remove existing empty view.
        if (null != mEmptyView) {
            removeView(mEmptyView);
        }

        mEmptyView = emptyView;
        if (null != mEmptyView) {
            addView(mEmptyView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        updateEmptyViewVisibility();
    }

    public void setMenu(@Nullable Menu menu) {
        mMenuListAdapter.setMenu(menu);
        updateEmptyViewVisibility();
    }

    public void setMenuItemSelectionListener(@Nullable MenuItemSelectionListener listener) {
        mMenuItemSelectionListener = listener;
    }

    private void updateEmptyViewVisibility() {
        boolean isEmpty = null == mMenuListAdapter || 0 == mMenuListAdapter.getCount();
        if (null != mEmptyView) {
            mEmptyView.setVisibility(isEmpty ? VISIBLE : GONE);
            mListView.setVisibility(isEmpty ? GONE : VISIBLE);
        } else {
            mListView.setVisibility(VISIBLE);
        }
    }

    public interface MenuItemSelectionListener {
        void onMenuItemSelected(@NonNull MenuItem menuItem);
    }
}
