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
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import org.codecanon.hover.hoverdemo.helloworld.R;

import java.util.ArrayList;
import java.util.List;

import io.mattcarroll.hover.Content;
import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.HoverView;
import io.mattcarroll.hover.window.HoverMenuService;

/**
 * Extend {@link HoverMenuService} to get a Hover menu that displays the tabs and content
 * in your custom {@link HoverMenu}.
 *
 * This HoverMenuService switches menus at runtime.
 */
public class ChangingMenusHoverMenuService extends HoverMenuService {

    private static final String TAG = "ChangingMenusHoverMenuService";

    private SingleSectionHoverMenu mHoverMenu1;
    private SingleSectionHoverMenu mHoverMenu2;
    private boolean mShowingMenu1 = true;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        mHoverMenu1 = new SingleSectionHoverMenu(getApplicationContext(), "changingmenus", 1, "This is menu 1");
        mHoverMenu2 = new SingleSectionHoverMenu(getApplicationContext(), "changingmenus", 3, "This is menu 2");
    }

    @Override
    protected void onHoverMenuLaunched(@NonNull Intent intent, @NonNull HoverView hoverView) {
        hoverView.setMenu(createHoverMenu());
        hoverView.collapse();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchMenus();

                mHandler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    @NonNull
    private HoverMenu createHoverMenu() {
        return mHoverMenu1;
    }

    private void switchMenus() {
        if (mShowingMenu1) {
            getHoverView().setMenu(mHoverMenu2);
        } else {
            getHoverView().setMenu(mHoverMenu1);
        }
        mShowingMenu1 = !mShowingMenu1;
    }

    @Override
    protected void onHoverMenuExitingByUserRequest() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private static class SingleSectionHoverMenu extends HoverMenu {

        private final String mId;
        private Context mContext;
        private List<Section> mSections;

        private SingleSectionHoverMenu(@NonNull Context context,
                                       @NonNull String menuId,
                                       int sectionCount,
                                       @NonNull String content) {
            mId = menuId;
            mContext = context;

            mSections = new ArrayList<>(sectionCount);
            for (int i = 1; i <= sectionCount; ++i) {
                mSections.add(new Section(
                        new SectionId(Integer.toString(i)),
                        createTabView(),
                        createScreen(content)
                ));
            }
        }

        private View createTabView() {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.tab_background);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            return imageView;
        }

        private Content createScreen(@NonNull String content) {
            return new HoverMenuScreen(mContext, content);
        }

        @Override
        public String getId() {
            return mId;
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
                if (sectionId.equals(section.getId())) {
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

        @NonNull
        @Override
        public void removeAt(int index) {
            if (mSections.size() > index) {
                mSections.remove(index);
                notifyMenuChanged();
            }
        }
    }
}
