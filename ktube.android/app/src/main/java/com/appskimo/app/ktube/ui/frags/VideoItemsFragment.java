package com.appskimo.app.ktube.ui.frags;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Loadable;
import com.appskimo.app.ktube.domain.Page;
import com.appskimo.app.ktube.domain.Video;
import com.appskimo.app.ktube.event.OnSelectVideo;
import com.appskimo.app.ktube.service.MiscService;
import com.appskimo.app.ktube.service.RestClient;
import com.appskimo.app.ktube.support.EventBusObserver;
import com.appskimo.app.ktube.ui.activity.PlayerRestActivity_;
import com.appskimo.app.ktube.ui.adapter.VideoRecyclerViewAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

@EFragment(R.layout.fragment_video_items)
public class VideoItemsFragment extends Fragment implements Loadable {
    @ViewById(R.id.refreshLayout) SwipeRefreshLayout refreshLayout;
    @ViewById(R.id.recyclerView) RecyclerView recyclerView;

    @Bean MiscService miscService;
    @Bean RestClient restClient;
    @Bean VideoRecyclerViewAdapter recyclerViewAdapter;

    @FragmentArg("videoDomain") Constants.VideoDomain videoDomain;

    private Page<Video> lastPage = null;
    private On<Page<Video>> on;

    @AfterInject
    void afterInject() {
        recyclerViewAdapter.setPageLoader(this);
        recyclerViewAdapter.setVideoDomain(videoDomain);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(new EventBusObserver.AtStartStop(this));
    }

    @AfterViews
    void afterViews() {
        recyclerView.setAdapter(recyclerViewAdapter);

        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            lastPage = null;
            load();
        });
        refreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.yellow);

        on = new On<Page<Video>>().addSuccessListener(this::manipulate);
        load();
    }

    @Subscribe
    public void onEvent(OnSelectVideo event) {
        if (videoDomain == event.getVideoDomain()) {
            miscService.showAdDialog(getActivity(), event.getVideo().getTitle(), R.string.label_continue, (dialog, i) -> PlayerRestActivity_.intent(getActivity()).video(event.getVideo()).videoDomain(videoDomain.clone(Constants.ViewStyle.MEDIUM)).start());
        }
    }

    @UiThread
    void manipulate(Page<Video> response) {
        if (recyclerView != null && recyclerViewAdapter != null && response != null) {
            List<Video> items = response.getContents();
            if (response.getPage() == 1) {
                recyclerViewAdapter.reset(items);
                recyclerView.scrollToPosition(0);
            } else {
                recyclerViewAdapter.add(items);
            }
            lastPage = response;
        }

        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void load() {
        if (restClient != null) {
            if (lastPage == null) {
                restClient.fetchYoutubeVideos(videoDomain, 1, on); // to the first-page
                return;
            }

            if (lastPage.getPage() < lastPage.getTotalPages()) {
                restClient.fetchYoutubeVideos(videoDomain, lastPage.getPage() + 1, on); // to the next-page
            }
        }
    }

    @Override
    public void onDestroyView() {
        lastPage = null;
        on = null;

        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.clear();
            recyclerViewAdapter.setPageLoader(null);
            recyclerViewAdapter = null;
        }

        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        super.onDestroyView();
    }
}
