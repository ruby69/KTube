package com.appskimo.app.ktube.ui.frags;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Video;
import com.appskimo.app.ktube.event.OnSelectVideo;
import com.appskimo.app.ktube.service.MiscService;
import com.appskimo.app.ktube.service.RestClient;
import com.appskimo.app.ktube.support.EventBusObserver;
import com.appskimo.app.ktube.ui.activity.PlayerRestActivity_;
import com.appskimo.app.ktube.ui.adapter.VideoRecyclerViewAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

@EFragment(R.layout.fragment_overview_scroll_video)
public class OverviewScrollVideoFragment extends Fragment {
    @ViewById(R.id.recyclerView) RecyclerView recyclerView;

    @Bean MiscService miscService;
    @Bean RestClient restClient;
    @Bean VideoRecyclerViewAdapter recyclerViewAdapter;

    @FragmentArg("items") ArrayList<Video> items;
    @FragmentArg("videoDomain") Constants.VideoDomain videoDomain;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(new EventBusObserver.AtStartStop(this));
    }

    @AfterViews
    void afterViews() {
        recyclerViewAdapter.setVideoDomain(videoDomain);
        recyclerView.setAdapter(recyclerViewAdapter);
        manipulate();
    }

    @Subscribe
    public void onEvent(OnSelectVideo event) {
        if (videoDomain == event.getVideoDomain()) {
            miscService.showAdDialog(getActivity(), event.getVideo().getTitle(), R.string.label_continue, (dialog, i) -> PlayerRestActivity_.intent(getActivity()).video(event.getVideo()).videoDomain(videoDomain.clone(Constants.ViewStyle.MEDIUM)).start());
        }
    }

    @UiThread
    void manipulate() {
        if (recyclerView != null && recyclerViewAdapter != null && items != null) {
            recyclerViewAdapter.reset(items);
            recyclerView.scrollToPosition(0);
        }
    }

    @Override
    public void onDestroyView() {
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.clear();
            recyclerViewAdapter = null;
        }

        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        super.onDestroyView();
    }
}