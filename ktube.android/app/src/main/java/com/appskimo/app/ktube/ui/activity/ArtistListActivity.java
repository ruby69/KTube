package com.appskimo.app.ktube.ui.activity;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Artist;
import com.appskimo.app.ktube.event.OnSelectArtist;
import com.appskimo.app.ktube.service.PrefsService_;
import com.appskimo.app.ktube.service.RestClient;
import com.appskimo.app.ktube.support.EventBusObserver;
import com.appskimo.app.ktube.ui.adapter.ArtistRecyclerViewAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@EActivity(R.layout.activity_artist_list)
@OptionsMenu(R.menu.menu_search)
public class ArtistListActivity extends AppCompatActivity {
    @ViewById(R.id.toolbar) Toolbar toolbar;
    @ViewById(R.id.refreshLayout) SwipeRefreshLayout refreshLayout;
    @ViewById(R.id.recyclerView) RecyclerView recyclerView;

    @OptionsMenuItem(R.id.menuSearch) MenuItem menuSearch;

    @Bean ArtistRecyclerViewAdapter recyclerViewAdapter;
    @Bean RestClient restClient;

    private List<Artist> artists;
    private On<Artist.Collection> on;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Constants.applyLanguage(base, new PrefsService_(base)));
    }

    @AfterInject
    void afterInject() {
        recyclerViewAdapter.setType(Constants.ViewStyle.LARGE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(new EventBusObserver.AtCreateDestroy(this));
    }

    @AfterViews
    void afterViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_artist);

        recyclerView.setAdapter(recyclerViewAdapter);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            load();
        });
        refreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.yellow);

        on = new On<Artist.Collection>().addSuccessListener(response -> manipulate(response));
        load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (artists != null) {
                    final String query = newText.toLowerCase();
                    if (query.trim().length() > 0) {
                        recyclerViewAdapter.reset(filtered(query));
                    } else {
                        recyclerViewAdapter.reset(artists);
                    }
                    recyclerView.scrollToPosition(0);
                }
                return false;
            }

            private List<Artist> filtered(String str) {
                List<Artist> list = new ArrayList<>();
                for(Artist artist : artists) {
                    if (artist.getTags() != null && artist.getTags().toLowerCase(Locale.getDefault()).contains(str)) {
                        list.add(artist);
                    }
                }
                return list;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @UiThread
    void manipulate(Artist.Collection response) {
        if(recyclerView != null && response != null && recyclerViewAdapter != null) {
            artists = response;
            recyclerViewAdapter.reset(response);
            recyclerView.scrollToPosition(0);
        }

        if(refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }

    private void load() {
        if (restClient != null) {
            restClient.fetchArtists(on);
        }
    }

    @Subscribe
    public void onEvent(OnSelectArtist event) {
        ArtistActivity_.intent(this).artist(event.getArtist()).start();
    }

    @Override
    protected void onDestroy() {
        artists = null;
        on = null;

        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.clear();
            recyclerViewAdapter = null;
        }

        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        super.onDestroy();
    }
}