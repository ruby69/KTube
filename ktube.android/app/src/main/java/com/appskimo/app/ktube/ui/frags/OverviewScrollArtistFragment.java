package com.appskimo.app.ktube.ui.frags;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Artist;
import com.appskimo.app.ktube.event.OnSelectArtist;
import com.appskimo.app.ktube.service.RestClient;
import com.appskimo.app.ktube.support.EventBusObserver;
import com.appskimo.app.ktube.ui.activity.ArtistActivity_;
import com.appskimo.app.ktube.ui.activity.ArtistListActivity_;
import com.appskimo.app.ktube.ui.adapter.ArtistRecyclerViewAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

@EFragment(R.layout.fragment_overview_scroll_artist)
public class OverviewScrollArtistFragment extends Fragment {
    @ViewById(R.id.title) TextView titleView;
    @ViewById(R.id.recyclerView) RecyclerView recyclerView;
    @Bean ArtistRecyclerViewAdapter recyclerViewAdapter;
    @Bean RestClient restClient;

    @FragmentArg("items") ArrayList<Artist> items;
    @FragmentArg("titleResId") int titleResId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(new EventBusObserver.AtStartStop(this));
    }

    @AfterViews
    void afterViews() {
        recyclerViewAdapter.setType(Constants.ViewStyle.SMALL);
        recyclerView.setAdapter(recyclerViewAdapter);

        titleView.setText(titleResId);
        manipulate();
    }

    @UiThread
    void manipulate() {
        if (recyclerView != null && recyclerViewAdapter != null && items != null) {
            recyclerViewAdapter.reset(items);
            recyclerView.scrollToPosition(0);
        }
    }

    @Click(R.id.more)
    void onClickMore() {
        ArtistListActivity_.intent(getActivity()).start();
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

    @Subscribe
    public void onEvent(OnSelectArtist event) {
        ArtistActivity_.intent(this).artist(event.getArtist()).start();
    }
}