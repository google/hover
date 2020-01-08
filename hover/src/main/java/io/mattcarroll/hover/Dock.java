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

import android.graphics.Point;
import androidx.annotation.NonNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A location on the screen that resolves to a {@link Point} when requested.  A {@code Dock} can
 * change its location.  {@link Listener}s can be added to receive updates when a {@code Dock}
 * changes its location.
 */
abstract class Dock {

    private final Set<Listener> mListeners = new CopyOnWriteArraySet<Listener>();

    @NonNull
    public abstract Point position();

    public void addListener(@NonNull Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(@NonNull Listener listener) {
        mListeners.remove(listener);
    }

    public void clearListeners() {
        mListeners.clear();
    }

    protected void notifyListeners() {
        for (Listener listener : mListeners) {
            listener.onDockChange(this);
        }
    }

    public interface Listener {
        void onDockChange(@NonNull Dock dock);
    }

}
