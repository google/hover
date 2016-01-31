package io.mattcarroll.hover.defaulthovermenu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
public class HoverMenuContentView extends FrameLayout {

    private static final String TAG = "HoverMenuContentView";

    private HoverMenuTabSelectorView mTabSelectorView;
    private View mSelectedTabView;
    private FrameLayout mContentView;
    private Drawable mContentBackground;
    private Navigator mNavigator;
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

        mContentView = (FrameLayout) findViewById(R.id.view_content_container);
        mContentBackground = ContextCompat.getDrawable(getContext(), R.drawable.round_rect_white);
        mContentView.setBackground(mContentBackground);
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

    public void setNavigator(@Nullable Navigator navigator) {
        if (null != mNavigator) {
            mContentView.removeView(mNavigator.getView());
        }

        if (null != navigator) {
            mNavigator = navigator;
            mContentView.addView(navigator.getView());
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        // Forward the call on to our constituent pieces.
        mTabSelectorView.setSelectorColor(color);
        mContentBackground.setTint(color);
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        // Don't allow setting a background.
    }

    @Override
    public void setBackgroundResource(int resid) {
        // Don't allow setting a background.
    }

    @Override
    public void setBackground(Drawable background) {
        // Don't allow setting a background.
    }

    //    @Override
//    public void setTitle(@NonNull String title) {
////        mContentContainer.setTitle(title);
//        // TODO: get rid of setTitle() method in interface.
//    }
//
//    @Override
//    public void pushContent(@NonNull NavigatorContent content) {
////        mContentContainer.pushContent(content);
//
//        // Remove the currently visible content (if there is any).
//        if (!mContentStack.isEmpty()) {
//            mContentContainer.removeView(mContentStack.peek().getView());
//            mContentStack.peek().onHidden();
//        }
//
//        // Push and display the new page.
//        mContentStack.push(content);
//        showContent(content);
//    }
//
//    @Override
//    public boolean popContent() {
////        return mContentContainer.popContent();
//
//        if (mContentStack.size() > 1) {
//            // Remove the currently visible content.
//            removeCurrentContent();
//
//            // Add back the previous content (if there is any).
//            if (!mContentStack.isEmpty()) {
//                showContent(mContentStack.peek());
//            }
//
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public void clearContent() {
//        if (mContentStack.isEmpty()) {
//            // Nothing to clear.
//            return;
//        }
//
//        // Pop every content View that we can.
//        boolean didPopContent = popContent();
//        while (didPopContent) {
//            didPopContent = popContent();
//        }
//
//        // Clear the root View.
//        removeCurrentContent();
//    }
//
//    private void showContent(@NonNull NavigatorContent content) {
//        mContentContainer.addView(content.getView(), mContentLayoutParams);
//        content.onShown(this);
//    }
//
//    private void removeCurrentContent() {
//        NavigatorContent visibleContent = mContentStack.pop();
//        mContentContainer.removeView(visibleContent.getView());
//        visibleContent.onHidden();
//    }

    /**
     * Tries to handle a back-press.
     * @return true if the back-press was handled, false otherwise
     */
    public boolean onBackPressed() {
//        return mContentContainer.popContent();
        if (null != mNavigator) {
            return mNavigator.popContent();
        } else {
            return false;
        }
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
