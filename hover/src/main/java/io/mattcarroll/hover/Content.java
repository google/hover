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
import android.view.View;

/**
 * Content to be displayed in a {@link HoverView}.
 */
public interface Content {

    /**
     * Returns the visual display of this content.
     *
     * @return the visual representation of this content
     */
    @NonNull
    View getView();

    /**
     * @return true to fill all available space, false to wrap content height
     */
    boolean isFullscreen();

    /**
     * Called when this content is displayed to the user.
     */
    void onShown();

    /**
     * Called when this content is no longer displayed to the user.
     *
     * Implementation Note: {@code Content} can be brought back due to user navigation so
     * this call must not release resources that are required to show this content again.
     */
    void onHidden();

}
