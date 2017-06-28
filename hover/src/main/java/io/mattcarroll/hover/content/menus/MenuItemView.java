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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import io.mattcarroll.hover.R;


/**
 * View that represents a {@link MenuItem} as a list item.
 */
public class MenuItemView extends FrameLayout {

    private TextView mTitleTextView;

    private String mTitle;

    public MenuItemView(Context context) {
        super(context);
        init();
    }

    public MenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_menu_item, this, true);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
    }

    public void setTitle(String title) {
        mTitle = title;
        updateView();
    }

    private void updateView() {
        mTitleTextView.setText(mTitle);
    }
}
