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
package io.mattcarroll.hover.window;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import io.mattcarroll.hover.HoverView;
import io.mattcarroll.hover.OnExitListener;
import io.mattcarroll.hover.SideDock;
import io.mattcarroll.hover.overlay.OverlayPermission;

/**
 * {@code Service} that presents a {@link HoverView} within a {@code Window}.
 *
 * The {@code HoverView} is displayed whenever any Intent is received by this {@code Service}. The
 * {@code HoverView} is removed and destroyed whenever this {@code Service} is destroyed.
 *
 * A {@link Service} is required for displaying a {@code HoverView} in a {@code Window} because there
 * is no {@code Activity} to associate with the {@code HoverView}'s UI. This {@code Service} is the
 * application's link to the device's {@code Window} to display the {@code HoverView}.
 */
public abstract class HoverMenuService extends Service {

    private static final String TAG = "HoverMenuService";

    private HoverView mHoverView;
    private boolean mIsRunning;
    private OnExitListener mOnMenuOnExitListener = new OnExitListener() {
        @Override
        public void onExit() {
            Log.d(TAG, "Menu exit requested. Exiting.");
            mHoverView.removeFromWindow();
            onHoverMenuExitingByUserRequest();
            stopSelf();
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        Notification foregroundNotification = getForegroundNotification();
        if (null != foregroundNotification) {
            int notificationId = getForegroundNotificationId();
            startForeground(notificationId, foregroundNotification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Stop and return immediately if we don't have permission to display things above other
        // apps.
        if (!OverlayPermission.hasRuntimePermissionToDrawOverlay(getApplicationContext())) {
            Log.e(TAG, "Cannot display a Hover menu in a Window without the draw overlay permission.");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (null == intent) {
            Log.e(TAG, "Received null Intent. Not creating Hover menu.");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!mIsRunning) {
            Log.d(TAG, "onStartCommand() - showing Hover menu.");
            mIsRunning = true;
            initHoverMenu(intent);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        if (mIsRunning) {
            mHoverView.removeFromWindow();
            mIsRunning = false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initHoverMenu(@NonNull Intent intent) {
        mHoverView = HoverView.createForWindow(
                this,
                new WindowViewController((WindowManager) getSystemService(Context.WINDOW_SERVICE)),
                new SideDock.SidePosition(SideDock.SidePosition.RIGHT, 0.5f)
        );
        mHoverView.setOnExitListener(mOnMenuOnExitListener);
        mHoverView.addToWindow();

        onHoverMenuLaunched(intent, mHoverView);
    }

    /**
     * Hook for subclasses to return a custom Context to be used in the creation of the {@code HoverMenu}.
     * For example, subclasses might choose to provide a ContextThemeWrapper.
     *
     * @return context for HoverMenu initialization
     */
    protected Context getContextForHoverMenu() {
        return this;
    }

    @NonNull
    protected HoverView getHoverView() {
        return mHoverView;
    }

    protected int getForegroundNotificationId() {
        // Subclasses should provide their own notification ID if using a notification.
        return 123456789;
    }

    @Nullable
    protected Notification getForegroundNotification() {
        // If subclass returns a non-null Notification then the Service will be run in
        // the foreground.
        return null;
    }

    protected void onHoverMenuLaunched(@NonNull Intent intent, @NonNull HoverView hoverView) {
        // Hook for subclasses.
    }

    /**
     * Hook method for subclasses to take action when the user exits the HoverMenu. This method runs
     * just before this {@code HoverMenuService} calls {@code stopSelf()}.
     */
    protected void onHoverMenuExitingByUserRequest() {
        // Hook for subclasses.
    }
}
