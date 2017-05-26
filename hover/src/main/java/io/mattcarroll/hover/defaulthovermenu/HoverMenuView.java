package io.mattcarroll.hover.defaulthovermenu;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.R;
import io.mattcarroll.hover.defaulthovermenu.view.InViewGroupDragger;
import io.mattcarroll.hover.defaulthovermenu.window.InWindowDragger;
import io.mattcarroll.hover.defaulthovermenu.window.WindowViewController;

/**
 * {@code HoverMenuView} is a floating menu implementation. This implementation displays tabs along
 * the top of its display, from right to left. Below the tabs, filling the remainder of the display
 * is a content region that displays the content for a selected tab.  The content region includes
 * a visual indicator showing which tab is currently selected.  Each tab's content includes a title
 * and a visual area.  The visual area can display any {@code View}.
 *
 * {@code HoverMenuView} cannot be used in XML because it requires additional parameters in its
 * constructor.
 */
public class HoverMenuView extends RelativeLayout {

    private static final String TAG = "HoverMenuView";

    @NonNull
    public static HoverMenuView createForWindow(@NonNull Context context,
                                                @NonNull WindowViewController windowViewController) {
        int touchDiameter = context.getResources().getDimensionPixelSize(R.dimen.exit_radius);
        int slop = ViewConfiguration.get(context).getScaledTouchSlop();
        InWindowDragger dragger = new InWindowDragger(
                context,
                windowViewController,
                touchDiameter,
                slop
        );

        return new HoverMenuView(context, dragger);
    }

    @NonNull
    public static HoverMenuView createForView(@NonNull Context context,
                                              @NonNull ViewGroup container) {
        int touchDiameter = context.getResources().getDimensionPixelSize(R.dimen.exit_radius);
        int slop = ViewConfiguration.get(context).getScaledTouchSlop();
        InViewGroupDragger dragger = new InViewGroupDragger(
                container,
                touchDiameter,
                slop
        );

        return new HoverMenuView(context, dragger);
    }

    private final Dragger mDragger;
    private final Screen mScreen;
    private HoverMenuViewStateCollapsed mCollapsedMenu;
    private Point mCollapsedDock;
    private HoverMenuViewStateExpanded mExpandedMenu;
    private HoverMenuAdapter mAdapter;
    private boolean mIsInitialized;
    private boolean mIsExpanded = false;
    private ExitListener mExitListener;
    private boolean mIsDebugMode = false;

    private HoverMenuView(Context context, @NonNull Dragger dragger) {
        super(context);
        mDragger = dragger;
        mScreen = new Screen(this);
    }

    public void release() {
        mDragger.deactivate();
        // TODO: should we also release the screen?
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
