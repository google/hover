package io.mattcarroll.hover.defaulthovermenu.ziggle;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.R;
import io.mattcarroll.hover.defaulthovermenu.DefaultNavigator;
import io.mattcarroll.hover.defaulthovermenu.Dragger;
import io.mattcarroll.hover.defaulthovermenu.view.InViewGroupDragger;

/**
 * TODO
 */
public class HoverMenuView2 extends RelativeLayout {

    private static final String TAG = "HoverMenuView2";

    //------ Views From Layout XML -------
    private TabStrip mTabStrip; // Horizontal tabs that appear at the top of the menu
    private HoverMenuContentView2 mContentView; // Displays Hover Menu content
    private View mShadeView; // Dark backdrop that fills screen behind menu
    private ExitView mExitView; // Appears when user is dragging with a "drop zone" to exit the menu

    private final Dragger mDragger;
    private DefaultHoverTab mHoverTab;
    private HoverMenuAdapter mAdapter;
    private int mActiveTabPosition = 0;
    private Navigator mNavigator;
    private FloatingHoverMenuController mFloatingController;
    private boolean mIsExpanded = false;
    private int mSelectedTab = 0;

    public HoverMenuView2(Context context, @NonNull Dragger dragger) {
        super(context);
        mDragger = dragger;
        init();
    }

    private void init() {
        Log.d(TAG, "init() " + hashCode());
        LayoutInflater.from(getContext()).inflate(R.layout.view_hover_menu_2, this, true);

        mTabStrip = (TabStrip) findViewById(R.id.tabstrip);
        mContentView = (HoverMenuContentView2) findViewById(R.id.view_content);
        mShadeView = findViewById(R.id.view_shade);
        mExitView = (ExitView) findViewById(R.id.exitview);

        mHoverTab = new DefaultHoverTab(getContext());
        addView(mHoverTab);

        mNavigator = new DefaultNavigator(getContext(), true);
        mContentView.setContentResizer(mHoverMenuContentResizer);
        mContentView.setNavigator(mNavigator);

        mFloatingController = new FloatingHoverMenuController(getContext(), mDragger, new Point(100, 500));
        mFloatingController.setListener(new FloatingHoverMenuController.Listener() {
            @Override
            public void onTap() {
                expand();
            }
        });
        addView(mFloatingController);

        setBackgroundColor(0x66FF0000);

        mTabStrip.setInteractionListener(new TabStrip.InteractionListener() {
            @Override
            public void onTabClicked(int id) {
                boolean isSameAsAlreadySelectedTab = id == mActiveTabPosition;
                if (isSameAsAlreadySelectedTab) {
                    collapse();
                } else {
                    mActiveTabPosition = id;
                    mNavigator.clearContent();
                    mNavigator.pushContent(mAdapter.getNavigatorContent(id));
                }
            }
        });

        mTabStrip.setVisualListener(new TabStrip.VisualListener() {
            @Override
            public void onActiveTabChange(@NonNull View activeTabView) {
                mContentView.setActiveTab(activeTabView);
            }

            @Override
            public void onTabsAppearing() {

            }

            @Override
            public void onTabsAppeared() {

            }

            @Override
            public void onTabsDisappearing() {

            }

            @Override
            public void onTabsDisappeared() {

            }
        });
    }

    public void setAdapter(@Nullable HoverMenuAdapter adapter) {
        if (null != mAdapter) {
            // TODO: cleanup existing menu.
        }

        mAdapter = adapter;

        if (null != mAdapter) {
            View hoverTabView = mAdapter.getTabView(0);
            mHoverTab.setTabView(hoverTabView);

            if (mIsExpanded) {
                mTabStrip.takeControl(mHoverTab);
            } else {
                mFloatingController.takeControl(mHoverTab);
            }

            List<View> regularTabs = new ArrayList<>();
            for (int i = 1; i < mAdapter.getTabCount(); ++i) {
                regularTabs.add(mAdapter.getTabView(i));
            }
            mTabStrip.setRegularTabs(regularTabs);

            mNavigator.pushContent(mAdapter.getNavigatorContent(mActiveTabPosition));

            // Demo expand and collapse
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    expand();

//                postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        collapse();
//                    }
//                }, 5000);
                }
            }, 5000);
        }
    }

    private void expand() {
        if (!mIsExpanded) {
            mIsExpanded = true;
            mTabStrip.expandTabs();
            mContentView.expand();
            mShadeView.setVisibility(VISIBLE);
            mExitView.setVisibility(GONE);
            mFloatingController.giveControlTo(mTabStrip);
        }
    }

    private void collapse() {
        if (mIsExpanded) {
            mIsExpanded = false;
            mTabStrip.collapseTabs();
            mContentView.collapse();
            mShadeView.setVisibility(GONE);
            mExitView.setVisibility(VISIBLE);
            mTabStrip.giveControlTo(mFloatingController);
        }
    }





    private HoverMenuContentView2.HoverMenuContentResizer mHoverMenuContentResizer = new HoverMenuContentView2.HoverMenuContentResizer() {
        @Override
        public void makeHoverMenuContentFullscreen() {
            RelativeLayout.LayoutParams contentContainerLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            contentContainerLayoutParams.height = 0;
            contentContainerLayoutParams.addRule(RelativeLayout.BELOW, R.id.tabstrip);
            contentContainerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            contentContainerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            contentContainerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mContentView.setLayoutParams(contentContainerLayoutParams);
        }

        @Override
        public void makeHoverMenuContentAsTallAsItsContent() {
            RelativeLayout.LayoutParams contentContainerLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            contentContainerLayoutParams.addRule(RelativeLayout.BELOW, R.id.tabstrip);
            contentContainerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            contentContainerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            mContentView.setLayoutParams(contentContainerLayoutParams);
        }
    };
}
