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
package io.mattcarroll.hover.hoverdemo.kitchensink;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.mattcarroll.hover.Content;
import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.hoverdemo.kitchensink.theming.HoverTheme;
import io.mattcarroll.hover.hoverdemo.kitchensink.ui.DemoTabView;

/**
 * Demo implementation of a {@link HoverMenu}.
 */
public class DemoHoverMenu extends HoverMenu {

    public static final String INTRO_ID = "intro";
    public static final String SELECT_COLOR_ID = "select_color";
    public static final String APP_STATE_ID = "app_state";
    public static final String MENU_ID = "menu";
    public static final String PLACEHOLDER_ID = "placeholder";

    private final Context mContext;
    private final String mMenuId;
    private HoverTheme mTheme;
    private final List<Section> mSections = new ArrayList<>();

    public DemoHoverMenu(@NonNull Context context,
                         @NonNull String menuId,
                         @NonNull Map<String, Content> data,
                         @NonNull HoverTheme theme) throws IOException {
        mContext = context;
        mMenuId = menuId;
        mTheme = theme;

        for (String tabId : data.keySet()) {
            mSections.add(new Section(
                    new SectionId(tabId),
                    createTabView(tabId),
                    data.get(tabId)
            ));
        }
    }

    public void setTheme(@NonNull HoverTheme theme) {
        mTheme = theme;
        // TODO: need to make theme changes work again with refactored menu
        notifyMenuChanged();
    }

    private View createTabView(String sectionId) {
        if (INTRO_ID.equals(sectionId)) {
            return createTabView(R.drawable.ic_orange_circle, mTheme.getAccentColor(), null);
        } else if (SELECT_COLOR_ID.equals(sectionId)) {
            return createTabView(R.drawable.ic_paintbrush, mTheme.getAccentColor(), mTheme.getBaseColor());
        } else if (APP_STATE_ID.equals(sectionId)) {
            return createTabView(R.drawable.ic_stack, mTheme.getAccentColor(), mTheme.getBaseColor());
        } else if (MENU_ID.equals(sectionId)) {
            return createTabView(R.drawable.ic_menu, mTheme.getAccentColor(), mTheme.getBaseColor());
        } else if (PLACEHOLDER_ID.equals(sectionId)) {
            return createTabView(R.drawable.ic_pen, mTheme.getAccentColor(), mTheme.getBaseColor());
        } else {
            throw new RuntimeException("Unknown tab selected: " + sectionId);
        }
    }

    private View createTabView(@DrawableRes int tabBitmapRes, @ColorInt int backgroundColor, @ColorInt Integer iconColor) {
        Resources resources = mContext.getResources();
        int elevation = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, resources.getDisplayMetrics());

        DemoTabView view = new DemoTabView(mContext, resources.getDrawable(R.drawable.tab_background), resources.getDrawable(tabBitmapRes));
        view.setTabBackgroundColor(backgroundColor);
        view.setTabForegroundColor(iconColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(elevation);
        }
        return view;
    }

    @Override
    public String getId() {
        return mMenuId;
    }

    @Override
    public int getSectionCount() {
        return mSections.size();
    }

    @Nullable
    @Override
    public Section getSection(int index) {
        return mSections.get(index);
    }

    @Nullable
    @Override
    public Section getSection(@NonNull SectionId sectionId) {
        for (Section section : mSections) {
            if (section.getId().equals(sectionId)) {
                return section;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public List<Section> getSections() {
        return new ArrayList<>(mSections);
    }

//    // use the code below to test how fixed section work.
//    @Override
//    protected Section getFixedSection() {
//        return mSections.get(0);
//    }
}
