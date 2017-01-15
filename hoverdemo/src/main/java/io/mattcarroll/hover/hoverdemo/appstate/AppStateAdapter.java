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
package io.mattcarroll.hover.hoverdemo.appstate;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.mattcarroll.hover.hoverdemo.R;
import io.mattcarroll.hover.hoverdemo.theming.HoverTheme;

/**
 * RecyclerView Adapter to display App Activitys and Services.
 */
public class AppStateAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ACTIVITY = 1;
    private static final int VIEW_TYPE_SERVICE = 2;

    private HoverTheme mTheme;
    private List<AppStateTracker.ActivityState> mActivityStates = new ArrayList<>();
    private List<AppStateTracker.ServiceState> mServiceStates = new ArrayList<>();

    public AppStateAdapter(@NonNull HoverTheme theme) {
        mTheme = theme;
    }

    public void setTheme(@NonNull HoverTheme theme) {
        mTheme = theme;
        notifyDataSetChanged();
    }

    public void setActivityStates(List<AppStateTracker.ActivityState> activityStates) {
        mActivityStates.clear();
        mActivityStates.addAll(activityStates);
        notifyDataSetChanged();
    }

    public void setServiceStates(List<AppStateTracker.ServiceState> serviceStates) {
        mServiceStates.clear();
        mServiceStates.addAll(serviceStates);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (0 == position) {
            return VIEW_TYPE_HEADER;
        } else if (position <= mActivityStates.size()) {
            return VIEW_TYPE_ACTIVITY;
        } else if (position == mActivityStates.size() + 1) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_SERVICE;
        }
    }

    public Object getItem(int position) {
        if (0 == position) {
            return "Activitys";
        } else if (position <= mActivityStates.size()) {
            // The Activity State list goes from deepest Activity to the highest, so process it in
            // reverse for presentation purposes.
            return mActivityStates.get(mActivityStates.size() - (position - 1) - 1);
        } else if (position == mActivityStates.size() + 1) {
            return "Services";
        } else {
            int index = position - mActivityStates.size() - 2;
            return mServiceStates.get(index);
        }
    }

    @Override
    public int getItemCount() {
        return 2 + mActivityStates.size() + mServiceStates.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView view;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_app_state_header, parent, false).findViewById(R.id.textview_header);
                return new HeaderViewHolder(view);
            case VIEW_TYPE_ACTIVITY:
                view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_activity_state, parent, false).findViewById(R.id.textview_activity_state);
                return new ActivityStateViewHolder(view);
            case VIEW_TYPE_SERVICE:
                view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_activity_state, parent, false).findViewById(R.id.textview_activity_state);
                return new ServiceStateViewHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                ((HeaderViewHolder) holder).setHeader((String) getItem(position), mTheme.getAccentColor());
                break;
            case VIEW_TYPE_ACTIVITY:
                ((ActivityStateViewHolder) holder).setActivityState((AppStateTracker.ActivityState) getItem(position));
                break;
            case VIEW_TYPE_SERVICE:
                ((ServiceStateViewHolder) holder).setServiceState((AppStateTracker.ServiceState) getItem(position));
                break;
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView mView;

        public HeaderViewHolder(TextView itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setHeader(@NonNull String header, @ColorInt int textColor) {
            mView.setText(header);
            mView.setTextColor(textColor);
        }
    }

    private static class ActivityStateViewHolder extends RecyclerView.ViewHolder {

        private TextView mView;

        public ActivityStateViewHolder(TextView itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setActivityState(@NonNull AppStateTracker.ActivityState activityState) {
            switch (activityState.getState()) {
                case CREATED:
                case STOPPED:
                    mView.setBackgroundDrawable(ContextCompat.getDrawable(mView.getContext(), R.drawable.rounded_rect_red));
                    break;
                case STARTED:
                case PAUSED:
                    mView.setBackgroundDrawable(ContextCompat.getDrawable(mView.getContext(), R.drawable.rounded_rect_yellow));
                    break;
                case RESUMED:
                    mView.setBackgroundDrawable(ContextCompat.getDrawable(mView.getContext(), R.drawable.rounded_rect_green));
                    break;
                case DESTROYED:
                    mView.setBackgroundDrawable(ContextCompat.getDrawable(mView.getContext(), R.drawable.rounded_rect_dark_gray));
                    break;
            }

            mView.setText(activityState.getActivityName());
        }
    }

    private static class ServiceStateViewHolder extends RecyclerView.ViewHolder {

        private TextView mView;

        public ServiceStateViewHolder(TextView itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setServiceState(@NonNull AppStateTracker.ServiceState serviceState) {
            mView.setBackgroundDrawable(ContextCompat.getDrawable(mView.getContext(), R.drawable.rounded_rect_green));
            mView.setText(serviceState.getServiceName());
        }
    }
}
