package io.mattcarroll.hover.defaulthovermenu;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.R;

/**
 * HoverMenu content area that shows a tab selector above the content area, and a {@code Toolbar} at the
 * top of the content area.  The content area itself can display anything provided by a given
 * {@link NavigatorContent}.
 */
public class HoverMenuContentView extends FrameLayout implements Navigator {

    private static final String TAG = "HoverMenuContentView";

    private HoverMenuTabSelectorView mTabSelectorView;
    private ToolbarNavigatorView mContentContainer;
    private View mSelectedTabView;
    // We need to update the tab selector position every draw frame so that animations don't result in a bad selector position.
    private ViewTreeObserver.OnDrawListener mOnDrawListener = new ViewTreeObserver.OnDrawListener() {
        @Override
        public void onDraw() {
            updateTabSelectorPosition();
        }
    };

    public HoverMenuContentView(Context context) {
        super(context);
        init();
    }

    public HoverMenuContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_hover_menu_content, this, true);

        int backgroundCornerRadiusPx = (int) getResources().getDimension(R.dimen.popup_corner_radius);
        mTabSelectorView = (HoverMenuTabSelectorView) findViewById(R.id.tabselector);
        mTabSelectorView.setPadding(backgroundCornerRadiusPx, 0, backgroundCornerRadiusPx, 0);

        mContentContainer = (ToolbarNavigatorView) findViewById(R.id.view_content_container);
    }

    /**
     * Positions the selector triangle below the center of the given {@code tabView}.
     *
     * @param tabView the tab with which this content view will align its selector
     */
    public void setActiveTab(@NonNull View tabView) {
        if (null != mSelectedTabView) {
            mSelectedTabView.getViewTreeObserver().removeOnDrawListener(mOnDrawListener);
        }

        mSelectedTabView = tabView;
        mSelectedTabView.getViewTreeObserver().addOnDrawListener(mOnDrawListener);
        updateTabSelectorPosition();
    }

    @Override
    public void setTitle(@NonNull String title) {
        mContentContainer.setTitle(title);
    }

    @Override
    public void pushContent(@NonNull NavigatorContent content) {
        mContentContainer.pushContent(content);
    }

    @Override
    public boolean popContent() {
        return mContentContainer.popContent();
    }

    @Override
    public void clearContent() {
        mContentContainer.clearContent();
    }

    /**
     * Tries to handle a back-press.
     * @return true if the back-press was handled, false otherwise
     */
    public boolean onBackPressed() {
        return mContentContainer.popContent();
    }

    private void updateTabSelectorPosition() {
        if (null != mSelectedTabView) {
            Rect tabBounds = new Rect();
            mSelectedTabView.getGlobalVisibleRect(tabBounds);
            int globalTabCenter = tabBounds.centerX();
            mTabSelectorView.setSelectorPosition(globalTabCenter);
        }
    }

}
