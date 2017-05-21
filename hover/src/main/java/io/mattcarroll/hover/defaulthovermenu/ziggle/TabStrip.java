package io.mattcarroll.hover.defaulthovermenu.ziggle;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import io.mattcarroll.hover.R;

/**
 * TODO:
 */
public class TabStrip extends RelativeLayout implements HoverMenuController {

    private static final String TAG = "TabStrip";

    private FrameLayout mPrimaryTabHolder;
    private HoverTab mPrimaryTab;
    private final List<View> mRegularTabs = new ArrayList<>();
    private boolean mIsExpanded = false;
    private int mActiveTabIndex = 0;

    private InteractionListener mInteractionListener;
    private VisualListener mVisualListener;

    private final OnClickListener mPrimaryTabOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            activateTab(0);
        }
    };

    public TabStrip(Context context) {
        this(context, null);
    }

    public TabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        createPrimaryTabHolder();
    }

    private void createPrimaryTabHolder() {
        int tabSize = getResources().getDimensionPixelSize(R.dimen.floating_icon_size);
        mPrimaryTabHolder = new FrameLayout(getContext());
        mPrimaryTabHolder.setId(R.id.primary_tab_holder);
        LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                tabSize,
                tabSize);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mPrimaryTabHolder.setLayoutParams(layoutParams);
        addView(mPrimaryTabHolder);
    }

    @Override
    public void takeControl(@NonNull HoverTab floatingIcon) {
        if (null != mPrimaryTab) {
            throw new RuntimeException("Cannot take control of a floatingIcon when we already control one.");
        }

        mPrimaryTab = floatingIcon;
        mPrimaryTab.setOnClickListener(mPrimaryTabOnClickListener);

        Point tabAnchorLocation = new Point(
                ((int) getX()) + (int) mPrimaryTabHolder.getX() + (mPrimaryTabHolder.getWidth() / 2),
                ((int) getY()) + (int) mPrimaryTabHolder.getY() + (mPrimaryTabHolder.getHeight() / 2)
        );
        Log.d(TAG, "Anchor screen pos: (" + tabAnchorLocation.x + ", " + tabAnchorLocation.y + ")");
        mPrimaryTab.slideTo(tabAnchorLocation);
    }

    @Override
    public void giveControlTo(@NonNull HoverMenuController otherController) {
        if (null == mPrimaryTab) {
            throw new RuntimeException("Cannot give control to another HoverMenuController when we don't have the primary tab.");
        }

        mPrimaryTab.setOnClickListener(null);
        otherController.takeControl(mPrimaryTab);
        mPrimaryTab = null;
    }

    public void setRegularTabs(@NonNull List<View> regularTabs) {
        removeRegularTabs();
        mRegularTabs.clear();
        mRegularTabs.addAll(regularTabs);
        addRegularTabs();

        mActiveTabIndex = 0;
        reportActiveTabPosition();
    }

    private void removeRegularTabs() {
        for (View tab : mRegularTabs) {
            removeView(tab);
        }
    }

    private void addRegularTabs() {
        View anchorView = mPrimaryTabHolder;
        int tabIndex = 1;
        for (View tabView : mRegularTabs) {
            tabView.setId((int)(Math.random() * 100000000));
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(RelativeLayout.LEFT_OF, anchorView.getId());
            tabView.setVisibility(mIsExpanded ? VISIBLE : INVISIBLE);
            addView(tabView, layoutParams);

            final int currentTabIndex = tabIndex;
            tabView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    activateTab(currentTabIndex);
                }
            });
            ++tabIndex;

            anchorView = tabView;
        }
    }

    public void expandTabs() {
        if (mIsExpanded) {
            // Already expanded.
            return;
        }
        mIsExpanded = true;

        if (null != mVisualListener) {
            mVisualListener.onTabsAppearing();
        }
        int delay = 0;
        for (int i = 0; i < mRegularTabs.size(); ++i) {
            final View tabView = mRegularTabs.get(i);
            final boolean isLastTab = i == mRegularTabs.size() - 1;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.appear_grow);
                    tabView.startAnimation(
                            animation
                    );
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            tabView.setVisibility(VISIBLE);

                            if (isLastTab && null != mVisualListener) {
                                mVisualListener.onTabsAppeared();
                            }
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }, delay);

            delay += 175;
        }
    }

    public void collapseTabs() {
        if (!mIsExpanded) {
            // Already collapsed.
            return;
        }
        mIsExpanded = false;

        if (null != mVisualListener) {
            mVisualListener.onTabsDisappearing();
        }
        int delay = 0;
        for (int i = mRegularTabs.size() - 1; i >= 0; --i) {
            final View tabView = mRegularTabs.get(i);
            final boolean isLastTab = i == 0;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.disappear_shrink);
                    tabView.startAnimation(
                            animation
                    );
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            tabView.setVisibility(INVISIBLE);

                            if (isLastTab && null != mVisualListener) {
                                mVisualListener.onTabsDisappeared();
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }, delay);

            delay += 175;
        }
    }

    public void setInteractionListener(@Nullable InteractionListener listener) {
        mInteractionListener = listener;
    }

    public void setVisualListener(@Nullable VisualListener listener) {
        mVisualListener = listener;
    }

    private void activateTab(int index) {
        if (null != mInteractionListener) {
            mInteractionListener.onTabClicked(index);
        }

        if (null != mVisualListener) {
//            Point tabCenter;
            if (index == 0) {
//                tabCenter = getCenterLocationOfView(mPrimaryTabHolder);
                mVisualListener.onActiveTabChange(mPrimaryTabHolder);
            } else {
//                tabCenter = getCenterLocationOfView(mRegularTabs.get(index - 1));
                mVisualListener.onActiveTabChange(mRegularTabs.get(index - 1));
            }
//            mVisualListener.onActiveTabLocationChange(tabCenter);
        }
    }

    private Point getCenterLocationOfView(@NonNull View view) {
        return new Point(
                (int) (view.getX() + (view.getWidth() / 2)),
                (int) (view.getY() + (view.getHeight() / 2))
        );
    }

    private void reportActiveTabPosition() {
        if (null != mVisualListener) {
            // TODO: need to account for floating tab that isn't in regular tabs.
            View activeTabView = mRegularTabs.get(mActiveTabIndex);
            Point tabCenterPosition = new Point(
                    (int) (activeTabView.getX() + (activeTabView.getWidth() / 2)),
                    (int) (activeTabView.getY() + (activeTabView.getHeight() / 2))
            );

//            mVisualListener.onActiveTabLocationChange(tabCenterPosition);
            mVisualListener.onActiveTabChange(activeTabView);
        }
    }

    public interface InteractionListener {
        void onTabClicked(int id);
    }

    public interface VisualListener {
//        void onActiveTabLocationChange(@NonNull Point tabCenterPosition);
        void onActiveTabChange(@NonNull View activeTabView);

        void onTabsAppearing();

        void onTabsAppeared();

        void onTabsDisappearing();

        void onTabsDisappeared();
    }
}
