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
package io.mattcarroll.hover.hoverdemo.colorselection;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import de.greenrobot.event.EventBus;
import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.hoverdemo.R;
import io.mattcarroll.hover.hoverdemo.theming.HoverTheme;
import io.mattcarroll.hover.hoverdemo.theming.HoverThemer;

/**
 * {@link NavigatorContent} that displays a color chooser and applies the color selection to the
 * Hover menu UI.
 */
public class ColorSelectionNavigatorContent extends FrameLayout implements NavigatorContent {

    private static final int MODE_ACCENT = 0;
    private static final int MODE_BASE = 1;

    private EventBus mBus;
    private HoverThemer mHoverThemer;
    private int mMode;
    private HoverTheme mTheme;
    private TabLayout mTabLayout;
    private ColorPicker mColorPicker;
    private TextView mAttributionTextView;

    public ColorSelectionNavigatorContent(@NonNull Context context, @NonNull EventBus bus, @NonNull HoverThemer hoverThemer, @NonNull HoverTheme theme) {
        super(context);
        mBus = bus;
        mHoverThemer = hoverThemer;
        mTheme = theme;
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_color_selection_content, this, true);
        mTabLayout = (TabLayout) findViewById(R.id.tablayout);
        mColorPicker = (ColorPicker) findViewById(R.id.colorpicker);
        mAttributionTextView = (TextView) findViewById(R.id.textview_attribution);

        mTabLayout.addTab(mTabLayout.newTab().setText("Accent Color"), true);
        mTabLayout.addTab(mTabLayout.newTab().setText("Primary Color"));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        mMode = MODE_ACCENT;
                        mColorPicker.setColor(mTheme.getAccentColor());
                        mColorPicker.setNewCenterColor(mTheme.getAccentColor());
                        mColorPicker.setOldCenterColor(mTheme.getAccentColor());
                        break;
                    case 1:
                        mMode = MODE_BASE;
                        mColorPicker.setColor(mTheme.getBaseColor());
                        mColorPicker.setNewCenterColor(mTheme.getBaseColor());
                        mColorPicker.setOldCenterColor(mTheme.getBaseColor());
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        mColorPicker.setColor(mTheme.getAccentColor());
        mColorPicker.setNewCenterColor(mTheme.getAccentColor());
        mColorPicker.setOldCenterColor(mTheme.getAccentColor());
        mColorPicker.addSaturationBar((SaturationBar) findViewById(R.id.saturationbar));
        mColorPicker.addValueBar((ValueBar) findViewById(R.id.valuebar));
        mColorPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                HoverTheme theme;
                if (MODE_ACCENT == mMode) {
                    theme = new HoverTheme(color, mTheme.getBaseColor());
                } else {
                    theme = new HoverTheme(mTheme.getAccentColor(), color);
                }
                mHoverThemer.setTheme(theme);
            }
        });

        updateView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBus.register(this);
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

    }

    @Override
    public void onHidden() {

    }

    public void onEventMainThread(@NonNull HoverTheme newTheme) {
        mTheme = newTheme;
        updateView();
    }

    private void updateView() {
        mTabLayout.setSelectedTabIndicatorColor(mTheme.getAccentColor());
        mTabLayout.setTabTextColors(0xFFCCCCCC, mTheme.getAccentColor());
        mAttributionTextView.setTextColor(mTheme.getAccentColor());
    }
}
