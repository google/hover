package io.mattcarroll.hover.defaulthovermenu;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.Stack;

import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;

/**
 * Implementation of a {@link Navigator} without any decoration or special features.
 */
public class DefaultNavigator extends FrameLayout implements Navigator {

    private Stack<NavigatorContent> mContentStack;
    private ViewGroup.LayoutParams mContentLayoutParams;

    public DefaultNavigator(Context context) {
        super(context);
        init();
    }

    private void init() {
        mContentStack = new Stack<>();
        mContentLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void setTitle(@NonNull String title) {
//        mContentContainer.setTitle(title);
        // TODO: get rid of setTitle() method in interface.
    }

    @Override
    public void pushContent(@NonNull NavigatorContent content) {
//        mContentContainer.pushContent(content);

        // Remove the currently visible content (if there is any).
        if (!mContentStack.isEmpty()) {
            removeView(mContentStack.peek().getView());
            mContentStack.peek().onHidden();
        }

        // Push and display the new page.
        mContentStack.push(content);
        showContent(content);
    }

    @Override
    public boolean popContent() {
//        return mContentContainer.popContent();

        if (mContentStack.size() > 1) {
            // Remove the currently visible content.
            removeCurrentContent();

            // Add back the previous content (if there is any).
            if (!mContentStack.isEmpty()) {
                showContent(mContentStack.peek());
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clearContent() {
        if (mContentStack.isEmpty()) {
            // Nothing to clear.
            return;
        }

        // Pop every content View that we can.
        boolean didPopContent = popContent();
        while (didPopContent) {
            didPopContent = popContent();
        }

        // Clear the root View.
        removeCurrentContent();
    }

    @Override
    public View getView() {
        return this;
    }

    private void showContent(@NonNull NavigatorContent content) {
        addView(content.getView(), mContentLayoutParams);
        content.onShown(this);
    }

    private void removeCurrentContent() {
        NavigatorContent visibleContent = mContentStack.pop();
        removeView(visibleContent.getView());
        visibleContent.onHidden();
    }
}
