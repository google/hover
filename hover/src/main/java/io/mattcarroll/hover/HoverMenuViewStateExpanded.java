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
package io.mattcarroll.hover;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.ListUpdateCallback;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 */
class HoverMenuViewStateExpanded implements HoverMenuViewState {

    private static final String TAG = "HoverMenuViewStateExpanded";

    private boolean mHasControl = false;
    private HoverMenu.SectionId mActiveSectionId;
    private String mPrimaryTabId; // TODO: temporary ref to 1st tab until we get tab equality
    private FloatingTab mPrimaryTab;
    private HoverMenu mMenu;
    private Screen mScreen;
    private final List<FloatingTab> mChainedTabs = new ArrayList<>();
    private final List<TabChain> mTabChains = new ArrayList<>();
    private final Map<FloatingTab, HoverMenu.Section> mSections = new HashMap<>();
    private Point mDock;
    private Listener mListener;

    private final Runnable mShowTabsRunnable = new Runnable() {
        @Override
        public void run() {
            mScreen.getShadeView().show();
            mScreen.getContentDisplay().activeTabIs(mPrimaryTab);

            HoverMenu.Section activeSection = null != mActiveSectionId
                    ? mMenu.getSection(mActiveSectionId)
                    : mMenu.getSection(0);
            mScreen.getContentDisplay().displayContent(activeSection.getContent());

            if (null != mListener) {
                mListener.onExpanded();
            }
        }
    };

    HoverMenuViewStateExpanded() { }

    HoverMenuViewStateExpanded(@Nullable HoverMenu.SectionId activeSectionId) {
        if (null != activeSectionId) {
            mActiveSectionId = activeSectionId;
        }
    }

    @Override
    public void takeControl(@NonNull Screen screen, @NonNull String primaryTabId) {
        Log.d(TAG, "Taking control.");
        if (mHasControl) {
            throw new RuntimeException("Cannot take control of a FloatingTab when we already control one.");
        }

        Log.d(TAG, "Taking control.");
        mHasControl = true;
        mActiveSectionId = new HoverMenu.SectionId(primaryTabId);
        mPrimaryTabId = primaryTabId;
        mScreen = screen;
        mDock = new Point(mScreen.getWidth() - 100, 100); // TODO: get rid of magic numbers
        if (null != mMenu) {
            Log.d(TAG, "Already has menu. Expanding.");
            expandMenu();
        }
    }

    private void expandMenu() {
        createChainedTabs();
        chainTabs();
        mPrimaryTab.dockTo(mPrimaryTab.getDockPosition(), mShowTabsRunnable);

        if (null != mListener) {
            mListener.onExpanding();
        }
    }

    private void createChainedTabs() {
        Log.d(TAG, "Creating chained tabs");
        if (null != mMenu) {
            for (int i = 0; i < mMenu.getSectionCount(); ++i) {
                HoverMenu.Section section = mMenu.getSection(i);
                Log.d(TAG, "Creating tab view for: " + section.getId().toString());
                final FloatingTab chainedTab = mScreen.createChainedTab(
                        section.getId().toString(),
                        section.getTabView()
                );
                Log.d(TAG, "Created FloatingTab for ID " + section.getId().toString());

                if (!mPrimaryTabId.equals(section.getId().toString())) {
                    chainedTab.disappearImmediate();
                } else {
                    mPrimaryTab = chainedTab;
                }

                Log.d(TAG, "Adding tabView: " + section.getTabView() + ". Its parent is: " + section.getTabView().getParent());
                mChainedTabs.add(chainedTab);
                mSections.put(chainedTab, section);
                mTabChains.add(new TabChain(chainedTab));

                chainedTab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: is not stable and is crashing.
                        onTabSelected(chainedTab);
                    }
                });
            }
        }
    }

    private void chainTabs() {
        Log.d(TAG, "Chaining tabs.");
        Tab predecessorTab = mChainedTabs.get(0);

        // Find the selected tab.
        int primaryTabIndex = 0;
        for (int i = 0; i < mChainedTabs.size(); ++i) {
            if (mPrimaryTab == mChainedTabs.get(i)) {
                primaryTabIndex = i;
                break;
            }
        }

        for (int i = 0; i < mChainedTabs.size(); ++i) {
            final FloatingTab chainedTab = mChainedTabs.get(i);
            final TabChain tabChain = mTabChains.get(i);

            if (i == 0) {
                // TODO: generalize the notion of a predecessor so that the 1st tab doesn't need
                // TODO: to be treated in a special way.
                tabChain.chainTo(mDock);
                tabChain.tightenChain();
            } else {
                final Tab currentPredecessor = predecessorTab;
                int displayDelayInMillis = (int) (Math.abs(primaryTabIndex - i) * 100);
                tabChain.chainTo(currentPredecessor);
                chainedTab.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Chaining " + chainedTab.getTabId() + " to " + currentPredecessor.getTabId());
                        tabChain.tightenChain();
                    }
                }, displayDelayInMillis);
            }

            predecessorTab = chainedTab;
        }
    }

    @Override
    public void giveControlTo(@NonNull HoverMenuViewState otherController) {
        Log.d(TAG, "Giving up control.");
        if (!mHasControl) {
            throw new RuntimeException("Cannot give control to another HoverMenuController when we don't have the HoverTab.");
        }

        mHasControl = false;
        mMenu.setUpdatedCallback(null);
        mMenu = null;
        unchainTabs();
        mScreen.getShadeView().hide();
        otherController.takeControl(mScreen, mPrimaryTabId);
    }

    private void unchainTabs() {
        int primaryTabIndex = 0;
        for (int i = 0; i < mChainedTabs.size(); ++i) {
            if (mPrimaryTab == mChainedTabs.get(i)) {
                primaryTabIndex = i;
                break;
            }
        }

        for (int i = 0; i < mChainedTabs.size(); ++i) {
            final FloatingTab chainedTab = mChainedTabs.get(i);
            final TabChain tabChain = mTabChains.get(i);

            if (mPrimaryTab != chainedTab) {
                int displayDelayInMillis = (int) (Math.abs(primaryTabIndex - i) * 100);
                chainedTab.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tabChain.unchain(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "Destroying chained tab: " + chainedTab);
                                mScreen.destroyChainedTab(chainedTab);
                            }
                        });
                    }
                }, displayDelayInMillis);
            }
        }
        mChainedTabs.clear();
        mTabChains.clear();
    }

    public void setMenu(@NonNull HoverMenu menu) {
        Log.d(TAG, "Setting menu.");
        mMenu = menu;
        mMenu.setUpdatedCallback(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.d(TAG, "onInserted. Position: " + position + ", Count: " + count);
                int[] sectionIndices = new int[count];
                for (int i = position; i < position + count; ++i) {
                    sectionIndices[i - position] = i;
                }
                createTabsForIndices(sectionIndices);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d(TAG, "onRemoved. Position: " + position + ", Count: " + count);
                int[] sectionIndices = new int[count];
                for (int i = position; i < position + count; ++i) {
                    sectionIndices[i - position] = i;
                }
                removeSections(sectionIndices);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.d(TAG, "onMoved from: " + fromPosition + ", to: " + toPosition);
                reorderSection(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.d(TAG, "Tab(s) changed. From: " + position + ", To: " + count);
                int[] sectionIndices = new int[count];
                for (int i = position; i < position + count; ++i) {
                    sectionIndices[i - position] = i;
                }
                updateSections(sectionIndices);
            }
        });

        if (mHasControl) {
            Log.d(TAG, "Has control.  Expanding menu.");
            expandMenu();
        }
    }

    private void createTabsForIndices(int ... sectionIndices) {
        for (int sectionIndex : sectionIndices) {
            Log.d(TAG, "Creating tab for section at index " + sectionIndex);
            HoverMenu.Section section = mMenu.getSection(sectionIndex);
            Log.d(TAG, "Adding new tab. Section: " + sectionIndex + ", ID: " + section.getId());
            FloatingTab newTab = addTab(section.getId(), section.getTabView(), sectionIndex);
            mSections.put(newTab, section);
        }

        updateChainedPositions();
    }

    private FloatingTab addTab(@NonNull HoverMenu.SectionId sectionId,
                               @NonNull View tabView,
                               int position) {
        final FloatingTab newTab = mScreen.createChainedTab(
                sectionId.toString(),
                tabView
        );
        newTab.disappearImmediate();
        if (mChainedTabs.size() <= position) {
            // This section was appended to the end.
            mChainedTabs.add(newTab);
            mTabChains.add(new TabChain(newTab));
        } else {
            mChainedTabs.add(position, newTab);
            mTabChains.add(position, new TabChain(newTab));
        }

        newTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTabSelected(newTab);
            }
        });

        return newTab;
    }

    private void reorderSection(int fromPosition, int toPosition) {
        Log.d(TAG, "Tab moved. From: " + fromPosition + ", To: " + toPosition);
        FloatingTab chainedTab = mChainedTabs.remove(fromPosition);
        mChainedTabs.add(toPosition, chainedTab);
        TabChain tabChain = mTabChains.remove(fromPosition);
        mTabChains.add(toPosition, tabChain);

        updateChainedPositions();
    }

    private void updateSections(int ... sectionIndices) {
        Log.d(TAG, "Tab(s) changed: " + Arrays.toString(sectionIndices));
        for (int sectionIndex : sectionIndices) {
            updateSection(sectionIndex);
        }
    }

    private void updateSection(int sectionIndex) {
        HoverMenu.Section section = mMenu.getSection(sectionIndex);
        if (null == section) {
            Log.e(TAG, "Tried to update section " + sectionIndex + " but could not locate the corresponding Section.");
            return;
        }

        // Update Tab View
        FloatingTab chainedTab = mChainedTabs.get(sectionIndex);
        chainedTab.setTabView(section.getTabView());

        // Update Section Content if this Section's Content is currently active.
        if (mActiveSectionId.equals(mMenu.getSection(sectionIndex).getId())) {
            mScreen.getContentDisplay().displayContent(section.getContent());
        }
    }

    private void removeSections(int ... sectionIndices) {
        Log.d(TAG, "Tab(s) removed: " + Arrays.toString(sectionIndices));
        for (int sectionIndex : sectionIndices) {
            removeSection(sectionIndex);
        }

        updateChainedPositions();
    }

    private void removeSection(int sectionIndex) {
        final FloatingTab chainedTab = mChainedTabs.remove(sectionIndex);
        TabChain tabChain = mTabChains.remove(sectionIndex);
        tabChain.unchain(new Runnable() {
            @Override
            public void run() {
                mScreen.destroyChainedTab(chainedTab);
            }
        });

        // If the removed section was the selected section then select a new section.
        HoverMenu.Section removedSection = mSections.get(chainedTab);
        if (removedSection.getId().equals(mActiveSectionId)) {
            int newSelectionIndex = sectionIndex > 0 ? sectionIndex - 1 : 0;
            selectSection(mMenu.getSection(newSelectionIndex));
        }

        // TODO: This cleanup should be centralized.
        chainedTab.setOnClickListener(null);
        mSections.remove(chainedTab);
    }

    public HoverMenu.SectionId getActiveSectionId() {
        return mActiveSectionId;
    }

    private void updateChainedPositions() {
        TabChain firstChain = mTabChains.get(0);
        firstChain.chainTo(mDock);
        firstChain.tightenChain();

        Tab predecessor = mChainedTabs.get(0);
        for (int i = 1; i < mChainedTabs.size(); ++i) {
            FloatingTab chainedTab = mChainedTabs.get(i);
            TabChain tabChain = mTabChains.get(i);
            tabChain.chainTo(predecessor);
            tabChain.tightenChain();
            predecessor = chainedTab;
        }
    }

    private void onTabSelected(@NonNull FloatingTab selectedTab) {
        HoverMenu.Section section = mSections.get(selectedTab);
        if (!section.getId().equals(mActiveSectionId)) {
            selectSection(section);
        } else if (null != mListener) {
            mListener.onCollapseRequested();
        }
    }

    private void selectSection(@NonNull HoverMenu.Section section) {
        mActiveSectionId = section.getId();
        mPrimaryTabId = mActiveSectionId.toString();
        mPrimaryTab = mScreen.createChainedTab(mPrimaryTabId, null);
        ContentDisplay contentDisplay = mScreen.getContentDisplay();
        contentDisplay.activeTabIs(mPrimaryTab);
        contentDisplay.displayContent(section.getContent());
    }

    public void setListener(@NonNull Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onExpanding();

        void onExpanded();

        void onCollapseRequested();
    }
}
