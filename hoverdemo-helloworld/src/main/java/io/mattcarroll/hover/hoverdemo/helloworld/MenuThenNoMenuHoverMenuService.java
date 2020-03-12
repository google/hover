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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.mattcarroll.hover.Content;
import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.HoverView;
import io.mattcarroll.hover.window.HoverMenuService;

/**
 * Extend {@link HoverMenuService} to get a Hover menu that displays the tabs and content
 * in your custom {@link HoverMenu}.
 *
 * This menu presents a Hover Menu that automatically transitions from having a menu set, to not
 * having a menu set, and does so while going through many different transitions.
 */
public class MenuThenNoMenuHoverMenuService extends HoverMenuService {

    private static final String TAG = "MenuThenNoMenuHoverMenuService";

    private Handler mHandler = new Handler();
    private int mNextStateTransition = 0;
    private HoverMenu mMenu;

    private final List<Runnable> mStateTransitions = Arrays.asList(
            new Runnable() {
                @Override
                public void run() {
                    // Remove the menu. Should go to closed state.
                    getHoverView().setMenu(null);
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    // Give the menu back. Should stay closed.
                    getHoverView().setMenu(mMenu);
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    // Expand the menu from the closed state.
                    getHoverView().expand();
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    // Remove the menu. Should go to closed state.
                    getHoverView().setMenu(null);
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    // Give the menu back.
                    getHoverView().setMenu(mMenu);
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    // Go to collapsed state.
                    getHoverView().collapse();
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    // Expand the menu.
                    getHoverView().expand();
                }
            }
    );

    @Override
    protected void onHoverMenuLaunched(@NonNull Intent intent, @NonNull HoverView hoverView) {
        hoverView.setMenu(createHoverMenu());
        hoverView.collapse();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                transitionToNextState();

                mHandler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    @NonNull
    private HoverMenu createHoverMenu() {
        mMenu = new SingleSectionHoverMenu(getApplicationContext());
        return mMenu;
    }

    private void transitionToNextState() {
        mStateTransitions.get(mNextStateTransition).run();
        mNextStateTransition = mNextStateTransition < mStateTransitions.size() - 1
                ? mNextStateTransition + 1
                : 0;
    }

    @Override
    protected void onHoverMenuExitingByUserRequest() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private static class SingleSectionHoverMenu extends HoverMenu {

        private Context mContext;
        private Section mSection;

        private SingleSectionHoverMenu(@NonNull Context context) {
            mContext = context;

            mSection = new Section(
                    new SectionId("1"),
                    createTabView(),
                    createScreen()
            );
        }

        private View createTabView() {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.tab_background);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            return imageView;
        }

        private Content createScreen() {
            return new HoverMenuScreen(mContext, "Screen 1");
        }

        @Override
        public String getId() {
            return "singlesectionmenu";
        }

        @Override
        public int getSectionCount() {
            return 1;
        }

        @Nullable
        @Override
        public Section getSection(int index) {
            if (0 == index) {
                return mSection;
            } else {
                return null;
            }
        }

        @Nullable
        @Override
        public Section getSection(@NonNull SectionId sectionId) {
            if (sectionId.equals(mSection.getId())) {
                return mSection;
            } else {
                return null;
            }
        }

        @NonNull
        @Override
        public List<Section> getSections() {
            return Collections.singletonList(mSection);
        }

        @NonNull
        @Override
        public void removeAt(int index) {
            // No-op
        }
    }
}
