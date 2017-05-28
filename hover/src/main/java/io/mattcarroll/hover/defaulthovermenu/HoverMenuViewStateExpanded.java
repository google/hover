package io.mattcarroll.hover.defaulthovermenu;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.ListUpdateCallback;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 */
class HoverMenuViewStateExpanded implements HoverMenuViewState {

    private static final String TAG = "HoverMenuViewStateExpanded";

    private boolean mHasControl = false;
    private HoverMenu.Section.SectionId mActiveSectionId;
    private HoverMenu mMenu;
    private Screen mScreen;
    private final List<FloatingTab> mChainedTabs = new ArrayList<>();
    private final List<TabChain> mTabChains = new ArrayList<>();
    private final Map<FloatingTab, HoverMenu.Section> mSections = new HashMap<>();
    private Point mDock;
    private Listener mListener;

    HoverMenuViewStateExpanded() { }

    HoverMenuViewStateExpanded(@Nullable String activeSectionId) {
        if (null != activeSectionId) {
            mActiveSectionId = new HoverMenu.Section.SectionId(activeSectionId);
        }
    }

    @Override
    public void takeControl(@NonNull Screen screen) {
        Log.d(TAG, "Taking control.");
        if (mHasControl) {
            throw new RuntimeException("Cannot take control of a FloatingTab when we already control one.");
        }

        Log.d(TAG, "Taking control.");
        mHasControl = true;
        mScreen = screen;
        mDock = new Point(mScreen.getWidth() - 100, 100); // TODO: get rid of magic numbers
        if (null != mMenu) {
            Log.d(TAG, "Already has menu. Expanding.");
            expandMenu();
        }
    }

    private void createChainedTabs() {
        Log.d(TAG, "Creating chained tabs");
        if (null != mMenu) {
            // TODO: it doesn't look like the initial tabs are being chained...
            // TODO: We shouldn't need to treat the first tab differently any more...
            FloatingTab firstTab = mScreen.createChainedTab("PRIMARY", null);
            mChainedTabs.add(firstTab);
            mTabChains.add(new TabChain(firstTab));
            mSections.put(firstTab, mMenu.getSection(0)); // TODO: we need to use an ID with the menu, not an index
            Log.d(TAG, "Created primary tab");
            for (int i = 1; i < mMenu.getSectionCount(); ++i) {
                HoverMenu.Section section = mMenu.getSection(i);
                View tabView = section.getTabView();
                final FloatingTab chainedTab = mScreen.createChainedTab(section.getId().toString(), tabView);
                chainedTab.disappearImmediate();
                Log.d(TAG, "Adding tabView: " + tabView + ". Its parent is: " + tabView.getParent());
                mChainedTabs.add(chainedTab);
                mTabChains.add(new TabChain(chainedTab));
                mSections.put(chainedTab, section);

                chainedTab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // is not stable and is crashing.
                        onTabSelected(chainedTab);
                    }
                });
            }
        }
    }

    private void destroyChainedTabs() {

    }

    private void chainTabs() {
        Tab predecessorTab = mChainedTabs.get(0);
        int displayDelay = 0;
        for (int i = 1; i < mChainedTabs.size(); ++i) {
            FloatingTab chainedTab = mChainedTabs.get(i);
            final TabChain tabChain = mTabChains.get(i);
            final Tab currentPredecessor = predecessorTab;
            chainedTab.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tabChain.chainTo(currentPredecessor);
                }
            }, displayDelay);

            predecessorTab = chainedTab;
            displayDelay += 100;
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
        otherController.takeControl(mScreen);
    }

    private void unchainTabs() {
        int displayDelay = 0;
        for (int i = 1; i < mChainedTabs.size(); ++i) {
            final FloatingTab chainedTab = mChainedTabs.get(i);
            final TabChain tabChain = mTabChains.get(i);
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
            }, displayDelay);

            displayDelay += 100;
        }
    }

    public void setMenu(@NonNull HoverMenu menu) {
        Log.d(TAG, "Setting menu.");
        mMenu = menu;
        mMenu.setUpdatedCallback(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.d(TAG, "Tab inserted. Position: " + position + ", Count: " + count);

                for (int i = 0; i < count; ++i) {
                    HoverMenu.Section section = mMenu.getSection(position + i);
                    Log.d(TAG, "Adding new tab. Section: " + (position + i) + ", ID: " + section.getId());
                    Log.d(TAG, " - tab View: " + section.getTabView().hashCode());
                    Log.d(TAG, " - screen: " + section.getContent().hashCode());
                    View tabView = section.getTabView();
                    FloatingTab newTab = mScreen.createChainedTab(section.getId().toString(), tabView);
                    newTab.disappearImmediate();
                    if (mChainedTabs.size() <= position) {
                        // This section was appended to the end.
                        mChainedTabs.add(newTab);
                        mTabChains.add(new TabChain(newTab));
                    } else {
                        int insertPosition = (position + i);
                        mChainedTabs.add(insertPosition, newTab);
                        mTabChains.add(insertPosition, new TabChain(newTab));
                    }
                }

                updateChainedPositions();
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d(TAG, "Tab(s) removed. Position: " + position + ", Count: " + count);
                for (int i = (position + count - 1); i >= position; --i) {
                    final FloatingTab chainedTab = mChainedTabs.remove(i);
                    TabChain tabChain = mTabChains.remove(i);
                    tabChain.unchain(new Runnable() {
                        @Override
                        public void run() {
                            mScreen.destroyChainedTab(chainedTab);
                        }
                    });
                }

                updateChainedPositions();
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.d(TAG, "Tab moved. From: " + fromPosition + ", To: " + toPosition);
                FloatingTab chainedTab = mChainedTabs.remove(fromPosition);
                mChainedTabs.add(toPosition, chainedTab);
                TabChain tabChain = mTabChains.remove(fromPosition);
                mTabChains.add(toPosition, tabChain);

                updateChainedPositions();
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.d(TAG, "Tab(s) changed. From: " + position + ", To: " + count);
                // TODO: tell content to update
            }
        });

        if (mHasControl) {
            Log.d(TAG, "Has control.  Expanding menu.");
            expandMenu();
        }
    }

    public String getActiveSectionId() {
        return mActiveSectionId.toString();
    }

    private void updateChainedPositions() {
        Tab predecessor = mChainedTabs.get(0);
        for (int i = 1; i < mChainedTabs.size(); ++i) {
            FloatingTab chainedTab = mChainedTabs.get(i);
            TabChain tabChain = mTabChains.get(i);
            tabChain.chainTo(predecessor);
            predecessor = chainedTab;
        }
    }

    private void expandMenu() {
//        HoverMenu.Section activeSection = null != mActiveSectionId ?
//                mMenu.getSection(Integer.valueOf(mActiveSectionId.toString())) : // TODO: change API to take SectionId
//                mMenu.getSection(0);
        final FloatingTab firstTab = mScreen.createChainedTab("PRIMARY", null); // TODO:

        mScreen.getShadeView().show();
        mScreen.getContentDisplay().anchorTo(firstTab);
//        mScreen.getContentDisplay().activeTabIs(activeTab);
//        mScreen.getContentDisplay().displayContent(activeSection.getContent());
        firstTab.dockTo(mDock, new Runnable() {
            @Override
            public void run() {
                createChainedTabs();
                chainTabs();

                final FloatingTab activeTab;
                if (null == mActiveSectionId || "0".equals(mActiveSectionId.toString())) {
                    activeTab = mScreen.createChainedTab("PRIMARY", null); // TODO:
                } else {
                    activeTab = mScreen.createChainedTab(mActiveSectionId.toString(), null);
                }
                mScreen.getContentDisplay().activeTabIs(activeTab);

                HoverMenu.Section activeSection = null != mActiveSectionId ?
                        mMenu.getSection(Integer.valueOf(mActiveSectionId.toString())) : // TODO: change API to take SectionId
                        mMenu.getSection(0);
                mScreen.getContentDisplay().displayContent(activeSection.getContent());

                if (null != mListener) {
                    mListener.onExpanded();
                }
            }
        });
        firstTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTabSelected(firstTab);
            }
        });

        if (null != mListener) {
            mListener.onExpanding();
        }
    }

    private void onTabSelected(@NonNull FloatingTab selectedTab) {
        HoverMenu.Section section = mSections.get(selectedTab);
        if (!section.getId().equals(mActiveSectionId)) {
            mActiveSectionId = section.getId();
            ContentDisplay contentDisplay = mScreen.getContentDisplay();
            contentDisplay.activeTabIs(selectedTab);
            contentDisplay.displayContent(section.getContent());
        } else if (null != mListener) {
            mListener.onCollapseRequested();
        }
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
