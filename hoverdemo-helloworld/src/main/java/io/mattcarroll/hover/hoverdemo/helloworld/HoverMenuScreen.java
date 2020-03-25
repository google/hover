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
package io.mattcarroll.hover.hoverdemo.helloworld;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.codecanon.hover.hoverdemo.helloworld.R;

import io.mattcarroll.hover.Content;

/**
 * A screen that is displayed in our Hello World Hover Menu.
 */
public class HoverMenuScreen implements Content {

    private final Context mContext;
    private final String mPageTitle;
    public final View mWholeScreen;

    public HoverMenuScreen(@NonNull final Context context, @NonNull String pageTitle) {
        mContext = context.getApplicationContext();
        mPageTitle = pageTitle;
        mWholeScreen = createScreenView();
    }

    @NonNull
    private View createScreenView() {
        return LayoutInflater.from(mContext).inflate(R.layout.layout_page, null, false);
    }

    // Make sure that this method returns the SAME View.  It should NOT create a new View each time
    // that it is invoked.
    @NonNull
    @Override
    public View getView() {
        return mWholeScreen;
    }

    @Override
    public boolean isFullscreen() {
        return true;
    }

    @Override
    public void onShown() {
        // No-op.
    }

    @Override
    public void onHidden() {
        // No-op.
    }

    @Override
    public boolean onContentBackPressed() {
        Toast.makeText(mContext, "On BackPressed Event", Toast.LENGTH_SHORT).show();
        return true;
    }
}
