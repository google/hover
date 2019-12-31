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
package io.mattcarroll.hover.content;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Stack;

/**
 * A visual display that can push and pop {@link NavigatorContent} in a content area. The size and
 * location of the content area is chosen by implementing classes.
 *
 * A {@code Navigator} also displays a title that can be set by a client.
 *
 * The content to display in a {@code Navigator} must be provided as a {@link NavigatorContent}. Each
 * pushed {@code NavigatorContent} is retained in a navigation stack until a corresponding
 * {@link #popContent()} is called.  Therefore, {@code NavigatorContent}s must retain their {@code View}
 * and state until garbage collected.
 */
public class Navigator extends FrameLayout {

    private final boolean mIsFullscreen;
    private Stack<NavigatorContent> mContentStack;
    private ViewGroup.LayoutParams mContentLayoutParams;

    public Navigator(@NonNull Context context) {
        this(context, true);
    }

    public Navigator(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
        mIsFullscreen = false;
        init();
    }

    public Navigator(@NonNull Context context, boolean isFullscreen) {
        super(context);
        mIsFullscreen = isFullscreen;
        init();
    }

    private void init() {
        int heightMode = mIsFullscreen ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;
        mContentStack = new Stack<>();
        mContentLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightMode);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightMode));
    }

    /**
     * Removes the current content {@code View} if content is visible. Then displays the provided
     * {@code content}.
     *
     * The given {@code content} is retained in a navigation stack so that this {@code Navigator}
     * can navigate to other content and then later return to this content.
     *
     * To remove the given {@code content}, make a corresponding call to {@link #popContent()}.
     *
     * @param content Content to display
     */
    public void pushContent(@NonNull NavigatorContent content) {
        // Remove the currently visible content (if there is any).
        if (!mContentStack.isEmpty()) {
            removeView(mContentStack.peek().getView());
            mContentStack.peek().onHidden();
        }

        // Push and display the new page.
        mContentStack.push(content);
        showContent(content);
    }

    /**
     * Removes the current content {@code View} and restores the previous content {@code View}. If
     * there is no previous content then this {@code Navigator} returns to its base visual state
     * without any content.
     *
     * @return true if there was content to remove, false if there was no content to remove
     */
    public boolean popContent() {
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

    /**
     * Pops all content {@code View}s and returns this {@code Navigator} to its base visual state
     * without any content.
     */
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
        addView(content.getView(), mContentLayoutParams);
        content.onShown(this);
    }

    private void removeCurrentContent() {
        NavigatorContent visibleContent = mContentStack.pop();
        removeView(visibleContent.getView());
        visibleContent.onHidden();
    }
}
