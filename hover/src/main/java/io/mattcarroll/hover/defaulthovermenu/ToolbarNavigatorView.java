package io.mattcarroll.hover.defaulthovermenu;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import io.mattcarroll.hover.R;
import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;

import java.util.Stack;

/**
 * {@link Navigator} implementation  that displays content with a {@link Toolbar} on top that allows
 * for back navigation and displays a title.
 */
public class ToolbarNavigatorView extends LinearLayout implements Navigator {

    private Toolbar mToolbar;
    private Stack<NavigatorContent> mContentStack;
    private FrameLayout mContentContainer;
    private LayoutParams mContentLayoutParams;

    public ToolbarNavigatorView(Context context) {
        this(context, null);
    }

    public ToolbarNavigatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.view_toolbar_navigator, this, true);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popContent();
            }
        });

        mContentContainer = (FrameLayout) findViewById(R.id.content_container);

        mContentLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mContentStack = new Stack<>();
    }

    @Override
    public void setTitle(@NonNull String title) {
        mToolbar.setTitle(title);
    }

    @Override
    public void pushContent(@NonNull NavigatorContent content) {
        // Remove the currently visible content (if there is any).
        if (!mContentStack.isEmpty()) {
            mContentContainer.removeView(mContentStack.peek().getView());
            mContentStack.peek().onHidden();
        }

        // Push and display the new page.
        mContentStack.push(content);
        mContentContainer.addView(content.getView(), mContentLayoutParams);
        content.onShown(this);

        updateToolbarBackButton();
    }

    @Override
    public boolean popContent() {
        if (mContentStack.size() > 1) {
            // Remove the currently visible content.
            removeCurrentContent();

            // Add back the previous content (if there is any).
            if (!mContentStack.isEmpty()) {
                mContentContainer.addView(mContentStack.peek().getView(), mContentLayoutParams);
                mContentStack.peek().onShown(this);
            }

            updateToolbarBackButton();

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

    private void removeCurrentContent() {
        NavigatorContent visibleContent = mContentStack.pop();
        mContentContainer.removeView(visibleContent.getView());
        visibleContent.onHidden();
    }

    private void updateToolbarBackButton() {
        if (mContentStack.size() >= 2) {
            // Show the back button.
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        } else {
            // Hide the back button.
            mToolbar.setNavigationIcon(null);
        }
    }

}
