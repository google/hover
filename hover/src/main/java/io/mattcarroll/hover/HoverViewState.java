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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A state of a {@link HoverView}. {@code HoverView} is implemented with a state pattern and this
 * is the interface that is implemented by all such states.
 */
public interface HoverViewState {
    /**
     * Activates this state.
     * @param hoverView hoverView
     * @param onStateChanged Runnable to be run after state has changed
     */
    void takeControl(@NonNull HoverView hoverView, Runnable onStateChanged);

    void giveUpControl(@NonNull HoverViewState nextState);

    /**
     * Displays the given {@code menu} within the HoverView.
     * @param menu menu
     */
    void setMenu(@Nullable HoverMenu menu);

    /**
     * Does this state respond to physical back button presses?
     * @return true if this state responds to physical back button presses, false otherwise
     */
    boolean respondsToBackButton();

    /**
     * Hook called when the hardware back button is pressed.  This method is only invoked if
     * {@link #respondsToBackButton()} returns true.
     */
    void onBackPressed();

    HoverViewStateType getStateType();
}
