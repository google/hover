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
package io.mattcarroll.hover;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * {@link HoverViewState} that includes behavior common to all implementations.
 */
abstract class BaseHoverViewState implements HoverViewState {

    private boolean mHasControl = false;
    protected HoverView mHoverView;

    @CallSuper
    @Override
    public void takeControl(@NonNull HoverView hoverView, Runnable onStateChanged) {
        if (mHasControl) {
            throw new RuntimeException("Cannot take control of a FloatingTab when we already control one.");
        }
        mHasControl = true;
        mHoverView = hoverView;
    }

    @CallSuper
    @Override
    public void giveUpControl(@NonNull HoverViewState nextState) {
        if (!mHasControl) {
            throw new RuntimeException("Cannot give up control of a FloatingTab when we don't have the control");
        }
        mHasControl = false;
        mHoverView = null;
    }

    protected final boolean hasControl() {
        return mHasControl;
    }

    @Override
    public void setMenu(@Nullable HoverMenu menu) {
    }
}
