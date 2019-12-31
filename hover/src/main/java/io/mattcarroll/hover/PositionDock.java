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

/**
 * {@link Dock} that has a static position as defined by a provided {@link Point}. A
 * {@code PositionDock} never changes its position after construction.
 */
class PositionDock extends Dock {

    private static final String TAG = "SideDock";

    private Point mPosition;

    PositionDock(@NonNull Point position) {
        mPosition = position;
    }

    @NonNull
    @Override
    public Point position() {
        return mPosition;
    }

}
