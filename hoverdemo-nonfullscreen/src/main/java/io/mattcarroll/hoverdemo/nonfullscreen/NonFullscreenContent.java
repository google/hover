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
package io.mattcarroll.hoverdemo.nonfullscreen;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.mattcarroll.hover.Content;

/**
 * A Hover menu screen that does not take up the entire screen.
 */
public class NonFullscreenContent implements Content {

    private final Context mContext;
    private View mContent;

    public NonFullscreenContent(@NonNull Context context) {
        mContext= context.getApplicationContext();
    }

    @NonNull
    @Override
    public View getView() {
        if (null == mContent) {
            mContent = LayoutInflater.from(mContext).inflate(R.layout.content_non_fullscreen, null);

            // We present our desire to be non-fullscreen by using WRAP_CONTENT for height.  This
            // preference will be honored by the Hover Menu to make our content only as tall as we
            // want to be.
            mContent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        return mContent;
    }

    @Override
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public void onShown() {
        // No-op.
    }

    @Override
    public void onHidden() {
        // No-op.
    }
}
