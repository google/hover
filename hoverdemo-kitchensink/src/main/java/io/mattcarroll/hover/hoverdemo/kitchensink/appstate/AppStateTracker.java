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
package io.mattcarroll.hover.hoverdemo.kitchensink.appstate;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.*;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Tracks Activity and Service state and makes that information available to clients.
 */
public class AppStateTracker {

    private static final String TAG = "AppStateTracker";

    private static AppStateTracker sInstance;

    public static synchronized void init(@NonNull Application application, @NonNull EventBus bus) {
        sInstance = new AppStateTracker(application, bus);
    }

    public static AppStateTracker getInstance() {
        if (null == sInstance) {
            throw new RuntimeException("Did you forget to call init()? You must initialize AppStateTracker before obtaining its instance.");
        }

        return sInstance;
    }

    private final Application mApplication;
    private final EventBus mBus;
    private ActivityManager.RecentTaskInfo mTaskToTrack;
    private List<ActivityState> mActivityStates = new ArrayList<>();
    private List<ServiceState> mServiceStates = new ArrayList<>();
    private final Application.ActivityLifecycleCallbacks mActivityLifecycleCallback = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.d(TAG, "onActivityCreated: " + activity.getLocalClassName());
            if (activity.getTaskId() == mTaskToTrack.id) {
                if (null == savedInstanceState) {
                    // This must be a new Activity. Add it.
                    mActivityStates.add(new ActivityState(activity.getLocalClassName(), ActivityState.State.CREATED));
                } else {
                    int correspondingBackstackIndex = findFirstInBackStack(activity.getLocalClassName(), ActivityState.State.DESTROYED);
                    if (correspondingBackstackIndex >= 0) {
                        // There is a corresponding Activity in the backstack, update it to CREATED.
                        updateActivityState(correspondingBackstackIndex, ActivityState.State.CREATED);
                    }
                }

                onActivityStackChanged();
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.d(TAG, "onActivityStarted: " + activity.getLocalClassName());
            if (activity.getTaskId() == mTaskToTrack.id) {
                // Note: When using the Android setting to forcibly kill Activitys, they go straight from destroyed to started.
                int correspondingBackstackIndex = findFirstInBackStack(activity.getLocalClassName(), ActivityState.State.CREATED, ActivityState.State.STOPPED, ActivityState.State.DESTROYED);
                if (correspondingBackstackIndex >= 0) {
                    // There is a corresponding Activity in the backstack, update it to STARTED.
                    updateActivityState(correspondingBackstackIndex, ActivityState.State.STARTED);
                }

                onActivityStackChanged();
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            Log.d(TAG, "onActivityResumed: " + activity.getLocalClassName());
            if (activity.getTaskId() == mTaskToTrack.id) {
                int correspondingBackstackIndex = findFirstInBackStack(activity.getLocalClassName(), ActivityState.State.STARTED, ActivityState.State.PAUSED);
                if (correspondingBackstackIndex >= 0) {
                    // There is a corresponding Activity in the backstack, update it to RESUMED.
                    updateActivityState(correspondingBackstackIndex, ActivityState.State.RESUMED);
                }

                onActivityStackChanged();
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Log.d(TAG, "onActivityPaused: " + activity.getLocalClassName());
            if (activity.getTaskId() == mTaskToTrack.id) {
                int correspondingBackstackIndex = findLastInBackStack(activity.getLocalClassName(), ActivityState.State.RESUMED);
                if (correspondingBackstackIndex >= 0) {
                    // There is a corresponding Activity in the backstack, update it to PAUSED.
                    updateActivityState(correspondingBackstackIndex, ActivityState.State.PAUSED);
                }

                onActivityStackChanged();
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.d(TAG, "onActivityStopped: " + activity.getLocalClassName());
            if (activity.getTaskId() == mTaskToTrack.id) {
                int correspondingBackstackIndex = findLastInBackStack(activity.getLocalClassName(), ActivityState.State.PAUSED, ActivityState.State.STARTED);
                if (correspondingBackstackIndex >= 0) {
                    // There is a corresponding Activity in the backstack, update it to STOPPED.
                    updateActivityState(correspondingBackstackIndex, ActivityState.State.STOPPED);
                }

                onActivityStackChanged();
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.d(TAG, "onActivityDestroyed: " + activity.getLocalClassName());
            if (activity.getTaskId() == mTaskToTrack.id) {
                int correspondingBackstackIndex = findLastInBackStack(activity.getLocalClassName(), ActivityState.State.STOPPED);
                if (correspondingBackstackIndex >= 0) {
                    if (!activity.isFinishing()) {
                        // There is a corresponding Activity in the backstack, update it to DESTROYED.
                        updateActivityState(correspondingBackstackIndex, ActivityState.State.DESTROYED);
                    } else {
                        mActivityStates.remove(correspondingBackstackIndex);
                    }
                }

                onActivityStackChanged();
            }
        }
    };

    public AppStateTracker(@NonNull Application application, @NonNull EventBus bus) {
        mApplication = application;
        mBus = bus;
    }

    public void trackTask(@NonNull ActivityManager.RecentTaskInfo taskToTrack) {
        mTaskToTrack = taskToTrack;
        Log.d(TAG, "Task ID: " + taskToTrack.id);
        Log.d(TAG, "Original activity: " + taskToTrack.origActivity);

        if (null != mTaskToTrack.origActivity) {
            mActivityStates.add(new ActivityState(mTaskToTrack.origActivity.getClassName(), ActivityState.State.CREATED));
        }

        mApplication.registerActivityLifecycleCallbacks(mActivityLifecycleCallback);
    }

    public List<ActivityState> getActivityStates() {
        return mActivityStates;
    }

    public List<ServiceState> getServiceStates() {
        ActivityManager activityManager = (ActivityManager) mApplication.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(100);
        List<ServiceState> serviceStates = new ArrayList<>(runningServices.size());
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            if (runningServiceInfo.pid == android.os.Process.myPid()) {
                serviceStates.add(new ServiceState(runningServiceInfo.service.getShortClassName()));
            }
        }
        return serviceStates;
    }

    private int findFirstInBackStack(@NonNull String activityName, @NonNull ActivityState.State... statesToFind) {
        ActivityState activityState;
        for (int i = mActivityStates.size() - 1; i >= 0; --i) {
            activityState = mActivityStates.get(i);
            if (activityState.getActivityName().equals(activityName)) {
                for (int j = 0; j < statesToFind.length; ++j) {
                    if (statesToFind[j] == activityState.getState()) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private int findLastInBackStack(@NonNull String activityName, @NonNull ActivityState.State... statesToFind) {
        ActivityState activityState;
        for (int i = 0; i < mActivityStates.size(); ++i) {
            activityState = mActivityStates.get(i);
            if (activityState.getActivityName().equals(activityName)) {
                for (int j = 0; j < statesToFind.length; ++j) {
                    if (statesToFind[j] == activityState.getState()) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private void updateActivityState(int index, ActivityState.State newState) {
        ActivityState activityState = mActivityStates.remove(index);
        mActivityStates.add(index, new ActivityState(activityState.getActivityName(), newState));
    }

    private void onActivityStackChanged() {
        logStack();
        mBus.postSticky(new ActivityStackChangeEvent(mActivityStates));
    }

    private void logStack() {
        Log.d(TAG, "Stack:");
        ActivityState activityState;
        for (int i = mActivityStates.size() - 1; i >= 0; --i) {
            activityState = mActivityStates.get(i);
            Log.d(TAG, " " + i + " - " + activityState.getActivityName() + ": " + activityState.getState());
        }
        Log.d(TAG, " ");
    }

    public static class ActivityState {

        public enum State {
            CREATED,
            STARTED,
            RESUMED,
            PAUSED,
            STOPPED,
            DESTROYED
        }

        private String mActivityName;
        private State mState;

        private ActivityState(@NonNull String name, @NonNull State state) {
            mActivityName = name;
            mState = state;
        }

        public String getActivityName() {
            return mActivityName;
        }

        public State getState() {
            return mState;
        }

    }

    public static class ServiceState {

        private String mServiceName;

        public ServiceState(@NonNull String serviceName) {
            mServiceName = serviceName;
        }

        public String getServiceName() {
            return mServiceName;
        }

    }

    public static class ActivityStackChangeEvent {

        private List<ActivityState> mBackstack;

        public ActivityStackChangeEvent(List<ActivityState> backstack) {
            mBackstack = backstack;
        }

        public List<ActivityState> getActivityStack() {
            return mBackstack;
        }
    }

}
