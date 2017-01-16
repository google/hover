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
package io.mattcarroll.hover.hoverdemo.introduction;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.hoverdemo.R;
import io.mattcarroll.hover.hoverdemo.theming.HoverTheme;

/**
 * {@link NavigatorContent} that displays an introduction to Hover.
 */
public class HoverIntroductionNavigatorContent extends FrameLayout implements NavigatorContent {

    private final EventBus mBus;
    private View mLogo;
    private HoverMotion mHoverMotion;
    private TextView mHoverTitleTextView;
    private TextView mGoalsTitleTextView;

    public HoverIntroductionNavigatorContent(@NonNull Context context, @NonNull EventBus bus) {
        super(context);
        mBus = bus;
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_content_introduction, this, true);

        mLogo = findViewById(R.id.imageview_logo);
        mHoverMotion = new HoverMotion();
        mHoverTitleTextView = (TextView) findViewById(R.id.textview_hover_title);
        mGoalsTitleTextView = (TextView) findViewById(R.id.textview_goals_title);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBus.registerSticky(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        mBus.unregister(this);
        super.onDetachedFromWindow();
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onShown(@NonNull Navigator navigator) {
        mHoverMotion.start(mLogo);
    }

    @Override
    public void onHidden() {
        mHoverMotion.stop();
    }

    public void onEventMainThread(@NonNull HoverTheme newTheme) {
        mHoverTitleTextView.setTextColor(newTheme.getAccentColor());
        mGoalsTitleTextView.setTextColor(newTheme.getAccentColor());
    }
}
