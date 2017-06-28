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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A state of a {@link HoverView}. {@code HoverView} is implemented with a state pattern and this
 * is the interface that is implemented by all such states.
 */
interface HoverViewState {
    /**
     * Activates this state.
     * @param hoverView hoverView
     */
    void takeControl(@NonNull HoverView hoverView);

    /**
     * Expands the HoverView.
     */
    void expand();

    /**
     * Collapses the HoverView.
     */
    void collapse();

    /**
     * Closes the HoverView (no menu or tabs are visible).
     */
    void close();

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

    /**
     * Adds the HoverView to the Android device's Window.
     */
    void addToWindow();

    /**
     * Removes the HoverView from the Android device's Window.
     */
    void removeFromWindow();

    /**
     * Assuming that the HoverView is added to the Android device's Window, makes the HoverView
     * touchable.
     */
    void makeTouchableInWindow();

    /**
     * Assuming that the HoverView is added to the Android device's Window, makes the HoverView
     * untouchable (touch events pass through the overlay to whatever is beneath).
     */
    void makeUntouchableInWindow();
}
