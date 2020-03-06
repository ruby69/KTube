package com.appskimo.app.ktube.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.appskimo.app.ktube.BuildConfig;
import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.SupportLanguage;
import com.appskimo.app.ktube.event.OnSelectLanguage;
import com.appskimo.app.ktube.event.OnYtMusic;
import com.appskimo.app.ktube.service.MiscService;
import com.appskimo.app.ktube.service.PrefsService_;
import com.appskimo.app.ktube.support.EventBusObserver;
import com.appskimo.app.ktube.support.NotiAlarmService;
import com.appskimo.app.ktube.ui.dialog.LanguageDialog;
import com.appskimo.app.ktube.ui.dialog.LanguageDialog_;
import com.appskimo.app.ktube.ui.view.NoticeView_;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;

import io.fabric.sdk.android.Fabric;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    @ViewById(R.id.drawerLayout) DrawerLayout drawerLayout;
    @ViewById(R.id.navigationView) NavigationView navigationView;
    @ViewById(R.id.toolbar) Toolbar toolbar;

    @Bean MiscService miscService;
    @Bean NotiAlarmService notiAlarmService;
    @Pref PrefsService_ prefs;

    private LanguageDialog languageDialog = LanguageDialog_.builder().build();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Constants.applyLanguage(base, new PrefsService_(base)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!BuildConfig.DEBUG) {
            FirebaseAnalytics.getInstance(this);
            Fabric.with(this, new Crashlytics());
        }
        getLifecycle().addObserver(new EventBusObserver.AtResumePause(this));
    }

    @AfterViews
    void afterViews() {
        miscService.initializeMobileAds();
        initNavigationDrawer();
        applyLaunchedCount();
//        notiYtMusicChart();
        miscService.checkVersion(new On<Void>().addSuccessListener(aVoid -> linkPlayStore(getPackageName())));
    }

    private void initNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(this);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View headerLayout = navigationView.getHeaderView(0);
        TextView language = headerLayout.findViewById(R.id.language);
        language.setText(SupportLanguage.valueOf(prefs.userLanguage().get()).getDisplayName());
        language.setOnClickListener(v -> languageDialog.show(getSupportFragmentManager(), LanguageDialog.TAG));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuFavorite) {
            MyActivity_.intent(this).start();
        } else if (id == R.id.menuRadio) {
            RadioActivity_.intent(this).start();
        } else if (id == R.id.menuVideo) {
            VideoActivity_.intent(this).videoGroup(Constants.VideoGroup.VIDEO).start();
        } else if (id == R.id.menuMillion) {
            VideoActivity_.intent(this).videoGroup(Constants.VideoGroup.MILLION).start();
        } else if (id == R.id.menuFancam) {
            VideoActivity_.intent(this).videoGroup(Constants.VideoGroup.FANCAM).start();
        } else if (id == R.id.menuLyrics) {
            VideoActivity_.intent(this).videoGroup(Constants.VideoGroup.LYRIC).start();
        } else if (id == R.id.menuKaraoke) {
            VideoActivity_.intent(this).videoGroup(Constants.VideoGroup.KARAOKE).start();
        } else if (id == R.id.menuArtist) {
            ArtistListActivity_.intent(this).start();
        } else if (id == R.id.menuSearch) {
            SearchActivity_.intent(this).start();
//        } else if (id == R.id.menuMusicChart) {
//            launchOrInstall();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    private void applyLaunchedCount() {
        int count = prefs.appLaunchedCount().get();
        prefs.appLaunchedCount().put(++count);
        prefs.launchedTime().put(new Date().getTime());
        notiAlarmService.reserve();
    }

    @Subscribe
    public void onEvent(OnSelectLanguage event) {
        SupportLanguage supportLanguage = event.getSupportLanguage();
        String current = prefs.userLanguage().get();
        if (SupportLanguage.valueOf(current) != supportLanguage) {
            prefs.userLanguage().put(supportLanguage.getCode());
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    @UiThread
    void linkPlayStore(String packageName) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(getString(R.string.url_store_app), packageName))));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(getString(R.string.url_store_web), packageName))));
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    private String ytMusicPackage = "com.appskimo.app.ytmusic";
    private AlertDialog ytMusicDialog;
    private void notiYtMusicChart() {
        long gap = System.currentTimeMillis() - prefs.musicChartNotiTime().get();
        if(!isInstalled(ytMusicPackage) && prefs.musicChartNotiCount().get() < 5 && gap > Constants.DAY_1) {
            ytMusicDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(R.string.label_noti_music_chart)
                    .setView(NoticeView_.build(this))
                    .setNegativeButton(R.string.label_cancel, (dialog, i) -> dialog.dismiss())
                    .setPositiveButton(R.string.label_confirm, (dialog, i) -> {
                        dialog.dismiss();
                        linkPlayStore(ytMusicPackage);
                    }).create();

            if (!this.isFinishing()) {
                ytMusicDialog.show();
                int notiCount = prefs.musicChartNotiCount().get();
                prefs.musicChartNotiCount().put(++notiCount);
                prefs.musicChartNotiTime().put(System.currentTimeMillis());
            }
        }
    }

    private boolean isInstalled(String packageName){
        return getPackageManager().getLaunchIntentForPackage(packageName) != null;
    }

    @Subscribe
    public void onEvent(OnYtMusic event) {
        linkPlayStore(ytMusicPackage);
        if (ytMusicDialog != null && ytMusicDialog.isShowing()) {
            ytMusicDialog.dismiss();
        }
    }

    private void launchOrInstall() {
        if (isInstalled(ytMusicPackage)) {
            Intent intent = getPackageManager().getLaunchIntentForPackage(ytMusicPackage);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            linkPlayStore(ytMusicPackage);
        }
    }
}
