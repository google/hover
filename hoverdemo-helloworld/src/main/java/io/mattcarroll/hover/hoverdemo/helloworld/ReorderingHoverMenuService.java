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
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import org.codecanon.hover.hoverdemo.helloworld.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.window.HoverMenuService;

/**
 * Extend {@link HoverMenuService} to get a Hover menu that displays the tabs and content
 * in your custom {@link HoverMenu}.
 *
 * This demo menu re-orders the Sections of its Hover Menu at scheduled times to demonstrate how
 * Hover handles re-ordering menu Sections.
 */
public class ReorderingHoverMenuService extends HoverMenuService {

    private static final String TAG = "ReorderingHoverMenuService";

    @Override
    protected HoverMenu createHoverMenu(@NonNull Intent intent) {
        return new ReorderingSectionHoverMenu(getApplicationContext());
    }

    private static class ReorderingSectionHoverMenu extends HoverMenu {

        private final Context mContext;
        private final List<Section> mSections = new ArrayList<>();

        private int mNextStep = 0;
        private final List<Runnable> mMutationSteps = Arrays.asList(
                new Runnable() {
                    @Override
                    public void run() {
                        moveTab(1, 2);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        moveTab(3, 1);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        moveTab(2, 3);
                    }
                }
        );

        ReorderingSectionHoverMenu(@NonNull Context context) {
            mContext = context;

            insertTab(0);
            insertTab(1);
            insertTab(2);
            insertTab(3);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMutationSteps.get(mNextStep).run();
                    ++mNextStep;

                    if (mNextStep < mMutationSteps.size()) {
                        new Handler(Looper.getMainLooper()).postDelayed(this, 1000);
                    }
                }
            }, 5000);
        }

        private View createTabView() {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.tab_background);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return imageView;

//            int tabSize = mContext.getResources().getDimensionPixelSize(R.dimen.tab_size);
//            FrameLayout tabView = new FrameLayout(mContext);
//            tabView.addView(imageView, new FrameLayout.LayoutParams(tabSize, tabSize));
//
//            Log.d(TAG, "Created new tab view: " + tabView.hashCode());
//            return tabView;
        }

        @Override
        public String getId() {
            return "reorderingmenu";
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

        private void insertTab(int position) {
            String id = Integer.toString(mSections.size());
            mSections.add(position, new Section(
                    new SectionId(id),
                    createTabView(),
                    new HoverMenuScreen(mContext, "Screen " + id)
            ));
            notifyMenuChanged();
        }

        private void moveTab(int startPosition, int endPosition) {
            Section section = mSections.remove(startPosition);
            mSections.add(endPosition, section);
            notifyMenuChanged();
        }
    }
}
