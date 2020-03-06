package com.appskimo.app.ktube.ui.frags;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Artist;
import com.appskimo.app.ktube.event.OnMyArtist;
import com.appskimo.app.ktube.service.KtubeService;
import com.appskimo.app.ktube.support.EventBusObserver;
import com.appskimo.app.ktube.ui.adapter.ArtistRecyclerViewAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

@EFragment(R.layout.fragment_my_artist)
public class MyArtistFragment extends Fragment {
    @ViewById(R.id.refreshLayout) SwipeRefreshLayout refreshLayout;
    @ViewById(R.id.recyclerView) RecyclerView recyclerView;

    @Bean KtubeService ktubeService;
    @Bean ArtistRecyclerViewAdapter recyclerViewAdapter;

    private int scrollPosition;
    private On<List<Artist>> on;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(new EventBusObserver.AtStartStop(this));
    }

    @AfterViews
    void afterViews() {
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setType(Constants.ViewStyle.LARGE);

        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            load();
        });
        refreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.yellow);

        on = new On<List<Artist>>().addSuccessListener(artists -> manipulate(artists)).addCompleteListener(artists -> finishRefresh());
        load();
    }

    public void load() {
        ktubeService.findFavoriteArtists(on);
    }

    @UiThread
    void manipulate(List<Artist> items) {
        if (recyclerView != null && recyclerViewAdapter != null && items != null) {
            recyclerViewAdapter.reset(items);
            recyclerView.scrollToPosition(scrollPosition);
            scrollPosition = 0; // reset to zero
        }
    }

    @Subscribe
    public void onEvent(OnMyArtist event) {
        if (event.getSeq() != null && recyclerViewAdapter != null) {
            scrollPosition = (int) (recyclerViewAdapter.getItemCount() - event.getSeq());
        }
        load();
    }

    @UiThread
    void finishRefresh() {
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onDestroyView() {
        on = null;

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

