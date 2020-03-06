package com.appskimo.app.ktube.support;

import android.app.Activity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import lombok.Getter;

@GlideModule
public final class KtubeGlideModule extends AppGlideModule {

    public static class OnActivity implements LifecycleObserver {
        private Activity activity;
        @Getter private GlideRequests glideRequests;

        public OnActivity(Activity activity) {
            this.activity = activity;
            glideRequests = GlideApp.with(activity);
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public void onStart(){
            if (glideRequests != null && !activity.isFinishing() && !activity.isDestroyed()) {
                glideRequests.onStart();
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onStop() {
            if (glideRequests != null && !activity.isFinishing() && !activity.isDestroyed()) {
                glideRequests.onStop();
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy() {
            if (glideRequests != null && !activity.isFinishing() && !activity.isDestroyed()) {
                glideRequests.onDestroy();
            }
        }
    }

    public static class OnFragment implements LifecycleObserver {
        private Fragment fragment;
        @Getter private GlideRequests glideRequests;

        public OnFragment(Fragment fragment) {
            this.fragment = fragment;
            glideRequests = GlideApp.with(fragment);
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public void onStart(){
            if (glideRequests != null && !fragment.getActivity().isFinishing() && !fragment.getActivity().isDestroyed()) {
                glideRequests.onStart();
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onStop() {
            if (glideRequests != null && !fragment.getActivity().isFinishing() && !fragment.getActivity().isDestroyed()) {
                glideRequests.onStop();
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy() {
            if (glideRequests != null && !fragment.getActivity().isFinishing() && !fragment.getActivity().isDestroyed()) {
                try {
                    glideRequests.onDestroy();
                } catch (Exception e) {
                }
            }
        }
    }
}
