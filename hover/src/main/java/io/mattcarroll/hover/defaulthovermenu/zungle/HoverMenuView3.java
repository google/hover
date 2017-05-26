package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.defaulthovermenu.Dragger;

/**
 * TODO
 */
class HoverMenuView3 extends RelativeLayout {

    private static final String TAG = "HoverMenuView3";

    private final Dragger mDragger;
    private Screen mScreen;
    private HoverMenuViewStateCollapsed mCollapsedMenu;
    private Point mCollapsedDock;
    private HoverMenuViewStateExpanded mExpandedMenu;
    private HoverMenuAdapter mAdapter;
    private boolean mIsInitialized;
    private boolean mIsExpanded = false;
    private ExitListener mExitListener;
    private boolean mIsDebugMode = false;

    public HoverMenuView3(Context context, @NonNull Dragger dragger) {
        super(context);
        mDragger = dragger;
        mScreen = new Screen(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                removeOnLayoutChangeListener(this);
                initAfterInitialLayout();
            }
        });
    }

    private void initAfterInitialLayout() {
        Log.d(TAG, "initAfterInitialLayout() " + hashCode());
        mIsInitialized = true;

        if (null != mAdapter) {
            applyAdapter();
        }

        createCollapsedMenu();
        mCollapsedMenu.takeControl(mScreen);
    }

    public void enableDebugMode(boolean debugMode) {
        mIsDebugMode = debugMode;

        mDragger.enableDebugMode(debugMode);
        mScreen.enableDrugMode(debugMode);
    }

    public void setExitListener(@Nullable ExitListener listener) {
        mExitListener = listener;
    }

    public void setAdapter(@Nullable HoverMenuAdapter adapter) {
        mAdapter = adapter;
        if (mIsInitialized) {
            applyAdapter();
        }
    }

    private void applyAdapter() {
        View floatingTabView = mAdapter.getTabView(0);
        mScreen.createChainedTab("PRIMARY", floatingTabView);
    }

    private void expand() {
        if (!mIsExpanded) {
            mIsExpanded = true;

            createExpandedMenu();

            mCollapsedDock = mCollapsedMenu.getDock();
            mCollapsedMenu.giveControlTo(mExpandedMenu);
            mCollapsedMenu = null;
        }
    }

    private void createExpandedMenu() {
        mExpandedMenu = new HoverMenuViewStateExpanded();
        mExpandedMenu.setListener(new HoverMenuViewStateExpanded.Listener() {
            @Override
            public void onExpanding() { }

            @Override
            public void onExpanded() {
                mScreen.getContentDisplay().setVisibility(VISIBLE);
            }

            @Override
            public void onCollapseRequested() {
                collapse();
            }
        });
        mExpandedMenu.setMenu(new HoverMenu(mAdapter));
    }

    private void collapse() {
        if (mIsExpanded) {
            mIsExpanded = false;

            createCollapsedMenu();

            mExpandedMenu.giveControlTo(mCollapsedMenu);
            mExpandedMenu = null;

            mScreen.getContentDisplay().setVisibility(GONE);
        }
    }

    private void createCollapsedMenu() {
        mCollapsedMenu = new HoverMenuViewStateCollapsed(mDragger, mCollapsedDock);
        mCollapsedMenu.setListener(new HoverMenuViewStateCollapsed.Listener() {
            @Override
            public void onDragStart() {
                mScreen.getExitView().setVisibility(VISIBLE);
            }

            @Override
            public void onDragEnd() {
                mScreen.getExitView().setVisibility(GONE);
            }

            @Override
            public void onDocked() { }

            @Override
            public void onTap() {
                expand();
            }

            @Override
            public void onDroppedOnExit() {
                Log.d(TAG, "Floating tab dropped on exit.");
                if (null != mExitListener) {
                    mExitListener.onExit();
                }
            }
        });
    }

}
