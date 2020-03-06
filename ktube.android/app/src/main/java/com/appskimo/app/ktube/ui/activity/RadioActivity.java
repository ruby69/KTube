package com.appskimo.app.ktube.ui.activity;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.RadioStation;
import com.appskimo.app.ktube.event.OnSelectRadioStation;
import com.appskimo.app.ktube.service.MiscService;
import com.appskimo.app.ktube.service.PrefsService_;
import com.appskimo.app.ktube.service.RadioPlayerService;
import com.appskimo.app.ktube.service.RestClient;
import com.appskimo.app.ktube.support.EventBusObserver;
import com.appskimo.app.ktube.support.RadioService_;
import com.appskimo.app.ktube.ui.adapter.RadioRecyclerViewAdapter;
import com.google.android.material.appbar.AppBarLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DrawableRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EActivity(R.layout.activity_radio)
public class RadioActivity extends AppCompatActivity {
    @ViewById(R.id.appBarLayout) AppBarLayout appBarLayout;
    @ViewById(R.id.toolbar) Toolbar toolbar;
    @ViewById(R.id.refreshLayout) SwipeRefreshLayout refreshLayout;
    @ViewById(R.id.recyclerView) RecyclerView recyclerView;
    @ViewById(R.id.control) ImageButton control;

    @Bean RadioRecyclerViewAdapter recyclerViewAdapter;
    @Bean RestClient restClient;
    @Bean RadioPlayerService radioPlayerService;
    @Bean MiscService miscService;
    @Pref PrefsService_ prefs;

    @DrawableRes(R.drawable.ic_stop_black_24dp) Drawable stopIcon;
    @DrawableRes(R.drawable.ic_play_arrow_black_24dp) Drawable playIcon;
    @ColorRes(android.R.color.white) int whiteColor;

    private RadioStation selectedRadioStation;
    private On<List<RadioStation>> on;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Constants.applyLanguage(base, new PrefsService_(base)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(new EventBusObserver.AtCreateDestroy(this));
    }

    @AfterViews
    void afterViews() {
        initIcons();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_radio);

        recyclerView.setAdapter(recyclerViewAdapter);

        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            load();
        });
        refreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.yellow);

        on = new On<List<RadioStation>>().addSuccessListener(this::manipulate);
        load();
        adjustControl(radioPlayerService.isPlaying());
        if (radioPlayerService.isPlaying()) {
            control.setVisibility(View.VISIBLE);
        }
    }

    private void initIcons() {
        stopIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        playIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
    }

    @UiThread
    void manipulate(List<RadioStation> response) {
        if(recyclerView != null && recyclerViewAdapter != null && response != null && response.size() > 0) {
            manipulateSelectedRadioStation(response);
            recyclerViewAdapter.reset(response);
            recyclerView.scrollToPosition(0);
        }

        if(refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }

    private void manipulateSelectedRadioStation(List<RadioStation> list) {
        if(list != null && list.size() > 0) {
            long lastRadioStationUid = prefs.lastRadioStationUid().getOr(0L);
            if(lastRadioStationUid > 0) {
                for(RadioStation radioStation : list) {
                    if(radioStation.getRadioStationUid().longValue() == lastRadioStationUid) {
                        selectedRadioStation = radioStation;
                        break;
                    }
                }
            } else {
                List<RadioStation> items = new ArrayList<>(list);
                Collections.shuffle(items);
                selectedRadioStation = items.get(0);
            }
        }
    }

    private void load() {
        if (restClient != null) {
            restClient.fetchRadioStations(on);
        }
    }

    @Subscribe
    public void onEvent(OnSelectRadioStation event) {
        selectedRadioStation = event.getRadioStation();
        appBarLayout.setExpanded(true, true);
        getSupportActionBar().setTitle(selectedRadioStation.getTitle());
        control.setVisibility(View.VISIBLE);

        if(radioPlayerService.isPlaying()) {
            radioPlayerService.setControlCallback(controlCallback);
            RadioService_.intent(getApplicationContext()).stop();
        }
    }

    private RadioPlayerService.ControlCallable controlCallback = new RadioPlayerService.ControlCallable() {
        @Override
        public void playCall() {
            adjustControl(true);
        }

        @Override
        public void stopCall() {
            adjustControl(false);
        }

        @Override
        public void finishCall() {
            enableControl(true);
        }
    };

    @Click(R.id.control)
    void clickControl() {
        if(control.isEnabled()) {
            enableControl(false);
            radioPlayerService.setControlCallback(controlCallback);
            if(radioPlayerService.isPlaying()) {
                RadioService_.intent(getApplicationContext()).stop();
            } else {
                RadioService_.start(getApplicationContext(), selectedRadioStation);
                miscService.showAdDialog(this, new On<Void>().addSuccessListener(aVoid -> {}));
            }
        }
    }

    @UiThread
    void adjustControl(boolean play) {
        if(control != null) {
            if(play) {
                control.setImageDrawable(stopIcon);
            } else {
                control.setImageDrawable(playIcon);
            }
        }
    }

    @UiThread
    void enableControl(boolean enabled) {
        if (control != null) {
            control.setEnabled(enabled);
        }
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

    @Override
    public void onDestroy() {
        selectedRadioStation = null;
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
