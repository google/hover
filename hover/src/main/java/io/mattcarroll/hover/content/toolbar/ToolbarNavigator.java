/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mattcarroll.hover.content.toolbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.Stack;

import io.mattcarroll.hover.Content;
import io.mattcarroll.hover.R;
import io.mattcarroll.hover.content.Navigator;
import io.mattcarroll.hover.content.NavigatorContent;

/**
 * A {@link Navigator} that offers a {@code Toolbar}.
 */
public class ToolbarNavigator extends Navigator implements Content {

    private Toolbar mToolbar;
    private Drawable mBackArrowDrawable;
    private Stack<NavigatorContent> mContentStack; // TODO: if we extend Navigator then we don't need to implement our own stack
    private FrameLayout mContentContainer;
    private LinearLayout.LayoutParams mContentLayoutParams;

    public ToolbarNavigator(Context context) {
        this(context, null);
    }

    public ToolbarNavigator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_toolbar_navigator, this, true);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popContent();
            }
        });
        mBackArrowDrawable = createBackArrowDrawable();

        mContentContainer = (FrameLayout) findViewById(R.id.content_container);
        mContentLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContentStack = new Stack<>();
    }

    private Drawable createBackArrowDrawable() {
        // Load the desired back-arrow color from the theme that we're using.
        int[] attrIds = new int[] { R.attr.colorControlNormal };
        TypedArray attrs = getContext().obtainStyledAttributes(attrIds);
        int backArrowColor = attrs.getColor(attrs.getIndex(0), 0xFF000000);
        attrs.recycle();

        // Apply the desired color to the back-arrow icon and return it.
        Drawable backArrowDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_back);
        backArrowDrawable.setColorFilter(backArrowColor, PorterDuff.Mode.SRC_ATOP);
        return backArrowDrawable;
    }

    public Toolbar getToolbar() {
        return mToolbar;
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
        showContent(content);

        updateToolbarBackButton();
    }

    @Override
    public boolean popContent() {
        if (mContentStack.size() > 1) {
            // Remove the currently visible content.
            removeCurrentContent();

            // Add back the previous content (if there is any).
            if (!mContentStack.isEmpty()) {
                showContent(mContentStack.peek());
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

    private void showContent(@NonNull NavigatorContent content) {
        mContentContainer.addView(content.getView(), mContentLayoutParams);
        content.onShown(this);
    }

    private void removeCurrentContent() {
        NavigatorContent visibleContent = mContentStack.pop();
        mContentContainer.removeView(visibleContent.getView());
        visibleContent.onHidden();
    }

    private void updateToolbarBackButton() {
        if (mContentStack.size() >= 2) {
            // Show the back button.
            mToolbar.setNavigationIcon(mBackArrowDrawable);
        } else {
            // Hide the back button.
            mToolbar.setNavigationIcon(null);
        }
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public boolean isFullscreen() {
        return true;
    }

    @Override
    public void onShown() {
        // Do nothing.
    }

    @Override
    public void onHidden() {
        // Do nothing.
    }

}
