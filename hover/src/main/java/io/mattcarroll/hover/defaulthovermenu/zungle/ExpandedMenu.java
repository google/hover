package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.util.ListUpdateCallback;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mattcarroll.hover.HoverMenuAdapter;

/**
 * TODO
 */
public class ExpandedMenu implements FloatingTabOwner {

    private static final String TAG = "ExpandedMenu";

    private boolean mHasControl = false;
    private int mActiveTabIndex = -1;
    private HoverMenuAdapter mAdapter;
    private HoverMenu mMenu;
    private Screen mScreen;
    private FloatingTab mFloatingTab;
    private final List<ChainedTab> mChainedTabs = new ArrayList<>();
    private Point mDock;
    private Listener mListener;

    ExpandedMenu(@NonNull Screen screen) {
        mScreen = screen;
        mDock = new Point(mScreen.getWidth() - 100, 100);
    }

    @Override
    public void takeControl(@NonNull FloatingTab floatingIcon) {
        if (mHasControl) {
            throw new RuntimeException("Cannot take control of a FloatingTab when we already control one.");
        }

        mHasControl = true;
        mActiveTabIndex = 0; // TODO: handle restoration of active tab
        mFloatingTab = floatingIcon;
        mScreen.getShadeView().show();
        mScreen.getContentDisplay().activeTabIs(mFloatingTab);
        mFloatingTab.dockTo(mDock, new Runnable() {
            @Override
            public void run() {
                chainTabs();

                if (null != mListener) {
                    mListener.onExpanded();
                }
            }
        });
        mFloatingTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTabSelected(0);
            }
        });

        if (null != mListener) {
            mListener.onExpanding();
        }
    }

    private void createChainedTabs() {
//        if (null != mAdapter) {
//            for (int i = 1; i < mAdapter.getTabCount(); ++i) {
//                View tabView = mAdapter.getTabView(i);
//                ChainedTab chainedTab = mScreen.createChainedTab();
//                Log.d(TAG, "Adding tabView: " + tabView + ". Its parent is: " + tabView.getParent());
//                chainedTab.setTabView(tabView);
//                mChainedTabs.add(chainedTab);
//
//                final int tabIndex = i;
//                chainedTab.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        onTabSelected(tabIndex);
//                    }
//                });
//            }
//        }

        if (null != mMenu) {
            for (int i = 1; i < mMenu.getSectionCount(); ++i) {
                HoverMenu.Section section = mMenu.getSection(i);

                View tabView = section.getTabView();
                ChainedTab chainedTab = mScreen.createChainedTab();
                Log.d(TAG, "Adding tabView: " + tabView + ". Its parent is: " + tabView.getParent());
                chainedTab.setTabView(tabView);
                mChainedTabs.add(chainedTab);

                final int tabIndex = i;
                chainedTab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onTabSelected(tabIndex);
                    }
                });
            }
        }
    }

    private void destroyChainedTabs() {

    }

    private void chainTabs() {
        Tab predecessorTab = mFloatingTab;
        int displayDelay = 0;
        for (final ChainedTab chainedTab : mChainedTabs) {
            final Tab currentPredecessor = predecessorTab;
            chainedTab.postDelayed(new Runnable() {
                @Override
                public void run() {
                    chainedTab.chainTo(currentPredecessor);
                }
            }, displayDelay);

            predecessorTab = chainedTab;
            displayDelay += 100;
        }
    }

    @Override
    public void giveControlTo(@NonNull FloatingTabOwner otherController) {
        if (!mHasControl) {
            throw new RuntimeException("Cannot give control to another HoverMenuController when we don't have the HoverTab.");
        }

        mHasControl = false;
        unchainTabs();
        mScreen.getShadeView().hide();
        otherController.takeControl(mFloatingTab);
        mFloatingTab = null;
    }

    private void unchainTabs() {
        Tab predecessorTab = mFloatingTab;
        int displayDelay = 0;
        for (final ChainedTab chainedTab : mChainedTabs) {
            final Tab currentPredecessor = predecessorTab;
            chainedTab.postDelayed(new Runnable() {
                @Override
                public void run() {
                    chainedTab.unchain(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Destroying chained tab: " + chainedTab);
                            mScreen.destroyChainedTab(chainedTab);
                        }
                    });
                }
            }, displayDelay);

            predecessorTab = chainedTab;
            displayDelay += 100;
        }
    }

    public void setAdapter(@NonNull HoverMenuAdapter adapter) {
        mAdapter = adapter;
        mMenu = new HoverMenu(adapter);
        createChainedTabs();
        mScreen.getContentDisplay().displayContent(adapter.getNavigatorContent(0));

        mMenu.setUpdatedCallback(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.d(TAG, "Tab inserted. Position: " + position + ", Count: " + count);
                for (int i = 0; i < count; ++i) {
                    if (mChainedTabs.size() <= (position - 1)) { // -1 because first tab is primary tab
                        // This section was appended to the end.
                        mChainedTabs.add(mScreen.createChainedTab());
                    } else {
                        mChainedTabs.add((position + i - 1), mScreen.createChainedTab());
                    }
                }

                // Re-organize all the chains.
                Tab predecessor = mFloatingTab;
                for (int i = 0; i < mChainedTabs.size(); ++i) {
                    ChainedTab chainedTab = mChainedTabs.get(i);
                    chainedTab.chainTo(predecessor);
                    predecessor = chainedTab;
                }
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d(TAG, "Tab(s) removed. Position: " + position + ", Count: " + count);
                for (int i = (position + count - 1); i >= position; --i) {
                    final ChainedTab chainedTab = mChainedTabs.get(i - 1); // TODO: -1 because primary tab isn't in this list
                    mChainedTabs.remove(i - 1);
                    chainedTab.unchain(new Runnable() {
                        @Override
                        public void run() {
                            mScreen.destroyChainedTab(chainedTab);
                        }
                    });
                }

                // Re-organize all the chains.
                Tab predecessor = mFloatingTab;
                for (int i = 0; i < mChainedTabs.size(); ++i) {
                    ChainedTab chainedTab = mChainedTabs.get(i);
                    chainedTab.chainTo(predecessor);
                    predecessor = chainedTab;
                }
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.d(TAG, "Tab moved. From: " + fromPosition + ", To: " + toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.d(TAG, "Tab(s) changed. From: " + position + ", To: " + count);
            }
        });
    }

    private void onTabSelected(int index) {
        if (mActiveTabIndex != index) {
            mActiveTabIndex = index;
            ContentDisplay contentDisplay = mScreen.getContentDisplay();
            contentDisplay.activeTabIs(
                    0 == index ?
                            mFloatingTab :
                            mChainedTabs.get(index - 1)
            );
            contentDisplay.displayContent(mAdapter.getNavigatorContent(mActiveTabIndex));
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
