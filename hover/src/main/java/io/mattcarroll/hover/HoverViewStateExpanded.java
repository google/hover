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
 * {@link HoverViewState} that operates the {@link HoverView} when it is expanded. Expanded means
 * that all menu tabs are displayed along the top of the {@code HoverView} and the selected
 * {@link HoverMenu.Section}'s {@link Content} is displayed below the row of tabs.
 *
 * When the selected tab is tapped again by the user, the {@code HoverView} is transitioned to its
 * collapsed state.
 */
class HoverViewStateExpanded extends BaseHoverViewState {

    private static final String TAG = "HoverMenuViewStateExpanded";
    private static final int ANCHOR_TAB_X_OFFSET_IN_PX = 100;
    private static final int ANCHOR_TAB_Y_OFFSET_IN_PX = 100;
    private static final int TAB_SPACING_IN_PX = 200;
    private static final int TAB_APPEARANCE_DELAY_IN_MS = 100;

    private boolean mHasControl = false;
    private HoverView mHoverView;
    private boolean mHasMenu = false;
    private FloatingTab mPrimaryTab;
    private final List<FloatingTab> mChainedTabs = new ArrayList<>();
    private final List<TabChain> mTabChains = new ArrayList<>();
    private final Map<FloatingTab, HoverMenu.Section> mSections = new HashMap<>();
    private Point mDock;
    private Listener mListener;

    private final Runnable mShowTabsRunnable = new Runnable() {
        @Override
        public void run() {
            mHoverView.mScreen.getShadeView().show();
            mHoverView.mScreen.getContentDisplay().activeTabIs(mPrimaryTab);

            HoverMenu.Section activeSection = null != mHoverView.mSelectedSectionId
                    ? mHoverView.mMenu.getSection(mHoverView.mSelectedSectionId)
                    : mHoverView.mMenu.getSection(0);
            mHoverView.mScreen.getContentDisplay().displayContent(activeSection.getContent());

            mHoverView.mScreen.getContentDisplay().setVisibility(View.VISIBLE);

            mHoverView.notifyListenersExpanded();
            if (null != mListener) {
                mListener.onExpanded();
            }
        }
    };

    HoverViewStateExpanded() { }

    @Override
    public void takeControl(@NonNull HoverView hoverView) {
        Log.d(TAG, "Taking control.");
        super.takeControl(hoverView);
        if (mHasControl) {
            throw new RuntimeException("Cannot take control of a FloatingTab when we already control one.");
        }

        mHasControl = true;
        mHoverView = hoverView;
        mHoverView.mState = this;
        mHoverView.makeTouchableInWindow();
        mHoverView.requestFocus(); // For handling hardware back button presses.
        mDock = new Point(
                mHoverView.mScreen.getWidth() - ANCHOR_TAB_X_OFFSET_IN_PX,
                ANCHOR_TAB_Y_OFFSET_IN_PX
        );
        if (null != mHoverView.mMenu) {
            Log.d(TAG, "Already has menu. Expanding.");
            setMenu(mHoverView.mMenu);
        }

        mHoverView.makeTouchableInWindow();
    }

    private void expandMenu() {
        // If the primary tab is not already visible then we want to dock it immediately without
        // animation.
        boolean dockPrimaryTabImmediately = null == mHoverView.mScreen.getChainedTab(mHoverView.mSelectedSectionId.toString());

        createChainedTabs();
        chainTabs(!dockPrimaryTabImmediately);

        if (dockPrimaryTabImmediately) {
            mPrimaryTab.dockImmediately();
            mHoverView.post(mShowTabsRunnable);
        } else {
            mPrimaryTab.dock(mShowTabsRunnable);
        }

        mHoverView.notifyListenersExpanding();
        if (null != mListener) {
            mListener.onExpanding();
        }
    }

    private void createChainedTabs() {
        Log.d(TAG, "Creating chained tabs");
        if (null != mHoverView.mMenu) {
            for (int i = 0; i < mHoverView.mMenu.getSectionCount(); ++i) {
                HoverMenu.Section section = mHoverView.mMenu.getSection(i);
                Log.d(TAG, "Creating tab view for: " + section.getId().toString());
                final FloatingTab chainedTab = mHoverView.mScreen.createChainedTab(
                        section.getId().toString(),
                        section.getTabView()
                );
                Log.d(TAG, "Created FloatingTab for ID " + section.getId().toString());

                if (!mHoverView.mSelectedSectionId.equals(section.getId())) {
                    chainedTab.disappearImmediate();
                } else {
                    mPrimaryTab = chainedTab;
                }

                Log.d(TAG, "Adding tabView: " + section.getTabView() + ". Its parent is: " + section.getTabView().getParent());
                mChainedTabs.add(chainedTab);
                mSections.put(chainedTab, section);
                mTabChains.add(new TabChain(chainedTab, TAB_SPACING_IN_PX));

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

    private void chainTabs(boolean animatePrimaryTab) {
        Log.d(TAG, "Chaining tabs.");
        FloatingTab predecessorTab = mChainedTabs.get(0);

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
                tabChain.tightenChain(!animatePrimaryTab);
            } else {
                final FloatingTab currentPredecessor = predecessorTab;
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
    public void expand() {
        Log.d(TAG, "Instructed to expand, but already expanded.");
    }

    @Override
    public void collapse() {
        Log.d(TAG, "Collapsing.");
        changeState(mHoverView.mCollapsed);
    }

    @Override
    public void close() {
        Log.d(TAG, "Closing.");
        changeState(mHoverView.mClosed);
    }

    private void changeState(@NonNull final HoverViewState nextState) {
        Log.d(TAG, "Giving up control.");
        if (!mHasControl) {
            throw new RuntimeException("Cannot give control to another HoverMenuController when we don't have the HoverTab.");
        }

        if (null != mHoverView.mMenu) {
            mHoverView.mMenu.setUpdatedCallback(null);
        }

        mHasControl = false;
        mHasMenu = false;
        mHoverView.mScreen.getContentDisplay().activeTabIs(null);
        mHoverView.mScreen.getContentDisplay().displayContent(null);
        mHoverView.mScreen.getContentDisplay().setVisibility(View.GONE);
        mHoverView.mScreen.getShadeView().hide();
        mHoverView.setState(nextState);
        unchainTabs(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Running unchained runnable.");
                // We wait to nullify our HoverMenuView because some final animations need it.
                // TODO: maybe the answer is for the collapse state to handle what happens to the tabs and content display and shade?
                mHoverView = null;
            }
        });
    }

    private int mTabsToUnchainCount;
    private void unchainTabs(@Nullable final Runnable onUnChained) {
        int primaryTabIndex = 0;
        for (int i = 0; i < mChainedTabs.size(); ++i) {
            if (mPrimaryTab == mChainedTabs.get(i)) {
                primaryTabIndex = i;
                break;
            }
        }

        int unchainCompletionTime = 0;
        mTabsToUnchainCount = mChainedTabs.size() - 1; // -1 for primary tab
        for (int i = 0; i < mChainedTabs.size(); ++i) {
            final FloatingTab chainedTab = mChainedTabs.get(i);
            final TabChain tabChain = mTabChains.get(i);

            if (mPrimaryTab != chainedTab) {
                int displayDelayInMillis = Math.abs(primaryTabIndex - i) * TAB_APPEARANCE_DELAY_IN_MS;
                unchainCompletionTime = Math.max(unchainCompletionTime, displayDelayInMillis);
                Log.d(TAG, "Queue'ing chained tab disappearance with delay: " + displayDelayInMillis);
                chainedTab.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tabChain.unchain(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "Destroying chained tab: " + chainedTab);
                                mHoverView.mScreen.destroyChainedTab(chainedTab);

                                --mTabsToUnchainCount;
                                if (0 == mTabsToUnchainCount && null != onUnChained) {
                                    onUnChained.run();
                                }
                            }
                        });
                    }
                }, displayDelayInMillis);
            }
        }

        mChainedTabs.clear();
        mTabChains.clear();

        // If there was only 1 tab, run onUnChained callback now.
        if (0 == mTabsToUnchainCount && null != onUnChained) {
            onUnChained.run();
        }
    }

    @Override
    public void setMenu(@Nullable HoverMenu menu) {
        Log.d(TAG, "Setting menu.");
        mHoverView.mMenu = menu;

        // Expanded menus can't be null/empty.  If it is then go to closed state.
        if (null == mHoverView.mMenu || mHoverView.mMenu.getSectionCount() == 0) {
            close();
            return;
        }

        mHoverView.restoreVisualState();

        if (null == mHoverView.mSelectedSectionId || null == mHoverView.mMenu.getSection(mHoverView.mSelectedSectionId)) {
            mHoverView.mSelectedSectionId = mHoverView.mMenu.getSection(0).getId();
        }

        mHoverView.mMenu.setUpdatedCallback(new ListUpdateCallback() {
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

        if (mHasControl && !mHasMenu) {
            Log.d(TAG, "Has control.  Received initial menu.  Expanding menu.");
            expandMenu();
        } else if (mHasControl) {
            Log.d(TAG, "Has control.  Already had menu.  Switching menu.");
            transitionDisplayFromOldMenuToNew();
        }
        mHasMenu = true;
    }

    private void transitionDisplayFromOldMenuToNew() {
        // TODO: implement a generalized display update mechanism rather than have sprawling update
        // TODO: logic throughout this Class.
        for (int i = 0; i < mHoverView.mMenu.getSectionCount(); ++i) {
            if (i < mChainedTabs.size()) {
                updateSection(i);
            } else {
                createTabsForIndices(i);
            }
        }

        if (mChainedTabs.size() > mHoverView.mMenu.getSectionCount()) {
            int[] removedSections = new int[mChainedTabs.size() - mHoverView.mMenu.getSectionCount()];
            for (int i = mHoverView.mMenu.getSectionCount(); i < mChainedTabs.size(); ++i) {
                removedSections[i - mHoverView.mMenu.getSectionCount()] = i;
            }
            removeSections(removedSections);
        }
    }

    @Override
    public boolean respondsToBackButton() {
        return true;
    }

    @Override
    public void onBackPressed() {
        collapse();
    }

    private void createTabsForIndices(int ... sectionIndices) {
        for (int sectionIndex : sectionIndices) {
            Log.d(TAG, "Creating tab for section at index " + sectionIndex);
            HoverMenu.Section section = mHoverView.mMenu.getSection(sectionIndex);
            Log.d(TAG, "Adding new tab. Section: " + sectionIndex + ", ID: " + section.getId());
            FloatingTab newTab = addTab(section.getId(), section.getTabView(), sectionIndex);
            mSections.put(newTab, section);
        }

        updateChainedPositions();
    }

    private FloatingTab addTab(@NonNull HoverMenu.SectionId sectionId,
                               @NonNull View tabView,
                               int position) {
        final FloatingTab newTab = mHoverView.mScreen.createChainedTab(
                sectionId.toString(),
                tabView
        );
        newTab.disappearImmediate();
        if (mChainedTabs.size() <= position) {
            // This section was appended to the end.
            mChainedTabs.add(newTab);
            mTabChains.add(new TabChain(newTab, TAB_SPACING_IN_PX));
        } else {
            mChainedTabs.add(position, newTab);
            mTabChains.add(position, new TabChain(newTab, TAB_SPACING_IN_PX));
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
        HoverMenu.Section section = mHoverView.mMenu.getSection(sectionIndex);
        if (null == section) {
            Log.e(TAG, "Tried to update section " + sectionIndex + " but could not locate the corresponding Section.");
            return;
        }

        // Update Tab View
        FloatingTab chainedTab = mChainedTabs.get(sectionIndex);
        chainedTab.setTabView(section.getTabView());

        // Update Section Content if this Section's Content is currently active.
        if (mHoverView.mSelectedSectionId.equals(mHoverView.mMenu.getSection(sectionIndex).getId())) {
            mHoverView.mScreen.getContentDisplay().displayContent(section.getContent());
        }
    }

    private void removeSections(int ... sectionIndices) {
        Log.d(TAG, "Tab(s) removed: " + Arrays.toString(sectionIndices));
        // Sort the indices so that they appear from lowest to highest.  Then process
        // in reverse order so that we don't remove sections out from under us.
        Arrays.sort(sectionIndices);
        for (int i = sectionIndices.length - 1; i >= 0; --i) {
            removeSection(sectionIndices[i]);
        }

        updateChainedPositions();
    }

    private void removeSection(int sectionIndex) {
        final FloatingTab chainedTab = mChainedTabs.remove(sectionIndex);
        TabChain tabChain = mTabChains.remove(sectionIndex);
        tabChain.unchain(new Runnable() {
            @Override
            public void run() {
                mHoverView.mScreen.destroyChainedTab(chainedTab);
            }
        });

        // If the removed section was the selected section then select a new section.
        HoverMenu.Section removedSection = mSections.get(chainedTab);
        if (removedSection.getId().equals(mHoverView.mSelectedSectionId)) {
            int newSelectionIndex = 0;
            if (sectionIndex - 1 < mHoverView.mMenu.getSectionCount() - 1) {
                newSelectionIndex = sectionIndex - 1;
            } else {
                newSelectionIndex = mHoverView.mMenu.getSectionCount() - 1;
            }

            selectSection(mHoverView.mMenu.getSection(newSelectionIndex));
        }

        // TODO: This cleanup should be centralized.
        chainedTab.setOnClickListener(null);
        mSections.remove(chainedTab);
    }

    private void updateChainedPositions() {
        TabChain firstChain = mTabChains.get(0);
        firstChain.chainTo(mDock);
        firstChain.tightenChain();

        FloatingTab predecessor = mChainedTabs.get(0);
        for (int i = 1; i < mChainedTabs.size(); ++i) {
            FloatingTab chainedTab = mChainedTabs.get(i);
            TabChain tabChain = mTabChains.get(i);
            tabChain.chainTo(predecessor);
            tabChain.tightenChain();
            predecessor = chainedTab;
        }
    }

    private void onTabSelected(@NonNull FloatingTab selectedTab) {
        Log.d(TAG, "onTabSelected(). Selected section: " + mSections.get(selectedTab).getId().toString() + ", mSelectedSectionId: " + mHoverView.mSelectedSectionId.toString());
        HoverMenu.Section section = mSections.get(selectedTab);
        if (!section.getId().equals(mHoverView.mSelectedSectionId)) {
            selectSection(section);
        } else {
            collapse();
        }
    }

    private void selectSection(@NonNull HoverMenu.Section section) {
        mHoverView.mSelectedSectionId = section.getId();
        mPrimaryTab = mHoverView.mScreen.createChainedTab(mHoverView.mSelectedSectionId.toString(), null);
        ContentDisplay contentDisplay = mHoverView.mScreen.getContentDisplay();
        contentDisplay.activeTabIs(mPrimaryTab);
        contentDisplay.displayContent(section.getContent());
    }

    // TODO: do we need this?
    public void setListener(@NonNull Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onExpanding();

        void onExpanded();

        // TODO: do we need this?
        void onCollapseRequested();
    }
}
